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
import static epmc.modelchecker.TestHelper.*;

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

public final class RewardSolverExplicitTest {

    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void hermanTest() {
        Value result;
        double tolerance = 1E-10;
        Options options = prepareOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);        
        options.set(TestHelper.PRISM_FLATTEN, false);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        Map<String,Object> constants = new HashMap<>();

        constants.put("N", "50");
        options.set(OptionsModelChecker.CONST, constants);

        result = computeResult(options, String.format(HERMAN_MODEL, 3), "R=? [ F \"stable\" {\"init\"}{max} ]");
        assertEquals("1.333333333333263", result, tolerance * 10);

        result = computeResult(options, String.format(HERMAN_MODEL, 5), "R=? [ F \"stable\" {\"init\"}{max} ]");
        assertEquals("3.199999999994394", result, tolerance * 10);

        result = computeResult(options, String.format(HERMAN_MODEL, 7), "R=? [ F \"stable\" {\"init\"}{max} ]");
        assertEquals("6.857142857113842", result, tolerance * 10);

        result = computeResult(options, String.format(HERMAN_MODEL, 9), "R=? [ F \"stable\" {\"init\"}{max} ]");
        assertEquals("11.999999999911205", result, tolerance * 10);

        result = computeResult(options, String.format(HERMAN_MODEL, 11), "R=? [ F \"stable\" {\"init\"}{max} ]");
        assertEquals("17.45454545434597", result, tolerance * 10);

        String propMax = "R=? [ F \"stable\" {num_tokens=%d}{max} ]";
        String propMin = "R=? [ F \"stable\" {num_tokens=%d}{min} ]";

        result = computeResult(options, String.format(HERMAN_MODEL, 3), String.format(propMax, 1));
        assertEquals("0", result, tolerance * 10);

        result = computeResult(options, String.format(HERMAN_MODEL, 3), String.format(propMax, 3));
        assertEquals("1.333333333333263", result, tolerance * 10);

        result = computeResult(options, String.format(HERMAN_MODEL, 3), String.format(propMin, 1));
        assertEquals("0", result, tolerance * 10);

        result = computeResult(options, String.format(HERMAN_MODEL, 3), String.format(propMin, 3));
        assertEquals("1.333333333333263", result, tolerance * 10);

        result = computeResult(options, String.format(HERMAN_MODEL, 5), String.format(propMax, 1));
        assertEquals("0", result, tolerance * 10);

        result = computeResult(options, String.format(HERMAN_MODEL, 5), String.format(propMax, 3));
        assertEquals("3.199999999994394", result, tolerance * 10);

        result = computeResult(options, String.format(HERMAN_MODEL, 5), String.format(propMax, 5));
        assertEquals("2.93333333332863", result, tolerance * 10);

        result = computeResult(options, String.format(HERMAN_MODEL, 5), String.format(propMin, 1));
        assertEquals("0", result, tolerance * 10);

        result = computeResult(options, String.format(HERMAN_MODEL, 5), String.format(propMin, 3));
        assertEquals("2.3999999999965356", result, tolerance * 10);

        constants.put("k", "5");
        result = computeResult(options, String.format(HERMAN_MODEL, 5), String.format(propMin, 5));
        assertEquals("2.93333333332863", result, tolerance * 10);

        result = computeResult(options, String.format(HERMAN_MODEL, 7), String.format(propMax, 1));
        assertEquals("0", result, tolerance * 10);

        result = computeResult(options, String.format(HERMAN_MODEL, 7), String.format(propMax, 3));
        assertEquals("6.857142857113842", result, tolerance * 10);

        result = computeResult(options, String.format(HERMAN_MODEL, 7), String.format(propMax, 5));
        assertEquals("5.97347480103774", result, tolerance * 10);

        result = computeResult(options, String.format(HERMAN_MODEL, 7), String.format(propMax, 7));
        assertEquals("5.493326596754396", result, tolerance * 10);

        result = computeResult(options, String.format(HERMAN_MODEL, 7), String.format(propMin, 1));
        assertEquals("0", result, tolerance * 10);

        result = computeResult(options, String.format(HERMAN_MODEL, 7), String.format(propMin, 3));
        assertEquals("2.857142857135691", result, tolerance * 10);

        result = computeResult(options, String.format(HERMAN_MODEL, 7), String.format(propMin, 5));
        assertEquals("5.0185676392389835", result, tolerance * 10);

        result = computeResult(options, String.format(HERMAN_MODEL, 7), String.format(propMin, 7));
        assertEquals("5.493326596754396", result, tolerance * 10);

        close(options);
    }

    @Test
    public void ijTest() {
        Options options = prepareOptions();
        double tolerance = 1E-10;
        Value result;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(TestHelper.PRISM_FLATTEN, false);
        Map<String,Object> constants = new HashMap<>();
        options.set(OptionsModelChecker.CONST, constants);

        result = computeResult(options, String.format(IJ_MODEL, 3), "Rmax=? [ F \"stable\" {\"init\"}{max} ]");
        assertEquals("2.999999999998181", result, tolerance * 10);

        String propMax = "Rmax=? [ F \"stable\" {num_tokens=%d}{max} ]";
        String propMin = "Rmin=? [ F \"stable\" {num_tokens=%d}{min} ]";

        result = computeResult(options, String.format(IJ_MODEL, 3), String.format(propMax, 1));
        assertEquals("0", result, tolerance * 10);

        result = computeResult(options, String.format(IJ_MODEL, 3), String.format(propMax, 2));
        assertEquals("1.9999999999990905", result, tolerance * 10);

        result = computeResult(options, String.format(IJ_MODEL, 3), String.format(propMax, 3));
        assertEquals("2.999999999998181", result, tolerance * 10);

        result = computeResult(options, String.format(IJ_MODEL, 3), String.format(propMin, 1));
        assertEquals("0", result, tolerance * 10);

        result = computeResult(options, String.format(IJ_MODEL, 3), String.format(propMin, 2));
        assertEquals("1.9999999999990905", result, tolerance * 10);

        result = computeResult(options, String.format(IJ_MODEL, 3), String.format(propMin, 3));
        assertEquals("2.999999999998181", result, tolerance * 10);

        result = computeResult(options, String.format(IJ_MODEL, 7), "Rmax=? [ F \"stable\" {\"init\"}{max} ]");
        assertEquals("20.999999999813085", result, tolerance * 10);

        result = computeResult(options, String.format(IJ_MODEL, 7), String.format(propMax, 1));
        assertEquals("0", result, tolerance * 10);

        result = computeResult(options, String.format(IJ_MODEL, 7), String.format(propMax, 2));
        assertEquals("11.999999999927763", result, tolerance * 10);

        result = computeResult(options, String.format(IJ_MODEL, 7), String.format(propMax, 3));
        assertEquals("15.999999999889033", result, tolerance * 10);

        result = computeResult(options, String.format(IJ_MODEL, 7), String.format(propMax, 4));
        assertEquals("17.999999999862926", result, tolerance * 10);

        result = computeResult(options, String.format(IJ_MODEL, 7), String.format(propMax, 5));
        assertEquals("18.99999999984827", result, tolerance * 10);

        result = computeResult(options, String.format(IJ_MODEL, 7), String.format(propMin, 1));
        assertEquals("0", result, tolerance * 10);

        result = computeResult(options, String.format(IJ_MODEL, 7), String.format(propMin, 5));
        assertEquals("17.999999999874547", result, tolerance * 10);

        close(options);
    }

    // TODO Moritz: ignoring this test for now, let's see what to do after the release
    @Ignore
    @Test
    public void ijDiscountedTest() {
        Options options = prepareOptions();
        double tolerance = 1E-10;
        Value result;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(TestHelper.PRISM_FLATTEN, false);
        Map<String,Object> constants = new HashMap<>();
        options.set(OptionsModelChecker.CONST, constants);

        result = computeResult(options, String.format(IJ_MODEL, 3), "Rmax=? [ C<=7 ]");
        System.out.println("RESULT " + result);
        result = computeResult(options, String.format(IJ_MODEL, 3), "Rmax=? [ C<=7, DISCOUNT=1 ]");
        System.out.println("RESULT " + result);
        result = computeResult(options, String.format(IJ_MODEL, 3), "Rmax=? [ C<=7, DISCOUNT=0.5 ]");
        System.out.println("RESULT " + result);
        result = computeResult(options, String.format(IJ_MODEL, 3), "Rmax=? [ C<=7, DISCOUNT=2 ]");
        System.out.println("RESULT " + result);
        //        assertEquals("2.999999999998181", result, tolerance * 10);

    }

    @Test
    public void beauquierTest() {
        Options options = prepareOptions();
        double tolerance = 1E-10;
        Value result;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(TestHelper.PRISM_FLATTEN, false);
        Map<String,Object> constants = new HashMap<>();
        options.set(OptionsModelChecker.CONST, constants);

        result = computeResult(options, String.format(BEAUQUIER_MODEL, 3), "Rmax=? [ F num_tokens=1 {\"init\"}{max} ]");
        assertEquals("1.999999999998181", result, tolerance * 10);

        String propA = "Rmax=? [ F num_tokens=1 {num_tokens=%d}{max} ]";
        String propB = "Rmin=? [ F num_tokens=1 {num_tokens=%d}{min} ]";

        result = computeResult(options, String.format(BEAUQUIER_MODEL, 3), String.format(propA, 1));
        assertEquals("0", result, tolerance * 10);

        result = computeResult(options, String.format(BEAUQUIER_MODEL, 3), String.format(propA, 3));
        assertEquals("1.999999999998181", result, tolerance * 10);

        constants.put("k", "1");
        result = computeResult(options, String.format(BEAUQUIER_MODEL, 3), String.format(propB, 1));
        assertEquals("0", result, tolerance * 10);

        result = computeResult(options, String.format(BEAUQUIER_MODEL, 3), String.format(propB, 3));
        assertEquals("1.999999999998181", result, tolerance * 10);

        result = computeResult(options, String.format(BEAUQUIER_MODEL, 7), "Rmax=? [ F num_tokens=1 {\"init\"}{max} ]");
        assertEquals("37.799223691293975", result, tolerance * 10);

        result = computeResult(options, String.format(BEAUQUIER_MODEL, 7), String.format(propA, 1));
        assertEquals("0", result, tolerance * 10);

        result = computeResult(options, String.format(BEAUQUIER_MODEL, 7), String.format(propA, 3));
        assertEquals("37.799223691293975", result, tolerance * 10);

        result = computeResult(options, String.format(BEAUQUIER_MODEL, 7), String.format(propA, 5));
        assertEquals("34.593083223028785", result, tolerance * 10);

        result = computeResult(options, String.format(BEAUQUIER_MODEL, 7), String.format(propB, 1));
        assertEquals("0", result, tolerance * 10);

        result = computeResult(options, String.format(BEAUQUIER_MODEL, 7), String.format(propB, 5));
        assertEquals("5.00407207291629", result, tolerance * 10);

        close(options);
    }

    @Test
    public void testAndSetTest() {
        Options options = prepareOptions();
        double tolerance = 1E-10;
        Value result;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(TestHelper.PRISM_FLATTEN, false);
        Map<String,Object> constants = new HashMap<>();
        options.set(OptionsModelChecker.CONST, constants);

        result = computeResult(options, TEST_AND_SET_MODEL, "filter(forall, t=1=>R{\"process0\"}<=10 [ F t=1&(l0=8|l0=9) ])");
        assertEquals(true, result);

        result = computeResult(options, TEST_AND_SET_MODEL, "filter(forall, t=1=>R{\"process1\"}<=10 [ F t=1&(l1=8|l1=9) ])");
        assertEquals(true, result);

        close(options);
    }

    @Test
    public void leaderSynchronousTest() {
        Options options = prepareOptions();
        double tolerance = 1E-10;
        Value result;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(TestHelper.PRISM_FLATTEN, false);
        Map<String,Object> constants = new HashMap<>();
        options.set(OptionsModelChecker.CONST, constants);

        result = computeResult(options,  String.format(LEADER_SYNC_MODEL, 3, 2), "R{\"num_rounds\"}=? [ F \"elected\" ]");
        assertEquals("1.3333333333330302", result, tolerance * 10);

        result = computeResult(options,  String.format(LEADER_SYNC_MODEL, 4, 5), "R{\"num_rounds\"}=? [ F \"elected\" ]");
        assertEquals("1.1160714285714137", result, tolerance * 10);

        close(options);
    }

    @Test
    public void leaderAsynchronousTest() {
        Options options = prepareOptions();
        double tolerance = 1E-10;
        Value result;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(TestHelper.PRISM_FLATTEN, false);
        Map<String,Object> constants = new HashMap<>();
        options.set(OptionsModelChecker.CONST, constants);

        result = computeResult(options, String.format(LEADER_ASYNC_MODEL, 3), "Rmin=? [ F \"elected\" ]");
        assertEquals("3.333333333321818", result, tolerance * 10);

        result = computeResult(options, String.format(LEADER_ASYNC_MODEL, 3), "Rmax=? [ F \"elected\" ]");
        assertEquals("3.333333333321818", result, tolerance * 10);

        result = computeResult(options, String.format(LEADER_ASYNC_MODEL, 6), "Rmin=? [ F \"elected\" ]");
        assertEquals("5.649769585206279", result, tolerance * 10);

        result = computeResult(options, String.format(LEADER_ASYNC_MODEL, 6), "Rmax=? [ F \"elected\" ]");
        assertEquals("5.649769585205323", result, tolerance * 10);

        close(options);
    }

    @Test
    public void gossipDTMCTest() {
        Options options = prepareOptions();
        double tolerance = 1E-9;
        Value result;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(TestHelper.PRISM_FLATTEN, false);

        String prop = "R{\"max_path_len\"}=?[I=%d]";
        result = computeResult(options, String.format(GOSSIP_DTMC_MODEL, 4), String.format(prop, 0));
        assertEquals("4", result, tolerance * 10);

        result = computeResult(options, String.format(GOSSIP_DTMC_MODEL, 4), String.format(prop, 1));
        assertEquals("4", result, tolerance * 10);

        result = computeResult(options, String.format(GOSSIP_DTMC_MODEL, 4), String.format(prop, 100));
        assertEquals("3.140988475746578", result, tolerance * 10);

        result = computeResult(options, String.format(GOSSIP_DTMC_MODEL, 4), String.format(prop, 500));
        assertEquals("2.093049152868182", result, tolerance * 10);

        result = computeResult(options, String.format(GOSSIP_DTMC_MODEL, 4), String.format(prop, 2342));
        assertEquals("2.000000717569714", result, tolerance * 10);

        String propB = "R{\"max_path_len\"}=?[I=%d]+pow(R{\"max_path_len_sq\"}=?[I=%d]-pow(R{\"max_path_len\"}=?[I=%d],2), 0.5)";
        result = computeResult(options, String.format(GOSSIP_DTMC_MODEL, 4), String.format(propB, 100, 100, 100));
        assertEquals("3.808321891967082", result, tolerance * 10);

        result = computeResult(options, String.format(GOSSIP_DTMC_MODEL, 4), String.format(propB, 500, 500, 500));
        assertEquals("2.4843812717913174", result, tolerance * 10);

        result = computeResult(options, String.format(GOSSIP_DTMC_MODEL, 4), String.format(propB, 2342, 2342, 2342));
        assertEquals("2.0011971678770712", result, tolerance * 1000);

        close(options);
    }

    @Test
    public void gossipMDPTest() {
        Options options = prepareOptions();
        double tolerance = 1E-9;
        Value result;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(TestHelper.PRISM_FLATTEN, false);

        String prop_min = "R{\"max_path_len\"}min=?[I=%d]";
        String prop_max = "R{\"max_path_len\"}max=?[I=%d]";

        result = computeResult(options, String.format(GOSSIP_MODEL, 4), String.format(prop_min, 0));
        assertEquals("4", result, tolerance * 10);

        result = computeResult(options, String.format(GOSSIP_MODEL, 4), String.format(prop_min, 1));
        assertEquals("4", result, tolerance * 10);

        result = computeResult(options, String.format(GOSSIP_MODEL, 4), String.format(prop_min, 100));
        assertEquals("2.5224609375", result, tolerance * 10);

        result = computeResult(options, String.format(GOSSIP_MODEL, 4), String.format(prop_min, 500));
        assertEquals("2.0002606653354573", result, tolerance * 10);

        result = computeResult(options, String.format(GOSSIP_MODEL, 4), String.format(prop_min, 2342));
        assertEquals("2.0", result, tolerance * 10);

        result = computeResult(options, String.format(GOSSIP_MODEL, 4), String.format(prop_max, 0));
        assertEquals("4", result, tolerance * 10);

        result = computeResult(options, String.format(GOSSIP_MODEL, 4), String.format(prop_max, 1));
        assertEquals("4", result, tolerance * 10);

        result = computeResult(options, String.format(GOSSIP_MODEL, 4), String.format(prop_max, 100));
        assertEquals("3.765625", result, tolerance * 10);

        result = computeResult(options, String.format(GOSSIP_MODEL, 4), String.format(prop_max, 500));
        assertEquals("3.2876948298313122", result, tolerance * 10);

        result = computeResult(options, String.format(GOSSIP_MODEL, 4), String.format(prop_max, 2342));
        assertEquals("2.0817980708028925", result, tolerance * 100);

        close(options);
    }

    @Test
    public void diceTest() {
        Options options = prepareOptions();
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.PRISM_FLATTEN, false);
        Value result = computeResult(options, DICE_MODEL, "R=? [ F s=7 ]");
        assertEquals("11/3", result, tolerance);
        close(options);
    }
}
