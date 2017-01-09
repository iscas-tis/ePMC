package epmc.lumping;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionFilter;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionTemporal;
import epmc.graph.CommonProperties;
import epmc.graph.explicit.GraphExplicit;
import epmc.graphsolver.OptionsGraphsolver;
import epmc.graphsolver.lumping.LumperExplicit;
import epmc.graphsolver.lumping.UtilLump;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitLump;
import epmc.modelchecker.Model;
import epmc.modelchecker.RawProperty;
import epmc.modelchecker.TestHelper;
import epmc.options.Options;
import epmc.util.Util;

import static epmc.expression.standard.ExpressionPropositional.isPropositional;
import static epmc.graph.TestHelperGraph.*;

public final class TestHelperLump {
    public static GraphExplicit computeQuotient(Options options, String modelFile,
            String property) {
        Set<Object> nodeProperties = new HashSet<>();
        nodeProperties.add(CommonProperties.STATE);
        return computeQuotient(options, modelFile, nodeProperties, property);
    }

    public static GraphExplicit computeQuotient(Options options, String modelFile,
            Set<Object> nodeProperties, String property) {
        try {
            Model model = TestHelper.loadModel(options, modelFile);
            assert model != null;
            Map<String,Class<? extends LumperExplicit>> lumpersExplicit = options.get(OptionsGraphsolver.GRAPHSOLVER_LUMPER_EXPLICIT_CLASS);
            LumperExplicit useInstance = null;
            TestHelper.addProperty(model, property);
            RawProperty raw = model.getPropertyList().getRawProperties().iterator().next();
            Expression expr = model.getPropertyList().getParsedProperty(raw);
            Set<Expression> atomics = collectAPs(expr);
            nodeProperties.addAll(atomics);
            GraphExplicit graph = exploreToGraph(model, nodeProperties);
            int[] partition = UtilLump.partitionByAPs(graph, atomics);
            GraphSolverObjectiveExplicitLump objective = new GraphSolverObjectiveExplicitLump();
            objective.setGraph(graph);
            objective.setPartition(partition);
            for (Class<? extends LumperExplicit> clazz : lumpersExplicit.values()) {
                LumperExplicit instance = Util.getInstance(clazz);
                instance.setOriginal(objective);
                if (instance.canLump()) {
                    useInstance = instance;
                    break;
                }
            }
            useInstance.lump();
            return useInstance.getQuotient().getGraph();
        } catch (EPMCException e) {
            throw new RuntimeException(e);
        }
    }

    public static Set<Expression> collectAPs(Expression expression) {
        if (ExpressionFilter.isFilter(expression)) {
            ExpressionFilter expressionFilter = ExpressionFilter.asFilter(expression);
            expression = expressionFilter.getProp();
        }
        if (expression instanceof ExpressionQuantifier) {
            ExpressionQuantifier expressionQuantifier = (ExpressionQuantifier) expression;
            expression = expressionQuantifier.getQuantified();
        }
        Set<Expression> atomics = collectLTLPropositional(expression);
        return atomics;
    }
    
    public static Set<Expression> collectLTLPropositional(Expression expression) {
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
	
	private TestHelperLump() {
	}
}
