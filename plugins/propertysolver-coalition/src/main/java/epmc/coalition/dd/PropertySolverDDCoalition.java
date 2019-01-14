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

package epmc.coalition.dd;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import epmc.automaton.AutomatonParity;
import epmc.automaton.AutomatonParityLabel;
import epmc.automaton.ProductGraphDDExplicit;
import epmc.automaton.UtilAutomaton;
import epmc.coalition.UtilCoalition;
import epmc.coalition.messages.MessagesCoalition;
import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionCoalition;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.SMGPlayer;
import epmc.expression.standard.evaluatordd.ExpressionToDD;
import epmc.graph.CommonProperties;
import epmc.graph.Player;
import epmc.graph.SemanticsSMG;
import epmc.graph.StateMap;
import epmc.graph.StateMapDD;
import epmc.graph.StateSet;
import epmc.graph.UtilGraph;
import epmc.graph.dd.GraphDD;
import epmc.graph.dd.StateSetDD;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.EngineDD;
import epmc.modelchecker.Log;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;
import epmc.operator.OperatorNot;
import epmc.options.Options;
import epmc.util.StopWatch;

public class PropertySolverDDCoalition implements PropertySolver {
    /** Identifier of the solver. */
    public final static String IDENTIFIER = "coalition-dd";
    /** String containing an opening brace. */
    final static String BRACE_OPEN = "(";
    /** String containing a closing brace. */
    final static String BRACE_CLOSE = ")";
    /** String containing a comma. */
    final static String COMMA = ",";

    private ModelChecker modelChecker;
    private List<SMGPlayer> playerList;
    private DD playerEven;
    private List<DD> priorities;
    private Expression property;
    private ExpressionCoalition propertyCoalition;
    private StateSet states;
    private ProductGraphDDExplicit game;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        assert modelChecker != null;
        this.modelChecker = modelChecker;
    }


    @Override
    public void setProperty(Expression property) {
        this.property = property;
        if (property instanceof ExpressionCoalition) {
            this.propertyCoalition = (ExpressionCoalition) property;
        }
    }

    @Override
    public void setForStates(StateSet forStates) {
        this.states = forStates;
    }


    @Override
    public boolean canHandle() {
        assert property != null;
        if (!(modelChecker.getEngine() instanceof EngineDD)) {
            return false;
        }
        if (!(property instanceof ExpressionCoalition)) {
            return false;
        }
        if (!SemanticsSMG.isSMG(modelChecker.getModel().getSemantics())) {
            return false;
        }
        if (!ExpressionCoalition.is(property)) {
            return false;
        }
        ExpressionQuantifier quantifier = UtilCoalition.getQuantifier(propertyCoalition);
        Set<Expression> inners = UtilCoalition.collectLTLInner(quantifier.getQuantified());
        StateSet allStates = UtilGraph.computeAllStatesDD(modelChecker.getLowLevel());
        for (Expression inner : inners) {
            modelChecker.ensureCanHandle(inner, allStates);
        }
        if (allStates != null) {
            allStates.close();
        }
        return true;
    }

    @Override
    public Set<Object> getRequiredGraphProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.EXPRESSION_TO_DD);
        required.add(CommonProperties.SEMANTICS);
        return Collections.unmodifiableSet(required);
    }

    @Override
    public Set<Object> getRequiredNodeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.STATE);
        ExpressionQuantifier quantifier = UtilCoalition.getQuantifier(propertyCoalition);
        Set<Expression> inners = UtilCoalition.collectLTLInner(quantifier.getQuantified());
        StateSet allStates = UtilGraph.computeAllStatesDD(modelChecker.getLowLevel());
        for (Expression inner : inners) {
            required.addAll(modelChecker.getRequiredNodeProperties(inner, allStates));
        }
        if (allStates != null) {
            allStates.close();
        }
        required.addAll(propertyCoalition.getPlayers());
        return Collections.unmodifiableSet(required);
    }

    @Override
    public Set<Object> getRequiredEdgeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        if (!UtilCoalition.isQualitative(propertyCoalition)) {
            required.add(CommonProperties.WEIGHT);
        }
        return Collections.unmodifiableSet(required);
    }

    @Override
    public StateMap solve() {
        assert states != null;
        StateSetDD forStates = (StateSetDD) states;
        getLog().send(MessagesCoalition.COALITION_MODEL_NODES, getLowLevel().getNumNodes());
        ExpressionQuantifier quantifier = UtilCoalition.getQuantifier(propertyCoalition);
        ExpressionCoalition propertyCoalition = ExpressionCoalition.as(property);
        this.playerList = propertyCoalition.getPlayers();
        Expression path = quantifier.getQuantified();
        if (UtilCoalition.isDirTypeMin(quantifier)) {
            path = not(path);
        }
        Set<Expression> inners = UtilCoalition.collectLTLInner(path);
        GraphDD modelGraph = modelChecker.getLowLevel();
        StateSet allStates = UtilGraph.computeAllStatesDD(modelChecker.getLowLevel());
        for (Expression inner : inners) {
            StateMapDD innerResult = (StateMapDD) modelChecker.check(inner, allStates);
            ExpressionToDD expressionToDD = modelGraph
                    .getGraphPropertyObject(CommonProperties.EXPRESSION_TO_DD);
            expressionToDD.putConstantWith(inner, innerResult.getValuesDD());
        }
        allStates.close();
        boolean qualitative = UtilCoalition.isQualitative(propertyCoalition);
        boolean trivialTrue = UtilCoalition.isTrivialTrue(propertyCoalition);
        boolean trivialFalse = UtilCoalition.isTrivialFalse(propertyCoalition);
        boolean strictEven = UtilCoalition.isStrictEven(propertyCoalition);

        StateMap result = null;
        if (trivialTrue || trivialFalse) {
            result = new StateMapDD(
                    (StateSetDD) states.clone(),
                    getContextDD().newConstant(trivialTrue));
        } else if (qualitative) {
            game = buildGame(forStates, path, qualitative);
            SolverQualitative solver = new SolverQualitativeMcNaughton();
            solver.setComputeStrategies(false, false);
            solver.setGame(game);
            solver.setStrictEven(strictEven);
            DDPair regions = solver.solve();
            /*
            DD nodes = game.getNodeSpace().clone();
            StopWatch watch = Util.newStopWatch();
            System.out.println("SOLVING");
            DDPair regions = zeroMcNaughton(nodes);
            System.out.println("TIME_SOLVER_SECONDS: " + watch.getTimeSeconds());
            System.out.println("RECURSIVE_CALLS: " + zeroMcNaughtonCalls);
             */
            result = toOrig(regions.getSet0(), forStates);
        } else {
            assert false;
        }

        return result;
    }

    private StateMap toOrig(DD res, StateSetDD forStates) {
        assert res != null;
        assert forStates != null;
        StateMap result;
        if (res.isBoolean()) {
            res = res.and(game.getInitialNodes());
            res = res.abstractExist((game.getAutomatonPresCube().clone()));
            result = new StateMapDD(forStates.clone(), res);
        } else {
            assert false;
            result = null;
        }

        return result;
    }

    private ProductGraphDDExplicit buildGame(StateSetDD forStates, Expression path, boolean qualitative) {
        getLog().send(MessagesCoalition.COALITION_PRODUCT_START);
        StopWatch watch = new StopWatch(true);
        Expression[] expressions = UtilCoalition.collectLTLInner(path).toArray(new Expression[0]);
        AutomatonParity automaton = UtilAutomaton.newAutomatonParity(path, expressions);
        List<Object> nodeProperties = new ArrayList<>();
        nodeProperties.add(CommonProperties.STATE);
        nodeProperties.addAll(playerList);
        List<Object> edgeProperties = new ArrayList<>();
        if (!qualitative) {
            edgeProperties.add(CommonProperties.WEIGHT);
        }
        GraphDD modelGraph = modelChecker.getLowLevel();
        ExpressionToDD expressionToDD = modelGraph.getGraphPropertyObject(CommonProperties.EXPRESSION_TO_DD);
        ProductGraphDDExplicit product = new ProductGraphDDExplicit(modelGraph, forStates.getStatesDD(), automaton, expressionToDD);
        product.getNodeSpace();
        this.playerEven = getContextDD().newConstant(false);
        for (int i = 0; i < playerList.size(); i++) {
            playerEven = playerEven.orWith(modelGraph.getNodeProperty(playerList.get(i)).clone());
        }        

        DD player = playerEven.iteWith
                (getContextDD().newConstant(Player.ONE_STOCHASTIC),
                        getContextDD().newConstant(Player.TWO_STOCHASTIC));
        product.registerNodeProperty(CommonProperties.PLAYER, player);

        this.priorities = computePriorities(automaton, product.getLabeling());
        getLog().send(MessagesCoalition.COALITION_PRODUCT_DONE, watch.getTimeSeconds(), product.getNumNodes());
        getLog().send(MessagesCoalition.COALITION_NUMBER_COLORS, priorities.size());
        return product;
    }

    private List<DD> computePriorities(AutomatonParity automaton,
            Object2IntOpenHashMap<DD> labelsMap) {
        assert labelsMap != null;

        List<DD> priorities = new ArrayList<>();
        int numPriorities = automaton.getNumPriorities();
        priorities.clear();

        for (int labelNr = 0; labelNr < numPriorities; labelNr++) {
            priorities.add(getContextDD().newConstant(false));
        }

        for (DD labelDD : labelsMap.keySet()) {
            int labelNr = labelsMap.getInt(labelDD);
            AutomatonParityLabel label = (AutomatonParityLabel) automaton.numberToLabel(labelNr);
            int priority = label.getPriority();
            if (priority != Integer.MAX_VALUE) {
                priorities.set(priority, priorities.get(priority).orWith(labelDD.clone()));
            }
        }
        return priorities;
    }

    private ContextDD getContextDD() {
        return ContextDD.get();
    }

    /**
     * Get log used for analysis.
     * 
     * @return log used for analysis
     */
    private Log getLog() {
        return Options.get().get(OptionsMessages.LOG);
    }

    private Expression not(Expression expression) {
        return new ExpressionOperator.Builder()
                .setOperator(OperatorNot.NOT)
                .setOperands(expression)
                .build();
    }

    /**
     * Get low level model of model checker.
     * 
     * @return low level model of model checker
     */
    private GraphDD getLowLevel() {
        return modelChecker.getLowLevel();
    }
}
