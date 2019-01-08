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

package epmc.expression.standard;

import static epmc.error.UtilError.ensure;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.expression.Expression;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.jani.exporter.error.ProblemsJANIExporter;
import epmc.jani.exporter.processor.JANIExporter_Processor;
import epmc.jani.exporter.processor.JANIExporter_ProcessorRegistrar;
import epmc.jani.model.UtilModelParser;
import epmc.operator.OperatorIsPosInf;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueBoolean;

public class QMCExporter_ExpressionQuantifier2JANIProcessor implements JANIExporter_Processor {
    private static final String OP = "op";
    private static final String QMAX = "Qmax";
    private static final String QMIN = "Qmin";
    private static final String SMAX = "Smax";
    private static final String SMIN = "Smin";
    private static final String EXP = "exp";

    private static final String EMAX = "Emax";
    private static final String EMIN = "Emin";
    private static final String ACCUMULATE = "accumulate";
    private static final String TIME = "time";
    private static final String STEPS = "steps";
    private static final String REACH = "reach";
    private static final String STEP_INSTANT = "step-instant";
    private static final String TIME_INSTANT = "time-instant";

    private ExpressionQuantifier expressionQuantifier = null;

    @Override
    public JANIExporter_Processor setElement(Object obj) {
        assert obj != null;
        assert obj instanceof ExpressionQuantifier; 

        expressionQuantifier = (ExpressionQuantifier) obj;
        
        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert expressionQuantifier != null;
        
        Expression quantified = expressionQuantifier.getQuantified();
        if (quantified instanceof ExpressionSteadyState) {
            return expressionSteadyStateToJSON();
        } else if (quantified instanceof ExpressionReward) {
            if (((ExpressionReward) quantified).getRewardType().isSteadystate()) {
                return expressionSteadyStateRewardToJSON();
            } else {
                return expressionRewardToJSON();
            }
        } else {
            return expressionQuantumToJSON();
        }
    }
    
    private JsonValue expressionSteadyStateToJSON() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        
        DirType dirType = expressionQuantifier.getDirType();
        if (dirType.isMax() || dirType.isNone()) {
            builder.add(OP, SMAX);
        } else if (dirType.isMin()) {
            builder.add(OP, SMIN);
        } else {
            ensure(false,
                    ProblemsJANIExporter.JANI_EXPORTER_ERROR_UNSUPPORTED_DIRTYPE,
                    dirType);
        }
        
        builder.add(EXP, JANIExporter_ProcessorRegistrar.getProcessor(expressionQuantifier.getQuantified())
                .toJSON());
        UtilModelParser.addPositional(builder, expressionQuantifier.getPositional());

        return builder.build();
    }
    
    private JsonValue expressionSteadyStateRewardToJSON() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        
        DirType dirType = expressionQuantifier.getDirType();
        if (dirType.isMax() || dirType.isNone()) {
            builder.add(OP, SMAX);
        } else if (dirType.isMin()) {
            builder.add(OP, SMIN);
        } else {
            ensure(false,
                    ProblemsJANIExporter.JANI_EXPORTER_ERROR_UNSUPPORTED_DIRTYPE,
                    dirType);
        }

        ExpressionReward quantified = (ExpressionReward) expressionQuantifier.getQuantified();
        builder.add(EXP, JANIExporter_ProcessorRegistrar.getProcessor(quantified.getReward().getExpression())
                .toJSON());
        UtilModelParser.addPositional(builder, expressionQuantifier.getPositional());

        return builder.build();
    }
    
    private JsonValue expressionQuantumToJSON() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        
        DirType dirType = expressionQuantifier.getDirType();
        if (dirType.isMax() || dirType.isNone()) {
            builder.add(OP, QMAX);
        } else if (dirType.isMin()) {
            builder.add(OP, QMIN);
        } else {
            ensure(false,
                    ProblemsJANIExporter.JANI_EXPORTER_ERROR_UNSUPPORTED_DIRTYPE,
                    dirType);
        }
        
        builder.add(EXP, JANIExporter_ProcessorRegistrar.getProcessor(expressionQuantifier.getQuantified())
                .toJSON());
        UtilModelParser.addPositional(builder, expressionQuantifier.getPositional());
        
        return builder.build();
    }
    
    private JsonValue expressionRewardToJSON() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        
        DirType dirType = expressionQuantifier.getDirType();
        if (dirType.isMax() || dirType.isNone()) {
            builder.add(OP, EMAX);
        } else if (dirType.isMin()) {
            builder.add(OP, EMIN);
        } else {
            ensure(false,
                    ProblemsJANIExporter.JANI_EXPORTER_ERROR_UNSUPPORTED_DIRTYPE,
                    dirType);
        }

        ExpressionReward quantified = (ExpressionReward) expressionQuantifier.getQuantified();
        builder.add(EXP, JANIExporter_ProcessorRegistrar.getProcessor(quantified.getReward().getExpression())
                .toJSON());
        
        if (isRewardReachability(quantified)) {
            JsonArrayBuilder builderAcc = Json.createArrayBuilder();
            boolean addAcc = false;
            if (JANIExporter_ProcessorRegistrar.isContinuousTimeModel()) {
                builderAcc.add(TIME);
                addAcc = true;
            }
            if (JANIExporter_ProcessorRegistrar.isDiscreteTimeModel()) {
                builderAcc.add(STEPS);
                addAcc = true;
            }
            if (addAcc) {
                builder.add(ACCUMULATE, builderAcc.build());
            }
            Expression reach = quantified.getRewardReachSet();
            if (reach != null) {
                builder.add(REACH, JANIExporter_ProcessorRegistrar.getProcessor(reach)
                        .toJSON());
            }
        } else {
            Expression time = quantified.getTime();
            if (!isPosInf(time)) {
                JsonValue timeBound = JANIExporter_ProcessorRegistrar.getProcessor(time).toJSON();
                if (JANIExporter_ProcessorRegistrar.isDiscreteTimeModel()) {
                    builder.add(STEP_INSTANT, timeBound);
                }
                if (JANIExporter_ProcessorRegistrar.isContinuousTimeModel()) {
                    builder.add(TIME_INSTANT, timeBound);
                }
            }
        }
        if (isRewardCumulative(quantified)) {
            JsonArrayBuilder builderAcc = Json.createArrayBuilder();
            boolean addAcc = false;
            if (JANIExporter_ProcessorRegistrar.isContinuousTimeModel()) {
                builderAcc.add(TIME);
                addAcc = true;
            }
            if (JANIExporter_ProcessorRegistrar.isDiscreteTimeModel()) {
                builderAcc.add(STEPS);
                addAcc = true;
            }
            if (addAcc) {
                builder.add(ACCUMULATE, builderAcc.build());
            }
        }

        UtilModelParser.addPositional(builder, expressionQuantifier.getPositional());
        
        return builder.build();
    }

    private static boolean isRewardCumulative(Expression expression) {
        if (!(expression instanceof ExpressionReward)) {
            return false;
        }
        ExpressionReward expressionReward = (ExpressionReward) expression;
        return expressionReward.getRewardType().isCumulative();
    }

    private static boolean isRewardReachability(Expression expression) {
        if (!(expression instanceof ExpressionReward)) {
            return false;
        }
        ExpressionReward expressionReward = (ExpressionReward) expression;
        return expressionReward.getRewardType().isReachability();
    }

    private static boolean isPosInf(Expression expression) {
        assert expression != null;
        if (!ExpressionLiteral.is(expression)) {
            return false;
        }
        Value expValue = UtilEvaluatorExplicit.evaluate(expression);
        OperatorEvaluator isPosInf = ContextValue.get().getEvaluator(OperatorIsPosInf.IS_POS_INF, expValue.getType());
        ValueBoolean cmp = TypeBoolean.get().newValue();
        isPosInf.apply(cmp, expValue);
        return cmp.getBoolean();
    }
}
