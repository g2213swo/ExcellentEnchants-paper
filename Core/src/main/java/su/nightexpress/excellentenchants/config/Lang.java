package su.nightexpress.excellentenchants.config;

import su.nexmedia.engine.api.lang.LangKey;
import su.nexmedia.engine.lang.EngineLang;
import su.nightexpress.excellentenchants.Placeholders;

public class Lang extends EngineLang {

    public static final LangKey COMMAND_LIST_DESC = new LangKey("Command.List.Desc", "List of all custom enchantments.");

    public static final LangKey COMMAND_ENCHANT_USAGE = new LangKey("Command.Enchant.Usage", "<enchant> <level>");
    public static final LangKey COMMAND_ENCHANT_DESC = new LangKey("Command.Enchant.Desc", "Enchants the item in your hand.");
    public static final LangKey COMMAND_ENCHANT_DONE = new LangKey("Command.Enchant.Done", "<green>Successfully enchanted!");

    public static final LangKey COMMAND_BOOK_USAGE = new LangKey("Command.Book.Usage", "<player> <enchant> <level>");
    public static final LangKey COMMAND_BOOK_DESC = new LangKey("Command.Book.Desc", "Gives custom enchanted book.");
    public static final LangKey COMMAND_BOOK_DONE = new LangKey("Command.Book.Done", "Given <gold>" + Placeholders.GENERIC_ENCHANT + "</gold> enchanted book to <gold>" + Placeholders.Player.DISPLAY_NAME + "</gold>.");

    public static final LangKey COMMAND_TIER_BOOK_USAGE = new LangKey("Command.TierBook.Usage", "<player> <tier> <level>");
    public static final LangKey COMMAND_TIER_BOOK_DESC = new LangKey("Command.TierBook.Desc", "Gives an enchanted book.");
    public static final LangKey COMMAND_TIER_BOOK_ERROR = new LangKey("Command.TierBook.Error", "<red>Invalid tier!");
    public static final LangKey COMMAND_TIER_BOOK_DONE = new LangKey("Command.TierBook.Done", "Given <gold>" + Placeholders.TIER_NAME + "</gold> enchanted book to <gold>" + Placeholders.Player.DISPLAY_NAME + "</gold>.");

    public static final LangKey ERROR_NO_ENCHANT = new LangKey("Error.NoEnchant", "<red>Invalid enchantment.");

}
