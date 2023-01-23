package su.nightexpress.excellentenchants.hook.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
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
import java.util.stream.Collectors;

public class ProtocolHook {

    private static final Style FALLBACK_STYLE = Style.empty()
        .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        .colorIfAbsent(NamedTextColor.GRAY);

    private static boolean isRegistered = false;

    public static void setup() {
        if (isRegistered) return;

        ProtocolManager manager = ProtocolLibrary.getProtocolManager();

        manager.addPacketListener(new PacketAdapter(ExcellentEnchantsAPI.PLUGIN, PacketType.Play.Server.SET_SLOT) {
            @Override public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();

                // Packet is buggy with creative mode, we just don't handle it
                if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;

                ItemStack item = packet.getItemModifier().read(0);
                packet.getItemModifier().write(0, update(item));
            }
        });

        manager.addPacketListener(new PacketAdapter(ExcellentEnchantsAPI.PLUGIN, PacketType.Play.Server.WINDOW_ITEMS) {
            @Override public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();

                // Packet is buggy with creative mode, we just don't handle it
                if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;

                List<ItemStack> items = packet.getItemListModifier().readSafely(0);
                items.replaceAll(ProtocolHook::update);
                packet.getItemListModifier().write(0, items);
            }
        });

        manager.addPacketListener(new PacketAdapter(ExcellentEnchantsAPI.PLUGIN, PacketType.Play.Server.OPEN_WINDOW_MERCHANT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();

                List<MerchantRecipe> list = new ArrayList<>();
                packet.getMerchantRecipeLists().read(0).forEach(recipe -> {
                    ItemStack result = update(recipe.getResult());
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

    private static @Nullable ItemStack update(@Nullable ItemStack item) {
        if (item == null || item.getType().isAir()) return item;

        if (!item.hasItemMeta()) return item; // if this is simple item
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        Map<ExcellentEnchant, Integer> enchants = EnchantManager.getExcellentEnchantments(item);
        if (enchants.isEmpty()) return item; // if no enchants on this item
        List<Component> lore = Optional.ofNullable(meta.lore()).orElseGet(ArrayList::new);

        // Sort enchantments by tier
        enchants = enchants.entrySet().stream()
            .sorted(Comparator.comparing(e -> e.getKey().getTier().getPriority()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (old, nev) -> nev, LinkedHashMap::new));

        // Add verbose enchantment description lore
        if (Config.ENCHANTMENTS_DESCRIPTION_ENABLED.get()) {
            enchants.forEach((enchant, level) -> {
                List<Component> desc = new ArrayList<>(ComponentUtil.asComponent(enchant.formatDescription(level)));
                desc.replaceAll(component -> component.applyFallbackStyle(FALLBACK_STYLE));
                lore.addAll(0, desc);
            });
        }

        // Add basic enchantment lore (mimic vanilla one)
        enchants.forEach((enchant, level) -> {
            int charges = EnchantManager.getEnchantmentCharges(item, enchant);
            lore.add(0, enchant.displayName(level, charges));
        });

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
