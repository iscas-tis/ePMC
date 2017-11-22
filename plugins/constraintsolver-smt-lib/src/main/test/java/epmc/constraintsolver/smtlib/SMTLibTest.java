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

package epmc.constraintsolver.smtlib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static epmc.modelchecker.TestHelper.*;
import static epmc.value.UtilValue.newValue;

import epmc.constraintsolver.ConstraintSolver;
import epmc.constraintsolver.ConstraintSolverConfiguration;
import epmc.constraintsolver.ConstraintSolverResult;
import epmc.constraintsolver.Feature;
import epmc.constraintsolver.smtlib.ConstraintSolverSMTLib;
import epmc.constraintsolver.smtlib.options.OptionsSMTLib;
import epmc.constraintsolver.smtlib.options.SMTLibVersion;
import epmc.main.options.UtilOptionsEPMC;
import epmc.modelchecker.UtilModelChecker;
import epmc.options.Options;
import epmc.plugin.OptionsPlugin;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;

public class SMTLibTest {
    /** Location of plugin directory in file system. */
    private final static String PLUGIN_DIR = System.getProperty("user.dir") + "/target/classes/";

    /**
     * Set up the tests.
     */
    @BeforeClass
    public static void initialise() {
        prepare();
    }

    /**
     * Prepare options including loading JANI plugin.
     * 
     * @return options usable for JANI model analysis
     */
    private final static Options prepareISat3Options() {
        Options options = UtilOptionsEPMC.newOptions();
        options.set(OptionsPlugin.PLUGIN, PLUGIN_DIR);
        prepareOptions(options);
        return options;
    }

    @Test
    public void iSat3ManualTest() {
        Options options = prepareISat3Options();
        options.set(OptionsSMTLib.SMTLIB_VERSION, SMTLibVersion.V25);
        ConstraintSolverConfiguration configuration = new ConstraintSolverConfiguration();
        configuration.requireFeature(Feature.SMT);
        ConstraintSolver solver = configuration.newProblem();
        assert solver instanceof ConstraintSolverSMTLib;
        TypeInteger typeInteger = TypeInteger.get();
        List<String> commandLine = new ArrayList<>();
        commandLine.add("/Users/emhahn/z3-master/build/z3");
        commandLine.add("-smt2");
        commandLine.add("{0}");
        options.set(OptionsSMTLib.SMTLIB_COMMAND_LINE, commandLine);
        solver.addVariable("a", typeInteger, newValue(typeInteger, 1), newValue(typeInteger, 100));
        solver.addVariable("b", typeInteger, newValue(typeInteger, 1), newValue(typeInteger, 100));
        solver.addVariable("c", typeInteger, newValue(typeInteger, 1), newValue(typeInteger, 100));

        solver.addConstraint(UtilModelChecker.parseExpression("a*a + b*b = c*c"));
        assertEquals(ConstraintSolverResult.SAT, solver.solve());
        /* multiple solutions possible, don't test result values */
    }

    @Test
    public void andreaTest() {
        Options options = prepareISat3Options();
        ConstraintSolverConfiguration configuration = new ConstraintSolverConfiguration();
        configuration.requireFeature(Feature.SMT);
        ConstraintSolver solver = configuration.newProblem();
        assert solver instanceof ConstraintSolverSMTLib;
        TypeReal typeReal = TypeReal.get();
        TypeInteger typeInteger = TypeInteger.get();
        List<String> commandLine = new ArrayList<>();
        commandLine.add("/Users/emhahn/z3-master/build/z3");
        commandLine.add("-smt2");
        commandLine.add("{0}");
        /*
        commandLine.add("/Users/emhahn/yices.sh");
        commandLine.add("{0}");
         */
        options.set(OptionsSMTLib.SMTLIB_COMMAND_LINE, commandLine);
        solver.addVariable("s0", typeReal, newValue(typeReal, 1), newValue(typeInteger, 1));
        solver.addVariable("s1", typeReal, newValue(typeReal, 0), newValue(typeInteger, 1));
        solver.addVariable("s2", typeReal, newValue(typeReal, 0), newValue(typeInteger, 0));
        solver.addVariable("err", typeReal, newValue(typeReal, 0), newValue(typeInteger, 10));

        solver.addConstraint(UtilModelChecker.parseExpression("(s0 = (0.5*err)*s1 + (1.0-(0.5*err))*s2) & (s1 = 1)"));
        assertEquals(ConstraintSolverResult.SAT, solver.solve());
        assertEquals("1", solver.getResultVariablesValues()[0], 1E-100);
        assertEquals("1", solver.getResultVariablesValues()[1], 1E-100);
        assertEquals("0", solver.getResultVariablesValues()[2], 1E-100);
        assertEquals("2", solver.getResultVariablesValues()[3], 1E-100);
    }

    @Test
    public void fractionTest() {
        Options options = prepareISat3Options();
        ConstraintSolverConfiguration configuration = new ConstraintSolverConfiguration();
        configuration.requireFeature(Feature.SMT);
        ConstraintSolver solver = configuration.newProblem();
        assert solver instanceof ConstraintSolverSMTLib;
        TypeReal typeReal = TypeReal.get();
        List<String> commandLine = new ArrayList<>();
        commandLine.add("/Users/emhahn/z3-master/build/z3");
        commandLine.add("-smt2");
        commandLine.add("{0}");
        /*
        commandLine.add("/Users/emhahn/yices.sh");
        commandLine.add("{0}");
         */
        options.set(OptionsSMTLib.SMTLIB_COMMAND_LINE, commandLine);
        solver.addVariable("s0", typeReal, newValue(typeReal, 1), newValue(typeReal, 1));
        solver.addVariable("s1", typeReal, newValue(typeReal, 0), newValue(typeReal, 1));
        solver.addVariable("s2", typeReal, newValue(typeReal, 0), newValue(typeReal, 0));
        solver.addVariable("err", typeReal, newValue(typeReal, 0), newValue(typeReal, 10));

        solver.addConstraint(UtilModelChecker.parseExpression("(s0 = (0.4*err)*s1 + (1.0-(0.6*err))*s2) & (s1 = 1)"));
        assertEquals(ConstraintSolverResult.SAT, solver.solve());
        System.out.println(Arrays.toString(solver.getResultVariablesValues()));
    }

}
