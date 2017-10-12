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
     */
    Value get(int node);

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
     */
    void set(int node, Value value);

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
     */
    default boolean getBoolean(int node) {
        Value value = get(node);
        assert ValueBoolean.is(value);
        return ValueBoolean.as(value).getBoolean();
    }

    /**
     * Return value of this node as integer.
     * In addition to the requirements of {@link #get()}, the node property must
     * indeed store integer values. If this is not the case, an
     * {@link AssertionError} may be thrown if assertions are enabled.
     * 
     * @return value of this node as integer
     */
    default int getInt(int node) {
        Value value = get(node);
        assert ValueInteger.is(value);
        return ValueInteger.as(value).getInt();
    }

    /**
     * Return value of this node as object.
     * In addition to the requirements of {@link #get()}, the node property must
     * indeed store object values. If this is not the case, an
     * {@link AssertionError} may be thrown if assertions are enabled.
     * 
     * @return value of this node as object
     */
    default <T> T getObject(int node) {
        Value value = get(node);
        assert ValueObject.is(value) : value + " " + value.getType();
        return ValueObject.as(value).getObject();
    }

    /**
     * Return value of this node as enum.
     * In addition to the requirements of {@link #get()}, the node property must
     * indeed store enum values. If this is not the case, an
     * {@link AssertionError} may be thrown if assertions are enabled.
     * 
     * @return value of this node as enum
     */
    default <T extends Enum<?>> T getEnum(int node) {
        Value value = get(node);
        assert ValueEnum.is(value);
        return ValueEnum.as(value).getEnum();
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
     */
    default void set(int node, Object object) {
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
     */
    default void set(int node, int value) {
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
     */
    default void set(int node, Enum<?> object) {
        assert object != null;
        assert false : getClass();
    }
}
