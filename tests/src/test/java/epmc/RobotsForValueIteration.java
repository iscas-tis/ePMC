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
import org.junit.Test;

import epmc.error.EPMCException;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.value.Value;

public class RobotsForValueIteration {
    private final static String GRAPHSOLVER_ITERATIVE_NATIVE = "graphsolver-iterative-native";

    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void smallTest() throws EPMCException {
        Options options = prepareOptions();
        Value result;
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Map<String,Object> constants = new HashMap<>();

        options.set(GRAPHSOLVER_ITERATIVE_NATIVE, true);
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOT_REDUCED, "Pmax=? [ F ((!running) & robotAt = 0 & box0At = 0) ]");
        assertEquals(1, result, tolerance);

        options.set(GRAPHSOLVER_ITERATIVE_NATIVE, false);
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOT_REDUCED, "Pmax=? [ F ((!running) & robotAt = 0 & box0At = 0) ]");
        assertEquals(1, result, tolerance);
        
        close(options);
    }
    
    @Test
    public void mediumTest() throws EPMCException {
        Options options = prepareOptions();
        Value result;
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Map<String,Object> constants = new HashMap<>();

        options.set(GRAPHSOLVER_ITERATIVE_NATIVE, true);
        constants.put("N", "6");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOT_ONE_DIR, "Pmax=? [ F ((!running) & robotAt = 0 & box0At = 0 & box1At = 1) ]");
        assertEquals(1, result, tolerance);

        options.set(GRAPHSOLVER_ITERATIVE_NATIVE, false);
        constants.put("N", "6");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOT_ONE_DIR, "Pmax=? [ F ((!running) & robotAt = 0 & box0At = 0 & box1At = 1) ]");
        assertEquals(1, result, tolerance);

        options.set(GRAPHSOLVER_ITERATIVE_NATIVE, true);
        constants.put("N", "10");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOT_ONE_DIR, "Pmax=? [ F ((!running) & robotAt = 0 & box0At = 0 & box1At = 1) ]");
        assertEquals(1, result, tolerance);
        
        options.set(GRAPHSOLVER_ITERATIVE_NATIVE, false);
        constants.put("N", "10");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOT_ONE_DIR, "Pmax=? [ F ((!running) & robotAt = 0 & box0At = 0 & box1At = 1) ]");
        assertEquals(1, result, tolerance);
        
        close(options);
    }
}
