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

import epmc.prism.exporter.JANIComponentRegistrar;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorStrict;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorRegistrar;

public class PRISMExporter_DestinationProcessor implements PRISMExporter_ProcessorStrict {

    private Destination destination = null;
    private String prefix = null;
    private Automaton automaton = null;

    @Override
    public PRISMExporter_ProcessorStrict setAutomaton(Automaton automaton) {
        assert automaton != null;
        
        this.automaton = automaton;
        return this;
    }

    @Override
    public PRISMExporter_ProcessorStrict setElement(Object obj) {
        assert obj != null;
        assert obj instanceof Destination; 

        destination = (Destination) obj;
        return this;
    }

    @Override
    public PRISMExporter_ProcessorStrict setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String toPRISM() {
        assert destination != null;
        assert automaton != null;

        StringBuilder prism = new StringBuilder();

        if (prefix != null)	{
            prism.append(prefix);
        }

        Probability probability = destination.getProbability();
        if (probability == null) {
            if (!PRISMExporter_ProcessorRegistrar.getUseExtendedPRISMSyntax()) {
                prism.append(PRISMExporter_ProcessorRegistrar.getProcessor(destination.getProbabilityExpressionOrOne())
                        .toPRISM())
                    .append(" : ");
            }
        } else {
            prism.append(PRISMExporter_ProcessorRegistrar.getProcessor(probability)
                    .toPRISM())
                .append(" : ");
        }

        Assignments assignments = destination.getAssignments();

        if (automaton.getLocations().size() > 1) {
            prism.append("(")
                .append(JANIComponentRegistrar.getLocationName(automaton))
                .append("'=")
                .append(JANIComponentRegistrar.getLocationIdentifier(automaton, destination.getLocation()))
                .append(")");
            if (assignments != null) {
                prism.append(" & ");
            }
        }
        if (assignments != null) {
            prism.append(PRISMExporter_ProcessorRegistrar.getProcessor(assignments)
                    .toPRISM());
        }

        return prism.toString();
    }

    @Override
    public void validateTransientVariables() {
        assert destination != null;

        PRISMExporter_ProcessorRegistrar.getProcessor(destination.getProbabilityExpressionOrOne())
            .validateTransientVariables();

        Assignments assignments = destination.getAssignments();
        if (assignments != null) {
            PRISMExporter_ProcessorRegistrar.getProcessor(assignments)
                .validateTransientVariables();
        }
    }

    @Override
    public boolean usesTransientVariables() {
        assert destination != null;

        boolean usesTransient = false;

        usesTransient |= PRISMExporter_ProcessorRegistrar.getProcessor(destination.getProbabilityExpressionOrOne())
                .usesTransientVariables();

        Assignments assignments = destination.getAssignments();
        if (assignments != null) {
            usesTransient |= PRISMExporter_ProcessorRegistrar.getProcessor(assignments)
                    .usesTransientVariables();
        }

        return usesTransient;
    }	
}
