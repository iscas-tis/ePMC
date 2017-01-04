package epmc.automaton;

import epmc.util.BitSet;

public interface AutomatonRabinLabel {
    BitSet getAccepting();

    BitSet getStable();
}
