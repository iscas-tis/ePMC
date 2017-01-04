package epmc.propertysolverltlfg.hoa;

import java.util.List;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.propertysolverltlfg.automaton.AcceptanceCondition;
import epmc.propertysolverltlfg.automaton.AutomatonRabin;
import epmc.propertysolverltlfg.automaton.AutomatonType;
import epmc.util.BitSet;
/**
 * General interface for parsing Hanoi format
 * @author Yong Li
 * */
public interface HOAUser {
	
	default void parseStart() {}
	default void setHOAVer(String version) {}
	default void setTool(String name, String version) {}
	default void setAutName(String name) {}
	default void addProperties(List<String> properties) {}
	void setNumOfStates(int numOfStates) throws EPMCException;
	void setStartStates(List<Integer> conjStates);
	default void setAccName(String name, List<Integer> nums) {}
	void setAcceptances(int numOfSets, List<AcceptanceCondition> accs); 
	void setAccExpressions(int numOfSets, List<Expression> accs); 
	void setAps(int numOfAps, List<String> aps); // how to deal with "dummy"
	default void startBody() {}
	default void startOfState() {}
	void setCurrState(int id, Expression label, String comment, BitSet signature);
	void addEdge(int succ, Expression label, BitSet signature);
	default void endOfState() throws EPMCException {}
	default void endBody() {}
	default void parseEnd() {}
	
	default void abort() {
		System.err.println("Expception occured during parsing");
		System.exit(-1);
	}
	AutomatonType getAutomatonType();
	AutomatonRabin getAutomaton();
	void prepare() throws EPMCException;  // prepare graph
}
