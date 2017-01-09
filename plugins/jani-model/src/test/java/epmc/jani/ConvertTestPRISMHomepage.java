package epmc.jani;

import static epmc.ModelNamesPRISM.*;
import static epmc.modelchecker.TestHelper.*;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import epmc.error.EPMCException;

public final class ConvertTestPRISMHomepage {
	/** Location of plugin directory in file system. */
    final static String PLUGIN_DIR = System.getProperty("user.dir") + "/target/classes/";

    /**
     * Set up the tests.
     */
    @BeforeClass
    public static void initialise() {
        prepare();
    }

    // BLUETOOTH_MODEL uses old syntax, thus not supported
    
    @Test
    public void broadcastCollAsyncTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(BROADCAST_COLL_ASYNC_MODEL)
    			.putConstant("psend", "0.2")
    			.setExploreAll()
    			.run();
    	System.out.println(statistics);
    }

    @Test
    public void broadcastCollSyncTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(BROADCAST_COLL_SYNC_MODEL)
    			.putConstant("psend", "0.2")
    			.setExploreAll()
    			.run();
    	System.out.println(statistics);
    }

    @Test
    public void broadcastCollSyncDelayTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(BROADCAST_COLL_SYNC_DELAY_MODEL)
    			.setExploreAll()
    			.run();
    	System.out.println(statistics);
    }

    @Test
    public void broadcastCollSyncLossyTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(BROADCAST_COLL_SYNC_LOSSY_MODEL)
    			.putConstant("precv", "0.3")
    			.setExploreAll()
    			.run();
    	System.out.println(statistics);
    }

    @Test
    public void broadcastNoCollSyncTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(BROADCAST_NO_COLL_SYNC_MODEL)
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
    public void ccEdfTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(CC_EDF_MODEL)
    			.setExploreAll()
    			.run();
    	System.out.println(statistics);
    }

    @Test
    public void circadianTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(CIRCADIAN_MODEL)
//    			.setExploreAll()
    			.run();
    	System.out.println(statistics);
    }

    @Test
    public void contractBmgrTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(CONTRACT_BMGR_MODEL)
    			.setExploreAll()
    			.run();
    	System.out.println(statistics);
    }

    // CONTRACT_EGL1_5_MODEL uses old syntax, not usable

    @Test
    public void crowdsTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(CROWDS_MODEL)
    			.setExploreAll()
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
    public void fgfTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(FGF_MODEL)
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
    public void investorTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(INVESTOR_MODEL)
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
    public void mdpttTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(MDPTT_MODEL)
    			.setExploreAll()
    			.run();
    	System.out.println(statistics);
    }

    // TODO this is a game model, cannot handle currently
    @Ignore
    @Test
    public void mdsmTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(MDSM_MODEL)
    			.setExploreAll()
    			.run();
    	System.out.println(statistics);
    }

    // TODO this is a game model, cannot handle currently
    @Ignore
    @Test
    public void mdsmPTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(MDSM_P_MODEL)
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
    public void negotiationTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(NEGOTIATION_MODEL)
    			.setExploreAll()
    			.run();
    	System.out.println(statistics);
    }

    @Test
    public void optimalTwoDiceTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(OPTIMAL_TWO_DICE_MODEL)
    			.setExploreAll()
    			.run();
    	System.out.println(statistics);
    }

    @Test
    public void pincrackingTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(PINCRACKING_MODEL)
    			.setExploreAll()
    			.run();
    	System.out.println(statistics);
    }
    
    @Ignore // TODO to make this work, will have to combine some of the files
    @Test
    public void powerTest() throws EPMCException {
    	new ConvertTestConfiguration()
		.setModelName(POWER_CTMC3_PM1_MODEL)
		.setExploreAll()
		.run();
    	
    	new ConvertTestConfiguration()
		.setModelName(POWER_CTMC3_SP_MODEL)
		.setExploreAll()
		.run();
    	
    	new ConvertTestConfiguration()
		.setModelName(POWER_CTMC3_SR_MODEL)
		.setExploreAll()
		.run();
    	
    	new ConvertTestConfiguration()
		.setModelName(POWER_CTMC4_PM1_MODEL)
		.setExploreAll()
		.run();
    	
    	new ConvertTestConfiguration()
		.setModelName(POWER_CTMC4_SP_MODEL)
		.setExploreAll()
		.run();
    	
    	new ConvertTestConfiguration()
		.setModelName(POWER_CTMC4_SR_MODEL)
		.setExploreAll()
		.run();
    	
    	new ConvertTestConfiguration()
		.setModelName(POWER_DTMC_BATTERY_MODEL)
		.setExploreAll()
		.run();
    	
    	new ConvertTestConfiguration()
		.setModelName(POWER_DTMC_CLOCK_MODEL)
		.setExploreAll()
		.run();
    	
    	new ConvertTestConfiguration()
		.setModelName(POWER_DTMC_PM_MODEL)
		.setExploreAll()
		.run();
    	
    	new ConvertTestConfiguration()
		.setModelName(POWER_DTMC_REWARDS_MODEL)
		.setExploreAll()
		.run();
    	
    	new ConvertTestConfiguration()
		.setModelName(POWER_DTMC_SP_MODEL)
		.setExploreAll()
		.run();
    	
    	new ConvertTestConfiguration()
		.setModelName(POWER_DTMC_SR_MODEL)
		.setExploreAll()
		.run();
    	
    	new ConvertTestConfiguration()
		.setModelName(POWER_DTMC_SRQ_MODEL)
		.setExploreAll()
		.run();
    }
    
    @Test
    public void rabinChoiceTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(RABIN_CHOICE_MODEL)
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
    public void stableMatchingTest() throws EPMCException {
    	ConvertTestStatistics statistics1 = new ConvertTestConfiguration()
    			.setModelName(String.format(STABLE_MATCHING_MODEL, 1))
    			.setExploreAll()
    			.run();
    	System.out.println(statistics1);
    	
    	ConvertTestStatistics statistics2 = new ConvertTestConfiguration()
    			.setModelName(String.format(STABLE_MATCHING_MODEL, 2))
    			.setExploreAll()
    			.run();
    	System.out.println(statistics2);

    	ConvertTestStatistics statistics3 = new ConvertTestConfiguration()
    			.setModelName(String.format(STABLE_MATCHING_MODEL, 3))
    			.setExploreAll()
    			.run();
    	System.out.println(statistics3);
    }
    
    @Test
    public void staticEdfTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(STATIC_EDF_MODEL)
    			.setExploreAll()
    			.run();
    	System.out.println(statistics);
    }

    @Test
    public void testAndSetTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(TEST_AND_SET_MODEL)
    			.setExploreAll()
    			.run();
    	System.out.println(statistics);
    }

    @Test
    public void thinkteamRetrialTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(THINKTEAM_RETRIAL_MODEL)
    			.setExploreAll()
    			.run();
    	System.out.println(statistics);
    }

    // TODO game, cannot yet handle
    @Ignore
    @Test
    public void uavGameTest() throws EPMCException {
    	ConvertTestStatistics statistics = new ConvertTestConfiguration()
    			.setModelName(UAV_GAME_MODEL)
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
