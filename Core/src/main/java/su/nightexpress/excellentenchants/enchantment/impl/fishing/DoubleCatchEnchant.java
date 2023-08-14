package su.nightexpress.excellentenchants.enchantment.impl.fishing;

import net.momirealms.customfishing.api.event.FishResultEvent;
import net.momirealms.customfishing.api.event.MiniGameStartEvent;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.FishingEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;

public class DoubleCatchEnchant extends ExcellentEnchant implements FishingEnchant, Chanced {

    public static final String ID = "double_catch";

    private ChanceImplementation chanceImplementation;

    public DoubleCatchEnchant(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.LOWEST);
        this.getDefaults().setDescription("<lang:enchantment.g2213swo." + this.getId() + ".desc:" + Placeholders.ENCHANTMENT_CHANCE + ">");
        // "enchantment.g2213swo.double_catch.desc": "Increases amount of caught item by x2 with %1$s%% chance."
        // %1$s = chance
        this.getDefaults().setLevelMax(3);
        this.getDefaults().setTier(0.7);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this,
                "10.0 * " + Placeholders.ENCHANTMENT_LEVEL);
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.FISHING_ROD;
    }

    @Override
    public @NotNull ChanceImplementation getChanceImplementation() {
        return this.chanceImplementation;
    }

    @Override
    public boolean onFishingResult(@NotNull FishResultEvent event, @NotNull ItemStack item, @NotNull ItemStack result, int level) {
        if (!this.isAvailableToUse(event.getPlayer())) return false;
        if (!this.checkTriggerChance(level)) return false;

        event.setDouble(true);

        return true;
    }

    @Override
    public boolean onFishingStart(@NotNull MiniGameStartEvent event, @NotNull ItemStack item, int level) {
        return false;
    }

    @Override
    public boolean onFishing(@NotNull PlayerFishEvent event, @NotNull ItemStack item, int level) {
        return false;
    }
}
