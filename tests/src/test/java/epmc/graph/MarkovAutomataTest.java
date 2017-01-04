package epmc.graph;

import static epmc.ModelNamesOwn.*;
import static epmc.graph.TestHelperGraph.exploreModel;
import static epmc.modelchecker.TestHelper.prepare;
import static epmc.modelchecker.TestHelper.prepareOptions;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.error.EPMCException;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.ExploreStatistics;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;

public class MarkovAutomataTest {
    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void constructionSimpleSingleModuleTest() throws EPMCException {
        Options options = prepareOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        ExploreStatistics result;
        result = exploreModel(options, MA_SINGLEMODULE);
        System.out.println(result);
        result = exploreModel(options, MA_SINGLEMODULE_TWORATE);
        System.out.println(result);
        result = exploreModel(options, MA_TWOMODULES);
        System.out.println(result);
        result = exploreModel(options, MA_DISABLING_RATE);
        System.out.println(result);
    }
}
