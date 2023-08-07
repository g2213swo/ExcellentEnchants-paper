package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.EntityUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;

public class EnchantTemper extends ExcellentEnchant implements CombatEnchant {

    public static final String ID = "temper";
    public static final String PLACEHOLDER_DAMAGE_AMOUNT = "%enchantment_damage_amount%";
    public static final String PLACEHOLDER_DAMAGE_CAPACITY = "%enchantment_damage_capacity%";
    public static final String PLACEHOLDER_HEALTH_POINT = "%enchantment_health_point%";

    private EnchantScaler damageAmount;
    private EnchantScaler damageCapacity;
    private EnchantScaler healthPoint;

    public EnchantTemper(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
        this.getDefaults().setDescription("<lang:enchantment.g2213swo." + this.getId() + ".desc:"
                + PLACEHOLDER_DAMAGE_AMOUNT + ":" + PLACEHOLDER_DAMAGE_CAPACITY + ":" + PLACEHOLDER_HEALTH_POINT + ">");
        // "enchantment.g2213swo.your_enchant_id.desc": "Inflicts %1$s%% (max. %2$s%%) more damage for each %3$s hearts missing."
        // %1$s = damage amount percentage, %2$s = damage capacity percentage, %3$s = health points

        this.getDefaults().setLevelMax(5);
        this.getDefaults().setTier(0.3);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.damageAmount = EnchantScaler.read(this, "Settings.Damage.Amount",
            "0.01 * " + Placeholders.ENCHANTMENT_LEVEL,
            "On how much (in percent) the damage will be increased per each Health Point?");
        this.damageCapacity = EnchantScaler.read(this, "Settings.Damage.Capacity", "2.0",
            "Maximal possible value for the Damage.Amount.");
        this.healthPoint = EnchantScaler.read(this, "Settings.Health.Point", "0.5",
            "For how much every missing hearts damage will be increased?");

        this.addPlaceholder(PLACEHOLDER_DAMAGE_AMOUNT, level -> NumberUtil.format(this.getDamageAmount(level) * 100D));
        this.addPlaceholder(PLACEHOLDER_DAMAGE_CAPACITY, level -> NumberUtil.format(this.getDamageCapacity(level) * 100D));
        this.addPlaceholder(PLACEHOLDER_HEALTH_POINT, level -> NumberUtil.format(this.getHealthPoint(level)));
    }

    public double getDamageAmount(int level) {
        return this.damageAmount.getValue(level);
    }

    public double getDamageCapacity(int level) {
        return this.damageCapacity.getValue(level);
    }

    public double getHealthPoint(int level) {
        return this.healthPoint.getValue(level);
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.isAvailableToUse(damager)) return false;

        double healthPoint = this.getHealthPoint(level);
        double healthHas = damager.getHealth();
        double healthMax = EntityUtil.getAttribute(damager, Attribute.GENERIC_MAX_HEALTH);
        double healthDiff = healthMax - healthHas;
        if (healthHas >= healthMax || healthDiff < healthPoint) return false;

        int pointAmount = (int) (healthDiff / healthPoint);
        if (pointAmount == 0) return false;

        double damageAmount = this.getDamageAmount(level);
        double damageCap = this.getDamageCapacity(level);
        double damageFinal = Math.min(damageCap, 1D + damageAmount * pointAmount);

        e.setDamage(e.getDamage() * damageFinal);
        return true;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
