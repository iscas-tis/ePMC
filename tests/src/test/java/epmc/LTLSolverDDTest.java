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

import static epmc.ModelNamesOwn.*;
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

import epmc.modelchecker.EngineDD;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.value.Value;

public class LTLSolverDDTest {
    private final static String LTL_LAZY_USE_SUBSET = "ltl-lazy-use-subset";
    private final static String LTL_LAZY_USE_BREAKPOINT = "ltl-lazy-use-breakpoint";
    private final static String LTL_LAZY_USE_BREAKPOINT_SINGLETONS = "ltl-lazy-use-breakpoint-singletons";
    private final static String LTL_LAZY_USE_RABIN = "ltl-lazy-use-rabin";
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
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        constants.put("N", "16");
        options.set(OptionsModelChecker.CONST, constants);
        // U-1
        result = computeResult(options, ModelNamesPRISM.CLUSTER_MODEL, "P=? [(left_n=N) U ((left_n=N-1) U (right_n!=N)) ]");
        assertEquals("0.5094707891071129", result, tolerance);

        // U-2
        result = computeResult(options, ModelNamesPRISM.CLUSTER_MODEL, "P=? [(left_n=N) U ((left_n=N-1) U ((left_n=N-2) U (right_n!=N))) ]");
        assertEquals("0.5096360303752221", result, tolerance);

        // U-3
        result = computeResult(options, ModelNamesPRISM.CLUSTER_MODEL, "P=? [(left_n=N) U ((left_n=N-1) U ((left_n=N-2) U ((left_n=N-3) U (right_n!=N)))) ]");
        assertEquals("0.5096412753408213", result, tolerance);

        // U-4
        result = computeResult(options, ModelNamesPRISM.CLUSTER_MODEL, "P=? [(left_n=N) U ((left_n=N-1) U ((left_n=N-2) U ((left_n=N-3) U ((left_n=N-4) U (right_n!=N))))) ]");
        assertEquals("0.5096417282249177", result, tolerance);

        // U-5
        result = computeResult(options, ModelNamesPRISM.CLUSTER_MODEL, "P=? [(left_n=N) U ((left_n=N-1) U ((left_n=N-2) U ((left_n=N-3) U ((left_n=N-4) U ((left_n=N-5) U (right_n!=N)))))) ]");
        assertEquals("0.5096417820162603", result, tolerance);

        // U-6
        result = computeResult(options, ModelNamesPRISM.CLUSTER_MODEL, "P=? [(left_n=N) U ((left_n=N-1) U ((left_n=N-2) U ((left_n=N-3) U ((left_n=N-4) U ((left_n=N-5) U ((left_n=N-6) U (right_n!=N))))))) ]");
        assertEquals("0.5096417883811015", result, tolerance);

        // U-7
        result = computeResult(options, ModelNamesPRISM.CLUSTER_MODEL, "P=? [(left_n=N) U ((left_n=N-1) U ((left_n=N-2) U ((left_n=N-3) U ((left_n=N-4) U ((left_n=N-5) U ((left_n=N-6) U ((left_n=N-7) U (right_n!=N)))))))) ]");
        assertEquals("0.5096417890890955", result, tolerance);

        // U-8
        result = computeResult(options, ModelNamesPRISM.CLUSTER_MODEL, "P=? [(left_n=N) U ((left_n=N-1) U ((left_n=N-2) U ((left_n=N-3) U ((left_n=N-4) U ((left_n=N-5) U ((left_n=N-6) U ((left_n=N-7) U ((left_n=N-8) U (right_n!=N))))))))) ]");
        assertEquals("0.5096417891616309", result, tolerance);
        close(options);
    }

    @Test
    public void clusterBugAndreaTest() {
        Options options = prepareOptions();
        Map<String,Object> constants = new HashMap<>();
        Value result;
        double tolerance = 1E-12;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        //        constants.put("N", "16");
        options.set(OptionsModelChecker.CONST, constants);
        // U-1
        result = computeResult(options, CLUSTER_DTMC_1, "P=? [(left_n=N) U (((left_n=N-1) & (right_n!=N)) U (right_n!=N)) ]");
        System.out.println(result);
        //        assertEquals("0.5094707891071129", result, tolerance);

        close(options);
    }

    @Ignore
    @Test
    public void firewireImplPatternRabinTest() {
        Options options = prepareOptions();
        Map<String,String> constants = new HashMap<>();
        Value result;
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        constants.put("delay", "5");
        constants.put("fast", "0.4");
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        options.set(OptionsModelChecker.CONST, constants);
        options.set(LTL_LAZY_USE_SUBSET, false);
        options.set(LTL_LAZY_USE_BREAKPOINT, false);
        options.set(LTL_LAZY_USE_BREAKPOINT_SINGLETONS, false);
        options.set(LTL_LAZY_USE_RABIN, true);
        options.set(LTL_LAZY_INCREMENTAL, true);
        result = computeResult(options, ModelNamesPRISM.FIREWIRE_IMPL_MODEL, "Pmax=? [ G(\"RQ\" => (((\"RP\" => (!\"RR\" U (\"RS\" & !\"RR\"))) U (\"RR\")) | (G(\"RP\" => (!\"RR\" U (\"RS\" & !\"RR\")))))) ]");
        assertEquals("0.76", result, tolerance);
        close(options);
    }

    @Test
    // TODO define custom labels
    @Ignore
    public void philLss3PatternRabinTest() {
        Options options = prepareOptions();
        Map<String,String> constants = new HashMap<>();
        Value result;
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        constants.put("K", "3");
        //        options.set(Option.MDP_ENCODING_MODE, Options.MDPEncoding.STATE_DISTRIBUTION);
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        options.set(OptionsModelChecker.CONST, constants);
        options.set(LTL_LAZY_USE_SUBSET, false);
        options.set(LTL_LAZY_USE_BREAKPOINT, false);
        options.set(LTL_LAZY_USE_BREAKPOINT_SINGLETONS, false);
        options.set(LTL_LAZY_USE_RABIN, true);
        options.set(LTL_LAZY_INCREMENTAL, true);
        result = computeResult(options, String.format(ModelNamesPRISM.PHIL_LSS_MODEL, 3), "Pmax=? [ G(\"RQ\" => (((\"RP\" => (!\"RR\" U (\"RS\" & !\"RR\"))) U (\"RR\")) | (G(\"RP\" => (!\"RR\" U (\"RS\" & !\"RR\")))))) ]");
        assertEquals(1, result, tolerance);
        close(options);
    }


    @Test
    public void zeroconfTest() {
        Options options = prepareOptions();
        Map<String,String> constants = new HashMap<>();
        Value result;
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);

        constants.put("n", "12");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ZEROCONF_SIMPLE, "P=?[(F( s=-1 )) & true ]");
        assertEquals("0.9831102937590025", result, tolerance);

        constants.put("n", "15");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ZEROCONF_SIMPLE, "P=?[(F( s=-1 )) & true ]");
        assertEquals("0.9912806035987779", result, tolerance);
        close(options);
    }

    @Test
    public void zeroconfBugFoundByLiYongTest() {
        Options options = prepareOptions();
        Map<String,String> constants = new HashMap<>();
        Value result;
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        //        options.set(OptionsAutomaton.LTL2BA_DET_NEG, OptionsAutomaton.Ltl2BaDetNeg.NEVER);
        options.set("ltl2ba-det-neg", "never");
        options.set(LTL_LAZY_USE_SUBSET, false);
        options.set(LTL_LAZY_USE_BREAKPOINT, false);
        options.set(LTL_LAZY_USE_BREAKPOINT_SINGLETONS, false);        
        options.set(LTL_LAZY_USE_RABIN, true);
        options.set(LTL_LAZY_INCREMENTAL, false);

        constants.put("n", "1");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ZEROCONF_SIMPLE, "P=?[!(((G (F (s=-2))) | (F (G (s=-1)))) & ((G (F (s=0))) | (F (G (s=n)))))]");
        assertEquals("1", result, tolerance);
        close(options);
    }

    @Test
    public void simpleTest() {
        Options options = prepareOptions();
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        computeResult(options, SIMPLE, "P=?[(F( s=1 )) & true ]");
        close(options);
    }

    @Test
    public void twoDiceTest() {
        Options options = prepareOptions();
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        double tolerance = 1E-11;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        Value result1 = computeResult(options, ModelNamesPRISM.TWO_DICE_MODEL, "Pmin=? [ true & (F s1=7 & s2=7 & d1+d2=2) ]");
        assertEquals("1/36", result1, tolerance * 4);
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
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        result = computeResult(options, ModelNamesPRISM.MDPTT_MODEL, "Pmax=? [ (G(F(p1=10))) & (G(F(p2=10))) & (G(F(p3=10))) ]");
        assertEquals("1", result, 1E-10);
        result = computeResult(options, ModelNamesPRISM.MDPTT_MODEL, "Pmax=? [ ((G(F(p1=0))) | (F(G(p2!=0)))) & ((G(F(p2=0))) | (F(G(p3!=0)))) ]");
        assertEquals("1", result, 1E-10);
        result = computeResult(options, ModelNamesPRISM.MDPTT_MODEL, "Pmax=? [ ((G(F(p1=0))) | (F(G(p1!=0)))) & ((G(F(p2=0))) | (F(G(p2!=0)))) & ((G(F(p3=0))) | (F(G(p3!=0)))) ]");
        assertEquals("1", result, 1E-10);
        result = computeResult(options, ModelNamesPRISM.MDPTT_MODEL, "Pmax=? [ (F(G(p1=10))) ]");
        assertEquals("1", result, 1E-10);

        close(options);
    }

}
