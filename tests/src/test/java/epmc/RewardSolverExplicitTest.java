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
import org.junit.Test;

import epmc.error.EPMCException;
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
    public void hermanTest() throws EPMCException {
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

        constants.put("k", "1");
        result = computeResult(options, String.format(HERMAN_MODEL, 3), "R=? [ F \"stable\" {num_tokens=k}{max} ]");
        assertEquals("0", result, tolerance * 10);

        constants.put("k", "3");
        result = computeResult(options, String.format(HERMAN_MODEL, 3), "R=? [ F \"stable\" {num_tokens=k}{max} ]");
        assertEquals("1.333333333333263", result, tolerance * 10);

        constants.put("k", "1");
        result = computeResult(options, String.format(HERMAN_MODEL, 3), "R=? [ F \"stable\" {num_tokens=k}{min} ]");
        assertEquals("0", result, tolerance * 10);

        constants.put("k", "3");
        result = computeResult(options, String.format(HERMAN_MODEL, 3), "R=? [ F \"stable\" {num_tokens=k}{min} ]");
        assertEquals("1.333333333333263", result, tolerance * 10);
        
        constants.put("k", "1");
        result = computeResult(options, String.format(HERMAN_MODEL, 5), "R=? [ F \"stable\" {num_tokens=k}{max} ]");
        assertEquals("0", result, tolerance * 10);

        constants.put("k", "3");
        result = computeResult(options, String.format(HERMAN_MODEL, 5), "R=? [ F \"stable\" {num_tokens=k}{max} ]");
        assertEquals("3.199999999994394", result, tolerance * 10);

        constants.put("k", "5");
        result = computeResult(options, String.format(HERMAN_MODEL, 5), "R=? [ F \"stable\" {num_tokens=k}{max} ]");
        assertEquals("2.93333333332863", result, tolerance * 10);

        constants.put("k", "1");
        result = computeResult(options, String.format(HERMAN_MODEL, 5), "R=? [ F \"stable\" {num_tokens=k}{min} ]");
        assertEquals("0", result, tolerance * 10);

        constants.put("k", "3");
        result = computeResult(options, String.format(HERMAN_MODEL, 5), "R=? [ F \"stable\" {num_tokens=k}{min} ]");
        assertEquals("2.3999999999965356", result, tolerance * 10);

        constants.put("k", "5");
        result = computeResult(options, String.format(HERMAN_MODEL, 5), "R=? [ F \"stable\" {num_tokens=k}{min} ]");
        assertEquals("2.93333333332863", result, tolerance * 10);
        
        constants.put("k", "1");
        result = computeResult(options, String.format(HERMAN_MODEL, 7), "R=? [ F \"stable\" {num_tokens=k}{max} ]");
        assertEquals("0", result, tolerance * 10);

        constants.put("k", "3");
        result = computeResult(options, String.format(HERMAN_MODEL, 7), "R=? [ F \"stable\" {num_tokens=k}{max} ]");
        assertEquals("6.857142857113842", result, tolerance * 10);

        constants.put("k", "5");
        result = computeResult(options, String.format(HERMAN_MODEL, 7), "R=? [ F \"stable\" {num_tokens=k}{max} ]");
        assertEquals("5.97347480103774", result, tolerance * 10);

        constants.put("k", "7");
        result = computeResult(options, String.format(HERMAN_MODEL, 7), "R=? [ F \"stable\" {num_tokens=k}{max} ]");
        assertEquals("5.493326596754396", result, tolerance * 10);

        constants.put("k", "1");
        result = computeResult(options, String.format(HERMAN_MODEL, 7), "R=? [ F \"stable\" {num_tokens=k}{min} ]");
        assertEquals("0", result, tolerance * 10);

        constants.put("k", "3");
        result = computeResult(options, String.format(HERMAN_MODEL, 7), "R=? [ F \"stable\" {num_tokens=k}{min} ]");
        assertEquals("2.857142857135691", result, tolerance * 10);

        constants.put("k", "5");
        result = computeResult(options, String.format(HERMAN_MODEL, 7), "R=? [ F \"stable\" {num_tokens=k}{min} ]");
        assertEquals("5.0185676392389835", result, tolerance * 10);

        constants.put("k", "7");
        result = computeResult(options, String.format(HERMAN_MODEL, 7), "R=? [ F \"stable\" {num_tokens=k}{min} ]");
        assertEquals("5.493326596754396", result, tolerance * 10);

        constants.put("k", "1");
        result = computeResult(options, String.format(HERMAN_MODEL, 9), "R=? [ F \"stable\" {num_tokens=k}{max} ]");
        assertEquals("0", result, tolerance * 10);

        constants.put("k", "3");
        result = computeResult(options, String.format(HERMAN_MODEL, 9), "R=? [ F \"stable\" {num_tokens=k}{max} ]");
        assertEquals("11.999999999911205", result, tolerance * 10);

        constants.put("k", "5");
        result = computeResult(options, String.format(HERMAN_MODEL, 9), "R=? [ F \"stable\" {num_tokens=k}{max} ]");
        assertEquals("10.465782097231259", result, tolerance * 10);

        constants.put("k", "7");
        result = computeResult(options, String.format(HERMAN_MODEL, 9), "R=? [ F \"stable\" {num_tokens=k}{max} ]");
        assertEquals("9.436383808009637", result, tolerance * 10);

        constants.put("k", "9");
        result = computeResult(options, String.format(HERMAN_MODEL, 9), "R=? [ F \"stable\" {num_tokens=k}{max} ]");
        assertEquals("8.921607607343546", result, tolerance * 10);

        constants.put("k", "1");
        result = computeResult(options, String.format(HERMAN_MODEL, 9), "R=? [ F \"stable\" {num_tokens=k}{min} ]");
        assertEquals("0", result, tolerance * 10);

        constants.put("k", "3");
        result = computeResult(options, String.format(HERMAN_MODEL, 9), "R=? [ F \"stable\" {num_tokens=k}{min} ]");
        assertEquals("3.111111111100831", result, tolerance * 10);

        constants.put("k", "5");
        result = computeResult(options, String.format(HERMAN_MODEL, 9), "R=? [ F \"stable\" {num_tokens=k}{min} ]");
        assertEquals("6.274896428025171", result, tolerance * 10);

        constants.put("k", "7");
        result = computeResult(options, String.format(HERMAN_MODEL, 9), "R=? [ F \"stable\" {num_tokens=k}{min} ]");
        assertEquals("8.52434890313052", result, tolerance * 10);

        constants.put("k", "9");
        result = computeResult(options, String.format(HERMAN_MODEL, 9), "R=? [ F \"stable\" {num_tokens=k}{min} ]");
        assertEquals("8.921607607343546", result, tolerance * 10);
        
        constants.put("k", "1");
        result = computeResult(options, String.format(HERMAN_MODEL, 11), "R=? [ F \"stable\" {num_tokens=k}{max} ]");
        assertEquals("0", result, tolerance * 10);

        constants.put("k", "3");
        result = computeResult(options, String.format(HERMAN_MODEL, 11), "R=? [ F \"stable\" {num_tokens=k}{max} ]");
        assertEquals("17.45454545434597", result, tolerance * 10);

        constants.put("k", "5");
        result = computeResult(options, String.format(HERMAN_MODEL, 11), "R=? [ F \"stable\" {num_tokens=k}{max} ]");
        assertEquals("16.176636511659563", result, tolerance * 10);

        constants.put("k", "7");
        result = computeResult(options, String.format(HERMAN_MODEL, 11), "R=? [ F \"stable\" {num_tokens=k}{max} ]");
        assertEquals("14.675663430690614", result, tolerance * 10);

        constants.put("k", "9");
        result = computeResult(options, String.format(HERMAN_MODEL, 11), "R=? [ F \"stable\" {num_tokens=k}{max} ]");
        assertEquals("13.702267162121155", result, tolerance * 10);

        constants.put("k", "11");
        result = computeResult(options, String.format(HERMAN_MODEL, 11), "R=? [ F \"stable\" {num_tokens=k}{max} ]");
        assertEquals("13.205978227919868", result, tolerance * 10);

        constants.put("k", "1");
        result = computeResult(options, String.format(HERMAN_MODEL, 11), "R=? [ F \"stable\" {num_tokens=k}{min} ]");
        assertEquals("0", result, tolerance * 10);

        constants.put("k", "3");
        result = computeResult(options, String.format(HERMAN_MODEL, 11), "R=? [ F \"stable\" {num_tokens=k}{min} ]");
        assertEquals("3.272727272713583", result, tolerance * 10);

        constants.put("k", "5");
        result = computeResult(options, String.format(HERMAN_MODEL, 11), "R=? [ F \"stable\" {num_tokens=k}{min} ]");
        assertEquals("7.081213390188822", result, tolerance * 10);

        constants.put("k", "7");
        result = computeResult(options, String.format(HERMAN_MODEL, 11), "R=? [ F \"stable\" {num_tokens=k}{min} ]");
        assertEquals("10.71648285571693", result, tolerance * 10);

        constants.put("k", "9");
        result = computeResult(options, String.format(HERMAN_MODEL, 11), "R=? [ F \"stable\" {num_tokens=k}{min} ]");
        assertEquals("12.732263512313752", result, tolerance * 10);

        constants.put("k", "11");
        result = computeResult(options, String.format(HERMAN_MODEL, 11), "R=? [ F \"stable\" {num_tokens=k}{min} ]");
        assertEquals("13.205978227919868", result, tolerance * 10);
        
        close(options);
    }

    @Test
    public void ijTest() throws EPMCException {
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

        constants.put("k", "1");
        result = computeResult(options, String.format(IJ_MODEL, 3), "Rmax=? [ F \"stable\" {num_tokens=k}{max} ]");
        assertEquals("0", result, tolerance * 10);

        constants.put("k", "2");
        result = computeResult(options, String.format(IJ_MODEL, 3), "Rmax=? [ F \"stable\" {num_tokens=k}{max} ]");
        assertEquals("1.9999999999990905", result, tolerance * 10);

        constants.put("k", "3");
        result = computeResult(options, String.format(IJ_MODEL, 3), "Rmax=? [ F \"stable\" {num_tokens=k}{max} ]");
        assertEquals("2.999999999998181", result, tolerance * 10);
        
        constants.put("k", "1");
        result = computeResult(options, String.format(IJ_MODEL, 3), "Rmin=? [ F \"stable\" {num_tokens=k}{min} ]");
        assertEquals("0", result, tolerance * 10);

        constants.put("k", "2");
        result = computeResult(options, String.format(IJ_MODEL, 3), "Rmin=? [ F \"stable\" {num_tokens=k}{min} ]");
        assertEquals("1.9999999999990905", result, tolerance * 10);

        constants.put("k", "3");
        result = computeResult(options, String.format(IJ_MODEL, 3), "Rmin=? [ F \"stable\" {num_tokens=k}{min} ]");
        assertEquals("2.999999999998181", result, tolerance * 10);

        result = computeResult(options, String.format(IJ_MODEL, 3), "Rmin=? [ F \"stable\" {num_tokens=k}{min} ]");
        assertEquals("2.999999999998181", result, tolerance * 10);
        
        result = computeResult(options, String.format(IJ_MODEL, 7), "Rmax=? [ F \"stable\" {\"init\"}{max} ]");
        assertEquals("20.999999999813085", result, tolerance * 10);

        constants.put("k", "1");
        result = computeResult(options, String.format(IJ_MODEL, 7), "Rmax=? [ F \"stable\" {num_tokens=k}{max} ]");
        assertEquals("0", result, tolerance * 10);

        constants.put("k", "2");
        result = computeResult(options, String.format(IJ_MODEL, 7), "Rmax=? [ F \"stable\" {num_tokens=k}{max} ]");
        assertEquals("11.999999999927763", result, tolerance * 10);

        constants.put("k", "3");
        result = computeResult(options, String.format(IJ_MODEL, 7), "Rmax=? [ F \"stable\" {num_tokens=k}{max} ]");
        assertEquals("15.999999999889033", result, tolerance * 10);

        constants.put("k", "4");
        result = computeResult(options, String.format(IJ_MODEL, 7), "Rmax=? [ F \"stable\" {num_tokens=k}{max} ]");
        assertEquals("17.999999999862926", result, tolerance * 10);

        constants.put("k", "5");
        result = computeResult(options, String.format(IJ_MODEL, 7), "Rmax=? [ F \"stable\" {num_tokens=k}{max} ]");
        assertEquals("18.99999999984827", result, tolerance * 10);

        constants.put("k", "1");
        result = computeResult(options, String.format(IJ_MODEL, 7), "Rmin=? [ F \"stable\" {num_tokens=k}{min} ]");
        assertEquals("0", result, tolerance * 10);

        constants.put("k", "5");
        result = computeResult(options, String.format(IJ_MODEL, 7), "Rmin=? [ F \"stable\" {num_tokens=k}{min} ]");
        assertEquals("17.999999999874547", result, tolerance * 10);
        
        close(options);
    }

    @Test
    public void ijDiscountedTest() throws EPMCException {
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
    public void beauquierTest() throws EPMCException {
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

        constants.put("k", "1");
        result = computeResult(options, String.format(BEAUQUIER_MODEL, 3), "Rmax=? [ F num_tokens=1 {num_tokens=k}{max} ]");
        assertEquals("0", result, tolerance * 10);

        constants.put("k", "3");
        result = computeResult(options, String.format(BEAUQUIER_MODEL, 3), "Rmax=? [ F num_tokens=1 {num_tokens=k}{max} ]");
        assertEquals("1.999999999998181", result, tolerance * 10);
        
        constants.put("k", "1");
        result = computeResult(options, String.format(BEAUQUIER_MODEL, 3), "Rmin=? [ F num_tokens=1 {num_tokens=k}{min} ]");
        assertEquals("0", result, tolerance * 10);

        constants.put("k", "3");
        result = computeResult(options, String.format(BEAUQUIER_MODEL, 3), "Rmin=? [ F num_tokens=1 {num_tokens=k}{min} ]");
        assertEquals("1.999999999998181", result, tolerance * 10);

        result = computeResult(options, String.format(BEAUQUIER_MODEL, 7), "Rmax=? [ F num_tokens=1 {\"init\"}{max} ]");
        assertEquals("37.799223691293975", result, tolerance * 10);

        constants.put("k", "1");
        result = computeResult(options, String.format(BEAUQUIER_MODEL, 7), "Rmax=? [ F num_tokens=1 {num_tokens=k}{max} ]");
        assertEquals("0", result, tolerance * 10);

        constants.put("k", "3");
        result = computeResult(options, String.format(BEAUQUIER_MODEL, 7), "Rmax=? [ F num_tokens=1 {num_tokens=k}{max} ]");
        assertEquals("37.799223691293975", result, tolerance * 10);

        constants.put("k", "5");
        result = computeResult(options, String.format(BEAUQUIER_MODEL, 7), "Rmax=? [ F num_tokens=1 {num_tokens=k}{max} ]");
        assertEquals("34.593083223028785", result, tolerance * 10);

        constants.put("k", "1");
        result = computeResult(options, String.format(BEAUQUIER_MODEL, 7), "Rmin=? [ F num_tokens=1 {num_tokens=k}{min} ]");
        assertEquals("0", result, tolerance * 10);

        constants.put("k", "5");
        result = computeResult(options, String.format(BEAUQUIER_MODEL, 7), "Rmin=? [ F num_tokens=1 {num_tokens=k}{min} ]");
        assertEquals("5.00407207291629", result, tolerance * 10);
        
        close(options);
    }

    @Test
    public void testAndSetTest() throws EPMCException {
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
    public void leaderSynchronousTest() throws EPMCException {
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
    public void leaderAsynchronousTest() throws EPMCException {
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
    public void gossipDTMCTest() throws EPMCException {
        Options options = prepareOptions();
        double tolerance = 1E-9;
        Value result;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(TestHelper.PRISM_FLATTEN, false);
        Map<String,Object> constants = new HashMap<>();
        options.set(OptionsModelChecker.CONST, constants);

        constants.put("T", "0");
        result = computeResult(options, String.format(GOSSIP_DTMC_MODEL, 4), "R{\"max_path_len\"}=?[I=T]");
        assertEquals("4", result, tolerance * 10);
        
        constants.put("T", "1");
        result = computeResult(options, String.format(GOSSIP_DTMC_MODEL, 4), "R{\"max_path_len\"}=?[I=T]");
        assertEquals("4", result, tolerance * 10);

        constants.put("T", "100");
        result = computeResult(options, String.format(GOSSIP_DTMC_MODEL, 4), "R{\"max_path_len\"}=?[I=T]");
        assertEquals("3.140988475746578", result, tolerance * 10);

        constants.put("T", "500");
        result = computeResult(options, String.format(GOSSIP_DTMC_MODEL, 4), "R{\"max_path_len\"}=?[I=T]");
        assertEquals("2.093049152868182", result, tolerance * 10);

        constants.put("T", "2342");
        result = computeResult(options, String.format(GOSSIP_DTMC_MODEL, 4), "R{\"max_path_len\"}=?[I=T]");
        assertEquals("2.000000717569714", result, tolerance * 10);
        
        constants.put("T", "100");
        result = computeResult(options, String.format(GOSSIP_DTMC_MODEL, 4), "R{\"max_path_len\"}=?[I=T]+pow(R{\"max_path_len_sq\"}=?[I=T]-pow(R{\"max_path_len\"}=?[I=T],2), 0.5)");
        assertEquals("3.808321891967082", result, tolerance * 10);

        constants.put("T", "500");
        result = computeResult(options, String.format(GOSSIP_DTMC_MODEL, 4), "R{\"max_path_len\"}=?[I=T]+pow(R{\"max_path_len_sq\"}=?[I=T]-pow(R{\"max_path_len\"}=?[I=T],2), 0.5)");
        assertEquals("2.4843812717913174", result, tolerance * 10);

        constants.put("T", "2342");
        result = computeResult(options, String.format(GOSSIP_DTMC_MODEL, 4), "R{\"max_path_len\"}=?[I=T]+pow(R{\"max_path_len_sq\"}=?[I=T]-pow(R{\"max_path_len\"}=?[I=T],2), 0.5)");
        assertEquals("2.0011971678770712", result, tolerance * 1000);
        
        close(options);
    }

    @Test
    public void gossipMDPTest() throws EPMCException {
        Options options = prepareOptions();
        double tolerance = 1E-9;
        Value result;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(TestHelper.PRISM_FLATTEN, false);
        Map<String,Object> constants = new HashMap<>();
        options.set(OptionsModelChecker.CONST, constants);

        constants.put("T", "0");
        result = computeResult(options, String.format(GOSSIP_MODEL, 4), "R{\"max_path_len\"}min=?[I=T]");
        assertEquals("4", result, tolerance * 10);
        
        constants.put("T", "1");
        result = computeResult(options, String.format(GOSSIP_MODEL, 4), "R{\"max_path_len\"}min=?[I=T]");
        assertEquals("4", result, tolerance * 10);

        constants.put("T", "100");
        result = computeResult(options, String.format(GOSSIP_MODEL, 4), "R{\"max_path_len\"}min=?[I=T]");
        assertEquals("2.5224609375", result, tolerance * 10);

        constants.put("T", "500");
        result = computeResult(options, String.format(GOSSIP_MODEL, 4), "R{\"max_path_len\"}min=?[I=T]");
        assertEquals("2.0002606653354573", result, tolerance * 10);

        constants.put("T", "2342");
        result = computeResult(options, String.format(GOSSIP_MODEL, 4), "R{\"max_path_len\"}min=?[I=T]");
        assertEquals("2.0", result, tolerance * 10);
        
        constants.put("T", "0");
        result = computeResult(options, String.format(GOSSIP_MODEL, 4), "R{\"max_path_len\"}max=?[I=T]");
        assertEquals("4", result, tolerance * 10);
        
        constants.put("T", "1");
        result = computeResult(options, String.format(GOSSIP_MODEL, 4), "R{\"max_path_len\"}max=?[I=T]");
        assertEquals("4", result, tolerance * 10);

        constants.put("T", "100");
        result = computeResult(options, String.format(GOSSIP_MODEL, 4), "R{\"max_path_len\"}max=?[I=T]");
        assertEquals("3.765625", result, tolerance * 10);

        constants.put("T", "500");
        result = computeResult(options, String.format(GOSSIP_MODEL, 4), "R{\"max_path_len\"}max=?[I=T]");
        assertEquals("3.2876948298313122", result, tolerance * 10);

        constants.put("T", "2342");
        result = computeResult(options, String.format(GOSSIP_MODEL, 4), "R{\"max_path_len\"}max=?[I=T]");
        assertEquals("2.0817980708028925", result, tolerance * 100);
        
        close(options);
    }

    @Test
    public void diceTest() throws EPMCException {
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
