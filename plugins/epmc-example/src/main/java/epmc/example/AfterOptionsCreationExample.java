package epmc.example;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.modelchecker.CommandTask;
import epmc.options.Options;
import epmc.options.OptionsEPMC;
import epmc.plugin.*;

public final class AfterOptionsCreationExample implements AfterOptionsCreation {
	private final static String IDENTIFIER = "after-options-creation-example";
	public String getIdentifier() { 
		return IDENTIFIER;
	}

	public void process(Options options) throws EPMCException {
		assert options != null;
		System.out.println("processing the after-options-creation operation of plugin epmc-example");
		// now try to load the CommandTask
		Map<String,Class<? extends CommandTask>> commandTaskClasses = options.get(OptionsEPMC.COMMAND_TASK_CLASS);
		 assert commandTaskClasses != null;
	     options.addCommand(null, ExampleCommandTask.IDENTIFIER, true, false, false);
	     commandTaskClasses.put(ExampleCommandTask.IDENTIFIER, ExampleCommandTask.class);
	     App.run();
	}

}
