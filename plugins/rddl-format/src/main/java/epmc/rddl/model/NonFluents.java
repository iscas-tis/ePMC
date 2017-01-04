package epmc.rddl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import epmc.value.UtilValue;
import epmc.value.Value;

final class NonFluents {
	private final static String NON_FLUENTS = "non-fluents";
	private final static String DOMAIN = "domain";
	private final static String OBJECTS = "objects";
	private final static String EQ_SPACE = " = ";
    private String name;
    private Domain domain;
    private final RDDLObjects objects = new RDDLObjects();
    private final Map<PVariableEntry,Value> nonFluentValues = new LinkedHashMap<>();
    private final Map<PVariableEntry,Value> nonFluentValuesExternal = Collections.unmodifiableMap(nonFluentValues);
    private final Set<String> nonFluents = new LinkedHashSet<>();
    private final List<String> nonFluentsList = new ArrayList<>();
    private final List<String> nonFluentsExternal = Collections.unmodifiableList(nonFluentsList);
    
    void setName(String name) {
        assert name != null;
        this.name = name;
    }
    
    String getName() {
        return name;
    }

    public void setDomain(Domain domain) {
        assert domain != null;
        this.domain = domain;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(NON_FLUENTS + " ");
        builder.append(name);
        builder.append(" {\n");
        builder.append(indent(1) + DOMAIN + EQ_SPACE);
        builder.append(domain.getName());
        builder.append(";\n");
        appendObjects(builder);
        appendNonFluents(builder);
        builder.append("}\n");
        return builder.toString();
    }

    private void appendNonFluents(StringBuilder builder) {
    	assert builder != null;
        builder.append(indent(1) + NON_FLUENTS + " {\n");
        for (Entry<PVariableEntry, Value> entry : nonFluentValues.entrySet()) {
            String name = entry.getKey().getPVariable().getName();
            List<String> parameters = entry.getKey().getParameterStrings();
            Value value = entry.getValue();
            builder.append(indent(2));
            builder.append(name);
            if (parameters.size() > 0) {
                builder.append("(");
                for (int i = 0; i < parameters.size(); i++) {
                    builder.append(parameters.get(i));
                    if (i < parameters.size() - 1) {
                        builder.append(",");
                    }
                }
                builder.append(")");
            }
            builder.append(EQ_SPACE);
            builder.append(value);
            builder.append(";\n");
        }
        builder.append(indent(1) + "};\n");
	}

	private void appendObjects(StringBuilder builder) {
    	assert builder != null;
        builder.append(indent(1) + OBJECTS + " {\n");
        for (Entry<String, List<RDDLObjectValue>> entry : objects.getObjectValues().entrySet()) {
            builder.append(indent(2));
            builder.append(entry.getKey());
            builder.append(" : {");
            List<RDDLObjectValue> list = entry.getValue();
            for (int i = 0; i < list.size(); i++) {
                builder.append(list.get(i).getName());
                if (i < list.size() - 1) {
                    builder.append(", ");
                }
            }
            builder.append("};\n");
        }
        builder.append(indent(1) + "};\n");
	}

	public void setObjectValues(String objectName, List<String> objectValues) {
    	this.objects.setObjectValues(objectName, objectValues);
    }
    
    public Map<String, List<RDDLObjectValue>> getObjectValues() {
    	return this.objects.getObjectValues();
    }
    
    public List<RDDLObjectValue> getObjectValue(String of) {
    	assert of != null;
    	return this.objects.getObjectValue(of);
    }

    public void setNonFluent(NonFluent nonFluent, List<String> parameters, Value value) {
        assert nonFluent != null;
        assert parameters != null;
        for (String parameter : parameters) {
            assert parameter != null;
        }
        assert value != null;
        PVariableEntry entry = new PVariableEntry(nonFluent, parameters);
        if (!nonFluents.contains(nonFluent)) {
        	nonFluentsList.add(nonFluent.getName());
        	nonFluents.add(nonFluent.getName());
        }
        value = UtilValue.clone(value);
        value.setImmutable();
        nonFluentValues.put(entry, value);
    }
    
    public Map<PVariableEntry, Value> getNonFluentValues() {
        return nonFluentValuesExternal;
    }
    
    List<String> getNonFluentsNames() {
    	return nonFluentsExternal;
    }

	public RDDLObject getObject(String name) {
		assert name != null;
		assert this.objects.contains(name) : name + " " + this.objects;
		return this.objects.getObject(name);
	}

	private String indent(int level) {
    	assert level >= 0;
    	StringBuffer buffer = new StringBuffer();
    	for (int l = 0; l < level; l++) {
    		buffer.append("    ");
    	}
    	return buffer.toString();
    }
}
