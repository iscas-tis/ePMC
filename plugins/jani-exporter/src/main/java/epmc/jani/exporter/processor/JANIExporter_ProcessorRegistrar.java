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

package epmc.jani.exporter.processor;

import static epmc.error.UtilError.ensure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import epmc.expression.standard.ExpressionFilter;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionSteadyState;
import epmc.expression.standard.ExpressionTemporalFinally;
import epmc.expression.standard.ExpressionTemporalGlobally;
import epmc.expression.standard.ExpressionTemporalNext;
import epmc.expression.standard.ExpressionTemporalRelease;
import epmc.expression.standard.ExpressionTemporalUntil;
import epmc.expression.standard.ExpressionType;
import epmc.expression.standard.ExpressionTypeBoolean;
import epmc.expression.standard.ExpressionTypeInteger;
import epmc.expression.standard.ExpressionTypeReal;
import epmc.expression.standard.FilterType;
import epmc.expression.standard.JANIExporter_ExpressionFilterProcessor;
import epmc.expression.standard.JANIExporter_ExpressionIdentifierStandardProcessor;
import epmc.expression.standard.JANIExporter_ExpressionLiteralProcessor;
import epmc.expression.standard.JANIExporter_ExpressionOperatorProcessor;
import epmc.expression.standard.JANIExporter_ExpressionQuantifierProcessor;
import epmc.expression.standard.JANIExporter_ExpressionSteadyStateProcessor;
import epmc.expression.standard.JANIExporter_ExpressionTemporalFinallyProcessor;
import epmc.expression.standard.JANIExporter_ExpressionTemporalGloballyProcessor;
import epmc.expression.standard.JANIExporter_ExpressionTemporalNextProcessor;
import epmc.expression.standard.JANIExporter_ExpressionTemporalReleaseProcessor;
import epmc.expression.standard.JANIExporter_ExpressionTemporalUntilProcessor;
import epmc.expression.standard.JANIExporter_FilterTypeProcessor;
import epmc.expression.standard.JANIExporter_TimeBoundProcessor;
import epmc.expression.standard.TimeBound;
import epmc.graph.SemanticsContinuousTime;
import epmc.graph.SemanticsDiscreteTime;
import epmc.graph.SemanticsTimed;
import epmc.jani.exporter.error.ProblemsJANIExporter;
import epmc.jani.exporter.options.OptionsJANIExporter;
import epmc.jani.model.Action;
import epmc.jani.model.Actions;
import epmc.jani.model.AssignmentSimple;
import epmc.jani.model.Assignments;
import epmc.jani.model.Automata;
import epmc.jani.model.Automaton;
import epmc.jani.model.Constant;
import epmc.jani.model.Constants;
import epmc.jani.model.Destination;
import epmc.jani.model.Destinations;
import epmc.jani.model.Edge;
import epmc.jani.model.Edges;
import epmc.jani.model.Guard;
import epmc.jani.model.InitialStates;
import epmc.jani.model.JANIExporter_ActionProcessor;
import epmc.jani.model.JANIExporter_ActionsProcessor;
import epmc.jani.model.JANIExporter_AssignmentSimpleProcessor;
import epmc.jani.model.JANIExporter_AssignmentsProcessor;
import epmc.jani.model.JANIExporter_AutomataProcessor;
import epmc.jani.model.JANIExporter_AutomatonProcessor;
import epmc.jani.model.JANIExporter_ConstantProcessor;
import epmc.jani.model.JANIExporter_ConstantsProcessor;
import epmc.jani.model.JANIExporter_DestinationProcessor;
import epmc.jani.model.JANIExporter_DestinationsProcessor;
import epmc.jani.model.JANIExporter_EdgeProcessor;
import epmc.jani.model.JANIExporter_EdgesProcessor;
import epmc.jani.model.JANIExporter_GuardProcessor;
import epmc.jani.model.JANIExporter_InitialStatesProcessor;
import epmc.jani.model.JANIExporter_LocationProcessor;
import epmc.jani.model.JANIExporter_LocationsProcessor;
import epmc.jani.model.JANIExporter_MetadataProcessor;
import epmc.jani.model.JANIExporter_ModelJANIProcessor;
import epmc.jani.model.JANIExporter_ProbabilityProcessor;
import epmc.jani.model.JANIExporter_RateProcessor;
import epmc.jani.model.JANIExporter_TimeProgressProcessor;
import epmc.jani.model.JANIExporter_VariableProcessor;
import epmc.jani.model.JANIExporter_VariablesProcessor;
import epmc.jani.model.Location;
import epmc.jani.model.Locations;
import epmc.jani.model.Metadata;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.Probability;
import epmc.jani.model.Rate;
import epmc.jani.model.TimeProgress;
import epmc.jani.model.Variable;
import epmc.jani.model.Variables;
import epmc.jani.model.component.ComponentSynchronisationVectors;
import epmc.jani.model.component.JANIExporter_ComponentSynchronisationVectorsProcessor;
import epmc.jani.model.component.JANIExporter_SynchronisationVectorElementProcessor;
import epmc.jani.model.component.JANIExporter_SynchronisationVectorSyncProcessor;
import epmc.jani.model.component.SynchronisationVectorElement;
import epmc.jani.model.component.SynchronisationVectorSync;
import epmc.jani.model.property.ExpressionInitial;
import epmc.jani.model.property.JANIExporter_ExpressionInitialProcessor;
import epmc.jani.model.property.JANIExporter_JANIPropertiesProcessor;
import epmc.jani.model.property.JANIExporter_JANIPropertyEntryProcessor;
import epmc.jani.model.property.JANIProperties;
import epmc.jani.model.property.JANIPropertyEntry;
import epmc.jani.model.type.JANIExporter_JANITypeBoolProcessor;
import epmc.jani.model.type.JANIExporter_JANITypeBoundedProcessor;
import epmc.jani.model.type.JANIExporter_JANITypeIntProcessor;
import epmc.jani.model.type.JANIExporter_JANITypeRealProcessor;
import epmc.jani.model.type.JANITypeBool;
import epmc.jani.model.type.JANITypeBounded;
import epmc.jani.model.type.JANITypeInt;
import epmc.jani.model.type.JANITypeReal;
import epmc.options.Options;
import epmc.time.JANIExporter_JANITypeClockProcessor;
import epmc.time.JANITypeClock;
import epmc.util.Util;

/**
 * Class that is responsible for registering the JANI components and their corresponding JANI processors.
 * 
 * @author Andrea Turrini
 *
 */
public class JANIExporter_ProcessorRegistrar {
    private static final Map<Class<? extends Object>, Class<? extends JANIExporter_Processor>> processors = registerProcessors();
    private static final Set<ExpressionType> numericTypes = nativeNumericTypes();
    private static final Set<ExpressionType> booleanTypes = nativeBooleanTypes();
    
    private static ModelJANI model = null;
    private static Boolean useDerivedOperators = null;
    private static Boolean isContinuousTimeModel = null;
    private static Boolean isDiscreteTimeModel = null;
    private static Boolean isTimedModel = null;
    
    public static void setModel(ModelJANI model) {
        assert model != null;
        
        JANIExporter_ProcessorRegistrar.model = model;
        useDerivedOperators = Options.get().getBoolean(OptionsJANIExporter.JANI_EXPORTER_USE_DERIVED_OPERATORS);
        isContinuousTimeModel = SemanticsContinuousTime.isContinuousTime(model.getSemantics());
        isDiscreteTimeModel = SemanticsDiscreteTime.isDiscreteTime(model.getSemantics());
        isTimedModel = SemanticsTimed.isTimed(model.getSemantics());
    }
    
    public static boolean useDerivedOperators() {
        assert model != null;

        return useDerivedOperators;
    }
    
    public static boolean isSilentAction(Action action) {
        assert model != null;
        assert action != null;
        
        return model.getSilentAction().equals(action);
    }
    
    public static boolean isContinuousTimeModel() {
        assert model != null;

        return isContinuousTimeModel;
    }
    
    public static boolean isDiscreteTimeModel() {
        assert model != null;

        return isDiscreteTimeModel;
    }
    
    public static boolean isTimedModel() {
        assert model != null;

        return isTimedModel;
    }
    
    public static boolean isBooleanType(ExpressionType expressionType) {
        assert model != null;
        
        return booleanTypes.contains(expressionType);
    }
    
    public static boolean isNumericType(ExpressionType expressionType) {
        assert model != null;
        
        return numericTypes.contains(expressionType);
    }

    /**
     * Add a new processor for a JANI component in the set of known processors.
     * 
     * @param component the JANI component to which associate the processor
     * @param JANIExporter_Processor the corresponding processor
     */
    public static void registerProcessor(Class<? extends Object> component, Class<? extends JANIExporter_Processor> processor) {
        processors.put(component, processor);
    }

    /**
     * Return the processor associated to the given JANI component.
     * 
     * @param component the JANI component for which obtain the processor
     * @return the corresponding processor
     */
    public static JANIExporter_Processor getProcessor(Object component) {
        assert model != null;
        assert component != null;

        JANIExporter_Processor processor = null;
        Class<? extends JANIExporter_Processor> processorClass = processors.get(component.getClass());
        if (processorClass != null) {
            processor = Util.getInstance(processorClass)
                    .setElement(component);
        } else {
            ensure(false, 
                    ProblemsJANIExporter.JANI_EXPORTER_ERROR_UNKNOWN_PROCESSOR, 
                    component.getClass().getSimpleName());
        }

        return processor;
    }

    private static Map<Class<? extends Object>, Class<? extends JANIExporter_Processor>> registerProcessors() {
        Map<Class<? extends Object>, Class<? extends JANIExporter_Processor>> processors = new HashMap<>();

        //Semantic types
//        processors.put(ModelExtensionCTMC.class, ModelExtensionCTMCProcessor.class);
//        processors.put(ModelExtensionDTMC.class, ModelExtensionDTMCProcessor.class);
//        processors.put(ModelExtensionMDP.class, ModelExtensionMDPProcessor.class);
//        processors.put(ModelExtensionSMG.class, ModelExtensionSMGProcessor.class);
//        processors.put(ModelExtensionLTS.class, ModelExtensionLTSProcessor.class);
//        processors.put(ModelExtensionMA.class, ModelExtensionMAProcessor.class);
//        processors.put(ModelExtensionCTMDP.class, ModelExtensionCTMDPProcessor.class);


        //JANI types
        processors.put(JANITypeBool.class, 
                JANIExporter_JANITypeBoolProcessor.class);
        processors.put(JANITypeBounded.class, 
                JANIExporter_JANITypeBoundedProcessor.class);
        processors.put(JANITypeInt.class, 
                JANIExporter_JANITypeIntProcessor.class);
        processors.put(JANITypeReal.class, 
                JANIExporter_JANITypeRealProcessor.class);
        processors.put(JANITypeClock.class, 
                JANIExporter_JANITypeClockProcessor.class);

        //JANI metadata
        processors.put(Metadata.class, 
                JANIExporter_MetadataProcessor.class);

        //JANI model
        processors.put(ModelJANI.class, 
                JANIExporter_ModelJANIProcessor.class);

        //Constants
        processors.put(Constants.class, 
                JANIExporter_ConstantsProcessor.class);
        processors.put(Constant.class, 
                JANIExporter_ConstantProcessor.class);

        //Variables
        processors.put(Variables.class, 
                JANIExporter_VariablesProcessor.class);
        processors.put(Variable.class, 
                JANIExporter_VariableProcessor.class);

        //Initial states
        processors.put(InitialStates.class, 
                JANIExporter_InitialStatesProcessor.class);

        //Automata
        processors.put(Automata.class, 
                JANIExporter_AutomataProcessor.class);
        processors.put(Automaton.class, 
                JANIExporter_AutomatonProcessor.class);

        //Synchronisation
        processors.put(ComponentSynchronisationVectors.class, 
                JANIExporter_ComponentSynchronisationVectorsProcessor.class);
        processors.put(SynchronisationVectorElement.class, 
                JANIExporter_SynchronisationVectorElementProcessor.class);
        processors.put(SynchronisationVectorSync.class, 
                JANIExporter_SynchronisationVectorSyncProcessor.class);

        //Locations
        processors.put(Locations.class, 
                JANIExporter_LocationsProcessor.class);
        processors.put(Location.class, 
                JANIExporter_LocationProcessor.class);

        //Time progress/invariants
        processors.put(TimeProgress.class, 
                JANIExporter_TimeProgressProcessor.class);

        //Edges
        processors.put(Edges.class, 
                JANIExporter_EdgesProcessor.class);
        processors.put(Edge.class, 
                JANIExporter_EdgeProcessor.class);

        //Actions
        processors.put(Actions.class, 
                JANIExporter_ActionsProcessor.class);
        processors.put(Action.class, 
                JANIExporter_ActionProcessor.class);

        //Destinations
        processors.put(Destinations.class, 
                JANIExporter_DestinationsProcessor.class);
        processors.put(Destination.class, 
                JANIExporter_DestinationProcessor.class);

        //Guards
        processors.put(Guard.class, 
                JANIExporter_GuardProcessor.class);

        //Assignments
        processors.put(Assignments.class, 
                JANIExporter_AssignmentsProcessor.class);
        processors.put(AssignmentSimple.class, 
                JANIExporter_AssignmentSimpleProcessor.class);

        //Probability/rate
        processors.put(Probability.class, 
                JANIExporter_ProbabilityProcessor.class);
        processors.put(Rate.class, 
                JANIExporter_RateProcessor.class);

        //JANI properties
        processors.put(ExpressionInitial.class,
                JANIExporter_ExpressionInitialProcessor.class);
        processors.put(JANIProperties.class, 
                JANIExporter_JANIPropertiesProcessor.class);
        processors.put(JANIPropertyEntry.class, 
                JANIExporter_JANIPropertyEntryProcessor.class);

        //SMG players
//        processors.put(PlayersJANI.class, JANIExporter_PlayersJANIProcessor.class);
//        processors.put(PlayerJANI.class, JANIExporter_PlayerJANIProcessor.class);

        // expressions
        processors.put(ExpressionFilter.class, 
                JANIExporter_ExpressionFilterProcessor.class);
        processors.put(ExpressionIdentifierStandard.class, 
                JANIExporter_ExpressionIdentifierStandardProcessor.class);
        processors.put(ExpressionLiteral.class, 
                JANIExporter_ExpressionLiteralProcessor.class);
        processors.put(ExpressionOperator.class, 
                JANIExporter_ExpressionOperatorProcessor.class);
        processors.put(ExpressionQuantifier.class, 
                JANIExporter_ExpressionQuantifierProcessor.class);
        processors.put(ExpressionSteadyState.class, 
                JANIExporter_ExpressionSteadyStateProcessor.class);
        processors.put(ExpressionTemporalFinally.class, 
                JANIExporter_ExpressionTemporalFinallyProcessor.class);
        processors.put(ExpressionTemporalGlobally.class, 
                JANIExporter_ExpressionTemporalGloballyProcessor.class);
        processors.put(ExpressionTemporalNext.class, 
                JANIExporter_ExpressionTemporalNextProcessor.class);
        processors.put(ExpressionTemporalRelease.class, 
                JANIExporter_ExpressionTemporalReleaseProcessor.class);
        processors.put(ExpressionTemporalUntil.class, 
                JANIExporter_ExpressionTemporalUntilProcessor.class);
        processors.put(FilterType.class, 
                JANIExporter_FilterTypeProcessor.class);
        processors.put(TimeBound.class, 
                JANIExporter_TimeBoundProcessor.class);
        
        return processors;
    }
    
    private static Set<ExpressionType> nativeNumericTypes() {
        Set<ExpressionType> set = new HashSet<>();

        set.add(ExpressionTypeReal.TYPE_REAL);
        set.add(ExpressionTypeInteger.TYPE_INTEGER);
        
        return set;
    }
    
    private static Set<ExpressionType> nativeBooleanTypes() {
        Set<ExpressionType> set = new HashSet<>();

        set.add(ExpressionTypeBoolean.TYPE_BOOLEAN);
        
        return set;
    }
}
