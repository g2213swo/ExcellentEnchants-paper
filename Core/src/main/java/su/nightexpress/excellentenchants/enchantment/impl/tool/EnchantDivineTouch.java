package su.nightexpress.excellentenchants.enchantment.impl.tool;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.particle.SimpleParticle;
import su.nexmedia.engine.utils.ComponentUtil;
import su.nexmedia.engine.utils.LocationUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockBreakEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockDropEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.type.FitItemType;
import su.nightexpress.excellentenchants.enchantment.util.EnchantDropContainer;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

public class EnchantDivineTouch extends ExcellentEnchant implements Chanced, BlockBreakEnchant, BlockDropEnchant {

    public static final String ID = "divine_touch";
    private static final String META_HANDLE = ID + "_handle";

    private String spawnerName;
    private ChanceImplementation chanceImplementation;

    public EnchantDivineTouch(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
        this.getDefaults().setDescription("<lang:enchantment.g2213swo." + this.getId() + ".desc:" + Placeholders.ENCHANTMENT_CHANCE + ">");
        // "enchantment.g2213swo.divine_touch.desc": "%1$s% chance to mine spawner."
        // %1$s = chance to mine spawner

        this.getDefaults().setLevelMax(5);
        this.getDefaults().setTier(1.0);
        this.getDefaults().setConflicts(EnchantInfernalTouch.ID);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this,
            "15.0 * " + Placeholders.ENCHANTMENT_LEVEL);
        this.spawnerName = JOption.create("Settings.Spawner_Item.Name",
                "<green>Mob Spawner <gray>(" + Placeholders.GENERIC_TYPE + ")", // adventure
                "Spawner item display name.",
                "Placeholder '" + Placeholders.GENERIC_TYPE + "' for the mob type.")
            .read(cfg); // adventure
    }

    @Override
    public @NotNull ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    @Override
    public @NotNull FitItemType[] getFitItemTypes() {
        return new FitItemType[]{FitItemType.PICKAXE};
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    public @NotNull ItemStack getSpawner(@NotNull CreatureSpawner spawnerBlock) {
        ItemStack itemSpawner = new ItemStack(Material.SPAWNER);
        BlockStateMeta stateItem = (BlockStateMeta) itemSpawner.getItemMeta();
        if (stateItem == null || spawnerBlock.getSpawnedType() == null) return itemSpawner;

        CreatureSpawner spawnerItem = (CreatureSpawner) stateItem.getBlockState();
        spawnerItem.setSpawnedType(spawnerBlock.getSpawnedType());
        spawnerItem.update(true);
        stateItem.setBlockState(spawnerItem);
        stateItem.displayName(ComponentUtil.replace(
            text(this.spawnerName),
            Placeholders.GENERIC_TYPE,
            translatable(spawnerBlock.getSpawnedType())
        )); // adventure
        itemSpawner.setItemMeta(stateItem);

        return itemSpawner;
    }

    @Override
    public boolean onDrop(@NotNull BlockDropItemEvent e, @NotNull EnchantDropContainer dropContainer, @NotNull Player player, @NotNull ItemStack item, int level) {
        BlockState state = e.getBlockState();
        Block block = state.getBlock();
        if (!block.hasMetadata(META_HANDLE)) return false;
        if (!(state instanceof CreatureSpawner spawnerBlock)) return false;

        dropContainer.getDrops().add(this.getSpawner(spawnerBlock)); // akiranya

        Location location = LocationUtil.getCenter(block.getLocation());
        if (this.hasVisualEffects()) {
            SimpleParticle.of(Particle.VILLAGER_HAPPY).play(location, 0.3, 0.15, 30);
        }
        block.removeMetadata(META_HANDLE, this.plugin);
        return true;
    }

    @Override
    public boolean onBreak(@NotNull BlockBreakEvent e, @NotNull Player player, @NotNull ItemStack item, int level) {
        Block block = e.getBlock();
        if (!this.isAvailableToUse(player)) return false;
        if (!(block.getState() instanceof CreatureSpawner spawnerBlock)) return false;
        if (!this.checkTriggerChance(level)) return false;

        e.setExpToDrop(0);
        e.setDropItems(true);
        block.setMetadata(META_HANDLE, new FixedMetadataValue(this.plugin, true));
        return false; // Do not consume charges
    }

    // Update spawner type of the placed spawner mined by Divine Touch.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerPlace(BlockPlaceEvent e) {
        Block block = e.getBlock();
        if (block.getType() != Material.SPAWNER) return;

        Player player = e.getPlayer();
        ItemStack spawner = player.getInventory().getItem(e.getHand());
        if (spawner == null || spawner.getType() != Material.SPAWNER || !(spawner.getItemMeta() instanceof BlockStateMeta meta))
            return;

        CreatureSpawner spawnerItem = (CreatureSpawner) meta.getBlockState();
        CreatureSpawner spawnerBlock = (CreatureSpawner) block.getState();

        spawnerBlock.setSpawnedType(spawnerItem.getSpawnedType());
        spawnerBlock.update();
    }
}
