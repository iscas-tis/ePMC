package epmc.unambiguous.automaton;

import epmc.util.BitSet;


public interface AutomatonUBALabel {
	/*get transition label */
	BitSet getLabel();
	/* get predecessor, do not know whether it is necessary */
	int getPredecessor();
}
