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

package epmc.jani.explorer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import epmc.expression.Expression;
import epmc.expression.evaluatorexplicit.EvaluatorCache;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.expression.standard.simplify.ContextExpressionSimplifier;
import epmc.expressionevaluator.ExpressionToType;
import epmc.graph.CommonProperties;
import epmc.graph.SemanticsNonDet;
import epmc.graph.SemanticsStochastic;
import epmc.jani.model.Action;
import epmc.jani.model.Automaton;
import epmc.jani.model.Edge;
import epmc.jani.model.Edges;
import epmc.jani.model.Location;
import epmc.jani.model.Locations;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.jani.model.Variable;
import epmc.jani.model.component.Component;
import epmc.jani.model.component.ComponentAutomaton;
import epmc.jani.model.type.JANIType;
import epmc.jani.value.TypeLocation;
import epmc.jani.value.ValueLocation;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorIsZero;
import epmc.operator.OperatorMultiply;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.TypeInteger;
import epmc.value.TypeWeightTransition;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueBoolean;
import epmc.value.ValueInteger;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

/**
 * Explorer for an automaton component.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ExplorerComponentAutomaton implements ExplorerComponent {
    private final class ExpressionToTypeAutomaton implements ExpressionToType {
        private final Map<Expression,Variable> variables = new LinkedHashMap<>();

        private ExpressionToTypeAutomaton(Collection<Variable> variables) {
            assert variables != null;
            for (Variable variable : variables) {
                assert variable != null;
            }
            for (Variable variable : variables) {
                this.variables.put(variable.getIdentifier(), variable);
            }
        }

        @Override
        public Type getType(Expression expression) {
            assert expression != null;
            Variable variable = variables.get(expression);
            if (variable == null && ExpressionIdentifierStandard.is(expression)) {
                expression = new ExpressionIdentifierStandard.Builder()
                        .setName(ExpressionIdentifierStandard.as(expression).getName())
                        .setScope(componentAutomaton.getAutomaton())
                        .build();
                variable = variables.get(expression);
            }
            if (variable != null) {
                JANIType type = variable.getType();
                if (type == null) {
                    return null;
                }
                return type.toType();
            }
            return null;
            // TODO ..
        }
    }

    private EvaluatorCache evaluatorCache;
    /** Name of variable denoting location of automaton. */
    private final static String LOCATION_IDENTIFIER = "%locId";
    private final static String EDGE_IDENTIFIER = "%edge";

    /** Explorer to which this component belongs. */
    private ExplorerJANI explorer;
    /** Component which this explorer is supposed to explore. */
    private Component component;
    /** Component which this explorer is supposed to explore.
     * This field is used in case this class can handle the component. */
    private ComponentAutomaton componentAutomaton;
    /** Automaton which this explorer is supposed to explore. */
    private Automaton automaton;
    /**
     * Evaluators of the edges of the automaton.
     * The size of this array is the same as the number of components. In each
     * array element, the array of evaluators for the edge with the according
     * source location is stored.
     * */
    private EdgeEvaluator[][] edgeEvaluators;
    private AssignmentsEvaluator[] locationEvaluators;

    /** Type for the location set of this automaton. */
    private TypeLocation typeLocation;
    /** Type for the edge of a given transition.
     * To denote the situation that no edge is chosen yet (such as in a model
     * state), -1 is used. This variable is only used for nondeterministic
     * models with probabilities.
     * */
    private Type typeEdge;
    /** Enumerates variables.
     * The first number are occupied by the local variables of the automaton.
     * The following numbers correspond to the global variables of the
     * automaton.
     */
    private Map<Variable,Integer> variableToNumber = new LinkedHashMap<>();
    /**
     * Map from identifier of automaton to local identifiers.
     * This map is necessary, because automata may occur multiple times in
     * system specification, and we must use separate variables for each
     * instance.
     */
    private Map<Expression,Expression> autVarToLocal = new LinkedHashMap<>();
    /** Number of automata location variable, or -1 if unused. */
    private int locationVarNr;
    /** Number of edge currently selected, or -1 if non selected or unused. */
    private int edgeVarNr;
    /** Number of successors of queried node. */
    private int numSuccessors;
    /** Array of successors of queried node. */
    private NodeJANI[] successors;
    /** Property to store whether given node is a state. */
    private PropertyNodeGeneral state;
    /** Transition weight property. */
    private PropertyEdgeGeneral weight;
    /** Label/action property. */
    private PropertyEdgeAction label;
    /** Sum of weight of outgoing transitions. */
    private ValueAlgebra probabilitySum;
    /** Whether the model is nondeterministic. */
    private boolean nonDet;
    /** Whether the model is stochastic. */
    private boolean stochastic;
    /** Value zero of weight type. */
    private ValueAlgebra weightZero;
    /** Successor weight value being computed. */
    private ValueAlgebra weightValue;
    /** Automata name (interned string). */
    private String name;
    private int number;
    private int[] actionFromTo;
    private OperatorEvaluator isZero;
    private ValueBoolean cmp;
    private OperatorEvaluator add;
    private OperatorEvaluator multiply;

    @Override
    public void setExplorer(ExplorerJANI explorer) {
        assert explorer != null;
        this.explorer = explorer;
    }

    @Override
    public void setComponent(Component component) {
        assert component != null;
        this.component = component;
    }

    @Override
    public boolean canHandle() {
        if (!(component instanceof ComponentAutomaton)) {
            return false;
        }
        return true;
    }

    @Override
    public void build() {
        assert explorer != null;
        assert component != null;
        evaluatorCache = explorer.getEvaluatorCache();
        componentAutomaton = (ComponentAutomaton) component;
        nonDet = SemanticsNonDet.isNonDet(explorer.getModel().getSemantics());
        stochastic = SemanticsStochastic.isStochastic(explorer.getModel().getSemantics());
        weightZero = UtilValue.newValue(TypeWeightTransition.get(), 0);
        automaton = componentAutomaton.getAutomaton();
        typeLocation = TypeLocation.get(automaton.getLocations());
        buildTypeEdge();
        probabilitySum = TypeWeightTransition.get().newValue();
        weightValue = TypeWeightTransition.get().newValue();
        prepareVariables();
        prepareProperties();
        name = componentAutomaton.getAutomaton().getName().intern();
        number = componentAutomaton.getAutomaton().getNumber();
        actionFromTo = new int[explorer.getModel().getActionsOrEmpty().size() + 1 + 1];
        cmp = TypeBoolean.get().newValue();
        isZero = ContextValue.get().getEvaluator(OperatorIsZero.IS_ZERO, TypeWeightTransition.get());
        add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeWeightTransition.get(), TypeWeightTransition.get());
        multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, TypeWeightTransition.get(), TypeWeightTransition.get());
    }

    @Override
    public void buildAfterVariables() {
        buildEdgeEvaluators();
        buildTransientValueEvaluators();
        prepareSuccessors();
    }

    private void prepareSuccessors() {
        int maxNumSucc = 0;
        if (nonDet && stochastic) {
            for (int loc = 0; loc < edgeEvaluators.length; loc++) {
                maxNumSucc = Math.max(maxNumSucc, edgeEvaluators[loc].length);
                for (int i = 0; i < edgeEvaluators[loc].length; i++) {
                    maxNumSucc = Math.max(maxNumSucc,
                            edgeEvaluators[loc][i].getNumDestinations());
                }
            }
        } else {
            for (int loc = 0; loc < edgeEvaluators.length; loc++) {
                int locMax = 0;
                for (int i = 0; i < edgeEvaluators[loc].length; i++) {
                    locMax += Math.max(maxNumSucc,
                            edgeEvaluators[loc][i].getNumDestinations());
                }
                maxNumSucc = Math.max(maxNumSucc, locMax);
            }			
        }
        successors = new NodeJANI[maxNumSucc];
        for (int succ = 0; succ < maxNumSucc; succ++) {
            successors[succ] = newNode();
        }
    }

    private void prepareVariables() {
        StateVariables stateVariables = explorer.getStateVariables();
        if (typeLocation.getNumValues() > 1) {
            Expression locationIdentifier = new ExpressionIdentifierStandard.Builder()
                    .setName(LOCATION_IDENTIFIER)
                    .setScope(this)
                    .build();
            locationVarNr = stateVariables.add(new StateVariable.Builder().setIdentifier(locationIdentifier).setType(typeLocation).setPermanent(true).build());
        } else {
            locationVarNr = -1;
        }
        if (nonDet && stochastic) {
            Expression edgeIdentifier = new ExpressionIdentifierStandard.Builder()
                    .setName(EDGE_IDENTIFIER)
                    .setScope(this)
                    .build();
            edgeVarNr = stateVariables.add(new StateVariable.Builder().setIdentifier(edgeIdentifier).setType(typeEdge).setPermanent(false).setDecision(true).build());
        }
        for (Variable variable : automaton.getVariablesOrEmpty()) {
            Expression identifier = new ExpressionIdentifierStandard.Builder()
                    .setName(variable.getName())
                    .setScope(this)
                    .build();
            boolean store = !variable.isTransient();
            variableToNumber.put(variable, stateVariables.add(new StateVariable.Builder().setIdentifier(identifier).setType(variable.toType()).setPermanent(store).setInitialValue(variable.getInitialValueOrNull()).build()));
            autVarToLocal.put(variable.getIdentifier(), identifier);
        }
        for (Variable variable : explorer.getModel().getGlobalVariablesOrEmpty()) {
            if (variable.isTransient() && !stateVariables.contains(variable.getIdentifier())) {
                continue;
            }
            variableToNumber.put(variable, stateVariables.getVariableNumber(variable.getIdentifier()));
        }
    }

    /**
     * Build the type storing the edge number of an automaton.
     */
    private void buildTypeEdge() {
        Object2IntOpenHashMap<Location> locationNumEdges = new Object2IntOpenHashMap<>();
        for (Edge edge : automaton.getEdges()) {
            Location location = edge.getLocation();
            locationNumEdges.put(location, locationNumEdges.getInt(location) + 1);
        }
        int[] maxNumEdges = new int[1];
        locationNumEdges.forEach((a,b) -> {
            maxNumEdges[0] = Math.max(maxNumEdges[0], b);
        });
        typeEdge = TypeInteger.get(-1, maxNumEdges[0] - 1);
    }

    /**
     * Build the edge evaluators of the component explorer
     * 
     */
    private void buildEdgeEvaluators() {
        Map<Action,Integer> actionToInteger = computeActionToInteger();
        Locations locations = automaton.getLocations();
        int[] locationsNumEdges = new int[locations.size()];
        List<Edge> edges = resortEdges(automaton.getEdges());
        for (Edge edge : automaton.getEdges()) {
            locationsNumEdges[typeLocation.getNumber(edge.getLocation())]++;
        }
        edgeEvaluators = new EdgeEvaluator[locations.size()][];
        for (int locNr = 0; locNr < locations.size(); locNr++) {
            edgeEvaluators[locNr] = new EdgeEvaluator[locationsNumEdges[locNr]];
        }
        Arrays.fill(locationsNumEdges, 0);
        ExpressionToTypeAutomaton expressionToType = new ExpressionToTypeAutomaton(this.variableToNumber.keySet());
        ContextExpressionSimplifier simplifier = new ContextExpressionSimplifier(expressionToType, evaluatorCache);
        for (Edge edge : edges) {
            Location location = edge.getLocation();
            int locNr = typeLocation.getNumber(location);
            EdgeEvaluator edgeEvaluator = new EdgeEvaluator.Builder()
                    .setEvaluatorCache(evaluatorCache)
                    .setActionNumbers(actionToInteger)
                    .setEdge(edge)
                    .setVariables(explorer.getStateVariables().getIdentifiersArray())
                    .setVariablesMap(variableToNumber)
                    .setLocationVariable(locationVarNr)
                    .setTypeLocation(typeLocation)
                    .setAutVarToLocal(autVarToLocal)
                    .setExpressionToType(expressionToType)
                    .setSimplifier(simplifier)
                    .build();
            edgeEvaluators[locNr][locationsNumEdges[locNr]] = edgeEvaluator;
            locationsNumEdges[locNr]++;
        }
    }

    private void buildTransientValueEvaluators() {
        locationEvaluators = new AssignmentsEvaluator[automaton.getLocations().size()];
        int index = 0;
        ExpressionToTypeAutomaton expressionToType = new ExpressionToTypeAutomaton(this.variableToNumber.keySet());
        ContextExpressionSimplifier simplifier = new ContextExpressionSimplifier(expressionToType, evaluatorCache);
        for (Location location : automaton.getLocations()) {
            locationEvaluators[index] = new AssignmentsEvaluator.Builder()
                    .setAssignments(location.getTransientValueAssignmentsOrEmpty())
                    .setAutVarToLocal(autVarToLocal)
                    .setExpressionToType(expressionToType)
                    .setVariableMap(variableToNumber)
                    .setVariables(explorer.getStateVariables().getIdentifiersArray())
                    .setSimplifier(simplifier)
                    .setEvaluatorCache(evaluatorCache)
                    .build();
            index++;
        }
    }

    private List<Edge> resortEdges(Edges edges) {
        assert edges != null;
        ModelJANI model = edges.getModel();
        assert model != null;
        List<List<Edge>> actionToEdges = new ArrayList<>();
        for (int actNr = 0; actNr < model.getActionsOrEmpty().size() + 1; actNr++) {
            actionToEdges.add(new ArrayList<>());
        }
        Map<Action, Integer> map = UtilExplorer.computeActionToInteger(model);
        for (Edge edge : edges) {
            int action = map.get(edge.getActionOrSilent());
            actionToEdges.get(action).add(edge);
        }
        List<Edge> result = new ArrayList<>();
        for (List<Edge> actionEdges : actionToEdges) {
            result.addAll(actionEdges);
        }
        return result;
    }

    private Map<Action, Integer> computeActionToInteger() {
        return UtilExplorer.computeActionToInteger(explorer.getModel());
    }

    /**
     * Prepare the graph, node, and edge properties of this explorer.
     */
    private void prepareProperties() {
        PropertyNodeGeneral state = new PropertyNodeGeneral(this, TypeBoolean.get());
        if (!nonDet) {
            state.set(true);
        }
        this.state = state;
        weight = new PropertyEdgeGeneral(this, TypeWeightTransition.get());
        label = new PropertyEdgeAction(explorer);
    }

    @Override
    public Value getGraphProperty(Object property) {
        assert property != null;
        return null;
    }

    @Override
    public PropertyNode getNodeProperty(Object property) {
        assert property != null;
        if (property == CommonProperties.STATE) {
            return state;
        }
        return null;
    }

    @Override
    public PropertyEdge getEdgeProperty(Object property) {
        assert property != null;
        if (property == CommonProperties.WEIGHT) {
            return weight;
        } else if (property == CommonProperties.TRANSITION_LABEL) {
            return label;
        }
        return null;
    }

    @Override
    public NodeJANI newNode() {
        return explorer.newNode();
    }

    @Override
    public Collection<NodeJANI> getInitialNodes() {
        Expression initialExpression = automaton.getInitialStatesExpressionOrTrue();
        Expression bounds = UtilModelParser.restrictToVariableRange(automaton.getVariablesOrEmpty());
        initialExpression = UtilExpressionStandard.opAnd(initialExpression, bounds);
        initialExpression = automaton.getModel().replaceConstants(initialExpression);
        VariableValuesEnumerator enumerator = new VariableValuesEnumerator();
        enumerator.setExpression(initialExpression);
        enumerator.setVariables(automaton.getVariablesNonTransient());
        enumerator.setExpressionToType(explorer);
        List<Map<Variable, Value>> enumerated = enumerator.enumerate();
        List<NodeJANI> result = new ArrayList<>();
        for (Location initialLocation : automaton.getInitialLocations()) {
            for (Map<Variable, Value> initialValuesMap : enumerated) {
                NodeJANI initialNode = newNode();
                if (locationVarNr != -1) {
                    initialNode.setVariable(locationVarNr, initialLocation);
                }
                if (nonDet && stochastic) {
                    initialNode.setVariable(edgeVarNr, -1);
                }
                for (Entry<Variable, Value> entry : initialValuesMap.entrySet()) {
                    int varNr = variableToNumber.get(entry.getKey());
                    initialNode.setVariable(varNr, entry.getValue());
                }
//                initialNode.unmark();
                result.add(initialNode);
            }
        }
        return result;
    }

    @Override
    public void queryNode(NodeJANI node) {
        if (nonDet && stochastic) {
            queryNondetStochastic(node);
        } else if (nonDet && !stochastic) {
            queryNondetNonStochastic(node);			
        } else {
            queryNoNondet(node);
        }
    }

    private void queryNoNondet(NodeJANI node) {
        numSuccessors = 0;
        assert node != null;
        Value[] nodeValues = node.getValues();
        int location;
        if (locationVarNr == -1) {
            location = 0;
        } else {
            location = ValueLocation.as(nodeValues[locationVarNr]).getValueNumber();
        }
        locationEvaluators[location].apply(node, node);
        EdgeEvaluator[] locationEdgeEvaluators = edgeEvaluators[location];
        int lastAction = 0;
        actionFromTo[0] = 0;
        for (EdgeEvaluator evaluator : locationEdgeEvaluators) {
            evaluator.setVariableValues(nodeValues);
        }
        for (EdgeEvaluator evaluator : locationEdgeEvaluators) {
            if (evaluator.evaluateGuard()) {
                Value rate = evaluator.hasRate() ? evaluator.evaluateRate() : null;
                int numDestinations = evaluator.getNumDestinations();
                for (int destNr = 0; destNr < numDestinations; destNr++) {
                    DestinationEvaluator destinationEval = evaluator.getDestinationEvaluator(destNr);
                    NodeJANI successor = successors[numSuccessors];
                    successor.unmark();
                    //					successor.set(node);
                    ValueAlgebra probability = ValueAlgebra.as(destinationEval.evaluateProbability(node));
                    isZero.apply(cmp, probability);
                    if (cmp.getBoolean()) {
                        continue;
                    }
                    if (rate != null) {
                        multiply.apply(weightValue, rate, probability);
                        this.weight.set(numSuccessors, weightValue);
                    } else {
                        this.weight.set(numSuccessors, probability);
                    }
                    add.apply(probabilitySum, probabilitySum, probability);
                    int action = evaluator.getAction();
                    label.set(numSuccessors, action);
                    destinationEval.assignTo(node, successor);
                    successor.setNotSet(node);
                    if (lastAction != action) {
                        for (int act = lastAction + 1; act <= action; act++) {
                            actionFromTo[act] = numSuccessors;
                        }
                        lastAction = action;
                    }
                    numSuccessors++;
                }
            }
        }
        for (int act = lastAction + 1; act < actionFromTo.length; act++) {
            actionFromTo[act] = numSuccessors;
        }
    }

    private void queryNondetStochastic(NodeJANI node) {
        assert node != null;
        numSuccessors = 0;
        Value[] nodeValues = node.getValues();
        int edge = ValueInteger.as(nodeValues[edgeVarNr]).getInt();
        int location;
        if (locationVarNr == -1) {
            location = 0;
        } else {
            location = ValueLocation.as(nodeValues[locationVarNr]).getValueNumber();
        }
        EdgeEvaluator[] locationEdgeEvaluators = edgeEvaluators[location];
        if (edge == -1) {
            /* the node queries is a state node */
            state.set(true);
            int edgeNr = 0;
            int lastAction = 0;
            actionFromTo[0] = 0;
            locationEvaluators[location].apply(node, node);
            for (EdgeEvaluator evaluator : locationEdgeEvaluators) {
                evaluator.setVariableValues(nodeValues);
            }
            for (EdgeEvaluator evaluator : locationEdgeEvaluators) {
                if (evaluator.evaluateGuard()) {
                    NodeJANI successor = successors[numSuccessors];
                    successor.unmark();
                    successor.set(node);
                    successor.setVariable(this.edgeVarNr, edgeNr);
                    weight.set(numSuccessors, weightZero);
                    int action = evaluator.getAction();
                    label.set(numSuccessors, action);
                    if (lastAction != action) {
                        for (int act = lastAction + 1; act <= action; act++) {
                            actionFromTo[act] = numSuccessors;
                        }
                        lastAction = action;
                    }
                    numSuccessors++;
                }
                edgeNr++;
            }
            for (int act = lastAction + 1; act < actionFromTo.length; act++) {
                actionFromTo[act] = numSuccessors;
            }
        } else {
            /* the node queried is a distribution node */
            probabilitySum.set(0);
            state.set(false);
            EdgeEvaluator evaluator = locationEdgeEvaluators[edge];
            Value rate = evaluator.hasRate() ? evaluator.evaluateRate() : null;
            int numDestinations = evaluator.getNumDestinations();
            for (int destNr = 0; destNr < numDestinations; destNr++) {
                DestinationEvaluator destinationEval = evaluator.getDestinationEvaluator(destNr);
                NodeJANI successor = successors[numSuccessors];
                successor.unmark();
                //				successor.set(nodeAutomaton);
                successor.setVariable(edgeVarNr, -1);
                ValueAlgebra probability = destinationEval.evaluateProbability(node);
                isZero.apply(cmp, probability);
                if (cmp.getBoolean()) {
                    continue;
                }
                if (rate != null) {
                    multiply.apply(weightValue, rate, probability);
                    weight.set(numSuccessors, weightValue);
                } else {
                    weight.set(numSuccessors, probability);
                }
                add.apply(probabilitySum, probabilitySum, probability);
                label.set(numSuccessors, 0);
                destinationEval.assignTo(node, successor);
                successor.setNotSet(node);
                numSuccessors++;
            }
        }
        for (ExplorerExtension extension : explorer.getExtensions()) {
            extension.afterQueryAutomaton(this);
        }
    }

    private void queryNondetNonStochastic(NodeJANI node) {
        numSuccessors = 0;
        Value[] nodeValues = node.getValues();
        int location = ValueInteger.as(nodeValues[locationVarNr]).getInt();
        EdgeEvaluator[] locationEdgeEvaluators = edgeEvaluators[location];
        int lastAction = 0;
        actionFromTo[0] = 0;
        locationEvaluators[location].apply(node, node);
        for (EdgeEvaluator evaluator : locationEdgeEvaluators) {
            evaluator.setVariableValues(nodeValues);
        }
        for (EdgeEvaluator evaluator : locationEdgeEvaluators) {
            if (evaluator.evaluateGuard()) {
                int action = evaluator.getAction();
                label.set(numSuccessors, action);
                DestinationEvaluator destinationEval = evaluator.getDestinationEvaluator(0);
                NodeJANI successor = successors[numSuccessors];
                successor.unmark();
                destinationEval.assignTo(node, successor);
                successor.setNotSet(node);
                if (lastAction != action) {
                    for (int act = lastAction + 1; act <= action; act++) {
                        actionFromTo[act] = numSuccessors;
                    }
                    lastAction = action;
                }
                numSuccessors++;
            }
        }
        for (int act = lastAction + 1; act < actionFromTo.length; act++) {
            actionFromTo[act] = numSuccessors;
        }
    }


    @Override
    public int getNumSuccessors() {
        return numSuccessors;
    }

    @Override
    public NodeJANI getSuccessorNode(int succNr) {
        assert succNr >= 0;
        assert succNr < numSuccessors;
        return successors[succNr];
    }

    @Override
    public ExplorerJANI getExplorer() {
        return explorer;
    }

    @Override
    public int getNumNodeBits() {
        return explorer.getNumNodeBits();
    }

    @Override
    public void setNumSuccessors(int numSuccessors) {
        this.numSuccessors = numSuccessors;
    }

    @Override
    public boolean isState(NodeJANI node) {
        Value[] nodeValues = node.getValues();
        int edge = ValueInteger.as(nodeValues[edgeVarNr]).getInt();
        return edge == -1;
    }

    @Override
    public boolean isState() {
        return state.getBoolean();
    }

    public boolean isNonDet() {
        return nonDet;
    }

    public PropertyEdgeGeneral getWeight() {
        return weight;
    }

    public ValueAlgebra getProbabilitySum() {
        return probabilitySum;
    }

    public PropertyEdgeAction getActions() {
        return label;
    }

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }

    public int getActionFrom(int action) {
        return actionFromTo[action];
    }

    public int getActionTo(int action) {
        return actionFromTo[action + 1];
    }

    @Override
    public void close() {
    }
}
