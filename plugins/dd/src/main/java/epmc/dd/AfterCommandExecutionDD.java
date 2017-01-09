package epmc.dd;

import epmc.error.EPMCException;
import epmc.plugin.AfterCommandExecution;
import epmc.value.ContextValue;

public final class AfterCommandExecutionDD implements AfterCommandExecution {
	public final static String IDENTIFIER = "after-command-execution-dd";
	
	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(ContextValue contextValue) throws EPMCException {
		assert contextValue != null;
		ContextDD.close(contextValue);
	}

}
