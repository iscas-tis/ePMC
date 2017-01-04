package epmc.automaton;

import epmc.util.BitSet;

public interface AutomatonSafra extends Automaton {
	interface Builder extends Automaton.Builder {
	    @Override
		Builder setBuechi(Buechi buechi);
	    Builder setInit(BitSet initialStates);
	}
}
