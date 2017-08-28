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

package epmc.solver;


import static epmc.modelchecker.TestHelper.assertEquals;
import static epmc.modelchecker.TestHelper.prepare;
import static epmc.modelchecker.TestHelper.prepareOptions;
import static epmc.value.UtilValue.newValue;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.constraintsolver.ConstraintSolver;
import epmc.constraintsolver.ConstraintSolverConfiguration;
import epmc.constraintsolver.ConstraintType;
import epmc.constraintsolver.Direction;
import epmc.constraintsolver.Feature;
import epmc.value.TypeReal;
import epmc.value.Value;
import epmc.value.ValueArray;

public class SolverLPTest {

    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void wikipediaTest() {
        final double tolerance = 1E-10;
        prepareOptions();
        ConstraintSolverConfiguration contextSolver = new ConstraintSolverConfiguration();
        contextSolver.requireFeature(Feature.LP);
        ConstraintSolver problem = contextSolver.newProblem();
        problem.setDirection(Direction.MAX);
        int x = problem.addVariable("x", TypeReal.get());
        int y = problem.addVariable("y", TypeReal.get());
        TypeReal typeReal = TypeReal.get();
        int[] variables = new int[]{x, y};

        problem.setObjective(new Value[]{newValue(typeReal, 300), newValue(typeReal, 500)},
                variables);
        problem.addConstraint(new Value[]{newValue(typeReal, 1), newValue(typeReal, 2)}, variables,
                ConstraintType.LE, newValue(typeReal, 170));
        problem.addConstraint(new Value[]{newValue(typeReal, 1), newValue(typeReal, 1)}, variables,
                ConstraintType.LE, newValue(typeReal, 150));
        problem.addConstraint(new Value[]{newValue(typeReal, 0), newValue(typeReal, 3)}, variables,
                ConstraintType.LE, newValue(typeReal, 180));
        problem.solve();
        Value optimalValue = problem.getResultObjectiveValue();
        ValueArray optimalVariables = problem.getResultVariablesValuesSingleType();
        assertEquals(49000, optimalValue, tolerance);
        Value entry = typeReal.newValue();
        optimalVariables.get(entry, x);
        assertEquals(130, entry, tolerance);
        optimalVariables.get(entry, y);
        assertEquals(20, entry, tolerance);
    }
}
