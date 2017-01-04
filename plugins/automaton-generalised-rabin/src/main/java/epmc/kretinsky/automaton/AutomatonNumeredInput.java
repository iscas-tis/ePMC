package epmc.kretinsky.automaton;

import epmc.automaton.Automaton;
import epmc.error.EPMCException;

public interface AutomatonNumeredInput extends Automaton {
    void queryState(int modelState, int observerState) throws EPMCException;
}
