package epmc.jani.plugin;

import epmc.error.EPMCException;
import epmc.plugin.BeforeModelCreation;
import epmc.value.ContextValue;

/**
 * JANI plugin class containing method to execute just before model creation.
 * 
 * @author Ernst Moritz Hahn
 */
public final class BeforeModelCreationJANI implements BeforeModelCreation {
	/** Identifier of this class. */
	public final static String IDENTIFIER = "before-model-loading-jani";
	
	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(ContextValue contextValue) throws EPMCException {
	}
}
