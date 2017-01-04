package epmc.uct;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.StateMap;
import epmc.modelchecker.StateSet;
import epmc.propertysolver.PropertySolver;

public final class PropertySolverExplicitUCT implements PropertySolver {
    public final static String IDENTIFIER = "explicit-uct";

    @Override

    public void setModelChecker(ModelChecker modelChecker) {
        assert modelChecker != null;
        // ...
    }

    @Override
    public StateMap solve(Expression property, StateSet forStates)
            throws EPMCException {
    	// ...
    	return null;
    }


    @Override
    public boolean canHandle(Expression property, StateSet states)
            throws EPMCException {
        assert property != null;
        assert states != null;
        // ...
        return false;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }
}
