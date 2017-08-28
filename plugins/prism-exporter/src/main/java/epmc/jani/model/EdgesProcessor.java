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

import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;
import epmc.prism.exporter.processor.ProcessorRegistrar;

public class EdgesProcessor implements JANI2PRISMProcessorStrict {

	private Edges edges = null;
	private String prefix = null;
	private Automaton automaton = null;
	
	@Override
	public JANI2PRISMProcessorStrict setElement(Object obj) {
		assert obj != null;
		assert obj instanceof Edges; 
		
		edges = (Edges) obj;
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
		assert edges != null;
		
		StringBuilder prism = new StringBuilder();
		
		for (Edge edge : edges) {
			prism.append(ProcessorRegistrar.getProcessor(edge)
					  					   .setPrefix(prefix)
					  					   .setAutomaton(automaton)
					  					   .toPRISM());
		}
		
		return prism.toString();
	}
	
	@Override
	public void validateTransientVariables() {
		assert edges != null;
		
		for (Edge edge : edges) {
			ProcessorRegistrar.getProcessor(edge)
							  .validateTransientVariables();
		}
	}

	@Override
	public boolean usesTransientVariables() {
		assert edges != null;
		
		boolean usesTransient = false;
		for (Edge edge : edges) {
			usesTransient |= ProcessorRegistrar.getProcessor(edge)
											   .usesTransientVariables();
		}
		
		return usesTransient;
	}	
}
