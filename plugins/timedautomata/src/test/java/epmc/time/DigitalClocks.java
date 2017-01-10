/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

*****************************************************************************/

package epmc.time;

import static epmc.ModelNamesPRISM.*;
import static epmc.graph.TestHelperGraph.*;
import static epmc.modelchecker.TestHelper.*;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.error.EPMCException;
import epmc.jani.ConvertTestConfiguration;
import epmc.jani.ConvertTestStatistics;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.time.DigitalClocksTransformer;

public final class DigitalClocks {
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
    	DigitalClocksTransformer transformer = new DigitalClocksTransformer();
    	ModelJANI model = statistics.getJaniModel();
    	transformer.setModel(model);
    	transformer.transform();
    	System.out.println(UtilModelParser.prettyString(model));
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
    			.putConstant("delay", 13)
    			.setPrismFlatten(true)
    			.run();
    	DigitalClocksTransformer transformer = new DigitalClocksTransformer();
    	ModelJANI model = statistics.getJaniModel();
    	transformer.setModel(model);
    	transformer.transform();
    	System.out.println(exploreModel(model));

//    	System.out.println(UtilModelParser.prettyString(statistics.getJaniModel()));
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
    	DigitalClocksTransformer transformer = new DigitalClocksTransformer();
    	ModelJANI model = statistics.getJaniModel();
    	transformer.setModel(model);
    	transformer.transform();
    	System.out.println(exploreModel(model));
//    	System.out.println(UtilModelParser.prettyString(model));
    	System.out.println(UtilModelParser.prettyString(model));
	}

    @Test
	public void repudiationMaliciousTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(PTA_REPUDIATION_MALICIOUS_MODEL)
    			.setPrismFlatten(false)
    			.run();
    	DigitalClocksTransformer transformer = new DigitalClocksTransformer();
    	ModelJANI model = statistics.getJaniModel();
    	transformer.setModel(model);
    	transformer.transform();
    	System.out.println(exploreModel(model));
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
    	DigitalClocksTransformer transformer = new DigitalClocksTransformer();
    	ModelJANI model = statistics.getJaniModel();
    	transformer.setModel(model);
    	transformer.transform();
    	System.out.println(exploreModel(model));
//    	System.out.println(UtilModelParser.prettyString(model));
	}

}
