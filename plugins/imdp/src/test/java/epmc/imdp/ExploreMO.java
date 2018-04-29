package epmc.imdp;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.imdp.model.ModelIMDP;
import epmc.modelchecker.ExploreStatistics;
import epmc.modelchecker.Model;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.prism.model.convert.OptionsPRISMConverter;

import static epmc.graph.TestHelperGraph.*;
import static epmc.modelchecker.TestHelper.*;

public final class ExploreMO {
    /**
     * Set up the tests.
     */
    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void exploreMOTest() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDP.IDENTIFIER);
        options.set(OptionsPRISMConverter.PRISM_CONVERTER_REWARD_METHOD, "external");
        Model model = null;
        model = loadModel(options, "/Users/emhahn/robot_IMDP.prism");
        ExploreStatistics statistics = exploreModel(model);
        System.out.println(statistics);
        System.out.println(exploreModelGraph(model));
    }
}
