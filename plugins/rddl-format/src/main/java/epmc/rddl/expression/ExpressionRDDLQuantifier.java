package epmc.rddl.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import epmc.error.EPMCException;
import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expression.standard.ExpressionLiteral;
import epmc.rddl.value.TypeRDDLObject;
import epmc.value.Operator;
import epmc.value.OperatorAdd;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.TypeInteger;
import epmc.value.Value;

public class ExpressionRDDLQuantifier implements Expression {
    private ContextExpressionRDDL contextExpressionRDDL;
    private final Operator operator;
    private final Positional positional;
    private final List<Expression> parameters;
    private final List<TypeRDDLObject> ranges = new ArrayList<>();
    private final List<TypeRDDLObject> rangesExternal = Collections.unmodifiableList(ranges);
    private final List<Expression> children = new ArrayList<>();
    private final List<Expression> childrenExternal = Collections.unmodifiableList(children);
   
    ExpressionRDDLQuantifier(ContextExpressionRDDL contextExpressionRDDL,
            Operator operator,
            List<Expression> parameters,
            List<TypeRDDLObject> ranges,
            Expression initialValue,
            Expression over,
            Positional positional) {
        assert contextExpressionRDDL != null;
        assert operator != null;
        assert parameters != null;
        assert ranges != null;
        assert parameters.size() == ranges.size();
        for (Expression parameter : parameters) {
            assert parameter != null;
            assert parameter instanceof ExpressionIdentifierStandard;
        }
        for (TypeRDDLObject range : ranges) {
            assert range != null;
        }
        assert over != null;
        // TOOD ??
        ExpressionLiteral initialValueLiteral = (ExpressionLiteral) initialValue;
        initialValueLiteral.getValue();
        this.contextExpressionRDDL = contextExpressionRDDL;
        this.operator = operator;
        this.ranges.addAll(ranges);
        this.children.add(initialValue);
        this.children.add(over);
        this.children.addAll(parameters);
        this.parameters = Collections.unmodifiableList(this.children.subList(2, this.children.size()));
        this.positional = positional;
    }

    public ContextExpressionRDDL getContextExpressionRDDL() {
        return contextExpressionRDDL;
    }
    
    public Operator getOperator() {
        return operator;
    }
    
    @Override
    public Type getType(ExpressionToType expressionToType) throws EPMCException {
    	assert expressionToType != null;
    	// TODO
    	if (expressionToType.getType(this) != null) {
    		return expressionToType.getType(this);
    	}
    	Type overType = getOver().getType(null);
    	if (TypeBoolean.isBoolean(overType)
    			&& operator.getIdentifier().equals(OperatorAdd.IDENTIFIER)) {
    		return TypeInteger.get(this.contextExpressionRDDL.getContextValue());
    	} else {
    		return overType;
    	}
    }

    @Override
    public List<Expression> getChildren() {
        return childrenExternal;
    }

    @Override
    public Positional getPositional() {
        return positional;
    }

    @Override
    public ExpressionRDDLQuantifier replaceChildren(List<Expression> newChildren) {
//    	Util.printStackTrace();
        assert newChildren != null;
        return new ExpressionRDDLQuantifier(contextExpressionRDDL,
                operator,
                newChildren.subList(2, newChildren.size()),
                ranges,
                newChildren.get(0),
                newChildren.get(1),
                positional);
    }

    public List<Expression> getParameters() {
        return parameters;
    }
    
    public List<TypeRDDLObject> getRanges() {
        return rangesExternal;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(operator.toString().toLowerCase());
        builder.append("_{");
        for (int i = 0; i < parameters.size(); i++) {
            builder.append(parameters.get(i));
            builder.append(" : ");
            builder.append(ranges.get(i));
            if (i < parameters.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append("}");
        builder.append(children.get(1));
        return builder.toString();
    }
    
    public Expression getOver() {
    	return children.get(1);
    }
    
    public Map<Expression,TypeRDDLObject> getParameterMap() {
    	// TODO would be nice to have specialised Map class handle this
    	Map<Expression,TypeRDDLObject> result = new LinkedHashMap<>();
    	for (int paramNr = 0; paramNr < getParameters().size(); paramNr++) {
    		Expression parameter = getParameters().get(paramNr);
    		TypeRDDLObject value = this.ranges.get(paramNr);
    		result.put(parameter, value);
    	}
    	return result;
    }
    
    public Value getInitialValue() {
    	// ??
    	return ((ExpressionLiteral) this.children.get(0)).getValue();
    }
    
    public ExpressionRDDLQuantifier replaceParameters(Expression over, Map<Expression,Expression> map) {
    	assert over != null;
    	assert map != null;
    	List<Expression> newChildren = new ArrayList<>();
    	newChildren.add(this.getChildren().get(0));
    	newChildren.add(over);
    	for (Expression parameter : this.getParameters()) {
    		assert map.containsKey(parameter);
    		newChildren.add(map.get(parameter));
    	}
    	return this.replaceChildren(newChildren);
    }
}
