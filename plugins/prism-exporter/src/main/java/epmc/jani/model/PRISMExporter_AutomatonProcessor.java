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

public class PRISMExporter_AutomatonProcessor implements PRISMExporter_ProcessorStrict {

    private Automaton automaton = null;

    @Override
    public PRISMExporter_ProcessorStrict setElement(Object obj) {
        assert obj != null;
        assert obj instanceof Automaton;

        automaton = (Automaton) obj;
        return this;
    }

    @Override
    public void findAssignedVariables() {
        for (Edge edge : automaton.getEdges()) {
            Action action = edge.getActionOrSilent();
            for (Destination destination : edge.getDestinations()) {
                for (AssignmentSimple assignment : destination.getAssignmentsOrEmpty()) {
                    JANIComponentRegistrar.registerNonTransientVariableAssignment(assignment.getRef(), action, automaton);
                }
            }
        }
    }

    @Override
    public String toPRISM() {
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
            prism.append(PRISMExporter_ProcessorRegistrar.getProcessor(local)
                    .setPrefix(PRISMExporter_ModelJANIProcessor.INDENT)
                    .setForDefinition(true)
                    .toPRISM());
        }

        if (automaton.equals(JANIComponentRegistrar.getDefaultAutomatonForUnassignedClocks())) {
            for (Variable variable : JANIComponentRegistrar.getUnassignedClockVariables()) {
                prism.append(PRISMExporter_ProcessorRegistrar.getProcessor(variable)
                        .setPrefix(PRISMExporter_ModelJANIProcessor.INDENT)
                        .setForDefinition(true)
                        .toPRISM());
            }
        }
        for (Variable variable : JANIComponentRegistrar.getLocalVariablesOrEmpty(automaton)) {
            prism.append(PRISMExporter_ProcessorRegistrar.getProcessor(variable)
                    .setPrefix(PRISMExporter_ModelJANIProcessor.INDENT)
                    .setForDefinition(true)
                    .toPRISM());
        }

        prism.append(PRISMExporter_ProcessorRegistrar.getProcessor(automaton.getLocations())
                .setPrefix(PRISMExporter_ModelJANIProcessor.INDENT)
                .setAutomaton(automaton)
                .toPRISM())
            .append("\n")
            .append(PRISMExporter_ProcessorRegistrar.getProcessor(automaton.getEdges())
                .setPrefix(PRISMExporter_ModelJANIProcessor.INDENT)
                .setAutomaton(automaton)
                .toPRISM())
            .append("endmodule\n");

        return prism.toString();
    }

    @Override
    public void validateTransientVariables() {
        assert automaton != null;

        InitialStates initial = automaton.getInitialStates();
        if (initial != null) {
            PRISMExporter_ProcessorRegistrar.getProcessor(initial)
                .validateTransientVariables();
        }
        PRISMExporter_ProcessorRegistrar.getProcessor(automaton.getLocations())
            .validateTransientVariables();
        PRISMExporter_ProcessorRegistrar.getProcessor(automaton.getEdges())
            .validateTransientVariables();
    }

    @Override
    public boolean usesTransientVariables() {
        assert automaton != null;

        boolean usesTransient = false;
        InitialStates initial = automaton.getInitialStates();
        if (initial != null) {
            usesTransient |= PRISMExporter_ProcessorRegistrar.getProcessor(initial)
                    .usesTransientVariables();
        }
        usesTransient |= PRISMExporter_ProcessorRegistrar.getProcessor(automaton.getLocations())
                .usesTransientVariables();
        usesTransient |= PRISMExporter_ProcessorRegistrar.getProcessor(automaton.getEdges())
                .usesTransientVariables();

        return usesTransient;
    }	
}
