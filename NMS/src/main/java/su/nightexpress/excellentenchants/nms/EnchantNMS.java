package su.nightexpress.excellentenchants.nms;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface EnchantNMS { // net.minecraft.server

    void sendAttackPacket(@NotNull Player player, int id);

    void retrieveHook(@NotNull FishHook hook, @NotNull ItemStack item);

    @Nullable ItemStack getSpawnEgg(@NotNull LivingEntity entity);

    @NotNull Set<Block> handleFlameWalker(@NotNull LivingEntity entity, @NotNull Location location, int level);

    @NotNull ItemStack createItemStack(Key item, int count, BinaryTagHolder nbt);
}
