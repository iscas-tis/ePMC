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

import java.io.File;

/**
 * Class collecting file names of models for JUnit tests.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ModelNames {
    public final static String JANI_EXPORT_DIR = System.getProperty("user.home") + "/exported-jani-models/";
    public final static String JANI_EXTENSION = ".jani";

    public static String getJANIFilenameFromPRISMFilename(String prismFilename) {
        return getJANIFilenameFromPRISMFilename(prismFilename, "");
    }

    public static String getJANIFilenameFromPRISMFilename(String prismFilename, String prefix) {
        String janiFilename = new File(prismFilename).getName();
        janiFilename = janiFilename.substring(0, janiFilename.lastIndexOf('.'));
        janiFilename = JANI_EXPORT_DIR + prefix + janiFilename + JANI_EXTENSION;
        return janiFilename;
    }

    /** Prefix of path where models are stored. */
    private final static String PREFIX = "epmc/jani/";

    /** Minimal model including only parts required by JANI specification. */
    public final static String MINIMAL = PREFIX + "minimal.jani";

    /** Minimal MDP model including only parts required by JANI specification
     * while not having a deadlock. */
    public final static String MINIMAL_NON_DEADLOCK_MDP = PREFIX + "minimal-non-deadlock-mdp.jani";

    /** Minimal DTMC model including only parts required by JANI specification
     * while not having a deadlock. */
    public final static String MINIMAL_NON_DEADLOCK_DTMC = PREFIX + "minimal-non-deadlock-dtmc.jani";

    /** MDP model consisting of a simple cycle of two states. */
    public final static String TWO_STATES_CYCLE_MDP = PREFIX + "two-states-cycle-mdp.jani";

    /** DTMC model consisting of a simple cycle of two states. */
    public final static String TWO_STATES_CYCLE_DTMC = PREFIX + "two-states-cycle-dtmc.jani";

    /** JANI version of the Knuth's dice model. */
    public final static String DICE = PREFIX + "dice.jani";

    /** JANI version of the Knuth's dice model exported from PRISM. */
    public final static String DICE_PRISM = PREFIX + "dice-prism.jani";

    /** JANI version of the Knuth's dice model using a global variable. */
    public final static String DICE_GLOBAL = PREFIX + "dice-global.jani";

    /** Single cell in wireless communication network, PRISM, [HMPT01]. */
    public final static String CELL = PREFIX + "cell.jani";

    /** MDP model of two simple non-synchronised proceses, forming a diamond-formed semantics. */
    public final static String DIAMOND_MDP = PREFIX + "diamond-mdp.jani";

    /** CTMC model of two simple non-synchronised proceses, forming a diamond-formed semantics. */
    public final static String DIAMOND_CTMC = PREFIX + "diamond-ctmc.jani";

    /** MDP model to check whether synchronisation works. */
    public final static String SYNC_MDP = PREFIX + "sync-mdp.jani";

    /** DTMC model to check whether synchronisation works. */
    public final static String SYNC_DTMC = PREFIX + "sync-dtmc.jani";

    /** Model containing a conflict writing a global variable. */
    public final static String SYNC_CONFLICT = PREFIX + "sync-conflict.jani";

    /** Test for rewards. */
    public final static String REWARDS = PREFIX + "rewards.jani";

    /** BEB model from Arnd Hartmanns. */
    public final static String BEB = PREFIX + "beb.jani";

    /** Model to demonstrate multi initial state features. */
    public final static String INIT = PREFIX + "init.jani";

    public final static String CLUSTER = PREFIX + "cluster.jani";

    public final static String BRP = PREFIX + "brp.jani";

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ModelNames() {
    }
}
