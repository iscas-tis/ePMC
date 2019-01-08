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

package epmc.prism;

import static epmc.ModelNamesPRISM.BEAUQUIER_MODEL;
import static epmc.ModelNamesPRISM.BEAUQUIER_PROPERTY;
import static epmc.ModelNamesPRISM.BRP_MODEL;
import static epmc.ModelNamesPRISM.BRP_PROPERTY;
import static epmc.ModelNamesPRISM.BYZANTINE_MODEL;
import static epmc.ModelNamesPRISM.CC_EDF_MODEL;
import static epmc.ModelNamesPRISM.CELL_MODEL;
import static epmc.ModelNamesPRISM.CELL_PROPERTY;
import static epmc.ModelNamesPRISM.CIRCADIAN_MODEL;
import static epmc.ModelNamesPRISM.CLUSTER_MODEL;
import static epmc.ModelNamesPRISM.CLUSTER_PROPERTY;
import static epmc.ModelNamesPRISM.COIN_MODEL;
import static epmc.ModelNamesPRISM.COIN_PROPERTY;
import static epmc.ModelNamesPRISM.CONTRACT_BMGR_MODEL;
import static epmc.ModelNamesPRISM.CROWDS_MODEL;
import static epmc.ModelNamesPRISM.CSMA_MODEL;
import static epmc.ModelNamesPRISM.CSMA_PROPERTY;
import static epmc.ModelNamesPRISM.CYCLIN_MODEL;
import static epmc.ModelNamesPRISM.DICE_MODEL;
import static epmc.ModelNamesPRISM.DICE_PROPERTY;
import static epmc.ModelNamesPRISM.DINING_CRYPT_MODEL;
import static epmc.ModelNamesPRISM.DINING_CRYPT_PROPERTY;
import static epmc.ModelNamesPRISM.FAIR_EXCHANGE_MODEL;
import static epmc.ModelNamesPRISM.FGF_MODEL;
import static epmc.ModelNamesPRISM.FIREWIRE_ABST_MODEL;
import static epmc.ModelNamesPRISM.FIREWIRE_ABST_PROPERTY;
import static epmc.ModelNamesPRISM.FIREWIRE_IMPL_MODEL;
import static epmc.ModelNamesPRISM.FIREWIRE_IMPL_PROPERTY;
import static epmc.ModelNamesPRISM.FMS_MODEL;
import static epmc.ModelNamesPRISM.FMS_PROPERTY;
import static epmc.ModelNamesPRISM.GOSSIP_MODEL;
import static epmc.ModelNamesPRISM.GRAPH_MODEL;
import static epmc.ModelNamesPRISM.HERMAN_MODEL;
import static epmc.ModelNamesPRISM.HERMAN_PROPERTY;
import static epmc.ModelNamesPRISM.IJ_MODEL;
import static epmc.ModelNamesPRISM.IJ_PROPERTY;
import static epmc.ModelNamesPRISM.INVESTOR_MODEL;
import static epmc.ModelNamesPRISM.KAMINSKY_MODEL;
import static epmc.ModelNamesPRISM.KANBAN_MODEL;
import static epmc.ModelNamesPRISM.KANBAN_PROPERTY;
import static epmc.ModelNamesPRISM.KNACL_MODEL;
import static epmc.ModelNamesPRISM.KNACL_PROPERTY;
import static epmc.ModelNamesPRISM.LEADER_ASYNC_MODEL;
import static epmc.ModelNamesPRISM.LEADER_ASYNC_PROPERTY;
import static epmc.ModelNamesPRISM.LEADER_SYNC_MODEL;
import static epmc.ModelNamesPRISM.LEADER_SYNC_PROPERTY;
import static epmc.ModelNamesPRISM.MAPK_CASCADE_MODEL;
import static epmc.ModelNamesPRISM.MC_MODEL;
import static epmc.ModelNamesPRISM.MC_PROPERTY;
import static epmc.ModelNamesPRISM.MDPTT_MODEL;
import static epmc.ModelNamesPRISM.MUTUAL_MODEL;
import static epmc.ModelNamesPRISM.MUTUAL_PROPERTY;
import static epmc.ModelNamesPRISM.NACL_MODEL;
import static epmc.ModelNamesPRISM.NACL_PROPERTY;
import static epmc.ModelNamesPRISM.NAND_MODEL;
import static epmc.ModelNamesPRISM.NEGOTIATION_MODEL;
import static epmc.ModelNamesPRISM.OPTIMAL_TWO_DICE_MODEL;
import static epmc.ModelNamesPRISM.PEER2PEER_MODEL;
import static epmc.ModelNamesPRISM.PEER2PEER_PROPERTY;
import static epmc.ModelNamesPRISM.PHIL_LSS_MODEL;
import static epmc.ModelNamesPRISM.PHIL_LSS_PROPERTY;
import static epmc.ModelNamesPRISM.PHIL_MODEL;
import static epmc.ModelNamesPRISM.PHIL_NOFAIR_MODEL;
import static epmc.ModelNamesPRISM.PHIL_NOFAIR_PROPERTY;
import static epmc.ModelNamesPRISM.PHIL_PROPERTY;
import static epmc.ModelNamesPRISM.PINCRACKING_MODEL;
import static epmc.ModelNamesPRISM.POLLING_MODEL;
import static epmc.ModelNamesPRISM.POLLING_PROPERTY;
import static epmc.ModelNamesPRISM.RABIN_CHOICE_MODEL;
import static epmc.ModelNamesPRISM.RABIN_MODEL;
import static epmc.ModelNamesPRISM.RABIN_PROPERTY;
import static epmc.ModelNamesPRISM.ROBOT_MODEL;
import static epmc.ModelNamesPRISM.STABLE_MATCHING_MODEL;
import static epmc.ModelNamesPRISM.STATIC_EDF_MODEL;
import static epmc.ModelNamesPRISM.TANDEM_MODEL;
import static epmc.ModelNamesPRISM.TANDEM_PROPERTY;
import static epmc.ModelNamesPRISM.TEST_AND_SET_MODEL;
import static epmc.ModelNamesPRISM.THINKTEAM_RETRIAL_MODEL;
import static epmc.ModelNamesPRISM.TWO_DICE_MODEL;
import static epmc.ModelNamesPRISM.TWO_DICE_PROPERTY;
import static epmc.ModelNamesPRISM.UAV_MDP_MODEL;
import static epmc.ModelNamesPRISM.VIRUS_MODEL;
import static epmc.ModelNamesPRISM.WALKERS_RING_LL_MODEL;
import static epmc.ModelNamesPRISM.WLAN_COLLIDE_MODEL;
import static epmc.ModelNamesPRISM.WLAN_COLLIDE_PROPERTY;
import static epmc.ModelNamesPRISM.WLAN_MODEL;
import static epmc.ModelNamesPRISM.WLAN_PROPERTY;
import static epmc.ModelNamesPRISM.WLAN_TIME_BOUNDED_MODEL;
import static epmc.ModelNamesPRISM.WLAN_TIME_BOUNDED_PROPERTY;
import static epmc.ModelNamesPRISM.ZEROCONF_MODEL;
import static epmc.ModelNamesPRISM.ZEROCONF_PROPERTY;
import static epmc.ModelNamesPRISM.ZEROCONF_TIME_BOUNDED_MODEL;
import static epmc.ModelNamesPRISM.ZEROCONF_TIME_BOUNDED_PROPERTY;
import static epmc.jani.ModelNames.JANI_EXPORT_DIR;
import static epmc.jani.ModelNames.JANI_EXTENSION;
import static epmc.jani.ModelNames.getJANIFilenameFromPRISMFilename;
import static epmc.modelchecker.TestHelper.prepare;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.jani.ConvertTestConfiguration;
import epmc.jani.model.ModelJANI;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.prism.exporter.JANI2PRISMConverter;

public final class ExportJANIToPRISMModels {
    /**
     * Set up the tests.
     */
    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void convertTest() {
        export(System.getProperty("user.home") + "/exporter.jani", System.getProperty("user.home") + "/exported.prism", System.getProperty("user.home") + "/exported.props");
    }

    @Test
    public void convertRandomWalk() {
        export(System.getProperty("user.home") + "/randomWalk.jani", System.getProperty("user.home") + "/randomWalkExported.prism", System.getProperty("user.home") + "/randomWalkExported.props");
    }

    @Test
    public void convertSingle() {
//        export(String.format(COIN_MODEL, 2), COIN_PROPERTY);
        export(ZEROCONF_MODEL, ZEROCONF_PROPERTY);
        export(ZEROCONF_TIME_BOUNDED_MODEL, ZEROCONF_TIME_BOUNDED_PROPERTY);
//        export(BRP_MODEL, BRP_PROPERTY);
    }

    @Test
    public void convertPRISMIncluded() {
        export(BRP_MODEL, BRP_PROPERTY);
        export(CELL_MODEL, CELL_PROPERTY);
        export(CLUSTER_MODEL, CLUSTER_PROPERTY);
        for (int i : new int[]{10,8,6,4,2}) {
            export(String.format(COIN_MODEL, i), COIN_PROPERTY);
        }
        for (int i : new int[]{4,3,2}) {
            for (int j : new int[]{6,4,2}) {
                export(String.format(CSMA_MODEL, i, j), CSMA_PROPERTY);
            }
        }
        export(DICE_MODEL, DICE_PROPERTY);
        export(TWO_DICE_MODEL, TWO_DICE_PROPERTY);
        for (int i : new int[]{15,10,9,8,7,6,5,4,3}) {
            export(String.format(DINING_CRYPT_MODEL, i), DINING_CRYPT_PROPERTY);
        }
        //    	export(EMBEDDED_MODEL, EMBEDDED_PROPERTY);
        export(JANI_EXPORT_DIR + "firewire_abs" + JANI_EXTENSION, FIREWIRE_ABST_MODEL, FIREWIRE_ABST_PROPERTY);
        export(JANI_EXPORT_DIR + "firewire_impl" + JANI_EXTENSION, FIREWIRE_IMPL_MODEL, FIREWIRE_IMPL_PROPERTY);
        export(FMS_MODEL, FMS_PROPERTY);
        export(KANBAN_MODEL, KANBAN_PROPERTY);
        //Before enabling this test, fix the LEADER_ASYNC_PROPERTY file since it contains
        //the wrong property   fiter(forall, leaders<=1)  instead of the correct one
        //  filter(forall, leaders<=1)  
        for (int i : new int[]{10,9,8,7,6,5,4,3}) {
            export(String.format(JANI_EXPORT_DIR + "leader_async_%d" + JANI_EXTENSION, i), 
                    String.format(LEADER_ASYNC_MODEL, i), LEADER_ASYNC_PROPERTY);
        }
        for (int i : new int[]{6,5,4,3}) {
            for (int j : new int[]{8,6,5,4,3,2}) {
                export(String.format(JANI_EXPORT_DIR + "leader_sync_%d_%d" + JANI_EXTENSION, i, j), 
                        String.format(LEADER_SYNC_MODEL, i, j), LEADER_SYNC_PROPERTY);
            }
        }
        export(KNACL_MODEL, KNACL_PROPERTY);
        export(NACL_MODEL, NACL_PROPERTY);
        export(MC_MODEL, MC_PROPERTY);
        for (int i : new int[]{10,8,5,4,3}) {
            export(String.format(MUTUAL_MODEL, i), MUTUAL_PROPERTY);
        }
        for (int i : new int[]{5,4}) {
            for (int j : new int[]{8,7,6,5,4}) {
                export(String.format(PEER2PEER_MODEL, i, j), PEER2PEER_PROPERTY);
            }
        }
        for (int i : new int[]{30,25,20,15,10,9,8,7,6,5,4,3}) {
            export(String.format(PHIL_MODEL, i), PHIL_PROPERTY);
        }
        for (int i : new int[]{10,9,8,7,6,5,4,3}) {
            export(String.format(PHIL_NOFAIR_MODEL, i), String.format(PHIL_NOFAIR_PROPERTY, i));
        }
        for (int i : new int[]{4,3}) {
            export(String.format(PHIL_LSS_MODEL, i), String.format(PHIL_LSS_PROPERTY, i));
        }
        for (int i : new int[]{20,19,18,17,16,15,14,13,12,11,10,9,8,7,6,5,4,3,2}) {
            export(String.format(POLLING_MODEL, i), POLLING_PROPERTY);
        }
        for (int i : new int[]{10,9,8,7,6,5,4,3}) {
            export(String.format(RABIN_MODEL, i), RABIN_PROPERTY);
        }
        for (int i : new int[]{11,9,7,5,3}) {
            export(String.format(BEAUQUIER_MODEL, i), BEAUQUIER_PROPERTY);
        }
        for (int i : new int[]{21,19,17,15,13,11,9,7,5,3}) {
            export(String.format(HERMAN_MODEL, i), HERMAN_PROPERTY);
        }
        for (int i : new int[]{21,20,19,18,17,16,15,14,13,12,11,10,9,8,7,6,5,4,3}) {
            export(String.format(IJ_MODEL, i), IJ_PROPERTY);
        }
        export(TANDEM_MODEL, TANDEM_PROPERTY);
        for (int i : new int[]{6,5,4,3,2,1,0}) {
            export(String.format(WLAN_MODEL, i), WLAN_PROPERTY);
        }
        for (int i : new int[]{6,5,4,3,2,1,0}) {
            export(String.format(WLAN_COLLIDE_MODEL, i), WLAN_COLLIDE_PROPERTY);
        }
        for (int i : new int[]{6,5,4,3,2,1,0}) {
            export(String.format(WLAN_TIME_BOUNDED_MODEL, i), WLAN_TIME_BOUNDED_PROPERTY);
        }
        export(ZEROCONF_MODEL, ZEROCONF_PROPERTY);
        export(ZEROCONF_TIME_BOUNDED_MODEL, ZEROCONF_TIME_BOUNDED_PROPERTY);
    }

    //    @Test
    public void convertPRISMHomepage() {
        //    	export(BROADCAST_COLL_ASYNC_MODEL);
        //    	export(BROADCAST_COLL_SYNC_MODEL);
        //    	export(BROADCAST_COLL_SYNC_DELAY_MODEL);
        //    	export(BROADCAST_COLL_SYNC_LOSSY_MODEL);
        //    	export(BROADCAST_NO_COLL_SYNC_MODEL);
        export(String.format(BYZANTINE_MODEL, 4, 1));
        export(CC_EDF_MODEL);
        export(CIRCADIAN_MODEL);
        export(CONTRACT_BMGR_MODEL);
        export(CROWDS_MODEL);
        export(CYCLIN_MODEL);
        export(String.format(FAIR_EXCHANGE_MODEL, 10));
        export(FGF_MODEL);
        export(String.format(GOSSIP_MODEL, 4));
        export(String.format(GRAPH_MODEL, 4));
        export(INVESTOR_MODEL);
        export(KAMINSKY_MODEL);
        export(MAPK_CASCADE_MODEL);
        export(MDPTT_MODEL);
        // export(MDSM_MODEL);
        // export(MDSM_P_MODEL);
        export(NAND_MODEL);
        export(NEGOTIATION_MODEL);
        export(OPTIMAL_TWO_DICE_MODEL);
        export(PINCRACKING_MODEL);
        // POWER_CTMC3_PM1_MODEL
        // POWER_CTMC3_SP_MODEL
        // POWER_CTMC3_SR_MODEL
        // POWER_CTMC4_PM1_MODEL
        // POWER_CTMC4_SP_MODEL
        // (POWER_CTMC4_SR_MODEL
        // POWER_DTMC_BATTERY_MODEL
        // POWER_DTMC_CLOCK_MODEL
        // POWER_DTMC_PM_MODEL
        // POWER_DTMC_REWARDS_MODEL
        // POWER_DTMC_SP_MODEL
        // POWER_DTMC_SR_MODEL
        // POWER_DTMC_SRQ_MODEL
        export(RABIN_CHOICE_MODEL);
        export(ROBOT_MODEL);
        for (int i : new int[]{1,2,3}) {
            export(String.format(STABLE_MATCHING_MODEL, i));
        }
        export(STATIC_EDF_MODEL);
        export(TEST_AND_SET_MODEL);
        export(THINKTEAM_RETRIAL_MODEL);
        // export(UAV_GAME_MODEL);
        export(UAV_MDP_MODEL);
        export(String.format(VIRUS_MODEL, 3));
        export(WALKERS_RING_LL_MODEL);
    }

    private static void export(String prismFilename) {
        export(null, prismFilename, null);
    }

    private static void export(String prismFilename, String propertyFilename) {
        export(null, prismFilename, propertyFilename);
    }

    private static void export(String janiFilename, String prismFilename, String propertyFilename) {
        String modelName = new File(prismFilename).getName();
        modelName = modelName.substring(0, modelName.lastIndexOf('.'));
        if (janiFilename == null) {
            janiFilename = getJANIFilenameFromPRISMFilename(prismFilename);
        }
        System.out.println("Exporting " + janiFilename + ":");
        System.out.println("Loading");
        Options options = ConvertTestConfiguration.prepareJANIOptions(null);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        ModelJANI jani = (ModelJANI) TestHelper.loadModelMulti(options, janiFilename);

        JANI2PRISMConverter converter = new JANI2PRISMConverter(jani);
        System.out.println("Converting");       
        try (PrintWriter out = new PrintWriter(prismFilename + "-exported")) {
            out.println(converter.convertModel()
                    .toString());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try (PrintWriter out = new PrintWriter(propertyFilename + "-exported")) {
            out.println(converter.convertProperties().toString());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Done");
        System.out.println();
    }
}
