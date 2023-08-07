package su.nightexpress.excellentenchants.enchantment.impl.tool;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockBreakEnchant;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.type.FitItemType;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;

public class EnchantLuckyMiner extends ExcellentEnchant implements Chanced, BlockBreakEnchant {

    public static final String ID = "lucky_miner";
    private static final String PLACEHOLDER_EXP_MODIFIER = "%enchantment_exp_modifier%";

    private EnchantScaler expModifier;
    private ChanceImplementation chanceImplementation;

    public EnchantLuckyMiner(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
        this.getDefaults().setDescription("<lang:enchantment.g2213swo." + this.getId()
                + ".desc:" + Placeholders.ENCHANTMENT_CHANCE + ":" + PLACEHOLDER_EXP_MODIFIER + ">");
        // "enchantment.g2213swo.luckyminer.desc": "%1$s% chance to gain %2$s% more exp from ores."
        // %1$s = Enchantment Chance
        // %2$s = EXP Modifier

        this.getDefaults().setLevelMax(5);
        this.getDefaults().setTier(0.1);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this,
            "30.0 + " + Placeholders.ENCHANTMENT_LEVEL + " * 7.0");
        this.expModifier = EnchantScaler.read(this, "Settings.Exp_Modifier",
            "1.0 + " + Placeholders.ENCHANTMENT_LEVEL + " * 0.5",
            "Exp modifier value. The original exp amount will be multiplied on this value.");

        this.addPlaceholder(PLACEHOLDER_EXP_MODIFIER, level -> NumberUtil.format(this.getExpModifier(level) * 100D - 100D));
    }

    @Override
    public @NotNull ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    public double getExpModifier(int level) {
        return this.expModifier.getValue(level);
    }

    @Override
    public @NotNull FitItemType[] getFitItemTypes() {
        return new FitItemType[]{FitItemType.PICKAXE};
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean onBreak(@NotNull BlockBreakEvent e, @NotNull Player player, @NotNull ItemStack item, int level) {
        if (!this.isAvailableToUse(player)) return false;
        if (!this.checkTriggerChance(level)) return false;

        double expMod = this.getExpModifier(level);
        e.setExpToDrop((int) ((double) e.getExpToDrop() * expMod));
        return true;
    }
}
