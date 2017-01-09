package epmc.jani.flatten;

import epmc.error.EPMCException;
import epmc.jani.model.component.Component;
import epmc.jani.model.component.ComponentAutomaton;
import epmc.jani.model.component.ComponentParallel;

public final class FlatterComponentParallel implements FlatterComponent {
	private Component component;

	@Override
	public void setComponent(Component component) {
		this.component = component;
	}

	@Override
	public boolean canHandle() {
		if (!(component instanceof ComponentParallel)) {
			return false;
		}
		return true;
	}

	@Override
	public ComponentAutomaton flatten() throws EPMCException {
		ComponentParallel componentParallel = (ComponentParallel) component;
		
		
		return (ComponentAutomaton) component;
	}

}
