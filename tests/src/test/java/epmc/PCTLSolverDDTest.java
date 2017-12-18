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

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.VariableDD;
import epmc.modelchecker.EngineDD;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.value.TypeInteger;
import epmc.value.Value;

public class PCTLSolverDDTest {

    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void coin06UntilTest() {
        Options options = prepareOptions();
        Map<String,Object> constants = new HashMap<>();
        Value result;
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        //        options.set(OptionsDD.DD_BINARY_ENGINE, "buddy");
        options.set("dd-and-exist", true);
        options.set("dd-init-cache-size", "2621440");
        constants.put("K", "16");
        options.set(OptionsModelChecker.CONST, constants);
        options.set("prism-flatten", true);
        // TODO why does this take so long?
        result = computeResult(options, String.format(ModelNamesPRISM.COIN_MODEL, 6), "P>=1 [ F (\"finished\") ]");
        assertEquals(true, result);
        close(options);
    }

    @Test
    public void philLss3GloballyTest() {
        Options options = prepareOptions();
        Value result;
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        Map<String,Object> constants = new HashMap<>();
        constants.put("K", "3");
        options.set(OptionsModelChecker.CONST, constants);

        //        result = computeResult(options, String.format(ModelNamesPRISM.PHIL_LSS_MODEL, 3), "Pmin=?[ F (\"entered\") ]");
        result = computeResult(options, String.format(ModelNamesPRISM.PHIL_LSS_MODEL, 3), "Pmin=?[ F (   ((p1>7) & (p1<13)) | ((p2>7) & (p2<13)) | ((p3>7) & (p3<13))      ) ]");
        assertEquals(0, result, tolerance);
        close(options);
    }

    @Test
    public void philLss4GloballyTest() {
        Options options = prepareOptions();
        Value result;
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        Map<String,Object> constants = new HashMap<>();
        constants.put("K", "3");
        options.set(OptionsModelChecker.CONST, constants);

        //        result = computeResult(options, String.format(ModelNamesPRISM.PHIL_LSS_MODEL, 4), "Pmin=?[ F (\"entered\") ]");
        result = computeResult(options, String.format(ModelNamesPRISM.PHIL_LSS_MODEL, 4), "Pmin=?[ F (   ((p1>7) & (p1<13)) | ((p2>7) & (p2<13)) | ((p3>7) & (p3<13)) | ((p4>7) & (p4<13))    ) ]");
        assertEquals(0, result, tolerance);
        close(options);
    }

    @Test
    // TODO ignored because "RP" not in PRISM file; 
    @Ignore
    public void clusterGTest() {
        Options options = prepareOptions();
        Map<String,Object> constants = new HashMap<>();
        Value result;
        double tolerance = 1E-13;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        options.set("prism-flatten", false);
        constants.put("N", "6");
        options.set(OptionsModelChecker.CONST, constants);

        result = computeResult(options, ModelNamesPRISM.CLUSTER_MODEL, "Pmax=? [ G(!\"RP\") ]");
        assertEquals(0, result, 1E-8);
        close(options);
    }

    @Test
    public void clusterBoundedUntilTest() {
        Options options = prepareOptions();
        Value result;
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        Map<String,Object> constants = new HashMap<>();
        constants.put("N", "1");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ModelNamesPRISM.CLUSTER_MODEL, "filter(min, P=? [ (\"minimum\") U<=0.5 (\"premium\")  ], \"minimum\");");
        assertEquals("0.007682184285169857", result, tolerance);
        close(options);
    }

    @Test
    public void hermanOpenIntervalTest() {
        Value result;
        Options options = prepareOptions();
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        double tolerance = 1E-10;

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 7), "filter(state, P=? [ G<6 !\"stable\" ], (x1=0)&(x2=0)&(x3=0)&(x4=0)&(x5=0)&(x6=0)&(x7=0))");
        assertEquals("0.35819912049919367", result, tolerance);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 7), "filter(state, P=? [ G<=6 !\"stable\" ], (x1=0)&(x2=0)&(x3=0)&(x4=0)&(x5=0)&(x6=0)&(x7=0))");
        assertEquals("0.28856343032384757", result, tolerance);

        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 7), "filter(state, P=? [ G<7 !\"stable\" ], (x1=0)&(x2=0)&(x3=0)&(x4=0)&(x5=0)&(x6=0)&(x7=0))");
        assertEquals("0.28856343032384757", result, tolerance);
    }

    @Test
    public void twoDiceTest() {
        Options options = prepareOptions();
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        double tolerance = 1E-6;
        //        options.set(OptionsEPMC.MDP_ENCODING_MODE, OptionsTypesEPMC.MDPEncoding.STATE);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        Value result1 = computeResult(options, ModelNamesPRISM.TWO_DICE_MODEL, "Pmin=? [ F s1=7 & s2=7 & d1+d2=2 ]");
        assertEquals("1/36", result1, tolerance);
        close(options);
    }

    @Test
    public void ddTest() {
        Options options = prepareOptions();
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        TypeInteger piecePositionType = TypeInteger.get(-2, 15);
        VariableDD player1Pos3 = ContextDD.get().newVariable("player1Pos3", piecePositionType, 1);
        VariableDD player2Pos3 = ContextDD.get().newVariable("player1Pos3", piecePositionType, 1);
        VariableDD player1Pos2 = ContextDD.get().newVariable("player1Pos2", piecePositionType, 1);
        VariableDD player2Pos2 = ContextDD.get().newVariable("player1Pos2", piecePositionType, 1);
        VariableDD player1Pos1 = ContextDD.get().newVariable("player1Pos1", piecePositionType, 1);
        VariableDD player2Pos1 = ContextDD.get().newVariable("player1Pos1", piecePositionType, 1);
        VariableDD turn = ContextDD.get().newBoolean("turn", 1);
        DD player2Pos3Enc = player2Pos3.getValueEncoding(0);
        DD player2Pos2Enc = player2Pos2.getValueEncoding(0);
        DD player2Pos1Enc = player2Pos1.getValueEncoding(0);
        DD player1Pos3Enc = player1Pos3.getValueEncoding(0);
        DD player1Pos2Enc = player1Pos2.getValueEncoding(0);
        DD player1Pos1Enc = player1Pos1.getValueEncoding(0);
        DD turnEnc = turn.getValueEncoding(0);
        DD support = ContextDD.get().newConstant(true);
        support = support.andWith(player1Pos1.newCube(0));
        support = support.andWith(player1Pos2.newCube(0));
        support = support.andWith(player1Pos3.newCube(0));
        support = support.andWith(player2Pos1.newCube(0));
        support = support.andWith(player2Pos2.newCube(0));
        support = support.andWith(player2Pos3.newCube(0));
        support = support.andWith(turn.newCube(0));

        DD valid = ContextDD.get().newConstant(true);

        valid = valid.andWith(player1Pos1Enc.le(ContextDD.get().newConstant(15)));
        valid = valid.andWith(player1Pos2Enc.le(ContextDD.get().newConstant(15)));
        valid = valid.andWith(player1Pos3Enc.le(ContextDD.get().newConstant(15)));

        valid = valid.andWith(player1Pos1Enc.le(player1Pos2Enc));
        valid = valid.andWith(player1Pos2Enc.le(player1Pos3Enc));

        valid = valid.andWith(player1Pos1Enc.clone().geWith(ContextDD.get().newConstant(0))
                .impliesWith(player1Pos1Enc.ne(player1Pos2Enc)));
        valid = valid.andWith(player1Pos2Enc.clone().geWith(ContextDD.get().newConstant(0))
                .impliesWith(player1Pos2Enc.ne(player1Pos3Enc)));

        valid = valid.andWith(player2Pos1Enc.le(ContextDD.get().newConstant(15)));
        valid = valid.andWith(player2Pos2Enc.le(ContextDD.get().newConstant(15)));
        valid = valid.andWith(player2Pos3Enc.le(ContextDD.get().newConstant(15)));
        valid = valid.andWith(player2Pos1Enc.le(player2Pos2Enc));
        valid = valid.andWith(player2Pos2Enc.le(player2Pos3Enc));
        valid = valid.andWith(player2Pos1Enc.clone().geWith(ContextDD.get().newConstant(0))
                .impliesWith(player2Pos1Enc.ne(player2Pos2Enc)));
        valid = valid.andWith(player2Pos2Enc.clone().geWith(ContextDD.get().newConstant(0))
                .impliesWith(player2Pos2Enc.ne(player2Pos3Enc)));

        valid = valid.andWith(player1Pos1Enc.ge(ContextDD.get().newConstant(0)).implies(player1Pos1Enc.ne(player2Pos1Enc)));
        valid = valid.andWith(player1Pos1Enc.ge(ContextDD.get().newConstant(0)).implies(player1Pos1Enc.ne(player2Pos2Enc)));
        valid = valid.andWith(player1Pos1Enc.ge(ContextDD.get().newConstant(0)).implies(player1Pos1Enc.ne(player2Pos3Enc)));

        valid = valid.andWith(player1Pos2Enc.ge(ContextDD.get().newConstant(0)).implies(player1Pos2Enc.ne(player2Pos1Enc)));
        valid = valid.andWith(player1Pos2Enc.ge(ContextDD.get().newConstant(0)).implies(player1Pos2Enc.ne(player2Pos2Enc)));
        valid = valid.andWith(player1Pos2Enc.ge(ContextDD.get().newConstant(0)).implies(player1Pos2Enc.ne(player2Pos3Enc)));

        valid = valid.andWith(player1Pos3Enc.ge(ContextDD.get().newConstant(0)).implies(player1Pos3Enc.ne(player2Pos1Enc)));
        valid = valid.andWith(player1Pos3Enc.ge(ContextDD.get().newConstant(0)).implies(player1Pos3Enc.ne(player2Pos2Enc)));
        valid = valid.andWith(player1Pos3Enc.ge(ContextDD.get().newConstant(0)).implies(player1Pos3Enc.ne(player2Pos3Enc)));

        valid = valid.andWith(
                (player1Pos1Enc.eq(ContextDD.get().newConstant(-2))
                        .andWith(player1Pos2Enc.eq(ContextDD.get().newConstant(-2)))
                        .andWith(player1Pos3Enc.eq(ContextDD.get().newConstant(-2))))
                .implies(turnEnc.eq(ContextDD.get().newConstant(false))));

        valid = valid.andWith(
                (player2Pos1Enc.eq(ContextDD.get().newConstant(-2))
                        .andWith(player2Pos2Enc.eq(ContextDD.get().newConstant(-2)))
                        .andWith(player2Pos3Enc.eq(ContextDD.get().newConstant(-2))))
                .implies(turnEnc.eq(ContextDD.get().newConstant(true))));

        ContextDD.get().reorder();
        System.out.println(valid.countSat(support));
        System.out.println(valid.countNodes());
        close(options);
    }
    
    /**
     * Test case by Andrea Turrini.
     * Tests correctness of PCTL X operator.
     */
    // TODO PCTL next operator currently does not for for DD engine
    @Ignore
    @Test
    public void pctlRecognition() {
        Options options = prepareOptions();
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        options.set(TestHelper.PRISM_FLATTEN, true);
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
