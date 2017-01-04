package epmc;

import static epmc.modelchecker.TestHelper.assertEquals;
import static epmc.modelchecker.TestHelper.close;
import static epmc.modelchecker.TestHelper.computeResult;
import static epmc.modelchecker.TestHelper.prepare;
import static epmc.modelchecker.TestHelper.prepareOptions;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.error.EPMCException;
import epmc.modelchecker.EngineDD;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.value.Value;

public class PCTLSolverDDTest {

    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void coin06UntilTest() throws EPMCException {
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
        result = computeResult(options, String.format(ModelNamesPRISM.COIN_MODEL, 6), "P>=1 [ F (\"finished\") ]");
        assertEquals(true, result);
        close(options);
    }

    @Test
    public void philLss3GloballyTest() throws EPMCException {
        Options options = prepareOptions();
        Value result;
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        Map<String,Object> constants = new HashMap<>();
        constants.put("K", "3");
        options.set(OptionsModelChecker.CONST, constants);

        result = computeResult(options, String.format(ModelNamesPRISM.PHIL_LSS_MODEL, 3), "Pmin=?[ F (\"entered\") ]");
        assertEquals(0, result, tolerance);
        close(options);
    }

    @Test
    public void philLss4GloballyTest() throws EPMCException {
        Options options = prepareOptions();
        Value result;
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        Map<String,Object> constants = new HashMap<>();
        constants.put("K", "3");
        options.set(OptionsModelChecker.CONST, constants);

        result = computeResult(options, String.format(ModelNamesPRISM.PHIL_LSS_MODEL, 4), "Pmin=?[ F (\"entered\") ]");
        assertEquals(0, result, tolerance);
        close(options);
    }

    @Test
    public void clusterGTest() throws EPMCException {
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
    public void clusterBoundedUntilTest() throws EPMCException {
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
    public void hermanOpenIntervalTest() throws EPMCException {
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
    public void twoDiceTest() throws EPMCException {
        Options options = prepareOptions();
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        double tolerance = 1E-6;
//        options.set(OptionsEPMC.MDP_ENCODING_MODE, OptionsTypesEPMC.MDPEncoding.STATE);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        Value result1 = computeResult(options, ModelNamesPRISM.TWO_DICE_MODEL, "Pmin=? [ F s1=7 & s2=7 & d1+d2=2 ]");
        assertEquals("1/36", result1, tolerance);
        close(options);
    }
}
