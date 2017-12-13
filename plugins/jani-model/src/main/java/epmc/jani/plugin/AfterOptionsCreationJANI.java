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

package epmc.jani.plugin;

import java.util.Map;

import epmc.graph.LowLevel;
import epmc.jani.dd.GraphDDJANI;
import epmc.jani.explorer.AssignmentEvaluator;
import epmc.jani.explorer.AssignmentSimpleEvaluator;
import epmc.jani.explorer.ExplorerExtension;
import epmc.jani.explorer.ExplorerJANI;
import epmc.jani.explorer.LowLevelExplicitBuilder;
import epmc.jani.explorer.OptionsJANIExplorer;
import epmc.jani.explorer.VariableValuesEnumerator;
import epmc.jani.model.ModelExtension;
import epmc.jani.model.ModelExtensionSemantics;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.OptionsJANIModel;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Category;
import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeEnum;
import epmc.options.OptionTypeIntegerNonNegative;
import epmc.options.OptionTypeMap;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;
import epmc.util.OrderedMap;

/**
 * JANI plugin class containing method to execute after options creation.
 * 
 * @author Ernst Moritz Hahn
 */
public final class AfterOptionsCreationJANI implements AfterOptionsCreation {
    /** Identifier of this class. */
    private final static String IDENTIFIER = "after-options-creation-jani";
    /** Default number of action encoding bits. */
    private final static int NUM_ACTION_BITS = 20;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        assert options != null;

        OptionTypeMap<Class<?>> modelInputType = options.getType(OptionsModelChecker.MODEL_INPUT_TYPE);
        assert modelInputType != null;
        modelInputType.put(ModelJANI.IDENTIFIER, ModelJANI.class);

        addOptions(options);
        addSemantics(options);
        addExtensions(options);
        addLowLevels(options);
        
        Map<String,Class<? extends ModelExtension>> modelExtensions = options.get(OptionsJANIModel.JANI_MODEL_EXTENSION_CLASS);
        if (modelExtensions == null) {
            modelExtensions = new OrderedMap<>();
            options.set(OptionsJANIModel.JANI_MODEL_EXTENSION_CLASS, modelExtensions);
        }
        Map<String,Class<? extends ExplorerExtension>> explorerExtensions = options.get(OptionsJANIExplorer.JANI_EXPLORER_EXTENSION_CLASS);
        if (explorerExtensions == null) {
            explorerExtensions = new OrderedMap<>();
            options.set(OptionsJANIExplorer.JANI_EXPLORER_EXTENSION_CLASS, explorerExtensions);
        }
        Map<String,Class<? extends AssignmentEvaluator.Builder>> assignmentEvaluators = options.get(OptionsJANIExplorer.JANI_EXPLORER_ASSIGNMENT_EVALUATOR_CLASS);
        if (assignmentEvaluators == null) {
            assignmentEvaluators = new OrderedMap<>();
            options.set(OptionsJANIExplorer.JANI_EXPLORER_ASSIGNMENT_EVALUATOR_CLASS, assignmentEvaluators);
        }
        assignmentEvaluators.put(AssignmentSimpleEvaluator.IDENTIFIER, AssignmentSimpleEvaluator.Builder.class);
        options.set(OptionsJANIExplorer.JANI_EXPLORER_ASSIGNMENT_EVALUATOR_CLASS, assignmentEvaluators);
    }

    private void addOptions(Options options) {
        assert options != null;
        Category category = options.addCategory()
                .setBundleName(OptionsJANIModel.OPTIONS_JANI_MODEL)
                .setIdentifier(OptionsJANIModel.JANI_MODEL_CATEGORY)
                .build();
        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();
        OptionTypeIntegerNonNegative typeIntegerNonNegative = OptionTypeIntegerNonNegative.getInstance();
        OptionTypeEnum typeExplorer = new OptionTypeEnum(VariableValuesEnumerator.EnumeratorType.class);
        options.addOption().setBundleName(OptionsJANIModel.OPTIONS_JANI_MODEL)
        .setIdentifier(OptionsJANIModel.JANI_FIX_DEADLOCKS)
        .setType(typeBoolean).setDefault(true)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
        options.addOption().setBundleName(OptionsJANIModel.OPTIONS_JANI_MODEL)
        .setIdentifier(OptionsJANIModel.JANI_ACTION_BITS)
        .setType(typeIntegerNonNegative).setDefault(NUM_ACTION_BITS)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
        options.addOption().setBundleName(OptionsJANIExplorer.OPTIONS_JANI_EXPLORER)
        .setIdentifier(OptionsJANIExplorer.JANI_EXPLORER_INITIAL_ENUMERATOR)
        .setType(typeExplorer)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
    }

    private void addSemantics(Options options) {
        assert options != null;
        Map<String, Class<? extends ModelExtensionSemantics>> modelSemanticTypes =
                options.get(OptionsJANIModel.JANI_MODEL_EXTENSION_SEMANTICS);
        if (modelSemanticTypes == null) {
            modelSemanticTypes = new OrderedMap<>(true);
        }
        options.set(OptionsJANIModel.JANI_MODEL_EXTENSION_SEMANTICS, modelSemanticTypes);
    }

    private void addExtensions(Options options) {
        assert options != null;
        // TODO Auto-generated method stub

    }
    
    private void addLowLevels(Options options) {
        assert options != null;
        Map<String,Class<? extends LowLevel.Builder>> map = 
                options.get(OptionsModelChecker.LOW_LEVEL_ENGINE_CLASS);
        map.put(ExplorerJANI.IDENTIFIER, ExplorerJANI.Builder.class);
        map.put(LowLevelExplicitBuilder.IDENTIFIER, LowLevelExplicitBuilder.class);
        map.put(GraphDDJANI.IDENTIFIER, GraphDDJANI.Builder.class);
    }
}
