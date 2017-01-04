package epmc.propertysolverltlfg.automaton;

public class AcceptanceLabel {
	
	private AcceptanceType type;
	// -1 for true, and -2 for false ? 
	// NO need , since it is impossible,
	// DO NOT ALLOW
	private Integer set = null;
	private boolean negatedOrValue = false;
	
	public AcceptanceLabel(boolean value) {
		this.negatedOrValue = value;
	}
	
	// if negated is true then we may have !8
	public AcceptanceLabel(AcceptanceType type, Integer acc, boolean isNegated) {
		this.type = type;
		this.set = acc;
		this.negatedOrValue = isNegated;
	}
	
	public void setType(AcceptanceType type) {
		this.type = type;
	}
	
	public void setStateSet(Integer acc) {
		this.set = acc;
	}
	
	public void setFlag(boolean isNegated) {
		this.negatedOrValue = isNegated;
	}
	
	public AcceptanceType getType() {
		return this.type;
	}
	
	public int getStateSet() {
		return this.set;
	}
	
	public boolean isNegated() {
		return this.negatedOrValue;
	}
	
	public boolean isTrue() {
		if(this.set == null) return this.negatedOrValue;
		return false;
	}
	
	public boolean isFalse() {
		if(this.set == null) return !this.negatedOrValue;
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if(this.set == null) {
			if(this.negatedOrValue) builder.append("tt");
			else builder.append("ff");
			return builder.toString();
		}else if(this.type == AcceptanceType.FIN) {
			builder.append("FIN(");
		}else {
			builder.append("INF(");
		}
		if(this.isNegated()) builder.append("!");
		builder.append(this.set + ")");
		return builder.toString();
	}

}
