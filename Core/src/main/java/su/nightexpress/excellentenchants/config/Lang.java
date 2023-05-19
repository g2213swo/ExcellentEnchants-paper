package su.nightexpress.excellentenchants.config;

import su.nexmedia.engine.api.lang.LangKey;
import su.nexmedia.engine.lang.EngineLang;
import su.nightexpress.excellentenchants.Placeholders;

public class Lang extends EngineLang {

    public static final LangKey COMMAND_LIST_DESC = LangKey.of("Command.List.Desc", "List of all custom enchantments.");

    public static final LangKey COMMAND_ENCHANT_USAGE = LangKey.of("Command.Enchant.Usage", "<enchant> <level>");
    public static final LangKey COMMAND_ENCHANT_DESC = LangKey.of("Command.Enchant.Desc", "Enchants the item in your hand.");
    public static final LangKey COMMAND_ENCHANT_DONE = LangKey.of("Command.Enchant.Done", "<green>Successfully enchanted!");

    public static final LangKey COMMAND_BOOK_USAGE = LangKey.of("Command.Book.Usage", "<player> <enchant> <level>");
    public static final LangKey COMMAND_BOOK_DESC = LangKey.of("Command.Book.Desc", "Gives custom enchanted book.");
    public static final LangKey COMMAND_BOOK_DONE = LangKey.of("Command.Book.Done", "Given <gold>" + Placeholders.GENERIC_ENCHANT + "</gold> enchanted book to <gold>" + Placeholders.Player.DISPLAY_NAME + "</gold>.");

    public static final LangKey COMMAND_TIER_BOOK_USAGE = LangKey.of("Command.TierBook.Usage", "<player> <tier1:x>[,<tier2:y>,...] <level>");
    public static final LangKey COMMAND_TIER_BOOK_DESC = LangKey.of("Command.TierBook.Desc", "Gives an enchanted book.");
    public static final LangKey COMMAND_TIER_BOOK_ERROR = LangKey.of("Command.TierBook.Error", "<red>Invalid tier!");
    public static final LangKey COMMAND_TIER_BOOK_DONE = LangKey.of("Command.TierBook.Done", "Given <gold>" + Placeholders.TIER_NAME + "</gold> enchanted book to <gold>" + Placeholders.Player.DISPLAY_NAME + "</gold>.");

    public static final LangKey ERROR_NO_ENCHANT = LangKey.of("Error.NoEnchant", "<red>Invalid enchantment.");

}
