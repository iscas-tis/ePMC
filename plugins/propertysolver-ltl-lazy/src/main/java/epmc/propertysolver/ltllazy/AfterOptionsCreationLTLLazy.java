package epmc.propertysolver.ltllazy;

import java.util.Map;

import epmc.automaton.OptionsAutomaton;
import epmc.error.EPMCException;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Category;
import epmc.options.OptionTypeBoolean;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;
import epmc.propertysolver.ltllazy.automata.AutomatonBreakpoint;
import epmc.propertysolver.ltllazy.automata.AutomatonSubset;

public final class AfterOptionsCreationLTLLazy implements AfterOptionsCreation {
	private final static String IDENTIFIER = "after-options-creation-ltl-lazy";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
		
		Category category = options.addCategory()
				.setBundleName(OptionsLTLLazy.OPTIONS_LTL_LAZY)
				.setIdentifier(OptionsLTLLazy.LTL_LAZY_CATEGORY)
				.build();
		
		Map<String,Class<?>> solvers = options.get(OptionsModelChecker.PROPERTY_SOLVER_CLASS);
		assert solvers != null;
        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();
		solvers.put(PropertySolverDDLTLLazy.IDENTIFIER, PropertySolverDDLTLLazy.class);
		solvers.put(PropertySolverExplicitLTLLazy.IDENTIFIER, PropertySolverExplicitLTLLazy.class);
		options.addOption().setBundleName(OptionsLTLLazy.OPTIONS_LTL_LAZY)
			.setIdentifier(OptionsLTLLazy.LTL_LAZY_INCREMENTAL)
			.setType(typeBoolean).setDefault(false)
			.setCommandLine().setGui().setWeb()
			.setCategory(category).build();
		options.addOption().setBundleName(OptionsLTLLazy.OPTIONS_LTL_LAZY)
			.setIdentifier(OptionsLTLLazy.LTL_LAZY_USE_SUBSET)
			.setType(typeBoolean).setDefault(true)
			.setCommandLine().setGui().setWeb()
			.setCategory(category).build();
		options.addOption().setBundleName(OptionsLTLLazy.OPTIONS_LTL_LAZY)
			.setIdentifier(OptionsLTLLazy.LTL_LAZY_USE_BREAKPOINT)
			.setType(typeBoolean).setDefault(true)
			.setCommandLine().setGui().setWeb()
			.setCategory(category).build();
		options.addOption().setBundleName(OptionsLTLLazy.OPTIONS_LTL_LAZY)
			.setIdentifier(OptionsLTLLazy.LTL_LAZY_USE_BREAKPOINT_SINGLETONS)
			.setType(typeBoolean).setDefault(true)
			.setCommandLine().setGui().setWeb()
			.setCategory(category).build();
		options.addOption().setBundleName(OptionsLTLLazy.OPTIONS_LTL_LAZY)
			.setIdentifier(OptionsLTLLazy.LTL_LAZY_USE_RABIN)
			.setType(typeBoolean).setDefault(true)
			.setCommandLine().setGui().setWeb()
			.setCategory(category).build();
        options.addOption().setBundleName(OptionsLTLLazy.OPTIONS_LTL_LAZY)
        	.setIdentifier(OptionsLTLLazy.LTL_LAZY_SCC_SKIP_TRANSIENT)
        	.setType(typeBoolean).setDefault(true)
        	.setCommandLine().setGui().setWeb()
        	.setCategory(category).build();
        options.addOption().setBundleName(OptionsLTLLazy.OPTIONS_LTL_LAZY)
        	.setIdentifier(OptionsLTLLazy.LTL_LAZY_REMOVE_DECIDED)
        	.setType(typeBoolean).setDefault(true)
        	.setCommandLine().setGui().setWeb()
        	.setCategory(category).build();
        options.addOption().setBundleName(OptionsLTLLazy.OPTIONS_LTL_LAZY)
        	.setIdentifier(OptionsLTLLazy.LTL_LAZY_STOP_IF_INIT_DECIDED)
        	.setType(typeBoolean).setDefault(true)
        	.setCommandLine().setGui().setWeb()
        	.setCategory(category).build();
        Map<String, Class<?>> automatonMap = options.get(OptionsAutomaton.AUTOMATON_CLASS);
        assert automatonMap != null;
        automatonMap.put(AutomatonSubset.IDENTIFIER, AutomatonSubset.Builder.class);
        automatonMap.put(AutomatonBreakpoint.IDENTIFIER, AutomatonBreakpoint.Builder.class);
	}
}
