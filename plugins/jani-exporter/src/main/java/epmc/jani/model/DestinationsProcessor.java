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

import javax.json.JsonValue;

import epmc.jani.exporter.processor.JANIProcessor;
import epmc.jani.exporter.processor.ProcessorRegistrar;

public class DestinationsProcessor implements JANIProcessor {

    private Destinations destinations = null;
    private Automaton automaton = null;

    @Override
    public JANIProcessor setAutomaton(Automaton automaton) {
        this.automaton = automaton;
        return this;
    }

    @Override
    public JANIProcessor setElement(Object component) {
        assert component != null;
        assert component instanceof Destinations; 

        destinations = (Destinations) component;
        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert destinations != null;

        StringBuilder prism = new StringBuilder();

        boolean remaining = false;
        for (Destination destination : destinations) {
            JANIProcessor processor = ProcessorRegistrar.getProcessor(destination);
            if (remaining) {
                processor.setPrefix(" + ");
            } else {
                remaining = true;
            }
            prism.append(processor.setAutomaton(automaton)
                    .toJSON());
        }

        return prism.toString();
    }

    @Override
    public void validateTransientVariables() {
        assert destinations != null;

        for (Destination destination : destinations) {
            ProcessorRegistrar.getProcessor(destination)
                .validateTransientVariables();
        }
    }

    @Override
    public boolean usesTransientVariables() {
        assert destinations != null;

        boolean usesTransient = false;
        for (Destination destination : destinations) {
            usesTransient |= ProcessorRegistrar.getProcessor(destination)
                    .usesTransientVariables();
        }

        return usesTransient;
    }	
}
