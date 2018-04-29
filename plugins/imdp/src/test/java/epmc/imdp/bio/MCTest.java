package epmc.imdp.bio;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.value.Value;

import static epmc.modelchecker.TestHelper.*;

public final class MCTest {
    /**
     * Set up the tests.
     */
    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void andrea1FilterTest() {
        Options options = UtilTestBio.prepareBioOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelBio.IDENTIFIER);
        Value result;
//        result = computeResult(options, ModelNames.TEST_ANDREA_1, "filter(sum, ambiguous ? 1 : 0)");
//        result = computeResult(options, ModelNames.TEST_ANDREA_1, "Smax=? [ ambiguous ]");
//      result = computeResult(options, ModelNames.TEST_ANDREA_1, "Smin=? [ unsafe ]");
        result = computeResult(options, ModelNames.TEST_ANDREA_1, "multi(Smin=? [ unsafe ], S<=0.001 [ ambiguous ])");
        System.out.println(result);
    }
}
