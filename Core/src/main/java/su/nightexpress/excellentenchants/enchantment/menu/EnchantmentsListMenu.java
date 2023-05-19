package su.nightexpress.excellentenchants.enchantment.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.AutoPaged;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.click.ClickHandler;
import su.nexmedia.engine.api.menu.click.ItemClick;
import su.nexmedia.engine.api.menu.impl.ConfigMenu;
import su.nexmedia.engine.api.menu.impl.MenuOptions;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.ComponentUtil;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PDCUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.enchantment.EnchantRegistry;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;

import java.util.*;

public class EnchantmentsListMenu extends ConfigMenu<ExcellentEnchants> implements AutoPaged<ExcellentEnchant> {

    private static final String PATH = "/menu/enchants_list.yml";

    private static final String PLACEHOLDER_CONFLICTS = "%conflicts%";
    private static final String PLACEHOLDER_CHARGES = "%charges%";
    private static final String PLACEHOLDER_OBTAINING = "%obtaining%";

    private final ItemStack enchantIcon;
    private final List<String> enchantLoreConflicts;
    private final List<String> enchantLoreCharges;
    private final List<String> enchantLoreObtaining;
    private final int[] enchantSlots;

    private final NamespacedKey keyLevel;
    private final Map<String, Map<Integer, ItemStack>> iconCache;

    public EnchantmentsListMenu(@NotNull ExcellentEnchants plugin) {
        super(plugin, JYML.loadOrExtract(plugin, PATH));
        this.keyLevel = new NamespacedKey(plugin, "list_display_level");
        this.iconCache = new HashMap<>();

        this.enchantIcon = this.cfg.getItem("Enchantments.Icon");
        this.enchantLoreConflicts = this.cfg.getStringList("Enchantments.Lore.Conflicts");
        this.enchantLoreCharges = this.cfg.getStringList("Enchantments.Lore.Charges");
        this.enchantLoreObtaining = this.cfg.getStringList("Enchantments.Lore.Obtaining");
        this.enchantSlots = this.cfg.getIntArray("Enchantments.Slots");

        this.registerHandler(MenuItemType.class)
            .addClick(MenuItemType.CLOSE, (viewer, event) -> plugin.runTask(task -> viewer.getPlayer().closeInventory()))
            .addClick(MenuItemType.PAGE_NEXT, ClickHandler.forNextPage(this))
            .addClick(MenuItemType.PAGE_PREVIOUS, ClickHandler.forPreviousPage(this));

        this.load();
    }

    @Override
    public void clear() {
        super.clear();
        this.iconCache.clear();
    }

    @Override
    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {
        super.onPrepare(viewer, options);
        this.getItemsForPage(viewer).forEach(this::addItem);
    }

    @Override
    public int[] getObjectSlots() {
        return this.enchantSlots;
    }

    @Override
    public @NotNull Comparator<ExcellentEnchant> getObjectSorter() {
        return (o1, o2) -> 0;
    }

    @Override
    public @NotNull List<ExcellentEnchant> getObjects(@NotNull Player player) {
        return new ArrayList<>(EnchantRegistry.getRegistered().stream()
            .sorted(Comparator.comparing(ExcellentEnchant::getName)).toList());
    }

    @Override
    public @NotNull ItemStack getObjectStack(@NotNull Player player, @NotNull ExcellentEnchant enchant) {
        return this.getEnchantIcon(enchant, 1);
    }

    @Override
    public @NotNull ItemClick getObjectClick(@NotNull ExcellentEnchant enchant) {
        return (viewer, event) -> {
            if (!event.isLeftClick()) return;

            ItemStack itemClick = event.getCurrentItem();
            if (itemClick == null) return;

            int levelHas = PDCUtil.getInt(itemClick, this.keyLevel).orElse(0);
            if (levelHas == 0) levelHas = enchant.getStartLevel();

            if (++levelHas > enchant.getMaxLevel()) levelHas = enchant.getStartLevel();
            itemClick = this.getEnchantIcon(enchant, levelHas);
            PDCUtil.set(itemClick, this.keyLevel, levelHas);

            event.setCurrentItem(itemClick);
        };
    }

    private ItemStack getEnchantIcon(@NotNull ExcellentEnchant enchant, int level) {
        return this.iconCache
            .computeIfAbsent(enchant.getId(), k -> new HashMap<>())
            .computeIfAbsent(level, k -> this.buildEnchantIcon(enchant, level));
    }

    private @NotNull ItemStack buildEnchantIcon(@NotNull ExcellentEnchant enchant, int level) {
        ItemStack icon = new ItemStack(this.enchantIcon);
        ItemMeta meta = icon.getItemMeta();
        if (meta == null) return icon;

        List<Component> lore = Optional.ofNullable(meta.lore()).orElseGet(ArrayList::new);

        List<String> conflicts = enchant.getConflicts().isEmpty() ? Collections.emptyList() : new ArrayList<>(this.enchantLoreConflicts);
        List<String> conflictNames = enchant.getConflicts()
            .stream()
            .map(key -> Enchantment.getByKey(NamespacedKey.minecraft(key)))
            .filter(Objects::nonNull)
            .map(LangManager::getEnchantment)
            .toList();
        conflicts = StringUtil.replacePlaceholderList(Placeholders.ENCHANTMENT_NAME, conflicts, conflictNames, true);

        List<String> charges = enchant.isChargesEnabled() ? new ArrayList<>(this.enchantLoreCharges) : Collections.emptyList();

        lore = ComponentUtil.replacePlaceholderList(PLACEHOLDER_CONFLICTS, lore, ComponentUtil.asComponent(conflicts), false);
        lore = ComponentUtil.replacePlaceholderList(PLACEHOLDER_CHARGES, lore, ComponentUtil.asComponent(charges), false);
        lore = ComponentUtil.replacePlaceholderList(PLACEHOLDER_OBTAINING, lore, ComponentUtil.asComponent(this.enchantLoreObtaining), false);
        lore = ComponentUtil.replacePlaceholderList(Placeholders.ENCHANTMENT_DESCRIPTION, lore, ComponentUtil.asComponent(enchant.getDescription()), true);

        meta.lore(lore);

        ItemUtil.removeItalic(meta);
        ItemUtil.replaceNameAndLore(meta, enchant.getPlaceholders(level).replacer());

        icon.setItemMeta(meta);
        return icon;
    }
}
