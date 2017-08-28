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
	public JANI2PRISMProcessorStrict setElement(Object obj) {
		assert obj != null;
		assert obj instanceof Edge; 
		
		edge = (Edge) obj;
		
		Action action = edge.getActionOrSilent();
		for (Destination destination: edge.getDestinations()) {
			for (AssignmentSimple assignment : destination.getAssignmentsOrEmpty()) {
				Variable reward = assignment.getRef();
				if (reward.isTransient()) {
					JANIComponentRegistrar.registerTransitionRewardExpression(reward, action, assignment.getValue());
				}
			}
		}
		return this;
	}

	@Override
	public JANI2PRISMProcessorStrict setAutomaton(Automaton automaton) {
		this.automaton = automaton;
		return this;
	}
	
	@Override
	public JANI2PRISMProcessorStrict setPrefix(String prefix) {
		this.prefix = prefix;
		return this;
	}

	@Override
	public String toPRISM() {
		assert edge != null;
		
		Rate edgeRate = edge.getRate();
		
		if (edgeRate != null && SemanticsDTMC.isDTMC(edge.getModel().getSemantics())) {
			Rate rateOne = new Rate();
			rateOne.setExp(ExpressionLiteral.getOne());
			ensure(rateOne.equals(edgeRate), 
				   ProblemsPRISMExporter.PRISM_EXPORTER_UNSUPPORTED_FEATURE_EDGE_RATE_NOT_ONE);
		}
		
		StringBuilder prism = new StringBuilder();

		String comment = edge.getComment();
		if (comment != null) {
			if (prefix != null) {
				prism.append(prefix);
			}
			prism.append("// ")
				 .append(comment)
				 .append("\n");
		}
		
		if (prefix != null)	{
			prism.append(prefix);
		}
		prism.append(ProcessorRegistrar.getProcessor(edge.getActionOrSilent())
									   .toPRISM())
			 .append(" ");
		
		if (automaton.getLocations().size() > 1) {
			prism.append("(")
			     .append(JANIComponentRegistrar.getLocationName(automaton))
			     .append(" = ")
			     .append(JANIComponentRegistrar.getLocationIdentifier(automaton, edge.getLocation()))
			     .append(") & ");
		}
		prism.append(ProcessorRegistrar.getProcessor(edge.getGuard())
									   .toPRISM())
			 .append(" -> ")
			 .append(ProcessorRegistrar.getProcessor(edge.getDestinations())
					 				   .setAutomaton(automaton)
					 				   .toPRISM())
			 .append(";\n");
		
		return prism.toString();
	}
	
	@Override
	public void validateTransientVariables() {
		assert edge != null;
		
		ProcessorRegistrar.getProcessor(edge.getGuard())
						  .validateTransientVariables();
		ProcessorRegistrar.getProcessor(edge.getDestinations())
					      .validateTransientVariables();
	}

	@Override
	public boolean usesTransientVariables() {
		assert edge != null;
		
		boolean usesTransient = false;
		usesTransient |= ProcessorRegistrar.getProcessor(edge.getGuard())
										   .usesTransientVariables();
		usesTransient |= ProcessorRegistrar.getProcessor(edge.getDestinations())
								 	 	   .usesTransientVariables();
		
		return usesTransient;
	}	
}
