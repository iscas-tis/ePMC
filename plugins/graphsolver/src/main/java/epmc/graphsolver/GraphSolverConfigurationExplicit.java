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

package epmc.graphsolver;

import static epmc.error.UtilError.ensure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import epmc.graphsolver.lumping.LumperExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.graphsolver.preprocessor.PreprocessorExplicit;
import epmc.options.Options;
import epmc.util.Util;

public final class GraphSolverConfigurationExplicit {
    private GraphSolverObjectiveExplicit objective;
    private GraphSolverExplicit graphSolver;
    private boolean lumpBeforeGraphSolving;

    GraphSolverConfigurationExplicit() {
        this.lumpBeforeGraphSolving = Options.get().getBoolean(OptionsGraphsolver.GRAPHSOLVER_LUMP_BEFORE_GRAPH_SOLVING);
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

    public void solve() {
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
        Map<String,Class<? extends LumperExplicit>> lumpersExplicit = Options.get().get(OptionsGraphsolver.GRAPHSOLVER_LUMPER_EXPLICIT_CLASS);
        Collection<String> lumperExplicitt = Options.get().get(OptionsGraphsolver.GRAPHSOLVER_LUMPER_EXPLICIT);
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

    private void doSolve(GraphSolverObjectiveExplicit objective) {
        Collection<String> solvers = Options.get().get(OptionsGraphsolver.GRAPHSOLVER_SOLVER);
        Map<String,Class<GraphSolverExplicit>> solverClasses = Options.get().get(OptionsGraphsolver.GRAPHSOLVER_SOLVER_CLASS);
        graphSolver = Util.getInstance(solverClasses, solvers,
                e -> { e.setGraphSolverObjective(objective);
                return e.canHandle();
                });
        ensure(graphSolver != null, ProblemsGraphsolver.GRAPHSOLVER_NO_SOLVER_AVAILABLE);
        graphSolver.solve();
    }

    private void preprocess() {
        Map<String,Class<? extends PreprocessorExplicit>> preprocessors = Options.get().get(OptionsGraphsolver.GRAPHSOLVER_PREPROCESSOR_EXPLICIT_CLASS);
        Collection<String> preprocessorsC = Options.get().get(OptionsGraphsolver.GRAPHSOLVER_PREPROCESSOR_EXPLICIT);
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
