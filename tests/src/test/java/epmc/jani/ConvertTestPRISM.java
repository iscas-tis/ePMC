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

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.jani.model.UtilModelParser;
import epmc.modelchecker.UtilModelChecker;

import static epmc.ModelNamesPRISM.*;
import static epmc.modelchecker.TestHelper.*;

import java.util.LinkedHashSet;
import java.util.Set;

public final class ConvertTestPRISM {
    /**
     * Set up the tests.
     */
    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void brpTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(BRP_MODEL)
                .putConstant("N", "16")
                .putConstant("MAX", "2")
                .setExploreAll()
                .run();
        System.out.println(statistics);
    }

    @Test
    public void cellTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(CELL_MODEL)
                .putConstant("N", "11")
                .setExploreAll()
                .run();
        System.out.println(statistics);
    }

    @Test
    public void clusterTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(CLUSTER_MODEL)
                .putConstant("N", "16")
                .putConstant("MAX", "8")
                //    			.setPrismFlatten(true)
                .setExploreAll()
                .run();
        System.out.println(statistics);
    }

    @Test
    public void coinTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(String.format(COIN_MODEL, 4))
                .putConstant("K", "4")
                .setExploreAll()
                .run();
        System.out.println(statistics);
    }

    @Test
    public void csmaTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(String.format(CSMA_MODEL, 2, 6))
                .setExploreAll()
                .run();
        System.out.println(statistics);
    }

    @Test
    public void diceTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(DICE_MODEL)
                .setExploreAll()
                .run();
        System.out.println(statistics);
    }

    @Test
    public void twoDiceTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(TWO_DICE_MODEL)
                .setExploreAll()
                .run();
        System.out.println(UtilModelParser.prettyString(statistics.getJaniModel()));
    }

    @Test
    public void diningCryptTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(String.format(DINING_CRYPT_MODEL, 3))
                .setExploreAll()
                .run();
        System.out.println(statistics);
    }

    @Test
    public void embeddedTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(EMBEDDED_MODEL)
                .putConstant("MAX_COUNT", 4)
                .setExploreAll()
                .run();
        System.out.println(statistics);
    }

    @Test
    public void firewireAbstTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(FIREWIRE_ABST_MODEL)
                .putConstant("delay", "1")
                .putConstant("fast", "0.1")
                .setExploreAll()
                //			.setPrintJANI(true)
                .run();
        System.out.println(statistics);
    }

    @Test
    public void firewireImplTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(FIREWIRE_IMPL_MODEL)
                .putConstant("delay", "5")
                .putConstant("fast", "0.4")
                .setExploreAll()
                .run();
        System.out.println(statistics);
    }

    @Test
    public void fmsTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .putConstant("n", 3)
                .setModelName(FMS_MODEL)
                .setExploreAll()
                .run();
        System.out.println(statistics);
    }

    @Test
    public void kanbanTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .putConstant("t", 2)
                .setModelName(KANBAN_MODEL)
                .setExploreAll()
                .run();
        System.out.println(statistics);
    }

    @Test
    public void asyncLeaderTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(String.format(LEADER_ASYNC_MODEL, 3))
                .setExploreAll()
                .run();
        System.out.println(statistics);
    }

    @Test
    public void syncLeaderTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(String.format(LEADER_SYNC_MODEL, 3, 3))
                .setExploreAll()
                .run();
        System.out.println(statistics);
    }

    @Test
    public void knaclTest() {
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
    public void naclTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(NACL_MODEL)
                .putConstant("N1", "10")
                .putConstant("N2", "10")
                .setExploreAll()
                .run();
        System.out.println(statistics);
    }

    @Test
    public void mcTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(MC_MODEL)
                .putConstant("N1", "10")
                .putConstant("N2", "10")
                .setExploreAll()
                .run();
        System.out.println(statistics);
    }

    @Test
    public void mutualTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(String.format(MUTUAL_MODEL, 4))
                //    			.setExploreAll()
                .run();

        Set<Object> nodeProperties = new LinkedHashSet<>();
        nodeProperties.add(UtilModelChecker.parseExpression("p1=10"));
        nodeProperties.add(UtilModelChecker.parseExpression("p2=10"));
        nodeProperties.add(UtilModelChecker.parseExpression("p3=10"));
        nodeProperties.add(UtilModelChecker.parseExpression("p4=10"));

        //    	GraphExplicit graph = exploreToGraph(statistics.getJaniModel(), nodeProperties);
        //	System.out.println(graph);
    }

    @Test
    public void peer2peerTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(String.format(PEER2PEER_MODEL, 4, 4))
                .setExploreAll()
                .run();
        System.out.println(statistics);
    }

    @Test
    public void philTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(String.format(PHIL_MODEL, 3))
                .setExploreAll()
                .run();
        System.out.println(statistics);
    }

    @Test
    public void philNoFairTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(String.format(PHIL_NOFAIR_MODEL, 3))
                .setExploreAll()
                .run();
        System.out.println(statistics);
    }

    @Test
    public void philLssTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(String.format(PHIL_LSS_MODEL, 3))
                .putConstant("K", "3")
                .setExploreAll()
                .run();
        System.out.println(statistics);
        System.out.println(UtilModelParser.prettyString(statistics.getJaniModel()));
    }

    @Test
    public void pollingTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(String.format(POLLING_MODEL, 3))
                .setExploreAll()
                .run();
        System.out.println(statistics);
    }

    @Test
    public void rabinTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(String.format(RABIN_MODEL, 3))
                .setExploreAll()
                .run();
        System.out.println(statistics);
    }

    @Test
    public void beauquierTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(String.format(BEAUQUIER_MODEL, 3))
                .setExploreAll()
                .run();
        System.out.println(statistics);
    }

    @Test
    public void hermanTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(String.format(HERMAN_MODEL, 3))
                .setExploreAll()
                .run();
        System.out.println(statistics);
    }

    @Test
    public void ijTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(String.format(IJ_MODEL, 3))
                .setExploreAll()
                .run();
        System.out.println(statistics);
    }

    @Test
    public void tandemTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(TANDEM_MODEL)
                .putConstant("c", "3")
                .setExploreAll()
                .run();
        System.out.println(statistics);
    }

    @Test
    public void wlanTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(String.format(WLAN_MODEL, 1))
                .putConstant("TRANS_TIME_MAX", "5")
                .setExploreAll()
                .run();
        System.out.println(statistics);
    }

    @Test
    public void wlanCollideTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(String.format(WLAN_COLLIDE_MODEL, 1))
                .putConstant("TRANS_TIME_MAX", "3")
                .putConstant("COL", "3")
                .setExploreAll()
                .run();
        System.out.println(statistics);
    }

    @Test
    public void wlanTimeBoundTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(String.format(WLAN_TIME_BOUNDED_MODEL, 1))
                .putConstant("TRANS_TIME_MAX", "5")
                .putConstant("DEADLINE", "4")
                .setExploreAll()
                .run();
        System.out.println(statistics);
    }

    @Test
    public void zeroconfTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(ZEROCONF_MODEL)
                .putConstant("reset", "false")
                .putConstant("N", "3")
                .putConstant("K", "2")
                .putConstant("err", "0.2")
                .setExploreAll()
                .run();
        System.out.println(statistics);
    }

    @Test
    public void zeroconfTimeBoundedTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(ZEROCONF_TIME_BOUNDED_MODEL)
                .putConstant("reset", "false")
                .putConstant("T", "5")
                .putConstant("N", "3")
                .putConstant("K", "2")
                .putConstant("err", "0.2")
                .setExploreAll()
                .run();
        System.out.println(statistics);
    }
}
