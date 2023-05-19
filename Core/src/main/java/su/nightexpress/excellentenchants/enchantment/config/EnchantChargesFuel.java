package su.nightexpress.excellentenchants.enchantment.config;

import cc.mewcraft.mewcore.item.api.PluginItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents charges fuel used by enchantments.
 */
public class EnchantChargesFuel {

    private final PluginItem<?> pluginItemFuel;
    private final ItemStack vanillaItemFuel;

    public static @NotNull EnchantChargesFuel read(@NotNull ExcellentEnchant enchant, @NotNull String path, @Nullable String... comments) {
        JYML config = enchant.getConfig();
        config.remove(path + ".Material"); // remove old default config
        if (!config.isString(path + ".External")) {
            config.addMissing(path + ".General.Material", Material.LAPIS_LAZULI);
        }
        if (comments != null) {
            config.setComments(path, comments);
        }
        PluginItem<?> pluginItem = config.getPluginItem(path + ".External");
        return pluginItem != null ? new EnchantChargesFuel(pluginItem) : new EnchantChargesFuel(config.getItem(path + ".General"));
    }

    public EnchantChargesFuel(final @NotNull PluginItem<?> pluginItemFuel) {
        this.pluginItemFuel = pluginItemFuel;
        this.vanillaItemFuel = null;
    }

    public EnchantChargesFuel(final @NotNull ItemStack vanillaItemFuel) {
        this.pluginItemFuel = null;
        this.vanillaItemFuel = vanillaItemFuel;
    }

    public @NotNull Optional<PluginItem<?>> getPluginItem() {
        return Optional.ofNullable(pluginItemFuel);
    }

    public @NotNull Optional<ItemStack> getGeneralItem() {
        return Optional.ofNullable(vanillaItemFuel);
    }

    public @NotNull ItemStack getItem() {
        if (this.getPluginItem().isPresent())
            return Objects.requireNonNull(this.getPluginItem().get().createItemStack());
        if (this.getGeneralItem().isPresent())
            return this.getGeneralItem().get();
        return new ItemStack(Material.AIR); // better implementation?
    }

    public boolean isFuel(@NotNull ItemStack item) {
        if (this.getPluginItem().isPresent())
            return this.getPluginItem().get().matches(item);
        if (this.getGeneralItem().isPresent())
            return this.getGeneralItem().get().isSimilar(item);
        return false;
    }

}
