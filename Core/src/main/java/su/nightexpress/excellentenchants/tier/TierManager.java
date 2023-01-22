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

        this.config = JYML.loadOrExtract(plugin, "tiers.yml");

        for (String sId : config.getSection("")) {
            String path = sId + ".";

            int priority = config.getInt(path + "Priority");
            String name = config.getString(path + "Name", sId);

            TextColor color = Optional
                .ofNullable(config.getString(path + "Color"))
                .map(v -> {
                    TextColor value = NamedTextColor.NAMES.value(v);
                    if (value == null) value = TextColor.fromHexString(v);
                    return value;
                })
                .orElse(NamedTextColor.GRAY);

            Map<ObtainType, Double> chance = new HashMap<>();
            for (ObtainType obtainType : ObtainType.values()) {
                config.addMissing(path + "Obtain_Chance." + obtainType.name(), 50D);

                double chanceType = config.getDouble(path + "Obtain_Chance." + obtainType.name());
                chance.put(obtainType, chanceType);
            }

            Tier tier = new Tier(sId, priority, name, color, chance);
            this.tiers.put(tier.getId(), tier);
        }

        if (this.tiers.isEmpty()) {
            this.tiers.put(Tier.DEFAULT.getId(), Tier.DEFAULT);
        }

        this.plugin.info("Tiers Loaded: " + this.tiers.size());
    }

    @Override
    protected void onShutdown() {
        this.tiers.clear();
    }

    @NotNull
    public JYML getConfig() {
        return config;
    }

    @Nullable
    public Tier getTierById(@NotNull String id) {
        return this.tiers.get(id.toLowerCase());
    }

    @NotNull
    public Collection<Tier> getTiers() {
        return this.tiers.values();
    }

    @NotNull
    public List<String> getTierIds() {
        return new ArrayList<>(this.tiers.keySet());
    }

    @Nullable
    public Tier getTierByChance(@NotNull ObtainType obtainType) {
        Map<Tier, Double> map = this.getTiers().stream().collect(Collectors.toMap(k -> k, v -> v.getChance(obtainType)));
        return Rnd.get(map);
    }
}
