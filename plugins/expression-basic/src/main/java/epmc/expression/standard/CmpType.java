package epmc.expression.standard;

import epmc.value.OperatorEq;
import epmc.value.OperatorGe;
import epmc.value.OperatorGt;
import epmc.value.OperatorLe;
import epmc.value.OperatorLt;
import epmc.value.OperatorNe;
import epmc.value.ContextValue;
import epmc.value.Operator;

/**
 * Comparison type, e.g. in quantifiers or reward properties.
 * 
 * @author Ernst Moritz Hahn
 */
public enum CmpType {
    /** Compute and return value without comparing it. */
    IS("=?", null),
    /** Check whether value computed equals given value. */
    EQ("=", OperatorEq.IDENTIFIER),
    /** Check whether value computed does not equal given value. */
    NE("!=", OperatorNe.IDENTIFIER),
    /** Check whether value computed is larger than given value. */
    GT(">", OperatorGt.IDENTIFIER),
    /** Check whether value computed is larger or equal than given value. */
    GE(">=", OperatorGe.IDENTIFIER),
    /** Check whether value computed is smaller than given value. */
    LT("<", OperatorLt.IDENTIFIER),
    /** Check whether value computed is smaller or equal than given value. */
    LE("<=", OperatorLe.IDENTIFIER);
    
    /** User-readable {@link String} representing the comparism. */
    private final String string;
    private final String operator;

    /**
     * Construct new comparison type.
     * The parameter must not be {@code null}.
     * 
     * @param string string representing comparison type.
     */
    private CmpType(String string, String operator) {
        assert string != null;
        this.string = string;
        this.operator = operator;
    }
    
    @Override
    public String toString() {
        return string;
    }
    
    /**
     * Check whether this comparison type requests to compute a value.
     * 
     * @return whether this comparison type requests to compute a value
     */
    public boolean isIs() {
        return this == IS;
    }

    /**
     * Checks whether comparison asks if computed value equals given one.
     * 
     * @return whether comparison asks if computed value equals given one
     */
    public boolean isEq() {
        return this == EQ;
    }

    /**
     * Checks whether comparison asks if computed value smaller than given one.
     * 
     * @return whether comparison asks if computed value smaller than given one
     */
    public boolean isLt() {
        return this == LT;
    }

    /**
     * Checks whether comparison asks if computed value smaller or equal to given one.
     * 
     * @return whether comparison asks if computed value smaller or equal to given one
     */
    public boolean isLe() {
        return this == LE;
    }

    /**
     * Checks whether comparison asks if computed value larger than given one.
     * 
     * @return whether comparison asks if computed value larger than given one
     */
    public boolean isGt() {
        return this == GT;
    }
    
    /**
     * Checks whether comparison asks if computed value larger or equal to given one.
     * 
     * @return whether comparison asks if computed value larger or equal to given one
     */
    public boolean isGe() {
        return this == GE;
    }

    /**
     * Transform comparison type to equivalent operator.
     * {@link CmpType#EQ} will not be translated and the function must not be
     * called on this object. For the other comparison types, the following
     * translation is used:
     * <table>
     * <tr>
     * <td>{@link CmpType#IS}</td>
     * <td>-</td>
     * </tr>
     * <tr>
     * <td>{@link CmpType#EQ}</td>
     * <td>{@link OperatorEq#IDENTIFIER}</td>
     * </tr>
     * <tr>
     * <td>{@link CmpType#NE}</td>
     * <td>{@link OperatorNe#IDENTIFIER}</td>
     * </tr>
     * <tr>
     * <td>{@link CmpType#GT}</td>
     * <td>{@link OperatorGt#IDENTIFIER}</td>
     * </tr>
     * <tr>
     * <td>{@link CmpType#GE}</td>
     * <td>{@link OperatorGe#IDENTIFIER}</td>
     * </tr>
     * <tr>
     * <td>{@link CmpType#LT}</td>
     * <td>{@link OperatorLe#IDENTIFIER}</td>
     * </tr>
     * <tr>
     * <td>{@link CmpType#LE}</td>
     * <td>{@link OperatorLe#IDENTIFIER}</td>
     * </tr>
     * </table>
     * The parameter of this function must not be {@code null}.
     * 
     * @param contextValue context value used
     * @return equivalent operator
     */
    public Operator asExOpType(ContextValue contextValue) {
        assert contextValue != null;
        assert this != IS;
        return contextValue.getOperator(operator);
    }
}
