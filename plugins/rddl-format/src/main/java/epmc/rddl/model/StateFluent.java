package epmc.rddl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import epmc.rddl.value.UtilValue;
import epmc.value.Type;
import epmc.value.Value;

final class StateFluent implements PVariable {
	private final static String STATE_FLUENT = "state-fluent";
	private final static String DEFAULT = "default";
	private final static String EQ_SPACE = " = ";
    private String name;
    private Type type;
    private Value defaultValue;
    private final List<String> parameters = new ArrayList<>();
    private final List<String> parametersExternal = Collections.unmodifiableList(parameters);
    
    void setName(String name) {
        assert name != null;
        this.name = name;
    }
    
    void setType(Type type) {
        assert type != null;
        this.type = type;
    }
    
    void setDefault(Value defaultValue) {
        assert defaultValue != null;
        this.defaultValue = epmc.value.UtilValue.clone(defaultValue);
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public Type getType() {
        return this.type;
    }
    
    Value getDefault() {
        return defaultValue;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
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
        builder.append(" : { " + STATE_FLUENT + ", ");
        builder.append(UtilValue.toString(type));
        builder.append(", " + DEFAULT + EQ_SPACE);
        builder.append(defaultValue);
        builder.append(" };");
        return builder.toString();
    }

    public void setParameters(List<String> parameters) {
        this.parameters.addAll(parameters);
    }

    @Override
    public List<String> getParameterStrings() {
        return parametersExternal;
    }

	public StateFluent replaceParameters(List<String> parameters) {
		StateFluent result = new StateFluent();
		result.setName(this.name);
		result.setType(this.type);
		result.setDefault(this.defaultValue);
		result.setParameters(parameters);
		return result;
	}
}
