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

public final class ModelNamesOwn {
    private final static String PREFIX = "epmc/";

    public final static String ZEROCONF_SIMPLE = PREFIX + "zeroconf.prism";
    public final static String CHAIN = PREFIX + "chain.prism";
    public final static String CLUSTER_DTMC_1 = PREFIX + "clusterDTMC1.prism";
    public final static String SIMPLE = PREFIX + "simple.prism";
    public final static String SIMPLE_QUEUE = PREFIX + "simple_queue.prism";
    public final static String VERYLARGE = PREFIX + "verylarge.prism";
    public final static String ER12_1 = PREFIX + "er12-1.prism";
    public final static String RANDOM_GOOGLE = PREFIX + "random_google.prism";

    public final static String MULTI_OBJECTIVE_SIMPLE = PREFIX + "multiObjectiveSimple.prism";
    public final static String MULTI_OBJECTIVE_SIMPLE_REWARDS = PREFIX + "multiObjectiveSimpleRewards.prism";

    public final static String ROBOT_ONE_DIR = PREFIX + "robotOneDir.prism";
    public final static String ROBOT_REDUCED = PREFIX + "robotReduced.prism";

    public final static String MA_SINGLEMODULE = PREFIX + "ma-singlemodule.prism";
    public final static String MA_SINGLEMODULE_TWORATE = PREFIX + "ma-singlemodule-tworate.prism";
    public final static String MA_TWOMODULES = PREFIX + "ma-twomodules.prism";
    public final static String MA_DISABLING_RATE = PREFIX + "ma-disabling-rate.prism";
    public final static String POW_TYPE = PREFIX + "pow-type.prism";
    public final static String NAVIGATION_1 = PREFIX + "navigation_1.prism";
    public static final String INCONSISTENT_OPERATOR = PREFIX + "inconsistent-operator.prism";
    public static final String INCONSISTENT_OPERATOR_INIT = PREFIX + "inconsistent-operator-init.prism";
    public static final String WRONG_ASSIGNMENT_INIT = PREFIX + "wrong-assignment-init.prism";
    public static final String CLUSTER_DTMC_3_SALOMON = PREFIX + "clusterDTMC3Salomon.prism";
    public static final String PETERSON = PREFIX + "petersonWP-nostorage-rid.prism";
    
    public final static String PAUL_GAINER_SMALL = PREFIX + "model-mirollo-strogatz-6-10-0.1-1-0-False.prism";
    public final static String PAUL_GAINER_MEDIUM = PREFIX + "model-mirollo-strogatz-7-10-0.1-1-0-False.prism";
    /** Model testing correct recognition of PCTL properties. */
    public final static String PCTL_RECOGNITION_TEST = PREFIX + "pctl-recognition-test.prism";
    
    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ModelNamesOwn() {
    }
}
