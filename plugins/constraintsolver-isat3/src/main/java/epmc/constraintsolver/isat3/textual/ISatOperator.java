package epmc.constraintsolver.isat3.textual;

final class ISatOperator {
	enum Type {
		INFIX,
		PREFIX;
		
		boolean isPrefix() {
			return this == PREFIX;
		}
		
		boolean isInfix() {
			return this == INFIX;
		}
	}
	
	static class Builder {
		private String identifier;
		private Type type;

		Builder setIdentifier(String identifier) {
			this.identifier = identifier;
			return this;
		}
		
		String getIdentifier() {
			return identifier;
		}

		Builder setType(Type type) {
			this.type = type;
			return this;
		}
		
		Type getType() {
			return type;
		}
		
		
		ISatOperator build() {
			return new ISatOperator(this);
		}
	}

	private String identifer;
	private Type type;
	
	private ISatOperator(Builder builder) {
		assert builder != null;
		assert builder.getIdentifier() != null;
		assert builder.getType() != null;
		
		identifer = builder.getIdentifier();
		type = builder.getType();
	}
	
	String getIdentifer() {
		return identifer;
	}
	
	Type getType() {
		return type;
	}
	
	boolean isInfix() {
		return type.isInfix();
	}
	
	boolean isPrefix() {
		return type.isPrefix();
	}
}
