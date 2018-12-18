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

package epmc.jani.exporter.expressionprocessor;

import static epmc.error.UtilError.ensure;

import java.util.HashMap;
import java.util.Map;

import epmc.expression.Expression;
import epmc.jani.exporter.error.ProblemsJANIExporter;
import epmc.jani.exporter.expressionprocessor.ExpressionProcessor;
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
import epmc.jani.model.ModelJANIProcessor;
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
import epmc.jani.model.property.JANIProperties;
import epmc.jani.model.property.JANIPropertiesProcessor;
import epmc.jani.model.property.JANIPropertyEntry;
import epmc.jani.model.property.JANIPropertyEntryProcessor;
import epmc.jani.model.type.JANITypeBool;
import epmc.jani.model.type.JANITypeBoolProcessor;
import epmc.jani.model.type.JANITypeBounded;
import epmc.jani.model.type.JANITypeBoundedProcessor;
import epmc.jani.model.type.JANITypeInt;
import epmc.jani.model.type.JANITypeIntProcessor;
import epmc.jani.model.type.JANITypeReal;
import epmc.jani.model.type.JANITypeRealProcessor;
import epmc.jani.type.ctmc.ModelExtensionCTMC;
import epmc.jani.type.ctmc.ModelExtensionCTMCProcessor;
import epmc.jani.type.ctmdp.ModelExtensionCTMDP;
import epmc.jani.type.ctmdp.ModelExtensionCTMDPProcessor;
import epmc.jani.type.dtmc.ModelExtensionDTMC;
import epmc.jani.type.dtmc.ModelExtensionDTMCProcessor;
import epmc.jani.type.lts.ModelExtensionLTS;
import epmc.jani.type.lts.ModelExtensionLTSProcessor;
import epmc.jani.type.ma.ModelExtensionMA;
import epmc.jani.type.ma.ModelExtensionMAProcessor;
import epmc.jani.type.mdp.ModelExtensionMDP;
import epmc.jani.type.mdp.ModelExtensionMDPProcessor;
import epmc.jani.type.smg.ModelExtensionSMG;
import epmc.jani.type.smg.ModelExtensionSMGProcessor;
import epmc.jani.type.smg.PlayerJANI;
import epmc.jani.type.smg.PlayerJANIProcessor;
import epmc.jani.type.smg.PlayersJANI;
import epmc.jani.type.smg.PlayersJANIProcessor;
import epmc.time.JANITypeClock;
import epmc.time.JANITypeClockProcessor;
import epmc.util.Util;

/**
 * Class that is responsible for registering the JANI components and their corresponding JANI processors.
 * 
 * @author Andrea Turrini
 *
 */
public class ExpressionProcessorRegistrar {
    private static Map<Class<? extends Expression>, Class<? extends ExpressionProcessor>> expressionProcessors = registerExpressionProcessors();
    
    private static ModelJANI model = null;
    
    public void setModel(ModelJANI model) {
        assert model != null;
        
        ExpressionProcessorRegistrar.model = model;
    }
    
    public ModelJANI getModel() {
        assert model != null;
        
        return model;
    }

    /**
     * Add a new processor for an expression in the set of known processors.
     * 
     * @param expression the expression to which associate the processor
     * @param processor the corresponding processor
     */
    public static void registerExpressionProcessor(Class<? extends Expression> expression, Class<? extends ExpressionProcessor> processor) {
        expressionProcessors.put(expression, processor);
    }

    /**
     * Return the processor associated to the given expression.
     * 
     * @param expression the expression for which obtain the processor
     * @return the corresponding processor
     */
    public static ExpressionProcessor getExpressionProcessor(Expression expression) {
        assert model != null;
        assert expression != null;

        ExpressionProcessor processor = null;
        Class<? extends ExpressionProcessor> processorClass = expressionProcessors.get(expression.getClass());
        if (processorClass != null) {
            processor = Util.getInstance(processorClass)
                    .setElement(model, expression);
        } else {
            ensure(false, 
                    ProblemsJANIExporter.JANI_EXPORTER_ERROR_UNKNOWN_PROCESSOR, 
                    expression.getClass().getSimpleName());
        }

        return processor;
    }

    private static Map<Class<? extends Expression>, Class<? extends ExpressionProcessor>> registerExpressionProcessors() {
        Map<Class<? extends Expression>, Class<? extends ExpressionProcessor>> processors = new HashMap<>();
        
        return processors;
    }
}
