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
import epmc.graph.SemanticsTimed;
import epmc.jani.model.component.Component;
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;
import epmc.prism.exporter.processor.JANIComponentRegistrar;
import epmc.prism.exporter.processor.ProcessorRegistrar;

public class ModelJANIProcessor implements JANI2PRISMProcessorStrict {
	
	public static final String INDENT = "\t"; 
	
	private ModelJANI jani = null;
	
	@Override
	public void setElement(Object obj) throws EPMCException {
		assert obj != null;
		assert obj instanceof ModelJANI;
		
		jani = (ModelJANI) obj;

		JANIComponentRegistrar.setIsTimedModel(SemanticsTimed.isTimed(jani.getSemantics()));
	}

	@Override
	public StringBuilder toPRISM() throws EPMCException {
		assert jani != null;
		
		StringBuilder prism = new StringBuilder();
		JANI2PRISMProcessorStrict processor;
		
		initialise();
		
		// Metadata
		Metadata metadata = jani.getMetadata();
		if (metadata != null) {
			processor = ProcessorRegistrar.getProcessor(metadata);
			prism.append(processor.toPRISM().toString()).append("\n");
		}
		
		// Semantic type
		ModelExtensionSemantics semantics = jani.getSemanticsExtension();
		processor = ProcessorRegistrar.getProcessor(semantics);
		prism.append(processor.toPRISM().toString()).append("\n");
		
		// Constants
		Constants constants = jani.getModelConstants();
		processor = ProcessorRegistrar.getProcessor(constants);
		prism.append(processor.toPRISM().toString()).append("\n");
		
		// Global variables
		for (Variable variable : JANIComponentRegistrar.getGlobalVariables()) {
			processor = ProcessorRegistrar.getProcessor(variable);
			processor.setPrefix("global ");
			processor.setForDefinition(true);
			prism.append(processor.toPRISM().toString()).append("\n");
		}
		
		// Automata
		Automata automata = jani.getAutomata();
		processor = ProcessorRegistrar.getProcessor(automata);
		prism.append(processor.toPRISM().toString()).append("\n");
		
		// Initial states expression
		InitialStates initial = jani.getRestrictInitial();
		if (initial != null) {
			processor = ProcessorRegistrar.getProcessor(initial);
			prism.append(processor.toPRISM().toString()).append("\n");
		}
		
		Component component = jani.getSystem();
		processor = ProcessorRegistrar.getProcessor(component);
		prism.append(processor.toPRISM().toString()).append("\n");
		
		// Rewards
		prism.append(JANIComponentRegistrar.toPRISMRewards().toString()).append("\n");
		
		return prism;
	}
	
	@Override
	public void validateTransientVariables() throws EPMCException {
		assert jani != null;
		
		ProcessorRegistrar.getProcessor(jani).validateTransientVariables();
	}
	
	@Override
	public boolean usesTransientVariables() throws EPMCException {
		assert jani != null;
		
		return ProcessorRegistrar.getProcessor(jani).usesTransientVariables();		
	}
	
	private void initialise() throws EPMCException {
		
		// Global variables to be registered
		for (Variable variable : jani.getGlobalVariables()) {
			JANIComponentRegistrar.registerVariable(variable);
		}
		
		// Variable assignment to be registered
		for (Automaton automaton : jani.getAutomata()) {
			ProcessorRegistrar.getProcessor(automaton).findAssignedVariables();
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
		ProcessorRegistrar.getProcessor(jani.getAutomata()).validateTransientVariables();
	}
}
