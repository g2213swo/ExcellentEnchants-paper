package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;

public class EnchantThunder extends ExcellentEnchant implements Chanced, CombatEnchant {

    public static final String ID = "thunder";

    private static final String META_NO_ITEM_DAMAGE = "noItemDamage";

    private boolean inThunderstormOnly;
    private ChanceImplementation chanceImplementation;

    public EnchantThunder(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
        this.getDefaults().setDescription("<lang:enchantment.g2213swo." + this.getId() + ".desc:"
                + Placeholders.ENCHANTMENT_CHANCE + ">");
        // "enchantment.g2213swo.your_enchant_id.desc": "%1$s%% chance to summon lightning to enemy on hit."
        // %1$s = enchantment chance

        this.getDefaults().setLevelMax(3);
        this.getDefaults().setTier(0.3);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this,
            "10.0 * " + Placeholders.ENCHANTMENT_LEVEL);
        this.inThunderstormOnly = JOption.create("Settings.During_Thunderstorm_Only", false,
            "When 'true' the enchantment will be triggered only if there is an active thunderstorm in the world.").read(this.cfg);
    }

    @Override
    public @NotNull ChanceImplementation getChanceImplementation() {
        return this.chanceImplementation;
    }

    public boolean isInThunderstormOnly() {
        return this.inThunderstormOnly;
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.isAvailableToUse(damager)) return false;
        if (this.isInThunderstormOnly() && !victim.getWorld().isThundering()) return false;
        if (victim.getLocation().getBlock().getLightFromSky() != 15) return false;
        if (!this.checkTriggerChance(level)) return false;

        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
            if (victim.isDead()) return;
            victim.setNoDamageTicks(0);
            victim.getWorld().strikeLightning(victim.getLocation()).setMetadata(META_NO_ITEM_DAMAGE, new FixedMetadataValue(this.plugin, true));
        });

        return true;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDamage(EntityDamageByEntityEvent e) {
        if (!e.getDamager().hasMetadata(META_NO_ITEM_DAMAGE)) return;
        if (!(e.getEntity() instanceof Item item)) return;

        e.setCancelled(true);
        item.setFireTicks(0);
    }
}
