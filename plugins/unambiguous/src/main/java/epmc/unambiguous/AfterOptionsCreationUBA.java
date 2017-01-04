package epmc.unambiguous;

import java.util.Map;

import epmc.automaton.OptionsAutomaton;
import epmc.error.EPMCException;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Category;
import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeString;
import epmc.options.Options;
import epmc.options.UtilOptions;
import epmc.plugin.AfterOptionsCreation;
import epmc.unambiguous.automaton.AutomatonDDUBA;
import epmc.unambiguous.automaton.AutomatonUBA;
import epmc.unambiguous.options.OptionsLTLUBA;

public class AfterOptionsCreationUBA implements AfterOptionsCreation {

	private final static String IDENTIFIER = "after-options-creation-uba";
	
	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
		
		Category category = options.addCategory()
				.setBundleName(OptionsLTLUBA.OPTIONS_LTL_UBA)
				.setIdentifier(OptionsLTLUBA.LTL_UBA_CATEGORY)
				.build();
		
		Map<String,Class<?>> solvers = options.get(OptionsModelChecker.PROPERTY_SOLVER_CLASS);
		assert solvers != null;
//        OptionTypeBoolean typeBoolean = UtilOptions.getTypeBoolean();
        OptionTypeString typeString = UtilOptions.getTypeString();
		solvers.put(PropertySolverDDLTLUBA.IDENTIFIER, PropertySolverDDLTLUBA.class);
		solvers.put(PropertySolverExplicitLTLUBA.IDENTIFIER, PropertySolverExplicitLTLUBA.class);
		solvers.put(PropertySolverParamLTLUBA.IDENTIFIER, PropertySolverParamLTLUBA.class);
		solvers.put(PropertySolverIMCLTLUBA.IDENTIFIER, PropertySolverIMCLTLUBA.class);
		options.addOption().setBundleName(OptionsLTLUBA.OPTIONS_LTL_UBA)
		.setIdentifier(OptionsLTLUBA.LTL_UBA_LTLFILT_CMD)
		.setType(typeString).setDefault("ltlfilt")
		.setCommandLine().setGui().setWeb()
		.setCategory(category).build();
		
        options.addOption().setBundleName(OptionsLTLUBA.OPTIONS_LTL_UBA)
		.setIdentifier(OptionsLTLUBA.LTL_UBA_LP_SOLVE_METHOD)
        .setType(UtilOptions.getTypeEnum(OptionsLTLUBA.LPMethod.class))
        .setDefault(OptionsLTLUBA.LPMethod.LP)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
		
//		options.addOption().setBundleName(OptionsLTLLazy.OPTIONS_LTL_LAZY)
//			.setIdentifier(OptionsLTLLazy.LTL_LAZY_INCREMENTAL)
//			.setType(typeBoolean).setDefault(false)
//			.setCommandLine().setGui().setWeb()
//			.setCategory(category).build();
//        Map<String, Class<?>> automatonMap = options.get(OptionsAutomaton.AUTOMATON_CLASS);
//        assert automatonMap != null;
//        automatonMap.put(AutomatonUBA.IDENTIFIER, AutomatonUBA.class);
//        automatonMap.put(AutomatonDDUBA.IDENTIFIER, AutomatonDDUBA.class);		
	}

}
