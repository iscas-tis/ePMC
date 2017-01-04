package epmc.unambiguous.automaton;

import epmc.automaton.BuechiTransition;
import epmc.value.Value;

public interface UBATransition extends BuechiTransition {
	
	public void setResult(Value result);

}
