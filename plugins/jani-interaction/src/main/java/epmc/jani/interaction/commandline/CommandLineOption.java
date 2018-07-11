package epmc.jani.interaction.commandline;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;

import com.google.common.base.CaseFormat;

import epmc.options.Category;
import epmc.util.UtilJSON;

public final class CommandLineOption {
    public final static class Builder {
        private JsonObject json;
        private Map<String, CommandLineCategory> categories;
        private String bundleName;
        private String identifier;
        private String type;
        private String defaultValue;
        private String category;

        Builder setJSON(JsonObject json) {
            this.json = json;
            return this;
        }
        
        Builder setCategories(Map<String, CommandLineCategory> categories) {
            this.categories = categories;
            return this;
        }
        
        public Builder setBundleName(String bundleName) {
            this.bundleName = bundleName;
            return this;
        }

        public Builder setBundleName(Enum<?> bundleName) {
            this.bundleName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, bundleName.name());
            return this;
        }

        public Builder setIdentifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder setIdentifier(Enum<?> identifier) {
            this.identifier = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, identifier.name());
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder setDefault(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder setCategory(String category) {
            this.category = category;
            return this;
        }

        public Builder setCategory(Enum<?> category) {
            this.category = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, category.name());
            return this;
        }

        public Builder setCategory(Category category) {
            this.category = category != null
                    ? category.getIdentifier()
                            : null;
                    return this;
        }
        
        CommandLineOption build() {
            return new CommandLineOption(this);
        }
    }
    
    private final static String EMPTY = "";
    private final static String SEPARATOR = ",";
    private final static String PIPE = "|";
    private final static String ID = "id";
    private final static String NAME = "name";
    private final static String DESCRIPTION = "description";
    private final static String TYPE = "type";
    private final static String DEFAULT_VALUE = "default-value";
    private final static String CATEGORY = "category";
    private final static String X_PRECISE_TYPE = "x-precise-type";
    private final static String X_PRECISE_CATEGORY = "x-precise-category";
    private final static String SHORT_PREFIX = "short-";
    private final static String BOOL = "bool";
    private final static String REAL = "real";
    private final static String INT = "int";
    
    private final CommandLineOptionUsage usage;
    private final String identifier;
    private final String name;
    private final String type;
    private final String description;
    private final String preciseType;
    private final Object defaultValue;
    private final CommandLineCategory category;
    private final CommandLineCategory preciseCategory;
    
    private Object value;
 
    private CommandLineOption(Builder builder) {
        assert builder != null;
        if (builder.json != null) {
            assert builder.categories != null;
            usage = CommandLineOptionUsage.SERVER;
            identifier = UtilJSON.getString(builder.json, ID);
            name = UtilJSON.getString(builder.json, NAME);
            description = UtilJSON.getStringOrNull(builder.json, DESCRIPTION);
            type = parseType(builder.json);
            preciseType = UtilJSON.getStringOrNull(builder.json, X_PRECISE_TYPE);
            defaultValue = parseDefaultValue(builder.json);
            category = parseCategory(builder.json, builder.categories);
            preciseCategory = parsePreciseCategory(builder.json, builder.categories);
        } else {
            usage = CommandLineOptionUsage.CLIENT;
            identifier = builder.identifier;
            name = getResourceString(SHORT_PREFIX + builder.identifier,
                    builder.bundleName);
            description = null;
            type = builder.type;
            preciseType = builder.type;
            defaultValue = builder.defaultValue;
            if (builder.categories != null) {
                category = builder.categories.get(builder.category);
            } else {
                category = null;
            }
            if (builder.categories != null) {
                preciseCategory = builder.categories.get(builder.category);
            } else {
                preciseCategory = null;
            }
        }
    }

    private static String getResourceString(String string, String bundleName) {
        ResourceBundle poMsg = getBundle(bundleName);
        return poMsg.getString(string);
    }

    private static ResourceBundle getBundle(String bundleName) {
        ResourceBundle poMsg = null;
        Locale locale = Locale.getDefault();
        poMsg = ResourceBundle.getBundle(bundleName, locale, Thread.currentThread().getContextClassLoader());
        return poMsg;
    }
    
    private static String parseType(JsonObject object) {
        String typeString;
        JsonValue type = UtilJSON.get(object, TYPE);
        switch (type.getValueType()) {
        case ARRAY:
            JsonArray typeEnum = UtilJSON.toArrayString(type);
            StringBuilder typeBuilder = new StringBuilder();
            for (JsonValue entry : typeEnum) {
                typeBuilder.append(entry.toString()).append(PIPE);
            }
            if (typeBuilder.length() > 0) {
                typeBuilder.replace(typeBuilder.length() - 1, typeBuilder.length(), EMPTY);
            }
            typeString = typeBuilder.toString();
            break;
        case STRING:
            typeString = type.toString();
            break;
        default:
            typeString = null;
            assert false; // TODO
            break;
        }
        return typeString;
    }

    private Object parseDefaultValue(JsonObject object) {
        assert object != null;
        JsonValue defaultValue = UtilJSON.get(object, DEFAULT_VALUE);
        switch (defaultValue.getValueType()) {
        case NULL:
            return null;
        case FALSE:
            return false;
        case TRUE:
            return true;
        case NUMBER:
            JsonNumber jsonNumber = ((JsonNumber) defaultValue);
            if (jsonNumber.isIntegral()) {
                return jsonNumber.bigIntegerValueExact();
            } else {
                return jsonNumber.bigDecimalValue();
            }
        case STRING:
            return defaultValue.toString();
        default:
            assert false; // TODO
            return null;
        }
    }

    private static CommandLineCategory parseCategory(JsonObject object, Map<String, CommandLineCategory> categories) {
        assert object != null;
        assert categories != null;
        String categoryString = UtilJSON.getStringOrNull(object, CATEGORY);
        if (categoryString == null) {
            return null;
        }
        CommandLineCategory category = categories.get(categoryString);
        if (category == null) {
            category = new CommandLineCategory(categoryString, categoryString, null);
            categories.put(categoryString, category);
        }
        return category;
    }
    
    private static CommandLineCategory parsePreciseCategory(JsonObject object, Map<String, CommandLineCategory> categories) {
        String categoryString = UtilJSON.getStringOrNull(object, X_PRECISE_CATEGORY);
        if (categoryString == null) {
            return null;
        }
        return categories.get(categoryString);
    }

    public void parse(String value) {
        if (this.value == null) {
            switch (type) {
            case BOOL:
                this.value = Boolean.parseBoolean(value);
                break;
            case REAL:
                this.value = new BigDecimal(value);
                break;
            case INT:
                this.value = new BigInteger(value);
                break;
            default:
                this.value = value;
            }
        } else {
            this.value = this.value + SEPARATOR + value;
        }
    }

    public String getIdentifier() {
        return identifier;
    }

    public CommandLineCategory getCategory() {
        if (preciseCategory != null) {
            return preciseCategory;
        } else {
            return category;
        }
    }

    public String getDefault() {
        return defaultValue == null ? null : defaultValue.toString();
    }

    public String getTypeInfo() {
        if (preciseType != null) {
            return preciseType;
        } else {
            return type;
        }
    }

    public String getShortDescription() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public CommandLineOptionUsage getUsage() {
        return usage;
    }
    
    public Object getValue() {
        return value;
    }

    public void clearValue() {
        value = null;
    }
}
