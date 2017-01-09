package epmc.propertysolver.ltllazy.automata;

import epmc.automaton.AutomatonStateBuechi;

public interface AutomatonSubsetState extends AutomatonStateBuechi {
    AutomatonSubset getAutomaton();
}
