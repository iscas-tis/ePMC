package epmc.modelchecker;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.options.UtilOptions;
import epmc.value.ContextValue;
import epmc.value.Type;

// TODO documentation

// TODO check whether works properly - interfaces might change

/**
 * Properties for {@link ModelDummy}.
 * 
 * @author Ernst Moritz Hahn
 */
final class PropertiesDummy implements Properties {
    /** Value context used. */
    private ContextValue contextValue;
    /** Set of properties stored. */
    private final Map<RawProperty,Expression> properties = new LinkedHashMap<>();
    /** Constants stored in these options. */
    private final Map<String,Expression> constants = new LinkedHashMap<>();
    /** Types of constants. */
    private final Map<String,Type> constantTypes = new LinkedHashMap<>();
    /** Labels stored. */
    private final Map<String,Expression> labels = new LinkedHashMap<>();

    /**
     * Create new properties object.
     * The value context parameter must not be {@code null}.
     * 
     * @param contextValue value context to use
     */
    PropertiesDummy(ContextValue contextValue) {
        assert contextValue != null;
        this.contextValue = contextValue;
    }
    
    @Override
    public void parseProperties(InputStream... inputs) throws EPMCException {
        assert inputs != null;
        for (InputStream input : inputs) {
            assert input != null;
        }
        for (InputStream input : inputs) {
            parseProperties(input);
        }
    }
    
    /**
     * Parse properties from a single input stream.
     * The input stream parameter must not be {@code null}.
     * 
     * @param input input string to parse from
     * @throws EPMCException thrown in case of problems
     */
    private void parseProperties(InputStream input) throws EPMCException {
        assert input != null;
        Options options = contextValue.getOptions();
        Property property = UtilOptions.getInstance(options,
                OptionsModelChecker.PROPERTY_INPUT_TYPE);
        RawProperties properties = new RawProperties();
        property.readProperties(properties, input);
        parseProperties(properties);
    }

    /**
     * Parse raw properties.
     * The raw properties parameter must not be {@code null}.
     * 
     * @param rawProperties raw properties to parse
     * @throws EPMCException thrown in case of problems
     */
    private void parseProperties(RawProperties rawProperties) throws EPMCException {
        assert rawProperties != null;
        Options options = contextValue.getOptions();
        Map<String,Object> optionsConsts = options.getMap(OptionsModelChecker.CONST);
        if (optionsConsts == null) {
            optionsConsts = new LinkedHashMap<>();
        }
        for (RawProperty prop : rawProperties.getProperties()) {
            String definition = prop.getDefinition();
            if (definition == null) {
                continue;
            }
            Expression parsed = UtilModelChecker.parseExpression(contextValue, definition);
            properties.put(prop, parsed);
        }
        for (Entry<String,String> entry : rawProperties.getConstants().entrySet()) {
            String name = entry.getKey();
            Object definition = entry.getValue();
            if (definition == null) {
                definition = optionsConsts.get(name);
            }
            Expression expr = null;
            if (definition != null && definition instanceof String) {
                expr = UtilModelChecker.parseExpression(contextValue, ((String) definition));
            } else if (definition != null && definition instanceof Expression) {
                expr = (Expression) definition;
            } else if (definition != null) {
                assert false : definition;
            }
            constants.put(name, expr);
            Type type = UtilModelChecker.parseType(contextValue, rawProperties.getConstantType(name));
            assert type != null;
            constantTypes.put(name, type);
        }
        for (Entry<String,String> entry : rawProperties.getLabels().entrySet()) {
            String name = entry.getKey();
            String definition = entry.getValue();
            Expression expr = null;
            if (definition != null) {
                expr = UtilModelChecker.parseExpression(contextValue, definition);
            }
            labels.put(name, expr);
        }
    }

    @Override
    public List<RawProperty> getRawProperties() {
        return Collections.list(Collections.enumeration(properties.keySet()));
    }

    @Override
    public Expression getParsedProperty(RawProperty property) {
        return properties.get(property);
    }
}
