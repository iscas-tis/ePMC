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

package epmc.modelchecker;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import epmc.error.UtilError;
import epmc.expression.Expression;
import epmc.graph.LowLevel;
import epmc.graph.LowLevel.Builder;
import epmc.modelchecker.error.ProblemsModelChecker;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeConstList;
import epmc.options.OptionTypeMap;
import epmc.options.OptionTypeStringListSubset;
import epmc.options.Options;
import epmc.options.UtilOptions;
import epmc.util.OrderedMap;
import epmc.util.Util;
import epmc.value.Type;

/**
 * Auxiliary functions for model checking.
 * 
 * @author Ernst Moritz Hahn
 */
public final class UtilModelChecker {
    public static Expression parseExpression(String string) {
        return parseExpression(null, string);
    }
    /**
     * Parse expression from string.
     * The string will be parsed using the {@link Property} from
     * {@link OptionsModelChecker#PROPERTY_INPUT_TYPE}.
     * None of the parameters may be {@code null}.
     * @param string string to parse to expression
     * 
     * @return parsed expression
     */
    public static Expression parseExpression(Object identifier, String string) {
        assert string != null;
        Property property = UtilOptions.getInstance(OptionsModelChecker.PROPERTY_INPUT_TYPE);
        InputStream stream = new ByteArrayInputStream(string.getBytes());
        return property.parseExpression(identifier, stream);
    }

    /**
     * Parse type from string.
     * The string will be parsed using the {@link Property} from
     * {@link OptionsModelChecker#PROPERTY_INPUT_TYPE}.
     * None of the parameters may be {@code null}.
     * @param string string to parse to expression
     * 
     * @return parsed expression
     */
    public static Type parseType(Object identifier, String string) {
        assert string != null;
        Property property = UtilOptions.getInstance(OptionsModelChecker.PROPERTY_INPUT_TYPE);
        return property.parseType(identifier, string);
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
        Map<String,Class<?>> lowLevelBuilders = new OrderedMap<>();
        options.set(OptionsModelChecker.LOW_LEVEL_ENGINE_CLASS, lowLevelBuilders);
        Map<String,Class<?>> schedulerPrinters = new OrderedMap<>();
        options.set(OptionsModelChecker.SCHEDULER_PRINTER_CLASS, schedulerPrinters);
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

        options.addOption().setIdentifier(OptionsModelChecker.COMPUTE_SCHEDULER)
        .setBundleName(OptionsModelChecker.OPTIONS_MODEL_CHECKER)
        .setType(OptionTypeBoolean.getInstance())
        .setCommandLine().setGui().setWeb().build();
    }

    public static LowLevel buildLowLevel(
            Model model,
            Engine engine,
            Set<Object> graphProperties,
            Set<Object> nodeProperties,
            Set<Object> edgeProperties) {
        Options options = Options.get();
        Map<String,Class<LowLevel.Builder>> lowLevelBuilders = options.get(OptionsModelChecker.LOW_LEVEL_ENGINE_CLASS);
        for (Class<LowLevel.Builder> clazz : lowLevelBuilders.values()) {
            Builder builder = Util.getInstance(clazz);
            LowLevel lowLevel = builder.setEngine(engine)
                    .setModel(model)
                    .addGraphProperties(graphProperties)
                    .addNodeProperties(nodeProperties)
                    .addEdgeProperties(edgeProperties)
                    .build();
            if (lowLevel != null) {
                return lowLevel;
            }
        }
        UtilError.fail(ProblemsModelChecker.NO_LOW_LEVEL_AVAILABLE,
                model.getIdentifier(), engine);
        assert false;
        return null;
    }

    public static LowLevel buildLowLevel(
            Model model,
            Set<Object> graphProperties,
            Set<Object> nodeProperties,
            Set<Object> edgeProperties) {
        Options options = Options.get();
        Engine engine = UtilOptions.getSingletonInstance(options,
                OptionsModelChecker.ENGINE);
        return buildLowLevel(model, engine, graphProperties, nodeProperties, edgeProperties);
    }

    /**
     * Private constructor to prevent creating instances of this class.
     */
    private UtilModelChecker() {
    }
}
