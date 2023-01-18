package su.nightexpress.excellentenchants.manager.object;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecorationAndState;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.*;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PDCUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.config.Lang;
import su.nightexpress.excellentenchants.manager.EnchantRegister;

import java.util.*;

public class EnchantListGUI extends AbstractMenu<ExcellentEnchants> {

    private final ItemStack enchantIcon;
    private final int[] enchantSlots;

    private final NamespacedKey keyLevel;
    private final Map<String, Map<Integer, ItemStack>> iconCache;

    public EnchantListGUI(@NotNull ExcellentEnchants plugin) {
        super(plugin, JYML.loadOrExtract(plugin, "gui.enchants.yml"), "");
        this.keyLevel = new NamespacedKey(plugin, "list_display_level");
        this.iconCache = new HashMap<>();

        this.enchantIcon = cfg.getItem("Enchantments.Icon");
        this.enchantSlots = cfg.getIntArray("Enchantments.Slots");

        MenuClick click = (p, type, e) -> {
            if (type instanceof MenuItemType type2) {
                switch (type2) {
                    case PAGE_NEXT -> this.open(p, this.getPage(p) + 1);
                    case PAGE_PREVIOUS -> this.open(p, this.getPage(p) - 1);
                    case CLOSE -> p.closeInventory();
                }
            }
        };

        for (String sId : cfg.getSection("Content")) {
            MenuItem menuItem = cfg.getMenuItem("Content." + sId);
            if (menuItem.getType() != null) {
                menuItem.setClickHandler(click);
            }
            this.addItem(menuItem);
        }
    }

    private ItemStack getEnchantIcon(@NotNull ExcellentEnchant enchant, int level) {
        return this.iconCache
            .computeIfAbsent(enchant.getId(), k -> new HashMap<>())
            .computeIfAbsent(level, k -> this.buildEnchantIcon(enchant, level));
    }

    @NotNull
    private ItemStack buildEnchantIcon(@NotNull ExcellentEnchant enchant, int level) {
        final ItemStack icon = this.enchantIcon.clone();

        // Override the conflicts placeholder display to make it in a list.
        final List<Component> conflicts;
        if (enchant.getConflicts().isEmpty()) {
            // This enchantment has no conflicts
            conflicts = List.of(Component.text()
                .append(Component.text("▸ ").color(NamedTextColor.RED))
                .append(plugin.getMessage(Lang.OTHER_NONE).getLocalizedComponent())
                .asComponent()
            );
        } else {
            // This enchantment has some conflicts
            conflicts = enchant.getConflicts().stream()
                .filter(Objects::nonNull)
                .map(LangManager::getEnchantment)
                .map(string -> Component.text()
                    .append(Component.text("▸ ").color(NamedTextColor.RED))
                    .append(Component.text(string).color(NamedTextColor.DARK_RED))
                    .asComponent())
                .toList();
        }

        icon.editMeta(meta -> {
            // Replace placeholders in the icon
            ItemUtil.replacePlaceholderListComponent(meta, Placeholders.ENCHANTMENT_CONFLICTS, conflicts);
            ItemUtil.replacePlaceholderListString(meta, Placeholders.ENCHANTMENT_DESCRIPTION, Config.formatDescription(enchant.getDescription(level)));
            ItemUtil.replaceNameAndLore(meta, enchant.formatString(level)); // This has to be the last as the line of code above creates new placeholder

            // Fix the issue where the italic deco would be back when switching the level
            TextDecorationAndState removeItalic = TextDecoration.ITALIC.withState(TextDecoration.State.FALSE);
            List<Component> lore = Objects.requireNonNull(meta.lore(), "lore");
            List<Component> loreWithoutItalic = lore.stream().map(component -> component.applyFallbackStyle(removeItalic)).toList();
            meta.lore(loreWithoutItalic);
            Component nameWithoutItalic = Objects.requireNonNull(meta.displayName(), "displayName").applyFallbackStyle(removeItalic);
            meta.displayName(nameWithoutItalic);
        });

        return icon;
    }

    @Override
    public boolean onPrepare(@NotNull Player player, @NotNull Inventory inventory) {
        int page = this.getPage(player);
        int length = this.enchantSlots.length;
        List<ExcellentEnchant> enchants = new ArrayList<>(EnchantRegister.ENCHANT_REGISTRY.values().stream().sorted(Comparator.comparing(ExcellentEnchant::getName)).toList());
        List<List<ExcellentEnchant>> split = CollectionsUtil.split(enchants, length);

        int pages = split.size();
        if (pages < 1 || pages < page) enchants = Collections.emptyList();
        else enchants = split.get(page - 1);

        int count = 0;
        for (ExcellentEnchant enchant : enchants) {
            ItemStack icon = this.getEnchantIcon(enchant, 1);
            PDCUtil.setData(icon, this.keyLevel, 1);

            MenuClick click = (p, type, e) -> {
                if (!e.isLeftClick()) return;

                ItemStack itemClick = e.getCurrentItem();
                if (itemClick == null) return;

                int levelHas = PDCUtil.getIntData(itemClick, this.keyLevel);
                if (levelHas == 0) return;

                if (++levelHas > enchant.getMaxLevel()) levelHas = enchant.getStartLevel();
                itemClick = this.getEnchantIcon(enchant, levelHas);
                PDCUtil.setData(itemClick, this.keyLevel, levelHas);

                e.setCurrentItem(itemClick);
            };

            MenuItem menuItem = new MenuItemImpl(icon, this.enchantSlots[count++]);
            menuItem.setClickHandler(click);
            this.addItem(player, menuItem);
        }
        this.setPage(player, page, pages);
        return true;
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        return true;
    }
}
