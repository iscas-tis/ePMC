package epmc.unambiguous.automaton;

public interface AutomatonUBAState {
	/* get buechi automaton state */
	int getAutomatonState();
	
	/* check whether it is a accepting state */
	boolean isAccepting();

}
