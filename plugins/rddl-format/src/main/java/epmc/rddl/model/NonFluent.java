package epmc.rddl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import epmc.rddl.value.UtilValue;
import epmc.value.Type;
import epmc.value.Value;

public class NonFluent implements PVariable {
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
        this.type = type;
    }
    
    void setDefault(Value defaultValue) {
        this.defaultValue = defaultValue;
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
        builder.append(" : { non-fluent, ");
        builder.append(UtilValue.toString(type));
        builder.append(", default = ");
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
}
