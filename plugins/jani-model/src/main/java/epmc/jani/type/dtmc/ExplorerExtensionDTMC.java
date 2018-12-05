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

package epmc.jani.type.dtmc;

import epmc.graph.CommonProperties;
import epmc.graph.Player;
import epmc.graph.explorer.ExplorerEdgeProperty;
import epmc.graph.explorer.ExplorerNodeProperty;
import epmc.jani.explorer.ExplorerComponent;
import epmc.jani.explorer.ExplorerComponentAutomaton;
import epmc.jani.explorer.ExplorerExtension;
import epmc.jani.explorer.ExplorerJANI;
import epmc.jani.explorer.NodeJANI;
import epmc.jani.explorer.PropertyEdgeGeneral;
import epmc.jani.explorer.PropertyNodeGeneral;
import epmc.jani.explorer.UtilExplorer;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorDivide;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeBoolean;
import epmc.value.TypeEnum;
import epmc.value.TypeWeightTransition;
import epmc.value.UtilValue;
import epmc.value.ValueAlgebra;
import epmc.value.ValueBoolean;

public final class ExplorerExtensionDTMC implements ExplorerExtension {
    public final static String IDENTIFIER = "dtmc";
    private ExplorerJANI explorer;
    private ExplorerComponent system;
    private PropertyNodeGeneral player;
    private PropertyEdgeGeneral systemWeight;
    private NodeJANI[] noNondetHelperNode;
    private ValueAlgebra dtmcSum;
    private ValueAlgebra dtmcAligned;
    private ValueAlgebra zero;
    private ValueAlgebra one;
    private OperatorEvaluator divide;
    private OperatorEvaluator eq;
    private OperatorEvaluator add;
    private OperatorEvaluator set;
    private ValueBoolean cmp;
    
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setExplorer(ExplorerJANI explorer) {
        assert this.explorer == null;
        assert explorer != null;
        this.explorer = explorer;
    }
    
    @Override
    public void afterSystemCreation() {
        this.system = explorer.getExplorerSystem();
        player = new PropertyNodeGeneral(explorer, TypeEnum.get(Player.class));
        player.set(Player.STOCHASTIC);
        noNondetHelperNode = new NodeJANI[1];
        noNondetHelperNode[0] = system.newNode();
        systemWeight = (PropertyEdgeGeneral) system.getEdgeProperty(CommonProperties.WEIGHT);
        dtmcSum = TypeWeightTransition.get().newValue();
        dtmcAligned = TypeWeightTransition.get().newValue();
        zero = UtilValue.newValue(TypeWeightTransition.get(), 0);
        one = UtilValue.newValue(TypeWeightTransition.get(), 1);
        divide = ContextValue.get().getEvaluator(OperatorDivide.DIVIDE, TypeWeightTransition.get(), TypeWeightTransition.get());
        eq = ContextValue.get().getEvaluatorOrNull(OperatorEq.EQ, TypeWeightTransition.get(), TypeWeightTransition.get());
        add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeWeightTransition.get(), TypeWeightTransition.get());
        set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeWeightTransition.get(), TypeWeightTransition.get());
        cmp = TypeBoolean.get().newValue();
    }

    @Override
    public void handleNoSuccessors(NodeJANI nodeJANI) {
        /*
		NodeJANI[] successors = explorer.getSuccessors();
		successors[0].set(nodeJANI);
         */
    }

    @Override
    public ExplorerNodeProperty getNodeProperty(Object property) {
        if (property == CommonProperties.PLAYER) {
            return player;
        } else {
            return null;
        }
    }

    @Override
    public ExplorerEdgeProperty getEdgeProperty(Object property) {
        if (property == CommonProperties.WEIGHT) {
            return systemWeight;
        }
        return null;
    }

    @Override
    public void afterQueryAutomaton(ExplorerComponentAutomaton automaton) {
        assert automaton != null;
        UtilExplorer.checkAutomatonProbabilitySum(automaton);
    }

    @Override
    public void afterQuerySystem(NodeJANI node) {
        int numSuccessors = explorer.getNumSuccessors();
        set.apply(dtmcSum, zero);
        for (int succ = 0; succ < numSuccessors; succ++) {
            add.apply(dtmcSum, dtmcSum, systemWeight.get(succ));
        }
        if (eq != null) {
            eq.apply(cmp, dtmcSum, one);
            if (!cmp.getBoolean()) {
                for (int succ = 0; succ < numSuccessors; succ++) {
                    divide.apply(dtmcAligned, systemWeight.get(succ), dtmcSum);
                    systemWeight.set(succ, dtmcAligned);
                }
            }
        }
    }
}
