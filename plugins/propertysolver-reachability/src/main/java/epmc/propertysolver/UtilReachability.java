package epmc.propertysolver;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionPropositional;
import epmc.expression.standard.ExpressionTemporal;
import epmc.expression.standard.TemporalType;
import epmc.graph.explicit.GraphExplicit;
import epmc.graphsolver.GraphSolverConfigurationExplicit;
import epmc.graphsolver.UtilGraphSolver;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitUnboundedReachability;
import epmc.util.BitSet;
import epmc.value.ValueArrayAlgebra;

// utility class for computing reachability
public final class UtilReachability {
	
	/** collect the atomic propositions in the given formula which will be later used in
	    building the graph of the model. */
	public static Set<Expression> collectReachabilityInner(Expression expression) {
		if (expression instanceof ExpressionTemporal) {
			ExpressionTemporal expressionTemporal = (ExpressionTemporal) expression;
			Set<Expression> result = new LinkedHashSet<>();
			for (Expression inner : expressionTemporal.getOperands()) {
				result.addAll(collectReachabilityInner(inner));
			}
			return result;
		} else {
			return Collections.singleton(expression);			
		}
	}
	
	/** Since we only consider the reachability here,
	the formula we consider is in form F a */
    public static boolean isReachability(Expression pathProp) {
        if (!(pathProp instanceof ExpressionTemporal)) {
            return false;
        }
        
        ExpressionTemporal asQuantifier = (ExpressionTemporal) pathProp;
        if(asQuantifier.getTemporalType() != TemporalType.FINALLY) return false;
        // check whether operand is a propositional formula
        if(!ExpressionPropositional.isPropositional(asQuantifier.getOperand1())) return false;
        
        return true;
    }
    
    // solve the linear equation system
    public static ValueArrayAlgebra computeReachabilityProbability(
    		GraphExplicit graph, BitSet oneStates) throws EPMCException {
    	
        GraphSolverConfigurationExplicit configuration = UtilGraphSolver.newGraphSolverConfigurationExplicit(graph.getOptions());
        GraphSolverObjectiveExplicitUnboundedReachability objective = new GraphSolverObjectiveExplicitUnboundedReachability();
        objective.setMin(false);
        objective.setGraph(graph);
        objective.setTarget(oneStates);
        //objective.setZeroSink(zeroStates);
        configuration.setObjective(objective);
        configuration.solve();
        
        return objective.getResult();
 
    }
    
	private UtilReachability() {
	}
}
