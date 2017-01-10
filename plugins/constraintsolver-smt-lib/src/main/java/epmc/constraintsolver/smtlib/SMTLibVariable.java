/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

*****************************************************************************/

package epmc.constraintsolver.smtlib;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.UtilValue;
import epmc.value.Value;

final class SMTLibVariable {
	
	public ContextValue getContextValue() {
		return contextValue;
	}
	
	static class Builder {
		private ContextValue contextValue;
		private String name;
		private Type type;
		private Value lower;
		private Value upper;
		
		Builder setContextValue(ContextValue contextValue) {
			this.contextValue = contextValue;
			return this;
		}
		
		ContextValue getContextValue() {
			return contextValue;
		}
		
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
		
		SMTLibVariable build() {
			return new SMTLibVariable(this);
		}
	}
	
	private final String name;
	private final Type type;
	private final Value lower;
	private final Value upper;
	private final Expression identifer;
	private final ContextValue contextValue;
	
	private SMTLibVariable(Builder builder) {
		assert builder != null;
		assert builder.getName() != null;
		assert builder.getContextValue() != null;
		this.name = builder.getName();
		this.type = builder.getType();
		this.lower = builder.getLower();
		this.upper = builder.getUpper();
		this.contextValue =  builder.getContextValue();
		this.identifer = new ExpressionIdentifierStandard.Builder()
				.setName(name)
				.build();
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
	
	Expression getIdentifer() {
		return identifer;
	}
}
