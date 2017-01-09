package epmc.constraintsolver.smtlib;

final class SMTLibOperator {
	static class Builder {
		private String identifier;

		Builder setIdentifier(String identifier) {
			this.identifier = identifier;
			return this;
		}
		
		String getIdentifier() {
			return identifier;
		}

		SMTLibOperator build() {
			return new SMTLibOperator(this);
		}
	}

	private String identifer;
	
	private SMTLibOperator(Builder builder) {
		assert builder != null;
		assert builder.getIdentifier() != null;
		
		identifer = builder.getIdentifier();
	}
	
	String getIdentifer() {
		return identifer;
	}	
}
