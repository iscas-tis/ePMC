package epmc.imdp;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import epmc.graph.explicit.GraphExplicit;
import epmc.imdp.model.ModelIMDP;
import epmc.imdp.model.PropertyIMDP;
import epmc.imdp.options.OptionsIMDP;
import epmc.imdp.options.OptionsIMDPLump;
import epmc.lumping.TestHelperLump;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.Log;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;

import static epmc.imdp.ModelNames.*;
import static epmc.modelchecker.TestHelper.*;

public final class LumpTest {
    /**
     * Set up the tests.
     */
    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void toyTest() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        options.set(OptionsModelChecker.PROPERTY_INPUT_TYPE, PropertyIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        GraphExplicit graph = TestHelperLump.computeQuotient(options, TOY, "Pmax=?[(pc1=0 | pc1=3) U (pc1=2)]");
        //        System.out.println(graph);
    }

    @Test
    public void coin2Test() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        Log log = prepareLog(options, LogType.TRANSLATE);
        options.set(OptionsIMDP.IMDP_INTERVAL_PLAYER, IntervalPlayer.ANTAGONISTIC);
        //   options.set(OptionsIMDPLump.IMDP_LUMP_METHOD, "statewise");
        options.set(OptionsMessages.LOG, log);
        options.set(OptionsModelChecker.PROPERTY_INPUT_TYPE, PropertyIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");

        TestHelperLump.computeQuotient(options, String.format(COIN, 2), "Pmin=? [ F ((pc1=3 & pc2=3) & (coin1=0 & coin2=0)) ]");
        //        TestHelperLump.computeQuotient(options, String.format(COIN, 2), "Pmin=? [ F ((pc1=3 & pc2=1) & (coin1=0 & coin2=0)) ]");
    }

    @Test
    public void coin4Test() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        Log log = prepareLog(options, LogType.TRANSLATE);
        options.set(OptionsIMDP.IMDP_INTERVAL_PLAYER, IntervalPlayer.ANTAGONISTIC);
        options.set(OptionsMessages.LOG, log);
        options.set(OptionsModelChecker.PROPERTY_INPUT_TYPE, PropertyIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        TestHelperLump.computeQuotient(options, String.format(COIN, 4), "Pmin=? [ F ((pc1=3 & pc2=3 & pc3=3 & pc4=3) & (coin1=0 & coin2=0 & coin3=0 & coin4=0)) ]");
    }

    @Test
    public void coin5Test() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        Log log = prepareLog(options, LogType.TRANSLATE);
        options.set(OptionsMessages.LOG, log);
        options.set(OptionsModelChecker.PROPERTY_INPUT_TYPE, PropertyIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");

        TestHelperLump.computeQuotient(options, String.format(COIN, 5), "Pmin=? [ F ((pc1=3 & pc2=3 & pc3=3 & pc4=3 & pc5=3) & (coin1=0 & coin2=0 & coin3=0 & coin4=0 & coin5=0)) ]");
    }

    @Test
    public void coin6Test() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        Log log = prepareLog(options, LogType.TRANSLATE);
        options.set(OptionsMessages.LOG, log);
        options.set(OptionsModelChecker.PROPERTY_INPUT_TYPE, PropertyIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        TestHelperLump.computeQuotient(options, String.format(COIN, 6), "Pmin=? [ F ((pc1=3 & pc2=3 & pc3=3 & pc4=3 & pc5=3 & pc6=3) & (coin1=0 & coin2=0 & coin3=0 & coin4=0 & coin5=0 & coin6=0)) ]");
    }

    @Test
    public void zeroconf100Test() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        Log log = prepareLog(options, LogType.TRANSLATE);
        options.set(OptionsIMDP.IMDP_INTERVAL_PLAYER, IntervalPlayer.ANTAGONISTIC);
        options.set(OptionsMessages.LOG, log);
        options.set(OptionsModelChecker.PROPERTY_INPUT_TYPE, PropertyIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        TestHelperLump.computeQuotient(options, String.format(ZEROCONF_DL, 100), "Pmax=? [ (!(l=4 & ip=2)) U (t>=100) ]");
    }

    @Test
    public void zeroconf25Test() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        Log log = prepareLog(options, LogType.TRANSLATE);
        options.set(OptionsIMDP.IMDP_INTERVAL_PLAYER, IntervalPlayer.ANTAGONISTIC);
        options.set(OptionsMessages.LOG, log);
        options.set(OptionsModelChecker.PROPERTY_INPUT_TYPE, PropertyIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsIMDPLump.IMDP_SHORTCUT_SINGLE_ACTION_BEFORE_NORMALISATION, true);
        TestHelperLump.computeQuotient(options, String.format(ZEROCONF_DL, 25), "Pmax=? [ (!(l=4 & ip=2)) U (t>=25) ]");
    }

    @Test
    public void philNofair6E() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        Log log = prepareLog(options, LogType.TRANSLATE);
        options.set(OptionsMessages.LOG, log);
        options.set(OptionsModelChecker.PROPERTY_INPUT_TYPE, PropertyIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsIMDPLump.IMDP_SHORTCUT_SINGLE_ACTION_BEFORE_NORMALISATION, true);
        TestHelperLump.computeQuotient(options, String.format(PHIL_NOFAIR, 6), "Pmax=? [ (F (((p1>=8)&(p1<=9))|((p2>=8)&(p2<=9))|((p3>=8)&(p3<=9))|((p4>=8)&(p4<=9))|((p5>=8)&(p5<=9))|((p6>=8)&(p6<=9)))) ]");
    }

    @Ignore
    @Test
    public void philNofair7E() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        Log log = prepareLog(options, LogType.TRANSLATE);
        options.set(OptionsMessages.LOG, log);
        options.set(OptionsModelChecker.PROPERTY_INPUT_TYPE, PropertyIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsIMDPLump.IMDP_SHORTCUT_SINGLE_ACTION_BEFORE_NORMALISATION, true);
        TestHelperLump.computeQuotient(options, String.format(PHIL_NOFAIR, 7), "Pmax=? [ (F (((p1>=8)&(p1<=9))|((p2>=8)&(p2<=9))|((p3>=8)&(p3<=9))|((p4>=8)&(p4<=9))|((p5>=8)&(p5<=9))|((p6>=8)&(p6<=9))|((p7>=8)&(p7<=9)))) ]");
    }
    
    @Test
    public void coin4RewardsTest() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        Log log = prepareLog(options, LogType.TRANSLATE);
        options.set(OptionsIMDP.IMDP_INTERVAL_PLAYER, IntervalPlayer.ANTAGONISTIC);
        //   options.set(OptionsIMDPLump.IMDP_LUMP_METHOD, "statewise");
        options.set(OptionsMessages.LOG, log);
        options.set(OptionsModelChecker.PROPERTY_INPUT_TYPE, PropertyIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        TestHelperLump.computeQuotient(options, String.format(COIN, 4), "Pmin=? [ F \"finished\" ]");

//        TestHelperLump.computeQuotient(options, String.format(COIN, 4), "R{\"steps\"}min=? [ F \"finished\" ]");
        //        TestHelperLump.computeQuotient(options, String.format(COIN, 2), "Pmin=? [ F ((pc1=3 & pc2=1) & (coin1=0 & coin2=0)) ]");
    }
}
