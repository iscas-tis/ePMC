package epmc.rddl.expression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import epmc.error.EPMCException;
import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.value.Type;

public class ExpressionRDDLQuantifiedIdentifier implements Expression {
    private ContextExpressionRDDL contextExpressionRDDL;
    private final Positional positional;
    private final List<Expression> parameters;
    private final List<Expression> children = new ArrayList<>();
    private final List<Expression> childrenExternal = Collections.unmodifiableList(children);
   
    ExpressionRDDLQuantifiedIdentifier(ContextExpressionRDDL contextExpressionRDDL,
            Expression name,
            List<Expression> parameters,
            Positional positional) {
        assert contextExpressionRDDL != null;
        assert parameters != null;
        for (Expression parameter : parameters) {
            assert parameter != null;
        }
        assert name instanceof ExpressionIdentifierStandard;
        this.contextExpressionRDDL = contextExpressionRDDL;
        this.children.add(name);
        this.children.addAll(parameters);
        this.parameters = Collections.unmodifiableList(this.children.subList(1, this.children.size()));
        this.positional = positional;
    }

    ExpressionRDDLQuantifiedIdentifier(ContextExpressionRDDL contextExpressionRDDL,
            List<Expression> children,
            Positional positional) {
        assert contextExpressionRDDL != null;
        this.contextExpressionRDDL = contextExpressionRDDL;
        assert children.get(0) instanceof ExpressionIdentifierStandard : " " + children.get(0);
        this.children.addAll(children);
        this.parameters = Collections.unmodifiableList(this.children.subList(1, this.children.size()));
        this.positional = positional;
    }

    public ContextExpressionRDDL getContextExpressionRDDL() {
        return contextExpressionRDDL;
    }
    
	@Override
	public Type getType(ExpressionToType expressionToType) throws EPMCException {
    	Type type;
    	type = expressionToType.getType(this);
    	if (type == null) {
    		// TODO
    		type = children.get(0).getType(null);
    	}
    	return type;
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
    public ExpressionRDDLQuantifiedIdentifier replaceChildren(List<Expression> newChildren) {
    	assert newChildren.get(0) instanceof ExpressionIdentifierStandard : this;
    	return new ExpressionRDDLQuantifiedIdentifier(this.contextExpressionRDDL,
                newChildren, positional);
    }

    public List<Expression> getParameters() {
        return parameters;
    }    
    
    public String getName() {
        return ((ExpressionIdentifierStandard) children.get(0)).getName();
    }
    
    public Expression getIdentifier() {
    	return children.get(0);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(children.get(0));
        if (parameters.size() > 0) {
            builder.append("(");
            for (int i = 0; i < parameters.size(); i++) {
                builder.append(parameters.get(i));
                if (i < parameters.size() - 1) {
                    builder.append(",");
                }
            }
            builder.append(")");
        }
        return builder.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
    	assert obj != null;
    	if (!(obj instanceof ExpressionRDDLQuantifiedIdentifier)) {
    		return false;
    	}
    	ExpressionRDDLQuantifiedIdentifier other = (ExpressionRDDLQuantifiedIdentifier) obj;
    	if (!this.children.equals(other.children)) {
    		return false;
    	}
    	return true;
    }
    
    @Override
    public int hashCode() {
    	return this.children.hashCode();
    }
    
    public ExpressionRDDLQuantifiedIdentifier replaceParameters(Map<Expression,Expression> map) {
    	assert map != null;
    	List<Expression> newChildren = new ArrayList<>();
    	newChildren.add(this.getIdentifier());
    	for (Expression parameter : this.getParameters()) {
    		assert map.containsKey(parameter) : parameter + " " + map + " " + this;
    		newChildren.add(map.get(parameter));
    	}
    	return this.replaceChildren(newChildren);
    }

}
