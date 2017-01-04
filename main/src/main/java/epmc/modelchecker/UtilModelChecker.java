package epmc.modelchecker;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.OptionTypeConstList;
import epmc.options.OptionTypeMap;
import epmc.options.OptionTypeStringListSubset;
import epmc.options.Options;
import epmc.options.UtilOptions;
import epmc.util.OrderedMap;
import epmc.value.ContextValue;
import epmc.value.Type;

/**
 * Auxiliary functions for model checking.
 * 
 * @author Ernst Moritz Hahn
 */
public final class UtilModelChecker {
    /**
     * Parse expression from string.
     * The string will be parsed using the {@link Property} from
     * {@link OptionsModelChecker#PROPERTY_INPUT_TYPE}.
     * None of the parameters may be {@code null}.
     * 
     * @param contextValue value context to use
     * @param string string to parse to expression
     * @return parsed expression
     * @throws EPMCException thrown if expression could not be parsed
     */
    public static Expression parseExpression(ContextValue contextValue, String string) throws EPMCException {
        assert contextValue != null;
        assert string != null;
        Options options = contextValue.getOptions();
        Property property = UtilOptions.getInstance(options,
                OptionsModelChecker.PROPERTY_INPUT_TYPE);
        property.setContext(contextValue);
        InputStream stream = new ByteArrayInputStream(string.getBytes());
        return property.parseExpression(stream);
    }

    /**
     * Parse type from string.
     * The string will be parsed using the {@link Property} from
     * {@link OptionsModelChecker#PROPERTY_INPUT_TYPE}.
     * None of the parameters may be {@code null}.
     * 
     * @param contextExpression expression context to use
     * @param string string to parse to expression
     * @return parsed expression
     * @throws EPMCException thrown if expression could not be parsed
     */
    public static Type parseType(ContextValue contextExpression, String string) throws EPMCException {
        assert contextExpression != null;
        assert string != null;
        Options options = contextExpression.getOptions();
        Property property = UtilOptions.getInstance(options,
                OptionsModelChecker.PROPERTY_INPUT_TYPE);
        property.setContext(contextExpression);
        return property.parseType(string);
    }

    /**
     * Add options of model checker part module.
     * The options parameter must not be {@code null}.
     * 
     * @param options options to add model checker options to
     */
    public static void addOptions(Options options) {
        assert options != null;
        Map<String, Class<?>> engineMap = new OrderedMap<>(true);
        OptionTypeMap<Class<?>> engineType = new OptionTypeMap<>(engineMap);
        options.addOption().setIdentifier(OptionsModelChecker.ENGINE)
            .setBundleName(OptionsModelChecker.OPTIONS_MODEL_CHECKER)
            .setType(engineType)
            .setCommandLine().setGui().setWeb().build();

        Map<String,Class<?>> propertySolvers = new OrderedMap<>();
        options.set(OptionsModelChecker.PROPERTY_SOLVER_CLASS, propertySolvers);
        OptionTypeStringListSubset<Class<?>> propertySolverType = new OptionTypeStringListSubset<>(propertySolvers);
        options.addOption().setIdentifier(OptionsModelChecker.PROPERTY_SOLVER)
            .setBundleName(OptionsModelChecker.OPTIONS_MODEL_CHECKER)
            .setType(propertySolverType)
            .setCommandLine().setGui().setWeb().build();

        OptionTypeConstList typeConstList = OptionTypeConstList.getInstance();
        options.addOption().setIdentifier(OptionsModelChecker.CONST)
            .setBundleName(OptionsModelChecker.OPTIONS_MODEL_CHECKER)
            .setType(typeConstList).setCommandLine().build();
        
        Map<String, Class<?>> propertyMap = new OrderedMap<>(true);
        options.set(OptionsModelChecker.PROPERTY_CLASS, propertyMap);
        OptionTypeMap<Class<?>> propertyInputType = new OptionTypeMap<>(propertyMap);
        options.addOption().setIdentifier(OptionsModelChecker.PROPERTY_INPUT_TYPE)
            .setBundleName(OptionsModelChecker.OPTIONS_MODEL_CHECKER)
            .setType(propertyInputType).setCommandLine().build();
                
        Map<String, Class<?>> modelMap = new OrderedMap<>(true);
        OptionTypeMap<Class<?>> modelInputType = new OptionTypeMap<>(modelMap);
        options.addOption().setIdentifier(OptionsModelChecker.MODEL_INPUT_TYPE)
            .setBundleName(OptionsModelChecker.OPTIONS_MODEL_CHECKER)
            .setType(modelInputType).setCommandLine().build();
    }
    
    /**
     * Private constructor to prevent creating instances of this class.
     */
    private UtilModelChecker() {
    }
}
