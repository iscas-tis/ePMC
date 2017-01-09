package epmc.modelchecker;

import com.google.common.base.MoreObjects;

// TODO in case server functionality is indeed moved to JANI interaction plugin,
// this class might be deleted

/**
 * Class representing a single model checker result.
 * This class either represents a result for a particular property, or a
 * general result independent of all properties or for all properties
 * together.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ModelCheckerResult {
    /** String "property". */
    private final static String PROPERTY = "property";
    /** String "result". */
    private final static String RESULT = "result";
    /** Original unparsed property. */
    private final RawProperty property;
    /** Result for this property. */
    private final Object result;
    
    /**
     * Construct a new model checker result.
     * The property parameter may be {@code null} in case of a common result,
     * while the result parameter must not be {@code null}.
     * 
     * @param property property to store result for or {@code null}
     * @param result model checking result
     */
    public ModelCheckerResult(RawProperty property, Object result) {
        assert result != null;
        this.property = property;
        this.result = result;
    }
    
    /**
     * Get the property for which this object provides the result.
     * In case of a common result, {@code null} will be returned.
     * 
     * @return property for which this object provides the result
     */
    public RawProperty getProperty() {
        return property;
    }
    
    /**
     * Get the result of the model checking process.
     * 
     * @return result of the model checkign process
     */
    public Object getResult() {
        return result;
    };
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add(PROPERTY, property)
                .add(RESULT, result)
                .toString();
    }
}
