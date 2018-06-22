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

package epmc.specialisesmg;

import epmc.algorithms.OptionsAlgorithm;
import epmc.dd.OptionsDD;
import epmc.dd.cudd.OptionsDDCUDD;
import epmc.dd.cuddmtbdd.OptionsDDCUDDMTBDD;
import epmc.expression.standard.OptionsExpressionBasic;
import epmc.graph.options.OptionsGraph;
import epmc.graphsolver.OptionsGraphsolver;
import epmc.jani.explorer.OptionsJANIExplorer;
import epmc.jani.model.OptionsJANIModel;
import epmc.jani.type.ctmc.OptionsJANICTMC;
import epmc.jani.type.dtmc.OptionsJANIDTMC;
import epmc.main.options.OptionsEPMC;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;
import epmc.plugin.OptionsPlugin;
import epmc.prism.model.convert.OptionsPRISMConverter;
import epmc.value.OptionsValue;

public final class AfterOptionsCreationSpecialiseSMG implements AfterOptionsCreation {
	public final static String IDENTIFIER = "after-options-creation-specialise-smg";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) {
		assert options != null;
		options.disableOption(OptionsModelChecker.MODEL_INPUT_TYPE);
		options.disableOption(OptionsModelChecker.PROPERTY_INPUT_TYPE);
		/* currently buggy */
//		options.disableOption("iteration-method");
	//	options.disableOption("iteration-stop-criterion");
		//options.disableOption("iteration-tolerance");
		options.disableOption(OptionsModelChecker.ENGINE);
		options.disableOption(OptionsDD.DD_AND_EXIST);
		options.disableOption(OptionsDD.DD_BINARY_ENGINE);
		options.disableOption(OptionsDD.DD_DEBUG);
		options.disableOption(OptionsDD.DD_LEAK_CHECK);
		options.disableOption(OptionsDD.DD_LIBRARY_CLASS);
		options.disableOption(OptionsDD.DD_MT_LIBRARY_CLASS);
		options.disableOption(OptionsDD.DD_MULTI_ENGINE);
		options.disableOption(OptionsDD.DD_CATEGORY);
		options.disableOption(OptionsDDCUDD.DD_CUDD_GARBAGE_COLLECT);
		options.disableOption(OptionsDDCUDD.DD_CUDD_INIT_CACHE_SIZE);
		options.disableOption(OptionsDDCUDD.DD_CUDD_LOOSE_UP_TO);
		options.disableOption(OptionsDDCUDD.DD_CUDD_MAX_CACHE_HARD);
		options.disableOption(OptionsDDCUDD.DD_CUDD_MAX_MEMORY);
		options.disableOption(OptionsDDCUDD.DD_CUDD_MIN_HIT);
		options.disableOption(OptionsDDCUDD.DD_CUDD_REORDER_HEURISTIC);
		options.disableOption(OptionsDDCUDD.DD_CUDD_SUBENGINE);
		options.disableOption(OptionsDDCUDD.DD_CUDD_UNIQUE_SLOTS);
		options.disableOption(OptionsDDCUDD.DD_CUDD_CATEGORY);
		options.disableOption(OptionsDDCUDD.OPTIONS_DD_CUDD);
		options.disableOption(OptionsDDCUDDMTBDD.DD_CUDD_MTBDD_GARBAGE_COLLECT);
		options.disableOption(OptionsDDCUDDMTBDD.DD_CUDD_MTBDD_INIT_CACHE_SIZE);
		options.disableOption(OptionsDDCUDDMTBDD.DD_CUDD_MTBDD_LOOSE_UP_TO);
		options.disableOption(OptionsDDCUDDMTBDD.DD_CUDD_MTBDD_MAX_CACHE_HARD);
		options.disableOption(OptionsDDCUDDMTBDD.DD_CUDD_MTBDD_MAX_MEMORY);
		options.disableOption(OptionsDDCUDDMTBDD.DD_CUDD_MTBDD_MIN_HIT);
		options.disableOption(OptionsDDCUDDMTBDD.DD_CUDD_MTBDD_UNIQUE_SLOTS);
		options.disableOption(OptionsDDCUDDMTBDD.DD_CUDD_MTBDD_CATEGORY);
		options.disableOption(OptionsDDCUDDMTBDD.OPTIONS_DD_CUDD_MTBDD);
		options.disableOption(OptionsGraphsolver.GRAPHSOLVER_CATEGORY);
		options.disableOption(OptionsGraphsolver.GRAPHSOLVER_DD_LUMPER_CLASS);
		options.disableOption(OptionsGraphsolver.GRAPHSOLVER_LUMP_BEFORE_GRAPH_SOLVING);
		options.disableOption(OptionsGraphsolver.GRAPHSOLVER_LUMPER_DD);
		options.disableOption(OptionsGraphsolver.GRAPHSOLVER_LUMPER_EXPLICIT);
		options.disableOption(OptionsGraphsolver.GRAPHSOLVER_SOLVER);
		options.disableOption(OptionsGraphsolver.GRAPHSOLVER_PREPROCESSOR_EXPLICIT);
		options.disableOption(OptionsGraphsolver.GRAPHSOLVER_PREPROCESSOR_EXPLICIT_CLASS);
		options.disableOption(OptionsGraphsolver.GRAPHSOLVER_SOLVER_CLASS);
		options.disableOption(OptionsJANIExplorer.OPTIONS_JANI_EXPLORER);
		options.disableOption(OptionsJANIExplorer.JANI_EXPLORER_ASSIGNMENT_EVALUATOR_CLASS);
		options.disableOption(OptionsJANIExplorer.JANI_EXPLORER_EXTENSION_CLASS);
		options.disableOption(OptionsJANIExplorer.JANI_EXPLORER_INITIAL_ENUMERATOR);
		options.disableOption(OptionsJANIModel.JANI_ACTION_BITS);
		options.disableOption(OptionsJANIModel.JANI_FIX_DEADLOCKS);
		options.disableOption(OptionsJANIModel.JANI_MODEL_CATEGORY);
		options.disableOption(OptionsJANIModel.JANI_MODEL_EXTENSION_CLASS);
		options.disableOption(OptionsJANIModel.JANI_MODEL_EXTENSION_SEMANTICS);
		options.disableOption(OptionsJANICTMC.OPTIONS_JANI_CTMC);
		options.disableOption(OptionsJANICTMC.JANI_CTMC_ALLOW_MULTI_TRANSITION);
		options.disableOption(OptionsJANIDTMC.OPTIONS_JANI_DTMC);
        // options.disableOption(OptionsJANIDTMC.JANI_DTMC_ALLOW_MULTI_TRANSITION);
		options.disableOption(OptionsPRISMConverter.PRISM_CONVERTER_OPTIONS);
		options.disableOption(OptionsPRISMConverter.PRISM_CONVERTER_REWARD_METHOD);
		options.disableOption(OptionsPRISMConverter.PRISM_CONVERTER_SYSTEM_METHOD);
		options.disableOption(OptionsAlgorithm.DD_SCC_ALGORITHM);
		options.disableOption(OptionsExpressionBasic.DD_EXPRESSION_CACHE);
		options.disableOption(OptionsExpressionBasic.DD_EXPRESSION_VECTOR);
		options.disableOption(OptionsModelChecker.PROPERTY_SOLVER);
		options.disableOption(OptionsPlugin.PLUGIN);
		options.disableOption(OptionsMessages.TIME_STAMPS);
		options.disableOption(OptionsGraph.STATE_STORAGE);
		options.disableOption(OptionsGraph.WRAPPER_GRAPH_SUCCESSORS_SIZE);
		options.disableOption("prism-flatten");
		options.disableOption(OptionsValue.VALUE_FLOATING_POINT_OUTPUT_FORMAT);
		options.setToolName("EPMC SMG");
		options.setToolDescription("Distribution of EPMC specialised to solving stochastic parity games.");
	}

}
