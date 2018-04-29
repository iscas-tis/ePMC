package epmc.imdp.bio;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.modelchecker.ExploreStatistics;
import epmc.modelchecker.Model;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;

import static epmc.graph.TestHelperGraph.*;
import static epmc.modelchecker.TestHelper.*;

public final class ExploreTest {
    /**
     * Set up the tests.
     */
    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void andrea1Test() {
        Options options = UtilTestBio.prepareBioOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelBio.IDENTIFIER);
        Model model = loadModel(options, ModelNames.TEST_ANDREA_1);
        ExploreStatistics statistics = exploreModel(model);
        System.out.println(statistics);
        System.out.println(exploreModelGraph(model));
    }
}
