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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import epmc.dd.DD;
import epmc.graph.CommonProperties;
import epmc.graph.GraphBuilderDD;
import epmc.graph.Semantics;
import epmc.graph.SemanticsNonDet;
import epmc.graph.dd.GraphDD;
import epmc.graph.explicit.GraphExplicit;
import epmc.graphsolver.lumping.LumperDD;
import epmc.graphsolver.objective.GraphSolverObjectiveDD;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitUnboundedReachability;
import epmc.options.Options;
import epmc.util.BitSet;
import epmc.util.Util;

public final class GraphSolverConfigurationDD {
    private GraphSolverObjectiveDD objective;
    private GraphDD graphDD;
    private DD targetDD;
    private List<DD> sinksDD;
    private GraphBuilderDD graphBuilderDD;
    private DD resultDD;
    private boolean lumpBeforeGraphSolving;
    private LumperDD lumperDD;
    private GraphSolverConfigurationExplicit configuration;

    public void setGraph(GraphDD graph) {
        assert graph != null;
        this.graphDD = graph;
        this.lumpBeforeGraphSolving = Options.get().getBoolean(OptionsGraphsolver.GRAPHSOLVER_LUMP_BEFORE_GRAPH_SOLVING);
    }

    // TODO subsume objective, min, and time to one new parameter type

    public void setObjective(GraphSolverObjectiveDD objective) {
        this.objective = objective;
    }

    public void solve() {
        GraphSolverObjectiveExplicit explicitObjective = preprocessDD();
        configuration = UtilGraphSolver.newGraphSolverConfigurationExplicit();
        configuration.setObjective(explicitObjective);
        configuration.solve();
        postprocessDD(explicitObjective);
    }

    private GraphSolverObjectiveExplicit preprocessDD() {
        if (lumpBeforeGraphSolving) {
            lumpDD();
        }
        Semantics semantics = graphDD.getGraphPropertyObject(CommonProperties.SEMANTICS);
        this.graphBuilderDD = new GraphBuilderDD(graphDD, sinksDD, SemanticsNonDet.isNonDet(semantics));
        GraphExplicit graphExplicit = graphBuilderDD.buildGraph();
        BitSet target = graphBuilderDD.ddToBitSet(targetDD);
        GraphSolverObjectiveExplicitUnboundedReachability objectiveUnboundedReachability = new GraphSolverObjectiveExplicitUnboundedReachability();
        objectiveUnboundedReachability.setGraph(graphExplicit);
        objectiveUnboundedReachability.setTarget(target);
        return objectiveUnboundedReachability;
    }

    private void postprocessDD(GraphSolverObjectiveExplicit explicitObjective) {
        this.resultDD = graphBuilderDD.valuesToDD(explicitObjective.getResult());
        graphBuilderDD.close();
        this.resultDD = this.resultDD.multiplyWith(graphDD.getNodeSpace().toMT());
        this.resultDD = this.resultDD.addWith(targetDD.andNot(graphDD.getNodeSpace()).toMTWith());
        if (this.lumpBeforeGraphSolving) {
            this.resultDD = lumperDD.quotientToOriginal(resultDD);
        }
        graphBuilderDD.close();
    }

    private void lumpDD() {
        for (DD sink : sinksDD) {
            graphDD.registerNodeProperty(sink, sink);
        }

        Options options = Options.get();
        Collection<String> lumpers = options.get(OptionsGraphsolver.GRAPHSOLVER_LUMPER_DD);
        Map<String,Class<? extends LumperDD>> lumpersDD = options.get(OptionsGraphsolver.GRAPHSOLVER_DD_LUMPER_CLASS);
        for (String lumperId : lumpers) {
            Class<? extends LumperDD> lumperClass = lumpersDD.get(lumperId);
            if (lumperClass == null) {
                continue;
            }
            LumperDD lumper = Util.getInstance(lumperClass);
            lumper.setOriginal(graphDD);
            if (lumper.canLump()) {
                this.lumperDD = lumper;
                break;
            }
        }
        if (lumperDD == null) {
            // No suitable lumper was found
            return;
        }

        lumperDD.lump();
        graphDD = lumperDD.getQuotient();
        targetDD = lumperDD.originalToQuotient(targetDD);
        List<DD> quotientSinks = new ArrayList<>();
        for(int i = 0; i < sinksDD.size(); i++) {
            quotientSinks.add(lumperDD.originalToQuotient(sinksDD.get(i)));
        }
        sinksDD = quotientSinks;
    }

    public GraphSolverObjectiveDD getObjective() {
        return objective;
    }

    public GraphDD getGraphDD() {
        return graphDD;
    }

    public void setTargetStates(DD target) {
        this.targetDD = target;
    }

    public DD getTargetStatesDD() {
        return this.targetDD;
    }

    public void setSinkStatesDD(List<DD> sinks) {
        assert sinks != null;
        for (DD sink : sinks) {
            assert sink != null;
            assert sink.alive();
        }
        this.sinksDD = sinks;
    }

    public List<DD> getSinksDD() {
        return sinksDD;
    }

    public DD getOutputValuesDD() {
        return resultDD;
    }
}
