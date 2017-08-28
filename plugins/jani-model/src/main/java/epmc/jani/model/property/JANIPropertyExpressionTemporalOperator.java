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

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionReward;
import epmc.expression.standard.ExpressionTemporal;
import epmc.expression.standard.RewardSpecification;
import epmc.expression.standard.TemporalType;
import epmc.expression.standard.TimeBound;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.graph.SemanticsContinuousTime;
import epmc.graph.SemanticsDiscreteTime;
import epmc.graph.SemanticsTimed;
import epmc.jani.model.JANIIdentifier;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.jani.model.expression.ExpressionParser;
import epmc.jani.model.expression.JANIExpression;
import epmc.util.UtilJSON;

/**
 * JANI expected quantifier expression.
 * 
 * @author Andrea Turrini
 */
public final class JANIPropertyExpressionTemporalOperator implements JANIExpression {
	/** Identifier of this JANI expression type. */
	public final static String IDENTIFIER = "property-expression-temporal-operator";
	
	private final static String OP = "op";
	private final static String U = "U";
	private final static String W = "W";
	private final static String LEFT = "left";
	private final static String RIGHT = "right";
	private final static String STEP_BOUNDS = "step-bounds";
	private final static String TIME_BOUNDS = "time-bounds";
	private final static String REWARD_BOUNDS = "reward-bounds";
	
	private final static Map<String,TemporalType> STRING_TO_TEMPORAL_TYPE;

	static {
		Map<String,TemporalType> temporalTypes = new LinkedHashMap<>();
		temporalTypes.put(U, TemporalType.UNTIL);
		temporalTypes.put(W, TemporalType.RELEASE);
		STRING_TO_TEMPORAL_TYPE = Collections.unmodifiableMap(temporalTypes);
	}
	

	private Map<String, ? extends JANIIdentifier> validIdentifiers;
	private ModelJANI model;
	private boolean forProperty;

	private boolean initialized;
	private String opValue;
	private JANIExpression left;
	private JANIExpression right;
	private JANIPropertyInterval stepBounds;
	private JANIPropertyInterval timeBounds;
	private List<JANIPropertyRewardBound> rewardBounds;
	
	private void resetFields() {
		initialized = false;
		opValue = null;
		left = null;
		right = null;
		stepBounds = null;
		timeBounds = null;
		rewardBounds = null;
	}
	
	public JANIPropertyExpressionTemporalOperator() {
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
		if (!object.containsKey(LEFT)) {
			return null;
		}
		if (!object.containsKey(RIGHT)) {
			return null;
		}
		opValue = UtilJSON.getString(object, OP);
		if (!opValue.equals(U) && !opValue.equals(W)) {
			return null;
		}
		ExpressionParser parser = new ExpressionParser(model, validIdentifiers, forProperty);
		left = parser.parseAsJANIExpression(object.get(LEFT));
		right = parser.parseAsJANIExpression(object.get(RIGHT));

		if (object.containsKey(STEP_BOUNDS)) {
			stepBounds = new JANIPropertyInterval();
			stepBounds.setIdentifiers(validIdentifiers);
			stepBounds.setForProperty(forProperty);
			stepBounds.setModel(model);
			stepBounds.parse(object.get(STEP_BOUNDS));
		} else {
			stepBounds = null;
		}

		if (object.containsKey(TIME_BOUNDS)) {
			timeBounds = new JANIPropertyInterval();
			timeBounds.setModel(model);
			timeBounds.setForProperty(forProperty);
			timeBounds.setIdentifiers(validIdentifiers);
			timeBounds.parse(object.get(TIME_BOUNDS));
		} else {
			timeBounds = null;
		}

		if (object.containsKey(REWARD_BOUNDS)) {
			JsonArray rewards = UtilJSON.getArray(object, REWARD_BOUNDS);
			this.rewardBounds = new ArrayList<>(rewards.size());
			for (JsonValue rew : rewards) {
				JANIPropertyRewardBound rb = new JANIPropertyRewardBound();
				rb.setIdentifiers(validIdentifiers);
				stepBounds.setForProperty(forProperty);
				rb.setModel(model);
				rb.parse(rew);
				this.rewardBounds.add(rb);
			}
		} else {
			rewardBounds = null;
		}
		initialized = true;
		return this;
	}

	@Override
	public JsonValue generate() {
		assert initialized;
		assert model != null;
		assert validIdentifiers != null;
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add(OP, opValue);
		builder.add(LEFT, left.generate());
		builder.add(RIGHT, right.generate());
		if (stepBounds != null) {
			builder.add(STEP_BOUNDS, stepBounds.generate());
		}
		if (timeBounds != null) {
			builder.add(TIME_BOUNDS, timeBounds.generate());
		}
		if (rewardBounds != null) {
			JsonArrayBuilder rewBuilder = Json.createArrayBuilder();
			for (JANIPropertyRewardBound rew : rewardBounds) {
				rewBuilder.add(rew.generate());
			}
			builder.add(REWARD_BOUNDS, rewBuilder);
		}
		return builder.build();
	}

	@Override
	public JANIExpression matchExpression(ModelJANI model, Expression expression) {
		assert expression != null;
		assert model != null;
		assert validIdentifiers != null;
		resetFields();
		if (!(expression instanceof ExpressionTemporal)) {
			return null;
		}
		ExpressionTemporal expressionTemporal = (ExpressionTemporal) expression;
		
		ExpressionParser parser = new ExpressionParser(model, validIdentifiers, forProperty);
		switch (expressionTemporal.getTemporalType()) {
		case FINALLY:
			opValue = U;
			left = parser.matchExpression(model, ExpressionLiteral.getTrue());
			right = parser.matchExpression(model, expressionTemporal.getOperand1());
			break;
		case GLOBALLY:
			opValue = W;
			left = parser.matchExpression(model, expressionTemporal.getOperand1());
			right = parser.matchExpression(model, ExpressionLiteral.getFalse());
			break;
		case NEXT:
			opValue = U;
			left = parser.matchExpression(model, ExpressionLiteral.getTrue());
			right = parser.matchExpression(model, expressionTemporal.getOperand1());
			stepBounds = new JANIPropertyInterval();
			stepBounds.setLower(ExpressionLiteral.getOne());
			stepBounds.setLowerExclusive(false);
			stepBounds.setUpper(ExpressionLiteral.getOne());
			stepBounds.setUpperExclusive(false);
			break;
		case RELEASE:
			opValue = W;
			//op1 R op2 = op2 W (op2 /\ op1)
			left = parser.matchExpression(model, expressionTemporal.getOperand2());
			right = parser.matchExpression(model, UtilExpressionStandard.opAdd(expressionTemporal.getOperand1(), expressionTemporal.getOperand2()));
			break;
		case UNTIL:
			opValue = U;
			left = parser.matchExpression(model, expressionTemporal.getOperand1());
			right = parser.matchExpression(model, expressionTemporal.getOperand2());
			break;
		}
		TimeBound tb = expressionTemporal.getTimeBound();
		if (tb != null && !tb.isUnbounded()) {
			JANIPropertyInterval interval = new JANIPropertyInterval();
			interval.setForProperty(forProperty);
			interval.setModel(model);
			interval.setIdentifiers(validIdentifiers);
			if (tb.isLeftBounded()) { 
				interval.setLower(tb.getLeft());
			}
			interval.setLowerExclusive(tb.isLeftOpen());
			if (tb.isRightBounded()) { 
				interval.setUpper(tb.getRight());
			}
			interval.setUpperExclusive(tb.isRightOpen());
			if (SemanticsContinuousTime.isContinuousTime(model.getSemantics())) {
				timeBounds = interval;
			}
			if (SemanticsDiscreteTime.isDiscreteTime(model.getSemantics())) {
				stepBounds = interval;
			}
			if (SemanticsTimed.isTimed(model.getSemantics())) {
				timeBounds = interval;
			}
		}
		if (expression instanceof ExpressionReward) {
			RewardSpecification rs = ((ExpressionReward) expression).getReward();
			if (rs != null) {
				rewardBounds = new ArrayList<>();
				JANIPropertyRewardBound rb = new JANIPropertyRewardBound();
				rb.setForProperty(forProperty);
				rb.setIdentifiers(validIdentifiers);
				rb.setModel(model);
				List<JANIPropertyAccumulateValue> acc = new ArrayList<>(1);
				if (SemanticsContinuousTime.isContinuousTime(model.getSemantics())) {
					acc.add(JANIPropertyAccumulateValue.TIME);
				}
				if (SemanticsDiscreteTime.isDiscreteTime(model.getSemantics())) {
					acc.add(JANIPropertyAccumulateValue.STEPS);
				}
				rb.setAccumulate(acc);
				rb.setExp(rs.getExpression());
				rewardBounds.add(rb);
			}
		}
		initialized = true;
		return this;
	}

	@Override
	public Expression getExpression() {
		assert initialized;
		assert model != null;
		assert validIdentifiers != null;
		
		Expression left = this.left.getExpression();
		Expression right = this.right.getExpression();
		Expression composed;
		TimeBound tb;
		if (stepBounds != null) {
			tb = stepBounds.getTimeBound();
		} else {
			if (timeBounds != null) {
				tb = timeBounds.getTimeBound();
			} else {
				tb = new TimeBound.Builder().build();
			}
		}
		switch (STRING_TO_TEMPORAL_TYPE.get(opValue)) {
		case UNTIL :
			composed = newTemporal(TemporalType.UNTIL, left, right, tb);
			break;
		case RELEASE:
			//phi W psi = psi R (phi \/ psi)
			composed = newTemporal(TemporalType.RELEASE, right, UtilExpressionStandard.opOr(left, right), tb);
		default: //it should never happen
			composed = null;
			break;
		}
		//TODO: see what to do with the rewards...
		return composed;
	}

	@Override
	public void setIdentifiers(Map<String, ? extends JANIIdentifier> identifiers) {
		this.validIdentifiers = identifiers;
	}	
	
	@Override
	public String toString() {
		return UtilModelParser.toString(this);
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
	
    private static ExpressionTemporal newTemporal
    (TemporalType type, Expression op1, Expression op2,
            TimeBound bound) {
        assert type != null;
        assert bound != null;
        return new ExpressionTemporal
                (op1, op2, type, bound, null);
    }
}
