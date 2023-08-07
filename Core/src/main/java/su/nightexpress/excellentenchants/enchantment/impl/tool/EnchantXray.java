package su.nightexpress.excellentenchants.enchantment.impl.tool;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.InternalStructure;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.type.InteractEnchant;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.task.AbstractEnchantmentTask;
import su.nightexpress.excellentenchants.enchantment.type.FitItemType;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class EnchantXray extends ExcellentEnchant implements InteractEnchant, ICleanable {

    public static final String ID = "xray";

    private static final String PLACEHOLDER_XRAY_DISTANCE = "%enchantment_xray_distance%";

    private static final String PLACEHOLDER_XRAY_COOLDOWN = "%enchantment_xray_cooldown%";

    private static final String PLACEHOLDER_XRAY_DURATION = "%enchantment_xray_duration%";

    private List<Material> normalOres;

    private List<Material> uncommonOres;

    private List<Material> rareOres;

    private BukkitTask task;

    private EnchantScaler distance;

    private long cooldown;

    private long duration;

    private final ProtocolManager manager;

    private final Map<Block, Integer> blockToEntityMap;

    private final Set<Integer> allocatedEntityIds;

    private final Map<Integer, UUID> entityIdToEntityUUIDMap;

    private final Map<Material, ChatColor> oreColors;
    private final Map<UUID, Integer> playerTickCountMap;

    private final Map<UUID, Long> cooldownMap;

    private BlockExistTask blockExistTask;

    public EnchantXray(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
        this.getDefaults().setDescription("<lang:enchantment.g2213swo." + this.getId()
                        + ".desc:" + PLACEHOLDER_XRAY_DURATION + ":" + PLACEHOLDER_XRAY_DISTANCE + ":" + PLACEHOLDER_XRAY_COOLDOWN + ">",
                "<lang:enchantment.g2213swo." + this.getId() + ".desc2>");

        // "enchantment.g2213swo.xray.desc": "当你右键时，在 %1$s 秒内透视范围为 %2$s 的矿石（冷却 %3$s 秒）"

        this.getDefaults().setLevelMax(3);
        this.getDefaults().setTier(0.7);

        this.blockToEntityMap = new HashMap<>();
        this.entityIdToEntityUUIDMap = new HashMap<>();
        this.allocatedEntityIds = new HashSet<>();
        this.oreColors = new HashMap<>();
        this.playerTickCountMap = new ConcurrentHashMap<>();
        this.cooldownMap = new ConcurrentHashMap<>();

        this.manager = ProtocolLibrary.getProtocolManager();
    }

    @Override
    public void loadSettings() {
        super.loadSettings();

        this.distance = EnchantScaler.read(this,
                "Settings.Xray.Distance", "3 + " + Placeholders.ENCHANTMENT_LEVEL + " * 2",
                "Xray");
        this.cooldown = JOption.create("Settings.Xray.Cooldown", 60,
                "Xray Cooldown").read(cfg);
        this.duration = JOption.create(
                "Settings.Xray.Duration", 30 * 20 / 2,
                "Xray Duration").read(cfg);
        Map<ChatColor, List<String>> oreColorMappings = buildOreColorMappings(cfg);

        for (Map.Entry<ChatColor, List<String>> entry : oreColorMappings.entrySet()) {
            ChatColor color = entry.getKey();
            List<String> ores = entry.getValue();
            for (String oreName : ores) {
                try {
                    Material oreMaterial = Material.valueOf(oreName);
                    this.oreColors.put(oreMaterial, color);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace(); // Handle or log the error.
                }
            }
        }

        normalOres = getOresFromConfig(
                "Settings.Xray.OreTypes.NORMAL",
                Arrays.asList("COAL_ORE", "DEEPSLATE_COAL_ORE", "COPPER_ORE", "DEEPSLATE_COPPER_ORE", "IRON_ORE", "DEEPSLATE_IRON_ORE", "LAPIS_ORE", "DEEPSLATE_LAPIS_ORE"),
                "Normal Ores",
                cfg);

        uncommonOres = getOresFromConfig(
                "Settings.Xray.OreTypes.UNCOMMON",
                Arrays.asList("GOLD_ORE", "DEEPSLATE_GOLD_ORE", "REDSTONE_ORE", "DEEPSLATE_REDSTONE_ORE", "DIAMOND_ORE", "DEEPSLATE_DIAMOND_ORE", "EMERALD_ORE", "DEEPSLATE_EMERALD_ORE"),
                "Uncommon Ores",
                cfg);

        rareOres = getOresFromConfig(
                "Settings.Xray.OreTypes.RARE",
                Arrays.asList("ANCIENT_DEBRIS", "NETHER_GOLD_ORE", "NETHER_QUARTZ_ORE"),
                "Rare Ores",
                cfg);


        this.blockExistTask = new BlockExistTask(plugin);
        this.blockExistTask.start();
    }

    public double getDistance(int level) {
        return distance.getValue(level);
    }

    @Override
    public void clear() {
        this.stopTask();

        this.blockToEntityMap.clear();
        this.allocatedEntityIds.clear();
        this.entityIdToEntityUUIDMap.clear();
        this.oreColors.clear();
    }

    private void stopTask() {
        if (this.task != null && !this.task.isCancelled()) {
            task.cancel();
            this.task = null;
        }
        if (this.blockExistTask != null) {
            this.blockExistTask.stop();
            this.blockExistTask = null;
        }
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public @NotNull FitItemType[] getFitItemTypes() {
        return new FitItemType[]{FitItemType.PICKAXE};
    }

    private final AtomicInteger entityIdCounter = new AtomicInteger(100000);

    private List<Material> getOresFromConfig(String path, List<String> defaultValues, String description, JYML cfg) {
        return JOption.create(path, defaultValues, description).read(cfg).stream()
                .map(oreName -> {
                    try {
                        return Material.valueOf(oreName);
                    } catch (IllegalArgumentException e) {
                        ComponentLogger.logger("X-ray").error("Warning: Invalid ore name " + oreName + " in config at " + path);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Map<ChatColor, List<String>> buildOreColorMappings(JYML cfg) {
        Map<ChatColor, List<String>> mappings = new HashMap<>();

        Map<String, ChatColor> colorKeyMap = new HashMap<>() {{
            put("BLACK", ChatColor.BLACK);
            put("GOLD", ChatColor.GOLD);
            put("GRAY", ChatColor.GRAY);
            put("RED", ChatColor.RED);
            put("BLUE", ChatColor.BLUE);
            put("GREEN", ChatColor.GREEN);
            put("AQUA", ChatColor.AQUA);
            put("WHITE", ChatColor.WHITE);
            put("YELLOW", ChatColor.YELLOW);
            put("DARK_RED", ChatColor.DARK_RED);
            put("DARK_BLUE", ChatColor.DARK_BLUE);
            put("DARK_GREEN", ChatColor.DARK_GREEN);
            put("DARK_AQUA", ChatColor.DARK_AQUA);
            put("DARK_GRAY", ChatColor.DARK_GRAY);
            put("DARK_PURPLE", ChatColor.DARK_PURPLE);
            put("LIGHT_PURPLE", ChatColor.LIGHT_PURPLE);
        }};

        for (Map.Entry<String, ChatColor> entry : colorKeyMap.entrySet()) {
            String colorKey = entry.getKey();
            ChatColor chatColor = entry.getValue();

            List<String> defaultOres = switch (colorKey) {
                case "BLACK" -> List.of("COAL_ORE", "DEEPSLATE_COAL_ORE");
                case "GOLD" -> List.of("COPPER_ORE", "DEEPSLATE_COPPER_ORE");
                case "GRAY" -> List.of("IRON_ORE", "DEEPSLATE_IRON_ORE");
                case "RED" -> List.of("REDSTONE_ORE", "DEEPSLATE_REDSTONE_ORE");
                case "BLUE" -> List.of("LAPIS_ORE", "DEEPSLATE_LAPIS_ORE");
                case "GREEN" -> List.of("EMERALD_ORE", "DEEPSLATE_EMERALD_ORE");
                case "AQUA" -> List.of("DIAMOND_ORE", "DEEPSLATE_DIAMOND_ORE");
                case "WHITE" -> List.of("NETHER_QUARTZ_ORE");
                case "YELLOW" -> List.of("GOLD_ORE", "DEEPSLATE_GOLD_ORE", "NETHER_GOLD_ORE", "NETHER_GOLD_ORE");
                case "DARK_RED" -> List.of("ANCIENT_DEBRIS");
                default -> Collections.emptyList();
            };

            mappings.put(chatColor, JOption.create("Settings.Xray.OreColors." + colorKey, defaultOres, "Xray Ore Colors").read(cfg));
        }

        return mappings;
    }

    @Override
    public boolean onInteract(@NotNull PlayerInteractEvent e, @NotNull Player player, @NotNull ItemStack item, int level) {
        if (!this.isAvailableToUse(player)) return false;

        Action action = e.getAction();

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (player.getGameMode() != GameMode.CREATIVE) {
                // 检查冷却
                UUID playerId = player.getUniqueId();
                if (cooldownMap.containsKey(playerId)) {
                    long remainingMillis = cooldownMap.get(playerId) - System.currentTimeMillis();
                    if (remainingMillis > 0) {
                        int remainingSeconds = (int) (remainingMillis / 1000);  // 转换为秒
                        player.sendMessage(Component.translatable("enchantment.g2213swo.xray.cooldown")
                                .args(Component.text(remainingSeconds).color(TextColor.color(0x81FBB8)))
                                .color(TextColor.color(0xEA5455)));  // 提示玩家还剩多少秒冷却时间
                        return false;
                    }
                }
            }

            // 在玩家右击前先销毁旧的潜影贝实体
            destroyAllExistingShulkers(player);

            task = plugin.getScheduler().runTaskAsynchronously(plugin, () -> {
                Set<Location> blockLocations = new HashSet<>();
                Location playerLoc = player.getLocation();
                World world = playerLoc.getWorld();
                int radius = (int) this.getDistance(level);

                for (int x = -radius; x <= radius; x++) {
                    for (int y = -radius; y <= radius; y++) {
                        for (int z = -radius; z <= radius; z++) {
                            Block block = world.getBlockAt(playerLoc.getBlockX() + x, playerLoc.getBlockY() + y, playerLoc.getBlockZ() + z);
                            Material type = block.getType();
                            switch (level) {
                                case 1:
                                    if (this.normalOres.contains(type)) {
                                        blockLocations.add(block.getLocation());
                                    }
                                    break;
                                case 2:
                                    if (this.normalOres.contains(type) || this.uncommonOres.contains(type)) {
                                        blockLocations.add(block.getLocation());
                                    }
                                    break;
                                case 3:
                                default:
                                    if (this.normalOres.contains(type) || this.uncommonOres.contains(type) || this.rareOres.contains(type)) {
                                        blockLocations.add(block.getLocation());
                                    }
                                    break;
                            }
                        }
                    }
                }

                if (blockLocations.isEmpty()) {
                    player.sendMessage(Component.translatable("enchantment.g2213swo.xray.nothing")
                            .color(TextColor.color(0x81FBB8)));
                    return;
                }

                if (player.getGameMode() != GameMode.CREATIVE) {
                    // 设置冷却时间
                    cooldownMap.put(player.getUniqueId(), System.currentTimeMillis() + cooldown * 1000);
                }

                player.sendMessage(Component.translatable("enchantment.g2213swo.xray.start")
                        .args(Component
                                .text(blockLocations.size())
                                .color(TextColor.color(0x5EFCE8)))
                        .color(TextColor.color(0x81FBB8)));

                playerTickCountMap.put(player.getUniqueId(), 0);
                rayBlocks(player, blockLocations);
            });
        }
        return false;
    }

    private void destroyAllExistingShulkers(Player player) {
        List<Integer> existingIds = new ArrayList<>(blockToEntityMap.values());
        for (Integer existingId : existingIds) {
            destroyShulker(player, existingId);
        }
    }

    private void destroyShulker(Player player, int entityId) {
        PacketContainer destroyPacket = manager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        destroyPacket.getIntLists().write(0, List.of(entityId));
        manager.sendServerPacket(player, destroyPacket);
        removeTeam(player, entityId);
        allocatedEntityIds.remove(entityId);
        entityIdToEntityUUIDMap.keySet().remove(entityId);
        blockToEntityMap.values().remove(entityId);
    }

    private void removeTeam(Player player, int entityId) {
        String teamName = "shulkerTeam_" + entityId;
        PacketContainer removeTeamPacket = manager.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);
        removeTeamPacket.getStrings().write(0, teamName); // Team name
        removeTeamPacket.getIntegers().write(0, 1); // 1 = remove team
        manager.sendServerPacket(player, removeTeamPacket);
    }

    private void rayBlocks(Player player, Set<Location> locations) {
        for (Location loc : locations) {
            // 创建一个新的SpawnEntity数据包
            PacketContainer shulkerPacket = this.manager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);

            int entityId = entityIdCounter.getAndIncrement();
            while (allocatedEntityIds.contains(entityId)) { // 确保不会分配到已使用的ID
                entityId = entityIdCounter.getAndIncrement();
            }
            shulkerPacket.getIntegers().write(0, entityId); // 唯一的实体ID
            allocatedEntityIds.add(entityId); // 添加到已分配的ID集合中

            UUID uuid = UUID.randomUUID();

            entityIdToEntityUUIDMap.put(entityId, uuid); // 添加到ID到UUID的映射中

            shulkerPacket.getUUIDs().write(0, uuid); // random uuid
            shulkerPacket.getEntityTypeModifier().write(0, EntityType.SHULKER); // shulker entity type
            shulkerPacket.getDoubles()
                    .write(0, loc.getX())
                    .write(1, loc.getY())
                    .write(2, loc.getZ());

            blockToEntityMap.put(loc.getBlock(), entityId);

            PacketContainer metadataPacket = this.manager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
            metadataPacket.getIntegers().write(0, entityId);

            byte flag = 0x20; // invisibility
            flag += 0x40; // glowing

            WrappedDataWatcher watcher = new WrappedDataWatcher();
            WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(Byte.class);
            watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, serializer), flag);


            List<WrappedDataValue> wrappedDataValueList = Lists.newArrayList();
            watcher.getWatchableObjects()
                    .stream()
                    .filter(Objects::nonNull)
                    .forEach(entry -> wrappedDataValueList
                            .add(new WrappedDataValue(entry.getWatcherObject()
                                    .getIndex(),
                                    entry.getWatcherObject()
                                            .getSerializer(),
                                    entry.getRawValue())));

            metadataPacket.getDataValueCollectionModifier().write(0, wrappedDataValueList);

            ChatColor chatColor = oreColors.get(loc.getBlock().getType());

            if (chatColor == null) {
                chatColor = ChatColor.WHITE;
            }


            this.manager.sendServerPacket(player, shulkerPacket);

            sendShulkerGlowColor(player, entityId, chatColor);

            this.manager.sendServerPacket(player, metadataPacket);

            blockToEntityMap.put(loc.getBlock(), entityId);
        }
    }

    private void sendShulkerGlowColor(Player player, int entityId, ChatColor color) {
        // 1. 创建一个独特的Team名，每个潜影贝都有一个单独的Team
        String teamName = "shulkerTeam_" + entityId;
        String entityUUID = entityIdToEntityUUIDMap.get(entityId).toString();

        // 2. 创建并发送一个创建新Team的数据包
        PacketContainer createTeamPacket = manager.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);
        createTeamPacket.getStrings().write(0, teamName); // Team name
        createTeamPacket.getIntegers().write(0, 0); // 0 = create team
        Optional<InternalStructure> optStruct = createTeamPacket.getOptionalStructures().read(0);
        if (optStruct.isPresent()) {
            InternalStructure internalStructure = optStruct.get();
            internalStructure.getChatComponents().write(1, WrappedChatComponent.fromText("")); // Team prefix
            internalStructure.getChatComponents().write(2, WrappedChatComponent.fromText("")); // Team suffix
            internalStructure.getIntegers().write(0, 2); // This new team has 2 members
            internalStructure.getStrings().write(0, "never"); // Team visibility (never = invisible)

            internalStructure.getEnumModifier(ChatColor.class,
                            MinecraftReflection.getMinecraftClass("EnumChatFormat"))
                    .write(0, color);
        }
        createTeamPacket.getModifier().write(2, Lists.newArrayList(entityUUID)); // Team consists of the viewer by name, and the fake entity by its generated UUID

        manager.sendServerPacket(player, createTeamPacket);
    }

    /**
     * 这是一个任务，用于检查潜影贝的方块是否被破坏，并且在一定时间后移除所有潜影贝
     * 移除玩家使用附魔的冷却时间
     */
    class BlockExistTask extends AbstractEnchantmentTask {

        public BlockExistTask(@NotNull ExcellentEnchants plugin) {
            super(plugin, 2, false);
        }

        @Override
        public void action() {
            for (LivingEntity entity : this.getEntities()) {
                if (!(entity instanceof Player player)) {
                    continue;
                }

                // 检查方块是否被破坏，如果是则移除对应的潜影贝
                for (Block block : blockToEntityMap.keySet().stream().filter(Objects::nonNull).toList()) {
                    if (block.getType() == Material.AIR) {
                        int entityId = blockToEntityMap.get(block);
                        destroyShulker(player, entityId);
                    }
                }

                UUID playerId = player.getUniqueId();

                // 清理已结束的冷却时间
                if (cooldownMap.containsKey(playerId) && cooldownMap.get(playerId) <= System.currentTimeMillis()) {
                    cooldownMap.remove(playerId);
                }

                playerTickCountMap.compute(player.getUniqueId(), (uuid, currentTickCount) -> {
                    if (currentTickCount == null) {
                        return 1;  // 初始值
                    }
                    if (currentTickCount >= duration) {
                        // 移除该玩家的所有潜影贝
                        destroyAllExistingShulkers(player);
                        return 0;  // 重置tickCount
                    }
                    return currentTickCount + 1;  // 增加tickCount
                });
            }
        }
    }


}
