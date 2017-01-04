package epmc.kretinsky.automaton;

import epmc.error.EPMCException;

public interface AutomatonGeneralisedRabinLabel {
    boolean isAccepting(int pair, int number) throws EPMCException;

    boolean isStable(int pair) throws EPMCException;
}
