package epmc.graphsolver;

import static epmc.error.UtilError.ensure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import epmc.error.EPMCException;
import epmc.graphsolver.lumping.LumperExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.graphsolver.preprocessor.PreprocessorExplicit;
import epmc.options.Options;
import epmc.util.Util;

public final class GraphSolverConfigurationExplicit {
    private final Options options;
    private GraphSolverObjectiveExplicit objective;
    private GraphSolverExplicit graphSolver;
	private boolean lumpBeforeGraphSolving;

    GraphSolverConfigurationExplicit(Options options) {
        assert options != null;
        this.options = options;
        this.lumpBeforeGraphSolving = options.getBoolean(OptionsGraphsolver.GRAPHSOLVER_LUMP_BEFORE_GRAPH_SOLVING);
    }
    
    public void setLumpBeforeGraphSolving(boolean lumpBeforeGraphSolving) {
		this.lumpBeforeGraphSolving = lumpBeforeGraphSolving;
	}
    
    public boolean isLumpBeforeGraphSolving() {
		return lumpBeforeGraphSolving;
	}
    
    public void setObjective(GraphSolverObjectiveExplicit objective) {
        this.objective = objective;
    }

    public void solve() throws EPMCException {
        preprocess();
        LumperExplicit lumper = getLumperExplicit();
        GraphSolverObjectiveExplicit objective;
        if (lumper != null) {
        	lumper.lump();
        	objective = lumper.getQuotient();
        } else {
        	objective = this.objective;
        }
        doSolve(objective);
        if (lumper != null) {
        	lumper.quotientToOriginal();
        }
    }

    private LumperExplicit getLumperExplicit() {
    	if (!lumpBeforeGraphSolving) {
    		return null;
    	}
        Map<String,Class<? extends LumperExplicit>> lumpersExplicit = options.get(OptionsGraphsolver.GRAPHSOLVER_LUMPER_EXPLICIT_CLASS);
        Collection<String> lumperExplicitt = options.get(OptionsGraphsolver.GRAPHSOLVER_LUMPER_EXPLICIT);
        ArrayList<String> lumperExplicit = new ArrayList<>(lumperExplicitt);
        for (String lumperId : lumperExplicit) {
            Class<? extends LumperExplicit> lumperClass = lumpersExplicit.get(lumperId);
            if (lumperClass == null) {
                continue;
            }
            LumperExplicit lumper = Util.getInstance(lumperClass);
            lumper.setOriginal(objective);
            if (lumper.canLump()) {
                return lumper;
            }
        }
        return null;
    }

    private void doSolve(GraphSolverObjectiveExplicit objective) throws EPMCException {
        Collection<String> solvers = options.get(OptionsGraphsolver.GRAPHSOLVER_SOLVER);
        Map<String,Class<GraphSolverExplicit>> solverClasses = options.get(OptionsGraphsolver.GRAPHSOLVER_SOLVER_CLASS);
        graphSolver = Util.getInstance(solverClasses, solvers,
                e -> {e.setGraphSolverObjective(objective);
                return e.canHandle();
                });
        ensure(graphSolver != null, ProblemsGraphsolver.GRAPHSOLVER_NO_SOLVER_AVAILABLE);
        graphSolver.solve();
    }

    private void preprocess() throws EPMCException {
        Map<String,Class<? extends PreprocessorExplicit>> preprocessors = options.get(OptionsGraphsolver.GRAPHSOLVER_PREPROCESSOR_EXPLICIT_CLASS);
        Collection<String> preprocessorsC = options.get(OptionsGraphsolver.GRAPHSOLVER_PREPROCESSOR_EXPLICIT);
        for (String lumperId : preprocessorsC) {
            Class<? extends PreprocessorExplicit> preprocessorClass = preprocessors.get(lumperId);
            if (preprocessorClass == null) {
                continue;
            }
            PreprocessorExplicit preprocessor = Util.getInstance(preprocessorClass);
            preprocessor.setObjective(objective);
            if (preprocessor.canHandle()) {
            	preprocessor.process();
            	return;
            }
        }
    }

    public GraphSolverObjectiveExplicit getObjective() {
        return objective;
    }
}
