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
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

import com.google.common.base.CaseFormat;
import com.google.common.base.MoreObjects;

import java.util.ResourceBundle;

/**
 * One option of a set of {@link Options}.
 * <p>
 * This class is responsible for storing option values and for calling according
 * parsers to parse strings to the correct type of the option. It is also
 * responsible for reading short and long descriptions of the from a resource
 * files. Note that, depending on the {@link OptionType} used, an option may
 * occur one or more times on the command line to read input from the user.
 * </p>
 * <p>
 * Objects of this class are serializable. However, during serialization, values
 * will get lost if it is not possible to store the value of the option in an
 * unparsed form. This happens if the value of the option is set by
 * {@link #set(Object)} and there is no type information to unparse this value
 * to a {@link String}. Also, the {@link OptionType} of the object will
 * not be stored.
 * </p>
 * <p>
 * The reason for performing serialization this way is that all types of values
 * which might be added by plugins can be transferred this way without problems,
 * without the need to transfer classes via RMI, which would be tedious and
 * would not work well together with our plugin approach. After options have
 * been transferred via RMI, the type information will be restored by the
 * then-available plugin classes.
 * </p>
 * 
 * @author Ernst Moritz Hahn
 */
public final class Option implements Serializable, Cloneable {
    /**
     * Builder for an options object.
     * Values should be set using the according {@code set} methods. Note that,
     * because the {@code set} methods return the builder object, it is possible
     * to chain calls such as e.g.
     * {@code builder.setBundleName(bundleName).setIdentifier(identifier). ...}.
     * The {@code set} methods will accept any value legal for their parameter
     * type. After the configuration is finished, the option can be created
     * using {@link #build()}. Note that before calling this method, the methods
     * {@link #setOptions(Options)}, {@link #setBundleName(String)}, and
     * {@link #setIdentifier(String)} must have been called on non-{@code null}
     * values the last time they are called before {@link #build()}.
     * After {@link #build()} has been called, no further calls to any method
     * of the builder is legal. If the options builder was created using
     * {@link Options#addOption()} then {@link #setOptions(Options)} and
     * {@link #setBundleName(String)} will automatically be called setting the
     * bundle to the default bundle of the options set.
     * 
     * @author Ernst Moritz Hahn
     */
    public static final class Builder {
        /** Denotes whether the builder was already used to build an option. */
        private boolean built;
        /** Options set the option belongs to. */
        private Options options;
        /** Base name of resource bundle descriptions are read from. */
        private String bundleName;
        /** Identifier of the option. */
        private String identifier;
        /** Option type with which String values are parsed, or {@code null}. */
        private OptionType type;
        /** The default value of the option, or {@null}. */
        private Object defaultValue;
        /** Whether this option can be seen and modified from the command line. */
        private boolean commandLine;
        /** Whether this option can be modified from the standalone GUI. */
        private boolean gui;
        /** Whether this option can be modified from the public web interface. */
        private boolean web;
        /** The value of the option, or {@code null}. */
        private Object value;
        /** The category of the option. */
        private String category;
        private String valueUnparsed;
        private String defaultUnparsed;

        Builder() {
        }

        /**
         * Set the options set the option belongs to.
         * 
         * @param options options set the option belongs to
         * @return {@code this} builder, for setter method chaining
         */
        Builder setOptions(Options options) {
            assert !built;
            this.options = options;
            return this;
        }

        /**
         * Get the options set the option belongs to.
         * 
         * @return options set the option belongs to
         */
        private Options getOptions() {
            assert built;
            return options;
        }

        /**
         * Set the base name of the resource bundle descriptions are read from.
         * 
         * @param bundleName base name of the resource bundle descriptions are read from
         * @return {@code this} builder, for setter method chaining
         */
        public Builder setBundleName(String bundleName) {
            assert !built;
            this.bundleName = bundleName;
            return this;
        }

        // TODO describe
        public Builder setBundleName(Enum<?> bundleName) {
            assert !built;
            this.bundleName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, bundleName.name());
            return this;
        }

        /**
         * Get the base name of the resource bundle descriptions are read from.
         * 
         * @return base name of the resource bundle descriptions are read from
         */
        private String getBundleName() {
            assert built;
            return bundleName;
        }

        /**
         * Set the identifier of the option.
         * 
         * @param identifier identifier of the option
         * @return {@code this} builder, for setter method chaining
         */
        public Builder setIdentifier(String identifier) {
            assert !built;
            this.identifier = identifier;
            return this;
        }

        // TODO describe
        public Builder setIdentifier(Enum<?> identifier) {
            assert !built;
            this.identifier = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, identifier.name());
            return this;
        }

        /**
         * Get the identifier of the option.
         * 
         * @return identifier of the option
         */
        private String getIdentifier() {
            assert built;
            return identifier;
        }

        /**
         * Set the option type with which values of the option are parsed.
         * 
         * @param type option type with which values of the option are parsed
         * @return {@code this} builder, for setter method chaining
         */
        public Builder setType(OptionType type) {
            assert !built;
            this.type = type;
            return this;
        }

        /**
         * Get the option type with which values of the option are parsed.
         * 
         * @return option type with which values of the option are parsed
         */
        private OptionType getType() {
            assert built;
            return type;
        }

        /**
         * Set the default value of the option.
         * 
         * @param defaultValue default value of the option
         * @return {@code this} builder, for setter method chaining
         */
        public Builder setDefault(Object defaultValue) {
            assert !built;
            this.defaultValue = defaultValue;
            return this;
        }

        /**
         * Get the default value of the option.
         * 
         * @return default value of the option
         */
        private Object getDefault() {
            assert built;
            return defaultValue;
        }

        /**
         * Set whether option shall be visible and modifiable from command line.
         * 
         * @param commandLine whether option shall be visible and modifiable from command line
         * @return {@code this} builder, for setter method chaining
         */
        public Builder setCommandLine(boolean commandLine) {
            assert !built;
            this.commandLine = commandLine;
            return this;
        }

        /**
         * Set the option to be visible and modifiable from command line.
         * 
         * @return {@code this} builder, for setter method chaining
         */
        public Builder setCommandLine() {
            assert !built;
            this.commandLine = true;
            return this;
        }

        /**
         * Returns whether option is visible and modifiable from command line.
         * 
         * @return whether option is visible and modifiable from command line
         */
        private boolean isCommandLine() {
            assert built;
            return commandLine;
        }

        /**
         * Set whether option shall be visible and modifiable from GUI.
         * 
         * @param gui whether option shall be visible and modifiable from GUI
         * @return {@code this} builder, for setter method chaining
         */
        public Builder setGui(boolean gui) {
            assert !built;
            this.gui = gui;
            return this;
        }

        /**
         * Set the option to be visible and modifiable from GUI.
         * 
         * @return {@code this} builder, for setter method chaining
         */
        public Builder setGui() {
            assert !built;
            this.gui = true;
            return this;
        }

        /**
         * Returns whether option is visible and modifiable from GUI.
         * 
         * @return whether option is visible and modifiable from GUI
         */
        private boolean isGui() {
            assert built;
            return gui;
        }

        /**
         * Set whether option shall be visible and modifiable in web interface.
         * 
         * @param web whether option shall be visible and modifiable in web interface
         * @return {@code this} builder, for setter method chaining
         */
        public Builder setWeb(boolean web) {
            assert !built;
            this.web = web;
            return this;
        }

        /**
         * Set option to be visiable and modifiable in web interface.
         * 
         * @return {@code this} builder, for setter method chaining
         */
        public Builder setWeb() {
            assert !built;
            this.web = true;
            return this;
        }

        /**
         * Returns whether option is visible and modifiable in web interface.
         * 
         * @return whether option is visible and modifiable in web interface
         */
        private boolean isWeb() {
            assert built;
            return web;
        }

        /**
         * Set the value with which the option is initially created.
         * 
         * @param value value with which the option is initially created
         * @return {@code this} builder, for setter method chaining
         */
        public Builder setValue(Object value) {
            assert !built;
            this.value = value;
            return this;
        }

        public Builder setValueUnparsed(String valueUnparsed) {
            this.valueUnparsed = valueUnparsed;
            return this;
        }

        // TODO describe
        private String getValueUnparsed() {
            return valueUnparsed;
        }

        // TODO describe
        public Builder setDefaultUnparsed(String defaultUnparsed) {
            this.defaultUnparsed = defaultUnparsed;
            return this;
        }

        // TODO describe
        private String getDefaultUnparsed() {
            return defaultUnparsed;
        }

        /**
         * Get the value with which the option is initially created.
         * 
         * @return value with which the option is initially created
         */
        private Object getValue() {
            assert built;
            return value;
        }

        /**
         * Set category of the option.
         * 
         * @param category category of the option
         */
        public Builder setCategory(String category) {
            this.category = category;
            return this;
        }

        // TODO describe
        public Builder setCategory(Enum<?> category) {
            this.category = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, category.name());
            return this;
        }


        /**
         * Set category of the option.
         * 
         * @param category category of the option
         */
        public Builder setCategory(Category category) {
            this.category = category != null
                    ? category.getIdentifier()
                            : null;
                    return this;
        }

        /**
         * Get the category of the option.
         * 
         * @return category of the option.
         */
        private String getCategory() {
            return category;
        }

        /**
         * Build a new option.
         * The values set previously using the setter methods will be used to
         * instantiate the option. Before calling this method, the methods
         * {@link #setOptions(Options)}, {@link #setBundleName(String)}, and
         * {@link #setIdentifier(String)} must have been called on
         * non-{@code null} values the last time they are called before
         * {@link #build()}. After {@link #build()} has been called, no further
         * calls to any method of the builder is legal. If the options builder
         * was created using {@link Options#addOption()} then
         * {@link #setOptions(Options)} and {@link #setBundleName(String)} will
         * automatically be called setting the bundle to the default bundle of
         * the options set.
         * 
         * @return newly created option
         */
        public Option build() {
            assert !built;
            assert options != null;
            assert identifier != null;
            assert bundleName != null;
            built = true;
            Option option = new Option(this);
            options.addOption(option);
            return option;
        }
    }

    /** Serial version UID - 1L, as I don't know any better. */
    private static final long serialVersionUID = 1L;
    /** Message to print in <code>assert</code> for missing resource. */
    private final static String MISSING_RESOUCE = "Missing resource";
    /** String containing space. */
    private static final String SPACE = " ";
    /** Prefix for short description in resource file. */
    private final static String SHORT_PREFIX = "short-";
    /** Prefix for long description in resource file. */
    private final static String LONG_PREFIX = "long-";
    /** String "bundleName", for {@link #toString()} method. */
    private static final String BUNDLE_NAME = "bundleName";
    /** String "identifier", for {@link #toString()} method. */
    private static final String IDENTIFIER = "identifier";
    /** String "type", for {@link #toString()} method. */
    private static final String TYPE = "type";
    /** String "value", for {@link #toString()} method. */
    private static final String VALUE = "value";
    /** String "unparsedValue", for {@link #toString()} method. */
    private static final String UNPARSED_VALUE = "unparsedValue";
    /** String "defaultValue", for {@link #toString()} method. */
    private static final String DEFAULT_VALUE = "defaultValue";
    /** String "defaultValueUnparsed", for {@link #toString()} method. */
    private static final String DEFAULT_VALUE_UNPARSED = "defaultValueUnparsed";
    /** String "commandLine", for {@link #toString()} method. */
    private static final String COMMAND_LINE = "commandLine";
    /** String "gui", for {@link #toString()} method. */
    private static final String GUI = "gui";
    /** String "web", for {@link #toString()} method. */
    private static final String WEB = "web";
    /** String "unchecked" to suppress according warning. */
    private final static String UNCHECKED = "unchecked";

    /** The options collection this option belongs to. */
    private final Options options;
    /** Base name of resource bundle descriptions are read from. */
    private final String bundleName;
    /** Identifier of the option. */
    private final String identifier;
    /** Type of the option, responsible for parsing strings to option values.
     * This field is transient, because otherwise it would be necessary to
     * transfer possible option types specified by plugins over the RMI
     * interface. Transferring classes over RMI is something I want to avoid
     * because it would lead to problems in combination with the plugin
     * approach.
     * */
    /** Option type with which String values are parsed, or {@code null}. */
    private final transient OptionType type;
    /** Whether this option can be seen and modified from the command line. */
    private final boolean commandLine;
    /** Whether this option can be modified from the standalone GUI. */
    private final boolean gui;
    /** Whether this option can be modified from the public web interface. */
    private final boolean web;
    /** Unparsed value currently stored in option. This value will be used as
     * the value of the option of {@link #type} is set and this value is also
     * set. */
    private String unparsedValue;
    /** Parsed value currently stored in the option. This value will be used
     * if {@link #unparsedValue} is not available. Note that this value will
     * not be transferred by serialization. */
    private transient Object value;
    /** The default unparsed value of the option, or {@code null}.
     * This value will be parsed and returned by {@code #get()} if no value is
     * set for this option. If this value is {@code null}, {@link #defaultValue}
     * will be used instead.
     */
    private final String defaultValueUnparsed;
    /** The default parsed value of the option, or {@null}.
     * If this value is {@code null}, the default value of the {@link #type}
     * will be used as the default value. Note that this value will not be
     * transferred using serialization. */
    private final transient Object defaultValue;
    private final Category category;

    // TODO describe
    private Option(Builder builder) {
        assert builder != null;
        assert builder.getOptions() != null;
        assert builder.getIdentifier() != null;
        assert builder.getBundleName() != null;
        this.options = builder.getOptions();
        this.bundleName = builder.getBundleName();
        this.identifier = builder.getIdentifier();
        /* for public options, we check immediately whether according entries
         * in the resource files exist. Currently, we only check for short
         * descriptions, as the long ones have not yet been written in most
         * cases. */
        if (builder.isCommandLine() | builder.isGui() | builder.isWeb()) {
            ResourceBundle poMsg = getBundle();
            assert poMsg != null : builder.getBundleName();
            assert poMsg.containsKey(SHORT_PREFIX + builder.getIdentifier()) :
                MISSING_RESOUCE + SPACE +
                builder.getBundleName() + SPACE + builder.getIdentifier();
        }
        this.type = builder.getType();
        if (builder.getValueUnparsed() != null) {
            this.unparsedValue = builder.getValueUnparsed();
        } else if (builder.getType() != null && builder.getValue() != null) {
            this.unparsedValue = builder.getType().unparse(builder.getValue());
        } else {
            this.value = builder.getValue();
        }
        if (builder.getDefaultUnparsed() != null) {
            this.defaultValueUnparsed = builder.getDefaultUnparsed();
            this.defaultValue = null;
        } else if (builder.getType() != null && builder.getDefault() != null) {
            this.defaultValueUnparsed = builder.getType().unparse(builder.getDefault());
            this.defaultValue = null;
        } else {
            this.defaultValueUnparsed = null;
            this.defaultValue = builder.getDefault();
        }
        this.commandLine = builder.isCommandLine();
        this.gui = builder.isGui();
        this.web = builder.isWeb();
        if (builder.getCategory() == null) {
            this.category = null;
        } else {
            this.category = options.getCategory(builder.getCategory());
        }
    }

    /**
     * Obtain the identifier of this option.
     * 
     * @return identifier of this option
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Obtain short user-readable description of the option.
     * The description shall be short enough to be shown in a single line of
     * around 72 characters.
     * 
     * @return short user-readable description of the option
     */
    public String getShortDescription() {
        ResourceBundle poMsg = getBundle();
        return poMsg.getString(SHORT_PREFIX + identifier);
    }

    /**
     * Obtain long user-readable description of the option.
     * The description is not restricted in size but shall concisely describe
     * the functionality of the option.
     * 
     * @return long user-readable description of the option
     */
    public String getLongDescription() {
        Locale locale = this.options.getLocale();
        assert locale != null;
        assert bundleName != null;
        ResourceBundle poMsg = getBundle();
        return poMsg.getString(LONG_PREFIX + identifier);
    }

    /**
     * Sets the value of the object manually, without parsing a {@link String}.
     * It is also marked as already set. This function can be used if the option
     * shall be set programmatically rather than being set by set user. The
     * value {@code null} is not allowed as parameter. To reset an option, use
     * {@link #unset()} instead.
     * 
     * @param value value to set for the option
     */
    public void set(Object value) {
        assert value != null;
        if (type != null) {
            unparsedValue = type.unparse(value);
        } else {
            this.value = value;
        }
    }

    /**
     * Returns {@code true} iff option was set.
     * This includes only the cases in which the option was set directly
     * with an according method such as {@link #set(Object)}
     * or {@link #setUnparsed(String)} but not the cases in which the option
     * only has a default value but was never set to a value directly.
     * 
     * @return {@code true} iff option was set
     */
    public boolean wasSet() {
        return value != null || unparsedValue != null;
    }
    
    /**
     * Parse an option from a {@link String} by the options {@link OptionType}.
     * The parameter must not be {@code null}. Whether the function may called
     * multiple times before resetting the option depends on
     * {@link OptionType#allowMulti()} of the option type used.
     * 
     * @param value the {@link String} to parse to an option value
     */
    public void parse(String value) {
        assert value != null;
        assert type != null;
        Object previous = null;
        if (unparsedValue != null) {
            previous = type.parse(unparsedValue, null);
        }
        unparsedValue = type.unparse(type.parse(value, previous));
    }

    /**
     * Obtain the value of this option.
     * If a value has been set by {@link #set(Object)} or by
     * {@link #parse(String)}, this value is returned. Otherwise, if the option
     * has a default value, this value is returned. If also no default value
     * exists, the default value of the {@link OptionType} is returned.
     * 
     * @return value of the option
     */
    public Object get() {
        if (this.unparsedValue != null && this.type != null) {
            return parseInternal(unparsedValue);
        } else if (this.value != null) {
            return value;
        } else if (this.defaultValueUnparsed != null && type != null) {
            return parseInternal(defaultValueUnparsed);
        } else if (this.defaultValue != null) {
            return defaultValue;
        } else if (this.type != null) {
            return type.getDefault();
        } else {
            return null;
        }
    }

    /**
     * Get unparsed value of this option.
     * 
     * @return unparsed value of this option
     */
    public String getUnparsed() {
        return unparsedValue;
    }

    /**
     * Obtain description of the type of the option.
     * The result is obtained using the method {@link OptionType#getInfo()} of
     * the option type used. If the option does not contain an option type,
     * {@code null} is returned.
     * 
     * @return description of the type of the option
     */
    public String getTypeInfo() {
        if (type == null) {
            return null;
        } else {
            return type.getInfo();
        }
    }

    /**
     * Return the type used to parse Strings to values of this option.
     * 
     * @return type used to parse Strings to values of this option
     */
    public <T extends OptionType> T getType() {
        @SuppressWarnings("unchecked")
        T typeCasted = (T) this.type;
        return typeCasted;
    }

    /**
     * Reset the option value to its default value and mark it as not set.
     */
    public void reset() {
        this.value = null;
        this.unparsedValue = null;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add(BUNDLE_NAME, bundleName)  
                .add(IDENTIFIER, identifier)
                .add(TYPE, type)
                .add(VALUE, value)
                .add(UNPARSED_VALUE, unparsedValue)
                .add(DEFAULT_VALUE, defaultValue)
                .add(DEFAULT_VALUE_UNPARSED, defaultValueUnparsed)
                .add(COMMAND_LINE, commandLine)
                .add(GUI, gui)
                .add(WEB, web)
                .toString();
    }

    /**
     * Obtain the default value of the option.
     * If the option was constructed with a default value, this value is
     * returned. Otherwise, the default value of the {@link OptionType} used
     * is returned.
     * 
     * @return default value of the option
     */
    public Object getDefault() {
        if (defaultValueUnparsed != null && type != null) {
            return parseInternal(defaultValueUnparsed);
        } else if (defaultValue != null) {
            return defaultValue;
        } else if (type != null) {
            return type.getDefault();
        } else {
            return null;
        }
    }

    /**
     * Return the default value of the option if given during construction.
     * Returns the default value given when construction the option, or {@code
     * null} if the default value given was {@code null}. In contrast to
     * {@link #getDefault()}, this method does never return the value of the
     * {@link OptionType} of the option.
     * 
     * @return
     */
    Object getDefaultInternal() {
        if (defaultValueUnparsed != null && type != null) {
            return parseInternal(defaultValueUnparsed);
        } else {
            return defaultValue;
        }
    }

    /**
     * Returns whether this option can be set from the command line.
     * 
     * @return whether this option can be set from the command line
     */
    public boolean isCommandLine() {
        return this.commandLine;
    }

    /**
     * Returns whether this option can be set in the standalone GUI.
     * 
     * @return whether this option can be set in the standalone GUI
     */
    public boolean isGUI() {
        return this.gui;
    }

    /**
     * Returns whether this option can be set in the public web interface.
     * 
     * @return whether this option can be set in the public web interface
     */
    public boolean isWeb() {
        return this.web;
    }

    /**
     * Read resource bundle from base name given with {@link Options} {@link Locale}.
     * @return
     */
    private ResourceBundle getBundle() {
        ResourceBundle poMsg = null;
        Locale locale = this.options.getLocale();
        poMsg = ResourceBundle.getBundle(this.bundleName, locale, Thread.currentThread().getContextClassLoader());
        return poMsg;
    }

    /**
     * Returns whether the option has a value.
     * 
     * @return whether the option has a value
     */
    public boolean hasValue() {
        return get() != null;
    }

    /**
     * Return value as string.
     * If the value contained in the option is a {@link String}, the value will
     * be returned. If the option contains an object of another type, its
     * {@link Object#toString()} method will be called
     * 
     * @return value as string
     */
    public String getString() {
        Object got = get();
        if (got instanceof String) {
            return (String) got;
        } else {
            return got.toString();
        }
    }

    /**
     * Return value as boolean.
     * If the value contained in the option is a {@link Boolean}, the value will
     * unwrapped and returned as a primitive type. Otherwise, if the value
     * contained is a {@link String}, the method
     * {@link Boolean#parseBoolean(String)} will be used to obtain a boolean. If
     * the value contained is neither a {@link Boolean} nor a {@link String}, an
     * {@link AssertionError} will be thrown if assertions are enabled.
     * 
     * @return value as boolean
     */
    public boolean getBoolean() {
        Object got = get();
        if (got instanceof Boolean) {
            return (Boolean) got;
        } else if (got instanceof String) {
            return Boolean.parseBoolean((String) got);
        } else {
            assert false;
            return false;
        }
    }

    /**
     * Return value as double.
     * If the value contained in the option is a {@link Double}, the value will
     * unwrapped and returned as a primitive type. Otherwise, if the value
     * contained is a {@link String}, the method
     * {@link Double#parseDouble(String)} will be used to obtain a double. If
     * the value contained is neither a {@link Double} nor a {@link String}, an
     * {@link AssertionError} will be thrown if assertions are enabled.
     * 
     * @return value as double
     */
    public double getDouble() {
        Object got = get();
        if (got instanceof Double) {
            return (Double) got;
        } else if (got instanceof String) {
            return Double.parseDouble((String) got);
        } else {
            assert false;
            return -1.0;
        }
    }

    /**
     * Return value as enum.
     * If the value contained in the option is a {@link Enum}, the value will
     * returned. Otherwise, an {@link AssertionError} will be thrown if
     * assertions are enabled.
     * 
     * @return value as enum
     */
    public <T extends Enum<T>> T getEnum() {
        assert get() instanceof Enum;
        @SuppressWarnings(UNCHECKED)
        T t = (T) get();
        return t;
    }

    /**
     * Return value as map.
     * If the value is a {@link Map}, the value will be casted to a map and
     * returned. Otherwise, an exception will occur.
     * 
     * @return value as map
     */
    public <K,V> Map<K,V> getMap() {
        assert get() instanceof Map;
        @SuppressWarnings(UNCHECKED)
        Map<K,V> map = (Map<K,V>) get();
        return map;
    }

    /**
     * Return value as integer.
     * If the value contained in the option is a {@link Integer}, the value will
     * unwrapped and returned as a primitive type. Otherwise, if the value
     * contained is a {@link String}, the method
     * {@link Integer#parseInt(String)} will be used to obtain an integer. If
     * the value contained is neither an {@link Integer} nor a {@link String},
     * an {@link AssertionError} will be thrown if assertions are enabled.
     * 
     * @return value as integer
     */
    public int getInteger() {
        Object got = get();
        assert got != null : identifier;
        if (got instanceof Integer) {
            return (Integer) got;
        } else if (got instanceof String) {
            return Integer.parseInt((String) got);
        } else {
            assert false : got + SPACE + got.getClass();
        return -1;
        }
    }

    /**
     * Return value as long.
     * If the value contained in the option is a {@link Long}, the value will
     * unwrapped and returned as a primitive type. Otherwise, if the value
     * contained is a {@link String}, the method
     * {@link Long#parseLong(String)} will be used to obtain a long. If the
     * value contained is neither a {@link Long} nor a {@link String}, an
     * {@link AssertionError} will be thrown if assertions are enabled.
     * 
     * @return value as long
     */
    public long getLong() {
        Object got = get();
        if (got instanceof Long) {
            return (Long) got;
        } else if (got instanceof String) {
            return Long.parseLong((String) got);
        } else {
            assert false;
            return -1;
        }        
    }

    /**
     * Unset the value of the option.
     * It is marked as not set, and can thus afterwards again be set by {@link
     * #set(Object)} or {@link #parse(String)}.
     */
    public void unset() {
        value = null;
        unparsedValue = null;
    }

    /**
     * Deserialize option.
     * This method is used for Java deserialization. It provides some more
     * information in case of errors than the default deserialization routine.
     * 
     * @param in input stream from which to read the object
     * @throws IOException thrown in case of I/O errors
     * @throws ClassNotFoundException thrown if a class was not found
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        assert in != null;
        try {
            in.defaultReadObject();
        } catch (ClassNotFoundException | NotSerializableException e) {
            assert false : identifier;
        throw new RuntimeException(identifier, e);
        }
    }    

    /**
     * Get resource bundle base name of this option.
     * 
     * @return bundle name of this option.
     */
    String getBundleName() {
        return bundleName;
    }

    /**
     * Return whether the option has already been set by the user.
     * 
     * @return whether the option has already been set by the user
     */
    public boolean isAlreadyParsed() {
        return unparsedValue != null || value != null;
    }

    @Override
    public Option clone() {
        return toBuilder().build();
    }

    /**
     * Obtain the options set to which this option belongs.
     * 
     * @return the options set to which this option belongs
     */
    Options getOptions() {
        return options;
    }

    /**
     * Parse string to value, ignoring exceptions.
     * This method can be used in cases where we already know that a certain
     * string will be parseable by the options type, so that we can safely
     * ignore the user exception the {@link OptionType#parse(String, Object)}
     * method could potentially throw.
     * 
     * @param unparsedValue value to be parsed
     * @return parsed value
     */
    private Object parseInternal(String unparsedValue) {
        Object result = null;
        result = type.parse(unparsedValue, result);
        return result;
    }

    /**
     * Set the unparsed value of the option.
     * This method is intended to be used after in combination with
     * serialization.
     * 
     * @param unparsedValue to be set as unparsed value for the option
     */
    public void setUnparsed(String unparsedValue) {
        this.unparsedValue = unparsedValue;
    }

    // TODO describe
    public Category getCategory() {
        return category;
    }

    /**
     * Build a builder with the information with which the option was built.
     * 
     * @return builder with the information with which the option was built
     */
    Builder toBuilder() {
        Builder builder = new Builder();
        builder.setBundleName(bundleName);
        builder.setCategory(category);
        builder.setCommandLine(commandLine);
        builder.setDefault(defaultValue);
        builder.setDefaultUnparsed(defaultValueUnparsed);
        builder.setGui(gui);
        builder.setIdentifier(identifier);
        builder.setOptions(options);
        builder.setType(type);
        builder.setValue(value);
        builder.setValueUnparsed(unparsedValue);
        builder.setWeb(web);
        return builder;
    }
}
