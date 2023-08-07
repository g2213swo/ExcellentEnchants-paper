package su.nightexpress.excellentenchants.enchantment.impl.armor;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;

import java.util.Set;

public class EnchantElementalProtection extends ExcellentEnchant {

    public static final String ID = "elemental_protection";
    public static final String PLACEHOLDER_PROTECTION_AMOUNT = "%enchantment_protection_amount%";
    public static final String PLACEHOLDER_PROTECTION_CAPACITY = "%enchantment_protection_capacity%";

    private static final Set<EntityDamageEvent.DamageCause> DAMAGE_CAUSES = Set.of(
        EntityDamageEvent.DamageCause.POISON, EntityDamageEvent.DamageCause.WITHER,
        EntityDamageEvent.DamageCause.MAGIC, EntityDamageEvent.DamageCause.FREEZE,
        EntityDamageEvent.DamageCause.LIGHTNING);

    private EnchantScaler protectionAmount;
    private double protectionCapacity;
    private boolean protectionAsModifier;

    public EnchantElementalProtection(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
        this.getDefaults().setLevelMax(5);
        this.getDefaults().setTier(0.2);
//        this.getDefaults().setDescription("Reduces Poison, Magic, Wither, Lightning, Freeze damage by " + PLACEHOLDER_PROTECTION_AMOUNT + ".");
        this.getDefaults().setDescription("<lang:enchantment.g2213swo." + this.getId() + ".desc:"
                + PLACEHOLDER_PROTECTION_AMOUNT + ">");
        // "enchantment.g2213swo.elemental_protection.desc": "Reduces Poison, Magic, Wither, Lightning, Freeze damage by %1$s."
        // %1$s = protection amount
    }

    @Override
    public void loadSettings() {
        super.loadSettings();

        this.protectionAmount = EnchantScaler.read(this, "Settings.Protection.Amount",
            "0.05 * " + Placeholders.ENCHANTMENT_LEVEL,
            "How protection the enchantment will have?");
        this.protectionCapacity = JOption.create("Settings.Protection.Capacity", 1D,
            "Maximal possible protection value from all armor pieces together.").read(cfg);
        this.protectionAsModifier = JOption.create("Settings.Protection.As_Modifier", false,
            "When 'true' damage will be reduced by a percent of protection value.",
            "When 'false' damage will be reduced by a plain protection value.").read(cfg);

        this.addPlaceholder(PLACEHOLDER_PROTECTION_AMOUNT, level -> NumberUtil.format(this.getProtectionAmount(level)));
        this.addPlaceholder(PLACEHOLDER_PROTECTION_CAPACITY, level -> NumberUtil.format(this.getProtectionCapacity()));
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR;
    }

    public double getProtectionAmount(int level) {
        return this.protectionAmount.getValue(level);
    }

    public double getProtectionCapacity() {
        return this.protectionCapacity;
    }

    public boolean isProtectionAsModifier() {
        return this.protectionAsModifier;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (!DAMAGE_CAUSES.contains(e.getCause())) return;
        if (!(e.getEntity() instanceof LivingEntity entity)) return;
        if (!this.isAvailableToUse(entity)) return;

        double protectionAmount = 0D;
        for (ItemStack armor : EnchantUtils.getEnchantedEquipment(entity).values()) {
            int level = EnchantUtils.getLevel(armor, this);
            if (level <= 0) continue;

            protectionAmount += this.getProtectionAmount(level);
            this.consumeCharges(armor);
        }

        if (protectionAmount <= 0D) return;
        if (protectionAmount > this.getProtectionCapacity()) {
            protectionAmount = this.getProtectionCapacity();
        }

        if (this.isProtectionAsModifier()) {
            e.setDamage(Math.max(0, e.getDamage() * (1D - protectionAmount)));
        } else {
            e.setDamage(Math.max(0, e.getDamage() - protectionAmount));
        }
    }
}
