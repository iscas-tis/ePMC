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

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;
import epmc.prism.exporter.processor.JANIComponentRegistrar;
import epmc.prism.exporter.processor.ProcessorRegistrar;

public class EdgeProcessor implements JANI2PRISMProcessorStrict {

	private Edge edge = null;
	private String prefix = null;
	
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
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public StringBuilder toPRISM() throws EPMCException {
		assert edge != null;
		
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
		
		Guard guard = edge.getGuard();
		processor = ProcessorRegistrar.getProcessor(guard);
		prism.append(processor.toPRISM().toString());
		
		prism.append(" -> ");
		
		Destinations destinations = edge.getDestinations();
		processor = ProcessorRegistrar.getProcessor(destinations);
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
