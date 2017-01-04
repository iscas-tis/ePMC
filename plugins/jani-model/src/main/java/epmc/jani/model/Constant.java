package epmc.jani.model;

import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.jani.model.expression.ExpressionParser;
import epmc.jani.model.type.JANIType;
import epmc.jani.model.type.TypeParser;
import epmc.util.UtilJSON;

public final class Constant implements JANINode, JANIIdentifier {
	/** Identifier for the name of the constant. */
	private final static String NAME = "name";
	/** Identifier for the type of the constant. */
	private final static String TYPE = "type";
	/** Identifier for the value of the constant. */
	private final static String VALUE = "value";
	/** Identifier for the comment of the constant. */
	private final static String COMMENT = "comment";
	
	/** Model to which this constant belongs. */
	private ModelJANI model;
	private Map<String, ? extends JANIIdentifier> validIdentifiers;
	/** Name of the constant. */
	private String name;
	/** Type of the variable. */
	private JANIType type;
	/** Value of the constant. */
	private Expression value;
	/** Comment of the constant. */
	private String comment;
	/** Identifier of this constant. */
	private ExpressionIdentifierStandard identifier;
	
	private void resetFields() {
		name = null;
		type = null;
		value = null;
		comment = null;
		identifier = null;
	}
	
	public Constant() {
		resetFields();
	}
	
	public Constant(ModelJANI model, String name, JANIType type, ExpressionIdentifierStandard identifier) {
		this.model = model;
		this.name = name;
		this.type = type;
		this.identifier = identifier;
	}

	@Override
	public void setModel(ModelJANI model) {
		this.model = model;
	}
	
	@Override
	public ModelJANI getModel() {
		return model;
	}
	
	public void setValidIdentifiers(Map<String, ? extends JANIIdentifier> validIdentifiers) {
		this.validIdentifiers = validIdentifiers;
	}
	
	@Override
	public JANINode parse(JsonValue value) throws EPMCException {
		assert value != null;
		assert model != null;
		resetFields();
		JsonObject object = UtilJSON.toObject(value);
		name = UtilJSON.getString(object, NAME);
		JsonValue typeV = object.get(TYPE);
		TypeParser typeParser = new TypeParser();
		typeParser.setModel(model);
		typeParser.parse(typeV);
		type = typeParser.getType();
		type.setContextValue(model.getContextValue());
		JsonValue constV = object.get(VALUE);
		if (constV != null) {
			this.value = ExpressionParser.parseExpression(model, constV, validIdentifiers);
		} else {
			this.value = null;
		}
		comment = UtilJSON.getStringOrNull(object, COMMENT);
		identifier = new ExpressionIdentifierStandard.Builder()
				.setName(name)
				.build();
		return this;
	}

	@Override
	public JsonValue generate() throws EPMCException {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add(NAME, name);
		builder.add(TYPE, type.generate());
		if (value != null) {
			builder.add(VALUE, ExpressionParser.generateExpression(model, value));
		}
		if (comment != null) {
			builder.add(COMMENT, comment);
		}
		return builder.build();
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public void setType(JANIType type) {
		this.type = type;
	}
	
	@Override
	public JANIType getType() {
		return type;
	}
	
	public void setValue(Expression value) {
		this.value = value;
	}
	
	public Expression getValue() {
		return value;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String getComment() {
		return comment;
	}
	
	@Override
	public ExpressionIdentifierStandard getIdentifier() {
		return identifier;
	}
	
	public void setIdentifier(ExpressionIdentifierStandard identifier) {
		this.identifier = identifier;
	}
	
	@Override
	public String toString() {
		return UtilModelParser.toString(this);
	}
}
