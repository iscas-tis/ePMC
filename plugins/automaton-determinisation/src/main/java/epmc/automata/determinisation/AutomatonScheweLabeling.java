package epmc.automata.determinisation;

import epmc.automaton.AutomatonLabelUtil;
import epmc.automaton.AutomatonParityLabel;
import epmc.automaton.AutomatonRabinLabel;
import epmc.util.BitSet;

final class AutomatonScheweLabeling implements AutomatonLabelUtil, AutomatonRabinLabel, AutomatonParityLabel {
	/** Space containing a single space. */
	private final static String SPACE = " ";
	
	/** Automaton to which this label belongs. */
	private AutomatonSchewe automaton;
	/** Accepting states, if used as Rabin automaton label. */
    private final BitSet accepting;
    /** Stable states, if used as Rabin automaton label. */
    private final BitSet stable;
    /** Priority, if used as parity automaton label. */
    private final int priority;
    /** Number of the automaton label. */
    private int number;
    
    AutomatonScheweLabeling(AutomatonSchewe automaton, BitSet accepting, BitSet stable, int priority) {
    	assert automaton != null;
    	this.automaton = automaton;
        this.accepting = accepting;
        this.stable = stable;
        this.priority = priority;
    }

    @Override
    public BitSet getAccepting() {
        return accepting;
    }

    @Override
    public BitSet getStable() {
        return stable;
    }
    
    @Override
    public int getPriority() {
        return priority;
    }
    
    @Override
    public String toString() {
    	if (automaton.isParity()) {
    		return Integer.toString(priority);
    	} else {
    		return accepting + SPACE + stable;
    	}
    }

    @Override
    public int getNumber() {
        return this.number;
    }

    @Override
    public void setNumber(int number) {
        this.number = number;
    }
}
