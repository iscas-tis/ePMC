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

import epmc.prism.exporter.processor.PRISMExporter_ProcessorStrict;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorRegistrar;

public class PRISMExporter_DestinationsProcessor implements PRISMExporter_ProcessorStrict {

    private Destinations destinations = null;
    private Automaton automaton = null;

    @Override
    public PRISMExporter_ProcessorStrict setAutomaton(Automaton automaton) {
        this.automaton = automaton;
        return this;
    }

    @Override
    public PRISMExporter_ProcessorStrict setElement(Object obj) {
        assert obj != null;
        assert obj instanceof Destinations; 

        destinations = (Destinations) obj;
        return this;
    }

    @Override
    public String toPRISM() {
        assert destinations != null;

        StringBuilder prism = new StringBuilder();

        boolean notFirst = false;
        for (Destination destination : destinations) {
            PRISMExporter_ProcessorStrict processor = PRISMExporter_ProcessorRegistrar.getProcessor(destination);
            if (notFirst) {
                processor.setPrefix(" + ");
            } else {
                notFirst = true;
            }
            prism.append(processor.setAutomaton(automaton)
                    .toPRISM());
        }

        return prism.toString();
    }

    @Override
    public void validateTransientVariables() {
        assert destinations != null;

        for (Destination destination : destinations) {
            PRISMExporter_ProcessorRegistrar.getProcessor(destination)
                .validateTransientVariables();
        }
    }

    @Override
    public boolean usesTransientVariables() {
        assert destinations != null;

        boolean usesTransient = false;
        for (Destination destination : destinations) {
            usesTransient |= PRISMExporter_ProcessorRegistrar.getProcessor(destination)
                    .usesTransientVariables();
        }

        return usesTransient;
    }	
}
