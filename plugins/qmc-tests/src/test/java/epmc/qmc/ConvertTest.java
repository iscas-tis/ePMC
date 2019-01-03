package epmc.qmc;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.jani.ConvertTestConfiguration;
import epmc.jani.ConvertTestStatistics;

import static epmc.modelchecker.TestHelper.*;
import static epmc.qmc.ModelNames.*;

// TODO
public final class ConvertTest {
    @BeforeClass
    public static void initialise() {
        prepare();
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
