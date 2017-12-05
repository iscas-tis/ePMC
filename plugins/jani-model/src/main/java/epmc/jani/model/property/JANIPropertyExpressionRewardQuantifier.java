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

package epmc.jani.model.property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;

import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.standard.CmpType;
import epmc.expression.standard.DirType;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionReward;
import epmc.expression.standard.RewardType;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.graph.SemanticsContinuousTime;
import epmc.graph.SemanticsDiscreteTime;
import epmc.jani.model.JANIIdentifier;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.jani.model.expression.ExpressionParser;
import epmc.jani.model.expression.JANIExpression;
import epmc.operator.OperatorIsPosInf;
import epmc.util.UtilJSON;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueBoolean;

/**
 * JANI expected quantifier expression.
 * 
 * @author Andrea Turrini
 */
public final class JANIPropertyExpressionRewardQuantifier implements JANIExpression {
    /** Identifier of this JANI expression type. */
    public final static String IDENTIFIER = "jani-property-expression-reward-quantifier";

    private final static String OP = "op";
    private final static String EMIN = "Emin";
    private final static String EMAX = "Emax";
    private final static String EXP = "exp";
    private final static String ACCUMULATE = "accumulate";
    private final static String REACH = "reach";
    private final static String STEP_INSTANT = "step-instant";
    private final static String TIME_INSTANT = "time-instant";
    private final static String REWARD_INSTANTS = "reward-instants";
    private final static Map<String,DirType> STRING_TO_DIR_TYPE;
    static {
        Map<String,DirType> stringToDirType = new LinkedHashMap<>();
        stringToDirType.put(EMIN, DirType.MIN);
        stringToDirType.put(EMAX, DirType.MAX);
        STRING_TO_DIR_TYPE = Collections.unmodifiableMap(stringToDirType);
    }

    private Map<String, ? extends JANIIdentifier> validIdentifiers;
    private ModelJANI model;
    private boolean forProperty;

    private boolean initialized;

    private String opValue;
    private JANIExpression exp;
    private DirType dirType;
    private List<JANIPropertyAccumulateValue> accumulate;
    private JANIExpression reach;
    private JANIExpression stepInstant;
    private JANIExpression timeInstant;
    private List<JANIPropertyRewardInstant> rewardInstants;
    /** Positional information. */
    private Positional positional;

    private void resetFields() {
        initialized = false;
        opValue = null;
        exp = null;
        dirType = null;
        accumulate = null;
        reach = null;
        stepInstant = null;
        timeInstant = null;
        rewardInstants = null;
        positional = null;
    }

    public JANIPropertyExpressionRewardQuantifier() {
        resetFields();
    }

    @Override
    public JANINode parse(JsonValue value) {
        return parseAsJANIExpression(value);
    }

    @Override 
    public JANIExpression parseAsJANIExpression(JsonValue value) {
        assert model != null;
        assert validIdentifiers != null;
        assert value != null;
        resetFields();
        if (!forProperty) {
            return null;
        }
        if (!(value instanceof JsonObject)) {
            return null;
        }
        JsonObject object = (JsonObject) value;
        if (!object.containsKey(OP)) {
            return null;
        }
        if (!(object.get(OP) instanceof JsonString)) {
            return null;
        }
        if (!object.containsKey(EXP)) {
            return null;
        }
        dirType = UtilJSON.toOneOfOrNullFailInvalidType(object, OP, STRING_TO_DIR_TYPE);
        if (dirType == null) {
            return null;
        }
        opValue = UtilJSON.getString(object, OP);
        //it is specified to be a simple Expression, not a PropertyExpression
        ExpressionParser parser = new ExpressionParser(model, validIdentifiers, false);
        exp = parser.parseAsJANIExpression(object.get(EXP));
        if (exp == null) {
            return null;
        }
        JsonArray accumulate = UtilJSON.getArrayOrNull(object, ACCUMULATE);
        if (accumulate != null) {
            this.accumulate = new ArrayList<>(accumulate.size());
            for (JsonValue acc : accumulate) {
                this.accumulate.add(UtilJSON.toOneOf(acc, JANIPropertyAccumulateValue.getAccumulateValues()));
            }
        } else {
            this.accumulate = null;
        }

        if (object.containsKey(REACH)) {
            parser.setForProperty(forProperty);
            reach = parser.parseAsJANIExpression(object.get(REACH));
        } else {
            reach = null;
        }

        if (object.containsKey(STEP_INSTANT)) {
            //it is specified to be a simple Expression, not a PropertyExpression
            parser.setForProperty(false);
            stepInstant = parser.parseAsJANIExpression(object.get(STEP_INSTANT));
        } else {
            stepInstant = null;
        }

        if (object.containsKey(TIME_INSTANT)) {
            //it is specified to be a simple Expression, not a PropertyExpression
            parser.setForProperty(false);
            timeInstant = parser.parseAsJANIExpression(object.get(TIME_INSTANT));
        } else {
            timeInstant = null;
        }

        JsonArray rewards = UtilJSON.getArrayOrNull(object, REWARD_INSTANTS);
        if (rewards != null) {
            this.rewardInstants = new ArrayList<>(rewards.size());
            for (JsonValue rew : rewards) {
                JANIPropertyRewardInstant ri = new JANIPropertyRewardInstant();
                ri.setIdentifiers(validIdentifiers);
                ri.setModel(model);
                ri.parse(rew);
                this.rewardInstants.add(ri);
            }
        } else {
            rewardInstants = null;
        }
        initialized = true;
        positional = UtilModelParser.getPositional(value);
        return this;
    }

    @Override
    public JsonValue generate() {
        assert initialized;
        assert model != null;
        assert validIdentifiers != null;
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add(OP, opValue);
        builder.add(EXP, exp.generate());
        if (accumulate != null) {
            JsonArrayBuilder accumulateBuilder = Json.createArrayBuilder();
            for (JANIPropertyAccumulateValue acc : accumulate) {
                accumulateBuilder.add(acc.toString());
            }
            builder.add(ACCUMULATE, accumulateBuilder);
        }
        if (reach != null) {
            builder.add(REACH, reach.generate());
        }
        if (stepInstant != null) {
            builder.add(STEP_INSTANT, stepInstant.generate());
        }
        if (timeInstant != null) {
            builder.add(TIME_INSTANT, timeInstant.generate());
        }
        if (rewardInstants != null) {
            JsonArrayBuilder rewBuilder = Json.createArrayBuilder();
            for (JANIPropertyRewardInstant rew : rewardInstants) {
                rewBuilder.add(rew.generate());
            }
            builder.add(REWARD_INSTANTS, rewBuilder);
        }
        UtilModelParser.addPositional(builder, positional);
        return builder.build();
    }

    @Override
    public JANIExpression matchExpression(ModelJANI model, Expression expression) {
        assert expression != null;
        assert model != null;
        assert validIdentifiers != null;
        resetFields();
        if (!(expression instanceof ExpressionQuantifier)) {
            return null;
        }
        ExpressionQuantifier expressionQuantifier = (ExpressionQuantifier) expression;
        Expression quantified = expressionQuantifier.getQuantified();
        if (!(quantified instanceof ExpressionReward)) {
            return null;
        }
        //a property like R=?[S] has to be translated as S=?[reward_expression]...
        if (((ExpressionReward) quantified).getRewardType().isSteadystate()) {
            return null;				
        }
        dirType = expressionQuantifier.getDirType();
        switch (dirType) {
        case MAX:
            opValue = EMAX;
            break;
        case MIN:
            opValue = EMIN;
            break;
        default:
            //			the only possibility is "NONE", i.e., we are in a dtmc/ctmc model
            //			thus, every operator is OK.
            opValue = EMAX;
            break;
        }
        ExpressionParser parser = new ExpressionParser(model, validIdentifiers, forProperty);
        if (isRewardReachability(quantified)) {
            List<JANIPropertyAccumulateValue> acc = new ArrayList<>(2);
            if (SemanticsContinuousTime.isContinuousTime(model.getSemantics())) {
                acc.add(JANIPropertyAccumulateValue.TIME);
            }
            if (SemanticsDiscreteTime.isDiscreteTime(model.getSemantics())) {
                acc.add(JANIPropertyAccumulateValue.STEPS);
            }
            accumulate = acc;
            //reach is of type PropertyExpression
            reach = parser.matchExpression(model, ((ExpressionReward) quantified).getRewardReachSet());
            if (reach == null) {
                return null;
            }
        } else {
            //time/step-instant are of type Expression, not of type PropertyExpression
            parser.setForProperty(false);
            ExpressionReward quantifiedReward = (ExpressionReward) quantified;
            Expression time = quantifiedReward.getTime();
            if (!isPosInf(time)) {
                JANIExpression timeBound = parser.matchExpression(model, time); 
                if (SemanticsContinuousTime.isContinuousTime(model.getSemantics())) {
                    timeInstant = timeBound;
                }
                if (SemanticsDiscreteTime.isDiscreteTime(model.getSemantics())) {
                    stepInstant = timeBound;
                }
            }
        }
        if (isRewardCumulative(quantified)) {
            List<JANIPropertyAccumulateValue> acc = new ArrayList<>(2);
            if (SemanticsContinuousTime.isContinuousTime(model.getSemantics())) {
                acc.add(JANIPropertyAccumulateValue.TIME);
            }
            if (SemanticsDiscreteTime.isDiscreteTime(model.getSemantics())) {
                acc.add(JANIPropertyAccumulateValue.STEPS);
            }
            accumulate = acc;		    
        }
        //the reward expression is specified to be a simple Expression, not a PropertyExpression
        parser.setForProperty(false);
        //exp stands for the reward expression, TODO: check if it is correct
        exp = parser.matchExpression(model, ((ExpressionReward) quantified).getReward().getExpression());
        initialized = true;
        positional = expression.getPositional();
        return this;
    }

    @Override
    public Expression getExpression() {
        assert initialized;
        assert model != null;
        assert validIdentifiers != null;
        Expression quantified = null;
        if (accumulate != null && reach != null) {
            //accumulate reachability -> F
            quantified = new ExpressionReward.Builder()
                    .setRewardType(RewardType.REACHABILITY)
                    .setReward(exp.getExpression())
                    .setReachSet(reach.getExpression())
                    .build();
        }
        if (accumulate != null && stepInstant != null) {
            //accumulate instantaneous, discrete time -> C<=
            quantified = newRewardCumulative(exp.getExpression(), stepInstant.getExpression());
        }
        if (accumulate != null && timeInstant != null) {
            //accumulate instantaneous, continuous time -> C<=
            quantified = newRewardCumulative(exp.getExpression(), timeInstant.getExpression());
        }
        if (accumulate != null && timeInstant == null && stepInstant == null && reach == null) {
            //accumulate -> C
            quantified = newRewardCumulative(exp.getExpression(), ExpressionLiteral.getPosInf());
        }
        if (accumulate == null && stepInstant != null) {
            //instantaneous, discrete time -> I=
            quantified = newRewardInstantaneous(exp.getExpression(), stepInstant.getExpression());
        }
        if (accumulate == null && timeInstant != null) {
            //instantaneous, continuous time -> I=
            quantified = newRewardInstantaneous(exp.getExpression(), timeInstant.getExpression());
        }
        return new ExpressionQuantifier.Builder()
                .setDirType(dirType)
                .setCmpType(CmpType.IS)
                .setQuantified(quantified)
                .setPositional(positional)
                .build();
    }

    @Override
    public void setIdentifiers(Map<String, ? extends JANIIdentifier> identifiers) {
        this.validIdentifiers = identifiers;
    }	

    @Override
    public void setForProperty(boolean forProperty) {
        this.forProperty = forProperty;
    }

    @Override
    public void setModel(ModelJANI model) {
        this.model = model;
    }

    @Override
    public ModelJANI getModel() {
        return model;
    }

    @Override
    public String toString() {
        return UtilModelParser.toString(this);
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

    private static ExpressionReward newRewardInstantaneous
    (Expression structure, Expression time) {
        return new ExpressionReward.Builder()
                .setRewardType(RewardType.INSTANTANEOUS)
                .setReward(structure)
                .setTime(time)
                .build();
    }

    private static ExpressionReward newRewardCumulative
    (Expression structure, Expression time) {
        return new ExpressionReward.Builder()
                .setRewardType(RewardType.CUMULATIVE)
                .setReward(structure)
                .setTime(time)
                .build();
    }
    
    @Override
    public void setPositional(Positional positional) {
        this.positional = positional;
    }
    
    @Override
    public Positional getPositional() {
        return positional;
    }
}
