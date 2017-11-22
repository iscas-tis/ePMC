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
 * Enum type for the player associated to a node.
 * The player is the instance which chooses the decisions in a given node. In
 * Markov decision processes, there is only a random player, because all choices
 * are purely stochastic. In labeled transition systems, there is one
 * nondeterministic player, because the successors of each node are chosen
 * nondeterministically. In a Markov decision process, there is a
 * nondeterministic player and a stochastic player, because in each state we
 * have a nondeterministic choice between several distributions, while the
 * distributions are resolved by a stochastic player. In non-probabilistic
 * two-player games, there are two different nondeterministic players. In
 * stochastic two-player games, there is one stochastic player and there are
 * two different nondeterministic players.
 * 
 * @author Ernst Moritz Hahn
 */
public enum Player {
    /** Randomised player, "1/2" player. */
    STOCHASTIC,
    /** Nondeterministic player 1. */
    ONE,
    /** Nondeterministic player 2. */
    TWO,
    /** Node in which a player 1 decision is immediately followed by a
     * stochastic one. This player type is used in DD-based symbolic model
     * representations, in which stochastic nodes are not explicitly encoded.
     * */
    ONE_STOCHASTIC,
    /** Node in which a player 2 decision is immediately followed by a
     * stochastic one. This player type is used in DD-based symbolic model
     * representations, in which stochastic nodes are not explicitly encoded.
     * */
    TWO_STOCHASTIC
}
