package epmc.example;

import epmc.modelchecker.CommandTask;
import epmc.modelchecker.ModelChecker;
import epmc.options.Options;

public final class ExampleCommandTask implements CommandTask{
	public final static String IDENTIFIER = "example-cmd";
	private Options options;
	
	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}
	
	@Override
	public void setOptions(Options options) {
		assert options != null;
		this.options = options;
	}

	@Override
	public void setModelChecker(ModelChecker modelChecker) {
		assert modelChecker != null;
		this.options = modelChecker.getOptions();
	}
	
	@Override
	public void executeInServer() {
		assert options != null;
	}
}
