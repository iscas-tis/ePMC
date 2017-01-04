package epmc.rddl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import epmc.rddl.value.UtilValue;
import epmc.value.Type;

final class IntermediateFluent implements PVariable {
	private final static String INTERM_FLUENT = "interm-fluent";
	private final static String LEVEL = "level";
	private final static String EQ_SPACE = " = ";
    private String name;
    private Type type;
    private int level;
    private final List<String> parameters = new ArrayList<>();
    private final List<String> parametersExternal = Collections.unmodifiableList(parameters);
    
    void setName(String name) {
        this.name = name;
    }
    
    void setType(Type type) {
        this.type = type;
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public Type getType() {
        return this.type;
    }
    
    public void setLevel(int level) {
        assert level > 0 : level;
        this.level = level - 1;
    }

    public int getLevel() {
        return this.level;
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
        builder.append(" : { " + INTERM_FLUENT + ", ");
        builder.append(UtilValue.toString(type));
        builder.append(", " + LEVEL + EQ_SPACE);
        builder.append(level + 1);
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
