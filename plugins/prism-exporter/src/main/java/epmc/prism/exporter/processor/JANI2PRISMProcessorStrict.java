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

import epmc.error.EPMCException;
import epmc.jani.model.Automaton;
import epmc.value.ContextValue;

public interface JANI2PRISMProcessorStrict {
	
	default JANI2PRISMProcessorStrict setContextValue(ContextValue contextValue) { return this; }
	
	default JANI2PRISMProcessorStrict setPrefix(String prefix) { return this; }

	default JANI2PRISMProcessorStrict setForDefinition(boolean forDefinition) { return this; }

	default JANI2PRISMProcessorStrict setAutomaton(Automaton automaton) { return this; }
	
	JANI2PRISMProcessorStrict setElement(Object obj) throws EPMCException;

	/**
	 * Generate a PRISM representation of the component.
	 * @return the PRISM representation
	 * @throws EPMCException if the component has no PRISM counterpart
	 */
	String toPRISM() throws EPMCException;
	
	/**
	 * Explore the JANI model and identify the variables that are assigned 
	 * in some edge of the automata labelled with a synchronising action. 
	 * @throws EPMCException 
	 */
	default void findAssignedVariables() throws EPMCException {}
	
	/**
	 * Explore the JANI model and check whether the use of transient variables is compatible with PRISM 
	 * @throws EPMCException if a transient variable is not used in a way not compatible with PRISM 
	 */
	void validateTransientVariables() throws EPMCException;
	
	/**
	 * Explore the JANI model and check whether a transient variable is used
	 * @return whether a transient variable is used
	 * @throws EPMCException in case of problems in exploring the JANI model
	 */
	boolean usesTransientVariables() throws EPMCException;
}
