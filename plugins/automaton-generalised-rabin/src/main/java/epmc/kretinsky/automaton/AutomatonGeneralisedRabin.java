package epmc.kretinsky.automaton;

import epmc.automaton.Automaton;

public interface AutomatonGeneralisedRabin extends Automaton {
    int getNumPairs();
    int getNumAccepting(int pair);
}
