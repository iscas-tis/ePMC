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

package epmc.lumping;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionFilter;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionTemporalFinally;
import epmc.expression.standard.ExpressionTemporalGlobally;
import epmc.expression.standard.ExpressionTemporalRelease;
import epmc.expression.standard.ExpressionTemporalUntil;
import epmc.expression.standard.UtilExpressionStandard;
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

import static epmc.expression.standard.ExpressionPropositional.is;
import static epmc.graph.TestHelperGraph.*;

public final class TestHelperLump {
    public static GraphExplicit computeQuotient(Options options, String modelFile,
            String property) {
        Set<Object> nodeProperties = new HashSet<>();
        nodeProperties.add(CommonProperties.STATE);
        Model model = TestHelper.loadModel(options, modelFile);
        assert model != null;
        Map<String,Class<? extends LumperExplicit>> lumpersExplicit = options.get(OptionsGraphsolver.GRAPHSOLVER_LUMPER_EXPLICIT_CLASS);
        LumperExplicit useInstance = null;
        TestHelper.addProperty(model, property);
        RawProperty raw = model.getPropertyList().getRawProperties().iterator().next();
        Expression expr = model.getPropertyList().getParsedProperty(raw);
        Set<Expression> atomics = collectAPs(expr);
        nodeProperties.addAll(UtilExpressionStandard.collectIdentifiers(expr));
        GraphExplicit graph = exploreToGraph(model, nodeProperties);
        GraphSolverObjectiveExplicitLump objective = UtilLump.partitionByAPsObjective(graph, atomics);
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
    }

    public static Set<Expression> collectAPs(Expression expression) {
        if (ExpressionFilter.is(expression)) {
            ExpressionFilter expressionFilter = ExpressionFilter.as(expression);
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
        if (is(expression)) {
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
        } else if (ExpressionTemporalFinally.is(expression)) {
            ExpressionTemporalFinally expressionTemporal = ExpressionTemporalFinally.as(expression);
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

    private TestHelperLump() {
    }
}
