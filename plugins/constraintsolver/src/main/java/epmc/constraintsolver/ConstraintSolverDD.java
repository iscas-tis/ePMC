package epmc.constraintsolver;

import java.util.LinkedHashSet;
import java.util.Set;

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.evaluatordd.ExpressionToDD;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.ValueArray;

// TODO correct class once we actually use it

/**
 * Solve constraint problems over finite variables using decision diagrams.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ConstraintSolverDD implements ConstraintSolver {
	public final static String IDENTIFIER = "dd";
	
    private boolean closed;
    private ExpressionToDD expressionToDD;
    private DD conjunction;
    private Expression objective;
    private Direction direction;
    private final Set<Feature> features = new LinkedHashSet<>();
	private ContextValue contextValue;    

	@Override
	public void requireFeature(Feature feature) {
		assert feature != null;
		features.add(feature);
	}

	@Override
	public boolean canHandle() {
		// TODO 
		return false;
	}

	@Override
	public void setContextValue(ContextValue contextValue) {
		this.contextValue = contextValue;
	}

	@Override
	public void build() throws EPMCException {
        this.conjunction = getContextDD(contextValue).newConstant(false);
	}

    
    private ContextDD getContextDD(ContextValue contextValue) throws EPMCException {
    	return ContextDD.get(contextValue);
	}

	public void addConstraint(DD constraint) throws EPMCException {
        assert !closed;
        assert constraint != null;
        conjunction = conjunction.andWith(constraint);
    }

    @Override
    public void addConstraint(Expression expression) throws EPMCException {
        DD dd = expressionToDD.translate(expression);
        addConstraint(dd);
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        conjunction.dispose();
    }

    @Override
    public void setObjective(Expression objective) {
        assert objective != null;
        this.objective = objective;
    }

    @Override
    public int addVariable(String name, Type type) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void addConstraint(ValueArray row, int[] variables,
            ConstraintType constraintType, Value rightHandSide) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addConstraint(Value[] row, int[] variables,
            ConstraintType constraintType, Value rightHandSide) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setDirection(Direction direction) {
        assert direction != null;
        this.direction = direction;
    }

    @Override
    public void setObjective(ValueArray row, int[] variables) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public ConstraintSolverResult solve() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setObjective(Value[] row, int[] variables) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Value getResultObjectiveValue() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ValueArray getResultVariablesValuesSingleType() {
        // TODO Auto-generated method stub
        return null;
    }

	@Override
	public int addVariable(String name, Type type, Value lower, Value upper) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public ContextValue getContextValue() {
		return contextValue;
	}

	@Override
	public Value[] getResultVariablesValues() {
		// TODO Auto-generated method stub
		return null;
	}

}

