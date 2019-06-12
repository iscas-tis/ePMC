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

import static epmc.error.UtilError.ensure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import epmc.expression.evaluatorexplicit.EvaluatorCache;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expression.standard.RewardSpecification;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.graph.CommonProperties;
import epmc.graph.LowLevel;
import epmc.graph.Semantics;
import epmc.graph.SemanticsNonDet;
import epmc.graph.explorer.Explorer;
import epmc.graph.explorer.ExplorerEdgeProperty;
import epmc.graph.explorer.ExplorerNode;
import epmc.graph.explorer.ExplorerNodeProperty;
import epmc.jani.model.ModelExtension;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.OptionsJANIModel;
import epmc.jani.model.Variable;
import epmc.jani.model.property.ExpressionDeadlock;
import epmc.jani.model.property.ExpressionInitial;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Engine;
import epmc.modelchecker.EngineExplorer;
import epmc.modelchecker.Log;
import epmc.modelchecker.Model;
import epmc.options.Options;
import epmc.util.Util;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.TypeObject;
import epmc.value.Value;

// TODO should implement flattening, because of potential performance gain
// TODO improve robustness, e.g. check constants, duplicate variables, etc
// TODO improve special case check for initial states
// TODO integrate expression simplification (partly done)
// TODO extensions for SMG, IMDP, QMC, plus according web pages (partly done)
// TODO complete PTA stuff
// TODO improve node performance
// TODO get PRISM to run and integrate call to this tool for comparison

/**
 * Explicit-state low-level semantics explorer for JANI models.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ExplorerJANI implements Explorer {
    public final static class Builder implements LowLevel.Builder {
        private Model model;
        private Engine engine;
        private final Set<Object> graphProperties = new LinkedHashSet<>();
        private final Set<Object> nodeProperties = new LinkedHashSet<>();
        private final Set<Object> edgeProperties = new LinkedHashSet<>();

        @Override
        public Builder setModel(Model model) {
            this.model = model;
            return this;
        }

        @Override
        public Builder setEngine(Engine engine) {
            this.engine = engine;
            return this;
        }

        @Override
        public Builder addGraphProperties(Set<Object> graphProperties) {
            this.graphProperties.addAll(graphProperties);
            return this;
        }

        @Override
        public Builder addNodeProperties(Set<Object> nodeProperties) {
            this.nodeProperties.addAll(nodeProperties);
            return this;
        }

        @Override
        public Builder addEdgeProperties(Set<Object> edgeProperties) {
            this.edgeProperties.addAll(edgeProperties);
            return this;
        }

        @Override
        public ExplorerJANI build() {
            if (!(engine instanceof EngineExplorer)) {
                return null;
            }
            if (!(model instanceof ModelJANI)) {
                return null;
            }
            return new ExplorerJANI(this);
        }
    }
    
    public final static String IDENTIFIER = "jani-explorer";
    
    private final static String INITIAL_IDENTIFIER = "\"init\"";

    /** Model which this explorer is supposed to explore. */
    private final ModelJANI model;
    /** The top-level explorer of the system composition. */
    private final ExplorerComponent system;
    /** Model semantics in {@link Value} form. */
    private final Value semantics;
    /** Transition transient values properties. */
    Map<Expression,PropertyEdgeTransientValue> transitionTransientValuesMap = new LinkedHashMap<>();
    /** Node transient value properties. */
    Map<Expression,PropertyNodeTransientValue> nodeTransientValuesMap = new LinkedHashMap<>();
    /** Initial nodes of the model. */
    private final Collection<NodeJANI> initialNodes;
    /** Whether to allow and automatically fix deadlocks. */
    private boolean isDeadlock;
    private final boolean fixDeadlocks;
    private final Map<Expression,PropertyNodeExpression> expressionNodeProperties = new LinkedHashMap<>();
    private final Map<Expression,PropertyEdgeExpression> expressionEdgeProperties = new LinkedHashMap<>();
    private final Map<Expression,PropertyNodeConstant> constantProperies = new LinkedHashMap<>();
    /** Extensions used by this explorer. */
    private final ExplorerExtension[] extensions;
    /** All variables in nodes of this explorer. */
    private final StateVariables stateVariables = new StateVariables();
    /** For nondeterministic model stores whether node is special self loop node. */
    private int selfLoopVariable;
    /** TransientValues variables used. */
    private int[] transientValuesVariables;
    /** Whether this model is nondeterministic. */
    private final boolean nonDet;
    /** Node which was queried last. */
    private NodeJANI queriedNode;
    /** Initial nodes property. */
    private final PropertyNodeInitialNodes initNodesProp;
    private final PropertyNodeDeadlock deadlockNodesProp;
    private final PropertyNodeState stateProp;
    private final EvaluatorCache evaluatorCache = new EvaluatorCache();

    private boolean state;

    private ExplorerJANI(Builder builder) {
        this((ModelJANI) builder.model, builder.graphProperties, builder.nodeProperties, builder.edgeProperties);
    }

    /**
     * Construct new model explorer.
     * The model parameter may not be {@code null}. The model must already have
     * been parsed.
     * 
     * @param model model for which to construct an explorer
     * @param edgeProperties 
     * @param nodeProperties 
     * @param graphProperties 
     */
    public ExplorerJANI(ModelJANI model,
            Set<Object> graphProperties,
            Set<Object> nodeProperties,
            Set<Object> edgeProperties) {
        assert model != null;
        assert graphProperties != null;
        assert nodeProperties != null;
        assert edgeProperties != null;
        // TODO check validity of node properties already here
        getLog().send(MessagesJANIExplorer.START_BUILDING_EXPLORER);
        ensureNoUndefinedConstants(model);
        
        this.model = model;
        semantics = new TypeObject.Builder()
                .setClazz(Semantics.class)
                .build().newValue(model.getSemantics());
        nonDet = SemanticsNonDet.isNonDet(model.getSemantics());
        prepareGlobalVariables(model);
        buildTransientValues(nodeProperties, edgeProperties);
        system = prepareSystem(model);
        extensions = prepareExtensions(model);
        afterSystemCreation();
        initialNodes = computeInitialNodes();
        fixDeadlocks = Options.get().getBoolean(OptionsJANIModel.JANI_FIX_DEADLOCKS);
        prepareGraphProperties();
        prepareNodeProperties();
        prepareEdgeProperties();
        initNodesProp = new PropertyNodeInitialNodes(this);
        deadlockNodesProp = new PropertyNodeDeadlock(this);
        stateProp = new PropertyNodeState(this);
        this.state = true;
        getLog().send(MessagesJANIExplorer.DONE_BUILDING_EXPLORER);
    }

    private static void ensureNoUndefinedConstants(ModelJANI model) {
        String undefinedConstantsString = model.findUndefinedConstants().toString();
        if (model.containsUndefinedConstants()) {
            undefinedConstantsString = undefinedConstantsString.substring(1, undefinedConstantsString.length() - 1);
        }
        ensure(!model.containsUndefinedConstants(),
                ProblemsJANIExplorer.JANI_EXPLORER_UNDEFINED_CONSTANTS,
                undefinedConstantsString);
        
    }

    private void afterSystemCreation() {
        for (ExplorerExtension extension : extensions) {
            extension.afterSystemCreation();
        }
    }

    private void prepareGraphProperties() {
        // TODO Auto-generated method stub

    }

    private void prepareNodeProperties() {
        // TODO Auto-generated method stub

    }


    private void prepareEdgeProperties() {
        // TODO Auto-generated method stub

    }

    public int getSelfLoopVariable() {
        return selfLoopVariable;
    }

    private void prepareGlobalVariables(ModelJANI model) {
        assert model != null;
        if (nonDet) {
            Expression selfLoopIdentifier = new ExpressionIdentifierStandard.Builder()
                    .setName("%selfLoopId")
                    .build();
            TypeBoolean typeBoolean = TypeBoolean.get();
            selfLoopVariable = stateVariables.add(new StateVariable.Builder().setIdentifier(selfLoopIdentifier).setType(typeBoolean).setPermanent(false).build());
        }
        for (Variable variable : model.getGlobalVariablesOrEmpty()) {
            boolean store = !variable.isTransient();
            stateVariables.add(new StateVariable.Builder().setIdentifier(variable.getIdentifier()).setType(variable.getType().toType()).setPermanent(store).setInitialValue(model.replaceConstantsOrNull(variable.getInitialValueOrNull())).build());
        }
    }

    private ExplorerComponent prepareSystem(ModelJANI model2) {
        PreparatorComponentExplorer preparator = new PreparatorComponentExplorer();
        ExplorerComponent result = preparator.prepare(this, model.getSystem());
        result.buildAfterVariables();
        return result;
    }

    public StateVariables getStateVariables() {
        return stateVariables;
    }

    private ExplorerExtension[] prepareExtensions(ModelJANI model) {
        assert model != null;
        int size = 0;
        Options options = Options.get();
        List<ModelExtension> modelExtensions = new ArrayList<>();
        modelExtensions.add(model.getSemanticsExtension());
        modelExtensions.addAll(model.getModelExtensionsOrEmpty());
        Map<String,Class<? extends ExplorerExtension>> explorerExtensions = options.get(OptionsJANIExplorer.JANI_EXPLORER_EXTENSION_CLASS);
        for (ModelExtension modelExtension : modelExtensions) {
            if (explorerExtensions.containsKey(modelExtension.getIdentifier())) {
                size++;
            }
        }

        ExplorerExtension[] result = new ExplorerExtension[size];
        int index = 0;
        for (ModelExtension modelExtension : modelExtensions) {
            if (explorerExtensions.containsKey(modelExtension.getIdentifier())) {
                Class<? extends ExplorerExtension> clazz = explorerExtensions.get(modelExtension.getIdentifier());
                result[index] = Util.getInstance(clazz);
                result[index].setModelExtension(modelExtension);
                result[index].setExplorer(this);
                index++;
            }
        }
        return result;
    }

    /**
     * Build transient values structures.
     * @param edgeProperties 
     * @param nodeProperties 
     */
    private void buildTransientValues(Set<Object> nodeProperties, Set<Object> edgeProperties) {
        assert model != null;
        Set<Expression> usedTransientValues = new HashSet<>();
        for (Object property : nodeProperties) {
            if (property instanceof RewardSpecification) {
                RewardSpecification specification = (RewardSpecification) property;
                usedTransientValues.add(specification.getExpression());
            }
        }
        for (Object property : edgeProperties) {
            if (property instanceof RewardSpecification) {
                RewardSpecification specification = (RewardSpecification) property;
                usedTransientValues.add(specification.getExpression());
            }
        }
        transientValuesVariables = new int[usedTransientValues.size()];
        int trvNr = 0;
        for (Variable transientValue : model.getGlobalVariablesTransient()) {
            if (!usedTransientValues.contains(transientValue.getIdentifier())) {
                continue;
            }
            transientValuesVariables[trvNr] = stateVariables.getVariableNumber(transientValue.getIdentifier());
            //			transientValuesVariables[trvNr] = stateVariables.addVariable(transientValue.getIdentifier(), transientValue.getType().toType(), false);
            trvNr++;
        }

        int transientValueNr = 0;
        for (Variable transientValue : model.getGlobalVariablesTransient()) {
            if (!usedTransientValues.contains(transientValue.getIdentifier())) {
                continue;
            }
            int trvVarNr = transientValuesVariables[transientValueNr];
            String name = transientValue.getName();
            PropertyEdgeTransientValue edgeProperty = new PropertyEdgeTransientValue(this, trvVarNr);
            PropertyNodeTransientValue nodeProperty = new PropertyNodeTransientValue(this, trvVarNr);
            Expression identifier = new ExpressionIdentifierStandard.Builder()
                    .setName(name)
                    .build();
            transitionTransientValuesMap.put(identifier, edgeProperty);
            nodeTransientValuesMap.put(identifier, nodeProperty);
            transientValueNr++;
        }
    }

    /**
     * Compute initial nodes of the model.
     * 
     * @return initial nodes of the model
     */
    private Collection<NodeJANI> computeInitialNodes() {
        getLog().send(MessagesJANIExplorer.START_BUILDING_INITIAL_STATES_EXPLORER);
        Expression initialExpression = model.getInitialStatesExpressionOrTrue();
        initialExpression = model.replaceConstants(initialExpression);
        VariableValuesEnumerator enumerator = new VariableValuesEnumerator();
        enumerator.setExpression(initialExpression);
        enumerator.setExpressionToType(model);
        enumerator.setVariables(model.getGlobalVariablesNonTransient());
        List<Map<Variable, Value>> enumerated = enumerator.enumerate();
        Collection<NodeJANI> innerNodes = system.getInitialNodes();
        Collection<NodeJANI> result = new ArrayList<>();
        epmc.value.EvaluatorCache evaluatorCache = new epmc.value.EvaluatorCache();
        for (NodeJANI innerNode : innerNodes) {
            NodeJANI node = newNode();
            node.set(innerNode);
            for (Map<Variable, Value> globals : enumerated) {
                NodeJANI nodeWithGlobals = node.clone(evaluatorCache);
                for (Variable variable : model.getGlobalVariablesNonTransient()) {
                    Value value = globals.get(variable);
                    int nodeVarNr = stateVariables.getVariableNumber(variable.getIdentifier());
                    nodeWithGlobals.setVariable(nodeVarNr, value);
                }
                nodeWithGlobals.unmark();
                result.add(nodeWithGlobals);
            }
        }
        getLog().send(MessagesJANIExplorer.DONE_BUILDING_INITIAL_STATES_EXPLORER);
        return result;
    }

    @Override
    public Collection<NodeJANI> getInitialNodes() {
        return initialNodes;
    }

    @Override
    public void queryNode(ExplorerNode node) {
        assert node != null;
        assert node instanceof NodeJANI;
        NodeJANI nodeJANI = (NodeJANI) node;
        this.queriedNode = nodeJANI;
        nodeJANI.unmark();
        isDeadlock = system.getNumSuccessors() == 0;
        if (nonDet && nodeJANI.getBoolean(selfLoopVariable)) {
            system.setNumSuccessors(1);
            system.getSuccessorNode(0).setVariable(selfLoopVariable, false);
            system.getSuccessorNode(0).unmark();
            for (ExplorerExtension extension : extensions) {
                extension.handleSelfLoop(nodeJANI);
            }
            state = false;
        } else {
            for (ExplorerExtension extension : extensions) {
                extension.beforeQuerySystem(nodeJANI);
            }
            system.queryNode(nodeJANI);
            for (PropertyNodeExpression prop : expressionNodeProperties.values()) {
                prop.setVariableValues(nodeJANI.getValues());
            }
            for (ExplorerExtension extension : extensions) {
                extension.afterQuerySystem(nodeJANI);
            }
            int innerNumSuccessors = system.getNumSuccessors();
            ensure(fixDeadlocks || innerNumSuccessors > 0, ProblemsJANIExplorer.JANI_EXPLORER_DEADLOCK);
            state = nonDet ? system.isState() : true;
            for (int succNr = 0; succNr < innerNumSuccessors; succNr++) {
                for (PropertyEdgeExpression prop : expressionEdgeProperties.values()) {
                    prop.setVariableValues(system.getSuccessorNode(succNr).getValues(), succNr);
                }
            }
        }
        if (fixDeadlocks && system.getNumSuccessors() == 0 && nonDet) {
            system.setNumSuccessors(1);
            NodeJANI successor = system.getSuccessorNode(0);
            successor.set(nodeJANI);
            successor.setVariable(selfLoopVariable, true);
            successor.unmark();
            for (ExplorerExtension extension : extensions) {
                extension.handleNoSuccessors(nodeJANI);
            }
        } else if (fixDeadlocks && system.getNumSuccessors() == 0 && !nonDet) {
            system.setNumSuccessors(1);
            NodeJANI successor = system.getSuccessorNode(0);
            successor.set(nodeJANI);
            successor.unmark();
        }
    }

    public boolean isFixDeadlocks() {
        return fixDeadlocks;
    }

    public boolean isDeadlock() {
        return isDeadlock;
    }

    public ExplorerComponent getExplorerSystem() {
        return system;
    }

    @Override
    public int getNumSuccessors() {
        return system.getNumSuccessors();
    }

    @Override
    public NodeJANI getSuccessorNode(int number) {
        return system.getSuccessorNode(number);
    }

    @Override
    public Value getGraphProperty(Object property) {
        assert property != null;
        for (ExplorerExtension extension : extensions) {
            Value graphProperty = extension.getGraphProperty(property);
            if (graphProperty != null) {
                return graphProperty;
            }
        }
        if (property == CommonProperties.SEMANTICS) {
            return semantics;
        }
        return system.getGraphProperty(property);
    }

    @Override
    public ExplorerNodeProperty getNodeProperty(Object property) {
        assert property != null;
        for (ExplorerExtension extension : extensions) {
            ExplorerNodeProperty nodeProperty = extension.getNodeProperty(property);
            if (nodeProperty != null) {
                return nodeProperty;
            }
        }
        if (property == CommonProperties.STATE) {
            return stateProp;
        }
        if ((property instanceof Expression)
                && ExpressionIdentifier.is(property)
                && property.toString().equals(INITIAL_IDENTIFIER)) {
            return initNodesProp;
        }
        if (property instanceof ExpressionInitial) {
            return initNodesProp;
        }
        if (property instanceof ExpressionDeadlock) {
            return deadlockNodesProp;
        }
        if (property instanceof Expression) {
            if (model.getConstants().containsKey(property)) {
                ensure(model.getConstants().get(property) != null,
                        ProblemsJANIExplorer.JANI_EXPLORER_UNDEFINED_CONSTANT,
                        UtilExpressionStandard.niceForm((Expression) property));
                return getConstantExpressionNodeProperty((Expression) property);
            }
            PropertyNodeExpression result = expressionNodeProperties.get(property);
            if (result == null) {
                Type type = stateVariables.get((Expression) property).getType();
                result = new PropertyNodeExpression(this, stateVariables.getIdentifiersArray(), (Expression) property, type);
                expressionNodeProperties.put((Expression) property, result);
            }
            return result;
        }
        if (property instanceof RewardSpecification) {
            RewardSpecification specification = (RewardSpecification) property;
            ExplorerNodeProperty result = nodeTransientValuesMap.get(specification.getExpression());
            if (result != null) {
                return result;
            } else {
                result = new PropertyNodeExpression(this, stateVariables.getIdentifiersArray(), specification.getExpression(), null);
                expressionNodeProperties.put(specification.getExpression(), (PropertyNodeExpression) result);
                return result;
            }
        }
        return null;
    }

    private ExplorerNodeProperty getConstantExpressionNodeProperty(Expression property) {
        assert property != null;
        PropertyNodeConstant result = constantProperies.get(property);
        if (result != null) {
            return result;
        }
        Map<Expression,Expression> constants = model.getConstants();
        Expression constantValue = constants.get(property);
        assert constantValue != null : property;
        Value value = UtilEvaluatorExplicit.evaluate(constantValue);
        result = new PropertyNodeConstant(this, value);
        constantProperies.put(property, result);
        return result;
    }

    @Override
    public ExplorerEdgeProperty getEdgeProperty(Object property) {
        assert property != null;
        for (ExplorerExtension extension : extensions) {
            ExplorerEdgeProperty edgeProperty = extension.getEdgeProperty(property);
            if (edgeProperty != null) {
                return edgeProperty;
            }
        }
        if (property instanceof RewardSpecification) {
            // TODO fix for rewards not directly specified with transient vars
            RewardSpecification specification = (RewardSpecification) property;
            ExplorerEdgeProperty result = transitionTransientValuesMap.get(specification.getExpression());
            if (result != null) {
                return result;
            } else {
                result = new PropertyEdgeExpression(this, stateVariables.getIdentifiersArray(), specification.getExpression(), null);
                expressionEdgeProperties.put(specification.getExpression(), (PropertyEdgeExpression) result);
                return result;
            }
        }
        if (property == CommonProperties.TRANSITION_LABEL) {
            return system.getEdgeProperty(property);
        }
        return null;
    }

    @Override
    public NodeJANI newNode() {
        return new NodeJANI(this, stateVariables);
    }

    @Override
    public int getNumNodeBits() {
        return stateVariables.getNumBits();
    }

    public ModelJANI getModel() {
        return model;
    }

    public ExplorerExtension[] getExtensions() {
        return extensions;
    }

    public NodeJANI getQueriedNode() {
        return queriedNode;
    }

    public boolean isState() {
        return state;
    }

    @Override
    public Type getType(Expression expression) {
        Type type = stateVariables.get(expression).getType();
        if (type != null) {
            return type;
        }
        // TODO Auto-generated method stub
        return Explorer.super.getType(expression);
    }

    /**
     * Get log used for analysis.
     * 
     * @return log used for analysis
     */
    private Log getLog() {
        return Options.get().get(OptionsMessages.LOG);
    }

    @Override
    public void close() {
    }
    
    public EvaluatorCache getEvaluatorCache() {
        return evaluatorCache;
    }
}
