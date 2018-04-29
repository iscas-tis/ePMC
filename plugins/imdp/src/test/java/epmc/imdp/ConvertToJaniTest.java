package epmc.imdp;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.graph.CommonProperties;
import epmc.imdp.model.ModelIMDP;
import epmc.jani.ConvertTestConfiguration;
import epmc.jani.ConvertTestStatistics;
import epmc.jani.model.UtilModelParser;
import epmc.modelchecker.ExploreStatistics;
import epmc.modelchecker.Model;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;

import static epmc.graph.TestHelperGraph.exploreModel;
import static epmc.imdp.ModelNames.*;
import static epmc.imdp.ModelNamesPaperExperiments.*;
import static epmc.modelchecker.TestHelper.*;

import java.util.HashSet;
import java.util.Set;

public final class ConvertToJaniTest {
    /**
     * Set up the tests.
     */
    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void toyTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(TOY)
                .setExploreAll()
                .run();
        //    	System.out.println(statistics);
        System.out.println(UtilModelParser.prettyString(statistics.getJaniModel()));
    }

    @Test
    public void coinTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(String.format(COIN, 4))
                .setExploreAll()
                .run();
        System.out.println(statistics);
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

    @Test
    public void crowdsIntTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(String.format(CROWDSINT, 50, 4))
                .putConstant("err", 0.2)
                .setExploreAll()
                .setPrismFlatten(true)
                //    			.setExploreJANI()
                .run();
        Set<Object> properties = new HashSet<>();
        properties.add(CommonProperties.STATE);
        //    	System.out.println(exploreToGraph(statistics.getJaniModel(), properties));
    }

}
