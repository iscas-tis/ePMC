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

import epmc.graph.SemanticsNonDet;
import epmc.graph.SemanticsTimed;
import epmc.prism.exporter.JANIComponentRegistrar;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorStrict;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorRegistrar;

public class PRISMExporter_ModelJANIProcessor implements PRISMExporter_ProcessorStrict {

    public static final String INDENT = "\t"; 

    private ModelJANI jani = null;

    @Override
    public PRISMExporter_ProcessorStrict setElement(Object obj) {
        assert obj != null;
        assert obj instanceof ModelJANI;

        jani = (ModelJANI) obj;

        JANIComponentRegistrar.setIsTimedModel(SemanticsTimed.isTimed(jani.getSemantics()));
        JANIComponentRegistrar.setIsNonDeterministicModel(SemanticsNonDet.isNonDet(jani.getSemantics()));
        JANIComponentRegistrar.addSilentAction(jani.getSilentAction());
        return this;
    }

    @Override
    public String toPRISM() {
        assert jani != null;

        StringBuilder prism = new StringBuilder();

        initialise();

        // Metadata
        Metadata metadata = jani.getMetadata();
        if (metadata != null) {
            prism.append(PRISMExporter_ProcessorRegistrar.getProcessor(metadata)
                    .toPRISM())
            	.append("\n");
        }

        prism.append(JANIComponentRegistrar.actionsRenaming())
        // Semantic type
        	.append(PRISMExporter_ProcessorRegistrar.getProcessor(jani.getSemanticsExtension())
                .toPRISM())
        	.append("\n");
        // Constants
        prism.append(JANIComponentRegistrar.constantsRenaming());
        Constants constants = jani.getModelConstants();
        if (constants != null) {
            prism.append(PRISMExporter_ProcessorRegistrar.getProcessor(constants)
                    .toPRISM())
                .append("\n");
        }

        // Global variables
        prism.append(JANIComponentRegistrar.globalVariablesRenaming());
        for (Variable variable : JANIComponentRegistrar.getGlobalVariables()) {
            prism.append(PRISMExporter_ProcessorRegistrar.getProcessor(variable)
                    .setPrefix("global ")
                    .setForDefinition(true)
                    .toPRISM())
                .append("\n");
        }

        // Automata
        prism.append(PRISMExporter_ProcessorRegistrar.getProcessor(jani.getAutomata())
                .toPRISM())
            .append("\n")
        //Initial conditions
            .append(JANIComponentRegistrar.processInitialConditions())
        //Synchronisation vectors / system
            .append(PRISMExporter_ProcessorRegistrar.getProcessor(jani.getSystem())
                .toPRISM())
            .append("\n")
        // Rewards
            .append(JANIComponentRegistrar.toPRISMRewards())
            .append("\n");

        return prism.toString();
    }

    @Override
    public void validateTransientVariables() {
        assert jani != null;

        PRISMExporter_ProcessorRegistrar.getProcessor(jani.getAutomata())
            .validateTransientVariables();
    }

    @Override
    public boolean usesTransientVariables() {
        assert jani != null;

        return PRISMExporter_ProcessorRegistrar.getProcessor(jani.getAutomata())
                .usesTransientVariables();		
    }

    private void initialise() {

        // Variable assignment to be registered
        for (Automaton automaton : jani.getAutomata()) {
            JANIComponentRegistrar.setDefaultAutomatonForUnassignedClocks(automaton);
            PRISMExporter_ProcessorRegistrar.getProcessor(automaton)
                .findAssignedVariables();
        }

        // Global variables to be registered
        for (Variable variable : jani.getGlobalVariables()) {
            JANIComponentRegistrar.registerIdentifier(variable);
        }

        // Global variables non transient to be registered
        for (Variable variable : jani.getGlobalVariablesNonTransient()) {
            JANIComponentRegistrar.registerGlobalVariable(variable);
        }

        // Actions to be registered
        for (Action action : jani.getActionsOrEmpty()) {
            JANIComponentRegistrar.registerAction(action);
        }

        // check for transient variables being used in guards or in assigning values to non-transient variables
        PRISMExporter_ProcessorRegistrar.getProcessor(jani.getAutomata()).validateTransientVariables();

        // Initial states expression
        InitialStates initial = jani.getRestrictInitial();
        if (initial != null) {
            JANIComponentRegistrar.registerInitialRestriction(initial);
        }
        for (Automaton automaton : jani.getAutomata().getAutomata().values()) {
            JANIComponentRegistrar.registerInitialLocation(automaton, automaton.getInitialLocations());
        }
    }
}
