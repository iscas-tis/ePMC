package epmc.imdp.lump.antagonistic;

import java.io.Closeable;

import epmc.constraintsolver.ConstraintSolver;
import epmc.constraintsolver.ConstraintSolverConfiguration;
import epmc.constraintsolver.ConstraintSolverResult;
import epmc.constraintsolver.ConstraintType;
import epmc.constraintsolver.Direction;
import epmc.operator.OperatorAddInverse;
import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeArray;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueInterval;
import epmc.value.ValueReal;

final class MinimalityChecker implements Closeable {
    private final static String VAR_LAMBDA = "LAMBDA_%d";
    private final static String VAR_XI = "XI_%d_%d";
    private final static String VAR_ETA = "ETA_%d_%d";
    private final static String VAR_XI_HAT = "XI_HAT_%d_%d";
    private final static String VAR_ETA_HAT = "ETA_HAT_%d_%d";
    private final static String VAR_MU = "MU_%d_%d_d";
    private final static String VAR_NU = "NU_%d_%d_d";
    private final static String VAR_MU_HAT = "MU_HAT_%d_%d_%d";
    private final static String VAR_NU_HAT = "NU_HAT_%d_%d_%d";

    private final PolytopeSet polytopeSet;
    private final int forAction;
    private final int numClasses;
    private final int numActions;
    private final ConstraintSolver problem;
    private final ValueReal zero;
    private final ValueReal one;
    private final ValueReal mOne;
    private final ValueReal posInf;
    private final ValueInterval entryInterval;
    private final ValueReal entryReal;
    private final OperatorEvaluator addInverse;
    private final OperatorEvaluator set;

    MinimalityChecker(ConstraintSolverConfiguration contextConstraintSolver, PolytopeSet polytopeSet, int forAction) {
        assert contextConstraintSolver != null;
        this.polytopeSet = polytopeSet;
        this.forAction = forAction;
        numClasses = polytopeSet.getNumClasses();
        numActions = polytopeSet.getNumActions();
        problem = contextConstraintSolver.newProblem();
        assert problem != null; // TODO replace by ensure
        zero = UtilValue.newValue(TypeReal.get(), 0);
        one = UtilValue.newValue(TypeReal.get(), 1);
        mOne = TypeReal.get().newValue();
        addInverse = ContextValue.get().getEvaluator(OperatorAddInverse.ADD_INVERSE, TypeReal.get());
        addInverse.apply(mOne, one);
        posInf = UtilValue.newValue(TypeReal.get(), UtilValue.POS_INF);
        entryInterval = TypeInterval.get().newValue();
        entryReal = TypeReal.get().newValue();
        set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeReal.get(), TypeReal.get());
    }

    boolean isMinimal() {
        assert polytopeSet != null;
        assert forAction >= 0;
        problem.setDirection(Direction.MIN);
        if (numActions == 1) {
            return true;
        }
        prepareVariables();
        prepareLambdaSum();
        prepareBounds(false);
        prepareBounds(true);
        //		System.out.println(polytopeSet);
        //		System.out.println(problem);
        boolean min = problem.solve() != ConstraintSolverResult.SAT;
        /*
		System.out.println(polytopeSet);
		System.out.println(forAction);
		System.out.println(min);
         */

        //System.out.println("--------");
        /*
		if (numActions == 2 && numClasses == 2) {
			System.out.println(polytopeSet);
			System.out.println(problem);
			System.out.println(min);
		}
         */
        return min;
    }

    private void prepareVariables() {
        assert problem != null;
        assert numClasses >= 0;
        assert numActions >= 0;
        assert forAction >= 0;

        addLambdaVariables();
        addActionClassVariables(VAR_XI);
        addActionClassVariables(VAR_ETA);
        addActionClassVariables(VAR_XI_HAT);
        addActionClassVariables(VAR_ETA_HAT);
        addActionClassVector(VAR_MU);
        addActionClassVector(VAR_NU);
        addActionClassVector(VAR_MU_HAT);
        addActionClassVector(VAR_NU_HAT);
    }

    private void addLambdaVariables() {
        for (int actionNr = 0; actionNr < numActions; actionNr++) {
            if (actionNr == forAction) {
                continue;
            }
            problem.addVariable(String.format(VAR_LAMBDA, actionNr),
                    getTypeReal(), zero, one);
        }
    }

    private void addActionClassVariables(String pattern) {
        for (int actionNr = 0; actionNr < numActions; actionNr++) {
            if (actionNr == forAction) {
                continue;
            }
            for (int classNr = 0; classNr < numClasses; classNr++) {
                problem.addVariable(String.format(pattern, actionNr, classNr),
                        getTypeReal(), zero, posInf);
            }
        }
    }

    private void addActionClassVector(String pattern) {
        for (int actionNr = 0; actionNr < numActions; actionNr++) {
            if (actionNr == forAction) {
                continue;
            }
            for (int classNr = 0; classNr < numClasses; classNr++) {
                for (int entryNr = 0; entryNr < numClasses; entryNr++) {
                    problem.addVariable(String.format(pattern, actionNr, classNr, entryNr),
                            getTypeReal(), zero, posInf);
                }
            }
        }
    }

    private void prepareLambdaSum() {
        Value one = UtilValue.newValue(getTypeReal(), 1);
        ValueArrayAlgebra values = newArrayReal(numActions - 1);
        int[] variables = new int[numActions - 1];
        int varNr = 0;
        for (int actionNr = 0; actionNr < numActions; actionNr++) {
            if (actionNr == forAction) {
                continue;
            }
            variables[varNr] = varLambda(actionNr);
            values.set(one, varNr);
            varNr++;
        }
        problem.addConstraint(values, variables, ConstraintType.EQ, one);
    }


    private void prepareBounds(boolean forUpper) {
        for (int classNr = 0; classNr < numClasses; classNr++) {
            prepareBounds(forUpper, classNr);
        }
    }

    private void prepareBounds(boolean forUpper, int forClass) {
        assert forClass >= 0;
        assert forClass < numClasses;
        for (int actionNr = 0; actionNr < numActions; actionNr++) {
            if (actionNr == forAction) {
                continue;
            }
            prepareBoundsAction(forUpper, forClass, actionNr);
        }
        prepareBoundsClass(forUpper, forClass);
    }

    private void prepareBoundsAction(boolean forUpper, int classNr, int actionNr) {
        assert classNr >= 0;
        assert classNr < numClasses;
        assert actionNr >= 0;
        assert actionNr < numActions;
        ValueArrayAlgebra values = newArrayReal(5);
        int[] variables = new int[5];
        for (int entry = 0; entry < numClasses; entry++) {
            variables[0] = forUpper
                    ? varMuHat(actionNr, classNr, entry)
                            : varMu(actionNr, classNr, entry);
                    values.set(one, 0);
                    variables[1] = forUpper
                            ? varNuHat(actionNr, classNr, entry)
                                    : varNu(actionNr, classNr, entry);
                            values.set(mOne, 1);
                            variables[2] = forUpper
                                    ? varXiHat(actionNr, classNr)
                                            : varXi(actionNr, classNr);
                                    values.set(one, 2);
                                    variables[3] = forUpper
                                            ? varEtaHat(actionNr, classNr)
                                                    : varEta(actionNr, classNr);
                                            values.set(mOne, 3);
                                            variables[4] = varLambda(actionNr);
                                            if (forUpper) {
                                                values.set(classNr == entry ? mOne : zero, 4);				
                                            } else {
                                                values.set(classNr == entry ? one : zero, 4);
                                            }
                                            problem.addConstraint(values, variables, ConstraintType.EQ, zero);
        }
    }

    private void prepareBoundsClass(boolean forUpper, int classNr) {
        assert classNr >= 0;
        assert classNr < numClasses;
        int size = 2 * (numActions - 1) * (numClasses + 1);
        int[] variables = new int[size];
        ValueArrayAlgebra values = newArrayReal(size);
        int varNr = 0;
        for (int actionNr = 0; actionNr < numActions; actionNr++) {
            if (actionNr == forAction) {
                continue;
            }
            for (int entry = 0; entry < numClasses; entry++) {
                variables[varNr] = forUpper
                        ? varMuHat(actionNr, classNr, entry)
                                : varMu(actionNr, classNr, entry);
                        values.set(getUpperBound(actionNr, entry), varNr);
                        varNr++;
            }
            for (int entry = 0; entry < numClasses; entry++) {
                variables[varNr] = forUpper
                        ? varNuHat(actionNr, classNr, entry)
                                : varNu(actionNr, classNr, entry);
                        values.set(getLowerBound(actionNr, entry), varNr);
                        varNr++;
            }
        }
        for (int actionNr = 0; actionNr < numActions; actionNr++) {
            if (actionNr == forAction) {
                continue;
            }
            variables[varNr] = forUpper
                    ? varXiHat(actionNr, classNr)
                            : varXi(actionNr, classNr);
                    values.set(one, varNr);
                    varNr++;
                    variables[varNr] = forUpper
                            ? varEtaHat(actionNr, classNr)
                                    : varEta(actionNr, classNr);
                            values.set(mOne, varNr);
                            varNr++;
        }
        assert varNr == size;
        if (forUpper) {
            set.apply(entryReal, getUpperBound(forAction, classNr));
        } else {
            addInverse.apply(entryReal, getLowerBound(forAction, classNr));
        }
        problem.addConstraint(values, variables, ConstraintType.LE, entryReal);
    }

    private ValueReal getLowerBound(int actionNr, int entry) {
        polytopeSet.get(actionNr, entry, entryInterval);
        return entryInterval.getIntervalLower();
    }

    private ValueReal getUpperBound(int actionNr, int entry) {
        polytopeSet.get(actionNr, entry, entryInterval);
        return entryInterval.getIntervalUpper();
    }

    private int varLambda(int actionNr) {
        assert forAction != actionNr;
        if (actionNr > forAction) {
            actionNr--;
        }
        return actionNr;
    }

    private int varXi(int actionNr, int classNr) {
        assert forAction != actionNr;
        int offset = numActions - 1;
        if (actionNr > forAction) {
            actionNr--;
        }
        return offset + actionNr * numClasses + classNr;
    }

    private int varEta(int actionNr, int classNr) {
        assert forAction != actionNr;
        int offset = numActions - 1 + (numActions - 1) * numClasses;
        if (actionNr > forAction) {
            actionNr--;
        }
        return offset + actionNr * numClasses + classNr;
    }

    private int varXiHat(int actionNr, int classNr) {
        assert forAction != actionNr;
        int offset = numActions - 1 + (numActions - 1) * numClasses * 2;
        if (actionNr > forAction) {
            actionNr--;
        }
        return offset + actionNr * numClasses + classNr;
    }

    private int varEtaHat(int actionNr, int classNr) {
        assert forAction != actionNr;
        int offset = numActions - 1 + (numActions - 1) * numClasses * 3;
        if (actionNr > forAction) {
            actionNr--;
        }
        return offset + actionNr * numClasses + classNr;
    }

    private int varMu(int actionNr, int classNr, int entry) {
        assert forAction != actionNr;
        int offset = numActions - 1 + (numActions - 1) * numClasses * 4;
        if (actionNr > forAction) {
            actionNr--;
        }
        return offset + actionNr * numClasses * numClasses + classNr * numClasses + entry;
    }

    private int varNu(int actionNr, int classNr, int entry) {
        assert forAction != actionNr;
        int offset = numActions - 1 + (numActions - 1) * numClasses * 4
                + (numActions - 1) * numClasses * numClasses;
        if (actionNr > forAction) {
            actionNr--;
        }
        return offset + actionNr * numClasses * numClasses + classNr * numClasses + entry;
    }

    private int varMuHat(int actionNr, int classNr, int entry) {
        assert forAction != actionNr;
        int offset = numActions - 1 + (numActions - 1) * numClasses * 4
                + (numActions - 1) * numClasses * numClasses * 2;
        if (actionNr > forAction) {
            actionNr--;
        }
        return offset + actionNr * numClasses * numClasses + classNr * numClasses + entry;
    }

    private int varNuHat(int actionNr, int classNr, int entry) {
        assert forAction != actionNr;
        int offset = numActions - 1 + (numActions - 1) * numClasses * 4
                + (numActions - 1) * numClasses * numClasses * 3;
        if (actionNr > forAction) {
            actionNr--;
        }
        return offset + actionNr * numClasses * numClasses + classNr * numClasses + entry;
    }

    private ValueArrayAlgebra newArrayReal(int size) {
        assert size >= 0;
        return UtilValue.newArray(getTypeRealArray(), size);
    }

    private TypeArray getTypeRealArray() {
        return getTypeReal().getTypeArray();
    }

    private TypeReal getTypeReal() {
        return TypeReal.get();
    }

    @Override
    public void close() {
        problem.close();
    }
}
