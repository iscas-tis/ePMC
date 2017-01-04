package epmc.constraintsolver.smtlib.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import epmc.constraintsolver.options.OptionsConstraintsolver;
import epmc.constraintsolver.smtlib.ConstraintSolverSMTLib;
import epmc.constraintsolver.smtlib.options.OptionsSMTLib;
import epmc.constraintsolver.smtlib.options.SMTLibVersion;
import epmc.error.EPMCException;
import epmc.options.Category;
import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeEnum;
import epmc.options.OptionTypeStringList;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public final class AfterOptionsCreationSMTLib implements AfterOptionsCreation {
	public final static String IDENTIFIER = "constraintsolver-smtlib";
	
	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
		
		Map<String,Class<?>> solvers = options.get(OptionsConstraintsolver.CONSTRAINTSOLVER_SOLVER_CLASS);
		assert solvers != null;
        solvers.put(ConstraintSolverSMTLib.IDENTIFIER, ConstraintSolverSMTLib.class);		
        
        Category category = options.addCategory()
        		.setBundleName(OptionsSMTLib.OPTIONS_SMTLIB)
        		.setIdentifier(OptionsSMTLib.SMTLIB_CATEGORY)
        		.setParent(OptionsConstraintsolver.CONSTRAINTSOLVER_CATEGORY)
        		.build();
        OptionTypeStringList typeCommand = new OptionTypeStringList("command");
        List<String> defaultCommandLine = new ArrayList<>();
        defaultCommandLine.add("z3");
        defaultCommandLine.add("-I");
        defaultCommandLine.add("{0}");
        options.addOption()
        	.setBundleName(OptionsSMTLib.OPTIONS_SMTLIB)
        	.setIdentifier(OptionsSMTLib.SMTLIB_COMMAND_LINE)
        	.setCategory(category)
        	.setType(typeCommand)
        	.setDefault(defaultCommandLine)
        	.setCommandLine().setGui().setWeb()
        	.build();
        
        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();
        options.addOption()
        	.setBundleName(OptionsSMTLib.OPTIONS_SMTLIB)
        	.setIdentifier(OptionsSMTLib.SMTLIB_KEEP_TEMPORARY_FILES)
        	.setCategory(category)
        	.setType(typeBoolean)
        	.setDefault(false)
        	.setCommandLine().setGui().setWeb()
        	.build();

        OptionTypeEnum typeSMTLibVersion = new OptionTypeEnum(SMTLibVersion.class);
        options.addOption()
        	.setBundleName(OptionsSMTLib.OPTIONS_SMTLIB)
        	.setIdentifier(OptionsSMTLib.SMTLIB_VERSION)
        	.setCategory(category)
        	.setType(typeSMTLibVersion)
        	.setDefault(SMTLibVersion.V20)
        	.setCommandLine().setGui().setWeb()
        	.build();
	}
}
