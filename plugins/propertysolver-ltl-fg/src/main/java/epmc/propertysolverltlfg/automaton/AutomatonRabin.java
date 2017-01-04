package epmc.propertysolverltlfg.automaton;

import java.util.List;

import epmc.automaton.Buechi;
import epmc.expression.Expression;

public interface AutomatonRabin extends Buechi {
	
	AutomatonType getAutomatonType();
	List<AcceptanceCondition> getAcceptances();
	List<Expression> getAccExpressions();

}
