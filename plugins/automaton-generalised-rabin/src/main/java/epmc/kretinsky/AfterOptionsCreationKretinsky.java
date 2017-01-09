package epmc.kretinsky;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.kretinsky.automaton.AutomatonKretinskyProduct;
import epmc.kretinsky.options.KretinskyOptimiseMojmir;
import epmc.kretinsky.options.OptionsKretinsky;
import epmc.kretinsky.propertysolver.PropertySolverDDGeneralisedRabin;
import epmc.kretinsky.propertysolver.PropertySolverExplicitGeneralisedRabin;
import epmc.kretinsky.propertysolver.PropertySolverExplicitGeneralisedRabinIncremental;
import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeEnum;
import epmc.options.OptionTypeString;
import epmc.options.Options;
import epmc.options.OptionsEPMC;
import epmc.options.UtilOptions;
import epmc.plugin.AfterOptionsCreation;

public class AfterOptionsCreationKretinsky implements AfterOptionsCreation {
	private final static String IDENTIFIER = "after-options-creation-kretinsky";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
        OptionTypeString typeString = UtilOptions.getTypeString();
		OptionTypeBoolean typeBoolean = UtilOptions.getTypeBoolean();
		OptionTypeEnum typeMojmir = UtilOptions.newTypeEnum(KretinskyOptimiseMojmir.class);
        options.addProgramOption(OptionsKretinsky.KRETINSKY_LTLFILT_CMD, typeString, "ltlfilt", true, true, false);
		options.addProgramOption(OptionsKretinsky.KRETINSKY_OPTIONS, OptionsKretinsky.KRETINSKY_DISABLE_UNUSED_SLAVES, typeBoolean, true, true, true, true);
		options.addProgramOption(OptionsKretinsky.KRETINSKY_OPTIONS, OptionsKretinsky.KRETINSKY_GFFG_OPTIMISATION, typeBoolean, true, true, true, true);
		options.addProgramOption(OptionsKretinsky.KRETINSKY_OPTIONS, OptionsKretinsky.KRETINSKY_OPTIMISE_MOJMIR, typeMojmir, true, true, true, true);
        options.addProgramOption(OptionsKretinsky.KRETINSKY_OPTIONS, OptionsKretinsky.KRETINSKY_PREPROCESS_SLAVES, typeString, "run-100", true, true, true);
		Map<String, Class<?>> automatonMap = options.get(OptionsEPMC.AUTOMATON_CLASS);
		assert automatonMap != null;
		automatonMap.put(AutomatonKretinskyProduct.IDENTIFIER, AutomatonKretinskyProduct.class);
		Map<String, Class<?>> propertySolverList = options.get(OptionsEPMC.PROPERTY_SOLVER_CLASS);
		assert propertySolverList != null;
		propertySolverList.put(PropertySolverDDGeneralisedRabin.IDENTIFIER, PropertySolverDDGeneralisedRabin.class);
		propertySolverList.put(PropertySolverExplicitGeneralisedRabin.IDENTIFIER, PropertySolverExplicitGeneralisedRabin.class);
		propertySolverList.put(PropertySolverExplicitGeneralisedRabinIncremental.IDENTIFIER, PropertySolverExplicitGeneralisedRabinIncremental.class);
	}

}
