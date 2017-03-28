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

package epmc.propertysolver.ltllazy.automata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import epmc.automaton.AutomatonDD;
import epmc.automaton.Buechi;
import epmc.automaton.BuechiTransition;
import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.VariableDD;
import epmc.error.EPMCException;
import epmc.expression.standard.evaluatordd.ExpressionToDD;
import epmc.graph.CommonProperties;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.util.BitSet;
import epmc.value.ContextValue;

public final class AutomatonDDSubset implements AutomatonDD {
    private final ContextDD contextDD;
    private final int numLabels;
    private final GraphExplicit automaton;
    private final ArrayList<DD> labels;
    private final ArrayList<DD> presVars;
    private final ArrayList<DD> nextVars;
    private final ArrayList<VariableDD> stateVariables = new ArrayList<>();
    private final DD presCube;
    private final DD nextCube;
    private final DD labelsCube;
    private final DD init;
    private final DD trans;
    private final DD under;
    private final DD over;
    private final Buechi buechi;
    private final ExpressionToDD expressionToDD;

    /* constructors */
    
    public AutomatonDDSubset(ExpressionToDD expressionToDD, Buechi buechi, DD states)
            throws EPMCException {
        assert expressionToDD != null;
        assert buechi != null;
        assert states != null;
        assert buechi.getNumLabels() > 0;
        this.expressionToDD = expressionToDD;
        this.buechi = buechi;
        this.contextDD = expressionToDD.getContextDD();
        this.numLabels = buechi.getNumLabels();
        this.automaton = buechi.getGraph();
        this.labels = new ArrayList<>();
        this.presVars = new ArrayList<>();
        this.nextVars = new ArrayList<>();
        prepareVariables();
        this.presCube = contextDD.listToCube(presVars);
        this.nextCube = contextDD.listToCube(nextVars);
        this.labelsCube = contextDD.listToCube(labels);
        DD init = computeInit();
        this.init = init.andWith(states.clone());
        DD stateStaySame = computeStateStaySame();
        DD tr = computeTransition(buechi.getContextValue());
        trans = states.clone().andWith(tr).orWith(states.not().andWith(stateStaySame));
        under = computeUnder(buechi.getContextValue(), contextDD);
        over = computeOver(buechi.getContextValue(), contextDD);
    }

    @Override
    public DD getInitial() {
        return init;
    }

    @Override
    public DD getTransitions() {
        return trans;
    }

    @Override
    public List<DD> getPresVars() {
        return presVars;
    }
    
    @Override
    public List<DD> getNextVars() {
        return nextVars;
    }

    @Override
    public List<DD> getLabelVars() {
        return labels;
    }

    public DD getUnder() {
        return under;
    }
    
    public DD getOver() {
        return over;
    }

    /* private auxiliary methods */
    
    private void prepareVariables() throws EPMCException {
        for (int labelNr = 0; labelNr < numLabels; labelNr++) {
            String labelName = "%autlabel" + labelNr;
            VariableDD variable = contextDD.newBoolean(labelName, 1);
            labels.add(variable.getDDVariables(0).get(0).clone());
        }
        for (int node = 0; node < automaton.getNumNodes(); node++) {
            String stateName = "%autstate" + node;
            VariableDD variable = contextDD.newBoolean(stateName, 2);
            stateVariables.add(variable);
            for (DD var : variable.getDDVariables(0)) {
                presVars.add(var.clone());
            }
            for (DD var : variable.getDDVariables(1)) {
                nextVars.add(var.clone());
            }
        }
    }
    
    private DD computeInit() throws EPMCException {
        DD init = contextDD.newConstant(true);
        BitSet bInit = buechi.getGraph().getInitialNodes();
        for (int node = 0; node < automaton.getNumNodes(); node++) {
        	if (bInit.get(node)) {
        		init = init.andWith(presVars.get(node).clone());        		
        	} else {
        		init = init.andWith(presVars.get(node).not());
        	}
        }
        return init;
    }
    
    private DD computeTransition(ContextValue context) throws EPMCException {
        List<DD> rSucc = subsetImage();
        
        int trueState = buechi.getTrueState();
        if (trueState != -1) {
            DD rNextTrue = rSucc.get(trueState);
            for (int state = 0; state < rSucc.size(); state++) {
                if (state != trueState) {
                    rSucc.set(state, rSucc.get(state).andNotWith(rNextTrue.clone()));
                }
            }
        }
        
        DD trans = eq(rSucc, nextVars);
        contextDD.dispose(rSucc);
        return trans;
    }

    private List<DD> subsetImage() throws EPMCException {
        List<DD> nextOns = new ArrayList<>();
        for (int node = 0; node < automaton.getNumNodes(); node++) {
            nextOns.add(contextDD.newConstant(false));
        }
        EdgeProperty labels = automaton.getEdgeProperty(CommonProperties.AUTOMATON_LABEL);
        for (int state = 0; state < automaton.getNumNodes(); state++) {
            automaton.queryNode(state);
            DD presVar = presVars.get(state);
            for (int succNr = 0; succNr < automaton.getNumSuccessors(); succNr++) {
                BuechiTransition trans = labels.getObject(state, succNr);
                int succ = automaton.getSuccessorNode(succNr);
                DD guard = expressionToDD.translate(trans.getExpression());
                DD nextOn = nextOns.get(succ);
                nextOn = nextOn.orWith(guard.andWith(presVar.clone()));
                nextOns.set(succ, nextOn);
            }
        }

        return nextOns;
    }
    
    private DD eq(List<DD> set1, List<DD> set2) throws EPMCException {
        assert set1 != null;
        assert set2 != null;
        for (DD dd : set1) {
            assert dd != null;
        }
        for (DD dd : set2) {
            assert dd != null;
        }
        assert set1.size() == set2.size();
        DD result = contextDD.newConstant(true);
        Iterator<DD> set1Iter = set1.iterator();
        Iterator<DD> set2Iter = set2.iterator();
        while (set1Iter.hasNext()) {
            DD state1 = set1Iter.next();
            DD state2 = set2Iter.next();
            result = result.andWith(state1.iff(state2));
        }

        return result;
    }

    private DD computeUnder(ContextValue context, ContextDD encoding) throws EPMCException {
        ArrayList<DD> labelsOns = new ArrayList<>();
        for (int labelNr = 0; labelNr < numLabels; labelNr++) {
            labelsOns.add(contextDD.newConstant(true));
        }
        EdgeProperty labelsProps = automaton.getEdgeProperty(CommonProperties.AUTOMATON_LABEL);
        for (int state = 0; state < automaton.getNumNodes(); state++) {
            automaton.queryNode(state);
            DD presVar = presVars.get(state);
            for (int succNr = 0; succNr < automaton.getNumSuccessors(); succNr++) {
                BuechiTransition trans = labelsProps.getObject(state, succNr);
                DD guard = expressionToDD.translate(trans.getExpression());
                DD notGuardAndPres = guard.andWith(presVar.clone()).notWith();
                BitSet labeling = trans.getLabeling();
                for (int labelNr = 0; labelNr < numLabels; labelNr++) {
                    if (!labeling.get(labelNr)) {
                        DD labelOn = labelsOns.get(labelNr);
                        labelOn = labelOn.andWith(notGuardAndPres.clone());
                        labelsOns.set(labelNr, labelOn);
                    }
                }
                notGuardAndPres.dispose();
            }
        }
        
        DD under = contextDD.newConstant(true);
        for (int labelNr = 0; labelNr < numLabels; labelNr++) {
            DD labelOn = labelsOns.get(labelNr);
            DD label = labels.get(labelNr);
            under = under.andWith(labelOn.iff(label));
        }
        contextDD.dispose(labelsOns);
        DD underOld = under;
        under = fixAcceptanceEmptySet(under);
        underOld.dispose();
                
        return under;
    }

    private DD fixAcceptanceEmptySet(DD acceptance) throws EPMCException {
        DD allStatesOff = contextDD.newConstant(true);
        for (int state = 0; state < automaton.getNumNodes(); state++) {
            DD nextVar = nextVars.get(state);
            allStatesOff = allStatesOff.andWith(nextVar.not());
        }
        
        allStatesOff = allStatesOff.abstractAndExistWith(trans.clone(), nextCube.clone());
        
        
        DD allLabelsOff = contextDD.newConstant(true);
        for (int labelNr = 0; labelNr < numLabels; labelNr++) {
            DD label = labels.get(labelNr);
            allLabelsOff = allLabelsOff.andWith(label.not());
        }
        acceptance = allStatesOff.not().andWith(acceptance.clone())
                .orWith(allStatesOff.andWith(allLabelsOff));
        
        return acceptance;
    }

    private DD computeOver(ContextValue context, ContextDD encoding)
            throws EPMCException {
        ArrayList<DD> labelsOns = new ArrayList<>();
        for (int labelNr = 0; labelNr < numLabels; labelNr++) {
            labelsOns.add(contextDD.newConstant(false));
        }
        EdgeProperty labelsProp = automaton.getEdgeProperty(CommonProperties.AUTOMATON_LABEL);
        for (int state = 0; state < automaton.getNumNodes(); state++) {
            DD presVar = presVars.get(state);
            automaton.queryNode(state);
            for (int succNr = 0; succNr < automaton.getNumSuccessors(); succNr++) {
                BuechiTransition trans = labelsProp.getObject(state, succNr);
                DD guard = expressionToDD.translate(trans.getExpression());
                DD guardAndPres = guard.andWith(presVar.clone());
                BitSet labeling = trans.getLabeling();
                for (int labelNr = 0; labelNr < numLabels; labelNr++) {
                    if (labeling.get(labelNr)) {
                        DD labelOn = labelsOns.get(labelNr);
                        labelOn = labelOn.orWith(guardAndPres.clone());
                        labelsOns.set(labelNr, labelOn);
                    }
                }
                guardAndPres.dispose();
            }
        }
        
        DD over = contextDD.newConstant(true);
        for (int labelNr = 0; labelNr < numLabels; labelNr++) {
            DD labelOn = labelsOns.get(labelNr);
            DD label = labels.get(labelNr);
            over = over.andWith(labelOn.iff(label));
        }
        contextDD.dispose(labelsOns);
        DD overOld = over;
        over = fixAcceptanceEmptySet(over);
        overOld.dispose();

        return over;
    }
    
    private DD computeStateStaySame() throws EPMCException {
        DD result = contextDD.newConstant(true);
        Iterator<DD> presIter = presVars.iterator();
        Iterator<DD> nextIter = nextVars.iterator();
        while (presIter.hasNext()) {
            DD pres = presIter.next();
            DD next = nextIter.next();
            result = result.andWith(pres.iff(next));
        }
        return result;
    }

    public Buechi getBuechi() {
        return buechi;
    }

    public List<VariableDD> getStateVariables() {
        return stateVariables;
    }

    @Override
    public void close() {
        contextDD.dispose(labels);
        contextDD.dispose(presVars);
        contextDD.dispose(nextVars);
        presCube.dispose();
        nextCube.dispose();
        labelsCube.dispose();
        init.dispose();
        trans.dispose();
        under.dispose();
        over.dispose();
    }

    @Override
    public DD getPresCube() {
        return presCube;
    }

    @Override
    public DD getNextCube() {
        return nextCube;
    }

    @Override
    public DD getLabelCube() {
        return labelsCube;
    }
}
