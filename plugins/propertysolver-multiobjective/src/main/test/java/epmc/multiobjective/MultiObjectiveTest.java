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

import static epmc.modelchecker.TestHelper.assertEquals;
import static epmc.modelchecker.TestHelper.close;
import static epmc.modelchecker.TestHelper.computeResult;
import static epmc.modelchecker.TestHelper.prepare;
import static epmc.modelchecker.TestHelper.prepareOptions;
import static epmc.multiobjective.ModelNames.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.main.options.UtilOptionsEPMC;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.plugin.OptionsPlugin;
import epmc.value.Value;

public class MultiObjectiveTest {
    private final static String PLUGIN_DIR = System.getProperty("user.dir") + "/target/classes/";

    @BeforeClass
    public static void initialise() {
        prepare();
    }

    private final static Options prepareMultiOptions() {
        Options options = UtilOptionsEPMC.newOptions();
        options.set(OptionsPlugin.PLUGIN, PLUGIN_DIR);
        prepareOptions(options);
        // TODO HACK because loading of native libraries seems not to work correctly
        // when loading native files from other plugins
        return options;
    }

    @Test
    public void miniQualitative() {
        Options options = prepareMultiOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Value result;

        result = computeResult(options, MULTI_OBJECTIVE_SIMPLE, "multi(P>=1 [ F x=1 ], P>=1[ F x=2 ])");
        assertEquals(true, result);

        result = computeResult(options, MULTI_OBJECTIVE_SIMPLE, "multi(P>=1 [ F (G(x=1)) ], P>=1[ F(G( x=2)) ])");
        assertEquals(false, result);

        result = computeResult(options, MULTI_OBJECTIVE_SIMPLE, "multi(P>=1 [ F (G(x=1)) ], P>=1[ F(G( x=2)) ])");
        assertEquals(false, result);

        result = computeResult(options, MULTI_OBJECTIVE_SIMPLE, "multi(P>=0.1 [ F (G(x=1)) ], P>=0.1[ F(G( x=2)) ])");
        assertEquals(true, result);

        result = computeResult(options, MULTI_OBJECTIVE_SIMPLE, "multi(P>=0.5 [ F (G(x=1)) ], P>=0.5[ F(G( x=2)) ])");
        assertEquals(true, result);

        result = computeResult(options, MULTI_OBJECTIVE_SIMPLE, "multi(P>=0.6 [ F (G(x=1)) ], P>=0.5[ F(G( x=2)) ])");
        assertEquals(false, result);

        result = computeResult(options, MULTI_OBJECTIVE_SIMPLE, "multi(P>=0.5 [ F (G(x=1)) ], P>=0.6[ F(G( x=2)) ])");
        assertEquals(false, result);

        result = computeResult(options, MULTI_OBJECTIVE_SIMPLE, "multi(P>=0.6 [ F (G(x=1)) ], P>=0.4[ F(G( x=2)) ])");
        assertEquals(true, result);

        result = computeResult(options, MULTI_OBJECTIVE_SIMPLE, "multi(P>=0.7 [ F (G(x=1)) ], P>=0.4[ F(G( x=2)) ])");
        assertEquals(false, result);

        result = computeResult(options, MULTI_OBJECTIVE_SIMPLE, "multi(P>=1 [ F (x=1) ], P>=0.4[ F(G( x=2)) ])");
        assertEquals(true, result);

        result = computeResult(options, MULTI_OBJECTIVE_SIMPLE, "multi(P>=1 [ F (x=1) ], P>=1[ F(G( x=2)) ])");
        assertEquals(true, result);
        close(options);
    }

    @Test
    public void miniQuantitative() {
        Options options = prepareMultiOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Value result;

        result = computeResult(options, MULTI_OBJECTIVE_SIMPLE, "multi(Pmax=? [ F x=1 ], P>=1[ F x=2 ])");
        assertEquals(1, result, 1E-10);

        result = computeResult(options, MULTI_OBJECTIVE_SIMPLE, "multi(Pmax=? [ F (G(x=1)) ], P>=1[ F(G( x=2)) ])");
        assertEquals(0, result, 1E-10);

        result = computeResult(options, MULTI_OBJECTIVE_SIMPLE, "multi(Pmax=? [ F (G(x=1)) ], P>=0.1[ F(G( x=2)) ])");
        assertEquals("0.9", result, 1E-10);

        result = computeResult(options, MULTI_OBJECTIVE_SIMPLE, "multi(Pmax=? [ F (G(x=1)) ], P>=0.5[ F(G( x=2)) ])");
        assertEquals("0.5", result, 1E-10);

        result = computeResult(options, MULTI_OBJECTIVE_SIMPLE, "multi(Pmax=? [ F (G(x=1)) ], P>=0.6[ F(G( x=2)) ])");
        assertEquals("0.4", result, 1E-10);

        result = computeResult(options, MULTI_OBJECTIVE_SIMPLE, "multi(Pmin=? [ F (G(x=1)) ], P>=0.6[ F(G( x=2)) ])");
        assertEquals(0, result, 1E-10);

        result = computeResult(options, MULTI_OBJECTIVE_SIMPLE, "multi(Pmin=? [ F (G(x=2)) ], P>=0.6[ F(G( x=2)) ])");
        assertEquals("0.6", result, 1E-10);

        result = computeResult(options, MULTI_OBJECTIVE_SIMPLE, "multi(Pmax=? [ F (G(x=2)) ], P>=0.6[ F(G( x=2)) ])");
        assertEquals(1, result, 1E-10);
        close(options);
    }

    @Test
    public void miniQualitativeRewards() {
        Options options = prepareMultiOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Value result;

        result = computeResult(options, MULTI_OBJECTIVE_SIMPLE_REWARDS, "multi(R>=2 [ C ])");
        assertEquals(true, result);

        //      result = computeResult(options, MULTI_OBJECTIVE_SIMPLE_REWARDS, "multi(R>=2.1 [ C ], P>=1[ F x=3 ])");
        //        assertEquals(false, result);
        close(options);
    }

    @Test
    public void miniQuantitativeRewards() {
        Options options = prepareMultiOptions();
        double tolerance = 1E-20;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Value result;

        result = computeResult(options, MULTI_OBJECTIVE_SIMPLE_REWARDS, "multi(Rmax=? [ C ], P>=1[ F x=3 ])");
        assertEquals("2", result, 1E-10);

        result = computeResult(options, MULTI_OBJECTIVE_SIMPLE_REWARDS, "multi(R>=2 [ C ], Pmax=?[ F x=3 ])");
        assertEquals("1", result, 1E-10);

        result = computeResult(options, MULTI_OBJECTIVE_SIMPLE_REWARDS, "multi(Rmax=? [ C ], P>=0.5[ F x=3 ])");
        assertEquals("3", result, 1E-10);

        result = computeResult(options, MULTI_OBJECTIVE_SIMPLE_REWARDS, "multi(Rmax=? [ C ], P>=0.1[ F x=3 ])");
        assertEquals("3.8", result, 1E-10);


        //        result = computeResult(options, MULTI_OBJECTIVE_SIMPLE_REWARDS, "multi(Rmin=? [ C ], P>=0.5[ F x=3 ])");
        //        System.out.println("result " + result);
        //        assertEquals("0", result, 1E-10);

        //        result = computeResult(options, MULTI_OBJECTIVE_SIMPLE_REWARDS, "multi(Rmin=? [ C ], P<=0.5[ F x=3 ])");
        //        result = computeResult(options, MULTI_OBJECTIVE_SIMPLE_REWARDS, "multi(Rmax=?[C], P>=0.9[!(F(x=3))])");
        //        System.out.println(result);
        close(options);
    }

    @Test
    public void dinnerReducedProbBounded1() {
        Options options = prepareMultiOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Map<String,Object> constants = new HashMap<>();
        constants.put("stepN", "2");
        options.set(OptionsModelChecker.CONST, constants);
        Value result;

        result = computeResult(options, DINNER_REDUCED_PROB_BOUNDED_1, "multi(P>=0.75[ F (!running & sated & kitchenClean) ], P>=1[ F(X(act = eatPizza)) ])");
        assertEquals(false, result);

        result = computeResult(options, DINNER_REDUCED_PROB_BOUNDED_1, "multi(P>=0[ F (!running & sated & kitchenClean) ], P>=0.951[ F(X(act = eatPizza)) ])");
        assertEquals(false, result);

        result = computeResult(options, DINNER_REDUCED_PROB_BOUNDED_1, "multi(Pmax=?[ F (!running & sated & kitchenClean) ], P>=0.95[ F(X(act = eatPizza)) ])");
        assertEquals("0.9025", result, 1E-10);

        result = computeResult(options, DINNER_REDUCED_PROB_BOUNDED_1, "multi(P>=0.95[ F(X(act = eatPizza)) ])");
        assertEquals(true, result);

        result = computeResult(options, DINNER_REDUCED_PROB_BOUNDED_1, "multi(P>=0.95001[ F(X(act = eatPizza)) ])");
        assertEquals(false, result);

        result = computeResult(options, DINNER_REDUCED_PROB_BOUNDED_1, "multi(Pmax=?[ F(X(act = eatPizza)) ])");
        assertEquals("0.95", result, 1E-10);
    }    

    /**
     * Checking absence of bug which had lead to an infinite loop in the
     * multi-objective property checker.
     * 
     */
    @Test
    public void andreaBug() {
        Options options = prepareMultiOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Map<String,Object> constants = new HashMap<>();
        constants.put("N", "2");
        options.set(OptionsModelChecker.CONST, constants);

        Value result;

        result = computeResult(options, ANDREA_BUG, "multi(P>=1[F(state=N)],P>=1[F(state=0)])");
        assertEquals(true, result);

        result = computeResult(options, ANDREA_BUG, "multi(P>=0.999[F(state=N)],P>=0.999[F(state=0)])");
        assertEquals(true, result);
    }

    @Test
    public void andreaBugSecond() {
        Options options = prepareMultiOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Map<String,Object> constants = new HashMap<>();
        constants.put("N_ROWS", "3");
        constants.put("N_COLS", "4");
        options.set(OptionsModelChecker.CONST, constants);

        Value result;

        result = computeResult(options, ANDREA_BUG_SECOND, "multi(P>=0.75[F(halted & at_goal)], P>=1[G(!(row=N_ROWS-1 & col>N_COLS-3))])");
        System.out.println(result);
    }
}
