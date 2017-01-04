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
import epmc.modelchecker.EngineDD;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.value.Value;

public class FilterTest {

    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void chainTest() throws EPMCException {
        Options options = prepareOptions();
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        chainTestRun(options);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        chainTestRun(options);
        close(options);
    }

    private void chainTestRun(Options options) {
        Value result;
        double tolerance = 1E-10;
        Map<String,Object> constants = new HashMap<>();
        constants.put("N", "10");
        constants.put("M", "2");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, CHAIN, "filter(sum,s,true)");
        assertEquals(165, result, tolerance);

        constants.put("N", "10");
        constants.put("M", "1");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, CHAIN, "filter(sum,s*s,true)");
        assertEquals(770, result, tolerance);
        
        constants.put("N", "10");
        constants.put("M", "3");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, CHAIN, "filter(sum,s*s+s+a,true)");
        assertEquals(1826, result, tolerance);

        constants.put("N", "10");
        constants.put("M", "3");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, CHAIN, "filter(max,s*s+s+a,true)");
        assertEquals(113, result, tolerance);

        constants.put("N", "10");
        constants.put("M", "3");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, CHAIN, "filter(min,(s+1)*(s-1)+s+a,true)");
        assertEquals(-1, result, tolerance);
    }
}
