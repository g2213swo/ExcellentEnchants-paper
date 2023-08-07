package su.nightexpress.excellentenchants.enchantment.impl.tool;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.particle.SimpleParticle;
import su.nexmedia.engine.utils.LocationUtil;
import su.nexmedia.engine.utils.MessageUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockDropEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.type.FitItemType;
import su.nightexpress.excellentenchants.enchantment.util.EnchantDropContainer;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;

import java.util.Map;

public class EnchantSmelter extends ExcellentEnchant implements Chanced, BlockDropEnchant {

    public static final String ID = "smelter";

    private Sound sound;
    private Map<Material, Material> smeltingTable;
    private ChanceImplementation chanceImplementation;

    public EnchantSmelter(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
        this.getDefaults().setDescription("<lang:enchantment.g2213swo." + this.getId() + ".desc:"
                + Placeholders.ENCHANTMENT_CHANCE + ">");
        // "enchantment.g2213swo.your_enchant_id.desc": "%1$s%% chance to smelt a block/ore."
        // %1$s = enchantment chance

        this.getDefaults().setLevelMax(5);
        this.getDefaults().setTier(0.3);
        this.getDefaults().setConflicts(
            EnchantDivineTouch.ID,
            Enchantment.SILK_TOUCH.getKey().getKey()
        );
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this,
            "25.0 + " + Placeholders.ENCHANTMENT_LEVEL + " * 10");

        this.sound = JOption.create("Settings.Sound", Sound.class, Sound.BLOCK_LAVA_EXTINGUISH,
            "Sound to play on smelting.",
            "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html").read(cfg);

        this.smeltingTable = JOption.forMap("Settings.Smelting_Table",
            key -> Material.getMaterial(key.toUpperCase()),
            (cfg, path, key) -> Material.getMaterial(cfg.getString(path + "." + key, "").toUpperCase()),
            Map.of(
                Material.RAW_IRON, Material.IRON_INGOT,
                Material.RAW_GOLD, Material.GOLD_INGOT
            ),
            "Table of Original -> Smelted items.",
            "Syntax: 'Material Source : Material Result'.",
            "Note: Material source is material name of the dropped item, not the broken block!",
            "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html"
        ).setWriter((cfg, path, map) -> map.forEach((src, to) -> cfg.set(path + "." + src.name(), to.name()))).read(cfg);
    }

    @Override
    public @NotNull ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    @Override
    public @NotNull FitItemType[] getFitItemTypes() {
        return new FitItemType[]{FitItemType.PICKAXE, FitItemType.AXE, FitItemType.SHOVEL};
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean onDrop(@NotNull BlockDropItemEvent e, @NotNull EnchantDropContainer dropContainer, @NotNull Player player, @NotNull ItemStack item, int level) {
        if (e.getBlockState() instanceof Container) return false;
        if (!this.isAvailableToUse(player)) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (e.getItems().stream().noneMatch(drop -> this.isSmeltable(drop.getItemStack().getType()))) return false;

        e.getItems().forEach(drop -> {
            Material material = this.smeltingTable.get(drop.getItemStack().getType());
            if (material != null) drop.getItemStack().setType(material);
        });

        Block block = e.getBlockState().getBlock();
        if (this.hasVisualEffects()) {
            Location location = LocationUtil.getCenter(block.getLocation(), true);
            MessageUtil.playSound(location, this.sound); // akiranya
            SimpleParticle.of(Particle.FLAME).play(location, 0.25, 0.05, 20);
        }
        return true;
    }

    public boolean isSmeltable(@NotNull Material material) {
        return this.smeltingTable.containsKey(material);
    }
}
