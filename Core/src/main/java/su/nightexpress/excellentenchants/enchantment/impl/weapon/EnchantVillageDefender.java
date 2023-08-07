package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.Particle;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Illager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.particle.SimpleParticle;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;

public class EnchantVillageDefender extends ExcellentEnchant implements CombatEnchant {

    public static final String ID = "village_defender";
    public static final String PLACEHOLDER_DAMAGE_AMOUNT = "%enchantment_damage_amount%";

    private boolean damageMultiplier;
    private EnchantScaler damageAmount;

    public EnchantVillageDefender(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
        this.getDefaults().setDescription("<lang:enchantment.g2213swo." + this.getId() + ".desc:"
                + PLACEHOLDER_DAMAGE_AMOUNT + ">");
        // "enchantment.g2213swo.your_enchant_id.desc": "Inflicts %1$s more damage to all pillagers."
        // %1$s = damage amount

        this.getDefaults().setLevelMax(5);
        this.getDefaults().setTier(0.1);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();

        this.damageAmount = EnchantScaler.read(this, "Settings.Damage.Formula",
            "0.5 * " + Placeholders.ENCHANTMENT_LEVEL,
            "Amount of additional damage.");

        this.damageMultiplier = JOption.create("Settings.Damage.As_Modifier", false,
            "When 'true' the 'Damage.Formula' will work as a multiplier to the original damage.").read(cfg);

        this.addPlaceholder(PLACEHOLDER_DAMAGE_AMOUNT, level -> NumberUtil.format(this.getDamageAddict(level)));
    }

    public double getDamageAddict(int level) {
        return this.damageAmount.getValue(level);
    }

    public boolean isDamageMultiplier() {
        return damageMultiplier;
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.isAvailableToUse(damager)) return false;
        if (!(victim instanceof Illager)) return false;

        double damageAdd = this.getDamageAddict(level);
        double damageHas = e.getDamage();
        double damageFinal = this.isDamageMultiplier() ? (damageHas * damageAdd) : (damageHas + damageAdd);

        e.setDamage(damageFinal);

        if (this.hasVisualEffects()) {
            SimpleParticle.of(Particle.VILLAGER_ANGRY).play(victim.getEyeLocation(), 0.25, 0.1, 30);
        }
        return true;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
