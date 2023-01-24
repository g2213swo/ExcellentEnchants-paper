package su.nightexpress.excellentenchants.hook.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.GameMode;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.ComponentUtil;
import su.nightexpress.excellentenchants.ExcellentEnchantsAPI;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.enchantment.EnchantManager;

import java.util.*;

public class ProtocolHook {

    private static final Style FALLBACK_STYLE = Style.style(config -> {
        config.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
        config.colorIfAbsent(NamedTextColor.GRAY);
    });

    private static boolean isRegistered = false;

    public static void setup() {
        if (isRegistered) return;

        ProtocolManager manager = ProtocolLibrary.getProtocolManager();

        manager.addPacketListener(new PacketAdapter(ExcellentEnchantsAPI.PLUGIN, ListenerPriority.LOWEST, PacketType.Play.Client.SET_CREATIVE_SLOT) {
            // modifying the items received from creative players
            @Override public void onPacketReceiving(final PacketEvent event) {
                PacketContainer packet = event.getPacket();

                ItemStack item = packet.getItemModifier().read(0);
                ItemStack reverted = revert(item);
                packet.getItemModifier().write(0, reverted);
            }
        });

        manager.addPacketListener(new PacketAdapter(ExcellentEnchantsAPI.PLUGIN, ListenerPriority.HIGHEST, PacketType.Play.Server.SET_SLOT) {
            // write lore when the server sets an item in a slot
            @Override public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();

                boolean isCreative = event.getPlayer().getGameMode() == GameMode.CREATIVE;
                ItemStack item = packet.getItemModifier().read(0);
                ItemStack updated = update(item, isCreative);
                packet.getItemModifier().write(0, updated);
            }
        });

        manager.addPacketListener(new PacketAdapter(ExcellentEnchantsAPI.PLUGIN, ListenerPriority.HIGHEST, PacketType.Play.Server.WINDOW_ITEMS) {
            // write lore when the server sets a window of items
            @Override public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();

                boolean isCreative = event.getPlayer().getGameMode() == GameMode.CREATIVE;
                List<ItemStack> items = packet.getItemListModifier().readSafely(0);
                items.replaceAll(item -> update(item, isCreative));
                packet.getItemListModifier().write(0, items);
            }
        });

        manager.addPacketListener(new PacketAdapter(ExcellentEnchantsAPI.PLUGIN, ListenerPriority.HIGHEST, PacketType.Play.Server.OPEN_WINDOW_MERCHANT) {
            // write lore when the server sends the merchant recipes
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();

                List<MerchantRecipe> list = new ArrayList<>();
                boolean isCreative = event.getPlayer().getGameMode() == GameMode.CREATIVE;
                packet.getMerchantRecipeLists().read(0).forEach(recipe -> {
                    ItemStack result = update(recipe.getResult(), isCreative);
                    if (result == null) return;

                    MerchantRecipe r2 = new MerchantRecipe(result, recipe.getUses(), recipe.getMaxUses(), recipe.hasExperienceReward(), recipe.getVillagerExperience(), recipe.getPriceMultiplier(), recipe.getDemand(), recipe.getSpecialPrice());
                    r2.setIngredients(recipe.getIngredients());
                    list.add(r2);
                });
                packet.getMerchantRecipeLists().write(0, list);
            }
        });

        isRegistered = true;
    }

    private static @Nullable ItemStack update(@Nullable ItemStack item, boolean isCreative) {
        if (item == null || item.getType().isAir()) return item;

        if (!item.hasItemMeta()) return item; // if this is simple item
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        Map<ExcellentEnchant, Integer> enchants = EnchantManager.getExcellentEnchantments(item);
        if (enchants.isEmpty()) return item; // if no enchants on this item
        List<Component> lore = Optional.ofNullable(meta.lore()).orElseGet(ArrayList::new);

        // Sort enchantments by tier
        // enchants = enchants.entrySet().stream()
        //     .sorted(Comparator.comparing(e -> e.getKey().getTier().getPriority()))
        //     .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (old, nev) -> nev, LinkedHashMap::new));

        // Add verbose enchantment description lore (except for creative mode)
        if (Config.ENCHANTMENTS_DESCRIPTION_ENABLED.get() && !isCreative) {
            enchants.forEach((enchant, level) -> {
                List<Component> desc = new ArrayList<>(ComponentUtil.asComponent(enchant.formatDescription(level)));
                desc.replaceAll(component -> component.applyFallbackStyle(FALLBACK_STYLE));
                lore.addAll(0, desc);
            });
        }

        // Add vanilla-like enchantment lore
        enchants.forEach((enchant, level) -> {
            int charges = EnchantManager.getEnchantmentCharges(item, enchant);
            lore.add(0, enchant.displayName(level, charges));
        });

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static @Nullable ItemStack revert(@Nullable ItemStack item) {
        if (item == null || item.getType().isAir()) return item;

        if (!item.hasItemMeta()) return item; // if this is simple item
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return item; // if no lore on this item

        List<Component> lore = Objects.requireNonNull(meta.lore());
        int size = EnchantManager.getExcellentEnchantments(item).size();
        if (size == 0) return item; // if no custom enchantment on this item
        List<Component> reverted = lore.subList(size, lore.size());
        meta.lore(reverted.isEmpty() ? null : reverted); // remove the custom enchantment lore

        item.setItemMeta(meta);
        return item;
    }
}
