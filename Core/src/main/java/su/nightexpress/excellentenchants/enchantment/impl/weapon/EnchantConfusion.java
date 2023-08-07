package su.nightexpress.excellentenchants.enchantment.impl.weapon;

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

public class EnchantConfusion extends ExcellentEnchant implements Chanced, Potioned, CombatEnchant {

    public static final String ID = "confusion";

    private ChanceImplementation chanceImplementation;
    private PotionImplementation potionImplementation;

    public EnchantConfusion(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
        this.getDefaults().setDescription("<lang:enchantment.g2213swo." + this.getId() + ".desc:"
                + Placeholders.ENCHANTMENT_CHANCE + ":" + Placeholders.ENCHANTMENT_POTION_TYPE + ":" + Placeholders.ENCHANTMENT_POTION_LEVEL + ":" + Placeholders.ENCHANTMENT_POTION_DURATION + ">");
        // "enchantment.g2213swo.your_enchant_id.desc": "%1$s%% chance to apply %2$s %3$s (%4$ss.) on hit."
        // %1$s = enchantment chance, %2$s = potion type, %3$s = potion level, %4$s = potion duration

        this.getDefaults().setLevelMax(3);
        this.getDefaults().setTier(0.1);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this,
            "15.0 * " + Placeholders.ENCHANTMENT_LEVEL);
        this.potionImplementation = PotionImplementation.create(this, PotionEffectType.CONFUSION, false,
            "5.0 + " + Placeholders.ENCHANTMENT_LEVEL + " * 1.5",
            Placeholders.ENCHANTMENT_LEVEL);
    }

    @Override
    public @NotNull ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    @Override
    public @NotNull PotionImplementation getPotionImplementation() {
        return potionImplementation;
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.isAvailableToUse(damager)) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (!this.addEffect(victim, level)) return false;

        if (this.hasVisualEffects()) {
            SimpleParticle.of(Particle.ITEM_CRACK, new ItemStack(Material.ROTTEN_FLESH))
                .play(victim.getEyeLocation(), 0.25, 0.1, 30);
        }
        return true;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
