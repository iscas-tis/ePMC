package epmc.modelchecker;

/**
 * Model checking engine.
 * The engine decides the principle way in which low-level models are
 * represented and in which model checking is performed. Examples include
 * <ul>
 * <li>explicit-state representation of the complete model,</li>
 * <li>decision-diagram-based representation of the complete model,</li>
 * <li>predicate abstraction,</li>
 * <li>simulation-based partial exploration of the model.</li>
 * </ul>
 * EPMC engines should be singleton objects. Thus, they should have a private
 * constructor and provide a static method {@code getInstance()} to obtain the
 * single instance existing for each engine class.
 * 
 * @author Ernst Moritz Hahn
 */
public interface Engine {
    /**
     * Get identifier string for this engine.
     * The identifier can be used e.g. to have a textual representation to allow
     * the user to choose between different engines.
     * 
     * @return identifier string for this engine
     */
    String getIdentifier();
}
