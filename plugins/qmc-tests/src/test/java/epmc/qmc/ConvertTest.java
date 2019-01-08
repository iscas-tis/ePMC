package epmc.qmc;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.jani.ConvertTestConfiguration;
import epmc.jani.ConvertTestStatistics;
import epmc.main.options.UtilOptionsEPMC;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.plugin.OptionsPlugin;
import epmc.qmc.model.ModelPRISMQMC;
import epmc.qmc.model.PropertyPRISMQMC;

import static epmc.modelchecker.TestHelper.*;
import static epmc.qmc.ModelNames.*;

import java.util.ArrayList;
import java.util.List;

// TODO
public final class ConvertTest {
    @BeforeClass
    public static void initialise() {
        prepare();
    }

    private final static Options prepareQMCOptions() {
        List<String> qmcPlugins = new ArrayList<>();
        qmcPlugins.add(System.getProperty("user.dir") + "/../qmc/target/classes/");
        qmcPlugins.add(System.getProperty("user.dir") + "/../qmc-exporter/target/classes/");
        
        Options options = UtilOptionsEPMC.newOptions();
        options.set(OptionsPlugin.PLUGIN, qmcPlugins);
        prepareOptions(options, ModelPRISMQMC.IDENTIFIER);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISMQMC.IDENTIFIER);
        options.set(OptionsModelChecker.PROPERTY_INPUT_TYPE, PropertyPRISMQMC.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        return options;
    }

    @Test
    public void keyDistributionTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(KEY_DISTRIBUTION_MODEL)
                .setExploreAll()
                .run();
        System.out.println(statistics);
        //    	System.out.println(UtilModelParser.prettyString(statistics.getJaniModel()));
    }

    @Test
    public void loopTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(LOOP_MODEL)
                .setExploreAll()
                .run();
        System.out.println(statistics);
    }

    @Test
    public void superdenseCodingTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(SUPERDENSE_CODING_MODEL)
                .setExploreAll()
                .run();
        System.out.println(statistics);
    }
}
