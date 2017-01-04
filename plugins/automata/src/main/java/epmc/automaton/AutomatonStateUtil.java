package epmc.automaton;

public interface AutomatonStateUtil extends Cloneable {
    Automaton getAutomaton();
    
    void setNumber(int number);

    int getNumber();
}
