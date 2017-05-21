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

package epmc.expression.standard;

import java.util.Collections;
import java.util.List;

import epmc.error.EPMCException;
import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;

/**
 * A literal, such as booleans, numeric values, etc. Basically a container for
 * Value objects.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ExpressionLiteral implements ExpressionPropositional {
	@FunctionalInterface
	public interface ValueProvider {
		Value provideValue() throws EPMCException;
	}
	
	public static boolean isLiteral(Expression expression) {
		return expression instanceof ExpressionLiteral;
	}
	
	public static ExpressionLiteral asLiteral(Expression expression) {
		if (isLiteral(expression)) {
			return (ExpressionLiteral) expression;
		} else {
			return null;
		}
	}
	
    public final static class Builder {
        private Value value;
		private ValueProvider valueProvider;
        private Positional positional;

        public Builder setValue(Value value) {
            this.value = value;
            return this;
        }
        
        private Value getValue() {
            return value;
        }
        
        public Builder setValueProvider(ValueProvider valueProvider) {
        	assert valueProvider != null;
        	this.valueProvider = valueProvider;
        	return this;
        }
        
        private ValueProvider getValueProvider() {
        	return valueProvider;
        }
        
        public Builder setPositional(Positional positional) {
            this.positional = positional;
            return this;
        }
        
        private Positional getPositional() {
            return positional;
        }
        
        public ExpressionLiteral build() {
            return new ExpressionLiteral(this);
        }
    }
    
    private final Value value;
	private final ValueProvider valueProvider;
    private final Positional positional;

    private ExpressionLiteral(Builder builder) {
        assert builder != null;
        assert (builder.getValue() != null) != (builder.getValueProvider() != null);
        this.positional = builder.getPositional();
        if (builder.getValue() != null) {
        	this.valueProvider = null;
            this.value = UtilValue.clone(builder.getValue());
            this.value.setImmutable();
        } else if (builder.getValueProvider() != null) {
        	this.valueProvider = builder.getValueProvider();
        	this.value = null;
        } else {
            throw new RuntimeException();
        }
    }

    public Value getValue() throws EPMCException {
    	if (value != null) {
    		return value;
    	} else if (valueProvider != null) {
    		Value result = valueProvider.provideValue();
    		result.setImmutable();
    		return result;
    	} else {
    		throw new RuntimeException();
    	}
    }
    
    @Override
    public Expression replaceChildren(List<Expression> children) {
        assert children != null;
        assert children.size() == 0;
        return this;
    }

    @Override
    public Type getType(ExpressionToType expressionToType) throws EPMCException {
    	assert expressionToType != null;
    	if (value != null) {
    		return value.getType();
    	} else if (valueProvider != null) {
    		return valueProvider.provideValue().getType();
    	} else {
    		throw new RuntimeException();
    	}
    }
    
    @Override
    public boolean isPropositional() {
        return true;
    }
        
    @Override
    public List<Expression> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public Positional getPositional() {
        return positional;
    }    
    
    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        if (value != null) {
        	builder.append(value);
        } else if (valueProvider != null) {
        	builder.append(valueProvider);
        	builder.append(":");
        	try {
				builder.append(valueProvider.provideValue());
			} catch (EPMCException e) {
				throw new RuntimeException(e);
			}
        } else {
        	throw new RuntimeException();
        }
        if (getPositional() != null) {
            builder.append(" (" + getPositional() + ")");
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        ExpressionLiteral other = (ExpressionLiteral) obj;
        List<Expression> thisChildren = this.getChildren();
        List<Expression> otherChildren = other.getChildren();
        if (thisChildren.size() != otherChildren.size()) {
            return false;
        }
        for (int entry = 0; entry < thisChildren.size(); entry++) {
            if (!thisChildren.get(entry).equals(otherChildren.get(entry))) {
                return false;
            }
        }
        if ((this.value != null) != (other.value != null)) {
        	return false;
        }
        if (value != null && !this.value.equals(other.value)) {
        	return false;
        }
        if (valueProvider != null &&
        		!this.valueProvider.equals(other.valueProvider)) {
        	return false;
        }
        return true;
    }    
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        for (Expression expression : this.getChildren()) {
            assert expression != null;
            hash = expression.hashCode() + (hash << 6) + (hash << 16) - hash;
        }
        if (value != null) {
        	hash = value.hashCode() + (hash << 6) + (hash << 16) - hash;
        } else if (valueProvider != null) {
        	hash = valueProvider.hashCode() + (hash << 6) + (hash << 16) - hash;        	
        } else {
        	throw new RuntimeException();
        }
        return hash;
    }

    /**
     * Obtain expression representing positive infinity.
     * 
     * @return expression representing positive infinity
     */
    public static ExpressionLiteral getPosInf() {
        ExpressionLiteral posInfExpr = new Builder()
                .setValueProvider(() -> TypeReal.get().getPosInf())
                .build();
        return posInfExpr;
    }

    /**
     * Obtain expression representing the value &quot;1&quot;
     * 
     * @return expression representing the value &quot;1&quot;
     */
    public static Expression getOne() {
        ExpressionLiteral oneExpr = new Builder()
                .setValueProvider(() -> TypeInteger.get().getOne())
                .build();
        return oneExpr;
    }

    /**
     * Obtain expression representing the value &quot;0&quot;
     * 
     * @return expression representing the value &quot;0&quot;
     */
    public static ExpressionLiteral getZero() {
        ExpressionLiteral zeroExpr = new Builder()
                .setValueProvider(() -> TypeInteger.get().getZero())
                .build();
        return zeroExpr;
    }

    /**
     * Obtain expression representing the value &quot;true&quot;
     * 
     * @return expression representing the value &quot;true&quot;
     */
    public static ExpressionLiteral getTrue() {
        ExpressionLiteral trueExpr = new Builder()
                .setValueProvider(() -> TypeBoolean.get().getTrue())
                .build();
        return trueExpr;
    }

    /**
     * Obtain expression representing the value &quot;false&quot;
     * 
     * @return expression representing the value &quot;false&quot;
     */
    public static Expression getFalse() {
        ExpressionLiteral falseExpr = new Builder()
                .setValueProvider(() -> TypeBoolean.get().getFalse())
                .build();
        return falseExpr;
    }

	@Override
	public Expression replacePositional(Positional positional) {
		Builder builder = new ExpressionLiteral.Builder();
		if (value != null) {
			builder.setValue(value);
		} else if (valueProvider != null) {
			builder.setValueProvider(valueProvider);
		} else {
			assert false;
		}
		builder.setPositional(positional);
		return builder.build();
	}
}
