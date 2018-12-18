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

import epmc.jani.exporter.JANIComponentRegistrar;
import epmc.jani.exporter.processor.JANIProcessor;
import epmc.jani.exporter.processor.ProcessorRegistrar;

public class DestinationProcessor implements JANIProcessor {

    private Destination destination = null;
    private String prefix = null;
    private Automaton automaton = null;

    @Override
    public JANIProcessor setAutomaton(Automaton automaton) {
        this.automaton = automaton;
        return this;
    }

    @Override
    public JANIProcessor setElement(Object component) {
        assert component != null;
        assert component instanceof Destination; 

        destination = (Destination) component;
        return this;
    }

    @Override
    public JANIProcessor setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert destination != null;

        StringBuilder prism = new StringBuilder();

        if (prefix != null)	{
            prism.append(prefix);
        }

        Probability probability = destination.getProbability();
        if (probability == null) {
            if (!ProcessorRegistrar.getUseExtendedPRISMSyntax()) {
                prism.append(ProcessorRegistrar.getProcessor(destination.getProbabilityExpressionOrOne())
                        .toJSON())
                    .append(" : ");
            }
        } else {
            prism.append(ProcessorRegistrar.getProcessor(probability)
                    .toJSON())
                .append(" : ");
        }

        if (automaton.getLocations().size() > 1) {
            prism.append("(")
                .append(JANIComponentRegistrar.getLocationName(automaton))
                .append("'=")
                .append(JANIComponentRegistrar.getLocationIdentifier(automaton, destination.getLocation()))
                .append(") & ");
        }
        Assignments assignments = destination.getAssignments();
        if (assignments != null) {
            prism.append(ProcessorRegistrar.getProcessor(assignments)
                    .toJSON());
        }

        return prism.toString();
    }

    @Override
    public void validateTransientVariables() {
        assert destination != null;

        ProcessorRegistrar.getProcessor(destination.getProbabilityExpressionOrOne())
            .validateTransientVariables();

        Assignments assignments = destination.getAssignments();
        if (assignments != null) {
            ProcessorRegistrar.getProcessor(assignments)
                .validateTransientVariables();
        }
    }

    @Override
    public boolean usesTransientVariables() {
        assert destination != null;

        boolean usesTransient = false;

        usesTransient |= ProcessorRegistrar.getProcessor(destination.getProbabilityExpressionOrOne())
                .usesTransientVariables();

        Assignments assignments = destination.getAssignments();
        if (assignments != null) {
            usesTransient |= ProcessorRegistrar.getProcessor(assignments)
                    .usesTransientVariables();
        }

        return usesTransient;
    }	
}
