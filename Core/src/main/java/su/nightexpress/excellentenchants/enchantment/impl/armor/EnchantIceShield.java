package su.nightexpress.excellentenchants.enchantment.impl.armor;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.particle.SimpleParticle;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.meta.Potioned;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.impl.meta.PotionImplementation;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;

public class EnchantIceShield extends ExcellentEnchant implements Chanced, Potioned, CombatEnchant {

    public static final String ID = "ice_shield";

    private ChanceImplementation chanceImplementation;
    private PotionImplementation potionImplementation;

    public EnchantIceShield(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
        this.getDefaults().setLevelMax(3);
        this.getDefaults().setTier(0.1);
//        this.getDefaults().setDescription(Placeholders.ENCHANTMENT_CHANCE + "% chance to freeze and apply " + Placeholders.ENCHANTMENT_POTION_TYPE + " " + Placeholders.ENCHANTMENT_POTION_LEVEL + " (" + Placeholders.ENCHANTMENT_POTION_DURATION + "s.) on attacker.");
        this.getDefaults().setDescription("<lang:enchantment.g2213swo." + this.getId() + ".desc:"
                + Placeholders.ENCHANTMENT_CHANCE + ":" + Placeholders.ENCHANTMENT_POTION_TYPE + ":" + Placeholders.ENCHANTMENT_POTION_LEVEL + ":" + Placeholders.ENCHANTMENT_POTION_DURATION + ">");
        // "enchantment.g2213swo.ice_shield.desc": "%1$s%% chance to freeze and apply %2$s %3$s (%4$s sec.) on attacker."
        // %1$s = chance, %2$s = potion type, %3$s = potion level, %4$s = potion duration
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this, "25.0 * " + Placeholders.ENCHANTMENT_LEVEL);
        this.potionImplementation = PotionImplementation.create(this, PotionEffectType.SLOW, false,
            "3.0 + " + Placeholders.ENCHANTMENT_LEVEL,
            Placeholders.ENCHANTMENT_LEVEL);
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR_TORSO;
    }

    @Override
    public @NotNull Chanced getChanceImplementation() {
        return this.chanceImplementation;
    }

    @Override
    public @NotNull PotionImplementation getPotionImplementation() {
        return potionImplementation;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.isAvailableToUse(victim)) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (!this.addEffect(damager, level)) return false;

        damager.setFreezeTicks(damager.getMaxFreezeTicks());

        if (this.hasVisualEffects()) {
            SimpleParticle.of(Particle.BLOCK_CRACK, Material.ICE.createBlockData())
                .play(damager.getEyeLocation(), 0.25, 0.1, 20);
        }
        return true;
    }
}
