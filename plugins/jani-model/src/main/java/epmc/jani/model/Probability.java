package epmc.jani.model;

import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.jani.model.expression.ExpressionParser;
import epmc.util.UtilJSON;

public final class Probability implements JANINode {
	private final static String EXP = "exp";
	private final static String COMMENT = "comment";
	private ModelJANI model;
	private Map<String, ? extends JANIIdentifier> identifiers;
	private Expression exp;
	private String comment;

	@Override
	public void setModel(ModelJANI model) {
		this.model = model;
	}

	@Override
	public ModelJANI getModel() {
		return model;
	}
	
	public void setIdentifiers(Map<String, ? extends JANIIdentifier> identifiers) {
		this.identifiers = identifiers;
	}
	
	@Override
	public JANINode parse(JsonValue value) throws EPMCException {
		assert value != null;
		JsonObject object = UtilJSON.toObject(value);
		exp = ExpressionParser.parseExpression(model, object.get(EXP), identifiers);
		comment = UtilJSON.getStringOrNull(object, COMMENT);
		return this;
	}

	@Override
	public JsonValue generate() throws EPMCException {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add(EXP, ExpressionParser.generateExpression(model, exp));
		UtilJSON.addOptional(builder, COMMENT, comment);
		return builder.build();
	}

	public void setExp(Expression exp) {
		this.exp = exp;
	}
	
	public Expression getExp() {
		return exp;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String getComment() {
		return comment;
	}
	
}
