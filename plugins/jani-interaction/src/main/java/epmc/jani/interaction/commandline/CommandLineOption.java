package epmc.jani.interaction.commandline;

import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;

import epmc.util.UtilJSON;

public final class CommandLineOption {
    public final static class Builder {
        private JsonObject json;
        private Map<String, CommandLineCategory> categories;

        Builder setJSON(JsonObject json) {
            this.json = json;
            return this;
        }
        
        Builder setCategories(Map<String, CommandLineCategory> categories) {
            this.categories = categories;
            return this;
        }
        
        CommandLineOption build() {
            return new CommandLineOption(this);
        }
    }
    
    private final static String EMPTY = "";
    private final static String SPACE = " ";
    private final static String PIPE = "|";
    private final static String ID = "id";
    private final static String NAME = "name";
    private final static String DESCRIPTION = "description";
    private final static String TYPE = "type";
    private final static String DEFAULT_VALUE = "default-value";
    private final static String CATEGORY = "category";
    private final static String X_PRECISE_TYPE = "x-precise-type";
    private final static String X_PRECISE_CATEGORY = "x-precise-category";
    
    private final CommandLineOptionType where;
    private final String identifier;
    private final String name;
    private final String type;
    private final String description;
    private final String preciseType;
    private final Object defaultValue;
    private final CommandLineCategory category;
    private CommandLineCategory preciseCategory;
    private String value;
 
    public CommandLineOption(Builder builder) {
        assert builder.json != null;
        assert builder.categories != null;
        where = CommandLineOptionType.SERVER;
        identifier = UtilJSON.getString(builder.json, ID);
        name = UtilJSON.getString(builder.json, NAME);
        description = UtilJSON.getStringOrNull(builder.json, DESCRIPTION);
        type = parseType(builder.json);
        preciseType = UtilJSON.getStringOrNull(builder.json, X_PRECISE_TYPE);
        defaultValue = parseDefaultValue(builder.json);
        category = parseCategory(builder.json, builder.categories);
        preciseCategory = parsePreciseCategory(builder.json, builder.categories);
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
            typeBuilder.replace(typeBuilder.length() - 1, typeBuilder.length(), EMPTY);
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
            this.value = value;
        } else {
            this.value = this.value + SPACE + value;
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
}
