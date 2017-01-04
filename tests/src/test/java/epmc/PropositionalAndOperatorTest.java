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

public class PropositionalAndOperatorTest {
    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void operatorTest() throws EPMCException {
        Options options = prepareOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Value result;
        double tolerance = 1E-10;
        Map<String,Object> constants = new HashMap<>();
        constants.put("N", "10");
        constants.put("M", "2");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, CHAIN, "(filter(sum,s,true)+2)*3");
        assertEquals(501, result, tolerance);
        result = computeResult(options, CHAIN, "(filter(sum,s,true)+2)*3*2");
        assertEquals(1002, result, tolerance);
        result = computeResult(options, CHAIN, "(filter(sum,s,true)+2)*(3*2)");
        assertEquals(1002, result, tolerance);
        result = computeResult(options, CHAIN,
                "(filter(sum,s,true)+2)*(3*2) = 1002");
        assertEquals(true, result);
        result = computeResult(options, CHAIN,
                "(filter(sum,s,true)+2)*(3*2) != 1002");
        assertEquals(false, result);
        close(options);
    }
}
