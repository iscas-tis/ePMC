package epmc.constraintsolver.isat3.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import epmc.constraintsolver.isat3.textual.ConstraintSolverISat3Textual;
import epmc.constraintsolver.options.OptionsConstraintsolver;
import epmc.constraintsolver.sat3.options.OptionsISat3;
import epmc.error.EPMCException;
import epmc.options.Category;
import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeStringList;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public final class AfterOptionsCreationISat3 implements AfterOptionsCreation {
	public final static String IDENTIFIER = "constraintsolver-isat3";
	
	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
		
		Map<String,Class<?>> solvers = options.get(OptionsConstraintsolver.CONSTRAINTSOLVER_SOLVER_CLASS);
		assert solvers != null;
        solvers.put(ConstraintSolverISat3Textual.IDENTIFIER, ConstraintSolverISat3Textual.class);		
        
        Category category = options.addCategory()
        		.setBundleName(OptionsISat3.OPTIONS_ISAT3)
        		.setIdentifier(OptionsISat3.ISAT3_CATEGORY)
        		.setParent(OptionsConstraintsolver.CONSTRAINTSOLVER_CATEGORY)
        		.build();
        OptionTypeStringList typeCommand = new OptionTypeStringList("command");
        List<String> defaultCommandLine = new ArrayList<>();
        defaultCommandLine.add("isat3");
        defaultCommandLine.add("-I");
        defaultCommandLine.add("-v");
        defaultCommandLine.add("{0}");
        options.addOption()
        	.setBundleName(OptionsISat3.OPTIONS_ISAT3)
        	.setIdentifier(OptionsISat3.ISAT3_COMMAND_LINE)
        	.setCategory(category)
        	.setType(typeCommand)
        	.setDefault(defaultCommandLine)
        	.setCommandLine().setGui().setWeb()
        	.build();
        
        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();
        options.addOption()
        	.setBundleName(OptionsISat3.OPTIONS_ISAT3)
        	.setIdentifier(OptionsISat3.ISAT3_KEEP_TEMPORARY_FILES)
        	.setCategory(category)
        	.setType(typeBoolean)
        	.setDefault(false)
        	.setCommandLine().setGui().setWeb()
        	.build();
	}

}
