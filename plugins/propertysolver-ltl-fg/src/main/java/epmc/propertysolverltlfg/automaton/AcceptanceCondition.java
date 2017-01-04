package epmc.propertysolverltlfg.automaton;

import java.util.List;

// This class is enough currently
public class AcceptanceCondition {
	
	private AcceptanceLabel finiteStates = null;
	private List<AcceptanceLabel> infiniteStates = null;
	
	public AcceptanceCondition(AcceptanceLabel fin, List<AcceptanceLabel> infs) {
		this.finiteStates = fin;
		this.infiniteStates = infs;
	}
	
	public AcceptanceLabel getFiniteStates() {
		return this.finiteStates;
	}
	
	public List<AcceptanceLabel> getInfiniteStates() {
		return this.infiniteStates;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if(this.finiteStates != null) {
			builder.append(this.finiteStates.toString());
			if(this.infiniteStates.size() > 0) builder.append(" & ");
		}
		boolean first = true;
		for(AcceptanceLabel inf : infiniteStates) {
			if(! first) builder.append(" & ");
			first = false;
			builder.append(inf.toString());
		}
		return builder.toString();
	}

}
