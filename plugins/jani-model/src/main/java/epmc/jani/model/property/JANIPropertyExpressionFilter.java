package epmc.jani.model.property;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionFilter;
import epmc.expression.standard.FilterType;
import epmc.jani.model.JANIIdentifier;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.jani.model.expression.ExpressionParser;
import epmc.jani.model.expression.JANIExpression;
import epmc.util.UtilJSON;

/**
 * JANI filter expression.
 * 
 * @author Ernst Moritz Hahn
 */
public final class JANIPropertyExpressionFilter implements JANIExpression {
	/** Identifier of this JANI expression type. */
	public final static String IDENTIFIER = "jani-property-expression-filter";
	private final static String OP = "op";
	private final static String FILTER = "filter";
	private final static String FUN = "fun";
	private final static String FUN_MIN = "min";
	private final static String FUN_MAX = "max";
	private final static String FUN_SUM = "sum";
	private final static String FUN_AVG = "avg";
	private final static String FUN_COUNT = "count";
	private final static String FUN_FORALL = "∀";
	private final static String FUN_EXIST = "∃";
	private final static String FUN_ARGMIN = "argmin";
	private final static String FUN_ARGMAX = "argmax";
	private final static String FUN_VALUES = "values";
	private final static String VALUES = "values";
	private final static String STATES = "states";
	private final static Map<String,FilterType> STRING_TO_FILTER_TYPE;
	static {
		Map<String,FilterType> filterTypes = new LinkedHashMap<>();
		filterTypes.put(FUN_MIN, FilterType.MIN);
		filterTypes.put(FUN_MAX, FilterType.MAX);
		filterTypes.put(FUN_SUM, FilterType.SUM);
		filterTypes.put(FUN_AVG, FilterType.AVG);
		filterTypes.put(FUN_COUNT, FilterType.COUNT);
		filterTypes.put(FUN_FORALL, FilterType.FORALL);
		filterTypes.put(FUN_EXIST, FilterType.EXISTS);
		filterTypes.put(FUN_ARGMIN, FilterType.ARGMIN);
		filterTypes.put(FUN_ARGMAX, FilterType.ARGMAX);
		filterTypes.put(FUN_VALUES, FilterType.PRINTALL); // TODO check
		STRING_TO_FILTER_TYPE = Collections.unmodifiableMap(filterTypes);
	}
	private final static Map<FilterType,String> FILTER_TYPE_TO_STRING =
			Collections.unmodifiableMap(UtilJSON.invertMap(STRING_TO_FILTER_TYPE));
	
	private Map<String, ? extends JANIIdentifier> validIdentifiers;
	private ModelJANI model;
	private boolean forProperty;

	private boolean initialized;
	private FilterType fun;
	private JANIExpression values;
	private JANIExpression states;
	
	private void resetFields() {
		initialized = false;
		fun = null;
		values = null;
		states = null;
	}
	
	public JANIPropertyExpressionFilter() {
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
		if (!UtilJSON.getString(object, OP).equals(FILTER)) {
			return null;
		}
		fun = UtilJSON.toOneOf(object, FUN, STRING_TO_FILTER_TYPE);
		ExpressionParser parser = new ExpressionParser(model, validIdentifiers, forProperty);
		values = parser.parseAsJANIExpression(object.get(VALUES));
		if (values == null) {
			return null;
		}
		states = parser.parseAsJANIExpression(object.get(STATES));
		if (states == null) {
			return null;
		}
		initialized = true;
		return this;
	}

	@Override
	public JsonValue generate() throws EPMCException {
		assert initialized;
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add(OP, FILTER);
		builder.add(FUN, FILTER_TYPE_TO_STRING.get(fun));
		builder.add(VALUES, values.generate());
		builder.add(STATES, states.generate());
		return builder.build();
	}

	@Override
	public JANIExpression matchExpression(ModelJANI model, Expression expression) throws EPMCException {
		assert model != null;
		assert validIdentifiers != null;
		assert expression != null;
		resetFields();
		if (!ExpressionFilter.isFilter(expression)) {
			return null;
		}
		ExpressionParser parser = new ExpressionParser(model, validIdentifiers, forProperty);
		ExpressionFilter expressionFilter = ExpressionFilter.asFilter(expression);
		states = parser.matchExpression(model, expressionFilter.getStates());
		if (states == null) {
			return null;
		}
		values = parser.matchExpression(model, expressionFilter.getProp());
		if (values == null) {
			return null;
		}
		fun = expressionFilter.getFilterType();		
		initialized = true;
		return this;
	}

	@Override
	public Expression getExpression() throws EPMCException {
		assert initialized;
		assert model != null;
		assert validIdentifiers != null;
		return new ExpressionFilter.Builder()
		        .setFilterType(fun)
		        .setProp(values.getExpression())
		        .setStates(states.getExpression())
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
}
