/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

package epmc.constraintsolver.lpsolve;

import com.sun.jna.Pointer;

import static epmc.error.UtilError.ensure;

import java.util.LinkedHashSet;
import java.util.Set;

import epmc.constraintsolver.ConstraintSolver;
import epmc.constraintsolver.ConstraintSolverResult;
import epmc.constraintsolver.ConstraintType;
import epmc.constraintsolver.Direction;
import epmc.constraintsolver.Feature;
import epmc.constraintsolver.error.ProblemsConstraintsolver;
import epmc.expression.Expression;
import epmc.util.BitSet;
import epmc.util.JNATools;
import epmc.util.UtilBitSet;
import epmc.value.Type;
import epmc.value.TypeArrayAlgebra;
import epmc.value.TypeBoolean;
import epmc.value.TypeDouble;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueBoolean;
import epmc.value.ValueContentDoubleArray;
import epmc.value.ValueDouble;
import epmc.value.ValueInteger;
import epmc.value.ValueNumber;
import epmc.value.ValueReal;
import epmc.value.ValueSetString;

public final class ConstraintSolverLPSolve implements ConstraintSolver {
    public final static String IDENTIFIER = "lp-solve";
    /* input constants to lp_solve */

    /** Encoding constant {@code false} of lp_solve. */
    private final static byte FALSE = 0;
    /** Encoding constant {@code true} of lp_solve. */
    private final static byte TRUE = 1;
    /** Encoding less-or-equal in lp_solve. */
    private final static int LE = 1;
    /** Encoding equality in lp_solve. */
    private final static int EQ = 3;
    /** Encoding greater-or-equal in lp_solve. */    
    private final static int GE = 2;

    /* return codes of lp_solve */

    private final static int NOTRUN = -1;
    private final static int NOMEMORY = -2;
    private final static int NOBFP = -3;
    private final static int OPTIMAL = 0;
    private final static int SUBOPTIMAL = 1;
    private final static int INFEASIBLE = 2;
    private final static int UNBOUNDED = 3;
    private final static int DEGENERATE = 4;
    private final static int NUMFAILURE = 5;
    private final static int USERABORT = 6;
    private final static int TIMEOUT = 7;
    private final static int PRESOLVED = 9;
    private final static int PROCFAIL = 10;
    private final static int PROCBREAK = 11;
    private final static int FEASFOUND = 12;
    private final static int NOFEASFOUND = 13;

    /* reporting details mode of lp_solve */

    private final static int NEUTRAL = 0;
    private final static int CRITICAL = 1;
    private final static int SEVERE = 2;
    private final static int IMPORTANT = 3;
    private final static int NORMAL = 4;
    private final static int DETAILED = 5;
    private final static int FULL = 6;

    private final static class LpSolve {

        static native Pointer make_lp(int rows, int columns);

        static native void delete_lp(Pointer lp);

        static native byte set_add_rowmode(Pointer lp, byte turnon);

        static native byte add_constraint(Pointer lp, double[] row, int constr_type, double rh);

        static native byte add_constraintex(Pointer lp, int count, double[] row, int[] colno, int constr_type, double rh);

        static native int solve(Pointer lp);

        static native byte has_BFP(Pointer lp);

        static native byte is_feasible(Pointer lp, double[] values, double threshold);

        static native byte get_primal_solution(Pointer lp, double[] pv);

        static native double get_objective(Pointer lp);

        static native byte set_obj_fn(Pointer lp, double[] row);

        static native byte set_obj_fnex(Pointer lp, int count, double[] row, int[] colno);

        static native void set_maxim(Pointer lp);

        static native void set_minim(Pointer lp);

        static native void print_constraints(Pointer lp, int columns);

        static native void print_lp(Pointer lp);

        static native byte set_int(Pointer lp, int column, byte must_be_int);

        static native byte set_binary(Pointer lp, int column, byte must_be_bin);

        static native byte add_columnex(Pointer lp, int count, double[] column, int[] rowno);

        static native byte set_rowex(Pointer lp, int row_no, int count, double[] row, int[] colno);

        static native void set_debug(Pointer lp, byte debug);

        static native void set_verbose(Pointer lp, int verbose);

        static native double get_var_primalresult(Pointer lp, int index);

        static native byte set_bounds(Pointer lp, int column, double lower, double upper);

        static native byte set_col_name(Pointer lp, int column, String name);

        static native void set_use_names(Pointer lp, byte isrow, byte use_names);

        static native void set_outputstream(Pointer lp, Pointer stream);

        private final static boolean loaded =
                JNATools.registerLibrary(LpSolve.class, "lpsolve55");        
    }

    private final Set<Feature> features = new LinkedHashSet<>();
    private Pointer lp;
    private int numVariables;
    private int numConstraints;
    private boolean closed;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void requireFeature(Feature feature) {
        assert feature != null;
        features.add(feature);
    }

    @Override
    public boolean canHandle() {
        for (Feature feature : features) {
            if (feature != Feature.LP) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void build() {
        this.lp = LpSolve.make_lp(numConstraints, numVariables);
        ensure(this.lp != null, ProblemsConstraintsolver.CONSTRAINTSOLVER_INSUFFICIENT_NATIVE_MEMORY);
        LpSolve.set_debug(this.lp, FALSE);
        LpSolve.set_verbose(this.lp, NEUTRAL);
        LpSolve.set_use_names(this.lp, (byte) 0, (byte) 1);
    }

    private int toLpSolveConstraintType(ConstraintType type) {
        assert type != null;
        switch (type) {
        case EQ:
            return EQ;
        case GE:
            return GE;
        case LE:
            return LE;
        default:
            assert false;
            return -1;
        }
    }

    @Override
    public int addVariable(String name, Type type, Value lower, Value upper) {
        assert !closed;
        assert type != null;
        assert TypeReal.is(type) || TypeInteger.is(type);
        assert lower == null || ValueReal.is(lower) || ValueInteger.is(lower) || ValueBoolean.is(lower);
        assert upper == null || ValueReal.is(upper) || ValueInteger.is(upper) || ValueBoolean.is(upper);
        LpSolve.set_add_rowmode(lp, FALSE);
        LpSolve.add_columnex(lp, 0, new double[0], new int[0]);
        int variable = numVariables;
        if (TypeInteger.is(type)) {
            LpSolve.set_int(lp, variable, TRUE);
        } else if (TypeBoolean.is(type)) {
            LpSolve.set_binary(lp, variable, TRUE);
        }
        if (lower != null && upper != null) {
            LpSolve.set_bounds(lp, variable + 1, ValueNumber.as(lower).getDouble(), ValueNumber.as(upper).getDouble());
        }
        if (name != null) {
            LpSolve.set_col_name(lp, variable + 1, name);
        }
        numVariables++;
        return variable;
    }    

    private void addConstraint(double[] row, int[] variables,
            ConstraintType constraintType, Value rightHandSide) {
        assert !closed;
        assert row != null;
        assert variables != null;
        assert constraintType != null;
        assert rightHandSide != null;
        assert row.length == variables.length;
        assert assertNoDublicateVariables(variables);
        incIntArrayOne(variables);
        LpSolve.set_add_rowmode(lp, TRUE);
        int lpConstraintType = toLpSolveConstraintType(constraintType);
        double lpRhs = ValueNumber.as(rightHandSide).getDouble();
        LpSolve.add_constraintex(lp, row.length, row, variables,
                lpConstraintType, lpRhs);
        decIntArrayOne(variables);
        numConstraints++;
    }

    private boolean assertNoDublicateVariables(int[] variables) {
        BitSet variablesBs = UtilBitSet.newBitSetUnbounded();
        for (int variable : variables) {
            assert !variablesBs.get(variable) : variable;
            variablesBs.set(variable);
        }
        return true;
    }

    @Override
    public void addConstraint(ValueArray row, int[] variables,
            ConstraintType constraintType, Value rightHandSide) {
        assert !closed;
        assert row != null;
        assert variables != null;
        for (int var : variables) {
            assert var >= 0;
            assert var < numVariables;
        }
        assert rightHandSide != null;
        assert TypeReal.is(rightHandSide.getType());
        double[] rowDouble = arrayToDouble(row);

        addConstraint(rowDouble, variables, constraintType, rightHandSide);
    }

    @Override
    public void addConstraint(Value[] row, int[] variables,
            ConstraintType constraintType, Value rightHandSide) {
        assert !closed;
        assert row != null;
        assert variables != null;
        for (int var : variables) {
            assert var >= 0;
            assert var < numVariables;
        }
        assert rightHandSide != null;
        assert TypeReal.is(rightHandSide.getType());
        double[] rowDouble = arrayToDouble(row);

        addConstraint(rowDouble, variables, constraintType, rightHandSide);
    }

    @Override
    public void setObjective(Expression objective) {
        assert !closed;
        assert objective != null;
        // TODO Auto-generated method stub
    }

    @Override
    public void setObjective(ValueArray row, int[] variables) {
        assert !closed;
        assert row != null;
        assert variables != null;
        assert row.size() == variables.length;
        double[] rowDouble = arrayToDouble(row);
        incIntArrayOne(variables);
        byte result = LpSolve.set_obj_fnex(lp, rowDouble.length, rowDouble, variables);
        incIntArrayOne(variables);
        assert result == TRUE;
    }


    @Override
    public void setObjective(Value[] row, int[] variables) {
        assert !closed;
        assert row != null;
        assert variables != null;
        assert row.length == variables.length;
        double[] rowDouble = arrayToDouble(row);
        LpSolve.set_add_rowmode(lp, FALSE);
        incIntArrayOne(variables);
        byte result = LpSolve.set_obj_fnex(lp, rowDouble.length, rowDouble, variables);
        decIntArrayOne(variables);
        assert result == TRUE;
    }

    @Override
    public void addConstraint(Expression expression) {
        assert !closed;
        assert expression != null;
        assert false;
        // TODO Auto-generated method stub

    }

    @Override
    public void setDirection(Direction direction) {
        assert !closed;
        assert direction != null;
        switch (direction) {
        case MAX:
            LpSolve.set_maxim(this.lp);
            break;
        case MIN:
            LpSolve.set_minim(this.lp);
            break;
        case FEASIBILITY: {
            LpSolve.set_maxim(this.lp);
            TypeArrayAlgebra typeArray = TypeWeight.get().getTypeArray();
            ValueArrayAlgebra problemWeights = UtilValue.newArray(typeArray, 1);
            int[] problemVariables = new int[1];
            problemWeights.set(1, 0);
            problemVariables[0] = 0;
            setObjective(problemWeights, problemVariables);
            break;
        }
        default:
            assert false;
            break;
        }
    }

    @Override
    public ConstraintSolverResult solve() {
        assert !closed;
        LpSolve.set_add_rowmode(lp, FALSE);
        int result = LpSolve.solve(this.lp);
        switch (result) {
        case OPTIMAL: case SUBOPTIMAL: case UNBOUNDED: case PRESOLVED: case FEASFOUND:
            return ConstraintSolverResult.SAT;
        case INFEASIBLE:
            return ConstraintSolverResult.UNSAT;
        case NOMEMORY: case DEGENERATE: case NUMFAILURE: case USERABORT:
        case TIMEOUT: case PROCFAIL: case PROCBREAK: case NOFEASFOUND:
            return ConstraintSolverResult.UNKNOWN;
        default:
            assert false : result;
        }
        return null;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        this.closed = true;
        LpSolve.delete_lp(this.lp);
    }


    private double[] arrayToDouble(ValueArray row) {
        assert row != null;
        double[] rowDouble = null;

        if (TypeDouble.is(row.getType().getEntryType())) {
            rowDouble = ValueContentDoubleArray.getContent(row);
        } else {
            rowDouble = new double[row.size()];
            Value entry = row.getType().getEntryType().newValue();
            for (int i = 0; i < row.size(); i++) {
                row.get(entry, i);
                rowDouble[i] = ValueNumber.as(entry).getDouble();
            }
        }
        return rowDouble;
    }


    private double[] arrayToDouble(Value[] row) {
        assert row != null;
        for (Value value : row) {
            assert value != null;
        }
        double[] rowDouble = new double[row.length];
        for (int i = 0; i < row.length; i++) {
            rowDouble[i] =  ValueNumber.as(row[i]).getDouble();
        }
        return rowDouble;
    }

    @Override
    public ValueReal getResultObjectiveValue() {
        assert !closed;
        double result = LpSolve.get_var_primalresult(lp, 0);
        ValueReal resultValue = TypeReal.get().newValue();
        if (ValueDouble.is(resultValue)) {
            ValueDouble.as(resultValue).set(result);
        } else if (ValueSetString.is(resultValue)) {
            ValueSetString.as(resultValue).set(Double.toString(result));
        } else {
            assert false;
        }
        return resultValue;
    }

    @Override
    public ValueArray getResultVariablesValuesSingleType() {
        assert !closed;
        Value entry = TypeReal.get().newValue();
        ValueArray result = UtilValue.newArray(TypeReal.get().getTypeArray(),
                numVariables);
        for (int i = 0; i < result.size(); i++) {
            double doubleVal = LpSolve.get_var_primalresult(lp, 1 + numConstraints + i);
            if (ValueDouble.is(entry)) {
                ValueDouble.as(entry).set(doubleVal);
            } else if (ValueSetString.is(entry)) {
                ValueSetString.as(entry).set(Double.toString(doubleVal));
            } else {
                assert false;
            }
            result.set(entry, i);
        }
        return result;
    }

    @Override
    public Value[] getResultVariablesValues() {
        assert !closed;
        Value[] result = new Value[numVariables];
        TypeReal typeReal = TypeReal.get();
        for (int i = 0; i < result.length; i++) {
            double doubleVal = LpSolve.get_var_primalresult(lp, 1 + numConstraints + i);
            Value entry = typeReal.newValue();
            if (ValueDouble.is(entry)) {
                ValueDouble.as(entry).set(doubleVal);
            } else if (ValueSetString.is(entry)) {
                ValueSetString.as(entry).set(Double.toString(doubleVal));
            } else {
                assert false;
            }
            result[i] = entry;
        }
        return result;
    }

    private void incIntArrayOne(int[] variables) {
        for (int i = 0; i < variables.length; i++) {
            variables[i]++;
        }
    }

    private void decIntArrayOne(int[] variables) {
        for (int i = 0; i < variables.length; i++) {
            variables[i]--;
        }
    }

    @Override
    public String toString() {
        if (!closed) {
            Pointer tmpfile = JNATools.tmpfile();
            LpSolve.set_outputstream(lp, tmpfile);
            LpSolve.print_lp(lp);
            String lpOutput = JNATools.readStreamToString(tmpfile);
            JNATools.fclose(tmpfile);
            LpSolve.set_outputstream(lp, JNATools.getStdout());
            return lpOutput;
        } else {
            return "(closed LP problem)";
        }
    }

    @Override
    public String getVariableName(int number) {
        // TODO
        return null;
    }
}
