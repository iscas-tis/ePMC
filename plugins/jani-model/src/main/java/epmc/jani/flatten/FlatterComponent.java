package epmc.jani.flatten;

import epmc.error.EPMCException;
import epmc.jani.model.component.Component;
import epmc.jani.model.component.ComponentAutomaton;

public interface FlatterComponent {
	void setComponent(Component component);
	
	boolean canHandle();
	
	ComponentAutomaton flatten() throws EPMCException;
}
