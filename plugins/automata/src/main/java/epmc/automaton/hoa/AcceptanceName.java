package epmc.automaton.hoa;

import java.util.ArrayList;

public final class AcceptanceName {
    private final static String SPACE = " ";
    private String name;
    private final ArrayList<Object> parameters = new ArrayList<>();

    void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    void addParameter(String identifier) {
        assert identifier != null;
        parameters.add(identifier);
    }
    
    void addParameter(int integer) {
        parameters.add(integer);
    }
    
    public int getNumParameters() {
        return parameters.size();
    }
    
    public AcceptanceNameParameterType getParameterType(int parameterNumber) {
        assert parameterNumber >= 0 : parameterNumber;
        assert parameterNumber < parameters.size() : parameterNumber + SPACE + parameters.size();
        if (parameters.get(parameterNumber) instanceof String) {
            return AcceptanceNameParameterType.IDENTIFIER;
        } else if (parameters.get(parameterNumber) instanceof Integer) {
            return AcceptanceNameParameterType.INTEGER;
        } else {
            throw new RuntimeException(parameters.get(parameterNumber).getClass().toString());
        }
    }
    
    public String getParameterIdentifier(int parameterNumber) {
        assert parameterNumber >= 0 : parameterNumber;
        assert parameterNumber < parameters.size() : parameterNumber + SPACE + parameters.size();
        assert parameters.get(parameterNumber) instanceof String;
        return (String) parameters.get(parameterNumber);
    }
    
    public int getParameterInteger(int parameterNumber) {
        assert parameterNumber >= 0 : parameterNumber;
        assert parameterNumber < parameters.size() : parameterNumber + SPACE + parameters.size();
        assert parameters.get(parameterNumber) instanceof Integer;
        return (Integer) parameters.get(parameterNumber);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(name);
        if (parameters.size() > 0) {
            builder.append(SPACE);
        }
        for (int paramNr = 0; paramNr < parameters.size(); paramNr++) {
            builder.append(parameters.get(paramNr));
            if (paramNr < parameters.size() - 1) {
                builder.append(SPACE);
            }
        }
        return builder.toString();
    }
}
