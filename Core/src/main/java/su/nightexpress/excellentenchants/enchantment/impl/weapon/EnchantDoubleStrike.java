package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.particle.SimpleParticle;
import su.nexmedia.engine.utils.MessageUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;

public class EnchantDoubleStrike extends ExcellentEnchant implements Chanced, CombatEnchant {

    public static final String ID = "double_strike";

    private ChanceImplementation chanceImplementation;

    public EnchantDoubleStrike(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.LOW);
        this.getDefaults().setDescription("<lang:enchantment.g2213swo." + this.getId() + ".desc:"
                + Placeholders.ENCHANTMENT_CHANCE + ">");
        // "enchantment.g2213swo.your_enchant_id.desc": "%1$s%% chance to inflict double damage."
        // %1$s = enchantment chance

        this.getDefaults().setLevelMax(4);
        this.getDefaults().setTier(1.0);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this,
            "4.0 + " + Placeholders.ENCHANTMENT_LEVEL + " * 0.8");
    }

    @Override
    public @NotNull ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.isAvailableToUse(damager)) return false;
        if (!this.checkTriggerChance(level)) return false;

        e.setDamage(e.getDamage() * 2D);

        if (this.hasVisualEffects()) {
            SimpleParticle.of(Particle.EXPLOSION_NORMAL).play(victim.getEyeLocation(), 0.25, 0.15, 15);
            MessageUtil.playSound(victim.getLocation(), Sound.ENTITY_GENERIC_EXPLODE); // akiranya
        }
        return true;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
