package epmc.graph.explorer;

import epmc.util.BitStoreable;
import epmc.util.BitStream;

/**
 * Node of an {@link Explorer}.
 * This class represents states or intermediate nodes, such as distributions,
 * of a an explicit-state explorer.
 * 
 * @author Ernst Moritz Hahn
 */
public interface ExplorerNode extends Cloneable, BitStoreable {
    /**
     * Get the explorer the node belongs to.
     * 
     * @return explorer the node belongs to
     */
    Explorer getExplorer();
    
    ExplorerNode clone();

    @Override
    boolean equals(Object obj);

    @Override
    int hashCode();
    
    @Override
    void read(BitStream reader);
    
    @Override
    void write(BitStream writer);

    /**
     * Set the content of the node to the one of an existing node.
     * Afterwards, the {@link #equals(Object)} method should return {@code true}
     * when applied on this and the other node.
     * 
     * @param node node the content of which this node should be set
     */
    void set(ExplorerNode node);

    /**
     * Obtain the number of nodes needed to store this particular node.
     * The result of this method will not be larger than the result of {@link
     * Explorer#getNumNodeBits()} of the explorer obtained by
     * {@link #getExplorer()}.
     * 
     * @return number of nodes needed to store this particular node
     */
    int getNumBits();
}
