package epmc.imdp.bio;

import epmc.graph.CommonProperties;
import epmc.graph.SemanticsMDP;
import epmc.graph.explicit.GraphExplicitSparseAlternate;
import epmc.graphsolver.GraphSolverExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueContentDoubleArray;

public final class GraphSolverLongRunAverageStateOnlyMDP implements GraphSolverExplicit {
    public final static String IDENTIFIER = "long-run-average-mdp";
    private GraphSolverObjectiveExplicit objective;
    private GraphExplicitSparseAlternate iterGraph;
    private ValueArrayAlgebra rewards;

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
        if (!(objective instanceof GraphSolverObjectiveSteadyStateStateOnly)) {
            return false;
        }
        if (objective.getGraph().getGraphPropertyObject(CommonProperties.SEMANTICS)
                != SemanticsMDP.MDP) {
            return false;
        }
        return true;
    }

    @Override
    public void solve() {
        if (objective.getGraph() instanceof GraphExplicitSparseAlternate) {
            this.iterGraph = (GraphExplicitSparseAlternate) objective.getGraph();
        } else {
            assert false;
        }
        GraphSolverObjectiveSteadyStateStateOnly objectiveS = (GraphSolverObjectiveSteadyStateStateOnly) objective;
        this.rewards = objectiveS.getRewards();
        // assuming unichain
        objectiveS.setResult(solveUniChain(objectiveS.isMin()));
    }

    private ValueArray solveUniChain(boolean min) {
        int numStates = iterGraph.computeNumStates();
        ValueArray presValue = UtilValue.newArray(TypeReal.get().getTypeArray(), numStates);
        ValueArray nextValue = UtilValue.newArray(TypeReal.get().getTypeArray(), numStates);
        double[] pres = ValueContentDoubleArray.getContent(presValue);
        double[] next = ValueContentDoubleArray.getContent(nextValue);
        int[] stateBounds = iterGraph.getStateBoundsJava();
        int[] nondetBounds = iterGraph.getNondetBoundsJava();
        int[] targets = iterGraph.getTargetsJava();
        double[] probs = ValueContentDoubleArray.getContent(iterGraph.getEdgePropertySparseNondet(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        double[] rew = ValueContentDoubleArray.getContent(rewards);
        double sp;
        do {
            double maxSp = Double.NEGATIVE_INFINITY;
            double minSp = Double.POSITIVE_INFINITY;
            for (int state = 0; state < numStates; state++) {
                double opt = min ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
                for (int nondet = stateBounds[state]; nondet < stateBounds[state+1]; nondet++) {
                    double current = rew[state];
                    for (int succ = nondetBounds[nondet]; succ < nondetBounds[nondet+1]; succ++) {
                        int target = targets[succ];
                        double prob = probs[succ];
                        current += prob * pres[target];
                    }
                    if (min ? current < opt : current > opt) {
                        opt = current;
                    }
                }
                next[state] = opt;
                double diff = next[state] - pres[state];
                maxSp = Math.max(maxSp, diff);
                minSp = Math.min(minSp, diff);
            }
            
            double[] swap = pres;
            pres = next;
            next = swap;
            sp = maxSp - minSp;
        } while (sp > 1E-5);
        for (int state = 0; state < numStates; state++) {
            pres[state] -= next[state];
            next[state] = pres[state];
        }
        return presValue;
    }

}
