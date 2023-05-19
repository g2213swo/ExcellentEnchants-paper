package su.nightexpress.excellentenchants.tier;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.enchantment.type.ObtainType;

import java.util.*;
import java.util.stream.Collectors;

public class TierManager extends AbstractManager<ExcellentEnchants> {

    public static final String FILE_NAME = "tiers.yml";

    private JYML config;
    private final Map<String, Tier> tiers;

    public TierManager(@NotNull ExcellentEnchants plugin) {
        super(plugin);
        this.tiers = new HashMap<>();
    }

    @Override
    protected void onLoad() {
        /*if (ExcellentEnchants.isLoaded) {
            this.getTiers().forEach(tier -> tier.getEnchants().clear());
            return;
        }*/

        this.config = JYML.loadOrExtract(plugin, FILE_NAME);

        for (String sId : this.config.getSection("")) {
            String path = sId + ".";

            int priority = this.config.getInt(path + "Priority");
            String name = this.config.getString(path + "Name", sId);

            TextColor color = Optional
                .ofNullable(this.config.getString(path + "Color"))
                .map(v -> {
                    TextColor value = NamedTextColor.NAMES.value(v);
                    if (value == null) value = TextColor.fromHexString(v);
                    return value;
                })
                .orElse(NamedTextColor.GRAY);

            Map<ObtainType, Double> chance = new HashMap<>();
            for (ObtainType obtainType : ObtainType.values()) {
                this.config.addMissing(path + "Obtain_Chance." + obtainType.name(), 50D);

                double chanceType = this.config.getDouble(path + "Obtain_Chance." + obtainType.name());
                chance.put(obtainType, chanceType);
            }

            Tier tier = new Tier(sId, priority, name, color, chance);
            this.tiers.put(tier.getId(), tier);
        }

        this.plugin.info("Tiers Loaded: " + this.tiers.size());
    }

    @Override
    protected void onShutdown() {
        this.tiers.clear();
    }

    public @NotNull JYML getConfig() {
        return this.config;
    }

    public @Nullable Tier getTierById(@NotNull String id) {
        return this.tiers.get(id.toLowerCase());
    }

    public @NotNull Collection<Tier> getTiers() {
        return this.tiers.values();
    }

    public @NotNull List<String> getTierIds() {
        return new ArrayList<>(this.tiers.keySet());
    }

    public @Nullable Tier getTierByChance(@NotNull ObtainType obtainType) {
        Map<Tier, Double> map = this.getTiers().stream().collect(Collectors.toMap(k -> k, v -> v.getChance(obtainType)));
        return Rnd.getByWeight(map);
    }

    public @NotNull Tier getMostCommon() {
        return this.getTiers().stream().min(Comparator.comparingInt(Tier::getPriority)).orElseThrow();
    }

    public @NotNull Tier getByRarityModifier(double point) {
        int minPriority = this.getTiers().stream().mapToInt(Tier::getPriority).min().orElse(0);
        int maxPriority = this.getTiers().stream().mapToInt(Tier::getPriority).max().orElse(0);

        int threshold = (int) Math.ceil(minPriority + (maxPriority - minPriority) * point);

        return this.getTiers().stream().filter(tier -> tier.getPriority() <= threshold)
            .max(Comparator.comparingInt(tier -> tier.getPriority() - threshold))
            .orElse(this.getMostCommon());
    }
}
