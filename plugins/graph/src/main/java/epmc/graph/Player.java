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
