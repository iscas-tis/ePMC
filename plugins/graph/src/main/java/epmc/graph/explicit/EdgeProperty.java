/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

package epmc.graph.explicit;

import epmc.value.Type;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueObject;
import epmc.value.ValueSetString;

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
     */
    Value get(int node, int successor);

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
     */
    void set(int node,  int successor, Value value);

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
     */
    default <T> T getObject(int node, int successor) {
        assert successor >= 0;
        Value value = get(node, successor);
        assert ValueObject.is(value);
        return ValueObject.as(value).getObject();
    }

    /**
     * Set string value for edge of node queried last.
     * The requirements and effects are as for {@link #set(Value, int)}, except
     * that the value parameter is a {@link String} and not a {@link Value}.
     * Note that the default implementation of this method is not tuned for
     * efficiency as it creates a new object in each call, and thus should only
     * be used if speed is not crucial.
     * 
     * @param value string value to set for the edge
     * @param successor number of successor edge to set value of
     */
    default void set(int node, int successor, String value) {
        Value tmpValue = getType().newValue();
        ValueSetString.as(tmpValue).set(value);
        set(node, successor, tmpValue);
    }
}
