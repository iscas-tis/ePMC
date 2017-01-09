package epmc.jani.model.type;

import java.util.Map;

import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.util.Util;

public final class TypeParser implements JANINode {
	private JANIType type;
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
		Map<String,Class<? extends JANIType>> types = model.getTypes();
		for (Class<? extends JANIType> clazz : types.values()) {
			JANIType tryType = Util.getInstance(clazz);
			tryType.setModel(model);
			tryType.setContextValue(model.getContextValue());
			tryType = tryType.parseAsJANIType(value);
			if (tryType != null) {
				type = tryType;
				break;
			}
		}
		assert type != null : value; // TODO exception
		return type;
	}

	@Override
	public JsonValue generate() {
		assert false;
		return null;
	}

	public JANIType getType() {
		return type;
	}	
}
