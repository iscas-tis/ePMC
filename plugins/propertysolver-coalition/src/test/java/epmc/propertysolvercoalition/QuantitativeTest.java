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

package epmc.propertysolvercoalition;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.main.options.UtilOptionsEPMC;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.plugin.OptionsPlugin;
import epmc.value.Value;

import static epmc.modelchecker.TestHelper.*;
import static epmc.propertysolvercoalition.ModelNames.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Class containing the JUnit tests for the quantitative coalition solver.
 * 
 * @author Ernst Moritz Hahn
 */
public final class QuantitativeTest {
    private final static String USER_DIR = TestHelper.USER_DIR;
    private final static String TARGET_CLASSES = "/target/classes/";
    private final static String PLUGIN_DIR = System.getProperty(USER_DIR) + TARGET_CLASSES;

    @BeforeClass
    public static void initialise() {
        prepare();
    }

    private final static Options prepareCoalitionOptions() {
        Options options = UtilOptionsEPMC.newOptions();
        options.set(OptionsPlugin.PLUGIN, PLUGIN_DIR);
        prepareOptions(options, LogType.TRANSLATE, TestHelper.MODEL_INPUT_TYPE_PRISM);
        return options;
    }

    @Test
    public void robotOldQuantitativeTest() {
        Options options = prepareCoalitionOptions();
        double tolerance = 1E-10;
        //        options.set(OptionsValue.ITERATION_NATIVE, false);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Map<String,String> constants = new HashMap<>();
        Value result;

        //        options.set(Options.ITERATION_NATIVE, false);
        //      options.set(Options.ITERATION_METHOD, OptionsSet.IterationMethod.JACOBI);
        //        constants.put("size", "4");
        //      options.set(OptionsEPMC.CONST, constants);
        //    result = computeResult(options, ROBOTS_MODIFIED_MEDIUM, "<<1>> Pmax=? [ (!\"z1\") U (\"z2\")  ] ");
        //  assertEquals("0.96484375", result, 1E-8);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> Pmax=? [ (!\"z1\") U (\"z2\")  ] ");
        assertEquals("0.5830078125", result, 1E-8);
        close(options);

        /*
        constants.put("size", "8");
        options.set(Options.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> Pmax=1 [ (!\"z1\") U (\"z2\")  ] ");
        System.out.println(result);
         */

        //        assertEquals(true, result);
    }


    @Test
    public void robotQuantitativeTest() {
        Options options = prepareCoalitionOptions();
        double tolerance = 1E-9;
        options.set("prism-flatten", false);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        //        options.set(TestHelper.it, value);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Map<String,String> constants = new HashMap<>();
        Value result;

        constants.put("size", "10");
        constants.put("n_bullets", "1");
        constants.put("p_hit_1_tile", "0.3");
        options.set(OptionsModelChecker.CONST, constants);

        result = computeResult(options, ROBOTS_QUANTITATIVE, "<<2>> Pmax=? [ !((G(F(\"z2\"))) & (G(F(\"z3\")))) ]");
        assertEquals("0.1821956", result, 1E-6);

        result = computeResult(options, ROBOTS_QUANTITATIVE, "<<1>> Pmax=? [ (G(F(\"z2\"))) & (G(F(\"z3\"))) ]");
        assertEquals("0.8178044", result, 1E-6);

        constants.put("size", "8");
        constants.put("n_bullets", "2");
        constants.put("p_hit_1_tile", "0.3");
        options.set(OptionsModelChecker.CONST, constants);

        result = computeResult(options, ROBOTS_QUANTITATIVE, "<<2>> Pmax=? [ !(((F(\"z2\"))) & ((F(\"z3\")))) ]");
        assertEquals("0.1359274", result, 1E-6);

        result = computeResult(options, ROBOTS_QUANTITATIVE, "<<1>> Pmax=? [ (((F(\"z2\"))) & ((F(\"z3\")))) ]");
        assertEquals("0.8640726", result, 1E-6);

        constants.put("size", "9");
        constants.put("n_bullets", "2");
        constants.put("p_hit_1_tile", "0.3");
        options.set(OptionsModelChecker.CONST, constants);

        result = computeResult(options, ROBOTS_QUANTITATIVE, "<<2>> Pmax=? [ !(((F(\"z2\"))) & ((F(\"z3\")))) ]");
        assertEquals("0.214627", result, 1E-6);

        result = computeResult(options, ROBOTS_QUANTITATIVE, "<<1>> Pmax=? [ (((F(\"z2\"))) & ((F(\"z3\")))) ]");
        assertEquals("0.785373", result, 1E-6);

        constants.put("size", "10");
        constants.put("n_bullets", "2");
        constants.put("p_hit_1_tile", "0.3");
        options.set(OptionsModelChecker.CONST, constants);

        result = computeResult(options, ROBOTS_QUANTITATIVE, "<<2>> Pmax=? [ !(((F(\"z2\"))) & ((F(\"z3\")))) ]");
        assertEquals("0.2518487", result, 1E-6);

        result = computeResult(options, ROBOTS_QUANTITATIVE, "<<1>> Pmax=? [ (((F(\"z2\"))) & ((F(\"z3\")))) ]");
        assertEquals("0.7481513", result, 1E-6);        

        close(options);
    }

    /**
     * Test to make sure that a certain bug found by Andrea Turrini is fixed.
     * 
     */
    @Test
    public void robotBugAndreaTest() {
        Options options = prepareCoalitionOptions();
        double tolerance = 1E-9;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Value result;
        Map<String,String> constants = new HashMap<>();
        options.set(OptionsModelChecker.CONST, constants);

        /*
        result = computeResult(options, ROBOTS_SMALL4, "<<player1>> Pmax=? [ (G(F(\"z3\")))]");
        assertEquals("1", result, 1E-6);
         */

        result = computeResult(options, ROBOTS_SMALL4, "<<player2>> Pmax=? [ !(G(F(\"z3\")))]");
        assertEquals("0", result, 1E-6);

        close(options);
    }
}
