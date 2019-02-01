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

package epmc.graph;

/**
 * Common graph, node, and edge properties.
 * 
 * @author Ernst Moritz Hahn
 */
public enum CommonProperties {
    /* graph properties */
    /** Model on which a given graph is based */
    MODEL,
    /** Semantics of a given graph */
    SEMANTICS,
    /** number of labels in an automaton */
    NUM_LABELS,
    /** ExpressionToDD */
    EXPRESSION_TO_DD,
    /** model graph on which a product graph with an automaton is based on */
    MODEL_GRAPH,
    /** automaton on which a product graph with a model is based on */
    AUTOMATON,
    /** inner graph in a wrapped graph */
    INNER_GRAPH,

    /* node properties */
    /** Whether graph node is a state or not. */
    STATE,
    /** By which Player a node is controlled. */
    PLAYER,
    /** Whether a Markov automaton state is stable. */
    STABLE,
    /** Explorer node from which model node was built. */
    NODE_EXPLORER,
    /** Model node part of a model automaton product. */
    NODE_MODEL,
    /** Automaton node part of a model automaton product. */
    NODE_AUTOMATON,

    /* edge properties */
    /** edge weight (rate or probability) */
    WEIGHT,
    /** label of an automaton (for acceptance condition) */
    AUTOMATON_LABEL,
    /** transition label (as e.g. used for synchronisation or rewards) */
    TRANSITION_LABEL,
    /** identifies command/edge/etc in high-level model */
    DECISION,
    AUTOMATON_NAME,    
}
