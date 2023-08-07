package su.nightexpress.excellentenchants;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.Version;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nexmedia.engine.command.list.ReloadSubCommand;
import su.nexmedia.engine.hooks.Hooks;
import su.nightexpress.excellentenchants.command.BookCommand;
import su.nightexpress.excellentenchants.command.EnchantCommand;
import su.nightexpress.excellentenchants.command.ListCommand;
import su.nightexpress.excellentenchants.command.TierbookCommand;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.config.Lang;
import su.nightexpress.excellentenchants.enchantment.EnchantManager;
import su.nightexpress.excellentenchants.enchantment.EnchantRegistry;
import su.nightexpress.excellentenchants.enchantment.type.FitItemType;
import su.nightexpress.excellentenchants.hook.HookId;
import su.nightexpress.excellentenchants.hook.impl.PlaceholderHook;
import su.nightexpress.excellentenchants.hook.impl.ProtocolHook;
import su.nightexpress.excellentenchants.nms.EnchantNMS;
import su.nightexpress.excellentenchants.nms.v1_20_R1.V1_20_R1;
import su.nightexpress.excellentenchants.tier.TierManager;

public class ExcellentEnchants extends NexPlugin<ExcellentEnchants> {

    private EnchantRegistry enchantRegistry;
    private EnchantManager enchantManager;
    private EnchantNMS enchantNMS;
    private TierManager tierManager;

    @Override
    protected @NotNull ExcellentEnchants getSelf() {
        return this;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.enchantRegistry = new EnchantRegistry(this);
    }

    @Override
    public void enable() {
        this.setNMS();

        this.tierManager = new TierManager(this);
        this.tierManager.setup();

        this.enchantRegistry.setup();

        this.enchantManager = new EnchantManager(this);
        this.enchantManager.setup();
    }

    @Override
    public void disable() {
        if (this.enchantManager != null) {
            this.enchantManager.shutdown();
            this.enchantManager = null;
        }
        if (this.tierManager != null) {
            this.tierManager.shutdown();
            this.tierManager = null;
        }
        PlaceholderHook.shutdown();
        //this.enchantRegistry.shutdown(); Do we ever need this at all?
    }

    private void setNMS() {
        this.enchantNMS = switch (Version.CURRENT) {
            case V1_20_R1 -> new V1_20_R1();
            default -> throw new IllegalStateException("Unsupported Minecraft version: " + Version.CURRENT);
        };
    }

    @Override
    public void loadConfig() {
        this.getConfig().initializeOptions(Config.class);
    }

    @Override
    public void loadLang() {
        this.getLangManager().loadMissing(Lang.class);
        this.getLangManager().setupEnum(FitItemType.class);
        this.getLang().saveChanges();
    }

    @Override
    public void registerCommands(@NotNull GeneralCommand<ExcellentEnchants> mainCommand) {
        mainCommand.addChildren(new BookCommand(this));
        mainCommand.addChildren(new EnchantCommand(this));
        mainCommand.addChildren(new ListCommand(this));
        mainCommand.addChildren(new TierbookCommand(this));
        mainCommand.addChildren(new ReloadSubCommand<>(this, Perms.COMMAND_RELOAD));
    }

    @Override
    public void registerHooks() {
        if (Hooks.hasPlugin(HookId.PROTOCOL_LIB)) {
            ProtocolHook.setup();
        } else {
            this.warn(HookId.PROTOCOL_LIB + " is not installed. Enchantments won't be displayed on items.");
        }
        if (Hooks.hasPlaceholderAPI()) {
            PlaceholderHook.setup();
        }
    }

    @Override
    public void registerPermissions() {
        this.registerPermissions(Perms.class);
    }

    public @NotNull TierManager getTierManager() {
        return this.tierManager;
    }

    public @NotNull EnchantRegistry getEnchantRegistry() {
        return this.enchantRegistry;
    }

    public @NotNull EnchantManager getEnchantManager() {
        return this.enchantManager;
    }

    public @NotNull EnchantNMS getEnchantNMS() {
        return enchantNMS;
    }
}
