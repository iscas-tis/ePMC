package epmc.constraintsolver.plugin;

import java.util.Map;

import epmc.constraintsolver.options.OptionsConstraintsolver;
import epmc.error.EPMCException;
import epmc.options.Category;
import epmc.options.OptionTypeStringListSubset;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;
import epmc.util.OrderedMap;

public class AfterOptionsCreationConstraintsolver implements AfterOptionsCreation {
	public final static String IDENTIFIER = "after-options-creation-constraintsolver";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
		
		Category category = options.addCategory()
				.setBundleName(OptionsConstraintsolver.OPTIONS_CONSTRAINTSOLVER)
				.setIdentifier(OptionsConstraintsolver.CONSTRAINTSOLVER_CATEGORY)
				.build();

        Map<String,Class<?>> solvers = new OrderedMap<>();
        options.set(OptionsConstraintsolver.CONSTRAINTSOLVER_SOLVER_CLASS, solvers);
        OptionTypeStringListSubset<Class<?>> solverType = new OptionTypeStringListSubset<>(solvers);
        options.addOption().setBundleName(OptionsConstraintsolver.OPTIONS_CONSTRAINTSOLVER)
        	.setIdentifier(OptionsConstraintsolver.CONSTRAINTSOLVER_SOLVER)
            .setType(solverType)
            .setCommandLine().setGui().setWeb()
            .setCategory(category).build();
	}

}
