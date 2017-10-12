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

package epmc.graph.explorer;

import epmc.value.Type;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueBoolean;

/**
 * Node properties of an explorer.
 * This class allows to associate properties to the nodes of an
 * {@link Explorer}.
 * 
 * @author Ernst Moritz Hahn
 */
public interface ExplorerNodeProperty {
    /* methods to be implemented by implementing classes */

    /**
     * Get value for node queried last.
     * The value obtained is the value for the node from the latest call of
     * {@link Explorer#queryNode(ExplorerNode)} of the explorer obtained by
     * {@link #getExplorer()}. The function must not be called before the first
     * call of {@link Explorer#queryNode(ExplorerNode)} of that explorer.
     * Note that for efficiency the value this function returns may be
     * the same value (with different content) for each call of the function.
     * Thus, the value returned should not be stored by reference, but rather
     * stored e.g. in an array value or copied using {@link UtilValue#clone(Value)}.
     * 
     * @return value for node queried last
     */
    Value get();

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
    default boolean getBoolean() {
        Value value = get();
        assert ValueBoolean.is(value);
        return ValueBoolean.as(value).getBoolean();
    }
}
