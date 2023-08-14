package su.nightexpress.excellentenchants.enchantment.impl.armor;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
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

/**
 * 威慑
 * 被攻击时概率对对手产生强力击退效果
 * {'velocity': 'level*1.5'}
 */
public class EnchantAnnihilate extends ExcellentEnchant implements Chanced, CombatEnchant {

    public static final String ID = "annihilate";

    private static final String PLACEHOLDER_VELOCITY = "%enchantment_velocity%";

    private EnchantScaler velocity;

    private ChanceImplementation chanceImplementation;

    public EnchantAnnihilate(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
        this.getDefaults().setLevelMax(5);
        this.getDefaults().setTier(0.6);
        this.getDefaults().setDescription("<lang:enchantment.g2213swo." + this.getId() + ".desc:"
                + Placeholders.ENCHANTMENT_CHANCE + ":" + PLACEHOLDER_VELOCITY + ">");
        // "enchantment.g2213swo.annihilate.desc": "%1$s%% Chance to apply %2$s Knockback to the attacker."
    }

    @Override
    public void loadSettings() {
        super.loadSettings();

        this.velocity = EnchantScaler.read(this, "Settings.Velocity", "1.5 * " + Placeholders.ENCHANTMENT_LEVEL,
                "Velocity of the knockback effect.");

        this.chanceImplementation = ChanceImplementation.create(this, "10.0 * " + Placeholders.ENCHANTMENT_LEVEL);
        this.addPlaceholder(PLACEHOLDER_VELOCITY, level -> NumberUtil.format(this.getVelocity(level)));
    }


    public double getVelocity(int level) {
        return velocity.getValue(level);
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR;
    }

    @NotNull
    @Override
    public Chanced getChanceImplementation() {
        return this.chanceImplementation;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.isAvailableToUse(damager)) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (damager.getUniqueId().equals(victim.getUniqueId())) return false;
        if (damager instanceof Projectile) {
            if (((Projectile) damager).getShooter() instanceof LivingEntity) {
                damager = (LivingEntity) ((Projectile) damager).getShooter();
            }
        }

        Location damagerLocation = damager.getLocation();
        // 获取击退抗性
        AttributeInstance knockbackResistanceAttribute = victim.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        double resistance = (knockbackResistanceAttribute != null) ? knockbackResistanceAttribute.getValue() : 0.0;
        // 考虑击退抗性来获取实际的击退力度
        double adjustedKnockbackStrength = getVelocity(level) * (1 - resistance);  // 减少的击退效果与抗性成正比
        Vector direction = damagerLocation.getDirection();
        // 将方向乘以-1，使其反向
        direction = direction.multiply(-1);
        direction.multiply(adjustedKnockbackStrength);
        direction.setY(0.8);
        // 应用这个新的运动向量给玩家，使其被击退
        damager.setVelocity(direction);
        damagerLocation.getWorld().playSound(damagerLocation, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0F, 1.0F);
        victim.getWorld().spawnParticle(Particle.SWEEP_ATTACK, victim.getLocation(), 1, 0.0D, 0.0D, 0.0D, 0.0D);


        return true;
    }


}
