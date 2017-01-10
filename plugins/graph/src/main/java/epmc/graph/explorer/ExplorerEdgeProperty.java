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

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueObject;

/**
 * Edge properties of an explorer.
 * This class allows to associate properties to the edges of an
 * {@link Explorer}.
 * 
 * @author Ernst Moritz Hahn
 */
public interface ExplorerEdgeProperty {
    /* methods to be implemented by implementing classes */

    /**
     * Get the explorer to which the edge property belongs.
     * 
     * @return explorer to which the edge property belongs
     */
    Explorer getExplorer();
    
    /**
     * Get value for an edge of the node queried last.
     * The value obtained is the value for the edge with the given number of the
     * node from the latest call of {@link Explorer#queryNode(ExplorerNode)} of
     * the explorer obtained by {@link #getExplorer()}. The function must not be
     * called before the first call of {@link Explorer#queryNode(ExplorerNode)}
     * of that explorer. The successor edge number must be nonnegative and
     * strictly smaller than the value returned by a call to
     * {@link Explorer#getNumSuccessors()} after a call to
     * {@link Explorer#queryNode(ExplorerNode)} on the explorer obtained by
     * {@link #getExplorer()}.
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
     * Obtain type of the values returned by {@link #get(int)}.
     * 
     * @return type of the values returned by {@link #get(int)}
     */
    Type getType();

    
    /* default methods */
    
    default <T> T getObject(int successor) throws EPMCException {
        assert successor >= 0;
        ValueObject value = ValueObject.asObject(get(successor));
        return value.getObject();
    }
   
    /**
     * Get value context used by explorer of this edge property.
     * 
     * @return value context used by explorer of this edge property
     */
    default ContextValue getContextValue() {
        return getExplorer().getContextValue();
    }
}
