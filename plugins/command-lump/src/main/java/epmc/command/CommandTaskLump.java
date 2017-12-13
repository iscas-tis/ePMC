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

package epmc.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionPropositional;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionReward;
import epmc.expression.standard.ExpressionTemporalFinally;
import epmc.expression.standard.ExpressionTemporalGlobally;
import epmc.expression.standard.ExpressionTemporalNext;
import epmc.expression.standard.ExpressionTemporalRelease;
import epmc.expression.standard.ExpressionTemporalUntil;
import epmc.expression.standard.RewardSpecification;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.graph.CommonProperties;
import epmc.graph.dd.GraphDD;
import epmc.graph.explicit.GraphExplicit;
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
import epmc.modelchecker.Log;
import epmc.modelchecker.Model;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.ModelCheckerResult;
import epmc.modelchecker.Properties;
import epmc.modelchecker.RawProperty;
import epmc.modelchecker.UtilModelChecker;
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
        lump();
    }

    public void lump() {
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

    private void lumpExplicit() {
        long time = System.nanoTime();
        Model model = modelChecker.getModel();
        Options options = Options.get();
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
            if (ExpressionTemporalUntil.is(expression)) {
                ExpressionTemporalUntil expressionTemporal = ExpressionTemporalUntil.as(expression);
                stateLabels.addAll(UtilExpressionStandard.collectIdentifiers(expressionTemporal.getOperandLeft()));
                stateLabels.addAll(UtilExpressionStandard.collectIdentifiers(expressionTemporal.getOperandRight()));
            } else if (ExpressionTemporalFinally.is(expression)) {
                ExpressionTemporalFinally expressionTemporal = ExpressionTemporalFinally.as(expression);
                stateLabels.addAll(UtilExpressionStandard.collectIdentifiers(expressionTemporal.getOperand()));                
            }
        }
        nodeProperties.addAll(stateLabels);
        nodeProperties.add(CommonProperties.STATE);
        Set<Object> edgeProperties = new LinkedHashSet<>();
        edgeProperties.add(CommonProperties.WEIGHT);


        GraphExplicit modelGraph = (GraphExplicit) UtilModelChecker.buildLowLevel(model, graphProperties, nodeProperties, edgeProperties);
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
            GraphExplicit graph, Expression expression) {
        if (expression instanceof ExpressionQuantifier) {
            ExpressionQuantifier expressionQuantifier = (ExpressionQuantifier) expression;
            expression = expressionQuantifier.getQuantified();
        }
        Set<Expression> atomics = collectLTLPropositional(expression);
        return UtilLump.partitionByAPsObjective(graph, atomics);
    }

    private static Set<Expression> collectLTLPropositional(Expression expression) {
        if (ExpressionPropositional.is(expression)) {
            return Collections.singleton(expression);
        } else if (ExpressionTemporalFinally.is(expression)) {
            ExpressionTemporalFinally expressionTemporal = ExpressionTemporalFinally.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectLTLPropositional(expressionTemporal.getOperand()));
            return result;
        } else if (ExpressionTemporalGlobally.is(expression)) {
            ExpressionTemporalGlobally expressionTemporal = ExpressionTemporalGlobally.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectLTLPropositional(expressionTemporal.getOperand()));
            return result;
        } else if (ExpressionTemporalNext.is(expression)) {
            ExpressionTemporalNext expressionTemporal = ExpressionTemporalNext.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectLTLPropositional(expressionTemporal.getOperand()));
            return result;
        } else if (ExpressionTemporalRelease.is(expression)) {
            ExpressionTemporalRelease expressionTemporal = ExpressionTemporalRelease.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectLTLPropositional(expressionTemporal.getOperandLeft()));
            result.addAll(collectLTLPropositional(expressionTemporal.getOperandRight()));
            return result;
        } else if (ExpressionTemporalUntil.is(expression)) {
            ExpressionTemporalUntil expressionTemporal = ExpressionTemporalUntil.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectLTLPropositional(expressionTemporal.getOperandLeft()));
            result.addAll(collectLTLPropositional(expressionTemporal.getOperandRight()));
            return result;
        } else if (ExpressionOperator.is(expression)) {
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

    private void lumpDD() {
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
        Options options = Options.get();
        Log log = options.get(OptionsMessages.LOG);

        GraphDD modelGraph = (GraphDD) UtilModelChecker.buildLowLevel(model, graphProperties, nodeProperties, edgeProperties);
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
    {
        assert model != null;
        Properties properties = model.getPropertyList();
        Set<RewardSpecification> result = new LinkedHashSet<>();
        for (RawProperty property : properties.getRawProperties()) {
            Expression expression = properties.getParsedProperty(property);
            result.addAll(collectRewards(model, expression));
        }
        return Collections.unmodifiableSet(result);
    }

    private LumperDD getLumperForModelAndProp(GraphDD modelGraph, Collection<Expression> propertyList) {
        Options options = Options.get();
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

    static Set<RewardSpecification> collectRewards(Model model, Expression property) {
        assert model != null;
        assert property != null;
        Set<RewardSpecification> result = new LinkedHashSet<>();
        collectRewardsRec(model, property, result);
        return Collections.unmodifiableSet(result);
    }

    private static void collectRewardsRec(Model model, Expression property,
            Set<RewardSpecification> result) {
        assert model != null;
        assert property != null;
        assert result != null;
        if (ExpressionReward.is(property)) {
            ExpressionReward propertyReward = ExpressionReward.as(property);
            RewardSpecification structure = propertyReward.getReward();
            result.add(structure);
        } else {
            for (Expression child : property.getChildren()) {
                collectRewardsRec(model, child, result);
            }
        }
    }
}
