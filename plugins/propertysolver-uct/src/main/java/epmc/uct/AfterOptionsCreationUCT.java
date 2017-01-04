package epmc.uct;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.options.OptionTypeInteger;
import epmc.options.Options;
import epmc.options.OptionsEPMC;
import epmc.options.UtilOptions;
import epmc.plugin.AfterOptionsCreation;

public final class AfterOptionsCreationUCT implements AfterOptionsCreation {
	private final static String IDENTIFIER = "after-options-creation-propertysolver-uct";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
		Map<String,Class<?>> solvers = options.get(OptionsEPMC.PROPERTY_SOLVER_CLASS);
		assert solvers != null;
		solvers.put(PropertySolverExplicitUCT.IDENTIFIER, PropertySolverExplicitUCT.class);
        OptionTypeInteger typeInteger = UtilOptions.getTypeInteger();
        options.addOption(OptionsUCT.OPTIONS_UCT, OptionsUCT.UCT_EXAMPLE, typeInteger, "123", true, true, true);
	}
}
