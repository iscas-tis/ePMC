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

package epmc.jani.model;

import static epmc.error.UtilError.ensure;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionLiteral;
import epmc.graph.SemanticsDTMC;
import epmc.prism.exporter.error.ProblemsPRISMExporter;
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;
import epmc.prism.exporter.processor.JANIComponentRegistrar;
import epmc.prism.exporter.processor.ProcessorRegistrar;

public class EdgeProcessor implements JANI2PRISMProcessorStrict {

	private Edge edge = null;
	private String prefix = null;
	private Automaton automaton = null;
	
	@Override
	public void setElement(Object obj) throws EPMCException {
		assert obj != null;
		assert obj instanceof Edge; 
		
		edge = (Edge) obj;
		
		Action action = edge.getActionOrSilent();
		for (Destination destination: edge.getDestinations()) {
			for (AssignmentSimple assignment : destination.getAssignmentsOrEmpty()) {
				Variable reward = assignment.getRef();
				if (reward.isTransient()) {
					Expression expression = assignment.getValue();
					JANIComponentRegistrar.registerTransitionRewardExpression(reward, action, expression);
				}
			}
		}
	}

	@Override
	public void setAutomaton(Automaton automaton) {
		this.automaton = automaton;
	}
	
	@Override
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public StringBuilder toPRISM() throws EPMCException {
		assert edge != null;
		
		Rate edgeRate = edge.getRate();
		
		if (edgeRate != null && SemanticsDTMC.isDTMC(edge.getModel().getSemantics())) {
			Rate rateOne = new Rate();
			rateOne.setExp(ExpressionLiteral.getOne(edge.getModel().getContextValue()));
			ensure(rateOne.equals(edgeRate), ProblemsPRISMExporter.PRISM_EXPORTER_UNSUPPORTED_FEATURE_EDGE_RATE_NOT_ONE);
		}
		
		StringBuilder prism = new StringBuilder();
		JANI2PRISMProcessorStrict processor; 

		String comment = edge.getComment();
		if (comment != null) {
			if (prefix != null) {
				prism.append(prefix);
			}
			prism.append("// ").append(comment).append("\n");
		}
		
		Action action = edge.getActionOrSilent();
		processor = ProcessorRegistrar.getProcessor(action);
		if (prefix != null)	{
			prism.append(prefix);
		}
		prism.append(processor.toPRISM().toString()).append(" ");
		
		if (automaton.getLocations().size() > 1) {
			prism.append("(")
			     .append(JANIComponentRegistrar.getLocationName(automaton))
			     .append(" = ")
			     .append(JANIComponentRegistrar.getLocationIdentifier(automaton, edge.getLocation()))
			     .append(") & ");
		}
		Guard guard = edge.getGuard();
		processor = ProcessorRegistrar.getProcessor(guard);
		prism.append(processor.toPRISM().toString());
		
		prism.append(" -> ");
		
		Destinations destinations = edge.getDestinations();
		processor = ProcessorRegistrar.getProcessor(destinations);
		processor.setAutomaton(automaton);
		prism.append(processor.toPRISM().toString());
				
		prism.append(";\n");
		
		return prism;
	}
	
	@Override
	public void validateTransientVariables() throws EPMCException {
		assert edge != null;
		
		ProcessorRegistrar.getProcessor(edge.getGuard()).validateTransientVariables();
		ProcessorRegistrar.getProcessor(edge.getDestinations()).validateTransientVariables();
	}

	@Override
	public boolean usesTransientVariables() throws EPMCException {
		assert edge != null;
		
		boolean usesTransient = false;
		usesTransient |= ProcessorRegistrar.getProcessor(edge.getGuard()).usesTransientVariables();
		usesTransient |= ProcessorRegistrar.getProcessor(edge.getDestinations()).usesTransientVariables();
		
		return usesTransient;
	}	
}
