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

package epmc;

import static epmc.ModelNamesPRISM.*;
import static epmc.modelchecker.TestHelper.assertEquals;
import static epmc.modelchecker.TestHelper.close;
import static epmc.modelchecker.TestHelper.computeResult;
import static epmc.modelchecker.TestHelper.prepare;
import static epmc.modelchecker.TestHelper.prepareOptions;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.value.Value;

public class LTLSolverExplicitTest {
    private final static String LTL_LAZY_INCREMENTAL = "ltl-lazy-incremental";

    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void clusterUntilTest() {
        Options options = prepareOptions();
        Map<String,Object> constants = new HashMap<>();
        Value result;
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        constants.put("N", "16");
        options.set(OptionsModelChecker.CONST, constants);

        // U-1
        result = computeResult(options, CLUSTER_MODEL, "P=? [(left_n=N) U ((left_n=N-1) U (right_n!=N)) ]");
        assertEquals("0.5094707891071129", result, tolerance);

        // U-2
        result = computeResult(options, CLUSTER_MODEL, "P=? [(left_n=N) U ((left_n=N-1) U ((left_n=N-2) U (right_n!=N))) ]");
        assertEquals("0.5096360303752221", result, tolerance);

        // U-3
        result = computeResult(options, CLUSTER_MODEL, "P=? [(left_n=N) U ((left_n=N-1) U ((left_n=N-2) U ((left_n=N-3) U (right_n!=N)))) ]");
        assertEquals("0.5096412753408213", result, tolerance);

        // U-4
        result = computeResult(options, CLUSTER_MODEL, "P=? [(left_n=N) U ((left_n=N-1) U ((left_n=N-2) U ((left_n=N-3) U ((left_n=N-4) U (right_n!=N))))) ]");
        assertEquals("0.5096417282249177", result, tolerance);

        // U-5
        result = computeResult(options, CLUSTER_MODEL, "P=? [(left_n=N) U ((left_n=N-1) U ((left_n=N-2) U ((left_n=N-3) U ((left_n=N-4) U ((left_n=N-5) U (right_n!=N)))))) ]");
        assertEquals("0.5096417820162603", result, tolerance);

        // U-6
        result = computeResult(options, CLUSTER_MODEL, "P=? [(left_n=N) U ((left_n=N-1) U ((left_n=N-2) U ((left_n=N-3) U ((left_n=N-4) U ((left_n=N-5) U ((left_n=N-6) U (right_n!=N))))))) ]");
        assertEquals("0.5096417883811015", result, tolerance);

        // U-7
        result = computeResult(options, CLUSTER_MODEL, "P=? [(left_n=N) U ((left_n=N-1) U ((left_n=N-2) U ((left_n=N-3) U ((left_n=N-4) U ((left_n=N-5) U ((left_n=N-6) U ((left_n=N-7) U (right_n!=N)))))))) ]");
        assertEquals("0.5096417890890955", result, tolerance);

        // U-8
        result = computeResult(options, CLUSTER_MODEL, "P=? [(left_n=N) U ((left_n=N-1) U ((left_n=N-2) U ((left_n=N-3) U ((left_n=N-4) U ((left_n=N-5) U ((left_n=N-6) U ((left_n=N-7) U ((left_n=N-8) U (right_n!=N))))))))) ]");
        assertEquals("0.5096417891616309", result, tolerance);
        close(options);
    }

    // TODO ignored because labels not defined in PRISM file
    @Test
    @Ignore
    public void firewireImplPatternTest() {
        Options options = prepareOptions();
        Map<String,String> constants = new HashMap<>();
        Value result;
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        constants.put("delay", "5");
        constants.put("fast", "0.4");
        constants.put("RQ", "1");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, FIREWIRE_IMPL_MODEL, "Pmax=? [ G(\"RQ\" => (((\"RP\" => (!\"RR\" U (\"RS\" & !\"RR\"))) U (\"RR\")) | (G(\"RP\" => (!\"RR\" U (\"RS\" & !\"RR\")))))) ]");
        assertEquals("0.76", result, tolerance);
        close(options);
    }

    @Test
    public void dining_crypt3Test() {
        Options options = prepareOptions();
        double tolerance = 1E-10;
        Value result;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        result = computeResult(options, String.format(DINING_CRYPT_MODEL, 3), "filter(forall, (pay>0) => P>=1 [ true & (F ((\"done\") & (parity!=func(mod, N, 2)))) ])");
        assertEquals(true, result);
        close(options);
    }

    /**
     * Test model provided by Li Yong.
     * The test case helped to detect an issue in the initial state
     * computation of BDD-based breakpoint automata.
     * 
     */
    @Test
    public void mdpttTest() {
        Options options = prepareOptions();
        double tolerance = 1E-10;
        Value result;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        result = computeResult(options, MDPTT_MODEL, "Pmax=? [ (G(F(p1=10))) & (G(F(p2=10))) & (G(F(p3=10))) ]");
        assertEquals("1", result, 1E-10);
        result = computeResult(options, MDPTT_MODEL, "Pmax=? [ ((G(F(p1=0))) | (F(G(p2!=0)))) & ((G(F(p2=0))) | (F(G(p3!=0)))) ]");
        assertEquals("1", result, 1E-10);
        result = computeResult(options, MDPTT_MODEL, "Pmax=? [ ((G(F(p1=0))) | (F(G(p1!=0)))) & ((G(F(p2=0))) | (F(G(p2!=0)))) & ((G(F(p3=0))) | (F(G(p3!=0)))) ]");
        assertEquals("1", result, 1E-10);
        result = computeResult(options, MDPTT_MODEL, "Pmax=? [ (F(G(p1=10))) ]");
        assertEquals("1", result, 1E-10);
        close(options);
    }

    @Test
    public void mutualTest() {
        Options options = prepareOptions();
        double tolerance = 1E-10;
        Value result;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        result = computeResult(options, String.format(MUTUAL_MODEL, 3), "Pmin=? [ ((G(F(p1!=10))) | (G(F(p1=0))) | (F(G(p1=1)))) & (G(F(p1!=0))) & (G(F(p1=1))) ]");
        System.out.println(result);

        result = computeResult(options, String.format(MUTUAL_MODEL, 3), "filter(forall, num_crit <= 1)");
        System.out.println(result);

        /* Check bug found by Andrea Turrini indeed fixed. */
        result = computeResult(options, String.format(MUTUAL_MODEL, 3), "filter(forall, num_crit > 0 => P>=1 [ F num_crit = 0 ])");
        assertEquals(false, result);
        System.out.println(result);
    }

    @Test
    public void problemAndreaNavigationTest() {
        Options options = prepareOptions();
        double tolerance = 1E-10;
        Value result;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));


        /* Check bug found by Andrea Turrini indeed fixed. 
        	The problem occurring was due to an incorrect computation of the
        	states which reach target states with a probability one when 
        	maximising. Due to an incorrect algorithm used, this set was
        	overestimated, leading to a result of one instead of 0.9510333.
         */
        result = computeResult(options, ModelNamesOwn.NAVIGATION_1, "Pmax=?[((F(at_goal)))]");
        assertEquals("0.9510333", result, 1E-7);
        result = computeResult(options, ModelNamesOwn.NAVIGATION_1, "Pmax=?[(F(F(at_goal)))]");
        assertEquals("0.9510333", result, 1E-7);
        result = computeResult(options, ModelNamesOwn.NAVIGATION_1, "Pmax=?[(G(F(at_goal)))]");
        assertEquals("0.9510333", result, 1E-7);
    }

    /**
     * Test for an issue in DTMC cluster model reported by Salomon Sickert
     * via Andrea Turrini about a probability being reported as infinity.
     */
    @Test
    public void clusterDTMC3Salomon() {
        Options options = prepareOptions();
        Value result;
        double tolerance = 1E-6;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Map<String,Object> constants = new HashMap<>();
        options.set(OptionsModelChecker.CONST, constants);
        options.set(TestHelper.PRISM_FLATTEN, true);
        result = computeResult(options, ModelNamesOwn.CLUSTER_DTMC_3_SALOMON, "P=? [(left_n=N) U ((left_n=N-1) U (left_n=N-2)) ]");
        assertEquals("0.9621298", result, tolerance);
        
        options.set(TestHelper.PRISM_FLATTEN, false);
        result = computeResult(options, ModelNamesOwn.CLUSTER_DTMC_3_SALOMON, "P=? [(left_n=N) U ((left_n=N-1) U (left_n=N-2)) ]");
        assertEquals("0.9621298", result, tolerance);
        close(options);
    }
    
    /**
     * Test for a problem reported by Andrea Turrini 17/03/2017.
     * In this case, the non-incremental LTL solver returned wrong results.
     */
    @Test
    public void petersonBugAndrea() {
        Options options = prepareOptions();
        Value result;
        double tolerance = 1E-6;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Map<String,Object> constants = new HashMap<>();
        options.set(OptionsModelChecker.CONST, constants);

        options.set(LTL_LAZY_INCREMENTAL, true);

        result = computeResult(options, ModelNamesOwn.PETERSON, "Pmin=?[G(state1 = 1 => (F(state1 = 3)))]");
        assertEquals("0", result, tolerance);
        
        result = computeResult(options, ModelNamesOwn.PETERSON, "Pmax=?[F(state1 = 1 & (G !(state1 = 3)))]");
        assertEquals("1", result, tolerance);

        options.set(LTL_LAZY_INCREMENTAL, false);

        result = computeResult(options, ModelNamesOwn.PETERSON, "Pmin=?[G(state1 = 1 => (F(state1 = 3)))]");
        assertEquals("0", result, tolerance);
        
        result = computeResult(options, ModelNamesOwn.PETERSON, "Pmax=?[F(state1 = 1 & (G !(state1 = 3)))]");
        assertEquals("1", result, tolerance);

        close(options);
    }
}
