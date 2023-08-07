package su.nightexpress.excellentenchants.api.enchantment;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nightexpress.excellentenchants.enchantment.config.EnchantChargesFuel;
import su.nightexpress.excellentenchants.enchantment.type.ObtainType;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;
import su.nightexpress.excellentenchants.tier.Tier;

import java.util.List;
import java.util.Set;

public interface IEnchantment {
    @NotNull PlaceholderMap getPlaceholders(int level); // Mewcraft - depends on interface

    @NotNull JYML getConfig();

    @NotNull String getId();

    @NotNull EnchantPriority getPriority();

    @NotNull String getDisplayName();

    @NotNull String getNameFormatted(int level);

    @NotNull String getNameFormatted(int level, int charges);

    @NotNull List<String> getDescription();

    @NotNull List<String> getDescription(int level);

    @NotNull Set<String> getConflicts();

    @NotNull Tier getTier();

    int getMaxLevel();

    int getStartLevel();

    int getLevelByEnchantCost(int expLevel);

    double getObtainChance(@NotNull ObtainType obtainType);

    int getObtainLevelMin(@NotNull ObtainType obtainType);

    int getObtainLevelMax(@NotNull ObtainType obtainType);

    int generateLevel(@NotNull ObtainType obtainType);

    int getAnvilMergeCost(int level);

    boolean conflictsWith(@NotNull Enchantment enchantment);

    boolean canEnchantItem(@Nullable ItemStack item);

    boolean isCursed();

    boolean isTreasure();

    boolean isChargesEnabled();

    int getChargesMax(int level);

    int getChargesConsumeAmount(int level);

    int getChargesRechargeAmount(int level);

    @NotNull EnchantChargesFuel getChargesFuel(); // Akiranya - plugin item support

    boolean isChargesFuel(@NotNull ItemStack item);

    int getCharges(@NotNull ItemStack item);

    boolean isFullOfCharges(@NotNull ItemStack item);

    boolean isOutOfCharges(@NotNull ItemStack item);

    void consumeCharges(@NotNull ItemStack item);
}