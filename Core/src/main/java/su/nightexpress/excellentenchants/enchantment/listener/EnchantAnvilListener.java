package su.nightexpress.excellentenchants.enchantment.listener;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nexmedia.engine.utils.MessageUtil;
import su.nexmedia.engine.utils.PDCUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.ExcellentEnchantsAPI;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class EnchantAnvilListener extends AbstractListener<ExcellentEnchants> {

    private static final NamespacedKey RECHARGED = new NamespacedKey(ExcellentEnchantsAPI.PLUGIN, "item.recharged");

    public EnchantAnvilListener(@NotNull ExcellentEnchants plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAnvilRename(PrepareAnvilEvent e) {
        AnvilInventory inventory = e.getInventory();

        ItemStack first = inventory.getFirstItem();
        ItemStack second = inventory.getSecondItem();
        ItemStack result = e.getResult();

        if (first == null) first = new ItemStack(Material.AIR);
        if (second == null) second = new ItemStack(Material.AIR);
        if (result == null) result = new ItemStack(Material.AIR);

        // Check if source item is an enchantable single item.
        if (first.getType().isAir() || first.getAmount() > 1 || !EnchantUtils.isEnchantable(first)) return;

        if (this.handleRename(e, first, second, result)) return;
        if (this.handleRecharge(e, first, second, result)) return;

        this.handleEnchantMerging(e, first, second, result);
    }

    private boolean handleRename(
        @NotNull PrepareAnvilEvent e,
        @NotNull ItemStack first,
        @NotNull ItemStack second,
        @NotNull ItemStack result
    ) {
        if (!second.getType().isAir() && (second.getType() == first.getType() || second.getType() == Material.ENCHANTED_BOOK))
            return false;
        if (result.getType() != first.getType())
            return false;

        ItemStack resultCopy = result.clone();
        EnchantUtils.getExcellents(first).forEach((hasEnch, hasLevel) -> {
            EnchantUtils.add(resultCopy, hasEnch, hasLevel, true);
        });
        e.setResult(resultCopy);
        return true;
    }

    private boolean handleRecharge(
        @NotNull PrepareAnvilEvent e,
        @NotNull ItemStack first,
        @NotNull ItemStack second,
        @NotNull ItemStack result
    ) {
        if (second.getType().isAir()) return false;

        Set<ExcellentEnchant> chargeables = EnchantUtils.getExcellents(first).keySet().stream()
            .filter(en -> en.isChargesEnabled() && en.isChargesFuel(second) && !en.isFullOfCharges(first))
            .collect(Collectors.toSet());
        if (chargeables.isEmpty()) return false;

        ItemStack resultNew = first.clone();

        int count = 0;
        while (count < second.getAmount() && !chargeables.stream().allMatch(en -> en.isFullOfCharges(resultNew))) {
            chargeables.forEach(enchant -> EnchantUtils.rechargeCharges(resultNew, enchant));
            count++;
        }

        PDCUtil.set(resultNew, RECHARGED, count);
        e.setResult(resultNew);
        this.plugin.runTask(task -> e.getInventory().setRepairCost(chargeables.size()));
        return true;
    }

    private boolean handleEnchantMerging(
        @NotNull PrepareAnvilEvent e,
        @NotNull ItemStack first,
        @NotNull ItemStack second,
        @NotNull ItemStack result
    ) {
        // Validate items in the first two slots.
        if (second.getType().isAir() || second.getAmount() > 1 || !EnchantUtils.isEnchantable(second)) return false;
        if (first.getType() == Material.ENCHANTED_BOOK && second.getType() != first.getType()) return false;

        ItemStack copy = result.getType().isAir() ? first.clone() : result.clone();
        Map<ExcellentEnchant, Integer> enchantments = EnchantUtils.getExcellents(first);
        Map<ExcellentEnchant, Integer> charges = new HashMap<>(enchantments.keySet().stream().collect(Collectors.toMap(k -> k, v -> v.getCharges(first))));
        AtomicInteger repairCost = new AtomicInteger(e.getInventory().getRepairCost());

        // Merge only if it's Item + Item, Item + Enchanted book or Enchanted Book + Enchanted Book
        if (second.getType() == Material.ENCHANTED_BOOK || second.getType() == first.getType()) {
            EnchantUtils.getExcellents(second).forEach((enchant, level) -> {
                enchantments.merge(enchant, level, (oldLvl, newLvl) -> (oldLvl.equals(newLvl)) ? (Math.min(enchant.getMaxLevel(), oldLvl + 1)) : (Math.max(oldLvl, newLvl)));
                charges.merge(enchant, enchant.getCharges(second), Integer::sum);
            });
        }

        // Recalculate operation cost depends on enchantments merge cost.
        enchantments.forEach((enchant, level) -> {
            if (EnchantUtils.add(copy, enchant, level, false)) {
                repairCost.addAndGet(enchant.getAnvilMergeCost(level));
                EnchantUtils.setCharges(copy, enchant, charges.getOrDefault(enchant, 0));
            }
        });

        if (first.equals(copy)) return false;

        e.setResult(copy);

        // NMS ContainerAnvil will set level cost to 0 right after calling the event, need 1 tick delay.
        this.plugin.runTask(task -> e.getInventory().setRepairCost(repairCost.get()));
        return true;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onClickAnvil(InventoryClickEvent e) {
        if (!(e.getInventory() instanceof AnvilInventory inventory)) return;
        if (e.getRawSlot() != 2) return;

        ItemStack item = e.getCurrentItem();
        if (item == null) return;

        int count = PDCUtil.getInt(item, RECHARGED).orElse(0);
        if (count == 0) return;

        Player player = (Player) e.getWhoClicked();
        if (player.getLevel() < inventory.getRepairCost()) return;

        player.setLevel(player.getLevel() - inventory.getRepairCost());
        PDCUtil.remove(item, RECHARGED);
        e.getView().setCursor(item);
        e.setCancelled(false);

        MessageUtil.playSound(player, Sound.BLOCK_ENCHANTMENT_TABLE_USE);

        ItemStack second = inventory.getSecondItem();
        if (second != null && !second.getType().isAir()) {
            second.setAmount(second.getAmount() - count);
        }
        inventory.setFirstItem(null);
        inventory.setResult(null);
    }
}
