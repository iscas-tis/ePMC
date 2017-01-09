package epmc.jani.model.property;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.jani.model.JANIIdentifier;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.modelchecker.Properties;
import epmc.modelchecker.RawProperty;
import epmc.util.UtilJSON;

// TODO conversion between JANI properties and general properties must be improved

/**
 * Class representing the properties of a JANI model.
 * 
 * @author Ernst Moritz Hahn
 */
public final class JANIProperties implements JANINode, Properties {
	/** String used for naming unnamed properties as DEFAULT_NAME_num */
	private final static String DEFAULT_NAME = "Property_%s_%d";

	/** Model to which the properties belong. */
	private ModelJANI model;
	private Map<String, ? extends JANIIdentifier> validIdentifiers;
	/** Properties stored in this properties object, transformed form. */
	private final Map<String,JANIPropertyEntry> properties = new LinkedHashMap<>();

	@Override
	public void setModel(ModelJANI model) {
		this.model = model;
	}
	
	@Override
	public ModelJANI getModel() {
		return model;
	}

	public void setValidIdentifiers(Map<String, ? extends JANIIdentifier> identifiers) {
		this.validIdentifiers = identifiers;
	}

	@Override
	public JANINode parse(JsonValue value) throws EPMCException {
		assert model != null;
		assert value != null;
		assert validIdentifiers != null;
		properties.clear();		
		JsonArray array = UtilJSON.toArrayObject(value);
		for (JsonValue entryValue : array) {
			JANIPropertyEntry entry = new JANIPropertyEntry();
			entry.setModel(model);
			entry.setValidIdentifiers(validIdentifiers);
			entry.parse(entryValue);
			properties.put(entry.getName(), entry);
		}
		return this;
	}

	@Override
	public JsonValue generate() throws EPMCException {
		assert validIdentifiers != null;
		assert model != null;
		assert properties != null;
		JsonArrayBuilder result = Json.createArrayBuilder();
		for (JANIPropertyEntry property : properties.values()) {
			result.add(property.generate());
		}
		return result.build();
	}

	@Override
	public String toString() {
		return UtilModelParser.toString(this);
	}
	
	public void addProperty(String name, Expression property, String comment) {
		assert property != null;
		if (name == null) {
			int propertyNumber = 0;
			do {
				name = String.format(DEFAULT_NAME, model.getName(), propertyNumber);
				propertyNumber++;
			} while (properties.containsKey(name));
		}
		JANIPropertyEntry janiProperty = new JANIPropertyEntry();
		janiProperty.setModel(model);
		janiProperty.setValidIdentifiers(validIdentifiers);
		janiProperty.setExpression(property);
		janiProperty.setName(name);
		janiProperty.setComment(comment);
		properties.put(name, janiProperty);
	}
	
	@Override
	public void parseProperties(InputStream... inputs) throws EPMCException {
		assert inputs != null;
		for (InputStream input : inputs) {
			assert input != null;
		}
		assert false;
	}

	@Override
	public List<RawProperty> getRawProperties() {
		List<RawProperty> rawProperties = new ArrayList<>();
		for (JANIPropertyEntry entry : this.properties.values()) {
			RawProperty raw = new RawProperty();
			raw.setName(entry.getName());
			rawProperties.add(raw);
		}
		return rawProperties;
	}

	@Override
	public Expression getParsedProperty(RawProperty property) {
		return properties.get(property.getName()).getExpression();
	}
}
