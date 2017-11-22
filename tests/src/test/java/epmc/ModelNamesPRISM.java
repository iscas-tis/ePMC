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

package epmc;

public final class ModelNamesPRISM {
    private final static String PREFIX = System.getProperty("user.home") + "/prism-examples/";
    //    private final static String PREFIX = "/Users/emhahn/prism-4.2.1-src/examples/";
    private final static String PREFIX_PTA = PREFIX + "pta/";

    private final static String PREFIX_HP = "/Users/emhahn/prism-models-homepage/";

    /* Models shipped with PRISM. */
    public final static String HERMAN_MODEL = PREFIX + "self-stabilisation/herman/herman%d.pm";
    public final static String HERMAN_PROPERTY = PREFIX + "self-stabilisation/herman/herman.pctl";
    public final static String CLUSTER_MODEL = PREFIX + "cluster/cluster.sm";
    public final static String CLUSTER_PROPERTY = PREFIX + "cluster/cluster.csl";
    public final static String FIREWIRE_IMPL_MODEL = PREFIX + "firewire/impl/firewire.nm";
    public final static String FIREWIRE_IMPL_PROPERTY = PREFIX + "firewire/impl/liveness.pctl";
    public final static String FIREWIRE_ABST_MODEL = PREFIX + "firewire/abst/firewire.nm";
    public final static String FIREWIRE_ABST_PROPERTY = PREFIX + "firewire/abst/liveness.pctl";
    public final static String PHIL_MODEL = PREFIX + "phil/original/phil%d.nm";
    public final static String PHIL_PROPERTY = PREFIX + "phil/original/phil.pctl";
    public final static String PHIL_NOFAIR_MODEL = PREFIX + "phil/nofair/phil-nofair%d.nm";
    public final static String PHIL_NOFAIR_PROPERTY = PREFIX + "phil/nofair/phil.pctl";
    public final static String PHIL_LSS_MODEL = PREFIX + "phil_lss/phil_lss%d.nm";
    public final static String PHIL_LSS_PROPERTY = PREFIX + "phil_lss/phil_lss%d.pctl";
    public final static String COIN_MODEL = PREFIX + "consensus/coin%d.nm";
    public final static String COIN_PROPERTY = PREFIX + "consensus/coin.pctl";
    public final static String DICE_MODEL = PREFIX + "dice/dice.pm";
    public final static String DICE_PROPERTY = PREFIX + "dice/dice.pctl";
    public final static String TWO_DICE_MODEL = PREFIX + "dice/two_dice.nm";
    public final static String TWO_DICE_PROPERTY = PREFIX + "dice/two_dice.pctl";
    public final static String IJ_MODEL = PREFIX + "self-stabilisation/israeli-jalfon/ij%d.nm";
    public final static String IJ_PROPERTY = PREFIX + "self-stabilisation/israeli-jalfon/ij.pctl";
    public final static int IJ_MIN_NUM_PROCS = 3;
    public final static int IJ_MAX_NUM_PROCS = 14;
    public final static String CELL_MODEL = PREFIX + "cell/cell.sm";
    public final static String CELL_PROPERTY = PREFIX + "cell/cell.csl";
    public final static String MUTUAL_MODEL = PREFIX + "mutual/mutual%d.nm";
    public final static String MUTUAL_PROPERTY = PREFIX + "mutual/mutual.pctl";
    public final static int[] MUTUAL_SIZES = {3, 4, 5};
    public final static String DINING_CRYPT_MODEL = PREFIX + "dining_crypt/dining_crypt%d.nm";    
    public final static String DINING_CRYPT_PROPERTY = PREFIX + "dining_crypt/correctness.pctl";    
    public final static String BEAUQUIER_MODEL = PREFIX + "self-stabilisation/beauquier/beauquier%d.nm";
    public final static String BEAUQUIER_PROPERTY = PREFIX + "self-stabilisation/beauquier/beauquier.pctl";
    public final static int BEAUQUIER_MIN_NUM_PROCS = 3;
    public final static int BEAUQUIER_MAX_NUM_PROCS = 9;
    public final static String CSMA_MODEL = PREFIX + "csma/csma%d_%d.nm";    
    public final static String CSMA_PROPERTY = PREFIX + "csma/csma.pctl";    
    public final static String BRP_MODEL = PREFIX + "brp/brp.pm";
    public final static String BRP_PROPERTY = PREFIX + "brp/brp.pctl";
    public final static String RABIN_MODEL = PREFIX + "rabin/rabin%d.nm";
    public final static String RABIN_PROPERTY = PREFIX + "rabin/rabin.pctl";
    public final static String LEADER_ASYNC_MODEL = PREFIX + "leader/asynchronous/leader%d.nm";
    public final static String LEADER_ASYNC_PROPERTY = PREFIX + "leader/asynchronous/leader.pctl";
    public final static String LEADER_SYNC_MODEL = PREFIX + "leader/synchronous/leader%d_%d.pm";
    public final static String LEADER_SYNC_PROPERTY = PREFIX + "leader/synchronous/leader.pctl";
    public final static String EMBEDDED_MODEL = PREFIX + "embedded/embedded.sm";
    public final static String EMBEDDED_PROPERTY = PREFIX + "embedded/embedded.csl";
    public final static String FMS_MODEL = PREFIX + "fms/fms.sm";
    public final static String FMS_PROPERTY = PREFIX + "fms/fms.csl";
    public final static String KANBAN_MODEL = PREFIX + "kanban/kanban.sm";
    public final static String KANBAN_PROPERTY = PREFIX + "kanban/kanban.csl";
    public final static String KNACL_MODEL = PREFIX + "molecules/knacl.sm";
    public final static String KNACL_PROPERTY = PREFIX + "molecules/knacl.csl";
    public final static String MC_MODEL = PREFIX + "molecules/mc.sm";
    public final static String MC_PROPERTY = PREFIX + "molecules/mc.csl";
    public final static String NACL_MODEL = PREFIX + "molecules/nacl.sm";
    public final static String NACL_PROPERTY = PREFIX + "molecules/nacl.csl";
    public final static String PEER2PEER_MODEL = PREFIX + "peer2peer/peer2peer%d_%d.sm";
    public final static String PEER2PEER_PROPERTY = PREFIX + "peer2peer/peer2peer.csl";
    public final static String POLLING_MODEL = PREFIX + "polling/poll%d.sm";
    public final static String POLLING_PROPERTY = PREFIX + "polling/poll.csl";
    public final static String TANDEM_MODEL = PREFIX + "tandem/tandem.sm";
    public final static String TANDEM_PROPERTY = PREFIX + "tandem/tandem.csl";
    public final static String WLAN_MODEL = PREFIX + "wlan/wlan%d.nm";
    public final static String WLAN_PROPERTY = PREFIX + "wlan/wlan.pctl";
    public final static String WLAN_COLLIDE_MODEL = PREFIX + "wlan/wlan%d_collide.nm";
    public final static String WLAN_COLLIDE_PROPERTY = PREFIX + "wlan/wlan_collide.pctl";
    public final static String WLAN_TIME_BOUNDED_MODEL = PREFIX + "wlan/wlan%d_time_bounded.nm";
    public final static String WLAN_TIME_BOUNDED_PROPERTY = PREFIX + "wlan/wlan_time_bounded.pctl";
    public final static String ZEROCONF_MODEL = PREFIX + "zeroconf/zeroconf.nm";
    public final static String ZEROCONF_PROPERTY = PREFIX + "zeroconf/zeroconf.pctl";
    public final static String ZEROCONF_TIME_BOUNDED_MODEL = PREFIX + "zeroconf/zeroconf_time_bounded.nm";
    public final static String ZEROCONF_TIME_BOUNDED_PROPERTY = PREFIX + "zeroconf/zeroconf_time_bounded.pctl";

    /* Models available on PRISM homepage (or linked there) but not shipped with PRISM. */
    public final static String BLUETOOTH_MODEL = PREFIX_HP + "bluetooth.pm";
    public final static String BROADCAST_COLL_ASYNC_MODEL = PREFIX_HP + "broadcast.coll_async.nm";
    public final static String BROADCAST_COLL_SYNC_MODEL = PREFIX_HP + "broadcast.coll_sync.pm";
    public final static String BROADCAST_COLL_SYNC_DELAY_MODEL = PREFIX_HP + "broadcast.coll_sync_delay.pm";
    public final static String BROADCAST_COLL_SYNC_LOSSY_MODEL = PREFIX_HP + "broadcast.coll_sync_lossy.pm";
    public final static String BROADCAST_NO_COLL_SYNC_MODEL = PREFIX_HP + "broadcast.no_coll_sync.pm";
    public final static String BYZANTINE_MODEL = PREFIX_HP + "byzantine%d_%d.nm";
    public final static String CC_EDF_MODEL = PREFIX_HP + "cc_edf.nm";
    /* some problem with this model, as we have reserved keyword "ma". */
    public final static String CIRCADIAN_MODEL = PREFIX_HP + "circadian.sm";
    /* some problems, as assignments have not been in braces. */
    public final static String CONTRACT_BMGR_MODEL = PREFIX_HP + "contract_bmgr.nm";
    /* uses old syntax, not usable. */
    public final static String CONTRACT_EGL1_5_MODEL = PREFIX_HP + "contract_egl1_5.pm";
    public final static String CROWDS_MODEL = PREFIX_HP + "crowds.pm";
    public final static String CYCLIN_MODEL = PREFIX_HP + "cyclin.sm";
    public final static String FAIR_EXCHANGE_MODEL = PREFIX_HP + "fair_exchange%d.nm";
    public final static String FGF_MODEL = PREFIX_HP + "fgf.sm";
    public final static String GOSSIP_MODEL = PREFIX_HP + "gossip%d.nm";
    public final static String GOSSIP_DTMC_MODEL = PREFIX_HP + "gossip%d.pm";
    public final static String GRAPH_MODEL = PREFIX_HP + "graph%d.nm";
    public final static String INVESTOR_MODEL = PREFIX_HP + "investor.nm";
    public final static String KAMINSKY_MODEL = PREFIX_HP + "kaminsky.sm";
    public final static String MAPK_CASCADE_MODEL = PREFIX_HP + "mapk_cascade.sm";
    public final static String MDPTT_MODEL = PREFIX_HP + "mdptt.prism";
    public final static String MDSM_MODEL = PREFIX_HP + "mdsm.prism";
    public final static String MDSM_P_MODEL = PREFIX_HP + "mdsm_p.prism";
    public final static String NAND_MODEL = PREFIX_HP + "nand.pm";
    public final static String NEGOTIATION_MODEL = PREFIX_HP + "negotiation.pm";
    public final static String OPTIMAL_TWO_DICE_MODEL = PREFIX_HP + "optimal_two_dice.pm";
    public final static String PINCRACKING_MODEL = PREFIX_HP + "pincracking.nm";
    public final static String POWER_CTMC3_PM1_MODEL = PREFIX_HP + "power_ctmc3_pm1.sm";
    public final static String POWER_CTMC3_SP_MODEL = PREFIX_HP + "power_ctmc3_sp.sm";
    public final static String POWER_CTMC3_SR_MODEL = PREFIX_HP + "power_ctmc3_sr.sm";
    public final static String POWER_CTMC4_PM1_MODEL = PREFIX_HP + "power_ctmc4_pm1.sm";
    public final static String POWER_CTMC4_SP_MODEL = PREFIX_HP + "power_ctmc4_sp.sm";
    public final static String POWER_CTMC4_SR_MODEL = PREFIX_HP + "power_ctmc4_sr.sm";
    public final static String POWER_DTMC_BATTERY_MODEL = PREFIX_HP + "power_dtmc_battery.pm";
    public final static String POWER_DTMC_CLOCK_MODEL = PREFIX_HP + "power_dtmc_clock.pm";
    public final static String POWER_DTMC_PM_MODEL = PREFIX_HP + "power_dtmc_pm.pm";
    public final static String POWER_DTMC_REWARDS_MODEL = PREFIX_HP + "power_dtmc_rewards.pm";
    public final static String POWER_DTMC_SP_MODEL = PREFIX_HP + "power_dtmc_sp.pm";
    public final static String POWER_DTMC_SR_MODEL = PREFIX_HP + "power_dtmc_sr.pm";
    public final static String POWER_DTMC_SRQ_MODEL = PREFIX_HP + "power_dtmc_srq.pm";
    public final static String RABIN_CHOICE_MODEL = PREFIX_HP + "rabin_choice.nm";
    public final static String ROBOT_MODEL = PREFIX_HP + "robot.sm";
    public final static String STABLE_MATCHING_MODEL = PREFIX_HP + "stable_matching%d.pm";
    public final static String STATIC_EDF_MODEL = PREFIX_HP + "static_edf.nm";
    public final static String TEST_AND_SET_MODEL = PREFIX_HP + "test-and-set.nm";
    public final static String THINKTEAM_RETRIAL_MODEL = PREFIX_HP + "thinkteam_retrial.sm";
    public final static String UAV_GAME_MODEL = PREFIX_HP + "uav-game.prism";
    public final static String UAV_MDP_MODEL = PREFIX_HP + "uav-mdp.nm";
    public final static String VIRUS_MODEL = PREFIX_HP + "virus%d.nm";
    public final static String WALKERS_RING_LL_MODEL = PREFIX_HP + "walkers_ringLL.sm";

    public final static String PTA_CSMA_ABST_MODEL = PREFIX_PTA + "csma/abst/csma.nm";
    public final static String PTA_CSMA_ABST_PROPERTY = PREFIX_PTA + "csma/abst/deadline.pctl";
    public final static String PTA_CSMA_FULL_MODEL = PREFIX_PTA + "csma/full/csma.nm";
    public final static String PTA_CSMA_FULL_PROPERTY = PREFIX_PTA + "csma/full/collisions.pctl";
    public final static String PTA_FIREWIRE_ABST_MODEL = PREFIX_PTA + "firewire/abst/firewire.nm";
    public final static String PTA_FIREWIRE_ABST_PROPERTY = PREFIX_PTA + "firewire/abst/deadline-max.pctl";
    public final static String PTA_FIREWIRE_IMPL_MODEL = PREFIX_PTA + "firewire/impl/firewire.nm";
    public final static String PTA_FIREWIRE_IMPL_PROPERTY = PREFIX_PTA + "firewire/impl/deadline.pctl";
    public final static String PTA_REPUDIATION_HONEST_MODEL = PREFIX_PTA + "repudiation/honest/repudiation.nm";
    public final static String PTA_REPUDIATION_HONEST_PROPERTY = PREFIX_PTA + "repudiation/honest/deadline.pctl";
    public final static String PTA_REPUDIATION_MALICIOUS_MODEL = PREFIX_PTA + "repudiation/malicious/repudiation.nm";
    public final static String PTA_REPUDIATION_MALICIOUS_PROPERTY = PREFIX_PTA + "repudiation/malicious/deadline.pctl";
    public final static String PTA_SIMPLE_MODEL = PREFIX_PTA + "simple/formats09.nm";
    public final static String PTA_SIMPLE_PROPERTY = PREFIX_PTA + "simple/formats09.pctl";
    public final static String PTA_ZEROCONF_MODEL = PREFIX_PTA + "zeroconf/zeroconf.nm";
    public final static String PTA_ZEROCONF_PROPERTY = PREFIX_PTA + "zeroconf/deadline.pctl";
}
