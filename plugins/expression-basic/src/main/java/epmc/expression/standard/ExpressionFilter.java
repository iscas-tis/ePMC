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

import static epmc.error.UtilError.ensure;

import java.util.ArrayList;
import java.util.List;

import epmc.value.TypeAlgebra;
import epmc.value.TypeBoolean;
import epmc.value.TypeInteger;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.ValueAlgebra;
import epmc.value.ValueBoolean;
import epmc.value.ValueInterval;
import epmc.error.EPMCException;
import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.value.Type;
import epmc.value.Value;

/**
 * Expression representing a state filter.
 * The filters basically work as described in 
 * <a href="https://www.prismmodelchecker.org/manual/PropertySpecification/Filters">
 * the PRISM manual</a>.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ExpressionFilter implements Expression {
	public static boolean isFilter(Expression expression) {
		return expression instanceof ExpressionFilter;
	}
	
	public static ExpressionFilter asFilter(Expression expression) {
		if (isFilter(expression)) {
			return (ExpressionFilter) expression;
		} else {
			return null;
		}
	}
	
    public final static class Builder {
        private Positional positional;
        private FilterType type;
        private Expression prop;
        private Expression states;

        public Builder setPositional(Positional positional) {
            this.positional = positional;
            return this;
        }
        
        private Positional getPositional() {
            return positional;
        }
        
        public Builder setFilterType(FilterType type) {
            this.type = type;
            return this;
        }
        
        private FilterType getFilterType() {
            return type;
        }
        
        public Builder setProp(Expression prop) {
            this.prop = prop;
            return this;
        }
        
        private Expression getProp() {
            return prop;
        }
        
        public Builder setStates(Expression states) {
            this.states = states;
            return this;
        }
        
        private Expression getStates() {
            return states;
        }
        
        public ExpressionFilter build() {
            return new ExpressionFilter(this);
        }
    }
    
    private final Positional positional;
    private final FilterType type;
    private Expression prop;
    private Expression states;
    
    private ExpressionFilter(Builder builder) {
        assert builder != null;
        assert builder.getFilterType() != null;
        assert builder.getProp() != null;
        assert builder.getStates() != null;
        this.prop = builder.getProp();
        this.states = builder.getStates();
        this.type = builder.getFilterType();
        this.positional = builder.getPositional();
    }

    @Override
    public Expression replaceChildren(List<Expression> children) {
        assert children.size() == 2;
        return new Builder()
                .setFilterType(type)
                .setPositional(positional)
                .setProp(children.get(0))
                .setStates(children.get(1))
                .build();
    }
    
    @Override
    public Type getType(ExpressionToType expressionToType) throws EPMCException {
    	Type result = expressionToType.getType(this);
        if (result != null) {
            return result;
        }
        Type propType = prop.getType(expressionToType);
        if (TypeInteger.isIntegerWithBounds(propType)) {
            propType = TypeInteger.get();
        }
        Type statesType = states.getType(expressionToType);
        ensure(statesType == null || TypeBoolean.isBoolean(statesType),
                ProblemsExpression.EXPR_INCONSISTENT, "", this);
        switch (type) {
        case AVG:
            ensure(propType == null || TypeWeight.isWeight(propType),
            ProblemsExpression.EXPR_INCONSISTENT, "", this);
            result = TypeWeight.get();
            break;
        case SUM:
            ensure(propType == null || TypeWeight.isWeight(propType)
            || TypeInteger.isInteger(propType),
            ProblemsExpression.EXPR_INCONSISTENT, "", this);
            result = TypeWeight.get();
            break;
        case RANGE:
            ensure(propType == null || TypeReal.isReal(propType)
            	|| TypeInterval.isInterval(propType),
            ProblemsExpression.EXPR_INCONSISTENT, "", this);
            result = TypeInterval.get();
            break;
        case MAX: case MIN:
            ensure(propType == null || TypeReal.isReal(propType)
            	|| TypeInteger.isInteger(propType),
            ProblemsExpression.EXPR_INCONSISTENT, "", this);
            result = TypeReal.get();
            break;
        case COUNT:
            ensure(propType == null || TypeBoolean.isBoolean(propType),
            	ProblemsExpression.EXPR_INCONSISTENT, "", this);
            result = TypeInteger.get();
            break;
        case FIRST: case STATE: case PRINT: case PRINTALL:
            result = propType;
            break;
        case FORALL: case EXISTS:
            ensure(propType == null || TypeBoolean.isBoolean(propType),
            ProblemsExpression.EXPR_INCONSISTENT, "", this);
            result = TypeBoolean.get();
            break;
        case ARGMAX: case ARGMIN:
            ensure(propType == null || TypeReal.isReal(propType),
            ProblemsExpression.EXPR_INCONSISTENT, "", this);
            result = TypeBoolean.get();
            break;
        }
        return result;
    }
    
    public FilterType getFilterType() {
        return type;
    }

    public boolean isSingleValue() {
        return type.isSingleValue();
    }
    
    public boolean isSameResultForAllStates() {
        return type != FilterType.ARGMIN && type != FilterType.ARGMAX
                && type != FilterType.PRINT && type != FilterType.PRINTALL;
    }

    public boolean isCount() {
        return type == FilterType.COUNT;
    }
    
    public boolean isExists() {
        return type == FilterType.EXISTS;
    }
    
    public boolean isForall() {
        return type == FilterType.FORALL;
    }

    public boolean isRange() {
        return type == FilterType.RANGE;
    }
    
    public boolean isAvg() {
        return type == FilterType.AVG;
    }
    
    public boolean isState() {
        return type == FilterType.STATE;
    }

    public boolean isArgMin() {
        return type == FilterType.ARGMIN;
    }

    public boolean isArgMax() {
        return type == FilterType.ARGMAX;
    }
    
    public boolean isSum() {
        return type == FilterType.SUM;
    }
    
    public Expression getProp() {
        return prop;
    }
    
    public Expression getStates() {
        return states;
    }
    
    public Value initialAccumulatorValue(ExpressionToType expressionToType, Value value) throws EPMCException {
    	assert expressionToType != null;
        assert value != null;
        switch (type) {
        case COUNT:
            return UtilValue.clone(TypeInteger.get().getZero());
        case EXISTS:
            return UtilValue.clone(TypeBoolean.get().getFalse());
        case FORALL:
            return UtilValue.clone(TypeBoolean.get().getTrue());
        case RANGE:
            return TypeInterval.get().newValue(value, value);
        case AVG: case SUM:
            return UtilValue.clone(TypeAlgebra.asAlgebra(getType(expressionToType)).getZero());
        default:
            return UtilValue.clone(value);
        }
    }

    public boolean isPrint() {
        return type == FilterType.PRINT;
    }
    
    public boolean isPrintAll() {
        return type == FilterType.PRINTALL;
    }

    public void accumulate(Value resultValue, Value value) throws EPMCException {
        switch (type) {
        case ARGMAX: case MAX:
            ValueAlgebra.asAlgebra(resultValue).max(resultValue, value);
            break;
        case ARGMIN: case MIN:
        	ValueAlgebra.asAlgebra(resultValue).min(resultValue, value);
            break;
        case AVG:
            ValueAlgebra.asAlgebra(resultValue).add(resultValue, value);
            break;
        case COUNT:
        	ValueAlgebra.asAlgebra(resultValue).add(resultValue, ValueBoolean.asBoolean(value).getBoolean()
                    ? TypeAlgebra.asAlgebra(resultValue.getType()).getOne()
                            : TypeAlgebra.asAlgebra(resultValue.getType()).getZero());
            break;
        case EXISTS:
        	ValueBoolean.asBoolean(resultValue).or(resultValue, value);
            break;
        case FIRST:
            break;
        case FORALL:
        	ValueBoolean.asBoolean(resultValue).and(resultValue, value);
            break;
        case PRINT:
            break;
        case PRINTALL:
            break;
        case RANGE: {
            Value resLo = ValueInterval.asInterval(resultValue).getIntervalLower();
            Value resUp = ValueInterval.asInterval(resultValue).getIntervalUpper();
            ValueAlgebra.asAlgebra(resLo).min(resLo, value);
            ValueAlgebra.asAlgebra(resUp).max(resUp, value);
        }
        break;
        case STATE:
            break;
        case SUM:
        	ValueAlgebra.asAlgebra(resultValue).add(resultValue, value);
            break;
        default:
            throw new RuntimeException();
        }
    }

    @Override
    public List<Expression> getChildren() {
        List<Expression> result = new ArrayList<>();
        result.add(prop);
        result.add(states);
        return result;
    }

    @Override
    public Positional getPositional() {
        return positional;
    }
    
    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("filter(" + type + ",");
        builder.append(prop);
        if (!isTrue(states)) {
            builder.append(",");
            builder.append(states);
        }
        builder.append(")");
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
        ExpressionFilter other = ExpressionFilter.asFilter((Expression) obj);
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
        return this.type == other.type;
    }    
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        for (Expression expression : this.getChildren()) {
            assert expression != null;
            hash = expression.hashCode() + (hash << 6) + (hash << 16) - hash;
        }
        hash = type.hashCode() + (hash << 6) + (hash << 16) - hash;            
        return hash;
    }
    
    private static boolean isTrue(Expression expression) {
        assert expression != null;
        if (!ExpressionLiteral.isLiteral(expression)) {
            return false;
        }
        ExpressionLiteral expressionLiteral = ExpressionLiteral.asLiteral(expression);
        return ValueBoolean.isTrue(getValue(expressionLiteral));
    }
    
    private static Value getValue(Expression expression) {
        assert expression != null;
        assert ExpressionLiteral.isLiteral(expression);
        ExpressionLiteral expressionLiteral = ExpressionLiteral.asLiteral(expression);
        return expressionLiteral.getValue();
    }
}
