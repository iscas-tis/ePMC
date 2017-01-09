package epmc.propertysolvercoalition;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import epmc.coalition.options.OptionsCoalition;
import epmc.error.EPMCException;
import epmc.main.options.UtilOptionsEPMC;
import epmc.modelchecker.EngineDD;
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
 * Class containing the JUnit tests for the qualitative coalition solver.
 * 
 * @author Ernst Moritz Hahn
 */
public final class QualitativeTest {
	private final static String USER_DIR = TestHelper.USER_DIR;
	private final static String TARGET_CLASSES = "/target/classes/";
    private final static String PLUGIN_DIR = System.getProperty(USER_DIR) + TARGET_CLASSES;

    @BeforeClass
    public static void initialise() {
        prepare();
    }

    private final static Options prepareCoalitionOptions() throws EPMCException {
        Options options = UtilOptionsEPMC.newOptions();
        options.set(OptionsPlugin.PLUGIN, PLUGIN_DIR);
        prepareOptions(options, LogType.TRANSLATE, TestHelper.MODEL_INPUT_TYPE_PRISM);
        return options;
    }
    
    @Test
    public void twoInvestorsTest() throws EPMCException {
        Options options = prepareCoalitionOptions();
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
//        options.set(OptionsCoalition.COALITION_SOLVER, "gadget");
//        options.set(OptionsModelChecker.ENGINE, EngineDD.class);
//        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Value result;

        result = computeResult(options, TWO_INVESTORS, "<<investor1>> P>=1 [ (G(!\"done1\")) & (F (((v>=5)))) ]");
        assertEquals(true, result);
        
        result = computeResult(options, TWO_INVESTORS, "<<investor1>> P>=1 [ (G(!\"done1\")) & (G (F((v>=5)))) ]");
        assertEquals(false, result);
        
        result = computeResult(options, TWO_INVESTORS, "<<investor1>> P>=1 [ (G(!\"done1\")) & (F (G((v>=5)))) ]");
        assertEquals(false, result);

        result = computeResult(options, TWO_INVESTORS, "<<investor1>> P>0 [ (G(!\"done1\")) & (F (G((v>=5)))) ]");
        assertEquals(true, result);

        result = computeResult(options, TWO_INVESTORS, "<<investor1,investor2,market>> P>=1 [ (G(!\"done1\")) & (F (G((v>=5)))) ]");
        assertEquals(false, result);
    }
    
    @Ignore
    @Test
    public void smallTest() throws EPMCException {
        Options options = prepareCoalitionOptions();
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
//        options.set(OptionsCoalition.COALITION_SOLVER, "gadget");
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);
//        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Map<String,String> constants = new HashMap<>();
        Value result;

        constants.put("size", "30");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (F (\"z1\"))  ]");
        assertEquals(true, result);
    }
    
    @Test
    public void robotReachabilityTest() throws EPMCException {
        Options options = prepareCoalitionOptions();
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        // options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Map<String,String> constants = new HashMap<>();
        Value result;

        constants.put("size", "6");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (F (\"z1\"))  ]");
        assertEquals(true, result);
        
        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (F (\"z1\"))  ]");
        assertEquals(true, result);
        
        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (F (\"z1\"))  ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (F (\"z1\")) & (F (\"z2\")) ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (F (\"z1\")) & (F (\"z2\")) ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (F (\"z1\")) & (F (\"z2\")) & (F (\"z3\")) ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (F (\"z1\")) & (F (\"z2\")) & (F (\"z3\")) ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (F (\"z1\")) & (F (\"z2\")) & (F (\"z3\")) & (F (\"z4\")) ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (F (\"z1\")) & (F (\"z2\")) & (F (\"z3\")) & (F (\"z4\")) ]");
        assertEquals(true, result);
        close(options);
    }

    @Test
    public void robotSmallRepeatedReachabilityTest() throws EPMCException {
        Options options = prepareCoalitionOptions();
//        options.set(Options.ENGINE, OptionsSet.Engine.DD);
//      options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsCoalition.COALITION_SOLVER, "gadget");
//        options.set(OptionsCoalition.COALITION_JURDZINSKY_CHOOSE_LIFT_NODES, OptionsCoalition.JurdzinskyChooseLiftNodes.ALL);
        Map<String,String> constants = new HashMap<>();
        Value result;

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (G(F (\"z1\")))  ]");
        assertEquals(true, result);
        
        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (G(F (\"z1\")))  ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (G(F (\"z1\"))) & (G(F (\"z2\"))) ]");
        assertEquals(true, result);
    }
    
    @Test
    public void robotRepeatedReachabilityTest() throws EPMCException {
        Options options = prepareCoalitionOptions();
//        options.set(Options.ENGINE, OptionsSet.Engine.DD);
      options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
//        options.set(Options.ENGINE, OptionsSet.Engine.EXPLICIT);
        Map<String,String> constants = new HashMap<>();
        Value result;

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (G(F (\"z1\")))  ]");
        assertEquals(true, result);
        
        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (G(F (\"z1\")))  ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (G(F (\"z1\"))) & (G(F (\"z2\"))) ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (G(F (\"z1\"))) & (G(F (\"z2\"))) ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (G(F (\"z1\"))) & (G(F (\"z2\"))) & (G(F (\"z3\"))) ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (G(F (\"z1\"))) & (G(F (\"z2\"))) & (G(F (\"z3\"))) ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (G(F (\"z1\"))) & (G(F (\"z2\"))) & (G(F (\"z3\"))) & (G(F (\"z4\"))) ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (G(F (\"z1\"))) & (G(F (\"z2\"))) & (G(F (\"z3\"))) & (G(F (\"z4\"))) ]");
        assertEquals(true, result);
        close(options);
    }
    
    @Test
    public void robotOrderedReachabilityTest() throws EPMCException {
        Options options = prepareCoalitionOptions();
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Map<String,Object> constants = new HashMap<>();
        Value result;

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ F (\"z1\" & (F(\"z2\")))  ]");
        assertEquals(true, result);
        
        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ F (\"z1\" & (F(\"z2\")))  ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (F (\"z1\" & (F(\"z2\" & (F(\"z4\"))))))  ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (F (\"z1\" & (F(\"z2\" & (F(\"z4\"))))))  ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ F (\"z1\" & (F(\"z2\" & (F(\"z4\" & (F(\"z3\")))))))  ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ F (\"z1\" & (F(\"z2\" & (F(\"z4\" & (F(\"z3\")))))))  ]");
        assertEquals(true, result);
        close(options);
    }
    
    @Ignore
    @Test
    public void robotRepeatedOrderedReachabilityTest() throws EPMCException {
        Options options = prepareCoalitionOptions();
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);
//        options.set(OptionsDD.DD_BINARY_ENGINE, "buddy");
        Map<String,String> constants = new HashMap<>();
        Value result;

        constants.put("size", "20");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ G(F (\"z1\" & (F(\"z2\"))))  ]");
        assertEquals(true, result);
        
        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ G(F (\"z1\" & (F(\"z2\"))))  ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ G((F (\"z1\" & (F(\"z2\" & (F(\"z4\")))))))  ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ G((F (\"z1\" & (F(\"z2\" & (F(\"z4\")))))))  ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ G(F (\"z1\" & (F(\"z2\" & (F(\"z4\" & (F(\"z3\"))))))))  ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ G(F (\"z1\" & (F(\"z2\" & (F(\"z4\" & (F(\"z3\"))))))))  ]");
        assertEquals(true, result);
        
        constants.put("size", "10");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ G(F (\"z1\" & (F(\"z2\" & (F(\"z4\" & (F(\"z3\"))))))))  ]");
        assertEquals(true, result);
        close(options);
    }

    
    @Ignore
    @Test
    public void robotSmallReachAvoidTest() throws EPMCException {
        Options options = prepareCoalitionOptions();
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(OptionsCoalition.COALITION_SOLVER, "gadget");
        options.set(OptionsCoalition.COALITION_SOLVER_NON_STOCHASTIC, "jurdzinski");
//        options.set(OptionsCoalition.COALITION_JURDZINSKY_CHOOSE_LIFT_NODES, OptionsCoalition.JurdzinskyChooseLiftNodes.ALL);

//        options.set(Options.DD_BINARY_ENGINE, OptionsSet.DdBinaryEngine.BUDDY);
//        options.set(Options.MDP_ENCODING_MODE, OptionsSet.MDPEncoding.STATE_DISTRIBUTION);
        Map<String,String> constants = new HashMap<>();

        Value result;

//        constants.put("size", "2");
  //      options.set(OptionsEPMC.CONST, constants);
    //    result = computeResult(options, ROBOTS_SMALL, "<<1>> P>=1 [ true U (\"z2\") ]");
      //  assertEquals(false, result);

        //if (true) {
        	//return;
       // }

        constants.put("size", "3");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS_SMALL, "<<1>> P>=1 [ true U (\"z2\") ]");
        assertEquals(false, result);

        if (true) {
        	return;
        }
        
        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS_SMALL, "<<1>> P>=1 [ (!\"z1\") U (\"z2\") ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS_SMALL, "<<1>> P>0 [ (!\"z1\") U (\"z2\") ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS_SMALL, "<<1>> P>0 [ (!\"z1\") U (\"z2\") ]");
        assertEquals(true, result);
    }

    @Test
    public void robotSmallModifiedReachAvoidTest() throws EPMCException {
        Options options = prepareCoalitionOptions();
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
//        options.set(OptionsCoalition.COALITION_SOLVER, "gadget");
//        options.set(OptionsCoalition.COALITION_SOLVER_NON_STOCHASTIC, "jurdzinski");
//        options.set(OptionsCoalition.COALITION_JURDZINSKY_LIFT_ORDER, "lifo");
//        options.set(OptionsCoalition.COALITION_JURDZINSKY_CHOOSE_LIFT_NODES, OptionsCoalition.JurdzinskyChooseLiftNodes.ALL);

//        options.set(Options.DD_BINARY_ENGINE, OptionsSet.DdBinaryEngine.BUDDY);
//        options.set(Options.MDP_ENCODING_MODE, OptionsSet.MDPEncoding.STATE_DISTRIBUTION);
        Map<String,String> constants = new HashMap<>();

        Value result;

//        constants.put("size", "3");
  //      options.set(OptionsEPMC.CONST, constants);
//        result = computeResult(options, ROBOTS_MODIFIED_SMALL, "<<1>> P>=1 [ true U (\"z2\") ]");
      //  assertEquals(false, result);

        //if (true) {
        	//return;
       // }

        constants.put("size", "6");
        options.set(OptionsModelChecker.CONST, constants);
//        result = computeResult(options, ROBOTS_MODIFIED_SMALL, "<<1>> P>=1 [ true U (\"z2\") ]");
        result = computeResult(options, ROBOTS_MODIFIED_MEDIUM, "<<1>> P>=1 [ (G(F(\"z1\"))) & (G(F(\"z2\"))) & (G(F(\"z4\"))) & (G(F(\"z3\"))) ]");

//        assertEquals(false, result);

        if (true) {
        	return;
        }
        
        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS_SMALL, "<<1>> P>=1 [ (!\"z1\") U (\"z2\") ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS_SMALL, "<<1>> P>0 [ (!\"z1\") U (\"z2\") ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS_SMALL, "<<1>> P>0 [ (!\"z1\") U (\"z2\") ]");
        assertEquals(true, result);
    }

    @Ignore
    @Test
    public void robotReachAvoidTest() throws EPMCException {
        Options options = prepareCoalitionOptions();
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
//        options.set(OptionsCoalition.COALITION_SOLVER, "gadget");

//        options.set(Options.DD_BINARY_ENGINE, OptionsSet.DdBinaryEngine.BUDDY);
//        options.set(Options.MDP_ENCODING_MODE, OptionsSet.MDPEncoding.STATE_DISTRIBUTION);
        Map<String,String> constants = new HashMap<>();

        Value result;

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (!\"z1\") U (\"z2\") ]");
        assertEquals(false, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (!\"z1\") U (\"z2\") ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>0 [ (!\"z1\") U (\"z2\") ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>0 [ (!\"z1\") U (\"z2\") ]");
        assertEquals(true, result);

        if (true) return;
        
        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1,2>> P>=1 [ (!\"z1\") U (\"z2\")  ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1,2>> P>=1 [ (!\"z1\") U (\"z2\")  ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ ((!\"z1\") U (\"z2\")) & ((!\"z4\") U (\"z2\")) ]");
        assertEquals(false, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ ((!\"z1\") U (\"z2\")) & ((!\"z4\") U (\"z2\")) ]");
        assertEquals(false, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ ((!\"z1\") U (\"z2\")) & ((!\"z4\") U (\"z2\")) & ((!\"z4\") U (\"z1\")) ]");
        assertEquals(false, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ ((!\"z1\") U (\"z2\")) & ((!\"z4\") U (\"z2\")) & ((!\"z4\") U (\"z1\")) ]");
        assertEquals(false, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ ((!\"z1\") U (\"z2\")) & ((!\"z4\") U (\"z2\")) & ((!\"z4\") U (\"z1\")) & (F(\"z4\")) ]");
        assertEquals(false, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ ((!\"z1\") U (\"z2\")) & ((!\"z4\") U (\"z2\")) & ((!\"z4\") U (\"z1\")) & (F(\"z4\")) ]");
        assertEquals(false, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>0 [ ((!\"z1\") U (\"z2\")) & ((!\"z4\") U (\"z2\")) & ((!\"z4\") U (\"z1\")) & (F(\"z4\")) ]");
        assertEquals(false, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>0 [ ((!\"z1\") U (\"z2\")) & ((!\"z4\") U (\"z2\")) & ((!\"z4\") U (\"z1\")) & (F(\"z4\")) ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1,2>> P>=1 [ ((!\"z1\") U (\"z2\")) & ((!\"z4\") U (\"z2\")) ]");
        assertEquals(false, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1,2>> P>=1 [ ((!\"z1\") U (\"z2\")) & ((!\"z4\") U (\"z2\")) ]");
        assertEquals(true, result);
        close(options);
    }
}
