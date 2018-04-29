package epmc.imdp;

/**
 * Enum to state in which way interval nondeterminism is resolved.
 * 
 * @author Ernst Moritz Hahn
 */
public enum IntervalPlayer {
    /** Resolved in same direction as action nondeterminism. */
    COOPERATIVE,
    /** Resolved in opposite direction as action nondeterminism. */
    ANTAGONISTIC,
}
