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

import epmc.jani.model.Automaton;

public interface PRISMExporter_ProcessorStrict {	
    default PRISMExporter_ProcessorStrict setPrefix(String prefix) { return this; };

    default PRISMExporter_ProcessorStrict setForDefinition(boolean forDefinition) { return this; };

    default PRISMExporter_ProcessorStrict setAutomaton(Automaton automaton) { return this; };

    PRISMExporter_ProcessorStrict setElement(Object obj);

    /**
     * Generate a PRISM representation of the component.
     * @return the PRISM representation
     */
    String toPRISM();

    /**
     * Explore the JANI model and identify the variables that are assigned 
     * in some edge of the automata labelled with a synchronising action. 
     */
    default void findAssignedVariables() {};

    /**
     * Explore the JANI model and check whether the use of transient variables is compatible with PRISM 
     */
    void validateTransientVariables();

    /**
     * Explore the JANI model and check whether a transient variable is used
     * @return whether a transient variable is used
     */
    boolean usesTransientVariables();
}
