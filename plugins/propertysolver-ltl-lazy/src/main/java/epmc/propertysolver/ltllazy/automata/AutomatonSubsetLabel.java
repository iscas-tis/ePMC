package epmc.propertysolver.ltllazy.automata;

import epmc.util.BitSet;

public interface AutomatonSubsetLabel {
    BitSet getUnder();
    
    BitSet getOver();
}
