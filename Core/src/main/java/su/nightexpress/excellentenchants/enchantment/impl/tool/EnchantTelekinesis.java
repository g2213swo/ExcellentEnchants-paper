package su.nightexpress.excellentenchants.enchantment.impl.tool;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockDropEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.type.FitItemType;
import su.nightexpress.excellentenchants.enchantment.util.EnchantDropContainer;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EnchantTelekinesis extends ExcellentEnchant implements Chanced, BlockDropEnchant {

    public static final String ID = "telekinesis";

    private ChanceImplementation chanceImplementation;

    public EnchantTelekinesis(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.LOWEST);
        this.getDefaults().setDescription("<lang:enchantment.g2213swo." + this.getId() + ".desc>");
        // "enchantment.g2213swo.your_enchant_id.desc": "Moves all block loot directly to your inventory."
        this.getDefaults().setLevelMax(1);
        this.getDefaults().setTier(0.75);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this, "100");
    }

    @Override
    public @NotNull ChanceImplementation getChanceImplementation() {
        return this.chanceImplementation;
    }

    @Override
    public @NotNull FitItemType[] getFitItemTypes() {
        return new FitItemType[]{FitItemType.TOOL};
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean onDrop(@NotNull BlockDropItemEvent e, @NotNull EnchantDropContainer dropContainer, @NotNull Player player, @NotNull ItemStack item, int level) {
        if (!this.isAvailableToUse(player)) return false;
        if (!this.checkTriggerChance(level)) return false;

        List<ItemStack> drops = new ArrayList<>();
        drops.addAll(e.getItems().stream().map(Item::getItemStack).toList());
        drops.addAll(dropContainer.getDrops());
        drops.removeIf(Objects::isNull);
        drops.forEach(drop -> PlayerUtil.addItem(player, drop));

        dropContainer.getDrops().clear();
        e.getItems().clear();

        return true;
    }
}
