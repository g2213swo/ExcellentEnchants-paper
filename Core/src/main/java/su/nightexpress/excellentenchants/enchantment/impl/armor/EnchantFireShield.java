package su.nightexpress.excellentenchants.enchantment.impl.armor;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;

public class EnchantFireShield extends ExcellentEnchant implements Chanced, CombatEnchant {

    public static final String ID = "fire_shield";
    public static final String PLACEHOLDER_FIRE_DURATION = "%enchantment_fire_duration%";

    private EnchantScaler fireDuration;
    private ChanceImplementation chanceImplementation;

    public EnchantFireShield(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
        this.getDefaults().setLevelMax(3);
        this.getDefaults().setTier(0.4);
//        this.getDefaults().setDescription(Placeholders.ENCHANTMENT_CHANCE + "% chance to ignite the attacker for " + PLACEHOLDER_FIRE_DURATION + "s.");
        this.getDefaults().setDescription("<lang:enchantment.g2213swo." + this.getId() + ".desc:"
                + Placeholders.ENCHANTMENT_CHANCE + ":" + PLACEHOLDER_FIRE_DURATION + ">");
        // "enchantment.g2213swo.fire_shield.desc": "%1$s%% chance to ignite the attacker for %2$s seconds."
        // %1$s = chance, %2$s = fire duration
    }

    @Override
    public void loadSettings() {
        super.loadSettings();

        this.chanceImplementation = ChanceImplementation.create(this,
            Placeholders.ENCHANTMENT_LEVEL + " * 15.0");
        this.fireDuration = EnchantScaler.read(this, "Settings.Fire.Duration",
            "2.5 * " + Placeholders.ENCHANTMENT_LEVEL,
            "Sets the fire duration (in seconds).",
            "If entity's current fire ticks amount is less than this value, it will be set to this value.",
            "If entity's current fire ticks amount is greater than this value, it won't be changed.");

        this.addPlaceholder(PLACEHOLDER_FIRE_DURATION, level -> NumberUtil.format(this.getFireDuration(level)));
    }

    @Override
    public @NotNull ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR;
    }

    public double getFireDuration(int level) {
        return this.fireDuration.getValue(level);
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent e,
        @NotNull LivingEntity damager, @NotNull LivingEntity victim,
        @NotNull ItemStack weapon, int level) {
        if (!this.isAvailableToUse(victim)) return false;
        if (!this.checkTriggerChance(level)) return false;

        int ticksToSet = (int) (this.getFireDuration(level) * 20);
        int ticksHas = damager.getFireTicks();
        if (ticksHas >= ticksToSet) return false;

        damager.setFireTicks(ticksToSet);
        return true;
    }
}
