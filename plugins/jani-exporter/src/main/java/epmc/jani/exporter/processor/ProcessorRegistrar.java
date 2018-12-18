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
import java.util.Map;

import epmc.jani.exporter.error.ProblemsJANIExporter;
import epmc.jani.model.Action;
import epmc.jani.model.ActionProcessor;
import epmc.jani.model.AssignmentSimple;
import epmc.jani.model.AssignmentSimpleProcessor;
import epmc.jani.model.Assignments;
import epmc.jani.model.AssignmentsProcessor;
import epmc.jani.model.Automata;
import epmc.jani.model.AutomataProcessor;
import epmc.jani.model.Automaton;
import epmc.jani.model.AutomatonProcessor;
import epmc.jani.model.Constant;
import epmc.jani.model.ConstantProcessor;
import epmc.jani.model.Constants;
import epmc.jani.model.ConstantsProcessor;
import epmc.jani.model.Destination;
import epmc.jani.model.DestinationProcessor;
import epmc.jani.model.Destinations;
import epmc.jani.model.DestinationsProcessor;
import epmc.jani.model.Edge;
import epmc.jani.model.EdgeProcessor;
import epmc.jani.model.Edges;
import epmc.jani.model.EdgesProcessor;
import epmc.jani.model.Guard;
import epmc.jani.model.GuardProcessor;
import epmc.jani.model.InitialStates;
import epmc.jani.model.InitialStatesProcessor;
import epmc.jani.model.Location;
import epmc.jani.model.LocationProcessor;
import epmc.jani.model.Locations;
import epmc.jani.model.LocationsProcessor;
import epmc.jani.model.Metadata;
import epmc.jani.model.MetadataProcessor;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.Probability;
import epmc.jani.model.ProbabilityProcessor;
import epmc.jani.model.Rate;
import epmc.jani.model.RateProcessor;
import epmc.jani.model.TimeProgress;
import epmc.jani.model.TimeProgressProcessor;
import epmc.jani.model.Variable;
import epmc.jani.model.VariableProcessor;
import epmc.jani.model.Variables;
import epmc.jani.model.VariablesProcessor;
import epmc.jani.model.component.ComponentSynchronisationVectors;
import epmc.jani.model.component.ComponentSynchronisationVectorsProcessor;
import epmc.jani.model.component.SynchronisationVectorElement;
import epmc.jani.model.component.SynchronisationVectorElementProcessor;
import epmc.jani.model.component.SynchronisationVectorSync;
import epmc.jani.model.component.SynchronisationVectorSyncProcessor;
import epmc.jani.model.type.JANITypeBool;
import epmc.jani.model.type.JANITypeBoolProcessor;
import epmc.jani.model.type.JANITypeBounded;
import epmc.jani.model.type.JANITypeBoundedProcessor;
import epmc.jani.model.type.JANITypeInt;
import epmc.jani.model.type.JANITypeIntProcessor;
import epmc.jani.model.type.JANITypeReal;
import epmc.jani.model.type.JANITypeRealProcessor;
import epmc.time.JANITypeClock;
import epmc.time.JANITypeClockProcessor;
import epmc.util.Util;

/**
 * Class that is responsible for registering the JANI components and their corresponding JANI processors.
 * 
 * @author Andrea Turrini
 *
 */
public class ProcessorRegistrar {
    private static Map<Class<? extends Object>, Class<? extends JANIProcessor>> processors = registerProcessors();
    
    private static ModelJANI model = null;
    
    public static void setModel(ModelJANI model) {
        assert model != null;
        
        ProcessorRegistrar.model = model;
    }
    
    public static boolean isSilentAction(Action action) {
        assert model != null;
        assert action != null;
        
        return model.getSilentAction().equals(action);
    }
    
    /**
     * Add a new processor for a JANI component in the set of known processors.
     * 
     * @param component the JANI component to which associate the processor
     * @param JANIProcessor the corresponding processor
     */
    public static void registerProcessor(Class<? extends Object> component, Class<? extends JANIProcessor> processor) {
        processors.put(component, processor);
    }

    /**
     * Return the processor associated to the given JANI component.
     * 
     * @param component the JANI component for which obtain the processor
     * @return the corresponding processor
     */
    public static JANIProcessor getProcessor(Object component) {
        assert model != null;
        assert component != null;

        JANIProcessor processor = null;
        Class<? extends JANIProcessor> processorClass = processors.get(component.getClass());
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

    private static Map<Class<? extends Object>, Class<? extends JANIProcessor>> registerProcessors() {
        Map<Class<? extends Object>, Class<? extends JANIProcessor>> processors = new HashMap<>();

        //Semantic types
//        processors.put(ModelExtensionCTMC.class, ModelExtensionCTMCProcessor.class);
//        processors.put(ModelExtensionDTMC.class, ModelExtensionDTMCProcessor.class);
//        processors.put(ModelExtensionMDP.class, ModelExtensionMDPProcessor.class);
//        processors.put(ModelExtensionSMG.class, ModelExtensionSMGProcessor.class);
//        processors.put(ModelExtensionLTS.class, ModelExtensionLTSProcessor.class);
//        processors.put(ModelExtensionMA.class, ModelExtensionMAProcessor.class);
//        processors.put(ModelExtensionCTMDP.class, ModelExtensionCTMDPProcessor.class);


        //JANI types
        processors.put(JANITypeBool.class, JANITypeBoolProcessor.class);
        processors.put(JANITypeBounded.class, JANITypeBoundedProcessor.class);
        processors.put(JANITypeInt.class, JANITypeIntProcessor.class);
        processors.put(JANITypeReal.class, JANITypeRealProcessor.class);
        processors.put(JANITypeClock.class, JANITypeClockProcessor.class);

        //JANI metadata
        processors.put(Metadata.class, MetadataProcessor.class);

        //JANI model
//        processors.put(ModelJANI.class, ModelJANIProcessor.class);

        //Constants
        processors.put(Constants.class, ConstantsProcessor.class);
        processors.put(Constant.class, ConstantProcessor.class);

        //Variables
        processors.put(Variables.class, VariablesProcessor.class);
        processors.put(Variable.class, VariableProcessor.class);

        //Initial states
        processors.put(InitialStates.class, InitialStatesProcessor.class);

        //Automata
        processors.put(Automata.class, AutomataProcessor.class);
        processors.put(Automaton.class, AutomatonProcessor.class);

        //Synchronisation
        processors.put(ComponentSynchronisationVectors.class, ComponentSynchronisationVectorsProcessor.class);
        processors.put(SynchronisationVectorElement.class, SynchronisationVectorElementProcessor.class);
        processors.put(SynchronisationVectorSync.class, SynchronisationVectorSyncProcessor.class);

        //Locations
        processors.put(Locations.class, LocationsProcessor.class);
        processors.put(Location.class, LocationProcessor.class);

        //Time progress/invariants
        processors.put(TimeProgress.class, TimeProgressProcessor.class);

        //Edges
        processors.put(Edges.class, EdgesProcessor.class);
        processors.put(Edge.class, EdgeProcessor.class);

        //Actions
        processors.put(Action.class, ActionProcessor.class);

        //Destinations
        processors.put(Destinations.class, DestinationsProcessor.class);
        processors.put(Destination.class, DestinationProcessor.class);

        //Guards
        processors.put(Guard.class, GuardProcessor.class);

        //Assignments
        processors.put(Assignments.class, AssignmentsProcessor.class);
        processors.put(AssignmentSimple.class, AssignmentSimpleProcessor.class);

        //Probability/rate
        processors.put(Probability.class, ProbabilityProcessor.class);
        processors.put(Rate.class, RateProcessor.class);

        //JANI properties
//        processors.put(JANIProperties.class, JANIPropertiesProcessor.class);
//        processors.put(JANIPropertyEntry.class, JANIPropertyEntryProcessor.class);

        //SMG players
//        processors.put(PlayersJANI.class, PlayersJANIProcessor.class);
//        processors.put(PlayerJANI.class, PlayerJANIProcessor.class);

        return processors;
    }
}
