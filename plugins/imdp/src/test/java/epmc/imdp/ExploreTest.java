package epmc.imdp;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.imdp.model.ModelIMDP;
import epmc.modelchecker.ExploreStatistics;
import epmc.modelchecker.Model;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;

import static epmc.graph.TestHelperGraph.*;
import static epmc.imdp.ModelNames.*;
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
    public void toyTest() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDP.IDENTIFIER);
        Model model = null;
        model = loadModel(options, TOY);
        ExploreStatistics statistics = exploreModel(model);
        System.out.println(statistics);
        System.out.println(exploreModelGraph(model));
    }

    @Test
    public void coinTest() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDP.IDENTIFIER);
        Model model = null;
        model = loadModel(options, String.format(COIN, 2));
        ExploreStatistics statistics;
        statistics = exploreModel(model);
        System.out.println(statistics);
        System.out.println(exploreModelGraph(model));

        model = loadModel(options, String.format(COIN, 4));
        statistics = exploreModel(model);
        System.out.println(statistics);
        //        System.out.println(exploreModelGraph(options, model));
    }

    @Test
    public void zeroconfTest() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDP.IDENTIFIER);
        Model model = null;
        model = loadModel(options, String.format(ZEROCONF_DL, 100));
        ExploreStatistics statistics = exploreModel(model);
        System.out.println(statistics);
        //        System.out.println(exploreModelGraph(options, model));
    }

}
