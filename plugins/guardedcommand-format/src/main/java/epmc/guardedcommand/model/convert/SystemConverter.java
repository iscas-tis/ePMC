package epmc.guardedcommand.model.convert;

import epmc.error.EPMCException;
import epmc.guardedcommand.model.ModelGuardedCommand;
import epmc.jani.model.ModelJANI;

public interface SystemConverter {
	void setGuardedCommandModel(ModelGuardedCommand modelGuardedCommand);
	
	void setJANIModel(ModelJANI modelJani);
	
	void convert() throws EPMCException;
}
