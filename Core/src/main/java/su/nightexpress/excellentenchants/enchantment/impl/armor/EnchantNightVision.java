package su.nightexpress.excellentenchants.enchantment.impl.armor;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Potioned;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.PotionImplementation;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;

public class EnchantNightVision extends ExcellentEnchant implements Potioned, PassiveEnchant {

    public static final String ID = "night_vision";

    private PotionImplementation potionImplementation;

    public EnchantNightVision(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
        this.getDefaults().setLevelMax(1);
        this.getDefaults().setTier(0.7);
//        this.getDefaults().setDescription("Grants permanent " + Placeholders.ENCHANTMENT_POTION_TYPE + " " + Placeholders.ENCHANTMENT_POTION_LEVEL + " effect.");
        this.getDefaults().setDescription("<lang:enchantment.g2213swo." + this.getId() + ".desc:"
                + Placeholders.ENCHANTMENT_POTION_TYPE + ":" + Placeholders.ENCHANTMENT_POTION_LEVEL + ">");
        // "enchantment.g2213swo.night_vision.desc": "Grants permanent %1$s %2$s effect."
        // %1$s = potion type, %2$s = potion level
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.potionImplementation = PotionImplementation.create(this, PotionEffectType.NIGHT_VISION, true);
    }

    @Override
    public @NotNull PotionImplementation getPotionImplementation() {
        return potionImplementation;
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR_HEAD;
    }

    @Override
    public boolean onTrigger(@NotNull LivingEntity entity, @NotNull ItemStack item, int level) {
        if (!this.isAvailableToUse(entity)) return false;

        return this.addEffect(entity, level);
    }
}