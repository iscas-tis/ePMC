package epmc.rddl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class RDDLObjects {
	private final Map<String,RDDLObject> nameToObject = new LinkedHashMap<>();
    private final Map<String,List<RDDLObjectValue>> objectValues = new LinkedHashMap<>();
    private final Map<String,List<RDDLObjectValue>> objectValuesExternal = Collections.unmodifiableMap(objectValues);

	public void setObjectValues(String objectName, List<String> objectValueStrings) {
		assert objectName != null;
		assert objectValueStrings != null;
		for (String value : objectValueStrings) {
			assert value != null;
		}
		List<RDDLObjectValue> objectValues = new ArrayList<>();
		int valueNumber = 0;
		for (String value : objectValueStrings) {
			objectValues.add(new RDDLObjectValue(value, valueNumber));
			valueNumber++;
		}
		RDDLObject object = new RDDLObject(objectName, objectValues);
		for (RDDLObjectValue objectValue : objectValues) {
			objectValue.setObject(object);
		}
		this.nameToObject.put(objectName, object);
        this.objectValues.put(objectName, Collections.unmodifiableList(objectValues));
	}

	public RDDLObject getObject(String name) {
		assert name != null;
		assert this.nameToObject.containsKey(name);
		return this.nameToObject.get(name);
	}
	
	public Map<String,List<RDDLObjectValue>> getObjectValues() {
		return this.objectValuesExternal;
	}

	public List<RDDLObjectValue> getObjectValue(String of) {
    	return this.objectValues.get(of);
	}

	public boolean contains(String parameter) {
		assert parameter != null;
		return this.objectValues.containsKey(parameter);
	}
}
