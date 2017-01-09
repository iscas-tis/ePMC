package epmc.jani.flatten;

import epmc.error.EPMCException;
import epmc.jani.model.component.Component;
import epmc.jani.model.component.ComponentAutomaton;

public final class FlatterComponentAutomaton implements FlatterComponent {
	private Component component;

	@Override
	public void setComponent(Component component) {
		this.component = component;
	}

	@Override
	public boolean canHandle() {
		if (!(component instanceof ComponentAutomaton)) {
			return false;
		}
		return true;
	}

	@Override
	public ComponentAutomaton flatten() throws EPMCException {
		return (ComponentAutomaton) component;
	}

}
