package epmc.jani.value;

import epmc.jani.model.Locations;
import epmc.value.ContextValue;

/**
 * Context value extension for JANI.
 * Class to generate types specific types for the JANI plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ContextValueJANI {
	/** Context value used for the plugin. */
	private final ContextValue contextValue;

	/**
	 * Construct new context value extension for JANI.
	 * 
	 * @param contextValue context value to use
	 */
	public ContextValueJANI(ContextValue contextValue) {
		assert contextValue != null;
		this.contextValue = contextValue;
	}
	
	/**
	 * Obtain context value used.
	 * 
	 * @return context value used
	 */
	public ContextValue getContextValue() {
		return contextValue;
	}
	
	/**
	 * Construct type to store location from a set of locations.
	 * 
	 * @param locations set of locations the type will represent
	 * @return type to store location from a set of locations
	 */
	public TypeLocation getTypeLocation(Locations locations) {
		assert locations != null;
		TypeLocation type = new TypeLocation(contextValue, locations);
		return contextValue.makeUnique(type);
	}
}
