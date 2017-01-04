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
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.value.Value;

public class InitLabelTest {

    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void herman03Test() throws EPMCException {
        Options options = prepareOptions();
        Value result;
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        Map<String,Object> constants = new HashMap<>();

        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 3), "filter(forall, \"init\" => P>=1 [ F (\"stable\") ])");
        assertEquals(true, result);
        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 3), "filter(count, \"init\")");
        assertEquals(8, result, tolerance);
        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 3), "filter(count, \"deadlock\")");
        assertEquals(0, result, tolerance);
        close(options);
    }
}
