package epmc.graph.explicit;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import epmc.value.ValueEnum;
import epmc.value.ValueInteger;
import epmc.value.ValueObject;

/**
 * Node property of a graph.
 * 
 * @author Ernst Moritz Hahn
 */
public interface NodeProperty {
    /* methods to be implemented by implementing classes */

    /**
     * Get the graph to which the node property belongs.
     * 
     * @return graph to which the node property belongs
     */
    GraphExplicit getGraph();

    /**
     * Get value for node queried last.
     * The value obtained is the value for the node from the latest call of
     * {@link GraphExplicit#queryNode(int)} of the graph obtained by
     * {@link #getGraph()}. The function must not be called before the first
     * call of {@link GraphExplicit#queryNode(int)} of that graph.
     * Note that for efficiency the value this function returns may be
     * the same value (with different content) for each call of the function.
     * Thus, the value returned should not be stored by reference, but rather
     * stored e.g. in an array value or copied using {@link UtilValue#clone(Value)}.
     * 
     * @return value for node queried last
     * @throws EPMCException thrown in case of problems obtaining the value
     */
    Value get() throws EPMCException;
    
    /**
     * Set value for node queried last.
     * The value is set for the node from the latest call of
     * {@link GraphExplicit#queryNode(int)} of the graph obtained by
     * {@link #getGraph()}. The function must not be called before the first
     * call of {@link GraphExplicit#queryNode(int)} of that graph. Note that not
     * all types of node properties may support this method. In this case, calls
     * to this method should not be performed and may result in
     * {@link AssertionError} if assertions are enabled or in other exceptions.
     * In any case, only values which can be imported by the type obtained by
     * {@link #getType()} may be set using this function.
     * 
     * @param value value to set for the node
     * @throws EPMCException thrown in case of problems setting the value
     */
    void set(Value value) throws EPMCException;

    /**
     * Obtain type of the values returned by {@link #get()}.
     * 
     * @return type of the values returned by {@link #get()}
     */
    Type getType();
    

    /* default methods */

    /**
     * Return value of this node as boolean.
     * In addition to the requirements of {@link #get()}, the node property must
     * indeed store boolean values. If this is not the case, an
     * {@link AssertionError} may be thrown if assertions are enabled.
     * 
     * @return value of this node as boolean
     * @throws EPMCException thrown in case of problems obtaining the value
     */
    default boolean getBoolean() throws EPMCException {
        Value value = get();
        assert ValueBoolean.isBoolean(value);
        return ValueBoolean.asBoolean(value).getBoolean();
    }

    /**
     * Return value of this node as integer.
     * In addition to the requirements of {@link #get()}, the node property must
     * indeed store integer values. If this is not the case, an
     * {@link AssertionError} may be thrown if assertions are enabled.
     * 
     * @return value of this node as integer
     * @throws EPMCException thrown in case of problems obtaining the value
     */
    default int getInt() throws EPMCException {
        Value value = get();
        assert ValueInteger.isInteger(value);
        return ValueInteger.asInteger(value).getInt();
    }

    /**
     * Return value of this node as object.
     * In addition to the requirements of {@link #get()}, the node property must
     * indeed store object values. If this is not the case, an
     * {@link AssertionError} may be thrown if assertions are enabled.
     * 
     * @return value of this node as object
     * @throws EPMCException thrown in case of problems obtaining the value
     */
    default <T> T getObject() throws EPMCException {
        Value value = get();
        assert ValueObject.isObject(value) : value + " " + value.getType();
        return ValueObject.asObject(value).getObject();
    }
    
    /**
     * Return value of this node as enum.
     * In addition to the requirements of {@link #get()}, the node property must
     * indeed store enum values. If this is not the case, an
     * {@link AssertionError} may be thrown if assertions are enabled.
     * 
     * @return value of this node as enum
     * @throws EPMCException thrown in case of problems obtaining the value
     */
    default <T extends Enum<?>> T getEnum() throws EPMCException {
        Value value = get();
        assert ValueEnum.isEnum(value);
        return ValueEnum.asEnum(value).getEnum();
    }
    
    /**
     * Get value context used by graph of this node property.
     * 
     * @return value context used by graph of this node property
     */
    default ContextValue getContextValue() {
        return getGraph().getContextValue();
    }
    
    /**
     * Set object value for node queried last.
     * The method has the same functionality as {@link #set(Value)}, except that
     * its parameter is an {@link Object} rather than a {@link Value}. In
     * addition to the restrictions of {@link #set(Value)}, the type obtained
     * by {@link #getType()} must be an object type, otherwise calls of this
     * method may result in an {@Link AssertionError} if assertions are enabled
     * or other exceptions. Also, not all node property classes may implement
     * this method, also resulting in runtime exceptions or errors if this is
     * not the case.
     * 
     * @param value object value to set for the node
     * @throws EPMCException thrown in case of problems setting the value
     */
    default void set(Object object) throws EPMCException {
        assert object != null;
        assert false : getClass();
    }    

    /**
     * Set integer value for node queried last.
     * The method has the same functionality as {@link #set(Value)}, except that
     * its parameter is an integer rather than a {@link Value}. In addition to
     * the restrictions of {@link #set(Value)}, the type obtained by
     * {@link #getType()} must be able to import integers, otherwise calls of
     * this method may result in an {@Link AssertionError} if assertions are
     * enabled or other exceptions. Also, not all node property classes may
     * implement this method, also resulting in runtime exceptions or errors if
     * this is not the case.
     * 
     * @param value integer value to set for the node
     * @throws EPMCException thrown in case of problems setting the value
     */
    default void set(int value) throws EPMCException {
        assert false : getClass();
    }    

    /**
     * Set enum value for node queried last.
     * The method has the same functionality as {@link #set(Value)}, except that
     * its parameter is an {@link Enum} rather than a {@link Value}. In
     * addition to the restrictions of {@link #set(Value)}, the type obtained
     * by {@link #getType()} must be an enum type, otherwise calls of this
     * method may result in an {@Link AssertionError} if assertions are enabled
     * or other exceptions. Also, not all node property classes may implement
     * this method, also resulting in runtime exceptions or errors if this is
     * not the case.
     * 
     * @param value enum value to set for the node
     * @throws EPMCException thrown in case of problems setting the value
     */
    default void set(Enum<?> object) throws EPMCException {
        assert object != null;
        assert false : getClass();
    }
}