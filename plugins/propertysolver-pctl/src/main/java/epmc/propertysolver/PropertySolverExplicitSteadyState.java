package epmc.propertysolver;

import java.util.LinkedHashSet;
import java.util.Set;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionSteadyState;
import epmc.graph.CommonProperties;
import epmc.graph.Semantics;
import epmc.graph.SemanticsContinuousTime;
import epmc.graph.SemanticsDiscreteTime;
import epmc.graph.StateMap;
import epmc.graph.StateSet;
import epmc.graph.UtilGraph;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.StateMapExplicit;
import epmc.graph.explicit.StateSetExplicit;
import epmc.graphsolver.GraphSolverConfigurationExplicit;
import epmc.graphsolver.UtilGraphSolver;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitSteadyState;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueBoolean;

public final class PropertySolverExplicitSteadyState implements PropertySolver {
    public final static String IDENTIFIER = "explicit-steady-state";
    private ModelChecker modelChecker;
    private StateSet forStates;
    private ExpressionQuantifier quantifier;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        this.modelChecker = modelChecker;
    }

    @Override
    public void setProperty(Expression property) {
        this.quantifier = null;
        ExpressionQuantifier quantifier = ExpressionQuantifier.as(property);
        if (quantifier == null) {
            return;
        }
        if (!ExpressionSteadyState.is(quantifier.getQuantified())) {
            return;
        }
        this.quantifier = quantifier;
    }

    @Override
    public void setForStates(StateSet forStates) {
        this.forStates = forStates;
    }

    @Override
    public boolean canHandle() {
        if (!(modelChecker.getEngine() instanceof EngineExplicit)) {
            return false;
        }
        Semantics semantics = modelChecker.getModel().getSemantics();
        if (!SemanticsDiscreteTime.isDiscreteTime(semantics)
                && !SemanticsContinuousTime.isContinuousTime(semantics)) {
            return false;
        }
        if (quantifier == null) {
            return false;
        }
        return true;
    }

    @Override
    public Set<Object> getRequiredGraphProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.SEMANTICS);
        ExpressionSteadyState steadyState = ExpressionSteadyState.as(quantifier.getQuantified());
        required.addAll(modelChecker.getRequiredGraphProperties(steadyState.getOperand1(), null));
        return required;
    }

    @Override
    public Set<Object> getRequiredNodeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.STATE);
        required.add(CommonProperties.PLAYER);
        ExpressionSteadyState steadyState = ExpressionSteadyState.as(quantifier.getQuantified());
        required.addAll(modelChecker.getRequiredNodeProperties(steadyState.getOperand1(), null));
        return required;
    }

    @Override
    public Set<Object> getRequiredEdgeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.WEIGHT);
        ExpressionSteadyState steadyState = ExpressionSteadyState.as(quantifier.getQuantified());
        required.addAll(modelChecker.getRequiredEdgeProperties(steadyState.getOperand1(), null));

        return required;
    }

    @Override
    public StateMap solve() {
        ExpressionSteadyState steadyState = ExpressionSteadyState.as(quantifier.getQuantified());
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        StateMapExplicit innerResult = (StateMapExplicit) modelChecker.check(steadyState.getOperand1(), allStates);
        ValueArrayAlgebra rewards = TypeWeight.get().getTypeArray().newValue();
        GraphExplicit graph = modelChecker.getLowLevel();
        rewards.setSize(graph.computeNumStates());
        Value value = innerResult.getType().newValue();
        ValueAlgebra one = UtilValue.newValue(TypeWeight.get(), 1);
        ValueAlgebra zero = UtilValue.newValue(TypeWeight.get(), 0);
        for (int stateNr = 0; stateNr < innerResult.size(); stateNr++) {
            int state = innerResult.getExplicitIthState(stateNr);
            innerResult.getExplicitIthValue(value, stateNr);
            if (ValueBoolean.is(value)) {
                rewards.set(ValueBoolean.as(value).getBoolean() ? one : zero, state);   
            } else {
                rewards.set(value, state);                   
            }
        }
        GraphSolverObjectiveExplicitSteadyState objective = new GraphSolverObjectiveExplicitSteadyState();
        objective.setGraph(graph);
        objective.setComputeFor(((StateSetExplicit) forStates).getStatesExplicit());
        objective.setStateRewards(rewards);
        objective.setMin(quantifier.isDirMin());
        GraphSolverConfigurationExplicit configuration = UtilGraphSolver.newGraphSolverConfigurationExplicit();
        configuration.setObjective(objective);
        configuration.solve();
        return new StateMapExplicit((StateSetExplicit) forStates.clone(), objective.getResult());
    }

}
