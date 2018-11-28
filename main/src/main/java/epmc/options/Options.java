/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

package epmc.options;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.CaseFormat;

import epmc.error.EPMCException;
import static epmc.error.UtilError.ensure;

import java.util.ResourceBundle;

/**
 * Options set.
 * <p>
 * This class is responsible for managing a set of options. It is responsible
 * for parsing options from the command line, GUI or web interface and allowing
 * the tool to later on retrieve the parsed values programmatically. It is also
 * responsible for printing an overview of the available option in a
 * human-readable format.
 * </p>
 * <p>
 * This class is written to be serialisable in order to transfer it via RMI.
 * However, note that it was written with the intention to be used in a context
 * where the transfer of classes is not available and where the transfer of
 * classes is not necessary. Thus, class objects might be removed from options
 * contained in this options set without further notice. Also note that
 * {@link OptionType}s will not be transferred. The reason is that otherwise it
 * might be necessary to transfer classes of these options types, in particular
 * if they were not loaded by the main class loader.
 * </p>
 * <p>
 * The call of command of programs using this options set to parse parameters
 * looks as follows:
 * </p>
 * 
 * <pre>
 * {@code <program> <command> --<option> <param> ... <param> --<option> ... --<option> <param> ...}
 * </pre>
 * <p>
 * {@code <program>} is the call of the program itself, e.g.
 * {@code jar -ea -jar EPMC.jar}. Afterwards, the {@code <command>} to be
 * executed follows. Here, commands do not have parameters. The reason for this
 * decision is that this eases integration of the commands into other contexts,
 * e.g. in a GUI or web interface. Here, it would for instance not be so useful
 * if a command {@code check} would always take a model filename and a property
 * filename as parameters, because models and properties would probably be read
 * by some part of the GUI rather than directly from the file system. With the
 * following parameters, program options may be set. A program option needs to
 * be prefixed by "--" to indicate that it is a program option and not a
 * parameter of an option or a command. After the program option, one or more
 * parameters to the option follow, the number and type of which depend on the
 * program option for which the parameters are given. Afterwards, further
 * program options may follow.
 * </p>
 * 
 * @author Ernst Moritz Hahn
 */
public final class Options implements Serializable, Cloneable {
    /** Serial version UID - 1L as I don't know any better. */
    private final static long serialVersionUID = 1L;
    /** String "help". */
    private final static String HELP = "help";
    /** Identifier of the options in which plugin file names will be stored. */
    public final static String PLUGIN = "plugin";
    public final static String PLUGIN_LIST_FILE = "plugin-list-file";

    /** Key in the resource file to read the name of the tool. */
    private final static String TOOL_CMD = "toolCmd";
    /** Key in the resource file to read a short description of the tool. */
    private final static String TOOL_DESCRIPTION = "toolDescription";
    /** Empty string. */
    private final static String EMPTY = "";
    /**
     * Key in resource file for short usage description if no command was given.
     */
    private final static String SHORT_USAGE = "shortUsage";
    /** Prefix used for the options. */
    private final static String OPTION_PREFIX = "--";
    /** Option type to use for commands. */
    private final static OptionTypeString TYPE_COMMANDS = OptionTypeString.getInstance();
    /** Identifier of option type storing the command chosen to execute. */
    public final static String COMMAND = "command";
    /** String containing smaller than character. */
    private final static String SMALLER_THAN = "<";
    /** String containing larger than character. */
    private final static String LARGER_THAN = ">";
    /** String containing single space character. */
    private final static String SPACE = " ";
    /** tool name entry */
    public final static String TOOL_NAME = "toolName";

    public final static String DEFAULT = "default";
    private static Options optionsUsed;
    /** String to disable unchecked warning. */
    private final String UNCHECKED = "unchecked";

    /** The available commands for this option set. */
    private transient Map<String, Command> commands = new LinkedHashMap<>();
    /** Write-protected available commands for external usage. */
    private transient Map<String, Command> commandsExternal = Collections.unmodifiableMap(commands);
    /** Available options in terms of map of option identifier to option. */
    private final Map<String, Option> options = new LinkedHashMap<>();
    /**
     * Available options in terms of map of option identifier to option for
     * external usage.
     */
    private final Map<String, Option> optionsExternal = Collections.unmodifiableMap(options);

    /**
     * Available categories in terms of map of option identifier to category.
     */
    private final Map<String, Category> categories = new LinkedHashMap<>();
    /**
     * Available options in terms of map of option identifier to option for
     * external usage.
     */
    private final Map<String, Category> categoriesExternal = Collections.unmodifiableMap(categories);

    /** Base name of default resource file for the options. */
    private final String defaultResourceBundle;
    /** Tool name, if set */
    private String toolName;
    /** Tool description, if set */
    private String toolDescription;

    public static void set(Options options) {
        Options.optionsUsed = options;
    }

    public static Options get() {
        return optionsUsed;
    }

    /**
     * Creates a new options set. No parameters may be {@code null}.
     * 
     * @param defaultResourceFileName
     *            base name of default resource file to use
     */
    public Options(String defaultResourceFileName) {
        assert defaultResourceFileName != null;
        this.defaultResourceBundle = defaultResourceFileName;
        Option commandOption = new Option.Builder().setOptions(this).setBundleName(defaultResourceFileName)
                .setIdentifier(Options.COMMAND).setType(TYPE_COMMANDS).setDefault(null).setCommandLine(false)
                .setGui(false).setWeb(false).setValue(null).build();

        options.put(Options.COMMAND, commandOption);
    }

    public Options(Enum<?> defaultResourceFileName) {
        this(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, defaultResourceFileName.name()));
    }

    /**
     * Add command to this options set. No parameters may be {@code null}. The
     * options obtained by the method {@link Command#getOptions()} must be
     * identical to this object.
     * 
     * @param command
     *            command to add to this options set
     */
    void addCommand(Command command) {
        assert command != null;
        assert command.getOptions() == this;
        commands.put(command.getIdentifier(), command);
    }

    public Command.Builder addCommand() {
        Command.Builder result = new Command.Builder();
        result.setOptions(this);
        return result;
    }

    /**
     * Add option to this options set. The option may not be {@code null}. The
     * method {@code Option#getOptions()} must return this options set, thus it
     * must have been set in the constructor of the option before.
     * 
     * @param option
     *            option to be added to this options set
     */
    void addOption(Option option) {
        assert option != null;
        assert option.getOptions() == this;
        String oldValue = null;
        String identifier = option.getIdentifier();
        if (options.get(identifier) != null) {
            Option previousOption = options.get(identifier);
            options.remove(identifier);
            oldValue = previousOption.getUnparsed();
        }
        options.put(option.getIdentifier(), option);
        if (oldValue != null) {
            option.unset();
            option.setUnparsed(oldValue);
        }
    }

    /**
     * Add a new option using a builder. This method will return a
     * {@link Option.Builder} object. The options set of the builder returned
     * will be set to this options set, and the resource bundle name will be set
     * to the default resource bundle name of this options set. Note that after
     * the configuration of the builder using its setters,
     * {@link Option.Builder#build()} must be called. Otherwise, the previous
     * setter method calls will have no visible effect. Note that because the
     * setter methods return the builder itself, method chaining in the form of
     * e.g. {@code options.addOption().setIdentifier(identifier).set....build()}
     * can be used to conveniently generate a new option.
     * 
     * @return builder for a new option
     */
    public Option.Builder addOption() {
        Option.Builder builder = new Option.Builder();
        builder.setOptions(this);
        builder.setBundleName(defaultResourceBundle);
        return builder;
    }

    public Category.Builder addCategory() {
        Category.Builder builder = new Category.Builder();
        builder.setOptions(this);
        builder.setBundleName(defaultResourceBundle);
        return builder;
    }

    /**
     * Parse program options from command-line parameters. Option values will be
     * read from the command line parameters, parsed by the according option
     * types of the options given, and stored into the according options.
     * 
     * @param args
     */
    public void parseOptions(String[] args) {
        assert args != null;
        for (String arg : args) {
            assert arg != null;
        }
        if (args.length == 0) {
            return;
        }
        String commandName = args[0];
        ensure(commands.containsKey(commandName), ProblemsOptions.OPTIONS_COMMAND_NOT_VALID, args[0]);
        options.get(Options.COMMAND).set(commandName);

        int argNr = 1;
        String option = null;
        String lastOption = null;
        String currentOption = null;
        while (argNr < args.length) {
            option = null;
            String arg = args[argNr];
            ensure(!arg.equals(OPTION_PREFIX), ProblemsOptions.OPTIONS_PROGRAM_OPTION_NOT_VALID, arg);
            if (arg.length() >= 2 && arg.substring(0, 2).equals(OPTION_PREFIX)) {
                option = arg.substring(2, arg.length());
            }
            ensure(option == null || options.containsKey(option),
                    ProblemsOptions.OPTIONS_PROGRAM_OPTION_NOT_VALID, option);
            ensure(lastOption == null || option == null, ProblemsOptions.OPTIONS_NO_VALUE_FOR_OPTION,
                    lastOption);
            ensure(currentOption != null || option != null, ProblemsOptions.OPTIONS_NO_OPTION_FOR_VALUE, arg);
            if (option == null) {
                String value = arg;
                try {
                    parse(currentOption, value);
                } catch (EPMCException e) {
                    String message = e.getProblem().getMessage(getLocale());
                    MessageFormat formatter = new MessageFormat(EMPTY);
                    formatter.applyPattern(ProblemsOptions.OPTIONS_PARSE_OPTION_FAILED.getMessage(getLocale()));
                    String formattedMessage = formatter.format(new Object[] { currentOption });
                    System.err.println(formattedMessage);
                    formatter.applyPattern(message);
                    formattedMessage = formatter.format(e.getArguments());
                    System.err.println(formattedMessage);
                    System.exit(1);
                }
            }
            lastOption = option;
            if (option != null) {
                currentOption = option;
            }
            argNr++;
        }
        ensure(lastOption == null, ProblemsOptions.OPTIONS_NO_VALUE_FOR_OPTION, lastOption);
    }

    /**
     * Parse the option with the given identifier, thereby setting its value.
     * The option identifier and the value to parse must not be {@code null}.
     * The option with the given identifier must exist in this options set. If
     * the value could be parsed correctly, it will be set (or added to) the
     * option. Otherwise, an {@link EPMCException} will be thrown to provide
     * feedback to the user about the correct usage of the option.
     * 
     * @param option
     *            identifier of the option the value of which to parse
     * @param value
     *            value to parse
     */
    public void parse(String option, String value) {
        assert option != null;
        assert value != null;
        Option opt = options.get(option);
        assert opt != null;
        opt.parse(value);
    }

    public void parse(Enum<?> identifier, String value) {
        assert identifier != null;
        String identifierString = enumToIdentifier(identifier);
        parse(identifierString, value);
    }

    /**
     * Obtain short usage instruction for this options set. The description
     * consists of a remark to use the "help" command to obtain more exhaustive
     * instructions.
     * 
     * @return short usage instruction for this options set
     */
    public String getShortUsage() {
        Locale locale = Locale.getDefault();
        MessageFormat formatter = new MessageFormat(EMPTY);
        ResourceBundle poMsg = ResourceBundle.getBundle(defaultResourceBundle, locale);
        formatter.setLocale(locale);
        formatter.applyPattern(poMsg.getString(SHORT_USAGE));
        String toolShortHelp = SMALLER_THAN + poMsg.getString(TOOL_CMD) + LARGER_THAN + SPACE + HELP;
        Object[] args = { toolShortHelp };
        return formatter.format(args);
    }

    /**
     * Obtain value of a given option as a {@link String}. If there is no
     * according option with the identifier, {@code null} will be returned. If
     * an option with this value exist but is not a string, the value will be
     * transformed to a {@link String} using the {@code toString()} of the given
     * object. However, if the value stored is an enum, this string will be
     * transformed to lower case and '_' will be transformed to '-'. None of the
     * parameters may be {@code null}.
     * 
     * @param identifier
     *            identifier of option to get {@link String} value of
     * @return value of a given option as a {@link String}
     */
    public String getString(String identifier) {
        assert identifier != null;
        Option got = options.get(identifier);
        if (got == null || got.get() == null) {
            return null;
        }
        String string = got.getString();
        if (got.get() instanceof Enum<?>) {
            string = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, string);
        }
        return string;
    }

    public String getString(Enum<?> identifier) {
        String identifierName = enumToIdentifier(identifier);
        return getString(identifierName);
    }

    /**
     * Obtain option value as boolean. The method tries to obtain a boolean
     * value from the option with the given identifier using the method
     * {@link Option#getBoolean()}. The identifier parameter must not be
     * {@code null}. The option with the given identifier must exist in this
     * options set.
     * 
     * @param identifier
     *            identifier of option from which to read boolean from
     * @return option value as boolean
     */
    public boolean getBoolean(String identifier) {
        assert identifier != null;
        Option option = options.get(identifier);
        assert option != null;
        return option.getBoolean();
    }

    public boolean getBoolean(Enum<?> identifier) {
        assert identifier != null;
        String identifierName = enumToIdentifier(identifier);
        return getBoolean(identifierName);
    }

    /**
     * Obtain option value as double. The method tries to obtain a double value
     * from the option with the given identifier using the method
     * {@link Option#getDouble()}. The identifier parameter must not be
     * {@code null}. The option with the given identifier must exist in this
     * options set.
     * 
     * @param identifier
     *            identifier of option from which to read double from
     * @return option value as boolean
     */
    public double getDouble(String identifier) {
        assert identifier != null;
        Option option = options.get(identifier);
        assert option != null;
        return option.getDouble();
    }

    public double getDouble(Enum<?> identifier) {
        assert identifier != null;
        String identifierString = enumToIdentifier(identifier);
        return getDouble(identifierString);
    }

    /**
     * Obtain option value as enum. The method tries to obtain a enum value from
     * the option with the given identifier using the method
     * {@link Option#getEnum()}. The identifier parameter must not be
     * {@code null}. The option with the given identifier must exist in this
     * options set.
     * 
     * @param identifier
     *            identifier of option from which to read enum from
     * @return option value as boolean
     */
    public <T extends Enum<T>> T getEnum(String identifier) {
        assert identifier != null;
        Option option = options.get(identifier);
        assert option != null;
        return option.getEnum();
    }

    public <T extends Enum<T>> T getEnum(Enum<?> identifier) {
        assert identifier != null;
        String identifierName = enumToIdentifier(identifier);
        return getEnum(identifierName);
    }

    /**
     * Obtain option value and cast it to given class. If there is no option
     * with the given identifier, {@code null} will be returned. If such an
     * option exists, its value may be read using {@link Option#get()} and the
     * result will be casted to the given class. Note that this will result in a
     * {@link ClassCastException} in case the cast cannot be performed for the
     * reason described in the documentation of the exception.
     * 
     * @param identifier
     *            identifier of option to get value of
     * @return casted option value
     */
    public <T> T get(String identifier) {
        assert identifier != null;
        Option option = options.get(identifier);
        if (option == null) {
            return null;
        }
        @SuppressWarnings(UNCHECKED)
        T t = (T) option.get();
        return t;
    }

    public <T> T get(Enum<?> identifier) {
        assert identifier != null;
        String identifierName = enumToIdentifier(identifier);
        return get(identifierName);
    }

    /**
     * Return type of the option with the given identifier. If an option with
     * this identifier does not exists, the method will return {@code null}.
     * Note that a {@link ClassCastException} may occur if the option type
     * cannot be casted to the template parameter with which this method is
     * called. The identifier parameter must not be {@code null}.
     * 
     * @param identifier
     *            parameter of option to get type of
     * @return type of option with given identifier, or {@code null}
     */
    public <T extends OptionType> T getType(String identifier) {
        assert identifier != null;
        Option option = options.get(identifier);
        if (option == null) {
            return null;
        }
        @SuppressWarnings(UNCHECKED)
        T t = (T) option.getType();
        return t;
    }

    public <T extends OptionType> T getType(Enum<?> identifier) {
        assert identifier != null;
        String identifierString = enumToIdentifier(identifier);
        return getType(identifierString);
    }

    public String getAndUnparse(String identifier) {
        assert identifier != null;
        Option option = options.get(identifier);
        if (option == null) {
            return null;
        }
        return option.getType().unparse(option.get());
    }

    public String getAndUnparse(Enum<?> identifier) {
        assert identifier != null;
        String identifierString = enumToIdentifier(identifier);
        return getAndUnparse(identifierString);
    }

    /**
     * Obtain unparsed value of a given option. This function is particularly
     * intended to be used for options which do not have a type set and also
     * have no parsed value set, such that {@link #get(String)} cannot be used
     * to read the option information. The identifier parameter may not be
     * {@code null}.
     * 
     * @param identifier
     *            identifier of option to get value of
     * @return unparsed value of the given option
     */
    public String getUnparsed(String identifier) {
        assert identifier != null;
        Option option = options.get(identifier);
        if (option == null) {
            return null;
        }
        return option.getUnparsed();
    }

    public String getUnparsed(Enum<?> identifier) {
        assert identifier != null;
        String identifierString = enumToIdentifier(identifier);
        return getUnparsed(identifierString);
    }

    /**
     * Obtain option value as map from {@code String} to given class. The method
     * tries to obtain a map value from the option with the given identifier
     * using the method {@link Option#getMap()}. The identifier parameter must
     * not be {@code null}. The option with the given identifier must exist in
     * this options set.
     * 
     * @param identifier
     *            identifier of option from which to read map from
     * @return option value as boolean
     */
    public <V> Map<String, V> getMap(String identifier) {
        assert identifier != null;
        Option option = options.get(identifier);
        assert option != null;
        return option.getMap();
    }

    public <V> Map<String, V> getMap(Enum<?> identifier) {
        assert identifier != null;
        String identifierString = enumToIdentifier(identifier);
        return getMap(identifierString);
    }

    /**
     * Obtain option value as integer. The method tries to obtain an integer
     * value from the option with the given identifier using the method
     * {@link Option#getInteger()}. The identifier parameter must not be
     * {@code null}. The option with the given identifier must exist in this
     * options set.
     * 
     * @param identifier
     *            identifier of option from which to read integer from
     * @return option value as boolean
     */
    public int getInteger(String identifier) {
        assert identifier != null;
        Option option = options.get(identifier);
        assert option != null;
        return option.getInteger();
    }

    public int getInteger(Enum<?> identifier) {
        assert identifier != null;
        String identifierName = enumToIdentifier(identifier);
        return getInteger(identifierName);
    }

    /**
     * Obtain option value as long. The method tries to obtain a long value from
     * the option with the given identifier using the method
     * {@link Option#getLong()}. The identifier parameter must not be
     * {@code null}. The option with the given identifier must exist in this
     * options set.
     * 
     * @param identifier
     *            identifier of option from which to read long from
     * @return option value as boolean
     */
    public long getLong(String identifier) {
        assert identifier != null;
        Option option = options.get(identifier);
        return option.getLong();
    }

    public long getLong(Enum<?> identifier) {
        assert identifier != null;
        String identifierString = enumToIdentifier(identifier);
        return getLong(identifierString);
    }

    /**
     * Set value of option with given identifier. The method tries to obtain the
     * option from this options set with the give identifier. If there is no
     * such option, a non-visible option will be created instead. If the value
     * parameter is a {@link String}, the option value will be unset using
     * {@link Option#unset()} and the value be parsed by
     * {@link Option#parse(String)}. Otherwise, the value will be set using
     * {@link Option#set(Object)}. None of the parameters of the method may be
     * {@code null}.
     * 
     * @param identifier
     *            identifier of option to set
     * @param value
     *            value to set or parse for option
     */
    public void set(String identifier, Object value) {
        assert identifier != null;
        assert value != null;
        boolean wasNull = false;
        Option option = options.get(identifier);
        if (option == null) {
            wasNull = true;
            Option newOption = new Option.Builder().setOptions(this).setBundleName(defaultResourceBundle)
                    .setIdentifier(identifier).setDefault(value).build();
            options.put(identifier, newOption);
            option = options.get(identifier);
        }
        if (value instanceof String && !wasNull) {
            option.reset();
            option.unset();
            option.parse((String) value);
        } else {
            option.set(value);
        }
    }

    public void set(Enum<?> identifier, Object value) {
        assert identifier != null;
        String identifierName = enumToIdentifier(identifier);
        set(identifierName, value);
    }

    public boolean wasSet(String identifier) {
        assert identifier != null;
        Option option = options.get(identifier);
        if (option == null) {
            return false;
        }
        return option.wasSet();
    }

    public boolean wasSet(Enum<?> identifier) {
        assert identifier != null;
        String identifierName = enumToIdentifier(identifier);
        return wasSet(identifierName);
    }

    /**
     * Unset the option with the given identifier. For this, the
     * {@link Option#unset()} method of the option obtained will be used. If the
     * option with this identifiers is not contained in this options set, the
     * call to this method has no effect. The identifier parameter may not be
     * {@code null}.
     * 
     * @param option
     *            the identifier of the option to unset
     */
    public void unset(String option) {
        assert option != null;
        Option got = options.get(option);
        if (got != null) {
            options.get(option).unset();
        }
    }

    public void unset(Enum<?> identifier) {
        assert identifier != null;
        String identifierString = enumToIdentifier(identifier);
        unset(identifierString);
    }

    /**
     * Resets values of all options to default values, except {@link #PLUGIN}.
     * Note that the options are not removed, just reset.
     */
    public void reset() {
        for (Entry<String, Option> entry : options.entrySet()) {
            if (!entry.getKey().equals(PLUGIN) && !entry.getKey().equals(PLUGIN_LIST_FILE)) {
                entry.getValue().reset();
            }
        }
    }

    /**
     * Obtain option value of option with given identifier as {@link String}
     * {@link List}. None of the parameters may be {@code null}. If the option
     * with the given identifier does not exist, {@code null} will be returned.
     * 
     * @param option
     *            identifier of option the value of which to obtain
     * @return value of option with given identifier as {@link String}
     *         {@link List}
     */
    public List<String> getStringList(String option) {
        assert option != null;
        List<String> result = get(option);
        return result;
    }

    public List<String> getStringList(Enum<?> identifier) {
        assert identifier != null;
        String identifierString = enumToIdentifier(identifier);
        return getStringList(identifierString);
    }

    /**
     * Obtain the option with the given identifier. If an option with this
     * identifier is not contained in the options set, {@code null} will be
     * returned.
     * 
     * @param name
     *            identifier of the option to get
     * @return option with specified identifier, or {@code null}
     */
    public Option getOption(String name) {
        return options.get(name);
    }

    public Option getOption(Enum<?> identifier) {
        assert identifier != null;
        String identifierString = enumToIdentifier(identifier);
        return getOption(identifierString);
    }

    /**
     * Obtain the locale specified for this options set.
     * 
     * @return locale specified for this options set
     */
    public Locale getLocale() {
        return Locale.getDefault();
    }

    /**
     * Obtain the default resource file name of this options set.
     * 
     * @return default resource file name of this options set
     */
    public String getResourceFileName() {
        return defaultResourceBundle;
    }

    /**
     * Remove option with given identifier from the option set.
     * 
     * @param option
     *            identifier of the option to remove
     */
    public void removeOption(String option) {
        assert option != null;
        options.remove(option);
    }

    public void removeOption(Enum<?> identifier) {
        assert identifier != null;
        String identifierString = enumToIdentifier(identifier);
        removeOption(identifierString);
    }

    /**
     * Deserialize options. We do not read the commands, as this would lead to
     * problems if they are objects of classes defined in plugins.
     * 
     * @param in
     *            stream to read options from
     * @throws IOException
     *             thrown in case of I/O problems
     * @throws ClassNotFoundException
     *             thrown if a class could not be found
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        commands = new LinkedHashMap<>();
        Option commandOption = new Option.Builder().setOptions(this).setBundleName(defaultResourceBundle)
                .setIdentifier(Options.COMMAND).setType(TYPE_COMMANDS).build();
        addOption(commandOption);
    }

    @Override
    public Options clone() {
        Options result = new Options(defaultResourceBundle);
        result.commands.clear();
        for (Entry<String, Command> entry : commands.entrySet()) {
            Command command = entry.getValue();
            Command clone = command.toBuilder().setOptions(result).build();
            result.commands.put(entry.getKey(), clone);
        }
        for (Entry<String, Option> entry : options.entrySet()) {
            Option option = entry.getValue();
            Option clone = option.toBuilder().setOptions(result).build();
            result.options.put(entry.getKey(), clone);
        }
        return result;
    }

    /**
     * Clears everything in this options set. All options, commands, etc. are
     * removed. Note that this includes the option with the identifier
     * {@link Options#PLUGIN}.
     */
    public void clear() {
        commands.clear();
        options.clear();
    }

    /**
     * Obtain a map from identifiers to commands. The map returned is
     * write-protected. Commands should be added or removed by using the
     * according functions of this class, not by trying to modify the map.
     * 
     * @return map from identifiers to commands
     */
    public Map<String, Command> getCommands() {
        return commandsExternal;
    }

    /**
     * Obtain resource bundle in locale set for this option The base name of the
     * resource bundle is given by {@link #BUNDLE_NAME}. The language used is
     * given by the method {@link Options#getLocale()} of the object
     * {@link #options}.
     * 
     * @param bundleName
     *            base name of resource bundle
     * @return resource bundle to use
     */
    public ResourceBundle getBundle(String bundleName) {
        assert bundleName != null;
        ResourceBundle poMsg = null;
        poMsg = ResourceBundle.getBundle(bundleName, getLocale(), Thread.currentThread().getContextClassLoader());
        return poMsg;
    }

    /**
     * Disable option with given identifier. If there is no option with the
     * given identifier, a call to this method has no effect. If the given
     * option is present in this options set, it will be hidden from the user,
     * such that it no longer appears on the command line, GUI, or web
     * interface. It will not be removed from the options set. The identifier
     * parameter may not be {@code null}.
     * 
     * @param identifier
     *            identifier of the option to disable
     */
    public void disableOption(String identifier) {
        assert identifier != null;
        Option option = options.get(identifier);
        if (option == null) {
            return;
        }
        options.put(identifier, option.toBuilder().setCommandLine(false).setGui(false).setWeb(false).build());
    }

    public void disableOption(Enum<?> identifier) {
        assert identifier != null;
        String identifierString = enumToIdentifier(identifier);
        disableOption(identifierString);
    }

    /**
     * Set the name of the tool.
     * 
     * @param toolName
     *            naqme to set for this tool
     * @see #getToolName()
     */
    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    /**
     * Obtain name of the tool. If the name has been set to a non-{@code null}
     * value using {@link #setToolName(String)}, the name set will be returned.
     * Otherwise, the resource string denoted by {@link Options#TOOL_NAME} will
     * be returned.
     * 
     * @return name of the tool
     */
    public String getToolName() {
        if (toolName != null) {
            return toolName;
        } else {
            Locale locale = Locale.getDefault();
            ResourceBundle poMsg = ResourceBundle.getBundle(defaultResourceBundle, locale);
            return poMsg.getString(TOOL_NAME);
        }
    }

    /**
     * Sets the description of the tool.
     * 
     * @param toolDescription
     *            description to set for the tool
     * @see #getToolDescription()
     */
    public void setToolDescription(String toolDescription) {
        this.toolDescription = toolDescription;
    }

    /**
     * Obtain description of the tool. If the description has been set to a
     * non-{@code null} value using {@link #setToolDescription(String)}, the
     * description set will be returned. Otherwise, the resource string denoted
     * by {@link Options#TOOL_DESCRIPTION} will be returned.
     * 
     * @return description of the tool
     */
    public String getToolDescription() {
        if (toolDescription != null) {
            return toolDescription;
        } else {
            Locale locale = Locale.getDefault();
            ResourceBundle poMsg = ResourceBundle.getBundle(defaultResourceBundle, locale);
            return poMsg.getString(TOOL_DESCRIPTION);
        }
    }

    /**
     * Return command with given identifier. Returns {@code null} if given
     * command cannot be found. The identifier parameter may not be
     * {@code null}.
     * 
     * @param identifier
     *            identifier of command to obtain
     * @return command of given identifier, or {@code null}
     */
    public Command getCommand(String identifier) {
        assert identifier != null;
        return commands.get(identifier);
    }

    /**
     * Get map of all options. The result is a map from option names to the
     * according options
     * 
     * @return map mapping option names to the according option
     */
    public Map<String, Option> getAllOptions() {
        return optionsExternal;
    }

    public Map<String, Category> getAllCategories() {
        return categoriesExternal;
    }

    public void addCategory(Category category) {
        assert category != null;
        this.categories.put(category.getIdentifier(), category);
    }

    public Category getCategory(String category) {
        assert category != null;
        return categories.get(category);
    }

    private String enumToIdentifier(Enum<?> whichEnum) {
        assert whichEnum != null;
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, whichEnum.name());
    }
}
