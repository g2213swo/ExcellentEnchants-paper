package su.nightexpress.excellentenchants.enchantment.impl.tool;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;

public class EnchantCurseOfBreaking extends ExcellentEnchant implements Chanced {

    public static final String ID = "curse_of_breaking";
    public static final String PLACEHOLDER_DURABILITY_AMOUNT = "%enchantment_durability_amount%";

    private EnchantScaler durabilityAmount;
    private ChanceImplementation chanceImplementation;

    public EnchantCurseOfBreaking(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
        this.getDefaults().setDescription("<lang:enchantment.g2213swo." + this.getId() + ".desc:" +
                Placeholders.ENCHANTMENT_CHANCE + ":" + PLACEHOLDER_DURABILITY_AMOUNT + ">");
        // "enchantment.g2213swo.curse_of_breaking.desc": "%1$s% chance to consume extra %2$s durability points."
        // %1$s = chance to consume extra durability
        // %2$s = amount of extra durability points consumed

        this.getDefaults().setLevelMax(3);
        this.getDefaults().setTier(0D);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this,
            "10.0 * " + Placeholders.ENCHANTMENT_LEVEL);
        this.durabilityAmount = EnchantScaler.read(this, "Settings.Durability_Amount",
            Placeholders.ENCHANTMENT_LEVEL,
            "Amount of durability points to be taken from the item.");

        this.addPlaceholder(PLACEHOLDER_DURABILITY_AMOUNT, level -> NumberUtil.format(this.getDurabilityAmount(level)));
    }

    @Override
    public boolean isCursed() {
        return true;
    }

    @Override
    public @NotNull ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    public int getDurabilityAmount(int level) {
        return (int) this.durabilityAmount.getValue(level);
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BREAKABLE;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemDurability(PlayerItemDamageEvent e) {
        Player player = e.getPlayer();
        if (!this.isAvailableToUse(player)) return;

        ItemStack item = e.getItem();
        int level = EnchantUtils.getLevel(item, this);

        if (level < 1) return;
        if (!this.checkTriggerChance(level)) return;

        int durabilityAmount = this.getDurabilityAmount(level);
        if (durabilityAmount <= 0) return;

        e.setDamage(e.getDamage() + durabilityAmount);
    }
}
