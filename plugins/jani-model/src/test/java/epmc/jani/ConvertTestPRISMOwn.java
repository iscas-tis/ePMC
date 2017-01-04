package epmc.jani;

import static epmc.modelchecker.TestHelper.*;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.ModelNamesOwn;
import epmc.error.EPMCException;

public final class ConvertTestPRISMOwn {
	/** Location of plugin directory in file system. */
    final static String PLUGIN_DIR = System.getProperty("user.dir") + "/target/classes/";

    /**
     * Set up the tests.
     */
    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void zeroconfSimpleTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(ModelNamesOwn.ZEROCONF_SIMPLE)
    			.putConstant("n", "10")
    			.setExploreAll()
    			.run();
    	System.out.println(statistics);
    }
}
