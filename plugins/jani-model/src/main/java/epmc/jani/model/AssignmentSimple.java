package epmc.jani.model;

import static epmc.error.UtilError.ensure;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.jani.model.expression.ExpressionParser;
import epmc.util.UtilJSON;
import epmc.value.Type;

/**
 * Stores a single variable assignment.
 * 
 * @author Ernst Moritz Hahn
 */
public final class AssignmentSimple implements Assignment {
	/** String specifying to which variable to assign to. */
	private final static String REF = "ref";
	/** String specifying expression of value to be assigned. */
	private final static String VALUE = "value";
	/** String identifying index of this assignment. */
	private final static String INDEX = "index";
	/** String specifying comment for this assignment. */
	private final static String COMMENT = "comment";

	/** Map mapping Strings to valid variables. */
	private Map<String,JANIIdentifier> validIdentifiers;
	/** Target of the assignment. */
	private Variable ref;
	/** Expression describing value assigned to target. */
	private Expression value;
	/** Optional comment of the assignment. */
	private String comment;
	/** Optional order index of the assignment. */
	private Integer index;
	/** Model to which this assignment belongs. */
	private ModelJANI model;

	/**
	 * Set valid variable assignments.
	 * This method must be called exactly once before parsing. It must not be
	 * called with a {@code null} parameter or with a parameter containing {@code
	 * null} entries.
	 * 
	 * @param variables variables which can be assigned
	 */
	public void setValidIdentifiers(Map<String,JANIIdentifier> variables) {
		assert this.validIdentifiers == null;
		assert variables != null;
		for (Entry<String, JANIIdentifier> entry : variables.entrySet()) {
			assert entry.getKey() != null;
			assert entry.getValue() != null;
		}
		this.validIdentifiers = variables;
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
	public JANINode parse(JsonValue value) throws EPMCException {
		assert model != null;
		assert value != null;
		assert validIdentifiers != null;
		JsonObject object = UtilJSON.toObject(value);
		Map<String,Variable> validVariables = new LinkedHashMap<>();
		for (Entry<String, JANIIdentifier> entry : validIdentifiers.entrySet()) {
			if (entry.getValue() instanceof Variable) {
				validVariables.put(entry.getKey(), (Variable) entry.getValue());
			}
		}
		ref = UtilJSON.toOneOf(object, REF, validVariables);
		this.value = ExpressionParser.getExpression(model, object, VALUE, validIdentifiers);
		if (!model.containsUndefinedConstants()) {
			Type variableType = ref.toType();
			Type assignedType = this.value.getType(model);
			ensure(assignedType != null, ProblemsJANIParser.JANI_PARSER_ASSIGNMENT_INCONSISTENT);
			ensure(variableType.canImport(assignedType), ProblemsJANIParser.JANI_PARSER_ASSIGNMENT_TYPE_ERROR);
		}
		index = UtilJSON.getIntegerOrNull(object, INDEX);
		comment = UtilJSON.getStringOrNull(object, COMMENT);
		return this;
	}

	@Override
	public JsonValue generate() throws EPMCException {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add(REF, ref.getName());
		builder.add(VALUE, ExpressionParser.generateExpression(model, this.value));
		UtilJSON.addOptional(builder, INDEX, index);
		UtilJSON.addOptional(builder, COMMENT, comment);
		return builder.build();
	}

	public void setRef(Variable ref) {
		this.ref = ref;
	}

	public Variable getRef() {
		return ref;
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
	
	public void setIndex(Integer index) {
		this.index = index;
	}
	
	public Integer getIndex() {
		return index;
	}
	
	public int getIndexOrZero() {
		if (index == null) {
			return 0;
		} else {
			return index;
		}
	}
	
	@Override
	public String toString() {
		return UtilModelParser.toString(this);
	}

}
