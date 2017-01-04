package epmc.automaton;

import epmc.error.EPMCException;

public interface AutomatonParity extends Automaton {
	interface Builder extends Automaton.Builder {
		@Override
		AutomatonParity build() throws EPMCException;
	}
    int getNumPriorities();
}
