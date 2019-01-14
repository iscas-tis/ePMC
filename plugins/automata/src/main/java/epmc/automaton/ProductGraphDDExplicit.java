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

import static epmc.error.UtilError.ensure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.Permutation;
import epmc.dd.VariableDD;
import epmc.expression.Expression;
import epmc.expression.standard.evaluatordd.ExpressionToDD;
import epmc.graph.CommonProperties;
import epmc.graph.dd.GraphDD;
import epmc.graph.dd.GraphDDProperties;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.options.Options;
import epmc.util.StopWatch;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.TypeObject;
import epmc.value.UtilValue;
import epmc.value.Value;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public final class ProductGraphDDExplicit implements ProductGraphDD {
    private final static String AUTSTATE = "%auttstate";
    private final static String EXPR = "%expr";

    private final GraphDDProperties properties;
    private final GraphDD model;
    private final Automaton automaton;
    private final DD varEqExpressions;
    private final DD exprVarsCube;
    private final DD initial;
    private final Int2ObjectOpenHashMap<Expression> varToExp = new Int2ObjectOpenHashMap<>();
    private final int[] exprVars;
    private final Expression[] expressions;
    private final Value falseValue;
    private final Value trueValue;
    private final Permutation swapPresNext;
    private final ArrayList<DD> presVars = new ArrayList<>();
    private final ArrayList<DD> nextVars = new ArrayList<>();
    private final DD presCube;
    private final DD presActionsCube;
    private final DD presCubeAutomaton;
    private final DD presCubeModel;
    private final DD nextCube;
    private final DD actionCube;
    private final Object2IntOpenHashMap<DD> ddToState = new Object2IntOpenHashMap<>();
    private final Int2ObjectOpenHashMap<DD> stateToPresDd = new Int2ObjectOpenHashMap<>();
    private final HashMap<Integer,DD> stateToNextDd = new HashMap<>();
    private final DD automatonStatesSame;
    private final DD autInit;
    private final int numStatesReserved;
    private DD states;
    private DD transitionsBoolean;
    private DD transitions;
    private DD transStateAut;
    private final Object2IntOpenHashMap<DD> labeling = new Object2IntOpenHashMap<>();
    private final VariableDD stateCounter;
    private boolean closed;
    private DD nodes;

    public ProductGraphDDExplicit(GraphDD model, DD modelInit, Automaton automaton,
            ExpressionToDD expressionToDD) {
        assert model != null;
        assert modelInit != null;
        assert automaton != null;
        this.properties = new GraphDDProperties(this);
        this.model = model;
        this.automaton = automaton;

        this.falseValue = UtilValue.newValue(TypeBoolean.get(), false);
        this.trueValue = UtilValue.newValue(TypeBoolean.get(), true);
        ContextDD contextDD = ContextDD.get();

        numStatesReserved = Options.get().getInteger(OptionsAutomaton.AUTOMATON_DD_MAX_STATES);
        stateCounter = contextDD.newInteger(AUTSTATE, 2, 0, numStatesReserved - 1);
        this.presVars.addAll(contextDD.cubeToListClone(model.getPresCube()));
        this.presVars.addAll(contextDD.clone(stateCounter.getDDVariables(0)));
        this.nextVars.addAll(contextDD.cubeToListClone(model.getNextCube()));
        this.nextVars.addAll(contextDD.clone(stateCounter.getDDVariables(1)));
        this.presCubeAutomaton = contextDD.listToCube(stateCounter.getDDVariables(0));
        this.presCubeModel = model.getPresCube().clone();

        DD ddAutInitPres = stateCounter.newIntValue(0, 0);
        DD ddAutInitNext = stateCounter.newIntValue(1, 0);
        this.autInit = ddAutInitPres;
        this.ddToState.put(ddAutInitPres, automaton.getInitState());
        this.stateToPresDd.put(automaton.getInitState(), ddAutInitPres.clone());
        this.stateToNextDd.put(automaton.getInitState(), ddAutInitNext);
        this.initial = modelInit.and(ddAutInitPres);

        DD varEqExpressions = contextDD.newConstant(true);
        DD variablesCube = contextDD.newConstant(true);
        IntArrayList exprVars = new IntArrayList();
        ArrayList<Expression> expressions = new ArrayList<>();
        int exprNr = 0;
        for (Expression expression : automaton.getExpressions()) {
            String exprName = EXPR + exprNr;
            VariableDD var = contextDD.newBoolean(exprName, 1);
            DD variable = var.newCube(0);
            DD expressionDD = expressionToDD.translate(expression);
            DD varEqExpression = variable.clone().iffWith(expressionDD);
            varEqExpressions = varEqExpressions.andWith(varEqExpression);
            varToExp.put(variable.variable(), expression);
            exprVars.add(variable.variable());
            variablesCube = variablesCube.andWith(variable);
            expressions.add(expression);
            exprNr++;
        }
        this.varEqExpressions = varEqExpressions;
        this.exprVarsCube = variablesCube;
        this.exprVars = exprVars.toIntArray();
        this.expressions = expressions.toArray(new Expression[0]);
        this.swapPresNext = contextDD.newPermutationListDD(presVars, nextVars);
        this.presCube = contextDD.listToCube(presVars);

        List<DD> presVarsAutomaton = stateCounter.getDDVariables(0);
        List<DD> nextVarsAutomaton = stateCounter.getDDVariables(1);
        DD same = contextDD.newConstant(true);
        Iterator<DD> presOIter = presVarsAutomaton.iterator();
        Iterator<DD> nextOIter = nextVarsAutomaton.iterator();
        while (presOIter.hasNext()) {
            DD presOVar = presOIter.next();
            DD nextOVar = nextOIter.next();
            same = same.andWith(presOVar.iff(nextOVar));
        }
        this.automatonStatesSame = same;
        this.presActionsCube = presCube.and(model.getActionCube());
        this.states = contextDD.newConstant(false);
        this.transStateAut = contextDD.newConstant(false);
        this.nextCube = contextDD.listToCube(nextVars);
        this.actionCube = model.getActionCube().clone();

        if (model.getGraphProperties().contains(CommonProperties.SEMANTICS)) {
            Object semantics = model.getGraphPropertyObject(CommonProperties.SEMANTICS);
            properties.registerGraphProperty(CommonProperties.SEMANTICS,
                    new TypeObject.Builder()
                    .setClazz(semantics.getClass())
                    .build());
            setGraphPropertyObject(CommonProperties.SEMANTICS,
                    semantics);
        }
        properties.registerNodeProperty(CommonProperties.STATE,
                model.getNodeProperty(CommonProperties.STATE));

        if (model.getNodeProperties().contains(CommonProperties.PLAYER)) {
            properties.registerNodeProperty(CommonProperties.PLAYER,
                    model.getNodeProperty(CommonProperties.PLAYER));
        }
    }

    public ProductGraphDDExplicit(GraphDD model, Automaton automaton, ExpressionToDD expressionToDD)
    {
        this(model, model.getInitialNodes(), automaton, expressionToDD);
    }

    @Override
    public DD getInitialNodes() {
        return initial;
    }

    private DD next(DD from) {
        assert from != null;
        states = states.orWith(from.clone());
        DD modelStates = model.getNodeProperty(CommonProperties.STATE);
        DD nonStates = from.andNot(modelStates);

        DD nonStateTransitionBoolean = model.getTransitions().clone();
        nonStateTransitionBoolean = nonStateTransitionBoolean.andWith(nonStates);
        nonStateTransitionBoolean = nonStateTransitionBoolean.andWith(automatonStatesSame.clone());
        DD nextNonStates = nonStateTransitionBoolean.abstractExistWith(presActionsCube.clone());
        nextNonStates = nextNonStates.permuteWith(swapPresNext);

        from = from.and(modelStates);
        DD fromAndVarEqExprs = from.and(varEqExpressions);
        DD assignmentsDD = fromAndVarEqExprs.abstractExist(presCube);
        ContextDD contextDD = ContextDD.get();
        DD nextStates = contextDD.newConstant(false);
        DD modelTrans = model.getTransitions().abstractExist(model.getActionCube());
        while (!assignmentsDD.isFalse()) {
            IntOpenHashSet assignment = assignmentsDD.findSatSet(exprVarsCube);
            DD assignmentDD = contextDD.intSetToDD(assignment, exprVarsCube);
            Value[] array = assignmentToArray(assignment);
            DD fromAssignment = fromAndVarEqExprs.and(assignmentDD);
            fromAssignment = fromAssignment.abstractExistWith(exprVarsCube.clone());
            DD automatonStatesDD = fromAssignment.abstractExist(presCubeModel);

            while (!automatonStatesDD.isFalse()) {
                IntOpenHashSet automatonStateSet = automatonStatesDD.findSatSet(presCubeAutomaton);
                DD automatonStateDD = contextDD.intSetToDD(automatonStateSet, presCubeAutomaton);
                int automatonState = ddToState.getInt(automatonStateDD);

                automaton.queryState(array, automatonState);
                int nextAutomatonState = automaton.getSuccessorState();
                DD presAutStateDD;
                DD nextAutStateDD;
                if (stateToNextDd.containsKey(nextAutomatonState)) {
                    presAutStateDD = stateToPresDd.get(nextAutomatonState);
                    nextAutStateDD = stateToNextDd.get(nextAutomatonState);
                } else {
                    int number = stateToPresDd.size();
                    ensure(number < numStatesReserved, ProblemsAutomaton.DD_INSUFFICIENT_STATES);
                    presAutStateDD = stateCounter.newIntValue(0, number);
                    nextAutStateDD = stateCounter.newIntValue(1, number);
                    stateToPresDd.put(nextAutomatonState, presAutStateDD);
                    stateToNextDd.put(nextAutomatonState, nextAutStateDD);
                    ddToState.put(presAutStateDD, nextAutomatonState);
                }
                DD assgAndAut = fromAssignment.and(automatonStateDD);
                DD presModel = assgAndAut.abstractExistWith(presCubeAutomaton.clone());
                DD nextModel = next(modelTrans, presModel, model.getPresCube(), model.getSwapPresNext());
                presModel.dispose();
                nextStates = nextStates.orWith(nextModel.andWith(presAutStateDD.clone()));

                DD autTr = automatonStateDD.and(nextAutStateDD).andWith(fromAssignment.clone());
                transStateAut = transStateAut.orWith(autTr);

                int labeling = automaton.getSuccessorLabel();
                this.labeling.put(automatonStateDD.and(fromAssignment), labeling);

                automatonStatesDD = automatonStatesDD.andNotWith(automatonStateDD);
            }
            automatonStatesDD.dispose();
            fromAssignment.dispose();

            assignmentsDD = assignmentsDD.andNotWith(assignmentDD);
        }
        modelTrans.dispose();
        assignmentsDD.dispose();
        from.dispose();
        fromAndVarEqExprs.dispose();
        return nextNonStates.orWith(nextStates);
    }

    private static DD next(DD trans, DD from, DD presCube, Permutation swap) {
        return trans.abstractAndExist(from, presCube).permuteWith(swap);
    }

    private Value[] assignmentToArray(IntOpenHashSet assignment) {
        Value[] result = new Value[expressions.length];
        for (int exprNr = 0; exprNr < expressions.length; exprNr++) {
            int variable = exprVars[exprNr];
            Value value = assignment.contains(variable) ? trueValue : falseValue;
            result[exprNr] = value;
        }
        return result;
    }

    @Override
    public DD getTransitions() {
        if (transitionsBoolean == null) {
            computeTransitions();
        }
        return transitionsBoolean;
    }

    private void computeTransitions() {
        assert transitions == null;
        assert transitionsBoolean == null;
        DD states = model.getNodeProperty(CommonProperties.STATE);
        DD tbAutNonStates = states.not().andWith(automatonStatesSame.clone());
        DD tbAutStates = states.and(transStateAut);
        DD tbAut = tbAutNonStates.orWith(tbAutStates);
        transitionsBoolean = tbAut.and(model.getTransitions());
        DD modelWeight = model.getEdgeProperty(CommonProperties.WEIGHT);
        if (transitions != null) {
            transitions = tbAut.toMTWith().multiplyWith(modelWeight.clone());
            properties.registerEdgeProperty(CommonProperties.WEIGHT, transitions);
        }
    }

    @Override
    public Permutation getSwapPresNext() {
        return swapPresNext;
    }

    public Object2IntOpenHashMap<DD> getLabeling() {
        return labeling;        
    }

    public Automaton getAutomaton() {
        return automaton;
    }

    public DD getAutomatonInit() {
        return autInit;
    }

    public DD getAutomatonPresCube() {
        return presCubeAutomaton;
    }

    public Object2IntOpenHashMap<DD> getAutomatonStates() {
        return ddToState;
    }

    @Override
    public DD getNodeSpace() {
        if (nodes == null) {
            nodes = exploreNodeSpace(this);
        }
        return nodes;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        varEqExpressions.dispose();
        exprVarsCube.dispose();
        ContextDD contextDD;
        contextDD = ContextDD.get();
        initial.dispose();
        contextDD.dispose(presVars);
        contextDD.dispose(nextVars);
        presCube.dispose();
        presActionsCube.dispose();
        presCubeAutomaton.dispose();
        presCubeModel.dispose();
        nextCube.dispose();
        actionCube.dispose();
        automatonStatesSame.dispose();
        autInit.dispose();
        states.dispose();
        transitionsBoolean.dispose();
        if (transitions != null) {
            transitions.dispose();
        }
        transStateAut.dispose();
        contextDD.dispose(labeling.keySet());
        contextDD.dispose(stateToPresDd.values());
        contextDD.dispose(stateToNextDd.values());
    }

    @Override
    public DD getPresCube()  {
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
    public ContextDD getContextDD() {
        return model.getContextDD();
    }

    @Override
    public GraphDDProperties getProperties() {
        return properties;
    }

    private static DD exploreNodeSpace(ProductGraphDDExplicit graph) {
        assert graph != null;
        Log log = Options.get().get(OptionsMessages.LOG);
        StopWatch timer = new StopWatch(true);
        log.send(MessagesAutomaton.EXPLORING);
        ContextDD contextDD = ContextDD.get();
        DD states = graph.getInitialNodes().clone();
        DD predecessors = contextDD.newConstant(false);
        while (!states.equals(predecessors)) {
            // only exploring new states important for Rabin semi-symbolic mtd
            //            DD andNot = states.andNot(predecessors);
            DD andNot = states.clone();
            predecessors.dispose();
            predecessors = states;
            DD graphNext = graph.next(andNot);
            DD statesOr = states.or(graphNext);
            graphNext.dispose();
            states = statesOr;
            andNot.dispose();
        }
        predecessors.dispose();
        log.send(MessagesAutomaton.EXPLORING_DONE, timer.getTimeSeconds());
        return states;
    }

    @Override
    public Type getType(Expression expression) {
        assert expression != null;
        return model.getType(expression);
    }
}
