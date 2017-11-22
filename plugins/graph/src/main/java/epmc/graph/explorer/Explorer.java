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

import java.util.Collection;

import epmc.expression.Expression;
import epmc.graph.CommonProperties;
import epmc.graph.LowLevel;
import epmc.graph.StateSet;
import epmc.graph.explicit.GraphExplicit;
import epmc.value.Type;
import epmc.value.Value;

/**
 * <p>
 * Explicit-state model explorer.
 * </p>
 * <p>
 * Classes implementing this interface allow for the explicit-state exploration
 * of models. In contrast to {@link GraphExplicit}, the nodes of the explorer
 * are not integers but object which store information about state variables
 * etc. Explorers do not automatically store nodes as in graphs, where each node
 * is identified by an integer. Explorers are thus meant to either be used for
 * stochastic simulation or to be used for explicit-state model checking by
 * exploring them to an according graph.
 * </p>
 * <p>
 * In this interface as well as in the graph interfaces, we distinguish between
 * nodes and states. States are the actual states of the model, whereas
 * non-state are intermediate nodes which are used to e.g. represent probability
 * distributions of nondeterministic models. This approach has the advantage
 * that it is easier to e.g. write methods for attractor computation, where
 * informations about probability distributions as well as for states have to be
 * stored.
 * </p>
 * @author Ernst Moritz Hahn
 */
public interface Explorer extends LowLevel {
    /* methods to be implemented by implementing classes. */

    /**
     * Get the initial nodes of the explorer.
     * 
     * @return initial nodes of the explorer
     */
    Collection<? extends ExplorerNode> getInitialNodes();

    /**
     * <p>
     * Queries a given node.
     * </p>
     * <p>
     * After a call to this function, the methods {@link #getNumSuccessors()}
     * and {@link #getSuccessorNode(int)} can be used to obtain further
     * information about this node. Also, the values obtained by edge and node
     * properties obtained by {@link #getNodeProperty(Object)} and
     * {@link #getEdgeProperty(Object)} now refer to the node which was queried.
     * </p>
     * <p>
     * The method must not be called with a {@code null} parameter. It may only
     * be called with a node which belongs to this explorer, that is, it was
     * obtained by {@link #getInitialNodes()},
     * {@link #getSuccessorNode(int)}, {@link #newNode()} or by cloning existing
     * nodes of the explorer. Nodes obtained using
     * {@link #getSuccessorNode(int)} should not be used directly with this
     * function; they might be cloned or {@link ExplorerNode#set(ExplorerNode)}
     * to an existing node of the explorer.
     * </p>
     * 
     * @param node node to query
     */
    void queryNode(ExplorerNode node);

    /**
     * Obtain number of successor nodes.
     * The number refers to the node on which
     * {@link Explorer#queryNode(ExplorerNode)} was called last. The method must
     * not be called before {@link #queryNode(ExplorerNode)} was called the
     * first time.
     * 
     * @return number of successor nodes
     */
    int getNumSuccessors();

    /**
     * Obtain successor node with the given number.
     * The result refers to the node on which {@link #queryNode(ExplorerNode)}
     * was called last. The method must not be called before
     * {@link #queryNode(ExplorerNode)} was called the first time. The number
     * parameter must be nonnegative and smaller than the result of
     * {@link #getNumSuccessors()} after the call to
     * {@link #queryNode(ExplorerNode)}.
     * 
     * @param number number of successor to obtain
     * @return successor with the given number
     */
    ExplorerNode getSuccessorNode(int number);

    /**
     * Obtain graph property with given identifier.
     * The method will return the given graph property as {@link Value} or
     * {@code null} if there is no such graph property in the explorer.
     * The property parameter may not be {@code null}.
     * 
     * @param property identifier of property to obtain
     * @return property with given identifier, or {@code null}
     * @see CommonProperties
     */
    Value getGraphProperty(Object property);

    /**
     * Obtain node property with given identifier.
     * The method will return the given node property or {@code null} if there
     * is no such node property in the explorer. The property parameter may not
     * be {@code null}.
     * 
     * @param property identifier of property to obtain
     * @return property with given identifier, or {@code null}
     * @see CommonProperties
     */    
    ExplorerNodeProperty getNodeProperty(Object property);

    /**
     * Obtain edge property with given identifier.
     * The method will return the given node property or {@code null} if there
     * is no such edge property in the explorer. The property parameter may not
     * be {@code null}.
     * 
     * @param property identifier of property to obtain
     * @return property with given identifier, or {@code null}
     * @see CommonProperties
     */    
    ExplorerEdgeProperty getEdgeProperty(Object property);

    /**
     * Construct a new explorer node.
     * The node node returned is uninitialised and must be initialised using
     * {@link ExplorerNode#set(ExplorerNode)} or
     * {@link ExplorerNode#read(epmc.util.BitStream) before using
     * it.
     * 
     * @return new explorer node
     */
    ExplorerNode newNode();

    /**
     * Obtain maximal number of bits needed to store a node of this explorer.
     * The actual number of bits written and read by
     * {@link ExplorerNode#write(epmc.util.BitStream) and
     * {@link ExplorerNode#read(epmc.util.BitStream)} might be
     * smaller. If this function returns {@link Integer#MAX_VALUE}, there is no
     * upper bound on the number of bits of an explorer node. Note that then the
     * number of bits needed to store an individual node can still be obtained
     * by {@link ExplorerNode#getNumBits()}.
     * 
     * @return maximal number of bits needed to store a node of this explorer
     */
    int getNumNodeBits();


    /* default methods. */

    /**
     * Obtain type of graph property with given identifier.
     * The method will return the given graph property type or {@code null} if
     * there is no such graph property in the explorer. The property parameter
     * may not be {@code null}.
     * 
     * @param property identifier of property type to obtain
     * @return type of property with given identifier, or {@code null}
     * @see CommonProperties
     */    
    default Type getGraphPropertyType(Object property) {
        assert property != null;
        Value graphProperty = getGraphProperty(property);
        if (graphProperty == null) {
            return null;
        }
        return graphProperty.getType();
    }

    /**
     * Obtain type of node property with given identifier.
     * The method will return the given node property type or {@code null} if
     * there is no such node property in the explorer. The property parameter
     * may not be {@code null}.
     * 
     * @param property identifier of property type to obtain
     * @return type of property with given identifier, or {@code null}
     * @see CommonProperties
     */    
    default Type getNodePropertyType(Object property) {
        assert property != null;
        if (getNodeProperty(property) == null) {
            return null;
        }
        return getNodeProperty(property).getType();
    }

    /**
     * Obtain type of edge property with given identifier.
     * The method will return the given edge property type or {@code null} if
     * there is no such edge property in the explorer. The property parameter
     * may not be {@code null}.
     * 
     * @param property identifier of property type to obtain
     * @return type of property with given identifier, or {@code null}
     * @see CommonProperties
     */    
    default Type getEdgePropertyType(Object property) {
        assert property != null;
        assert getEdgeProperty(property) != null : property;
        return getEdgeProperty(property).getType();
    }

    @Override
    default StateSet newInitialStateSet() {
        return new StateSetExplorer<>(this, getInitialNodes());
    }

    @Override
    default Type getType(Expression expression) {
        assert expression != null;
        Type type = null;
        type = getGraphPropertyType(expression);
        if (type != null) {
            return type;
        }
        type = getNodePropertyType(expression);
        if (type != null) {
            return type;
        }
        type = getEdgePropertyType(expression);
        if (type != null) {
            return type;
        }
        return null;
    }
}
