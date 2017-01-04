package epmc.time;

import static epmc.ModelNamesPRISM.*;
import static epmc.modelchecker.TestHelper.*;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.error.EPMCException;
import epmc.jani.ConvertTestConfiguration;
import epmc.jani.ConvertTestStatistics;
import epmc.jani.model.UtilModelParser;

public final class PRISM2JANI {
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
	public void csmaAbstTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(PTA_CSMA_ABST_MODEL)
    			.run();
    	System.out.println(UtilModelParser.prettyString(statistics.getJaniModel()));
	}
    
    @Test
	public void csmaFullTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(PTA_CSMA_FULL_MODEL)
    			.run();
    	System.out.println(UtilModelParser.prettyString(statistics.getJaniModel()));
	}
    
    @Test
	public void firewireAbstTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(PTA_FIREWIRE_ABST_MODEL)
    			.run();
    	System.out.println(UtilModelParser.prettyString(statistics.getJaniModel()));
	}

    @Test
	public void firewireImplTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(PTA_FIREWIRE_IMPL_MODEL)
    			.run();
    	System.out.println(UtilModelParser.prettyString(statistics.getJaniModel()));
	}
    
    @Test
	public void repudiationHonestTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(PTA_REPUDIATION_HONEST_MODEL)
    			.run();
    	System.out.println(UtilModelParser.prettyString(statistics.getJaniModel()));
	}

    @Test
	public void repudiationMaliciousTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(PTA_REPUDIATION_MALICIOUS_MODEL)
    			.run();
    	System.out.println(UtilModelParser.prettyString(statistics.getJaniModel()));
	}

    @Test
	public void simpleTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(PTA_SIMPLE_MODEL)
    			.run();
    	System.out.println(UtilModelParser.prettyString(statistics.getJaniModel()));
	}

    @Test
	public void zeroconfTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(PTA_ZEROCONF_MODEL)
    			.run();
    	System.out.println(UtilModelParser.prettyString(statistics.getJaniModel()));
	}

}
