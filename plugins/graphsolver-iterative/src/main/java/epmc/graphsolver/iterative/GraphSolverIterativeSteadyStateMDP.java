package epmc.graphsolver.iterative;

import epmc.algorithms.explicit.ComponentsExplicit;
import epmc.algorithms.explicit.EndComponents;
import epmc.error.EPMCException;
import epmc.graph.CommonProperties;
import epmc.graph.Semantics;
import epmc.graph.SemanticsMDP;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitSparseAlternate;
import epmc.graphsolver.GraphSolverExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitSteadyState;
import epmc.util.BitSet;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueObject;

public final class GraphSolverIterativeSteadyStateMDP implements GraphSolverExplicit {
	public final static String IDENTIFIER = "graph-solver-iterative-steady-state-mdp";
	private GraphSolverObjectiveExplicit objective;

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void setGraphSolverObjective(GraphSolverObjectiveExplicit objective) {
		this.objective = objective;
	}

	@Override
	public boolean canHandle() {
        Semantics semantics = ValueObject.asObject(objective.getGraph().getGraphProperty(CommonProperties.SEMANTICS)).getObject();
        if (!SemanticsMDP.isMDP(semantics)) {
        	return false;
        }
		if (!(objective instanceof GraphSolverObjectiveExplicitSteadyState)) {
			return false;
		}
		return true;
	}

	@Override
	public void solve() throws EPMCException {
		GraphSolverObjectiveExplicitSteadyState objectiveSteadyState = (GraphSolverObjectiveExplicitSteadyState) objective;
		GraphExplicit graph = objectiveSteadyState.getGraph();
		EndComponents components = new ComponentsExplicit().endComponents(graph, true);
		ValueArrayAlgebra stateRewards = objectiveSteadyState.getStateRewards();
		int numStates = graph.computeNumStates();
		ValueArrayAlgebra result = UtilValue.newArray(TypeWeight.get().getTypeArray(), numStates);
		for (BitSet component = components.next(); component != null; component = components.next()) {
			solveComponent(result, (GraphExplicitSparseAlternate) graph, stateRewards, component);
			
		}
		// TODO Auto-generated method stub
		
	}

	private void solveComponent(ValueArrayAlgebra result, GraphExplicitSparseAlternate graph, ValueArrayAlgebra stateRewards, BitSet component) throws EPMCException {
		assert result != null;
		assert graph != null;
		assert stateRewards != null;
		assert component != null;
        TypeWeight typeWeight = TypeWeight.get();
		ValueAlgebra spread = typeWeight.newValue();
		ValueArrayAlgebra pres = UtilValue.newArray(TypeWeight.get().getTypeArray(), graph.getNumNodes());
		ValueArrayAlgebra next = UtilValue.newArray(TypeWeight.get().getTypeArray(), graph.getNumNodes());
		ValueAlgebra zero = TypeWeight.get().getZero();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        ValueAlgebra nextStateProb = typeWeight.newValue();
		int numStates = graph.computeNumStates();
        Value optInitValue = min ? typeWeight.getPosInf() : typeWeight.getNegInf();
		for (int state = 0; state < numStates; state++) {
			if (!component.get(state)) {
				continue;
			}
			result.set(zero, state);
			pres.set(zero, state);
			next.set(zero, state);
		}
		for (int state = 0; state < numStates; state++) {
			if (!component.get(state)) {
				continue;
			}
            int stateFrom = stateBounds[state];
            int stateTo = stateBounds[state + 1];
            nextStateProb.set(optInitValue);
            for (int nondetNr = stateFrom; nondetNr < stateTo; nondetNr++) {
                int nondetFrom = nondetBounds[nondetNr];
                int nondetTo = nondetBounds[nondetNr + 1];
                choiceNextStateProb.set(zero);
                for (int stateSucc = nondetFrom; stateSucc < nondetTo; stateSucc++) {
                    weights.get(weight, stateSucc);
                    int succState = targets[stateSucc];
                    presValues.get(succStateProb, succState);
                    weighted.multiply(weight, succStateProb);
                    choiceNextStateProb.add(choiceNextStateProb, weighted);
                }
                if (min) {
                    nextStateProb.min(nextStateProb, choiceNextStateProb);
                } else {
                    nextStateProb.max(nextStateProb, choiceNextStateProb);
                }
		}
		// TODO Auto-generated method stub
		
	}

}
