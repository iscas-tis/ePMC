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

package epmc.prism.exporter.processor;

import static epmc.error.UtilError.ensure;

import java.util.HashMap;
import java.util.Map;

import epmc.expression.standard.ExpressionCoalition;
import epmc.expression.standard.PRISMExporter_ExpressionCoalitionProcessor;
import epmc.expression.standard.ExpressionFilter;
import epmc.expression.standard.PRISMExporter_ExpressionFilterProcessor;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expression.standard.PRISMExporter_ExpressionIdentifierStandardProcessor;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.PRISMExporter_ExpressionLiteralProcessor;
import epmc.expression.standard.ExpressionMultiObjective;
import epmc.expression.standard.PRISMExporter_ExpressionMultiObjectiveProcessor;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.PRISMExporter_ExpressionOperatorProcessor;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.PRISMExporter_ExpressionQuantifierProcessor;
import epmc.expression.standard.ExpressionReward;
import epmc.expression.standard.PRISMExporter_ExpressionRewardProcessor;
import epmc.expression.standard.ExpressionSteadyState;
import epmc.expression.standard.PRISMExporter_ExpressionSteadyStateProcessor;
import epmc.expression.standard.ExpressionTemporalFinally;
import epmc.expression.standard.PRISMExporter_ExpressionTemporalFinallyProcessor;
import epmc.expression.standard.ExpressionTemporalGlobally;
import epmc.expression.standard.PRISMExporter_ExpressionTemporalGloballyProcessor;
import epmc.expression.standard.ExpressionTemporalNext;
import epmc.expression.standard.PRISMExporter_ExpressionTemporalNextProcessor;
import epmc.expression.standard.ExpressionTemporalRelease;
import epmc.expression.standard.PRISMExporter_ExpressionTemporalReleaseProcessor;
import epmc.expression.standard.ExpressionTemporalUntil;
import epmc.expression.standard.PRISMExporter_ExpressionTemporalUntilProcessor;
import epmc.expression.standard.TimeBound;
import epmc.expression.standard.PRISMExporter_TimeBoundProcessor;
import epmc.jani.model.Action;
import epmc.jani.model.PRISMExporter_ActionProcessor;
import epmc.jani.model.AssignmentSimple;
import epmc.jani.model.PRISMExporter_AssignmentSimpleProcessor;
import epmc.jani.model.Assignments;
import epmc.jani.model.PRISMExporter_AssignmentsProcessor;
import epmc.jani.model.Automata;
import epmc.jani.model.PRISMExporter_AutomataProcessor;
import epmc.jani.model.Automaton;
import epmc.jani.model.PRISMExporter_AutomatonProcessor;
import epmc.jani.model.Constant;
import epmc.jani.model.PRISMExporter_ConstantProcessor;
import epmc.jani.model.Constants;
import epmc.jani.model.PRISMExporter_ConstantsProcessor;
import epmc.jani.model.Destination;
import epmc.jani.model.PRISMExporter_DestinationProcessor;
import epmc.jani.model.Destinations;
import epmc.jani.model.PRISMExporter_DestinationsProcessor;
import epmc.jani.model.Edge;
import epmc.jani.model.PRISMExporter_EdgeProcessor;
import epmc.jani.model.Edges;
import epmc.jani.model.PRISMExporter_EdgesProcessor;
import epmc.jani.model.Guard;
import epmc.jani.model.PRISMExporter_GuardProcessor;
import epmc.jani.model.InitialStates;
import epmc.jani.model.PRISMExporter_InitialStatesProcessor;
import epmc.jani.model.Location;
import epmc.jani.model.PRISMExporter_LocationProcessor;
import epmc.jani.model.Locations;
import epmc.jani.model.PRISMExporter_LocationsProcessor;
import epmc.jani.model.Metadata;
import epmc.jani.model.PRISMExporter_MetadataProcessor;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.PRISMExporter_ModelJANIProcessor;
import epmc.jani.model.Probability;
import epmc.jani.model.PRISMExporter_ProbabilityProcessor;
import epmc.jani.model.Rate;
import epmc.jani.model.PRISMExporter_RateProcessor;
import epmc.jani.model.TimeProgress;
import epmc.jani.model.PRISMExporter_TimeProgressProcessor;
import epmc.jani.model.Variable;
import epmc.jani.model.PRISMExporter_VariableProcessor;
import epmc.jani.model.Variables;
import epmc.jani.model.PRISMExporter_VariablesProcessor;
import epmc.jani.model.component.ComponentSynchronisationVectors;
import epmc.jani.model.component.PRISMExporter_ComponentSynchronisationVectorsProcessor;
import epmc.jani.model.property.ExpressionInitial;
import epmc.jani.model.property.PRISMExporter_ExpressionInitialProcessor;
import epmc.jani.model.property.JANIProperties;
import epmc.jani.model.property.PRISMExporter_JANIPropertiesProcessor;
import epmc.jani.model.property.JANIPropertyEntry;
import epmc.jani.model.property.PRISMExporter_JANIPropertyEntryProcessor;
import epmc.jani.model.type.JANITypeBool;
import epmc.jani.model.type.PRISMExporter_JANITypeBoolProcessor;
import epmc.jani.model.type.JANITypeBounded;
import epmc.jani.model.type.PRISMExporter_JANITypeBoundedProcessor;
import epmc.jani.model.type.JANITypeInt;
import epmc.jani.model.type.PRISMExporter_JANITypeIntProcessor;
import epmc.jani.model.type.JANITypeReal;
import epmc.jani.model.type.PRISMExporter_JANITypeRealProcessor;
import epmc.jani.type.ctmc.ModelExtensionCTMC;
import epmc.jani.type.ctmc.PRISMExporter_ModelExtensionCTMCProcessor;
import epmc.jani.type.ctmdp.ModelExtensionCTMDP;
import epmc.jani.type.ctmdp.PRISMExporter_ModelExtensionCTMDPProcessor;
import epmc.jani.type.dtmc.ModelExtensionDTMC;
import epmc.jani.type.dtmc.PRISMExporter_ModelExtensionDTMCProcessor;
import epmc.jani.type.lts.ModelExtensionLTS;
import epmc.jani.type.lts.PRISMExporter_ModelExtensionLTSProcessor;
import epmc.jani.type.ma.ModelExtensionMA;
import epmc.jani.type.ma.PRISMExporter_ModelExtensionMAProcessor;
import epmc.jani.type.mdp.ModelExtensionMDP;
import epmc.jani.type.mdp.PRISMExporter_ModelExtensionMDPProcessor;
import epmc.jani.type.smg.ModelExtensionSMG;
import epmc.jani.type.smg.PRISMExporter_ModelExtensionSMGProcessor;
import epmc.jani.type.smg.PlayerJANI;
import epmc.jani.type.smg.PRISMExporter_PlayerJANIProcessor;
import epmc.jani.type.smg.PlayersJANI;
import epmc.jani.type.smg.PRISMExporter_PlayersJANIProcessor;
import epmc.prism.exporter.error.ProblemsPRISMExporter;
import epmc.time.JANITypeClock;
import epmc.time.PRISMExporter_JANITypeClockProcessor;
import epmc.util.Util;

/**
 * Class that is responsible for registering the JANI components and their corresponding JANI2PRISM processors.
 * 
 * @author Andrea Turrini
 *
 */
public class PRISMExporter_ProcessorRegistrar {
    private static Map<Class<? extends Object>, Class<? extends PRISMExporter_ProcessorStrict>> strictProcessors = registerStrictProcessors();
    private static Map<Class<? extends Object>, Class<? extends PRISMExporter_ProcessorExtended>> extendedProcessors = registerExtendedProcessors();
    private static Map<Class<? extends Object>, Class<? extends PRISMExporter_ProcessorNonPRISM>> nonPRISMProcessors = registerNonPRISMProcessors();

    private static boolean allowMultipleLocations = false;
    private static boolean useExtendedSyntax = false;
    private static boolean useNonPRISMSyntax = false;

    /**
     * Add a new processor for a JANI component in the set of known strict processors.
     * 
     * @param JANIComponent the JANI component to which associate the processor
     * @param JANI2PRISMProcessor the corresponding processor
     */
    public static void registerStrictProcessor(Class<? extends Object> JANIComponent, Class<? extends PRISMExporter_ProcessorStrict> JANI2PRISMProcessor) {
        assert !PRISMExporter_ProcessorExtended.class.isAssignableFrom(JANI2PRISMProcessor);

        strictProcessors.put(JANIComponent, JANI2PRISMProcessor);
    }

    /**
     * Add a new processor for a JANI component in the set of known extended processors.
     * 
     * @param JANIComponent the JANI component to which associate the processor
     * @param JANI2PRISMProcessor the corresponding processor
     */
    public static void registerExtendedProcessor(Class<? extends Object> JANIComponent, Class<? extends PRISMExporter_ProcessorExtended> JANI2PRISMProcessor) {
        extendedProcessors.put(JANIComponent, JANI2PRISMProcessor);
    }

    /**
     * Add a new processor for a JANI component in the set of known non-PRISM processors.
     * 
     * @param JANIComponent the JANI component to which associate the processor
     * @param JANI2PRISMProcessor the corresponding processor
     */
    public static void registerNonPRISMProcessor(Class<? extends Object> JANIComponent, Class<? extends PRISMExporter_ProcessorNonPRISM> JANI2PRISMProcessor) {
        nonPRISMProcessors.put(JANIComponent, JANI2PRISMProcessor);
    }

    /**
     * Return the processor associated to the given JANI component.
     * 
     * @param JANIComponent the JANI component for which obtain the processor
     * @return the corresponding processor
     */
    public static PRISMExporter_ProcessorStrict getProcessor(Object JANIComponent) {
        assert JANIComponent != null;

        PRISMExporter_ProcessorStrict processor = null;
        Class<? extends PRISMExporter_ProcessorStrict> processorClass = strictProcessors.get(JANIComponent.getClass());
        if (processorClass != null) {
            processor = Util.getInstance(processorClass)
                    .setElement(JANIComponent);
        } else {
            Class<? extends PRISMExporter_ProcessorExtended> extendedProcessorClass = extendedProcessors.get(JANIComponent.getClass());
            if (extendedProcessorClass != null) {
                processor = Util.getInstance(extendedProcessorClass)
                        .setElement(JANIComponent);
                ensure(useExtendedSyntax, 
                        ProblemsPRISMExporter.PRISM_EXPORTER_ERROR_EXTENDED_SYNTAX_REQUIRED, 
                        ((PRISMExporter_ProcessorExtended)processor).getUnsupportedFeature()
                            .toArray());
            } else {
                Class<? extends PRISMExporter_ProcessorNonPRISM> nonPRISMProcessorClass = nonPRISMProcessors.get(JANIComponent.getClass());
                if (nonPRISMProcessorClass != null) {
                    processor = Util.getInstance(nonPRISMProcessorClass)
                            .setElement(JANIComponent);
                    ensure(useNonPRISMSyntax, 
                            ProblemsPRISMExporter.PRISM_EXPORTER_ERROR_EXTENDED_SYNTAX_REQUIRED, 
                            ((PRISMExporter_ProcessorNonPRISM)processor).getUnsupportedFeature()
                                .toArray());
                } else {
                    ensure(false, 
                            ProblemsPRISMExporter.PRISM_EXPORTER_ERROR_UNKNOWN_PROCESSOR, 
                            JANIComponent.getClass().getSimpleName());
                }
            }
        }

        return processor;
    }

    /**
     * Allow to use the extended PRISM syntax
     */
    public static void useExtendedPRISMSyntax() {
        useExtendedSyntax = true;
    }

    public static boolean getUseExtendedPRISMSyntax() {
        return useExtendedSyntax;
    }

    /**
     * Allow to use the non PRISM syntax
     */
    public static void useNonPRISMSyntax() {
        useNonPRISMSyntax = true;
    }

    public static boolean getUseNonPRISMSyntax() {
        return useNonPRISMSyntax;
    }

    public static void setAllowMultipleLocations(boolean allowMultipleLocations) {
        PRISMExporter_ProcessorRegistrar.allowMultipleLocations = allowMultipleLocations;
    }

    public static boolean getAllowMultipleLocations() {
        return allowMultipleLocations;
    }

    private static Map<Class<? extends Object>, Class<? extends PRISMExporter_ProcessorStrict>> registerStrictProcessors() {
        Map<Class<? extends Object>, Class<? extends PRISMExporter_ProcessorStrict>> processors = new HashMap<>();

        //Semantic types
        processors.put(ModelExtensionCTMC.class, PRISMExporter_ModelExtensionCTMCProcessor.class);
        processors.put(ModelExtensionDTMC.class, PRISMExporter_ModelExtensionDTMCProcessor.class);
        processors.put(ModelExtensionMDP.class, PRISMExporter_ModelExtensionMDPProcessor.class);

        //JANI types
        processors.put(JANITypeBool.class, PRISMExporter_JANITypeBoolProcessor.class);
        processors.put(JANITypeBounded.class, PRISMExporter_JANITypeBoundedProcessor.class);
        processors.put(JANITypeInt.class, PRISMExporter_JANITypeIntProcessor.class);
        processors.put(JANITypeReal.class, PRISMExporter_JANITypeRealProcessor.class);
        processors.put(JANITypeClock.class, PRISMExporter_JANITypeClockProcessor.class);

        //JANI metadata
        processors.put(Metadata.class, PRISMExporter_MetadataProcessor.class);

        //JANI model
        processors.put(ModelJANI.class, PRISMExporter_ModelJANIProcessor.class);

        //Constants
        processors.put(Constants.class, PRISMExporter_ConstantsProcessor.class);
        processors.put(Constant.class, PRISMExporter_ConstantProcessor.class);

        //Variables
        processors.put(Variables.class, PRISMExporter_VariablesProcessor.class);
        processors.put(Variable.class, PRISMExporter_VariableProcessor.class);

        //Initial states
        processors.put(InitialStates.class, PRISMExporter_InitialStatesProcessor.class);

        //Automata
        processors.put(Automata.class, PRISMExporter_AutomataProcessor.class);
        processors.put(Automaton.class, PRISMExporter_AutomatonProcessor.class);

        //Synchronisation vectors
        processors.put(ComponentSynchronisationVectors.class, PRISMExporter_ComponentSynchronisationVectorsProcessor.class);

        //Locations
        processors.put(Locations.class, PRISMExporter_LocationsProcessor.class);
        processors.put(Location.class, PRISMExporter_LocationProcessor.class);

        //Time progress/invariants
        processors.put(TimeProgress.class, PRISMExporter_TimeProgressProcessor.class);

        //Time bounds
        processors.put(TimeBound.class, PRISMExporter_TimeBoundProcessor.class);

        //Edges
        processors.put(Edges.class, PRISMExporter_EdgesProcessor.class);
        processors.put(Edge.class, PRISMExporter_EdgeProcessor.class);

        //Actions
        processors.put(Action.class, PRISMExporter_ActionProcessor.class);

        //Destinations
        processors.put(Destinations.class, PRISMExporter_DestinationsProcessor.class);
        processors.put(Destination.class, PRISMExporter_DestinationProcessor.class);

        //Guards
        processors.put(Guard.class, PRISMExporter_GuardProcessor.class);

        //Assignments
        processors.put(Assignments.class, PRISMExporter_AssignmentsProcessor.class);
        processors.put(AssignmentSimple.class, PRISMExporter_AssignmentSimpleProcessor.class);

        //Expressions
        processors.put(ExpressionOperator.class, PRISMExporter_ExpressionOperatorProcessor.class);
        processors.put(ExpressionIdentifierStandard.class, PRISMExporter_ExpressionIdentifierStandardProcessor.class);
        processors.put(ExpressionLiteral.class, PRISMExporter_ExpressionLiteralProcessor.class);
        processors.put(ExpressionFilter.class, PRISMExporter_ExpressionFilterProcessor.class);
        processors.put(ExpressionQuantifier.class, PRISMExporter_ExpressionQuantifierProcessor.class);
        processors.put(ExpressionTemporalUntil.class, PRISMExporter_ExpressionTemporalUntilProcessor.class);
        processors.put(ExpressionTemporalNext.class, PRISMExporter_ExpressionTemporalNextProcessor.class);
        processors.put(ExpressionTemporalFinally.class, PRISMExporter_ExpressionTemporalFinallyProcessor.class);
        processors.put(ExpressionTemporalGlobally.class, PRISMExporter_ExpressionTemporalGloballyProcessor.class);
        processors.put(ExpressionTemporalRelease.class, PRISMExporter_ExpressionTemporalReleaseProcessor.class);
        processors.put(ExpressionReward.class, PRISMExporter_ExpressionRewardProcessor.class);
        processors.put(ExpressionSteadyState.class, PRISMExporter_ExpressionSteadyStateProcessor.class);
        processors.put(ExpressionInitial.class, PRISMExporter_ExpressionInitialProcessor.class);
        processors.put(ExpressionMultiObjective.class, PRISMExporter_ExpressionMultiObjectiveProcessor.class);

        //Probability/rate
        processors.put(Probability.class, PRISMExporter_ProbabilityProcessor.class);
        processors.put(Rate.class, PRISMExporter_RateProcessor.class);

        //JANI properties
        processors.put(JANIProperties.class, PRISMExporter_JANIPropertiesProcessor.class);
        processors.put(JANIPropertyEntry.class, PRISMExporter_JANIPropertyEntryProcessor.class);

        return processors;
    }

    private static Map<Class<? extends Object>, Class<? extends PRISMExporter_ProcessorExtended>> registerExtendedProcessors() {
        Map<Class<? extends Object>, Class<? extends PRISMExporter_ProcessorExtended>> processors = new HashMap<>();

        //Semantic types
        processors.put(ModelExtensionSMG.class, PRISMExporter_ModelExtensionSMGProcessor.class);

        //Expressions
        processors.put(ExpressionCoalition.class, PRISMExporter_ExpressionCoalitionProcessor.class);

        //SMG players
        processors.put(PlayersJANI.class, PRISMExporter_PlayersJANIProcessor.class);
        processors.put(PlayerJANI.class, PRISMExporter_PlayerJANIProcessor.class);

        return processors;
    }

    private static Map<Class<? extends Object>, Class<? extends PRISMExporter_ProcessorNonPRISM>> registerNonPRISMProcessors() {
        Map<Class<? extends Object>, Class<? extends PRISMExporter_ProcessorNonPRISM>> processors = new HashMap<>();

        //Semantic types
        processors.put(ModelExtensionLTS.class, PRISMExporter_ModelExtensionLTSProcessor.class);
        processors.put(ModelExtensionMA.class, PRISMExporter_ModelExtensionMAProcessor.class);
        processors.put(ModelExtensionCTMDP.class, PRISMExporter_ModelExtensionCTMDPProcessor.class);

        return processors;
    }
}
