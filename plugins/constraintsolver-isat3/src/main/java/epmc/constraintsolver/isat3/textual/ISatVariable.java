package epmc.constraintsolver.isat3.textual;

import epmc.value.Type;
import epmc.value.UtilValue;
import epmc.value.Value;

final class ISatVariable {
	static class Builder {
		private String name;
		private Type type;
		private Value lower;
		private Value upper;
		
		Builder setName(String name) {
			this.name = name;
			return this;
		}
		
		String getName() {
			return name;
		}
		
		Builder setType(Type type) {
			this.type = type;
			return this;
		}
		
		Type getType() {
			return type;
		}
		
		Builder setLower(Value lower) {
			this.lower = lower == null ? null : UtilValue.clone(lower);
			if (this.lower != null) {
				this.lower.setImmutable();
			}
			return this;
		}
		
		Value getLower() {
			return lower;
		}
		
		Builder setUpper(Value upper) {
			this.upper = upper == null ? null : UtilValue.clone(upper);
			if (this.upper != null) {
				this.upper.setImmutable();
			}
			return this;
		}
		
		Value getUpper() {
			return upper;
		}
		
		ISatVariable build() {
			return new ISatVariable(this);
		}
	}
	
	private final String name;
	private final Type type;
	private final Value lower;
	private final Value upper;
	
	private ISatVariable(Builder builder) {
		assert builder != null;
		assert builder.getName() != null;
		this.name = builder.getName();
		this.type = builder.getType();
		this.lower = builder.getLower();
		this.upper = builder.getUpper();
	}
	
	String getName() {
		return name;
	}
	
	Type getType() {
		return type;
	}
	
	Value getLower() {
		return lower;
	}
	
	Value getUpper() {
		return upper;
	}
}
