package su.nightexpress.excellentenchants.enchantment.impl.bow;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;

public class EnchantBomber extends ExcellentEnchant implements Chanced, BowEnchant {

    public static final String ID = "bomber";
    public static final String PLACEHOLDER_FUSE_TICKS = "%enchantment_fuse_ticks%";

    private EnchantScaler fuseTicks;
    private ChanceImplementation chanceImplementation;

    public EnchantBomber(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.HIGHEST);
        this.getDefaults().setLevelMax(3);
        this.getDefaults().setTier(0.7);
        this.getDefaults().setConflicts(
                EnchantEnderBow.ID, EnchantGhast.ID,
                EnchantExplosiveArrows.ID, EnchantPoisonedArrows.ID, EnchantConfusingArrows.ID,
                EnchantWitheredArrows.ID, EnchantElectrifiedArrows.ID, EnchantDragonfireArrows.ID,
                EnchantHover.ID,
                Enchantment.ARROW_FIRE.getKey().getKey(),
                Enchantment.ARROW_KNOCKBACK.getKey().getKey(),
                Enchantment.ARROW_DAMAGE.getKey().getKey()
        );
//        this.getDefaults().setDescription(Placeholders.ENCHANTMENT_CHANCE + "% chance to launch TNT that explodes in " + PLACEHOLDER_FUSE_TICKS + "s.");
        // "enchantment.g2213swo.bomber.desc": "%1$s % chance to launch TNT that explodes in %2$s s."
        // %1$s = potion type, %2$s = potion level
        this.getDefaults().setDescription("<lang:enchantment.g2213swo." + this.getId() + ".desc:" + Placeholders.ENCHANTMENT_CHANCE + ":" + PLACEHOLDER_FUSE_TICKS + ">");
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this,
                "5.0 * " + Placeholders.ENCHANTMENT_LEVEL);
        this.fuseTicks = EnchantScaler.read(this, "Settings.Fuse_Ticks",
                "100 - " + Placeholders.ENCHANTMENT_LEVEL + " * 10",
                "Sets fuse ticks (before it will explode) for the launched TNT.");

        this.addPlaceholder(PLACEHOLDER_FUSE_TICKS, level -> NumberUtil.format((double) this.getFuseTicks(level) / 20D));
    }

    @Override
    public @NotNull ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    public int getFuseTicks(int level) {
        return (int) this.fuseTicks.getValue(level);
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BOW;
    }

    @Override
    public boolean onShoot(@NotNull EntityShootBowEvent e, @NotNull LivingEntity shooter, @NotNull ItemStack bow, int level) {
        if (!this.isAvailableToUse(shooter)) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (!(e.getProjectile() instanceof Projectile projectile)) return false;

        TNTPrimed primed = projectile.getWorld().spawn(projectile.getLocation(), TNTPrimed.class);
        primed.setVelocity(projectile.getVelocity().multiply(e.getForce() * 1.25));
        primed.setFuseTicks(this.getFuseTicks(level));
        primed.setSource(shooter);
        e.setProjectile(primed);
        return true;
    }

    @Override
    public boolean onHit(@NotNull ProjectileHitEvent e, @NotNull Projectile projectile, @NotNull ItemStack bow, int level) {
        return false;
    }

    @Override
    public boolean onDamage(@NotNull EntityDamageByEntityEvent e, @NotNull Projectile projectile, @NotNull LivingEntity shooter, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
