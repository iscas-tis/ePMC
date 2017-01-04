package epmc.propertysolverltlfg;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.OptionTypeBoolean;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public final class AfterOptionsCreationLTLTGRA implements AfterOptionsCreation {
	private final static String IDENTIFIER = "after-options-ltl-tgra";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
		Map<String,Class<?>> solvers = options.get(OptionsModelChecker.PROPERTY_SOLVER_CLASS);
		assert solvers != null;
        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();
        solvers.put(PropertySolverDDLTLRA.IDENTIFIER, PropertySolverDDLTLRA.class);
        solvers.put(PropertySolverExplicitLTLRA.IDENTIFIER, PropertySolverExplicitLTLRA.class);
		solvers.put(PropertySolverDDLTLTGRA.IDENTIFIER, PropertySolverDDLTLTGRA.class);
		solvers.put(PropertySolverExplicitLTLTGRA.IDENTIFIER, PropertySolverExplicitLTLTGRA.class);
		solvers.put(PropertySolverDDLTLGRA.IDENTIFIER, PropertySolverDDLTLGRA.class);
		solvers.put(PropertySolverExplicitLTLGRA.IDENTIFIER, PropertySolverExplicitLTLGRA.class);
		solvers.put(PropertySolverDDLTLFDA.IDENTIFIER, PropertySolverDDLTLFDA.class);
		solvers.put(PropertySolverExplicitLTLFDA.IDENTIFIER, PropertySolverExplicitLTLFDA.class);
		solvers.put(PropertySolverDDLTLSTGRA.IDENTIFIER, PropertySolverDDLTLSTGRA.class);
		solvers.put(PropertySolverExplicitLTLSTGRA.IDENTIFIER, PropertySolverExplicitLTLSTGRA.class);

		options.addOption().setBundleName(OptionsLTLTGRA.OPTIONS_LTL_TGRA)
		.setIdentifier(OptionsLTLTGRA.LTL_TGRA_SCC_SKIP_TRANSIENT)
		.setType(typeBoolean).setDefault(true)
		.setCommandLine().setGui().setWeb().build();
		options.addOption().setBundleName(OptionsLTLTGRA.OPTIONS_LTL_TGRA)
		.setIdentifier(OptionsLTLTGRA.LTL_TGRA_MEC_COMPUTATION)
		.setType(typeBoolean).setDefault(false)
		.setCommandLine().setGui().setWeb().build();
	}
}
