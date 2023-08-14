package su.nightexpress.excellentenchants.enchantment.impl.bow;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.particle.SimpleParticle;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Arrowed;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ArrowImplementation;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;

public class EnchantDragonfireArrows extends ExcellentEnchant implements Chanced, Arrowed, BowEnchant {

    public static final String ID = "dragonfire_arrows";

    public static final String PLACEHOLDER_FIRE_RADIUS = "%enchantment_fire_radius%";
    public static final String PLACEHOLDER_FIRE_DURATION = "%enchantment_fire_duration%";

    private EnchantScaler fireDuration;
    private EnchantScaler fireRadius;

    private ArrowImplementation arrowImplementation;
    private ChanceImplementation chanceImplementation;

    public EnchantDragonfireArrows(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
        this.getDefaults().setDescription("<lang:enchantment.g2213swo." + this.getId() + ".desc:"
                + Placeholders.ENCHANTMENT_CHANCE + ":" + PLACEHOLDER_FIRE_RADIUS + ":" + PLACEHOLDER_FIRE_DURATION + ">");
        // "enchantment.g2213swo.your_enchant_id.desc": "%1$s%% chance to launch a dragonfire arrow (R=%2$s, %3$ss)."
        // %1$s = enchantment chance, %2$s = fire radius, %3$s = fire duration

        this.getDefaults().setLevelMax(3);
        this.getDefaults().setTier(0.7);

        this.getDefaults().setConflicts(
            EnchantEnderBow.ID, EnchantGhast.ID, EnchantHover.ID,
            EnchantInstability.ID, EnchantPoisonedArrows.ID, EnchantConfusingArrows.ID,
            EnchantWitheredArrows.ID, EnchantElectrifiedArrows.ID
        );
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.arrowImplementation = ArrowImplementation.create(this, SimpleParticle.of(Particle.DRAGON_BREATH));
        this.chanceImplementation = ChanceImplementation.create(this,
            "10.0 + " + Placeholders.ENCHANTMENT_LEVEL + " * 5");
        this.fireDuration = EnchantScaler.read(this, "Settings.Fire.Duration",
            "100 * " + Placeholders.ENCHANTMENT_LEVEL,
            "Sets the dragonfire cloud effect duration (in ticks). 20 ticks = 1 second.");
        this.fireRadius = EnchantScaler.read(this, "Settings.Fire.Radius",
            "2.0 + " + Placeholders.ENCHANTMENT_LEVEL,
            "Sets the dragonfire cloud effect radius.");

        this.addPlaceholder(PLACEHOLDER_FIRE_DURATION, level -> NumberUtil.format(this.getFireDuration(level) / 20D));
        this.addPlaceholder(PLACEHOLDER_FIRE_RADIUS, level -> NumberUtil.format(this.getFireRadius(level)));
    }

    @Override
    public @NotNull ArrowImplementation getArrowImplementation() {
        return arrowImplementation;
    }

    @Override
    public @NotNull ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BOW;
    }

    public int getFireDuration(int level) {
        return (int) this.fireDuration.getValue(level);
    }

    public double getFireRadius(int level) {
        return this.fireRadius.getValue(level);
    }

    @Override
    public boolean onShoot(@NotNull EntityShootBowEvent e, @NotNull LivingEntity shooter, @NotNull ItemStack bow, int level) {
        if (!this.isAvailableToUse(shooter)) return false;

        return this.checkTriggerChance(level);
    }

    @Override
    public boolean onHit(@NotNull ProjectileHitEvent e, @NotNull Projectile projectile, @NotNull ItemStack bow, int level) {
        if (!this.isOurProjectile(projectile)) return false;
        if (e.getHitEntity() != null) return false;

        this.createCloud(projectile.getShooter(), projectile.getLocation(), level);
        return true;
    }

    @Override
    public boolean onDamage(@NotNull EntityDamageByEntityEvent e, @NotNull Projectile projectile, @NotNull LivingEntity shooter, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.isOurProjectile(projectile)) return false;

        this.createCloud(shooter, victim.getLocation(), level);
        return true;
    }

    private void createCloud(@Nullable ProjectileSource shooter, @NotNull Location location, int level) {
        World world = location.getWorld();
        if (world == null) return;

        AreaEffectCloud cloud = world.spawn(location, AreaEffectCloud.class);
        cloud.clearCustomEffects();
        cloud.setSource(shooter);
        cloud.setParticle(Particle.DRAGON_BREATH);
        cloud.setRadius((float) this.getFireRadius(level));
        cloud.setDuration(this.getFireDuration(level));
        cloud.setRadiusPerTick((7.0F - cloud.getRadius()) / (float) cloud.getDuration());
        cloud.addCustomEffect(new PotionEffect(PotionEffectType.HARM, 1, 1), true);
    }
}
