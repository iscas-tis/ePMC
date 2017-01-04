package epmc.command;

import static epmc.expression.standard.ExpressionPropositional.isPropositional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionReward;
import epmc.expression.standard.ExpressionTemporal;
import epmc.expression.standard.RewardSpecification;
import epmc.expression.standard.TemporalType;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.graph.CommonProperties;
import epmc.graph.UtilGraph;
import epmc.graph.dd.GraphDD;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explorer.Explorer;
import epmc.graphsolver.OptionsGraphsolver;
import epmc.graphsolver.lumping.LumperDD;
import epmc.graphsolver.lumping.LumperExplicit;
import epmc.graphsolver.lumping.UtilLump;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitLump;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.CommandTask;
import epmc.modelchecker.Engine;
import epmc.modelchecker.EngineDD;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.EngineExplorer;
import epmc.modelchecker.Log;
import epmc.modelchecker.Model;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.ModelCheckerResult;
import epmc.modelchecker.Properties;
import epmc.modelchecker.RawProperty;
import epmc.options.Options;
import epmc.util.Util;

public class CommandTaskLump implements CommandTask {
    public final static String IDENTIFIER = "lump";
    private ModelChecker modelChecker;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        this.modelChecker = modelChecker;
    }

    @Override
    public void executeInServer() {
        try {
			lump();
		} catch (EPMCException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void lump() throws EPMCException {
        Engine engine = modelChecker.getEngine();
        if (engine instanceof EngineExplicit) {
            lumpExplicit();
        } else if (engine instanceof EngineDD) {
            lumpDD();
        } else {
        	assert false;
        	// TODO user exception
        }
    }
    
    private void lumpExplicit() throws EPMCException {
        long time = System.nanoTime();
        Model model = modelChecker.getModel();
    	Options options = model.getContextValue().getOptions();
        Log log = options.get(OptionsMessages.LOG);
        log.send(MessagesCommandLump.EXPLORING);
        Set<Object> graphProperties = new LinkedHashSet<>();
        graphProperties.add(CommonProperties.SEMANTICS);
        Set<Object> nodeProperties = new LinkedHashSet<>();
        Set<Expression> stateLabels = new HashSet<>();
        for (RawProperty property : model.getPropertyList().getRawProperties()) {
            Expression expression = model.getPropertyList().getParsedProperty(property);
            ExpressionQuantifier expressionQuantifier = (ExpressionQuantifier) expression;
            expression = expressionQuantifier.getQuantified();
        	ExpressionTemporal expressionTemporal = (ExpressionTemporal) expression;
            if (isUntil(expression)) {
            	stateLabels.add(expressionTemporal.getOperand1());
            	stateLabels.add(expressionTemporal.getOperand2());
            } else if (isFinally(expression)) {
            	stateLabels.add(expressionTemporal.getOperand1());            	
            }
        }
        nodeProperties.addAll(stateLabels);
        nodeProperties.add(CommonProperties.STATE);
        Set<Object> edgeProperties = new LinkedHashSet<>();
        edgeProperties.add(CommonProperties.WEIGHT);
        
        Explorer explorer = (Explorer) model.newLowLevel(EngineExplorer.getInstance(), graphProperties, nodeProperties, edgeProperties);
        GraphExplicit modelGraph = UtilGraph.buildModelGraphExplicit(explorer, graphProperties, nodeProperties, edgeProperties);
        time = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - time);
        log.send(MessagesCommandLump.EXPLORING_DONE, time);
        
        Map<String,Class<? extends LumperExplicit>> lumpersExplicit = options.get(OptionsGraphsolver.GRAPHSOLVER_LUMPER_EXPLICIT_CLASS);
        Collection<String> lumperExplicitt = options.get(OptionsGraphsolver.GRAPHSOLVER_LUMPER_EXPLICIT);
        ArrayList<String> lumperExplicit = new ArrayList<>(lumperExplicitt);

        for (RawProperty property : model.getPropertyList().getRawProperties()) {
        	for (String lumperId : lumperExplicit) {
        		Class<? extends LumperExplicit> lumperClass = lumpersExplicit.get(lumperId);
        		if (lumperClass == null) {
        			continue;
        		}
        		LumperExplicit lumper = Util.getInstance(lumperClass);
        		lumper.setOriginal(partitionByProperty(modelGraph, model.getPropertyList().getParsedProperty(property)));
        		if (lumper.canLump()) {
        			lumper.lump();
        			GraphExplicit quotient = lumper.getQuotient().getGraph();
        			OutputType outputType = options.get(OptionsCommandLump.LUMP_OUTPUT_TYPE);
        			Object result;
        			if (outputType == OutputType.QUOTIENT) {
        				result = quotient;
        			} else if (outputType == OutputType.STATISTICS) {
        				result = new Statistics(quotient);
        			} else {
        				result = null;
        				assert false;
        			}
        			ModelCheckerResult modelCheckerResult = new ModelCheckerResult(property, result);
        			log.send(modelCheckerResult);
        			break;
        		}
            }
        }
    }
    
    private static GraphSolverObjectiveExplicitLump partitionByProperty(
            GraphExplicit graph, Expression expression) throws EPMCException {
        if (expression instanceof ExpressionQuantifier) {
        	ExpressionQuantifier expressionQuantifier = (ExpressionQuantifier) expression;
            expression = expressionQuantifier.getQuantified();
        }
        Set<Expression> atomics = collectLTLPropositional(expression);
        return UtilLump.partitionByAPsObjective(graph, atomics);
    }

    private static Set<Expression> collectLTLPropositional(Expression expression) {
        if (isPropositional(expression)) {
            return Collections.singleton(expression);
        } else if (expression instanceof ExpressionTemporal) {
        	ExpressionTemporal expressionTemporal = (ExpressionTemporal) expression;
            Set<Expression> result = new LinkedHashSet<>();
            for (Expression inner : expressionTemporal.getOperands()) {
                result.addAll(collectLTLPropositional(inner));
            }
            return result;
        } else if (expression instanceof ExpressionOperator) {
        	ExpressionOperator expressionOperator = (ExpressionOperator) expression;
            Set<Expression> result = new LinkedHashSet<>();
            for (Expression inner : expressionOperator.getOperands()) {
                result.addAll(collectLTLPropositional(inner));
            }
            return result;
        } else {
            return Collections.singleton(expression);           
        }
    }

    private void lumpDD() throws EPMCException {
        Set<Object> graphProperties = new LinkedHashSet<>();
        graphProperties.add(CommonProperties.SEMANTICS);
        graphProperties.add(CommonProperties.EXPRESSION_TO_DD);
        
        Set<Object> nodeProperties = new LinkedHashSet<>();
        Set<Object> edgeProperties = new LinkedHashSet<>();
        Set<Expression> stateLabels = new HashSet<>();
        Model model = modelChecker.getModel();
        for (RawProperty property : model.getPropertyList().getRawProperties()) {
            Expression expression = model.getPropertyList().getParsedProperty(property);
            stateLabels.addAll(UtilExpressionStandard.collectIdentifiers(expression));
        }
        nodeProperties.addAll(stateLabels);
        nodeProperties.addAll(collectRewards(model));
        nodeProperties.add(CommonProperties.STATE);
        edgeProperties.add(CommonProperties.WEIGHT);
        edgeProperties.addAll(collectRewards(model));
    	Options options = modelChecker.getModel().getContextValue().getOptions();
        Log log = options.get(OptionsMessages.LOG);

        GraphDD modelGraph = (GraphDD) model.newLowLevel(EngineDD.getInstance(), graphProperties, nodeProperties, edgeProperties);
//        GraphBuilderDD build = new GraphBuilderDD(modelGraph, new ArrayList<>());
//        GraphExplicit graph = build.buildGraph();
//        build.close();
//        String graphString = GraphExporter.toString(graph);
        if (model.getPropertyList().getRawProperties().size() > 1) {
            for (RawProperty property : model.getPropertyList().getRawProperties()) {
                LumperDD lumper = getLumperForModelAndProp(modelGraph, 
                        Collections.singleton(model.getPropertyList().getParsedProperty(property)));
                if(lumper != null) {
                    lumper.lump();
                    ModelCheckerResult result = new ModelCheckerResult(property, lumper.getQuotient());
                    log.send(result);
                }
            }
        }
    }
    
    public static Set<RewardSpecification> collectRewards(Model model)
            throws EPMCException {
        assert model != null;
        Properties properties = model.getPropertyList();
        Set<RewardSpecification> result = new LinkedHashSet<>();
        for (RawProperty property : properties.getRawProperties()) {
            Expression expression = properties.getParsedProperty(property);
            result.addAll(collectRewards(model, expression));
        }
        return Collections.unmodifiableSet(result);
    }

    private LumperDD getLumperForModelAndProp(GraphDD modelGraph, Collection<Expression> propertyList) throws EPMCException {
    	Options options = modelChecker.getModel().getContextValue().getOptions();
        Collection<String> lumperDD = options.get(OptionsGraphsolver.GRAPHSOLVER_LUMPER_DD);
        assert lumperDD != null;
        lumperDD = new ArrayList<>(lumperDD);
        Map<String,Class<? extends LumperDD>> lumpersDD = options.get(OptionsGraphsolver.GRAPHSOLVER_DD_LUMPER_CLASS);

        for (String lumperId : lumperDD) {
            Class<? extends LumperDD> lumperClass = lumpersDD.get(lumperId);
            if(lumperClass == null) {
                continue;
            }
            LumperDD lumper = Util.getInstance(lumperClass);
            lumper.setModelChecker(modelChecker);
            lumper.setOriginal(modelGraph);
            for (Expression property : propertyList) {
                lumper.requireValidFor(property);
            }
            if (lumper.canLump()) {
                return lumper;
            }
        }
        // No suitable lumper found
        return null;
    }

    private static boolean isFinally(Expression expression) {
        if (!(expression instanceof ExpressionTemporal)) {
            return false;
        }
        ExpressionTemporal expressionTemporal = (ExpressionTemporal) expression;
        return expressionTemporal.getTemporalType() == TemporalType.FINALLY;
    }
    
    private static boolean isUntil(Expression expression) {
        if (!(expression instanceof ExpressionTemporal)) {
            return false;
        }
        ExpressionTemporal expressionTemporal = (ExpressionTemporal) expression;
        return expressionTemporal.getTemporalType() == TemporalType.UNTIL;
    }
    
    static Set<RewardSpecification> collectRewards(Model model, Expression property)
            throws EPMCException {
        assert model != null;
        assert property != null;
        Set<RewardSpecification> result = new LinkedHashSet<>();
        collectRewardsRec(model, property, result);
        return Collections.unmodifiableSet(result);
    }

    private static void collectRewardsRec(Model model, Expression property,
            Set<RewardSpecification> result) throws EPMCException {
        assert model != null;
        assert property != null;
        assert result != null;
        if (ExpressionReward.isReward(property)) {
            ExpressionReward propertyReward = ExpressionReward.asReward(property);
            RewardSpecification structure = propertyReward.getReward();
            result.add(structure);
        } else {
            for (Expression child : property.getChildren()) {
                collectRewardsRec(model, child, result);
            }
        }
    }
}
