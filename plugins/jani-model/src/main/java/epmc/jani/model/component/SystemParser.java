package epmc.jani.model.component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.json.JsonObject;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.util.UtilJSON;

/**
 * System parser of JANI model.
 * This class is responsible for detecting of which subtype a system component
 * is of so as to obtain a proper instance.
 * 
 * @author Ernst Moritz Hahn
 */
public final class SystemParser implements JANINode {
	/** String to identify the type of composition. */
	private final static String COMPOSITION = "composition";
	/** Maps composition names to its representing classes */
	private final static Map<String,Class<? extends Component>> COMPOSITIONS;
	
	static {
		Map<String,Class<? extends Component>> compositions = new LinkedHashMap<>();
		compositions.put(ComponentParallel.IDENTIFIER, ComponentParallel.class);
		compositions.put(ComponentRename.IDENTIFIER, ComponentRename.class);
		compositions.put(ComponentAutomaton.IDENTIFIER, ComponentAutomaton.class);
		COMPOSITIONS = Collections.unmodifiableMap(compositions);
	}

	/** System component parsed by this system parser. */
	private Component systemComponent;
	private ModelJANI model;

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
		JsonObject object = UtilJSON.toObject(value);
		Class<? extends Component> composition;
		if (object.containsKey(COMPOSITION)) {
			composition = UtilJSON.toOneOf(object, COMPOSITION, COMPOSITIONS);			
		} else {
			composition = ComponentSynchronisationVectors.class;
		}
		try {
			systemComponent = composition.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
			assert false;
		}
		systemComponent.setModel(model);
		systemComponent.parse(value);
		return this;
	}

	@Override
	public JsonValue generate() {
		assert false;
		return null;
	}

	/**
	 * Return the parsed system component.
	 * 
	 * @return parsed system component
	 */
	public Component getSystemComponent() {
		return systemComponent;
	}
	
	@Override
	public String toString() {
		return UtilModelParser.toString(this);
	}
}
