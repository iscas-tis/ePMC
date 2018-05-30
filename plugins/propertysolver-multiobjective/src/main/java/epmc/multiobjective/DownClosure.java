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

package epmc.multiobjective;

import java.util.ArrayList;
import java.util.List;

import epmc.constraintsolver.ConstraintSolver;
import epmc.constraintsolver.ConstraintSolverConfiguration;
import epmc.constraintsolver.ConstraintType;
import epmc.constraintsolver.Direction;
import epmc.constraintsolver.Feature;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorDivide;
import epmc.operator.OperatorIsZero;
import epmc.operator.OperatorMax;
import epmc.operator.OperatorSet;
import epmc.options.Options;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeArray;
import epmc.value.TypeBoolean;
import epmc.value.TypeReal;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueBoolean;
import epmc.value.ValueSetString;

final class DownClosure {
    private final static String SLACK_VARIABLE = "v";
    private final static String WEIGHT_VARIABLE = "w%d";
    private final static String DIFFERENCE_VARIABLE = "d";
    private final ConstraintSolverConfiguration contextSolver;
    private final int dimension;
    private final List<IterationResult> elements = new ArrayList<>();

    DownClosure(int dimension) {
        assert dimension >= 0;
        assert TypeReal.is(TypeWeight.get());
        this.dimension = dimension;
        this.contextSolver = new ConstraintSolverConfiguration();
        contextSolver.requireFeature(Feature.LP);
    }

    void add(IterationResult entry) {
        assert entry != null;
        elements.add(entry);
    }

    ValueArrayAlgebra findSeparating(ValueArrayAlgebra outside, boolean numerical) {
        assert outside != null;
        assert outside.size() == dimension;
        if (elements.size() == 0) {
            return findSeparatingEmptyEntries(outside, numerical);
        } else {
            return findSeparatingNonEmptyEntries(outside, numerical);
        }
    }

    private ValueArrayAlgebra findSeparatingNonEmptyEntries(ValueArray outside,
            boolean numerical) {
        assert outside != null;
        ValueAlgebra zero = UtilValue.newValue(TypeWeight.get(), 0);
        ValueAlgebra lowerBound = TypeWeight.get().newValue();
        OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeWeight.get(), TypeWeight.get());
        set.apply(lowerBound, zero);
        ValueArrayAlgebra unrestrictedResult = findSeparatingNonEmptyEntries(outside, numerical, lowerBound);
        if (unrestrictedResult == null) {
            return null;
        }
        ValueSetString.as(lowerBound).set(Options.get().getString(OptionsMultiObjective.MULTI_OBJECTIVE_MIN_NONZERO_WEIGHT));
        ValueArrayAlgebra restrictedResult = findSeparatingNonEmptyEntries(outside, numerical, lowerBound);
        if (restrictedResult != null) {
            return restrictedResult;
        }
        OperatorEvaluator divide = ContextValue.get().getEvaluator(OperatorDivide.DIVIDE, TypeWeight.get(), TypeWeight.get());
        ValueAlgebra entry = TypeWeight.get().newValue();
        ValueAlgebra sum = TypeWeight.get().newValue();
        ValueBoolean cmp = TypeBoolean.get().newValue();
        OperatorEvaluator isZero = ContextValue.get().getEvaluator(OperatorIsZero.IS_ZERO, TypeWeight.get());
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeWeight.get(), TypeWeight.get());
        for (int i = 0; i < unrestrictedResult.size(); i++) {
            unrestrictedResult.get(entry, i);
            isZero.apply(cmp, entry);
            if (cmp.getBoolean()) {
                unrestrictedResult.set(lowerBound, i);
                add.apply(sum, sum, lowerBound);
            } else {
                add.apply(sum, sum, entry);
            }
        }
        for (int i = 0; i < unrestrictedResult.size(); i++) {
            unrestrictedResult.get(entry, i);
            divide.apply(entry, entry, sum);
            unrestrictedResult.set(entry, i);
        }
        return unrestrictedResult;
    }

    private ValueArrayAlgebra findSeparatingNonEmptyEntries(ValueArray outside,
            boolean numerical, Value lowerBound) {
        Value zero = UtilValue.newValue(TypeWeight.get(), 0);
        Value one = UtilValue.newValue(TypeWeight.get(), 1);
        ValueBoolean cmp = TypeBoolean.get().newValue();
        OperatorEvaluator isZero = ContextValue.get().getEvaluator(OperatorIsZero.IS_ZERO, TypeWeight.get());
        ConstraintSolver problem = contextSolver.newProblem();
        int[] wLpVars = new int[dimension];
        for (int i = 0; i < dimension; i++) {
            wLpVars[i] = problem.addVariable(String.format(WEIGHT_VARIABLE, i), TypeWeight.get());
        }
        int dLpVar = problem.addVariable(DIFFERENCE_VARIABLE, TypeWeight.get());
        int vLpVar = problem.addVariable(SLACK_VARIABLE, TypeWeight.get(), UtilValue.newValue(TypeReal.get(), UtilValue.NEG_INF), UtilValue.newValue(TypeReal.get(), UtilValue.POS_INF));

        ValueArrayAlgebra problemWeights;
        int[] problemVariables;
        problemWeights = newValueArrayWeight(dimension + 1);
        problemVariables = new int[dimension + 1];
        ValueAlgebra entry = newValueWeight();
        for (int dim = 0; dim < dimension; dim++) {
            outside.get(entry, dim);
            problemWeights.set(entry, dim);
            problemVariables[dim] = wLpVars[dim];
        }
        problemWeights.set(-1, dimension);
        problemVariables[dimension] = vLpVar;
        problem.addConstraint(problemWeights, problemVariables, ConstraintType.GE, zero);

        for (IterationResult element : elements) {
            ValueArrayAlgebra array = element.getQ();
            problemWeights = newValueArrayWeight(dimension + 2);
            problemVariables = new int[dimension + 2];
            for (int dim = 0; dim < dimension; dim++) {
                array.get(entry, dim);
                problemWeights.set(entry, dim);
                problemVariables[dim] = wLpVars[dim];
            }
            problemWeights.set(1, dimension);
            problemVariables[dimension] = dLpVar;
            problemWeights.set(-1, dimension + 1);
            problemVariables[dimension + 1] = vLpVar;
            problem.addConstraint(problemWeights, problemVariables, ConstraintType.LE, zero);
        }

        for (int dim = 0; dim < dimension; dim++) {
            problemWeights = newValueArrayWeight(1);
            problemVariables = new int[1];
            problemWeights.set(1, 0);
            problemVariables[0] = wLpVars[dim];
            problem.addConstraint(problemWeights, problemVariables, ConstraintType.GE, lowerBound);
        }

        problemWeights = newValueArrayWeight(dimension);
        problemVariables = new int[dimension];
        for (int dim = 0; dim < dimension; dim++) {
            problemWeights.set(1, dim);
            problemVariables[dim] = wLpVars[dim];
        }
        problem.addConstraint(problemWeights, problemVariables, ConstraintType.EQ, one);

        if (numerical) {
            problemWeights = newValueArrayWeight(1);
            problemVariables = new int[1];
            problemWeights.set(1, 0);
            problemVariables[0] = wLpVars[0];
            String minIncrease = Options.get().getString(OptionsMultiObjective.MULTI_OBJECTIVE_MIN_INCREASE);
            ValueSetString.as(entry).set(minIncrease);
            problem.addConstraint(problemWeights, problemVariables, ConstraintType.GE, entry);
        }

        problemWeights = newValueArrayWeight(1);
        problemVariables = new int[1];
        problemWeights.set(1, 0);
        problemVariables[0] = dLpVar;
        problem.setObjective(problemWeights, problemVariables);
        problem.setDirection(Direction.MAX);
        if (!problem.solve().isSat()) {
            problem.close();
            return null;
        }
        ValueArray solverResult = problem.getResultVariablesValuesSingleType();
        solverResult.get(entry, dLpVar);
        isZero.apply(cmp, entry);
        if (!numerical && cmp.getBoolean()) {
            problem.close();
            return null;
        }
        ValueArrayAlgebra result = newValueArrayWeight(dimension);
        for (int dim = 0; dim < dimension; dim++) {
            solverResult.get(entry, wLpVars[dim]);
            result.set(entry, dim);
        }
        problem.close();
        return result;
    }

    private ValueArrayAlgebra findSeparatingEmptyEntries(
            ValueArrayAlgebra outside,
            boolean numerical) {
        boolean outsideNonZero = false;
        ValueAlgebra entry = newValueWeight();
        ValueBoolean cmp = TypeBoolean.get().newValue();
        OperatorEvaluator isZero = ContextValue.get().getEvaluator(OperatorIsZero.IS_ZERO, entry.getType());
        for (int i = 0; i < outside.size(); i++) {
            outside.get(entry, i);
            isZero.apply(cmp, entry);
            if (!cmp.getBoolean()) {
                outsideNonZero = true;
                break;
            }
        }
        ValueArrayAlgebra separating;
        if (outsideNonZero) {
            separating = UtilValue.clone(outside);
        } else {
            separating = newValueArrayWeight(dimension);
            for (int i = 0; i < separating.size(); i++) {
                separating.set(1, i);
            }
        }
        if (numerical) {
            ValueAlgebra smallValue = TypeWeight.get().newValue();
            String minIncrease = Options.get().getString(OptionsMultiObjective.MULTI_OBJECTIVE_MIN_INCREASE);
            ValueSetString.as(smallValue).set(minIncrease);
            separating.get(entry, 0);
            isZero.apply(cmp, entry);
            if (cmp.getBoolean()) {
                separating.set(smallValue, 0);
            }
        }
        // TODO HACK
        for (int index = 0; index < separating.size(); index++) {
            separating.set(1,  index);        	
        }
        /*
        separating.set(1,  1);
         */
        normalise(separating);
        return separating;
    }

    private void normalise(ValueArrayAlgebra array) {
        ValueAlgebra entry = newValueWeight();
        ValueAlgebra sum = newValueWeight();
        OperatorEvaluator divide = ContextValue.get().getEvaluator(OperatorDivide.DIVIDE, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeWeight.get(), TypeWeight.get());
        entry.set(0);
        for (int i = 0; i < array.size(); i++) {
            array.get(entry, i);
            add.apply(sum, sum, entry);
        }
        for (int i = 0; i < array.size(); i++) {
            array.get(entry, i);
            divide.apply(entry, entry, sum);
            array.set(entry, i);
        }
    }

    public void improveNumerical(ValueArray current) {
        assert current != null;
        assert current.size() == dimension;
        assert current.getType().getEntryType() == TypeWeight.get();
        Value zero = UtilValue.newValue(TypeWeight.get(), 0);
        Value one = UtilValue.newValue(TypeWeight.get(), 1);
        Value negInf =UtilValue.newValue(TypeWeight.get(), UtilValue.NEG_INF);
        Value posInf = UtilValue.newValue(TypeWeight.get(), UtilValue.POS_INF);
        ConstraintSolver problem = contextSolver.newProblem();

        int[] wLpVars = new int[elements.size() + 1];
        for (int i = 0; i < elements.size(); i++) {
            wLpVars[i] = problem.addVariable(String.format(WEIGHT_VARIABLE, i), TypeWeight.get());
        }
        int dLpVar = problem.addVariable(DIFFERENCE_VARIABLE, TypeWeight.get(), negInf, posInf);
        ValueAlgebra entry = newValueWeight();        
        ValueArrayAlgebra problemWeights;
        int[] problemVariables;

        // new d1 value constraint
        problemWeights = newValueArrayWeight(elements.size() + 1);
        problemVariables = new int[elements.size() + 1];
        for (int elementNr = 0; elementNr < elements.size(); elementNr++) {
            problemVariables[elementNr] = wLpVars[elementNr];
            elements.get(elementNr).getQ().get(entry, 0);
            problemWeights.set(entry, elementNr);
        }
        problemVariables[elements.size()] = dLpVar;
        problemWeights.set(-1, elements.size());
        problem.addConstraint(problemWeights, problemVariables, ConstraintType.GE, zero);

        // other dimensions restriction
        for (int dim = 1; dim < this.dimension; dim++) {
            problemWeights = newValueArrayWeight(elements.size());
            problemVariables = new int[elements.size()];
            for (int elementNr = 0; elementNr < elements.size(); elementNr++) {
                problemVariables[elementNr] = wLpVars[elementNr];
                elements.get(elementNr).getQ().get(entry, dim);
                problemWeights.set(entry, elementNr);
            }
            current.get(entry, dim);
            problem.addConstraint(problemWeights, problemVariables, ConstraintType.GE, entry);
        }

        // weights nonnegative
        for (int elementNr = 0; elementNr < elements.size(); elementNr++) {
            problemWeights = newValueArrayWeight(1);
            problemVariables = new int[1];
            problemWeights.set(1, 0);
            problemVariables[0] = wLpVars[elementNr];
            problem.addConstraint(problemWeights, problemVariables, ConstraintType.GE, zero);
        }

        // sum equal to one
        problemWeights = newValueArrayWeight(elements.size());
        problemVariables = new int[elements.size()];
        for (int elementNr = 0; elementNr < elements.size(); elementNr++) {
            problemWeights.set(1, elementNr);
            problemVariables[elementNr] = wLpVars[elementNr];
        }
        problem.addConstraint(problemWeights, problemVariables, ConstraintType.EQ, one);

        // maximise first dimension
        problemWeights = newValueArrayWeight(1);
        problemVariables = new int[1];
        problemWeights.set(1, 0);
        problemVariables[0] = dLpVar;
        problem.setObjective(problemWeights, problemVariables);
        problem.setDirection(Direction.MAX);
        if (!problem.solve().isSat()) {
            problem.close();
            return;
        }
        ValueArray solverResult = problem.getResultVariablesValuesSingleType();
        Value opt = problem.getResultObjectiveValue();
        solverResult.get(entry, dLpVar);
        /*
        if (entry.isZero()) {
            problem.close();
            return;
        }
         */
        ValueArrayAlgebra result = newValueArrayWeight(dimension);
        for (int dim = 0; dim < dimension; dim++) {
            solverResult.get(entry, wLpVars[dim]);
            result.set(entry, dim);
        }
        problem.close();
        current.get(entry, 0);
        OperatorEvaluator max = ContextValue.get().getEvaluator(OperatorMax.MAX, entry.getType(), opt.getType());
        max.apply(entry, entry, opt);
        current.set(entry, 0);
    }

    public ValueArrayAlgebra findFeasibleRandomisedScheduler(ValueArray current) {
        assert current != null;
        assert current.size() == dimension;
        assert current.getType().getEntryType() == TypeWeight.get();
        assert elements.size() > 0;
        Value zero = UtilValue.newValue(TypeWeight.get(), 0);
        Value one = UtilValue.newValue(TypeWeight.get(), 1);
        ConstraintSolver problem = contextSolver.newProblem();

        int[] wLpVars = new int[elements.size()];
        for (int i = 0; i < elements.size(); i++) {
            wLpVars[i] = problem.addVariable(String.format(WEIGHT_VARIABLE, i), TypeWeight.get());
        }
        ValueAlgebra entry = newValueWeight();        
        ValueArrayAlgebra problemWeights;
        int[] problemVariables;

        // dimensions restriction
        for (int dim = 0; dim < this.dimension; dim++) {
            problemWeights = newValueArrayWeight(elements.size());
            problemVariables = new int[elements.size()];
            for (int elementNr = 0; elementNr < elements.size(); elementNr++) {
                problemVariables[elementNr] = wLpVars[elementNr];
                elements.get(elementNr).getQ().get(entry, dim);
                problemWeights.set(entry, elementNr);
            }
            current.get(entry, dim);
            problem.addConstraint(problemWeights, problemVariables, ConstraintType.GE, entry);
        }

        // weights nonnegative
        for (int elementNr = 0; elementNr < elements.size(); elementNr++) {
            problemWeights = newValueArrayWeight(1);
            problemVariables = new int[1];
            problemWeights.set(1, 0);
            problemVariables[0] = wLpVars[elementNr];
            problem.addConstraint(problemWeights, problemVariables, ConstraintType.GE, zero);
        }

        // sum equal to one
        problemWeights = newValueArrayWeight(elements.size());
        problemVariables = new int[elements.size()];
        for (int elementNr = 0; elementNr < elements.size(); elementNr++) {
            problemWeights.set(1, elementNr);
            problemVariables[elementNr] = wLpVars[elementNr];
        }
        problem.addConstraint(problemWeights, problemVariables, ConstraintType.EQ, one);

        problem.setDirection(Direction.FEASIBILITY);
        if (!problem.solve().isSat()) {
            problem.close();
            return null;
        }
        ValueArray solverResult = problem.getResultVariablesValuesSingleType();
        ValueArrayAlgebra result = newValueArrayWeight(elements.size());
        for (int dim = 0; dim < elements.size(); dim++) {
            solverResult.get(entry, wLpVars[dim]);
            result.set(entry, dim);
        }
        problem.close();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("down(");
        builder.append(elements);
        builder.append(")");
        return builder.toString();
    }

    private ValueArrayAlgebra newValueArrayWeight(int size) {
        TypeArray typeArray = TypeWeight.get().getTypeArray();
        return UtilValue.newArray(typeArray, size);
    }

    private ValueAlgebra newValueWeight() {
        return TypeWeight.get().newValue();
    }

    IterationResult get(int index) {
        return elements.get(index);
    }

    int size() {
        return elements.size();
    }
}
