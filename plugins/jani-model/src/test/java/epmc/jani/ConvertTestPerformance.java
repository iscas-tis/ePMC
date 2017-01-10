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

package epmc.jani;

import static epmc.ModelNamesPRISM.*;
import static epmc.modelchecker.TestHelper.*;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.error.EPMCException;

/**
 * Performance tests to compare old and new state space exploration.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ConvertTestPerformance {
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
	public void brpTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(BRP_MODEL)
    			.putConstant("N", "200")
    			.putConstant("MAX", "512")
    			.setExploreAll()
    			.run();
//    	System.out.println(UtilModelParser.prettyString(statistics.getJaniModel()));
    	System.out.println(statistics);
	}

	@Test
    public void cellTest() throws EPMCException {
		ConvertTestStatistics statistics = new ConvertTestConfiguration()
				.setModelName(CELL_MODEL)
				.putConstant("N", "100")
				.setExploreAll()
				.run();
		System.out.println(statistics);
    }

    @Test
    public void clusterTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(CLUSTER_MODEL)
    			.putConstant("N", "256")
    			.putConstant("MAX", "512")
    			.setPrismFlatten(true)
    			.setExploreAll()
    			.run();
		System.out.println(statistics);
    }

    @Test
    public void coinTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(String.format(COIN_MODEL, 6))
    			.putConstant("K", "10")
    			.setExploreJANI()
    			.run();
		System.out.println(statistics);
    }

    @Test
    public void csmaTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(String.format(CSMA_MODEL, 3, 4))
    			.setExploreAll()
    			.run();
		System.out.println(statistics);
    }

    @Test
    public void diningCryptTest() throws EPMCException {
    	for (int num = 3; num < 9; num++) {
    		ConvertTestStatistics statistics = new ConvertTestConfiguration()
    				.setModelName(String.format(DINING_CRYPT_MODEL, num))
    				.setExploreAll()
//    				.setPrismFlatten(true)
    				.run();
    		System.out.println(statistics);
    	}
    }
    
    @Test
    public void embeddedTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(EMBEDDED_MODEL)
    			.putConstant("MAX_COUNT", 4)
    			.setExploreAll()
    			.run();
		System.out.println(statistics);
    }
    
    @Test
    public void firewireAbstTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(FIREWIRE_ABST_MODEL)
    			.putConstant("delay", "10")
    			.putConstant("fast", "0.1")
    			.setExploreAll()
	//			.setPrintJANI(true)
    			.run();
		System.out.println(statistics);
	    }

	@Test
    public void firewireImplTest() throws EPMCException {
		ConvertTestStatistics statistics = new ConvertTestConfiguration()
				.setModelName(FIREWIRE_IMPL_MODEL)
				.putConstant("delay", "10")
				.putConstant("fast", "0.4")
				.setExploreAll()
				.run();
		System.out.println(statistics);
    }
    
    @Test
    public void fmsTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.putConstant("n", 4)
    			.setModelName(FMS_MODEL)
    			.setExploreAll()
    			.run();
		System.out.println(statistics);
    }
    
    @Test
    public void kanbanTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.putConstant("t", 4)
    			.setModelName(KANBAN_MODEL)
    			.setExploreAll()
    			.run();
		System.out.println(statistics);
    }
    
    @Test
    public void asyncLeaderTest() throws EPMCException {
    	ConvertTestStatistics statistics1 = new ConvertTestConfiguration()
    			.setModelName(String.format(LEADER_ASYNC_MODEL, 3))
    			.setExploreAll()
    			.run();
		System.out.println(statistics1);
    	
    	ConvertTestStatistics statistics2 = new ConvertTestConfiguration()
    			.setModelName(String.format(LEADER_ASYNC_MODEL, 4))
    			.setExploreAll()
    			.run();
		System.out.println(statistics2);
    }

    @Test
    public void syncLeaderTest() throws EPMCException {
    	ConvertTestStatistics statistics1 = new ConvertTestConfiguration()
    			.setModelName(String.format(LEADER_SYNC_MODEL, 3, 3))
    			.setExploreAll()
    			.run();
		System.out.println(statistics1);
    	
    	ConvertTestStatistics statistics2 = new ConvertTestConfiguration()
    			.setModelName(String.format(LEADER_SYNC_MODEL, 3, 4))
    			.setExploreAll()
    			.run();
		System.out.println(statistics2);
    }

    @Test
    public void knaclTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(KNACL_MODEL)
    			.putConstant("N1", "5")
    			.putConstant("N2", "5")
    			.putConstant("N3", "5")
    			.setExploreAll()
    			.run();
		System.out.println(statistics);
    }

    @Test
    public void naclTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(NACL_MODEL)
    			.putConstant("N1", "10")
    			.putConstant("N2", "10")
    			.setExploreAll()
    			.run();
		System.out.println(statistics);
    }
    
    @Test
    public void mcTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(MC_MODEL)
    			.putConstant("N1", "10")
    			.putConstant("N2", "10")
    			.setExploreAll()
    			.run();
		System.out.println(statistics);
    }
    
    @Test
    public void mutualTest() throws EPMCException {
    	for (int i = 3; i < 6; i++) {
    		ConvertTestStatistics statistics = new ConvertTestConfiguration()
    				.setModelName(String.format(MUTUAL_MODEL, i))
    				.setPrismFlatten(false)
    				.setExploreAll()
    				.run();
    		System.out.println(statistics);
    	}
    }
    
    @Test
    public void peer2peerTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(String.format(PEER2PEER_MODEL, 4, 4))
    			.setExploreAll()
    			.run();
		System.out.println(statistics);
    }
    
    @Test
    public void philTest() throws EPMCException {
    	ConvertTestStatistics statistics1 = new ConvertTestConfiguration()
    			.setModelName(String.format(PHIL_MODEL, 3))
    			.setExploreAll()
    			.run();
		System.out.println(statistics1);
    	
    	ConvertTestStatistics statistics2 = new ConvertTestConfiguration()
    			.setModelName(String.format(PHIL_MODEL, 4))
    			.setExploreAll()
    			.run();
		System.out.println(statistics2);
    }
    
    @Test
    public void philNoFairTest() throws EPMCException {
    	ConvertTestStatistics statistics1 = new ConvertTestConfiguration()
    			.setModelName(String.format(PHIL_NOFAIR_MODEL, 3))
    			.setExploreAll()
    			.run();
    	System.out.println(statistics1);
    
    	ConvertTestStatistics statistics2 = new ConvertTestConfiguration()
    			.setModelName(String.format(PHIL_NOFAIR_MODEL, 4))
    			.setExploreAll()
    			.run();
    	System.out.println(statistics2);
    }
    
    @Test
    public void philLssTest() throws EPMCException {
    	for (int K = 4; K < 5; K++) {
    		ConvertTestStatistics statistics = new ConvertTestConfiguration()
    				.setModelName(String.format(PHIL_LSS_MODEL, 4))
    				.putConstant("K", K)
    				.setExploreAll()
    				.run();
    		System.out.println(statistics);
    	}
    }
    
    @Test
    public void pollingTest() throws EPMCException {
    	ConvertTestStatistics statistics1 = new ConvertTestConfiguration()
    			.setModelName(String.format(POLLING_MODEL, 3))
    			.setExploreAll()
    			.run();
    	System.out.println(statistics1);
    	
    	ConvertTestStatistics statistics2 = new ConvertTestConfiguration()
    			.setModelName(String.format(POLLING_MODEL, 4))
    			.setExploreAll()
    			.run();
    	System.out.println(statistics2);
    }
    
    @Test
    public void rabinTest() throws EPMCException {
    	ConvertTestStatistics statistics1 = new ConvertTestConfiguration()
    			.setModelName(String.format(RABIN_MODEL, 3))
    			.setExploreAll()
    			.run();
    	System.out.println(statistics1);
    	
    	ConvertTestStatistics statistics2 = new ConvertTestConfiguration()
    			.setModelName(String.format(RABIN_MODEL, 4))
    			.setExploreAll()
    			.run();
    	System.out.println(statistics2);
    }
    
    @Test
    public void beauquierTest() throws EPMCException {
    	ConvertTestStatistics statistics1 = new ConvertTestConfiguration()
    			.setModelName(String.format(BEAUQUIER_MODEL, 3))
    			.setExploreAll()
    			.run();
    	System.out.println(statistics1);
    	
    	ConvertTestStatistics statistics2 = new ConvertTestConfiguration()
    			.setModelName(String.format(BEAUQUIER_MODEL, 5))
    			.setExploreAll()
    			.run();
    	System.out.println(statistics2);
    }
    
    @Test
    public void hermanTest() throws EPMCException {
    	ConvertTestStatistics statistics1 = new ConvertTestConfiguration()
    			.setModelName(String.format(HERMAN_MODEL, 3))
    			.setExploreAll()
    			.run();
    	System.out.println(statistics1);
    	
    	ConvertTestStatistics statistics2 = new ConvertTestConfiguration()
    			.setModelName(String.format(HERMAN_MODEL, 5))
    			.setExploreAll()
    			.run();
    	System.out.println(statistics2);
    }
    
    @Test
    public void ijTest() throws EPMCException {
    	ConvertTestStatistics statistics1 = new ConvertTestConfiguration()
    			.setModelName(String.format(IJ_MODEL, 3))
    			.setExploreAll()
    			.run();
    	System.out.println(statistics1);
    	
    	ConvertTestStatistics statistics2 = new ConvertTestConfiguration()
    			.setModelName(String.format(IJ_MODEL, 5))
    			.setExploreAll()
    			.run();
    	System.out.println(statistics2);
    }

    @Test
    public void tandemTest() throws EPMCException {
    	ConvertTestStatistics statistics1 = new ConvertTestConfiguration()
    			.setModelName(TANDEM_MODEL)
    			.putConstant("c", "3")
    			.setExploreAll()
    			.run();
    	System.out.println(statistics1);
    	
    	ConvertTestStatistics statistics2 = new ConvertTestConfiguration()
    			.setModelName(TANDEM_MODEL)
    			.putConstant("c", "4")
    			.setExploreAll()
    			.run();
    	System.out.println(statistics2);
    }
    
    @Test
    public void wlanTest() throws EPMCException {
    	ConvertTestStatistics statistics1 = new ConvertTestConfiguration()
    			.setModelName(String.format(WLAN_MODEL, 1))
    			.putConstant("TRANS_TIME_MAX", "10")
    			.setExploreAll()
    			.run();
    	System.out.println(statistics1);
    	
    	ConvertTestStatistics statistics2 = new ConvertTestConfiguration()
    			.setModelName(String.format(WLAN_MODEL, 2))
    			.putConstant("TRANS_TIME_MAX", "10")
    			.setExploreAll()
    			.run();
    	System.out.println(statistics2);
    }

    @Test
    public void wlanCollideTest() throws EPMCException {
    	ConvertTestStatistics statistics1 = new ConvertTestConfiguration()
    			.setModelName(String.format(WLAN_COLLIDE_MODEL, 1))
    			.putConstant("TRANS_TIME_MAX", "10")
    			.putConstant("COL", "3")
    			.setExploreAll()
    			.run();
    	System.out.println(statistics1);
    	
    	ConvertTestStatistics statistics2 = new ConvertTestConfiguration()
    			.setModelName(String.format(WLAN_COLLIDE_MODEL, 2))
    			.putConstant("TRANS_TIME_MAX", "10")
    			.putConstant("COL", "3")
    			.setExploreAll()
    			.run();
    	System.out.println(statistics2);
    }

    @Test
    public void wlanTimeBoundTest() throws EPMCException {
    	ConvertTestStatistics statistics1 = new ConvertTestConfiguration()
    			.setModelName(String.format(WLAN_TIME_BOUNDED_MODEL, 1))
    			.putConstant("TRANS_TIME_MAX", "10")
    			.putConstant("DEADLINE", "4")
    			.setExploreAll()
    			.run();
    	System.out.println(statistics1);
    	
    	ConvertTestStatistics statistics2 = new ConvertTestConfiguration()
    			.setModelName(String.format(WLAN_TIME_BOUNDED_MODEL, 2))
    			.putConstant("TRANS_TIME_MAX", "10")
    			.putConstant("DEADLINE", "4")
    			.setExploreAll()
    			.run();
    	System.out.println(statistics2);
    }

    @Test
    public void zeroconfTest() throws EPMCException {
    	ConvertTestStatistics statistics1 = new ConvertTestConfiguration()
    			.setModelName(ZEROCONF_MODEL)
    			.putConstant("reset", "false")
    			.putConstant("N", "3")
    			.putConstant("K", "2")
    			.putConstant("err", "0.2")
    			.setExploreAll()
    			.run();
    	System.out.println(statistics1);

    	ConvertTestStatistics statistics2 = new ConvertTestConfiguration()
    			.setModelName(ZEROCONF_MODEL)
    			.putConstant("reset", "false")
    			.putConstant("N", "4")
    			.putConstant("K", "3")
    			.putConstant("err", "0.2")
    			.setExploreAll()
    			.run();
    	System.out.println(statistics2);
    	
    	ConvertTestStatistics statistics3 = new ConvertTestConfiguration()
    			.setModelName(ZEROCONF_MODEL)
    			.putConstant("reset", "true")
    			.putConstant("N", "3")
    			.putConstant("K", "2")
    			.putConstant("err", "0.2")
    			.setExploreAll()
    			.run();
    	System.out.println(statistics3);

    	ConvertTestStatistics statistics4 = new ConvertTestConfiguration()
    			.setModelName(ZEROCONF_MODEL)
    			.putConstant("reset", "true")
    			.putConstant("N", "4")
    			.putConstant("K", "3")
    			.putConstant("err", "0.2")
    			.setExploreAll()
    			.run();
    	System.out.println(statistics4);
    }

    @Test
    public void zeroconfTimeBoundedTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(ZEROCONF_TIME_BOUNDED_MODEL)
    			.putConstant("reset", "false")
    			.putConstant("T", "30")
    			.putConstant("N", "8")
    			.putConstant("K", "10")
    			.putConstant("err", "0.2")
    			.setExploreAll()
    			.run();
    	System.out.println(statistics);
    }
    
    @Test
    public void byzantineTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(String.format(BYZANTINE_MODEL, 4, 1))
    			.setExploreJANI(true)
    			.setExploreJANICloned(true)
		// PRISM explorer fails due to some bug
//			.setExplorePRISM(true)
    			.run();
    	System.out.println(statistics);
    }

    @Test
    public void cyclinTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(CYCLIN_MODEL)
    			.putConstant("N", "4")
    			.setExploreAll()
    			.run();
    	System.out.println(statistics);
    }

    @Test
    public void fairExchangeTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(String.format(FAIR_EXCHANGE_MODEL, 10))
    			.setExploreAll()
    			.run();
    	System.out.println(statistics);
    }

    @Test
    public void gossipTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(String.format(GOSSIP_MODEL, 4))
    			.setExploreAll()
    			.run();
    	System.out.println(statistics);
    }

    @Test
    public void gossipDTMCTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(String.format(GOSSIP_DTMC_MODEL, 4))
    			.setExploreAll()
    			.run();
    	System.out.println(statistics);
    }

    @Test
    public void graphTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(String.format(GRAPH_MODEL, 4))
    			.putConstant("p", "0.2")
    			.setExploreAll()
    			.run();
    	System.out.println(statistics);
    }

    @Test
    public void kaminskyTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(KAMINSKY_MODEL)
    			.putConstant("TIMES_TO_REQUEST_URL", 10)
    			.putConstant("port_id", 5)
    			.putConstant("popularity", 5)
    			.putConstant("guess", 5)
    			.putConstant("other_legitimate_requests_rate", 3)
    			.setExploreAll()
    			.run();
    	System.out.println(statistics);
    }

    @Test
    public void mapkCascadeTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(MAPK_CASCADE_MODEL)
    			.putConstant("N", "4")
    			.setExploreAll()
    			.run();
    	System.out.println(statistics);
    }

    @Test
    public void nandTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(NAND_MODEL)
    			.putConstant("N", "3")
    			.putConstant("K", "4")
    			.setExploreAll()
    			.run();
    	System.out.println(statistics);
    }

    @Test
    public void robotTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(ROBOT_MODEL)
    			.putConstant("n", "3")
    			.setExploreAll()
    			.run();
    	System.out.println(statistics);
    }

    @Test
    public void uavMdpTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(UAV_MDP_MODEL)
    			.putConstant("accu_load1", "0.3")
    			.putConstant("accu_load2", "0.2")
    			.putConstant("fd", "0.6")
    			.putConstant("COUNTER", "10")
    			.putConstant("risky2", "0.2")
    			.putConstant("risky6", "0.15")
    			.setExploreAll()
    			.run();
    	System.out.println(statistics);
    }

    @Test
    public void virusTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(String.format(VIRUS_MODEL, 3))
    			.putConstant("detect2", "0.2")
    			.setExploreAll()
    			.run();
    	System.out.println(statistics);
    }

    @Test
    public void walkersRingLLTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(WALKERS_RING_LL_MODEL)
    			.putConstant("failureRate", 3)
//				.setExploreAll() // too large
    			.run();
    	System.out.println(statistics);
    }

}
