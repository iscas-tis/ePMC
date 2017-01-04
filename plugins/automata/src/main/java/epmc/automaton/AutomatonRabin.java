package epmc.automaton;

import epmc.error.EPMCException;

public interface AutomatonRabin extends Automaton {
	interface Builder extends Automaton.Builder {
		@Override
		AutomatonRabin build() throws EPMCException;
	}
	
    int getNumPairs();
}
