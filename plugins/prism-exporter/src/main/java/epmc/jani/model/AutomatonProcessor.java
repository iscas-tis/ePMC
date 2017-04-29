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
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;
import epmc.prism.exporter.processor.JANIComponentRegistrar;
import epmc.prism.exporter.processor.ProcessorRegistrar;

public class AutomatonProcessor implements JANI2PRISMProcessorStrict {

	private Automaton automaton = null;
	
	@Override
	public JANI2PRISMProcessorStrict setElement(Object obj) throws EPMCException {
		assert obj != null;
		assert obj instanceof Automaton;
		
		automaton = (Automaton) obj;
		return this;
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
	public String toPRISM() throws EPMCException {
		assert automaton != null;
		
		StringBuilder prism = new StringBuilder();
		
		String comment = automaton.getComment();
		if (comment != null) {
			prism.append("// ")
				 .append(comment)
				 .append("\n");
		}

		prism.append("module ")
			 .append(automaton.getName())
			 .append("\n");
		
		Variables local = automaton.getVariablesNonTransient();
		if (local != null) {
			prism.append(ProcessorRegistrar.getProcessor(local)
										   .setPrefix(ModelJANIProcessor.INDENT)
										   .setForDefinition(true)
										   .toPRISM());
		}
		
		if (automaton.equals(JANIComponentRegistrar.getDefaultAutomatonForUnassignedClocks())) {
			for (Variable variable : JANIComponentRegistrar.getUnassignedClockVariables()) {
				prism.append(ProcessorRegistrar.getProcessor(variable)
											   .setPrefix(ModelJANIProcessor.INDENT)
											   .setForDefinition(true)
											   .toPRISM());
			}
		}
		for (Variable variable : JANIComponentRegistrar.getAssignedVariablesOrEmpty(automaton)) {
			prism.append(ProcessorRegistrar.getProcessor(variable)
										   .setPrefix(ModelJANIProcessor.INDENT)
										   .setForDefinition(true)
										   .toPRISM());
		}
		
		prism.append(ProcessorRegistrar.getProcessor(automaton.getLocations())
									   .setPrefix(ModelJANIProcessor.INDENT)
									   .setAutomaton(automaton)
									   .toPRISM())
			 .append("\n")
			 .append(ProcessorRegistrar.getProcessor(automaton.getEdges())
									   .setPrefix(ModelJANIProcessor.INDENT)
									   .setAutomaton(automaton)
									   .toPRISM())
			 .append("endmodule\n");
		
		return prism.toString();
	}
	
	@Override
	public void validateTransientVariables() throws EPMCException {
		assert automaton != null;
		
		InitialStates initial = automaton.getInitialStates();
		if (initial != null) {
			ProcessorRegistrar.getProcessor(initial)
							  .validateTransientVariables();
		}
		ProcessorRegistrar.getProcessor(automaton.getLocations())
						  .validateTransientVariables();
		ProcessorRegistrar.getProcessor(automaton.getEdges())
						  .validateTransientVariables();
	}

	@Override
	public boolean usesTransientVariables() throws EPMCException {
		assert automaton != null;
		
		boolean usesTransient = false;
		InitialStates initial = automaton.getInitialStates();
		if (initial != null) {
			usesTransient |= ProcessorRegistrar.getProcessor(initial)
											   .usesTransientVariables();
		}
		usesTransient |= ProcessorRegistrar.getProcessor(automaton.getLocations())
										   .usesTransientVariables();
		usesTransient |= ProcessorRegistrar.getProcessor(automaton.getEdges())
										   .usesTransientVariables();
		
		return usesTransient;
	}	
}
