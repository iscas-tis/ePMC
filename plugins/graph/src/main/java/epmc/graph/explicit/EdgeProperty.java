package epmc.graph.explicit;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueObject;

/**
 * Edge property of a graph.
 * 
 * @author Ernst Moritz Hahn
 */
public interface EdgeProperty  {
    /* methods to be implemented by implementing classes */
    
    /**
     * Get the graph to which the edge property belongs.
     * 
     * @return graph to which the edge property belongs
     */
    GraphExplicit getGraph();
    
    /**
     * Get value for an edge of the node queried last.
     * The value obtained is the value for the edge with the given number of the
     * node from the latest call of {@link GraphExplicit#queryNode(int)} of
     * the graph obtained by {@link #getGraph()}. The function must not be
     * called before the first call of {@link Graph#queryNode(int)}
     * of that graph. The successor edge number must be nonnegative and
     * strictly smaller than the value returned by a call to
     * {@link GraphExplicit#getNumSuccessors()} after a call to
     * {@link GraphExplicit#queryNode(int)} on the graph obtained by
     * {@link #getGraph()}.
     * Note that for efficiency the value this function returns may be
     * the same value (with different content) for each call of the function.
     * Thus, the value returned should not be stored by reference, but rather
     * stored e.g. in an array value or copied using {@link UtilValue#clone(Value)}.
     * 
     * @param successor number of successor edge
     * @return value for edge with the given number of the node queried last
     * @throws EPMCException thrown in case of problems obtaining the value
     */
    Value get(int successor) throws EPMCException;
    
    /**
     * Set value for edge of node queried last.
     * The value is set for the edge with the given number of the node from the
     * latest call of {@link GraphExplicit#queryNode(int)} of the graph obtained
     * by {@link #getGraph()}. The method must not be called before the first
     * call of {@link GraphExplicit#queryNode(int)} of that graph. Note that not
     * all types of edge properties may support this method. In this case, calls
     * to this method should not be performed and may result in
     * {@link AssertionError} if assertions are enabled or in other exceptions.
     * In any case, only values which can be imported by the type obtained by
     * {@link #getType()} may be set using this function.
     * The successor edge number must be nonnegative and
     * strictly smaller than the value returned by a call to
     * {@link GraphExplicit#getNumSuccessors()} after a call to
     * {@link GraphExplicit#queryNode(int)} on the graph obtained by
     * {@link #getGraph()}.
     * 
     * @param successor number of successor edge to set
     * @param value value to set for the edge
     * @throws EPMCException thrown in case of problems setting the value
     */
    void set(Value value, int successor);
    
    /**
     * Obtain type of the values returned by {@link #get(int)}.
     * 
     * @return type of the values returned by {@link #get(int)}
     */
    Type getType();


    /* default methods */
    
    /**
     * Return value of this node as object.
     * In addition to the requirements of {@link #get(int)}, the edge property
     * must indeed store object values which can be casted according to the type
     * parameter {@code T}. If this is not the case, an {@link AssertionError}
     * may be thrown if assertions are enabled.
     * 
     * @param edge number to get value of
     * @return value of given edge of node as object
     * @throws EPMCException thrown in case of problems obtaining the value
     */
    default <T> T getObject(int successor) throws EPMCException {
        assert successor >= 0;
        Value value = get(successor);
        assert ValueObject.isObject(value);
        return ValueObject.asObject(value).getObject();
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
     * Set string value for edge of node queried last.
     * The requirements and effects are as for {@link #set(Value, int)}, except
     * that the value parameter is a {@link String} and not a {@link Value}.
     * The value will be set using the {@link Value#set(String)}.
     * Note that the default implementation of this method is not tuned for
     * efficiency as it creates a new object in each call, and thus should only
     * be used if speed is not crucial.
     * 
     * @param value string value to set for the edge
     * @param successor number of successor edge to set value of
     * @throws EPMCException thrown in case of problems during setting
     */
    default void set(String value, int successor) throws EPMCException {
        Value tmpValue = getType().newValue();
        tmpValue.set(value);
        set(tmpValue, successor);
    }
}
