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

package epmc.automaton;

import java.util.ArrayList;
import java.util.List;

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.Permutation;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.graph.CommonProperties;
import epmc.graph.dd.GraphDD;
import epmc.graph.dd.GraphDDProperties;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.util.StopWatch;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeObject;

public final class ProductGraphDDDD implements ProductGraphDD {
    private final GraphDDProperties properties;
    private final DD initial;
    private final DD transitionsBoolean;
    private final DD weight;
    private final ContextDD contextDD;
    private final DD states;
    private final DD player;
    private final List<DD> presVars;
    private final List<DD> nextVars;
    private final List<DD> actionVars;
    private final DD presCube;
    private final DD nextCube;
    private final DD actionCube;
    private final DD presAndActions;
    private final Permutation nextToPres;
    private final AutomatonDD automaton;
    private DD nodes;
    private boolean closed;
    private GraphDD model;

    public ProductGraphDDDD(GraphDD model, DD modelInit, AutomatonDD automaton)
            throws EPMCException {
        this.model = model;
        this.properties = new GraphDDProperties(this);
        assert model != null;
        assert automaton != null;
        assert model.getContextDD() == automaton.getContextDD();
        this.initial = modelInit.and(automaton.getInitial());
        this.transitionsBoolean = model.getTransitions().
                and(automaton.getTransitions());
        DD autTransNum = automaton.getTransitions().toMT();
        DD modelWeight = model.getEdgeProperty(CommonProperties.WEIGHT);
        this.weight = modelWeight.clone().multiplyWith(autTransNum);
        this.contextDD = model.getContextDD();
        this.states = model.getNodeProperty(CommonProperties.STATE).clone();
        this.player = model.getNodeProperty(CommonProperties.PLAYER).clone();
        this.actionVars = new ArrayList<>();
        for (DD var : contextDD.cubeToList(model.getActionCube())) {
            this.actionVars.add(var.clone());
        }
        this.presVars = new ArrayList<>();
        for (DD var : contextDD.cubeToList(model.getPresCube())) {
            this.presVars.add(var.clone());
        }
        for (DD var : automaton.getPresVars()) {
            this.presVars.add(var.clone());
        }
        this.nextVars = new ArrayList<>();
        for (DD var : contextDD.cubeToList(model.getNextCube())) {
            this.nextVars.add(var.clone());
        }
        for (DD var : automaton.getNextVars()) {
            this.nextVars.add(var.clone());
        }
        this.presAndActions = model.getPresCube().and(automaton.getPresCube(),
                model.getActionCube(), automaton.getLabelCube());
        this.nextToPres = contextDD.newPermutationListDD(presVars, nextVars);
        this.automaton = automaton;
        this.presCube = contextDD.listToCube(presVars);
        this.nextCube = contextDD.listToCube(nextVars);
        this.actionCube = contextDD.listToCube(actionVars);
        if (model.getGraphProperties().contains(CommonProperties.SEMANTICS)) {
            Object semantics = model.getGraphPropertyObject(CommonProperties.SEMANTICS);
            properties.registerGraphProperty(CommonProperties.SEMANTICS,
                    new TypeObject.Builder()
                            .setContext(contextDD.getContextValue())
                            .setClazz(semantics.getClass())
                            .build());
            setGraphPropertyObject(CommonProperties.SEMANTICS, semantics);
        }
        properties.registerNodeProperty(CommonProperties.STATE, states);
        properties.registerNodeProperty(CommonProperties.PLAYER, player);
        properties.registerEdgeProperty(CommonProperties.WEIGHT, weight);
    }
    
    public ProductGraphDDDD(GraphDD model, AutomatonDD automaton)
            throws EPMCException {
        this(model, model.getInitialNodes(), automaton);
    }
    
    @Override
    public DD getInitialNodes() throws EPMCException {
        return initial;
    }

    @Override
    public DD getTransitions() throws EPMCException {
        return transitionsBoolean;
    }

    @Override
    public Permutation getSwapPresNext() {
        return nextToPres;
    }

    public AutomatonDD getAutomaton() {
        return automaton;
    }

    @Override
    public DD getNodeSpace() throws EPMCException {
        if (nodes == null) {
            nodes = exploreNodeSpace(this);
        }
        return nodes;
    }

    @Override
    public void close() {
        if (!closed) {
            return;
        }
        this.closed = true;
        properties.close();
        initial.dispose();
        transitionsBoolean.dispose();
        weight.dispose();
        states.dispose();
        player.dispose();
        contextDD.dispose(presVars);
        contextDD.dispose(nextVars);
        contextDD.dispose(actionVars);
        presCube.dispose();
        nextCube.dispose();
//        actionCube.dispose();
        presAndActions.dispose();
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
    public DD getActionCube() {
        return actionCube;
    }

    @Override
    public GraphDDProperties getProperties() {
        return properties;
    }
    
    private static DD exploreNodeSpace(GraphDD graph) throws EPMCException {
        assert graph != null;
        Log log = graph.getOptions().get(OptionsMessages.LOG);
        StopWatch timer = new StopWatch(true);
        log.send(MessagesAutomaton.EXPLORING);
        ContextDD contextDD = graph.getContextDD();
        DD states = graph.getInitialNodes().clone();
        DD predecessors = contextDD.newConstant(false);
        DD trans = graph.getTransitions().abstractExist(graph.getActionCube());
        while (!states.equals(predecessors)) {
            // only exploring new states important for Rabin semi-symbolic mtd
//            DD andNot = states.andNot(predecessors);
            DD andNot = states.clone();
            predecessors.dispose();
            predecessors = states;
            DD graphNext = next(trans, andNot, graph.getPresCube(), graph.getSwapPresNext());
            DD statesOr = states.or(graphNext);
            graphNext.dispose();
            states = statesOr;
            andNot.dispose();
        }
        trans.dispose();
        predecessors.dispose();
        log.send(MessagesAutomaton.EXPLORING_DONE, timer.getTimeSeconds());
        return states;
    }
    
    private static DD next(DD trans, DD from, DD pres, Permutation swap) throws EPMCException {
        return trans.abstractAndExist(from, pres).permuteWith(swap);
    }

    
    @Override
    public ContextValue getContextValue() {
        return model.getContextValue();
    }

	@Override
	public Type getType(Expression expression) throws EPMCException {
		assert expression != null;
		return model.getType(expression);
	}
}
