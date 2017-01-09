package epmc.jani.flatten;

import epmc.jani.model.Automata;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.component.Component;
import epmc.jani.model.component.ComponentAutomaton;

public final class Flatter {
	private ModelJANI model;

	public void setModel(ModelJANI model) {
		this.model = model;
	}
	
	public void flatten() {
		Component origSystem = model.getSystem();
		ComponentAutomaton newSystem = flatten(origSystem);
		Automata newAutomata = new Automata();
		newAutomata.setModel(model);
		newAutomata.addAutomaton(newSystem.getAutomaton());
		model.setAutomata(newAutomata);
		model.setSystem(newSystem);
	}

	private ComponentAutomaton flatten(Component origSystem) {
		// TODO Auto-generated method stub
		return null;
	}
}
