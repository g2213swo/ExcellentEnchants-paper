package su.nightexpress.excellentenchants.enchantment.impl.tool;

import org.bukkit.block.Block;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
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
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
import su.nightexpress.excellentenchants.hook.impl.NoCheatPlusHook;

import java.util.List;

public class EnchantBlastMining extends ExcellentEnchant implements Chanced, BlockBreakEnchant {

    public static final String ID = "blast_mining";
    public static final String PLACEHOLDER_EXPLOSION_POWER = "%enchantment_explosion_power%";

    private static final String META_EXPLOSION_SOURCE = ID + "_explosion_source";
    private static final String META_EXPLOSION_MINED = ID + "_explosion_mined";

    private EnchantScaler explosionPower;
    private EnchantScaler minBlockStrength;

    private ChanceImplementation chanceImplementation;

    public EnchantBlastMining(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
        this.getDefaults().setDescription("<lang:enchantment.g2213swo." + this.getId() + ".desc:" + Placeholders.ENCHANTMENT_CHANCE + ">");
        // "enchantment.g2213swo.blast_mining.desc": "%1$s % chance to mine blocks by explosion."
        // %1$s = chance to mine by explosion

        this.getDefaults().setLevelMax(5);
        this.getDefaults().setTier(1.0);
        this.getDefaults().setConflicts(EnchantVeinminer.ID, EnchantTunnel.ID);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this,
            "10.0 * " + Placeholders.ENCHANTMENT_LEVEL);
        this.explosionPower = EnchantScaler.read(this, "Settings.Explosion.Power",
            "2.0 + (" + Placeholders.ENCHANTMENT_LEVEL + " - 1.2 * 0.25)",
            "Explosion power. The more power = the more blocks (area) to explode.");
        this.minBlockStrength = EnchantScaler.read(this, "Settings.Min_Block_Strength",
            "1.5 - " + Placeholders.ENCHANTMENT_LEVEL + " / 10",
            "Minimal block strength value for the enchantment to have effect.",
            "Block strength value is how long it takes to break the block by a hand.",
            "For example, a Stone has 3.0 strength.");

        this.addPlaceholder(PLACEHOLDER_EXPLOSION_POWER, level -> NumberUtil.format(this.getExplosionPower(level)));
    }

    @Override
    public @NotNull ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    public double getExplosionPower(int level) {
        return this.explosionPower.getValue(level);
    }

    public float getMinBlockStrength(int level) {
        return (float) minBlockStrength.getValue(level);
    }

    private boolean isBlockHardEnough(@NotNull Block block, int level) {
        float strength = block.getType().getHardness(); //plugin.getNMS().getBlockStrength(block);
        return (strength >= this.getMinBlockStrength(level));
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

        if (EnchantUtils.contains(item, EnchantVeinminer.ID)) return false;
        if (EnchantUtils.contains(item, EnchantTunnel.ID)) return false;

        Block block = e.getBlock();
        if (block.hasMetadata(META_EXPLOSION_MINED)) return false;

        if (!this.isBlockHardEnough(block, level)) return false;
        if (!this.checkTriggerChance(level)) return false;

        float power = (float) this.getExplosionPower(level);

        player.setMetadata(META_EXPLOSION_SOURCE, new FixedMetadataValue(plugin, level));
        NoCheatPlusHook.exemptBlocks(player);
        boolean exploded = block.getWorld().createExplosion(block.getLocation(), power, false, true, player);
        NoCheatPlusHook.unexemptBlocks(player);
        player.removeMetadata(META_EXPLOSION_SOURCE, plugin);
        return exploded;
    }

    // Process explosion event to mine blocks.
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlastExplosionEvent(EntityExplodeEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        if (!player.hasMetadata(META_EXPLOSION_SOURCE)) return;

        int level = player.getMetadata(META_EXPLOSION_SOURCE).get(0).asInt();
        List<Block> blockList = e.blockList();

        // Remove the 'source' block which player mined and caused the explosion to prevent duplicated drops.
        // Remove all the 'soft' blocks that should not be exploded.
        blockList.removeIf(block -> block.getLocation().equals(e.getLocation()) || !this.isBlockHardEnough(block, level));

        // Break all 'exploded' blocks by a player, adding metadata to them to prevent trigger enchantment in a loop.
        blockList.forEach(block -> {
            block.setMetadata(META_EXPLOSION_MINED, new FixedMetadataValue(plugin, true));
            //plugin.getNMS().breakBlock(player, block);
            player.breakBlock(block);
            block.removeMetadata(META_EXPLOSION_MINED, plugin);
        });

        // Clear list of 'exploded' blocks so the event won't affect them, as they are already mined by a player.
        blockList.clear();
    }

    // Do not damage around entities by enchantment explosion.
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlastExplosionDamage(EntityDamageByEntityEvent e) {
        if (e.getCause() != DamageCause.ENTITY_EXPLOSION) return;
        if (!(e.getDamager() instanceof Player player)) return;

        e.setCancelled(player.hasMetadata(META_EXPLOSION_SOURCE));
    }

    // Do not reduce item durability for 'exploded' blocks.
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlastExplosionItemDamage(PlayerItemDamageEvent e) {
        if (!e.getPlayer().hasMetadata(META_EXPLOSION_SOURCE)) return;

        e.setCancelled(true);
    }
}
