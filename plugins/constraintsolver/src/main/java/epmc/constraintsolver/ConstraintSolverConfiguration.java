package epmc.constraintsolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import epmc.constraintsolver.options.OptionsConstraintsolver;
import epmc.dd.ContextDD;
import epmc.error.EPMCException;
import epmc.expression.standard.evaluatordd.ExpressionToDD;
import epmc.options.Options;
import epmc.util.Util;
import epmc.value.ContextValue;

public class ConstraintSolverConfiguration {
    private final Options options;
    private final ContextValue contextValue;
    private final ExpressionToDD expressionToDD = null;
    private final Set<Feature> features = new LinkedHashSet<>();

    public ConstraintSolverConfiguration(ContextValue contextValue) {
    	assert contextValue != null;
        this.options = contextValue.getOptions();
        this.contextValue = contextValue;
        assert contextValue != null;
//        this.expressionToDD = options.get(OptionsEPMC.EXPRESSION_TO_DD);
    }
    
    public void requireFeature(Feature feature) {
    	assert feature != null;
    	features.add(feature);
    }
    
    public ConstraintSolver newProblem()
            throws EPMCException {
    	return buildSolver(features);
    }
    
    private ConstraintSolver buildSolver(Set<Feature> features) throws EPMCException {
    	Options options = contextValue.getOptions();
        Map<String,Class<? extends ConstraintSolver>> lumpersExplicit = options.get(OptionsConstraintsolver.CONSTRAINTSOLVER_SOLVER_CLASS);
        Collection<String> lumperExplicitt = options.get(OptionsConstraintsolver.CONSTRAINTSOLVER_SOLVER);
        ArrayList<String> lumperExplicit = new ArrayList<>(lumperExplicitt);
        for (String lumperId : lumperExplicit) {
            Class<? extends ConstraintSolver> solverClass = lumpersExplicit.get(lumperId);
            if (solverClass == null) {
                continue;
            }
            ConstraintSolver solver = Util.getInstance(solverClass);
            solver.setContextValue(contextValue);
			for (Feature feature : features) {
	            solver.requireFeature(feature);
            }
            if (solver.canHandle()) {
            	solver.build();
                return solver;
            }
        }
        return null;
    }

    public Options getOptions() {
        return options;
    }

    public ContextValue getContextValue() {
        return contextValue;
    }
    
    public ContextDD getContextDD() throws EPMCException {
        return ContextDD.get(contextValue);
    }
    
    public ExpressionToDD getExpressionToDD() {
        return expressionToDD;
    }
}
