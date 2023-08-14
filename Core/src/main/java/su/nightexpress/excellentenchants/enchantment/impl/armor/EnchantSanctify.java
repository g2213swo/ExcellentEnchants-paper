package su.nightexpress.excellentenchants.enchantment.impl.armor;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class EnchantSanctify extends ExcellentEnchant implements Chanced, CombatEnchant {

    public static final String ID = "sanctify";

    private List<PotionEffectType> debuffs;

    public EnchantSanctify(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
        this.getDefaults().setLevelMax(3);
        this.getDefaults().setTier(0.3);

        this.getDefaults().setDescription("<lang:enchantment.g2213swo." + getId() + ".desc:" + Placeholders.ENCHANTMENT_CHANCE + ">");
        // "enchantment.g2213swo.sanctify.desc": "%1$s%% chance to cleanse negative effects on hit."
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this, "25.0 * " + Placeholders.ENCHANTMENT_LEVEL);

        List<String> debuffList = JOption.create("Settings.Debuffs", Arrays.asList("BLINDNESS", "CONFUSION", "HARM", "HUNGER", "POISON", "SLOW", "SLOW_DIGGING", "UNLUCK", "WEAKNESS", "WITHER")).read(cfg);

        debuffs = debuffList.stream()
                .map(String::toUpperCase)
                .map(PotionEffectType::getByName)
                .filter(Objects::nonNull)
                .toList();
    }

    private ChanceImplementation chanceImplementation;

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR;
    }

    @NotNull
    @Override
    public Chanced getChanceImplementation() {
        return chanceImplementation;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.isAvailableToUse(victim)) return false;
        if (!this.checkTriggerChance(level)) return false;

        List<PotionEffectType> effectTypes = victim.getActivePotionEffects().stream()
                .map(PotionEffect::getType)
                .filter(debuffs::contains)
                .toList();

        if (effectTypes.isEmpty()) return false;

        effectTypes.forEach(victim::removePotionEffect);

        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.5F);
        victim.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, victim.getLocation(), 10, 0.5, 0.5, 0.5, 0.0);

        return true;
    }
}
