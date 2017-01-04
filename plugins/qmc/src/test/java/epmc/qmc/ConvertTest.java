package epmc.qmc;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.error.EPMCException;
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
    public void keyDistributionTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(KEY_DISTRIBUTION)
    			.setExploreAll()
    			.run();
    	System.out.println(statistics);
//    	System.out.println(UtilModelParser.prettyString(statistics.getJaniModel()));
    }
    
    @Test
    public void loopTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(LOOP)
    			.setExploreAll()
    			.run();
    	System.out.println(statistics);
    }
    
    @Test
    public void superdenseCodingTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(SUPERDENSE_CODING)
    			.setExploreAll()
    			.run();
    	System.out.println(statistics);
    }
}
