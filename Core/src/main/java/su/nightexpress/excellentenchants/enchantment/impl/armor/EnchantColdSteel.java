package su.nightexpress.excellentenchants.enchantment.impl.armor;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.meta.Potioned;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.impl.meta.PotionImplementation;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;

public class EnchantColdSteel extends ExcellentEnchant implements Chanced, Potioned, CombatEnchant {

    public static final String ID = "cold_steel";

    private ChanceImplementation chanceImplementation;
    private PotionImplementation potionImplementation;

    public EnchantColdSteel(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
        this.getDefaults().setTier(0.3);
        this.getDefaults().setLevelMax(3);
        this.getDefaults().setDescription("<lang:enchantment.g2213swo." + this.getId() + ".desc:"
                + Placeholders.ENCHANTMENT_CHANCE + ":" + Placeholders.ENCHANTMENT_POTION_TYPE + ":"
                + Placeholders.ENCHANTMENT_POTION_LEVEL + ":" + Placeholders.ENCHANTMENT_POTION_DURATION + ">");
        // "enchantment.g2213swo.cold_steel.desc": "%1$s % chance to apply %2$s %3$s (%4$s sec.) on attacker."
        // %1$s = chance, %2$s = potion type, %3$s = potion level, %4$s = potion duration
    }

    @Override
    public void loadSettings() {
        super.loadSettings();

        this.chanceImplementation = ChanceImplementation.create(this,
            "60 + " + Placeholders.ENCHANTMENT_LEVEL + " * 5");
        this.potionImplementation = PotionImplementation.create(this, PotionEffectType.SLOW_DIGGING, false,
            "4 + " + Placeholders.ENCHANTMENT_LEVEL,
            Placeholders.ENCHANTMENT_LEVEL);
    }

    @Override
    public @NotNull ChanceImplementation getChanceImplementation() {
        return this.chanceImplementation;
    }

    @Override
    public @NotNull PotionImplementation getPotionImplementation() {
        return potionImplementation;
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR_TORSO;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.isAvailableToUse(damager)) return false;
        if (!this.checkTriggerChance(level)) return false;

        return this.addEffect(damager, level);
    }
}
