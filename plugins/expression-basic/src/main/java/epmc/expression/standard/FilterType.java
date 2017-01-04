package epmc.expression.standard;

import java.io.Serializable;

public enum FilterType implements Serializable {
    MIN("min", true),
    MAX("max", true),
    COUNT("count", true),
    SUM("sum", true),
    AVG("avg", true),
    FIRST("first", true),
    RANGE("range", true),
    FORALL("forall", true),
    EXISTS("exists", true),
    STATE("state", true),
    ARGMIN("argmin", false),
    ARGMAX("argmax",false),
    PRINT("print", false),
    PRINTALL("printall", false);
    
    private final String string;
    private final boolean singleValue;
    
    private FilterType(String string, boolean singleValue) {
        this.string = string;
        this.singleValue = singleValue;
    }
    
    @Override
    public String toString() {
        return string;
    }
    
    public boolean isSingleValue() {
        return singleValue;
    }
}
