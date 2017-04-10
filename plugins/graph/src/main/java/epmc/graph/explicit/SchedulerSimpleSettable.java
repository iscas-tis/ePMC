package epmc.graph.explicit;

import epmc.graph.Scheduler;

public interface SchedulerSimpleSettable extends SchedulerSimple {
    /**
     * Set the decision for a given node.
     * The node must be nonnegative and smaller than the number of nodes.
     * The decision must be nonnegative or equal to
     * {@link Scheduler#UNSET}. It must also be smaller than the value obtained
     * by {@link GraphExplicit#getNumSuccessors()} directly after a call to
     * {@link GraphExplicit#queryNode(int)} on the node parameter of this
     * method. If the decision is set to {@link Scheduler#UNSET}, it means that
     * there is no decision for this node, otherwise the according outgoing edge
     * of the node is selected.
     * 
     * @param node node to which to set the decision
     * @param decision decision to set
     */
    void set(int node, int decision);

}
