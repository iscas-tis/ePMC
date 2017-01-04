package epmc.rddl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RDDLObject {
	private final String name;
	private final List<RDDLObjectValue> values = new ArrayList<>();
	private final List<RDDLObjectValue> valuesExternal = Collections.unmodifiableList(values);
	private final Map<String,RDDLObjectValue> nameToObjectValue = new LinkedHashMap<>();
	
	RDDLObject(String name, List<RDDLObjectValue> values) {
		assert name != null;
		assert values != null;
		for (RDDLObjectValue value : values) {
			assert value != null;
		}
		this.name = name;
		this.values.addAll(values);
		for (RDDLObjectValue value : values) {
			this.nameToObjectValue.put(value.getName(), value);
		}
	}
	
	public String getName() {
		return name;
	}
	
	public List<RDDLObjectValue> getValues() {
		return valuesExternal;
	}
	
	public RDDLObjectValue getValue(String name) {
		assert name != null;
		return this.nameToObjectValue.get(name);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("(");
		builder.append(this.name);
		builder.append(",");
		builder.append(this.values);
		builder.append(")");
		return builder.toString();
	}
	
	public int numValues() {
		return this.values.size();
	}
}
