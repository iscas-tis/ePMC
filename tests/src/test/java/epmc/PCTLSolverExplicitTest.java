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
import static epmc.modelchecker.TestHelper.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.Model;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.value.TypeInterval;
import epmc.value.Value;
import epmc.value.ValueInterval;
import static org.junit.Assert.assertTrue;

public final class PCTLSolverExplicitTest {
    private final static int[] NUMBERS_PHIL_LSS = new int[]{3,4};
    private final static int[] K_PHIL_LSS = new int[]{4,5,6};
    private final static int[] STEP_BOUNDS_PHIL_LSS = new int[]{160};

    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void diceTest() {
        Options options = prepareOptions();
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.PRISM_FLATTEN, false);
        Value result1 = computeResult(options, ModelNamesPRISM.DICE_MODEL, "P=?[(F((s=7)&(d=1)))]");
        assertEquals("1/6", result1, tolerance);
        Value result2 = computeResult(options, ModelNamesPRISM.DICE_MODEL, "P=?[(F((s=7)&(d=2)))]");
        assertEquals("1/6", result2, tolerance);
        Value result3 = computeResult(options, ModelNamesPRISM.DICE_MODEL, "P=?[(F((s=7)&(d=3)))]");
        assertEquals("1/6", result3, tolerance);
        Value result4 = computeResult(options, ModelNamesPRISM.DICE_MODEL, "P=?[(F((s=7)&(d=4)))]");
        assertEquals("1/6", result4, tolerance);
        Value result5 = computeResult(options, ModelNamesPRISM.DICE_MODEL, "P=?[(F((s=7)&(d=5)))]");
        assertEquals("1/6", result5, tolerance);
        Value result6 = computeResult(options, ModelNamesPRISM.DICE_MODEL, "P=?[(F((s=7)&(d=6)))]");
        assertEquals("1/6", result6, tolerance);
        Value shouldBeTrue = computeResult(options, ModelNamesPRISM.DICE_MODEL, "P>=1 [F (s=7) ]");
        assertEquals(true, shouldBeTrue);
        close(options);
    }

    @Test
    public void twoDiceTest() {
        Options options = prepareOptions();
        double tolerance = 1E-11;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.PRISM_FLATTEN, false);
        Value result;
        result = computeResult(options, ModelNamesPRISM.TWO_DICE_MODEL, "Pmin=? [ F s1=7 & s2=7 & d1+d2=2 ]");
        assertEquals("1/36", result, tolerance);
        result = computeResult(options, ModelNamesPRISM.TWO_DICE_MODEL, "Pmin=? [ F s1=7 & s2=7 & d1+d2=3 ]");
        assertEquals("1/18", result, tolerance);
        result = computeResult(options, ModelNamesPRISM.TWO_DICE_MODEL, "Pmin=? [ F s1=7 & s2=7 & d1+d2=4 ]");
        assertEquals("3/36", result, tolerance);
        result = computeResult(options, ModelNamesPRISM.TWO_DICE_MODEL, "Pmin=? [ F s1=7 & s2=7 & d1+d2=5 ]");
        assertEquals("1/9", result, tolerance);
        result = computeResult(options, ModelNamesPRISM.TWO_DICE_MODEL, "Pmin=? [ F s1=7 & s2=7 & d1+d2=6 ]");
        assertEquals("5/36", result, tolerance);
        result = computeResult(options, ModelNamesPRISM.TWO_DICE_MODEL, "Pmin=? [ F s1=7 & s2=7 & d1+d2=7 ]");
        assertEquals("1/6", result, tolerance);
        result = computeResult(options, ModelNamesPRISM.TWO_DICE_MODEL, "Pmin=? [ F s1=7 & s2=7 & d1+d2=8 ]");
        assertEquals("5/36", result, tolerance);
        result = computeResult(options, ModelNamesPRISM.TWO_DICE_MODEL, "Pmin=? [ F s1=7 & s2=7 & d1+d2=9 ]");
        assertEquals("1/9", result, tolerance);
        result = computeResult(options, ModelNamesPRISM.TWO_DICE_MODEL, "Pmin=? [ F s1=7 & s2=7 & d1+d2=10 ]");
        assertEquals("3/36", result, tolerance);
        result = computeResult(options, ModelNamesPRISM.TWO_DICE_MODEL, "Pmin=? [ F s1=7 & s2=7 & d1+d2=11 ]");
        assertEquals("1/18", result, tolerance);
        result = computeResult(options, ModelNamesPRISM.TWO_DICE_MODEL, "Pmin=? [ F s1=7 & s2=7 & d1+d2=12 ]");
        assertEquals("1/36", result, tolerance);
        result = computeResult(options, ModelNamesPRISM.TWO_DICE_MODEL, "Pmax=? [ F s1=7 & s2=7 & d1+d2=2 ]");
        assertEquals("1/36", result, tolerance);
        result = computeResult(options, ModelNamesPRISM.TWO_DICE_MODEL, "Pmax=? [ F s1=7 & s2=7 & d1+d2=3 ]");
        assertEquals("1/18", result, tolerance);
        result = computeResult(options, ModelNamesPRISM.TWO_DICE_MODEL, "Pmax=? [ F s1=7 & s2=7 & d1+d2=4 ]");
        assertEquals("3/36", result, tolerance);
        result = computeResult(options, ModelNamesPRISM.TWO_DICE_MODEL, "Pmax=? [ F s1=7 & s2=7 & d1+d2=5 ]");
        assertEquals("1/9", result, tolerance);
        result = computeResult(options, ModelNamesPRISM.TWO_DICE_MODEL, "Pmax=? [ F s1=7 & s2=7 & d1+d2=6 ]");
        assertEquals("5/36", result, tolerance);
        result = computeResult(options, ModelNamesPRISM.TWO_DICE_MODEL, "Pmax=? [ F s1=7 & s2=7 & d1+d2=7 ]");
        assertEquals("1/6", result, tolerance);
        result = computeResult(options, ModelNamesPRISM.TWO_DICE_MODEL, "Pmax=? [ F s1=7 & s2=7 & d1+d2=8 ]");
        assertEquals("5/36", result, tolerance);
        result = computeResult(options, ModelNamesPRISM.TWO_DICE_MODEL, "Pmax=? [ F s1=7 & s2=7 & d1+d2=9 ]");
        assertEquals("1/9", result, tolerance);
        result = computeResult(options, ModelNamesPRISM.TWO_DICE_MODEL, "Pmax=? [ F s1=7 & s2=7 & d1+d2=10 ]");
        assertEquals("3/36", result, tolerance);
        result = computeResult(options, ModelNamesPRISM.TWO_DICE_MODEL, "Pmax=? [ F s1=7 & s2=7 & d1+d2=11 ]");
        assertEquals("1/18", result, tolerance);
        result = computeResult(options, ModelNamesPRISM.TWO_DICE_MODEL, "Pmax=? [ F s1=7 & s2=7 & d1+d2=12 ]");
        assertEquals("1/36", result, tolerance);
        close(options);
    }

    @Test
    public void cellTest() {
        Options options = prepareOptions();
        double tolerance = 1E-10;
        Value result;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(TestHelper.PRISM_FLATTEN, false);
        Map<String,Object> constants = new HashMap<>();
        constants.put("N", "50");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ModelNamesPRISM.CELL_MODEL, "P=?[ true U<=1 (n=50) {n<N}{max} ]");
        // TODO check
        assertEquals("0.4365170664480672", result, tolerance);

        result = computeResult(options, ModelNamesPRISM.CELL_MODEL, "P=?[ true U<=10 (n=50) {n<N}{max} ]");
        assertEquals("0.45595193814466106", result, tolerance);

        result = computeResult(options, ModelNamesPRISM.CELL_MODEL, "P=?[ true U<=100 (n=50) {n<N}{max} ]");
        assertEquals("0.6168762826457692", result, tolerance);

        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ModelNamesPRISM.CELL_MODEL, "P=?[ true U<=1000 (n=50) {n<N}{max} ]");
        assertEquals("0.9885090286432617", result, tolerance);
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

        for (int numProcs = ModelNamesPRISM.IJ_MIN_NUM_PROCS; numProcs <= ModelNamesPRISM.IJ_MAX_NUM_PROCS; numProcs++) {
            String ijInstance = String.format(ModelNamesPRISM.IJ_MODEL, numProcs);
            result = computeResult(options, ijInstance, "Pmin=? [ F ( q1+q2+q3=2 ) ]");
            assertEquals(newValue(TypeInterval.get(), "[0,1]"), result, tolerance);
        }

        for (int numProcs = ModelNamesPRISM.IJ_MIN_NUM_PROCS; numProcs <= ModelNamesPRISM.IJ_MAX_NUM_PROCS; numProcs++) {
            String ijInstance = String.format(ModelNamesPRISM.IJ_MODEL, numProcs);
            result = computeResult(options, ijInstance, "filter(forall, \"init\"=>P>=1 [ F \"stable\" ])");
            assertEquals(true, result);
        }

        result = computeResult(options, String.format(ModelNamesPRISM.IJ_MODEL, 3), "Pmin=? [ F<=0 \"stable\" {\"init\"}{min} ]");
        assertEquals("0", result, tolerance);

        result = computeResult(options, String.format(ModelNamesPRISM.IJ_MODEL, 3), "Pmin=? [ F<=1 \"stable\" {\"init\"}{min} ]");
        assertEquals("0", result, tolerance);

        result = computeResult(options, String.format(ModelNamesPRISM.IJ_MODEL, 3), "Pmin=? [ F<=2 \"stable\" {\"init\"}{min} ]");
        assertEquals("0.5", result, tolerance);

        result = computeResult(options, String.format(ModelNamesPRISM.IJ_MODEL, 3), "Pmin=? [ F<=3 \"stable\" {\"init\"}{min} ]");
        assertEquals("0.75", result, tolerance);

        result = computeResult(options, String.format(ModelNamesPRISM.IJ_MODEL, 3), "Pmin=? [ F<=4 \"stable\" {\"init\"}{min} ]");
        assertEquals("0.875", result, tolerance);

        result = computeResult(options, String.format(ModelNamesPRISM.IJ_MODEL, 3), "Pmin=? [ F<=5 \"stable\" {\"init\"}{min} ]");
        assertEquals("0.9375", result, tolerance);

        result = computeResult(options, String.format(ModelNamesPRISM.IJ_MODEL, 7), "Pmin=? [ F<=15 \"stable\" {\"init\"}{min} ]");
        assertEquals("0.30029296875", result, tolerance);

        result = computeResult(options, String.format(ModelNamesPRISM.IJ_MODEL, 7), "Pmin=? [ F<=16 \"stable\" {\"init\"}{min} ]");
        assertEquals("0.35394287109375", result, tolerance);

        result = computeResult(options, String.format(ModelNamesPRISM.IJ_MODEL, 7), "Pmin=? [ F<=20 \"stable\" {\"init\"}{min} ]");
        assertEquals("0.5360813140869141", result, tolerance);
        close(options);
    }

    @Test
    public void mutualTest() {
        Options options = prepareOptions();
        double tolerance = 1E-10;
        Value result;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(TestHelper.PRISM_FLATTEN, false);
        for (int numProcs : ModelNamesPRISM.MUTUAL_SIZES) {
            String mutualInstance = String.format(ModelNamesPRISM.MUTUAL_MODEL, numProcs);
            result = computeResult(options, mutualInstance, "Pmin=? [ F (num_crit=0) ]");
            assertEquals(1, result, tolerance);
        }
        close(options);
    }

    @Test
    public void dining_crypt3Test() {
        Options options = prepareOptions();
        double tolerance = 1E-10;
        Value result;
        options.set(TestHelper.PRISM_FLATTEN, false);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        result = computeResult(options, String.format(ModelNamesPRISM.DINING_CRYPT_MODEL, 3), "filter(forall, (pay=0) => P>=1 [ F ((\"done\") & (parity=func(mod, N, 2))) ])");
        assertEquals(true, result);
        result = computeResult(options, String.format(ModelNamesPRISM.DINING_CRYPT_MODEL, 3), "filter(forall, (pay>0) => P>=1 [ F ((\"done\") & (parity!=func(mod, N, 2))) ])");
        assertEquals(true, result);
        result = computeResult(options, String.format(ModelNamesPRISM.DINING_CRYPT_MODEL, 3), "Pmin=? [ F \"done\" & outcome = 0 {\"init\"&pay>0}{min} ]");
        assertEquals("0.25", result, tolerance);
        result = computeResult(options, String.format(ModelNamesPRISM.DINING_CRYPT_MODEL, 3), "Pmax=? [ F \"done\" & outcome = 0 {\"init\"&pay>0}{min} ]");
        assertEquals("0.25", result, tolerance);
        close(options);
    }

    @Test
    public void philLss4GloballyTest() {
        Options options = prepareOptions();
        Value result;
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Map<String,Object> constants = new HashMap<>();
        constants.put("K", "3");
        options.set(OptionsModelChecker.CONST, constants);
        options.set(TestHelper.PRISM_FLATTEN, false);

        //        result = computeResult(options, String.format(ModelNamesPRISM.PHIL_LSS_MODEL, 4), "Pmin=?[ F (\"entered\") ]");
        result = computeResult(options, String.format(ModelNamesPRISM.PHIL_LSS_MODEL, 4), "Pmin=?[ F (  ((p1>7) & (p1<13)) | ((p2>7) & (p2<13)) | ((p3>7) & (p3<13)) | ((p4>7) & (p4<13))  ) ]");
        assertEquals(0, result, tolerance);
        close(options);
    }

    @Test
    public void clusterBoundedUntilTest() {
        Options options = prepareOptions();
        Value result;
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Map<String,Object> constants = new HashMap<>();
        constants.put("N", "1");
        options.set(OptionsModelChecker.CONST, constants);
        options.set(TestHelper.PRISM_FLATTEN, false);
        result = computeResult(options, ModelNamesPRISM.CLUSTER_MODEL, "filter(min, P=? [ (\"minimum\") U<=0.5 (\"premium\")  ], \"minimum\");");
        // TODO check
        assertEquals("0.007682184285169857", result, tolerance);
        close(options);
    }

    @Ignore
    // TODO where is the "RP" label specified?
    @Test
    public void clusterGTest() {
        Options options = prepareOptions();
        Map<String,Object> constants = new HashMap<>();
        Value result;
        double tolerance = 1E-13;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.PRISM_FLATTEN, false);
        constants.put("N", "6");
        options.set(OptionsModelChecker.CONST, constants);

        result = computeResult(options, ModelNamesPRISM.CLUSTER_MODEL, "Pmax=? [ G(!\"RP\") ]");
        assertEquals(0, result, 1E-8);
        close(options);
    }

    @Test
    public void er12_1Test() {
        Options options = prepareOptions();
        Value result;
        double tolerance = 1E-13;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.PRISM_FLATTEN, false);

        result = computeResult(options, ER12_1, "P=?[G<=10 num_affected<=4]");
        assertTrue(ValueInterval.is(result));
        ValueInterval resultInterval = ValueInterval.as(result);
        assertEquals("0.8533520129860975", resultInterval.getIntervalLower(), 1E-8);
        assertEquals("0.8940213623509145", resultInterval.getIntervalUpper(), 1E-8);
        close(options);
    }

    @Test
    public void hermanQualitativeTest() {
        Value result;
        Options options = prepareOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);        
        options.set(TestHelper.PRISM_FLATTEN, false);
        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 3), "filter(forall, \"init\" => P>=1 [ F \"stable\" ])");
        assertEquals(true, result);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 5), "filter(forall, \"init\" => P>=1 [ F \"stable\" ])");
        assertEquals(true, result);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 7), "filter(forall, \"init\" => P>=1 [ F \"stable\" ])");
        assertEquals(true, result);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 9), "filter(forall, \"init\" => P>=1 [ F \"stable\" ])");
        assertEquals(true, result);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 11), "filter(forall, \"init\" => P>=1 [ F \"stable\" ])");
        assertEquals(true, result);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 13), "filter(forall, \"init\" => P>=1 [ F \"stable\" ])");
        assertEquals(true, result);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 15), "filter(forall, \"init\" => P>=1 [ F \"stable\" ])");
        assertEquals(true, result);
    }

    // TODO get tests to provide correct results again
    @Test
    public void hermanQuantitativeTest() {
        Value result;
        Options options = prepareOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);        
        options.set(TestHelper.PRISM_FLATTEN, false);
        double tolerance = 1E-10;
        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 3), "P=? [ F<=0 \"stable\" {\"init\"}{min} ]");
        assertEquals("0", result, tolerance * 10);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 3), "P=? [ F<=1 \"stable\" {\"init\"}{min} ]");
        assertEquals("0.75", result, tolerance * 10);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 3), "P=? [ F<=2 \"stable\" {\"init\"}{min} ]");
        assertEquals("0.9375", result, tolerance * 10);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 3), "P=? [ F<=3 \"stable\" {\"init\"}{min} ]");
        assertEquals("0.984375", result, tolerance * 10);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 3), "P=? [ F<=4 \"stable\" {\"init\"}{min} ]");
        assertEquals("0.99609375", result, tolerance * 10);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 3), "P=? [ F<=5 \"stable\" {\"init\"}{min} ]");
        assertEquals("0.9990234375", result, tolerance * 10);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 3), "P=? [ F<=6 \"stable\" {\"init\"}{min} ]");
        assertEquals("0.999755859375", result, tolerance * 10);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 3), "P=? [ F<=7 \"stable\" {\"init\"}{min} ]");
        assertEquals("0.99993896484375", result, tolerance * 10);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 3), "P=? [ F<=8 \"stable\" {\"init\"}{min} ]");
        assertEquals("0.9999847412109375", result, tolerance * 10);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 3), "P=? [ F<=9 \"stable\" {\"init\"}{min} ]");
        assertEquals("0.9999961853027344", result, tolerance * 10);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 3), "P=? [ F<=10 \"stable\" {\"init\"}{min} ]");
        assertEquals("0.9999990463256836", result, tolerance * 10);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 5), "P=? [ F<=0 \"stable\" {\"init\"}{min} ]");
        assertEquals("0", result, tolerance * 10);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 5), "P=? [ F<=1 \"stable\" {\"init\"}{min} ]");
        assertEquals("0.25", result, tolerance * 10);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 5), "P=? [ F<=2 \"stable\" {\"init\"}{min} ]");
        assertEquals("0.5", result, tolerance * 10);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 5), "P=? [ F<=3 \"stable\" {\"init\"}{min} ]");
        assertEquals("0.671875", result, tolerance * 10);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 5), "P=? [ F<=4 \"stable\" {\"init\"}{min} ]");
        assertEquals("0.78515625", result, tolerance * 10);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 5), "P=? [ F<=5 \"stable\" {\"init\"}{min} ]");
        assertEquals("0.859375", result, tolerance * 10);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 5), "P=? [ F<=6 \"stable\" {\"init\"}{min} ]");
        assertEquals("0.907958984375", result, tolerance * 10);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 5), "P=? [ F<=7 \"stable\" {\"init\"}{min} ]");
        assertEquals("0.93975830078125", result, tolerance * 10);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 5), "P=? [ F<=8 \"stable\" {\"init\"}{min} ]");
        assertEquals("0.9605712890625", result, tolerance * 10);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 5), "P=? [ F<=9 \"stable\" {\"init\"}{min} ]");
        assertEquals("0.9741935729980469", result, tolerance * 10);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 5), "P=? [ F<=10 \"stable\" {\"init\"}{min} ]");
        assertEquals("0.9831094741821289", result, tolerance * 10);
        close(options);
    }


    @Test
    public void beauquierTest() {
        Options options = prepareOptions();
        double tolerance = 1E-10;
        Value result;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(TestHelper.PRISM_FLATTEN, false);

        for (int numProcs = ModelNamesPRISM.BEAUQUIER_MIN_NUM_PROCS; numProcs <= ModelNamesPRISM.BEAUQUIER_MAX_NUM_PROCS; numProcs += 2) {
            String beauquierInstance = String.format(ModelNamesPRISM.BEAUQUIER_MODEL, numProcs);
            result = computeResult(options, beauquierInstance, "filter(forall, \"init\"=>P>=1 [ F num_tokens=1 ])");
            assertEquals(true, result);
        }

        result = computeResult(options, String.format(ModelNamesPRISM.BEAUQUIER_MODEL, 3), "Pmin=? [ F<=0 num_tokens=1 {\"init\"}{min} ]");
        assertEquals("0", result, tolerance);

        result = computeResult(options, String.format(ModelNamesPRISM.BEAUQUIER_MODEL, 3), "Pmin=? [ F<=1 num_tokens=1 {\"init\"}{min} ]");
        assertEquals("0.5", result, tolerance);

        result = computeResult(options, String.format(ModelNamesPRISM.BEAUQUIER_MODEL, 3), "Pmin=? [ F<=2 num_tokens=1 {\"init\"}{min} ]");
        assertEquals("0.75", result, tolerance);

        result = computeResult(options, String.format(ModelNamesPRISM.BEAUQUIER_MODEL, 3), "Pmin=? [ F<=3 num_tokens=1 {\"init\"}{min} ]");
        assertEquals("0.875", result, tolerance);

        result = computeResult(options, String.format(ModelNamesPRISM.BEAUQUIER_MODEL, 3), "Pmin=? [ F<=4 num_tokens=1 {\"init\"}{min} ]");
        assertEquals("0.9375", result, tolerance);

        result = computeResult(options, String.format(ModelNamesPRISM.BEAUQUIER_MODEL, 3), "Pmin=? [ F<=5 num_tokens=1 {\"init\"}{min} ]");
        assertEquals("0.96875", result, tolerance);

        result = computeResult(options, String.format(ModelNamesPRISM.BEAUQUIER_MODEL, 7), "Pmin=? [ F<=15 num_tokens=1 {\"init\"}{min} ]");
        assertEquals("0", result, tolerance);

        result = computeResult(options, String.format(ModelNamesPRISM.BEAUQUIER_MODEL, 7), "Pmin=? [ F<=50 num_tokens=1 {\"init\"}{min} ]");
        assertEquals("0.7303882837295532", result, tolerance);     
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

        result = computeResult(options,  String.format(ModelNamesPRISM.LEADER_SYNC_MODEL, 3, 2), "P=? [ F \"elected\" ]");
        assertEquals("1", result, tolerance * 10);

        result = computeResult(options,  String.format(ModelNamesPRISM.LEADER_SYNC_MODEL, 3, 2), "P=? [ F<=(0*(N+1)) \"elected\" ]");
        assertEquals("0", result, tolerance * 10);

        result = computeResult(options,  String.format(ModelNamesPRISM.LEADER_SYNC_MODEL, 3, 2), "P=? [ F<=(1*(N+1)) \"elected\" ]");
        assertEquals("0.75", result, tolerance * 10);

        result = computeResult(options,  String.format(ModelNamesPRISM.LEADER_SYNC_MODEL, 3, 2), "P=? [ F<=(2*(N+1)) \"elected\" ]");
        assertEquals("0.9375", result, tolerance * 10);

        result = computeResult(options,  String.format(ModelNamesPRISM.LEADER_SYNC_MODEL, 3, 2), "P=? [ F<=(3*(N+1)) \"elected\" ]");
        assertEquals("0.984375", result, tolerance * 10);

        result = computeResult(options,  String.format(ModelNamesPRISM.LEADER_SYNC_MODEL, 4, 5), "P=? [ F \"elected\" ]");
        assertEquals("1", result, tolerance * 10);

        result = computeResult(options,  String.format(ModelNamesPRISM.LEADER_SYNC_MODEL, 4, 5), "P=? [ F<=(0*(N+1)) \"elected\" ]");
        assertEquals("0", result, tolerance * 10);

        result = computeResult(options,  String.format(ModelNamesPRISM.LEADER_SYNC_MODEL, 4, 5), "P=? [ F<=(1*(N+1)) \"elected\" ]");
        assertEquals("0.8960000000000092", result, tolerance * 10);

        result = computeResult(options,  String.format(ModelNamesPRISM.LEADER_SYNC_MODEL, 4, 5), "P=? [ F<=(2*(N+1)) \"elected\" ]");
        assertEquals("0.9891840000000127", result, tolerance * 10);

        result = computeResult(options,  String.format(ModelNamesPRISM.LEADER_SYNC_MODEL, 4, 5), "P=? [ F<=(3*(N+1)) \"elected\" ]");
        assertEquals("0.9988751360000127", result, tolerance * 10);
        // TODO check why closing options causes an error
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
        /*
        result = computeResult(options,  String.format(LEADER_ASYNC_MODEL, 3), "filter(forall, leaders<=1)");
        assertEquals(true, result);

        result = computeResult(options,  String.format(LEADER_ASYNC_MODEL, 3), "P>=1 [ F \"elected\" ]");
        assertEquals(true, result);

        constants.put("K", "35");
        result = computeResult(options,  String.format(LEADER_ASYNC_MODEL, 3), "Pmin=? [ F<=K \"elected\" ]");
        assertEquals("0.8203125", result, tolerance * 10);

        constants.put("K", "35");
        result = computeResult(options,  String.format(LEADER_ASYNC_MODEL, 3), "Pmax=? [ F<=K \"elected\" ]");
        assertEquals("0.8203125", result, tolerance * 10);

         */
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options,  String.format(ModelNamesPRISM.LEADER_ASYNC_MODEL, 6), "filter(forall, leaders<=1)");
        assertEquals(true, result);

        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options,  String.format(ModelNamesPRISM.LEADER_ASYNC_MODEL, 6), "P>=1 [ F \"elected\" ]");
        assertEquals(true, result);

        result = computeResult(options,  String.format(ModelNamesPRISM.LEADER_ASYNC_MODEL, 6), "Pmin=? [ F<=35 \"elected\" ]");
        assertEquals("0", result, tolerance * 10);

        result = computeResult(options,  String.format(ModelNamesPRISM.LEADER_ASYNC_MODEL, 6), "Pmin=? [ F<=100 \"elected\" ]");
        assertEquals("0.7506952285766602", result, tolerance * 10);

        result = computeResult(options,  String.format(ModelNamesPRISM.LEADER_ASYNC_MODEL, 6), "Pmax=? [ F<=35 \"elected\" ]");
        assertEquals("0", result, tolerance * 10);

        result = computeResult(options,  String.format(ModelNamesPRISM.LEADER_ASYNC_MODEL, 6), "Pmax=? [ F<=100 \"elected\" ]");
        assertEquals("0.7506952285766602", result, tolerance * 10);
        close(options);
    }

    @Test
    public void rabinTest() {
        Options options = prepareOptions();
        double tolerance = 1E-10;
        Value result;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(TestHelper.PRISM_FLATTEN, false);

        result = computeResult(options,  String.format(ModelNamesPRISM.RABIN_MODEL, 3), "filter(forall, num_procs_in_crit<=1)");
        assertEquals(true, result);

        result = computeResult(options,  String.format(ModelNamesPRISM.RABIN_MODEL, 3), "filter(forall, \"one_trying\"=>P>=1 [ F \"one_critical\" ])");
        assertEquals(true, result);

        result = computeResult(options,  String.format(ModelNamesPRISM.RABIN_MODEL, 3), "Pmin=? [ !\"one_critical\" U (p1=2) {draw1=1&!\"one_critical\"}{min} ]");
        assertEquals("0", result, tolerance * 10);


        String paramProp = "Pmin=? [ !\"one_critical\" U (p1=2) {draw1=1&!\"one_critical\"&maxb<=%d}{min} ]";
        result = computeResult(options,  String.format(ModelNamesPRISM.RABIN_MODEL, 3), String.format(paramProp, 0));
        assertEquals("0.237457275390625", result, tolerance * 10);

        result = computeResult(options,  String.format(ModelNamesPRISM.RABIN_MODEL, 3), String.format(paramProp, 1));
        assertEquals("0.237457275390625", result, tolerance * 10);

        result = computeResult(options,  String.format(ModelNamesPRISM.RABIN_MODEL, 3), String.format(paramProp, 2));
        assertEquals("0.2080078125", result, tolerance * 10);

        result = computeResult(options,  String.format(ModelNamesPRISM.RABIN_MODEL, 3), String.format(paramProp, 3));
        assertEquals("0.1142578125", result, tolerance * 10);

        result = computeResult(options,  String.format(ModelNamesPRISM.RABIN_MODEL, 3), String.format(paramProp, 4));
        assertEquals("0.0595703125", result, tolerance * 10);

        result = computeResult(options,  String.format(ModelNamesPRISM.RABIN_MODEL, 3), String.format(paramProp, 5));
        assertEquals("0.0302734375", result, tolerance * 10);

        result = computeResult(options,  String.format(ModelNamesPRISM.RABIN_MODEL, 3), String.format(paramProp, 6));
        assertEquals("0", result, tolerance * 10);
        close(options);
    }

    @Ignore
    @Test
    public void rabin4Test() {
        Options options = prepareOptions();
        double tolerance = 1E-10;
        Value result;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(TestHelper.PRISM_FLATTEN, false);
        Map<String,Object> constants = new HashMap<>();
        options.set(OptionsModelChecker.CONST, constants);

        result = computeResult(options,  String.format(ModelNamesPRISM.RABIN_MODEL, 4), "filter(forall, num_procs_in_crit<=1)");
        assertEquals(true, result);

        result = computeResult(options,  String.format(ModelNamesPRISM.RABIN_MODEL, 4), "filter(forall, \"one_trying\"=>P>=1 [ F \"one_critical\" ])");
        assertEquals(true, result);

        result = computeResult(options,  String.format(ModelNamesPRISM.RABIN_MODEL, 4), "Pmin=? [ !\"one_critical\" U (p1=2) {draw1=1&!\"one_critical\"}{min} ]");
        assertEquals("0", result, tolerance * 10);

        constants.put("k", "0");
        result = computeResult(options,  String.format(ModelNamesPRISM.RABIN_MODEL, 4), "Pmin=? [ !\"one_critical\" U (p1=2) {draw1=1&!\"one_critical\"&maxb<=k}{min} ]");
        assertEquals("0.18001461029052734", result, tolerance * 10);

        constants.put("k", "1");
        result = computeResult(options,  String.format(ModelNamesPRISM.RABIN_MODEL, 4), "Pmin=? [ !\"one_critical\" U (p1=2) {draw1=1&!\"one_critical\"&maxb<=k}{min} ]");
        assertEquals("0.18001461029052734", result, tolerance * 10);

        constants.put("k", "2");
        result = computeResult(options,  String.format(ModelNamesPRISM.RABIN_MODEL, 4), "Pmin=? [ !\"one_critical\" U (p1=2) {draw1=1&!\"one_critical\"&maxb<=k}{min} ]");
        assertEquals("0.174957275390625", result, tolerance * 10);

        constants.put("k", "3");
        result = computeResult(options,  String.format(ModelNamesPRISM.RABIN_MODEL, 4), "Pmin=? [ !\"one_critical\" U (p1=2) {draw1=1&!\"one_critical\"&maxb<=k}{min} ]");
        assertEquals("0.104644775390625", result, tolerance * 10);

        constants.put("k", "4");
        result = computeResult(options,  String.format(ModelNamesPRISM.RABIN_MODEL, 4), "Pmin=? [ !\"one_critical\" U (p1=2) {draw1=1&!\"one_critical\"&maxb<=k}{min} ]");
        assertEquals("0.056793212890625", result, tolerance * 10);

        constants.put("k", "5");
        result = computeResult(options,  String.format(ModelNamesPRISM.RABIN_MODEL, 4), "Pmin=? [ !\"one_critical\" U (p1=2) {draw1=1&!\"one_critical\"&maxb<=k}{min} ]");
        assertEquals("0.029327392578125", result, tolerance * 10);

        constants.put("k", "6");
        result = computeResult(options,  String.format(ModelNamesPRISM.RABIN_MODEL, 4), "Pmin=? [ !\"one_critical\" U (p1=2) {draw1=1&!\"one_critical\"&maxb<=k}{min} ]");
        assertEquals("0", result, tolerance * 10);
        close(options);
    }

    @Test
    public void cyclinTest() {
        Options options = prepareOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(TestHelper.PRISM_FLATTEN, false);
        Map<String,Object> constants = new HashMap<>();
        options.set(OptionsModelChecker.CONST, constants);

        constants.put("N", "2");
        constants.put("k", "3");
        constants.put("t", "10");

        // TODO

        //        result = computeResult(options,  CYCLIN_MODEL, "P=? [ true U[t,t] cyclin_bound=k ]");
        //      assertEquals("0.007764446365536075", result, tolerance * 10);
        close(options);

    }

    @Test
    public void hermanOpenIntervalTest() {
        Value result;
        Options options = prepareOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);        
        options.set(TestHelper.PRISM_FLATTEN, false);
        double tolerance = 1E-10;

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 7), "filter(state, P=? [ G<6 !\"stable\" ], (x1=0)&(x2=0)&(x3=0)&(x4=0)&(x5=0)&(x6=0)&(x7=0))");
        assertEquals("0.35819912049919367", result, tolerance);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 7), "filter(state, P=? [ G<=6 !\"stable\" ], (x1=0)&(x2=0)&(x3=0)&(x4=0)&(x5=0)&(x6=0)&(x7=0))");
        assertEquals("0.28856343032384757", result, tolerance);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 7), "filter(state, P=? [ G<7 !\"stable\" ], (x1=0)&(x2=0)&(x3=0)&(x4=0)&(x5=0)&(x6=0)&(x7=0))");
        assertEquals("0.28856343032384757", result, tolerance);
    }

    @Ignore
    @Test
    public void philLssTest() {
        Options options = prepareOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        Value result;
        for (int numProcesses : NUMBERS_PHIL_LSS) {
            for (int K : K_PHIL_LSS) {
                Map<String,String> constants = new HashMap<>();
                constants.put("K", Integer.toString(K));
                options.set(OptionsModelChecker.CONST, constants);
                for (int numSteps : STEP_BOUNDS_PHIL_LSS) {
                    result = computeResult(options, String.format(ModelNamesPRISM.PHIL_LSS_MODEL, numProcesses),
                            String.format("Pmin=? [ true U<=%d \"entered\" {\"trying\"}{min} ]", numSteps));
                    System.out.println("RES " + result);
                }
            }
        }
    }
    
    @Test
    public void paulGainerMedium() {
        Options options = prepareOptions();
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.PRISM_FLATTEN, false);
        Map<String,String> constants = new HashMap<>();
        constants.put("ML", "0.01");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = loadModel(options, PAUL_GAINER_MEDIUM);
        Value result = TestHelper.computeResult(model, "R{\"power_consumption\"}=?[F synchronised]");
        System.out.println(result);
//        TestHelperGraph.exploreModel(options, ModelNamesOwn.PAUL_GAINER_MEDIUM);
        close(options);
    }    

    /**
     * Test case by Andrea Turrini.
     * Tests correctness of PCTL X operator.
     */
    @Test
    public void pctlRecognition() {
        Options options = prepareOptions();
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.PRISM_FLATTEN, false);
        Value result;
        
        result = computeResult(options, ModelNamesOwn.PCTL_RECOGNITION_TEST, "P=? [X y=1]");
        assertEquals("1/5", result, tolerance);
        
        result = computeResult(options, ModelNamesOwn.PCTL_RECOGNITION_TEST, "P>=1[(x=1) U (P>=1 [X y=1])]");
        assertEquals(false, result);
        
        result = computeResult(options, ModelNamesOwn.PCTL_RECOGNITION_TEST, "P>=1[(x=1) U (P=0.2 [X y=1])]");
        assertEquals(true, result);

        close(options);
    }
}
