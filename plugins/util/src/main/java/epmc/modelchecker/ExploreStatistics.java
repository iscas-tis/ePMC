package epmc.modelchecker;

import java.io.Serializable;
import java.math.BigInteger;

public class ExploreStatistics implements Serializable {
    private static final long serialVersionUID = 1L;
    private final BigInteger numNodes;
    private final BigInteger numStates;
    private final BigInteger numTransitions;

    public ExploreStatistics(BigInteger numNodes, BigInteger numStates,
            BigInteger numTransitions) {
        assert numNodes != null;
        assert numStates != null;
        assert numTransitions != null;
        assert numNodes.compareTo(BigInteger.valueOf(0)) >= 0;
        assert numStates.compareTo(BigInteger.valueOf(0)) >= 0;
        assert numTransitions.compareTo(BigInteger.valueOf(0)) >= 0;
        assert numStates.compareTo(numNodes) <= 0;
        this.numNodes = numNodes;
        this.numStates = numStates;
        this.numTransitions = numTransitions;
    }
    
    public ExploreStatistics(int numNodes, int numStates, int numTransitions) {
        this(BigInteger.valueOf(numNodes), BigInteger.valueOf(numStates),
                BigInteger.valueOf(numTransitions));
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("numNodes: " + numNodes + "\n");
        builder.append("numStates: " + numStates + "\n");
        builder.append("numTransitions: " + numTransitions + "\n");
        return builder.toString();
    }
    
    public BigInteger getNumNodes() {
        return numNodes;
    }
    
    public BigInteger getNumStates() {
        return numStates;
    }
    
    public BigInteger getNumTransitions() {
        return numTransitions;
    }
}
