package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.DeathEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;

public class EnchantNimble extends ExcellentEnchant implements Chanced, DeathEnchant {

    public static final String ID = "nimble";

    private ChanceImplementation chanceImplementation;

    public EnchantNimble(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.LOWEST);
        this.getDefaults().setDescription("<lang:enchantment.g2213swo." + this.getId() + ".desc>");
        // "enchantment.g2213swo.your_enchant_id.desc": "Moves all mob's loot directly to your inventory."
        this.getDefaults().setLevelMax(1);
        this.getDefaults().setTier(0.4);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this);
    }

    @Override
    public @NotNull ChanceImplementation getChanceImplementation() {
        return this.chanceImplementation;
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean onKill(@NotNull EntityDeathEvent e, @NotNull LivingEntity entity, @NotNull Player killer, int level) {
        if (!this.isAvailableToUse(entity)) return false;
        if (!this.checkTriggerChance(level)) return false;

        e.getDrops().forEach(item -> PlayerUtil.addItem(killer, item));
        e.getDrops().clear();
        return true;
    }

    @Override
    public boolean onDeath(@NotNull EntityDeathEvent e, @NotNull LivingEntity entity, int level) {
        return false;
    }
}
