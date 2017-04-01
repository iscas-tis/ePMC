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

public class AutomatonProcessor implements JANI2PRISMProcessorStrict {

	private Automaton automaton = null;
	
	@Override
	public void setElement(Object obj) throws EPMCException {
		assert obj != null;
		assert obj instanceof Automaton;
		
		automaton = (Automaton) obj;
	}

	@Override
	public void findAssignedVariables() throws EPMCException {
		for (Edge edge : automaton.getEdges()) {
			Action action = edge.getActionOrSilent();
			if (!JANIComponentRegistrar.isSilentAction(action)) {
				for (Destination destination : edge.getDestinations()) {
					for (AssignmentSimple assignment : destination.getAssignmentsOrEmpty()) {
						JANIComponentRegistrar.registerNonTransientVariableAssignment(assignment.getRef(), automaton);
					}
				}
			}
		}
	}

	@Override
	public StringBuilder toPRISM() throws EPMCException {
		assert automaton != null;

		StringBuilder prism = new StringBuilder();
		JANI2PRISMProcessorStrict processor; 
		
		String comment = automaton.getComment();
		if (comment != null) {
			prism.append("// ").append(comment).append("\n");
		}

		prism.append("module ").append(automaton.getName()).append("\n");
		
		Variables local = automaton.getVariablesNonTransient();
		if (local != null) {
			processor = ProcessorRegistrar.getProcessor(local);
			processor.setPrefix(ModelJANIProcessor.INDENT);
			processor.setForDefinition(true);
			prism.append(processor.toPRISM().toString());
		}
		
		for (Variable variable : JANIComponentRegistrar.getAssignedVariablesOrEmpty(automaton)) {
			processor = ProcessorRegistrar.getProcessor(variable);
			processor.setPrefix(ModelJANIProcessor.INDENT);
			processor.setForDefinition(true);
			prism.append(processor.toPRISM().toString());
		}
		
		Locations locations = automaton.getLocations();
		processor = ProcessorRegistrar.getProcessor(locations);
		processor.setPrefix(ModelJANIProcessor.INDENT);
		prism.append(processor.toPRISM().toString());
		
		Edges edges = automaton.getEdges();
		processor = ProcessorRegistrar.getProcessor(edges);
		processor.setPrefix(ModelJANIProcessor.INDENT);
		prism.append(processor.toPRISM().toString());
		
		prism.append("endmodule\n");
		
		return prism;
	}
	
	@Override
	public void validateTransientVariables() throws EPMCException {
		assert automaton != null;
		
		InitialStates initial = automaton.getInitialStates();
		if (initial != null) {
			ProcessorRegistrar.getProcessor(initial).validateTransientVariables();
		}
		ProcessorRegistrar.getProcessor(automaton.getLocations()).validateTransientVariables();
		ProcessorRegistrar.getProcessor(automaton.getEdges()).validateTransientVariables();
	}

	@Override
	public boolean usesTransientVariables() throws EPMCException {
		assert automaton != null;
		
		boolean usesTransient = false;
		InitialStates initial = automaton.getInitialStates();
		if (initial != null) {
			usesTransient |= ProcessorRegistrar.getProcessor(initial).usesTransientVariables();
		}
		usesTransient |= ProcessorRegistrar.getProcessor(automaton.getLocations()).usesTransientVariables();
		usesTransient |= ProcessorRegistrar.getProcessor(automaton.getEdges()).usesTransientVariables();
		
		return usesTransient;
	}	
}
