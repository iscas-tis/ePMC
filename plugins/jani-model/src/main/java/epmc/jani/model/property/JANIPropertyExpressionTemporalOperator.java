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

import epmc.error.EPMCException;
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
	public JANINode parse(JsonValue value) throws EPMCException {
		return parseAsJANIExpression(value);
	}
	
	@Override 
	public JANIExpression parseAsJANIExpression(JsonValue value) throws EPMCException {
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
	public JsonValue generate() throws EPMCException {
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
	public JANIExpression matchExpression(ModelJANI model, Expression expression) throws EPMCException {
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
			left = parser.matchExpression(model, ExpressionLiteral.getTrue(model.getContextValue()));
			right = parser.matchExpression(model, expressionTemporal.getOperand1());
			break;
		case GLOBALLY:
			opValue = W;
			left = parser.matchExpression(model, expressionTemporal.getOperand1());
			right = parser.matchExpression(model, ExpressionLiteral.getFalse(model.getContextValue()));
			break;
		case NEXT:
			opValue = U;
			left = parser.matchExpression(model, ExpressionLiteral.getTrue(model.getContextValue()));
			right = parser.matchExpression(model, expressionTemporal.getOperand1());
			stepBounds = new JANIPropertyInterval();
			stepBounds.setLower(ExpressionLiteral.getOne(model.getContextValue()));
			stepBounds.setLowerExclusive(false);
			stepBounds.setUpper(ExpressionLiteral.getOne(model.getContextValue()));
			stepBounds.setUpperExclusive(false);
			break;
		case RELEASE:
			opValue = W;
			//op1 R op2 = op2 W (op2 /\ op1)
			left = parser.matchExpression(model, expressionTemporal.getOperand2());
			right = parser.matchExpression(model, UtilExpressionStandard.opAdd(model.getContextValue(), expressionTemporal.getOperand1(), expressionTemporal.getOperand2()));
			break;
		case UNTIL:
			opValue = U;
			left = parser.matchExpression(model, expressionTemporal.getOperand1());
			right = parser.matchExpression(model, expressionTemporal.getOperand2());
			break;
		}
		TimeBound tb = expressionTemporal.getTimeBound(model.getContextValue());
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
	public Expression getExpression() throws EPMCException {
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
				tb = new TimeBound.Builder()
						.setContext(model.getContextValue())
						.build();
			}
		}
		switch (STRING_TO_TEMPORAL_TYPE.get(opValue)) {
		case UNTIL :
			composed = newTemporal(TemporalType.UNTIL, left, right, tb);
			break;
		case RELEASE:
			//phi W psi = psi R (phi \/ psi)
			composed = newTemporal(TemporalType.RELEASE, right, UtilExpressionStandard.opOr(model.getContextValue(), left, right), tb);
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
