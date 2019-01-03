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
import static epmc.ModelNamesPRISM.CELL_MODEL;
import static epmc.ModelNamesPRISM.CELL_PROPERTY;
import static epmc.ModelNamesPRISM.CLUSTER_MODEL;
import static epmc.ModelNamesPRISM.CLUSTER_PROPERTY;
import static epmc.ModelNamesPRISM.COIN_MODEL;
import static epmc.ModelNamesPRISM.COIN_PROPERTY;
import static epmc.ModelNamesPRISM.CSMA_MODEL;
import static epmc.ModelNamesPRISM.CSMA_PROPERTY;
import static epmc.ModelNamesPRISM.DICE_MODEL;
import static epmc.ModelNamesPRISM.DICE_PROPERTY;
import static epmc.ModelNamesPRISM.DINING_CRYPT_MODEL;
import static epmc.ModelNamesPRISM.DINING_CRYPT_PROPERTY;
import static epmc.ModelNamesPRISM.FIREWIRE_ABST_MODEL;
import static epmc.ModelNamesPRISM.FIREWIRE_ABST_PROPERTY;
import static epmc.ModelNamesPRISM.FIREWIRE_IMPL_MODEL;
import static epmc.ModelNamesPRISM.FIREWIRE_IMPL_PROPERTY;
import static epmc.ModelNamesPRISM.FMS_MODEL;
import static epmc.ModelNamesPRISM.FMS_PROPERTY;
import static epmc.ModelNamesPRISM.HERMAN_MODEL;
import static epmc.ModelNamesPRISM.HERMAN_PROPERTY;
import static epmc.ModelNamesPRISM.IJ_MODEL;
import static epmc.ModelNamesPRISM.IJ_PROPERTY;
import static epmc.ModelNamesPRISM.KANBAN_MODEL;
import static epmc.ModelNamesPRISM.KANBAN_PROPERTY;
import static epmc.ModelNamesPRISM.KNACL_MODEL;
import static epmc.ModelNamesPRISM.KNACL_PROPERTY;
import static epmc.ModelNamesPRISM.LEADER_ASYNC_MODEL;
import static epmc.ModelNamesPRISM.LEADER_ASYNC_PROPERTY;
import static epmc.ModelNamesPRISM.LEADER_SYNC_MODEL;
import static epmc.ModelNamesPRISM.LEADER_SYNC_PROPERTY;
import static epmc.ModelNamesPRISM.MC_MODEL;
import static epmc.ModelNamesPRISM.MC_PROPERTY;
import static epmc.ModelNamesPRISM.MUTUAL_MODEL;
import static epmc.ModelNamesPRISM.MUTUAL_PROPERTY;
import static epmc.ModelNamesPRISM.NACL_MODEL;
import static epmc.ModelNamesPRISM.NACL_PROPERTY;
import static epmc.ModelNamesPRISM.PEER2PEER_MODEL;
import static epmc.ModelNamesPRISM.PEER2PEER_PROPERTY;
import static epmc.ModelNamesPRISM.PHIL_LSS_MODEL;
import static epmc.ModelNamesPRISM.PHIL_LSS_PROPERTY;
import static epmc.ModelNamesPRISM.PHIL_MODEL;
import static epmc.ModelNamesPRISM.PHIL_NOFAIR_MODEL;
import static epmc.ModelNamesPRISM.PHIL_NOFAIR_PROPERTY;
import static epmc.ModelNamesPRISM.PHIL_PROPERTY;
import static epmc.ModelNamesPRISM.POLLING_MODEL;
import static epmc.ModelNamesPRISM.POLLING_PROPERTY;
import static epmc.ModelNamesPRISM.RABIN_MODEL;
import static epmc.ModelNamesPRISM.RABIN_PROPERTY;
import static epmc.ModelNamesPRISM.TANDEM_MODEL;
import static epmc.ModelNamesPRISM.TANDEM_PROPERTY;
import static epmc.ModelNamesPRISM.TWO_DICE_MODEL;
import static epmc.ModelNamesPRISM.TWO_DICE_PROPERTY;
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
import static epmc.modelchecker.TestHelper.assertEquals;
import static epmc.modelchecker.TestHelper.computeResults;
import static epmc.modelchecker.TestHelper.computeResultsMapName;
import static epmc.modelchecker.TestHelper.loadModel;
import static epmc.modelchecker.TestHelper.prepare;
import static epmc.modelchecker.TestHelper.prepareOptions;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import epmc.main.options.UtilOptionsEPMC;
import epmc.messages.OptionsMessages;
import epmc.messages.TimeStampFormatSecondsStarted;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.Model;
import epmc.modelchecker.ModelCheckerResults;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.TestHelper.LogType;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.prism.model.ModelPRISM;
import epmc.propertysolver.ltllazy.OptionsLTLLazy;
import epmc.value.OptionsValue;
import epmc.value.Value;

/**
 * Tests for model checking of PRISM models; based on the one for JANI models
 * 
 * @author Andrea Turrini
 * @author Ernst Moritz Hahn
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class CheckExplicitTestExportedReduced {
    /** Location of plugin directory in file system. */
    //    private final static String PLUGIN_DIR = System.getProperty("user.dir") + "/target/classes/";

    /**
     * Set up the tests.
     */
    @BeforeClass
    public static void initialise() {
        prepare();
    }

    private final Model loadModelExported(Options options, String modelFile, String propertiesFile) {
        return loadModel(options, modelFile + "-exported", propertiesFile + "-exported");
//        return loadModel(options, modelFile, propertiesFile);
    }
    
    /**
     * Prepare options including loading PRISM plugin.
     * 
     * @return options usable for PRISM model analysis
     */
    private final static Options preparePRISMOptions() {
        try {
            System.setErr(new PrintStream(new FileOutputStream("/tmp/log_file.txt", true)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Options options = UtilOptionsEPMC.newOptions();
        prepareOptions(options, LogType.TRANSLATE, ModelPRISM.IDENTIFIER);
        options.set(OptionsMessages.TIME_STAMPS, TimeStampFormatSecondsStarted.class);
        options.set(OptionsMessages.TRANSLATE_MESSAGES, "false");
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(TestHelper.ITERATION_STOP_CRITERION, "absolute");
        options.set(OptionsLTLLazy.LTL_LAZY_INCREMENTAL, "true");
        options.set(OptionsValue.VALUE_FLOATING_POINT_OUTPUT_NATIVE, "true");
        return options;
    }

    private final static Options preparePRISMOptions(String outputLog) {
        try {
            System.setOut(new PrintStream(new FileOutputStream(outputLog + ".log", false)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return preparePRISMOptions();
    }

    @Ignore
    @Test
    public void testPRISMTest() {
        Map<String, Object> constants = new LinkedHashMap<>();
//    	constants.put("N", "4");
        Options options = preparePRISMOptions(System.getProperty("user.home") + "/test.prism");
        options.set(OptionsModelChecker.CONST, constants);
//        options.set(OptionsGraphsolver.GRAPHSOLVER_SOLVER, "graph-solver-lp");
        Model model = null;
        model = loadModelExported(options, System.getProperty("user.home") + "/test.prism", System.getProperty("user.home") + "/test.prop");

        ModelCheckerResults result = computeResults(model);
        int i = 0;
        //        assertEquals("1/6", result.get("ProbThrowSix"), 2.0E-7);
        //        assertEquals("11/3", result.get("StepsUntilReach"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISMRandomWalk() {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("p", "0.5");
    	Options options = preparePRISMOptions(System.getProperty("user.home") + "/randomWalkNegative.prism");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, System.getProperty("user.home") + "/randomWalkNegative.prism", System.getProperty("user.home") + "/randomWalk.pctl");
        
        ModelCheckerResults result = computeResults(model);
        int i = 0;
//        assertEquals("1/6", result.get("ProbThrowSix"), 2.0E-7);
//        assertEquals("11/3", result.get("StepsUntilReach"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISMTest1() {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("N", "10");
    	Options options = preparePRISMOptions(System.getProperty("user.home") + "/test1.prism");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, System.getProperty("user.home") + "/test1.prism", System.getProperty("user.home") + "/test1.props");
        
        ModelCheckerResults result = computeResults(model);
        int i = 0;
//        assertEquals("1/6", result.get("ProbThrowSix"), 2.0E-7);
//        assertEquals("11/3", result.get("StepsUntilReach"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISMClusterDTMC3() {
        Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions(System.getProperty("user.home") + "/prism-examples/clusterDTMC3.prism");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, System.getProperty("user.home") + "/prism-examples/clusterDTMC3.prism", System.getProperty("user.home") + "/prism-examples/clusterDTMC3.prop");

        ModelCheckerResults result = computeResults(model);
        int i = 0;
        //        assertEquals("1/6", result.get("ProbThrowSix"), 2.0E-7);
        //        assertEquals("11/3", result.get("StepsUntilReach"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISMPeterson() {
        Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions(System.getProperty("user.home") + "/Documenti/Ricerca/Working/Learning/AG/petersonWP-nostorage-rid.prism");
        options.set(OptionsModelChecker.CONST, constants);
        options.set(OptionsLTLLazy.LTL_LAZY_USE_BREAKPOINT_SINGLETONS, "true");
        options.set(OptionsLTLLazy.LTL_LAZY_USE_RABIN, "false");
        Model model = null;
        model = loadModelExported(options, System.getProperty("user.home") + "/Documenti/Ricerca/Working/Learning/AG/petersonWP-nostorage-rid.prism", System.getProperty("user.home") + "/Documenti/Ricerca/Working/Learning/AG/petersonWP.props");

        ModelCheckerResults result = computeResults(model);
        int i = 0;
        //        assertEquals("1/6", result.get("ProbThrowSix"), 2.0E-7);
        //        assertEquals("11/3", result.get("StepsUntilReach"), 2.0E-7);
    }

    @Test
    public void testPRISM_BRP() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("MAX", "4");
        constants.put("N", "64");
        Options options = preparePRISMOptions(BRP_MODEL);
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, BRP_MODEL, BRP_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.0000000000000000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.0000000000000000", result.get("Property_1"), 2.0E-7);
        assertEquals("0.0000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.0000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.0000015032933912", result.get("Property_4"), 2.0E-7);
        assertEquals("0.0000015032933912", result.get("Property_5"), 2.0E-7);
        assertEquals("0.0000000227728170", result.get("Property_6"), 2.0E-7);
        assertEquals("0.0000000227728170", result.get("Property_7"), 2.0E-7);
        assertEquals("0.0000012918248850", result.get("Property_8"), 2.0E-7);
        assertEquals("0.0000012918248850", result.get("Property_9"), 2.0E-7);
        assertEquals("0.0000000032000000", result.get("Property_10"), 2.0E-7);
        assertEquals("0.0000000032000000", result.get("Property_11"), 2.0E-7);
    }

    //It fails in computing the S properties as they are not supported yet
    @Ignore
    @Test
    public void testPRISM_Cell() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "0.5");
        constants.put("N", "50");
        Options options = preparePRISMOptions(CELL_MODEL);
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, CELL_MODEL, CELL_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.4345518395101758", result.get("Property_0"), 2.0E-7);
        assertEquals("0.9986990388753094", result.get("Property_1"), 2.0E-7);
        assertEquals("0.7135893078652826", result.get("Property_2"), 2.0E-7);
        assertEquals("27.519179355139090", result.get("Property_3"), 2.0E-7);
        assertEquals("0.3833839046826002", result.get("Property_5"), 2.0E-7);
        assertEquals("39.782917239421510", result.get("Property_7"), 2.0E-7);
    }

    //It fails in computing the S properties as they are not supported yet
    @Ignore
    @Test
    public void testPRISM_Cluster() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "10");
        constants.put("N", "20");
        Options options = preparePRISMOptions(CLUSTER_MODEL);
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, CLUSTER_MODEL, CLUSTER_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.9995511026598302", result.get("Property_0"), 2.0E-7);
        assertEquals("0.0000020960524843", result.get("Property_2"), 2.0E-7);
        assertEquals(true, result.get("Property_4"));
        assertEquals("0.0000032542950557", result.get("Property_5"), 2.0E-7);
        assertEquals("0.9841068485565170", result.get("Property_7"), 2.0E-7);
        assertEquals("0.3438476666230433", result.get("Property_8"), 2.0E-7);
        assertEquals("0.3101282255567485", result.get("Property_9"), 2.0E-7);
        assertEquals("0.9840380764831946", result.get("Property_10"), 2.0E-7);
        assertEquals("6.5535853675079330", result.get("Property_11"), 2.0E-7);
        assertEquals("0.0000071664386130", result.get("Property_12"), 2.0E-7);
        assertEquals("0.7522776563572369", result.get("Property_14"), 2.0E-7);
    }

    @Test
    public void testPRISM_Coin_2() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "2");
        constants.put("k", "10");
        Options options = preparePRISMOptions(String.format(COIN_MODEL, 2));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(COIN_MODEL, 2), COIN_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("0.3828124943782572", result.get("Property_2"), 2.0E-7);
        assertEquals("0.3828124943782572", result.get("Property_4"), 2.0E-7);
        assertEquals("0.1083333275562509", result.get("Property_6"), 2.0E-7);
        assertEquals("0.0000000000000000", result.get("Property_8"), 2.0E-7);
        assertEquals("0.0000000000000000", result.get("Property_10"), 2.0E-7);
        assertEquals("47.999999984292444", result.get("Property_12"), 2.0E-7);
        assertEquals("74.999999973388130", result.get("Property_14"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_Coin_4() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "2");
        constants.put("k", "10");
        Options options = preparePRISMOptions(String.format(COIN_MODEL, 4));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(COIN_MODEL, 4), COIN_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("0.3173827923614849", result.get("Property_2"), 2.0E-7);
        assertEquals("0.3173827907363523", result.get("Property_4"), 2.0E-7);
        assertEquals("0.2944318290184962", result.get("Property_6"), 2.0E-7);
        assertEquals("0.0000000000000000", result.get("Property_8"), 2.0E-7);
        assertEquals("0.0000000000000000", result.get("Property_10"), 2.0E-7);
        assertEquals("191.99999993151675", result.get("Property_12"), 2.0E-7);
        assertEquals("362.99999988911920", result.get("Property_14"), 2.0E-7);
    }

    //too slow
    @Ignore
    @Test
    public void testPRISM_Coin_6() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "2");
        constants.put("k", "10");
        Options options = preparePRISMOptions(String.format(COIN_MODEL, 6));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(COIN_MODEL, 6), COIN_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("0.2943502833478910", result.get("Property_2"), 2.0E-7);
        assertEquals("0.2943502833478910", result.get("Property_4"), 2.0E-7);
        assertEquals("0.3636447199694461", result.get("Property_6"), 2.0E-7);
        assertEquals("0.0000000000000000", result.get("Property_8"), 2.0E-7);
        assertEquals("0.0000000000000000", result.get("Property_10"), 2.0E-7);
        assertEquals("431.99999989136097", result.get("Property_12"), 2.0E-7);
        assertEquals("866.99999972962950", result.get("Property_14"), 2.0E-7);
    }

    //PRISM fails in generating the results
    @Ignore
    @Test
    public void testPRISM_Coin_8() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "2");
        constants.put("k", "10");
        Options options = preparePRISMOptions(String.format(COIN_MODEL, 8));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(COIN_MODEL, 8), COIN_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("", result.get("Property_2"), 2.0E-7);
        assertEquals("", result.get("Property_4"), 2.0E-7);
        assertEquals("", result.get("Property_6"), 2.0E-7);
        assertEquals("0.0000000000000000", result.get("Property_8"), 2.0E-7);
        assertEquals("0.0000000000000000", result.get("Property_10"), 2.0E-7);
        assertEquals("", result.get("Property_12"), 2.0E-7);
        assertEquals("", result.get("Property_14"), 2.0E-7);
    }

    //PRISM fails in generating the results
    @Ignore
    @Test
    public void testPRISM_Coin_10() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "2");
        constants.put("k", "10");
        Options options = preparePRISMOptions(String.format(COIN_MODEL, 10));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(COIN_MODEL, 10), COIN_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("", result.get("Property_2"), 2.0E-7);
        assertEquals("", result.get("Property_4"), 2.0E-7);
        assertEquals("", result.get("Property_6"), 2.0E-7);
        assertEquals("0.0000000000000000", result.get("Property_8"), 2.0E-7);
        assertEquals("0.0000000000000000", result.get("Property_10"), 2.0E-7);
        assertEquals("", result.get("Property_12"), 2.0E-7);
        assertEquals("", result.get("Property_14"), 2.0E-7);
    }

    @Test
    public void testPRISM_CSMA_2_2() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(CSMA_MODEL,2,2));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(CSMA_MODEL,2,2), CSMA_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("66.999322859407130", result.get("Property_0"), 2.0E-7);
        assertEquals("70.665759761897790", result.get("Property_2"), 2.0E-7);
        assertEquals("34.999999997097290", result.get("Property_4"), 2.0E-7);
        assertEquals("36.666666662763300", result.get("Property_6"), 2.0E-7);
        assertEquals("0.5000000000000000", result.get("Property_8"), 2.0E-7);
        assertEquals("0.5000000000000000", result.get("Property_10"), 2.0E-7);
        assertEquals("0.8750000000000000", result.get("Property_12"), 2.0E-7);
        assertEquals("0.8750000000000000", result.get("Property_14"), 2.0E-7);
        assertEquals("1.0000000000000000", result.get("Property_16"), 2.0E-7);
        assertEquals("1.0000000000000000", result.get("Property_18"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_CSMA_2_4() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(CSMA_MODEL,2,4));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(CSMA_MODEL,2,4), CSMA_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("75.650783290506550", result.get("Property_0"), 2.0E-7);
        assertEquals("78.971274954375760", result.get("Property_2"), 2.0E-7);
        assertEquals("35.366666666423505", result.get("Property_4"), 2.0E-7);
        assertEquals("37.008333332911190", result.get("Property_6"), 2.0E-7);
        assertEquals("0.5000000000000000", result.get("Property_8"), 2.0E-7);
        assertEquals("0.5000000000000000", result.get("Property_10"), 2.0E-7);
        assertEquals("0.9990234375000000", result.get("Property_12"), 2.0E-7);
        assertEquals("0.9990234375000000", result.get("Property_14"), 2.0E-7);
        assertEquals("1.0000000000000000", result.get("Property_16"), 2.0E-7);
        assertEquals("1.0000000000000000", result.get("Property_18"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_CSMA_2_6() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(CSMA_MODEL,2,6));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(CSMA_MODEL,2,6), CSMA_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("84.590412972822500", result.get("Property_0"), 2.0E-7);
        assertEquals("89.263941682646360", result.get("Property_2"), 2.0E-7);
        assertEquals("35.377666170634626", result.get("Property_4"), 2.0E-7);
        assertEquals("37.019298735118600", result.get("Property_6"), 2.0E-7);
        assertEquals("0.5000000000000000", result.get("Property_8"), 2.0E-7);
        assertEquals("0.5000000000000000", result.get("Property_10"), 2.0E-7);
        assertEquals("0.9999995231628418", result.get("Property_12"), 2.0E-7);
        assertEquals("0.9999995231628418", result.get("Property_14"), 2.0E-7);
        assertEquals("1.0000000000000000", result.get("Property_16"), 2.0E-7);
        assertEquals("1.0000000000000000", result.get("Property_18"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_CSMA_3_2() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(CSMA_MODEL,3,2));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(CSMA_MODEL,3,2), CSMA_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("93.624118012828090", result.get("Property_0"), 2.0E-7);
        assertEquals("105.21135383451656", result.get("Property_2"), 2.0E-7);
        assertEquals("30.000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("36.232181777496060", result.get("Property_6"), 2.0E-7);
        assertEquals("0.5859375000000000", result.get("Property_8"), 2.0E-7);
        assertEquals("1.0000000000000000", result.get("Property_10"), 2.0E-7);
        assertEquals("0.4349666248670221", result.get("Property_12"), 2.0E-7);
        assertEquals("0.8596150364756961", result.get("Property_14"), 2.0E-7);
        assertEquals("1.0000000000000000", result.get("Property_16"), 2.0E-7);
        assertEquals("1.0000000000000000", result.get("Property_18"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_CSMA_3_4() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(CSMA_MODEL,3,4));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(CSMA_MODEL,3,4), CSMA_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("107.31147849546767", result.get("Property_0"), 2.0E-7);
        assertEquals("116.81825582915883", result.get("Property_2"), 2.0E-7);
        assertEquals("30.000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("36.288596458474790", result.get("Property_6"), 2.0E-7);
        assertEquals("0.5859375000000000", result.get("Property_8"), 2.0E-7);
        assertEquals("1.0000000000000000", result.get("Property_10"), 2.0E-7);
        assertEquals("0.9046914309266432", result.get("Property_12"), 2.0E-7);
        assertEquals("0.9324469287782889", result.get("Property_14"), 2.0E-7);
        assertEquals("1.0000000000000000", result.get("Property_16"), 2.0E-7);
        assertEquals("1.0000000000000000", result.get("Property_18"), 2.0E-7);
    }

    //Fails by memory with 8GB
    @Ignore
    @Test
    public void testPRISM_CSMA_3_6() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(CSMA_MODEL,3,6));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(CSMA_MODEL,3,6), CSMA_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("136.85667366738778", result.get("Property_0"), 2.0E-7);
        assertEquals("151.80342150757490", result.get("Property_2"), 2.0E-7);
        assertEquals("30.000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("36.291320298493020", result.get("Property_6"), 2.0E-7);
        assertEquals("0.5859375000000000", result.get("Property_8"), 2.0E-7);
        assertEquals("1.0000000000000000", result.get("Property_10"), 2.0E-7);
        assertEquals("0.9971509368293339", result.get("Property_12"), 2.0E-7);
        assertEquals("0.9988350900161440", result.get("Property_14"), 2.0E-7);
        assertEquals("1.0000000000000000", result.get("Property_16"), 2.0E-7);
        assertEquals("1.0000000000000000", result.get("Property_18"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_CSMA_4_2() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(CSMA_MODEL,4,2));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(CSMA_MODEL,4,2), CSMA_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("124.46349552291959", result.get("Property_0"), 2.0E-7);
        assertEquals("142.21216908512903", result.get("Property_2"), 2.0E-7);
        assertEquals("30.000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("38.478929728988575", result.get("Property_6"), 2.0E-7);
        assertEquals("0.3554687500000000", result.get("Property_8"), 2.0E-7);
        assertEquals("1.0000000000000000", result.get("Property_10"), 2.0E-7);
        assertEquals("0.0924505134576788", result.get("Property_12"), 2.0E-7);
        assertEquals("0.7764601488419487", result.get("Property_14"), 2.0E-7);
        assertEquals("1.0000000000000000", result.get("Property_16"), 2.0E-7);
        assertEquals("1.0000000000000000", result.get("Property_18"), 2.0E-7);
    }

    //PRISM fails in generating the results
    @Ignore
    @Test
    public void testPRISM_CSMA_4_4() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(CSMA_MODEL,4,4));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(CSMA_MODEL,4,4), CSMA_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("", result.get("Property_0"), 2.0E-7);
        assertEquals("", result.get("Property_2"), 2.0E-7);
        assertEquals("30.000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("", result.get("Property_6"), 2.0E-7);
        assertEquals("", result.get("Property_8"), 2.0E-7);
        assertEquals("1.0000000000000000", result.get("Property_10"), 2.0E-7);
        assertEquals("", result.get("Property_12"), 2.0E-7);
        assertEquals("", result.get("Property_14"), 2.0E-7);
        assertEquals("1.0000000000000000", result.get("Property_16"), 2.0E-7);
        assertEquals("1.0000000000000000", result.get("Property_18"), 2.0E-7);
    }

    //PRISM fails in generating the results
    @Ignore
    @Test
    public void testPRISM_CSMA_4_6() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(CSMA_MODEL,4,6));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(CSMA_MODEL,4,6), CSMA_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("", result.get("Property_0"), 2.0E-7);
        assertEquals("", result.get("Property_2"), 2.0E-7);
        assertEquals("30.000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("", result.get("Property_6"), 2.0E-7);
        assertEquals("", result.get("Property_8"), 2.0E-7);
        assertEquals("1.0000000000000000", result.get("Property_10"), 2.0E-7);
        assertEquals("", result.get("Property_12"), 2.0E-7);
        assertEquals("", result.get("Property_14"), 2.0E-7);
        assertEquals("1.0000000000000000", result.get("Property_16"), 2.0E-7);
        assertEquals("1.0000000000000000", result.get("Property_18"), 2.0E-7);
    }

    @Test
    public void testPRISM_Dice() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("x", "3");
        Options options = preparePRISMOptions(DICE_MODEL);
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, DICE_MODEL, DICE_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("0.1666666660457849", result.get("Property_2"), 2.0E-7);
        assertEquals("0.1666666660457849", result.get("Property_4"), 2.0E-7);
        assertEquals("3.6666666651144624", result.get("Property_6"), 2.0E-7);
    }

    @Test
    public void testPRISM_TwoDice() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("x", "5");
        Options options = preparePRISMOptions(TWO_DICE_MODEL);
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, TWO_DICE_MODEL, TWO_DICE_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.1111111110221827", result.get("Property_0"), 2.0E-7);
        assertEquals("0.1111111110221827", result.get("Property_2"), 2.0E-7);
        assertEquals("7.3333333319606030", result.get("Property_4"), 2.0E-7);
        assertEquals("7.3333333319606030", result.get("Property_6"), 2.0E-7);
    }

    @Test
    public void testPRISM_DiningCrypt_3() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("k", "0");
        Options options = preparePRISMOptions(String.format(DINING_CRYPT_MODEL, 3));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(DINING_CRYPT_MODEL, 3), DINING_CRYPT_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals(true, result.get("Property_1"));
    }

    @Ignore
    @Test
    public void testPRISM_DiningCrypt_4() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("k", "0");
        Options options = preparePRISMOptions(String.format(DINING_CRYPT_MODEL, 4));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(DINING_CRYPT_MODEL, 4), DINING_CRYPT_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals(true, result.get("Property_1"));
    }

    @Ignore
    @Test
    public void testPRISM_DiningCrypt_5() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("k", "0");
        Options options = preparePRISMOptions(String.format(DINING_CRYPT_MODEL, 5));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(DINING_CRYPT_MODEL, 5), DINING_CRYPT_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals(true, result.get("Property_1"));
    }

    @Ignore
    @Test
    public void testPRISM_DiningCrypt_6() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("k", "0");
        Options options = preparePRISMOptions(String.format(DINING_CRYPT_MODEL, 6));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(DINING_CRYPT_MODEL, 6), DINING_CRYPT_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals(true, result.get("Property_1"));
    }

    @Ignore
    @Test
    public void testPRISM_DiningCrypt_7() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("k", "0");
        Options options = preparePRISMOptions(String.format(DINING_CRYPT_MODEL, 7));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(DINING_CRYPT_MODEL, 7), DINING_CRYPT_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals(true, result.get("Property_1"));
    }

    @Ignore
    @Test
    public void testPRISM_DiningCrypt_8() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("k", "0");
        Options options = preparePRISMOptions(String.format(DINING_CRYPT_MODEL, 8));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(DINING_CRYPT_MODEL, 8), DINING_CRYPT_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals(true, result.get("Property_1"));
    }

    //too slow
    @Ignore
    @Test
    public void testPRISM_DiningCrypt_9() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("k", "0");
        Options options = preparePRISMOptions(String.format(DINING_CRYPT_MODEL, 9));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(DINING_CRYPT_MODEL, 9), DINING_CRYPT_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals(true, result.get("Property_1"));
    }

    //Out of memory with 8GB
    @Ignore
    @Test
    public void testPRISM_DiningCrypt_10() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("k", "0");
        Options options = preparePRISMOptions(String.format(DINING_CRYPT_MODEL, 10));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(DINING_CRYPT_MODEL, 10), DINING_CRYPT_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals(true, result.get("Property_1"));
    }

    //Out of memory with 8GB
    @Ignore
    @Test
    public void testPRISM_DiningCrypt_15() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("k", "0");
        Options options = preparePRISMOptions(String.format(DINING_CRYPT_MODEL, 15));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(DINING_CRYPT_MODEL, 15), DINING_CRYPT_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals(true, result.get("Property_1"));
    }

    @Test
    public void testPRISM_FirewireAbs() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("delay", "36");
        constants.put("fast", "0.5");
        Options options = preparePRISMOptions(FIREWIRE_ABST_MODEL);
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, FIREWIRE_ABST_MODEL, FIREWIRE_ABST_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
    }

    @Ignore
    @Test
    public void testPRISM_FirewireImpl() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("delay", "36");
        constants.put("fast", "0.5");
        Options options = preparePRISMOptions(FIREWIRE_IMPL_MODEL);
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, FIREWIRE_IMPL_MODEL, FIREWIRE_IMPL_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
    }

    //No support yet for S
    @Ignore
    @Test
    public void testPRISM_FMS() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("n", "5");
        Options options = preparePRISMOptions(FMS_MODEL);
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, FMS_MODEL, FMS_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.0731715966472075", result.get("Property_0"), 2.0E-7);
        assertEquals("0.0365858002883267", result.get("Property_2"), 2.0E-7);
        assertEquals("0.0705561729026659", result.get("Property_4"), 2.0E-7);
        assertEquals("0.0146343195377433", result.get("Property_6"), 2.0E-7);
        assertEquals("74.373487613663340", result.get("Property_8"), 2.0E-7);
    }

    //No support yet for S
    @Ignore
    @Test
    public void testPRISM_Kanban() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("t", "4");
        Options options = preparePRISMOptions(KANBAN_MODEL);
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, KANBAN_MODEL, KANBAN_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("3.6464073760255790", result.get("Property_0"), 2.0E-7);
        assertEquals("2.5129835893535350", result.get("Property_2"), 2.0E-7);
        assertEquals("2.5129835893535350", result.get("Property_4"), 2.0E-7);
        assertEquals("1.5032531696976295", result.get("Property_6"), 2.0E-7);
        assertEquals("0.2758897217959078", result.get("Property_8"), 2.0E-7);
    }

    @Test
    public void testPRISM_LeaderAsync_3() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        Options options = preparePRISMOptions(String.format(LEADER_ASYNC_MODEL, 3));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_ASYNC_MODEL, 3), LEADER_ASYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals(true, result.get("Property_1"));
        assertEquals("0.0000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.0000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("3.3333333312534680", result.get("Property_6"), 2.0E-7);
        assertEquals("3.3333333290839740", result.get("Property_8"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_LeaderAsync_4() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        Options options = preparePRISMOptions(String.format(LEADER_ASYNC_MODEL, 4));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_ASYNC_MODEL, 4), LEADER_ASYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals(true, result.get("Property_1"));
        assertEquals("0.0000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.0000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("4.2857142797253770", result.get("Property_6"), 2.0E-7);
        assertEquals("4.2857142809989710", result.get("Property_8"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_LeaderAsync_5() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        Options options = preparePRISMOptions(String.format(LEADER_ASYNC_MODEL, 5));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_ASYNC_MODEL, 5), LEADER_ASYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals(true, result.get("Property_1"));
        assertEquals("0.0000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.0000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("5.0349206289624835", result.get("Property_6"), 2.0E-7);
        assertEquals("5.0349206294145750", result.get("Property_8"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_LeaderAsync_6() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        Options options = preparePRISMOptions(String.format(LEADER_ASYNC_MODEL, 6));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_ASYNC_MODEL, 6), LEADER_ASYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals(true, result.get("Property_1"));
        assertEquals("0.0000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.0000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("5.6497695794664630", result.get("Property_6"), 2.0E-7);
        assertEquals("5.6497695795053600", result.get("Property_8"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_LeaderAsync_7() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        Options options = preparePRISMOptions(String.format(LEADER_ASYNC_MODEL, 7));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_ASYNC_MODEL, 7), LEADER_ASYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals(true, result.get("Property_1"));
        assertEquals("0.0000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.0000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("6.1724981420792430", result.get("Property_6"), 2.0E-7);
        assertEquals("6.1724981422030500", result.get("Property_8"), 2.0E-7);
    }

    //Fails with OutOfMemoryError with 8G
    @Ignore
    @Test
    public void testPRISM_LeaderAsync_8() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        Options options = preparePRISMOptions(String.format(LEADER_ASYNC_MODEL, 8));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_ASYNC_MODEL, 8), LEADER_ASYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals(true, result.get("Property_1"));
        assertEquals("0.0000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.0000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("6.6265929913378920", result.get("Property_6"), 2.0E-7);
        assertEquals("6.6265929912001430", result.get("Property_8"), 2.0E-7);
    }

    //PRISM fails in generating the results
    @Ignore
    @Test
    public void testPRISM_LeaderAsync_9() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        Options options = preparePRISMOptions(String.format(LEADER_ASYNC_MODEL, 9));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_ASYNC_MODEL, 9), LEADER_ASYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals(true, result.get("Property_1"));
        assertEquals("0.0000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.0000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("", result.get("Property_6"), 2.0E-7);
        assertEquals("", result.get("Property_8"), 2.0E-7);
    }

    //PRISM fails in generating the results
    @Ignore
    @Test
    public void testPRISM_LeaderAsync_10() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        Options options = preparePRISMOptions(String.format(LEADER_ASYNC_MODEL, 10));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_ASYNC_MODEL, 10), LEADER_ASYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals(true, result.get("Property_1"));
        assertEquals("0.0000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.0000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("", result.get("Property_6"), 2.0E-7);
        assertEquals("", result.get("Property_8"), 2.0E-7);
    }

    @Test
    public void testPRISM_LeaderSync_3_2() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("L", "1");
        Options options = preparePRISMOptions(String.format(LEADER_SYNC_MODEL, 3, 2));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_SYNC_MODEL, 3, 2), LEADER_SYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("1.0000000000000000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.7500000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("1.3333333330228925", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_LeaderSync_3_3() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("L", "1");
        Options options = preparePRISMOptions(String.format(LEADER_SYNC_MODEL, 3, 3));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_SYNC_MODEL, 3, 3), LEADER_SYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("1.0000000000000000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.8888888888888884", result.get("Property_2"), 2.0E-7);
        assertEquals("1.1249999999641502", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_LeaderSync_3_4() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("L", "1");
        Options options = preparePRISMOptions(String.format(LEADER_SYNC_MODEL, 3, 4));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_SYNC_MODEL, 3, 4), LEADER_SYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("1.0000000000000000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.9375000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("1.0666666666511446", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_LeaderSync_3_5() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("L", "1");
        Options options = preparePRISMOptions(String.format(LEADER_SYNC_MODEL, 3, 5));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_SYNC_MODEL, 3, 5), LEADER_SYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("1.0000000000000000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.9600000000000007", result.get("Property_2"), 2.0E-7);
        assertEquals("1.0416666666598398", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_LeaderSync_3_6() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("L", "1");
        Options options = preparePRISMOptions(String.format(LEADER_SYNC_MODEL, 3, 6));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_SYNC_MODEL, 3, 6), LEADER_SYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("1.0000000000000000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.9722222222222251", result.get("Property_2"), 2.0E-7);
        assertEquals("1.028571428558303", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_LeaderSync_3_8() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("L", "1");
        Options options = preparePRISMOptions(String.format(LEADER_SYNC_MODEL, 3, 8));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_SYNC_MODEL, 3, 8), LEADER_SYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("1.0000000000000000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.9843750000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("1.015873015858233", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_LeaderSync_4_2() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("L", "1");
        Options options = preparePRISMOptions(String.format(LEADER_SYNC_MODEL, 4, 2));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_SYNC_MODEL, 4, 2), LEADER_SYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("1.0000000000000000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.5000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("1.9999999990686774", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_LeaderSync_4_3() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("L", "1");
        Options options = preparePRISMOptions(String.format(LEADER_SYNC_MODEL, 4, 3));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_SYNC_MODEL, 4, 3), LEADER_SYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("1.0000000000000000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.7407407407407418", result.get("Property_2"), 2.0E-7);
        assertEquals("1.3499999998541794", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_LeaderSync_4_4() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("L", "1");
        Options options = preparePRISMOptions(String.format(LEADER_SYNC_MODEL, 4, 4));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_SYNC_MODEL, 4, 4), LEADER_SYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("1.0000000000000000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.8437500000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("1.1851851851459685", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_LeaderSync_4_5() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("L", "1");
        Options options = preparePRISMOptions(String.format(LEADER_SYNC_MODEL, 4, 5));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_SYNC_MODEL, 4, 5), LEADER_SYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("1.0000000000000000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.8960000000000092", result.get("Property_2"), 2.0E-7);
        assertEquals("1.116071428554253", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_LeaderSync_4_6() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("L", "1");
        Options options = preparePRISMOptions(String.format(LEADER_SYNC_MODEL, 4, 6));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_SYNC_MODEL, 4, 6), LEADER_SYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("1.0000000000000000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.9259259259258992", result.get("Property_2"), 2.0E-7);
        assertEquals("1.0799999999274945", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_LeaderSync_4_8() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("L", "1");
        Options options = preparePRISMOptions(String.format(LEADER_SYNC_MODEL, 4, 8));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_SYNC_MODEL, 4, 8), LEADER_SYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("1.0000000000000000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.9570312500000000", result.get("Property_2"), 2.0E-7);
        assertEquals("1.0448979591715250", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_LeaderSync_5_2() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("L", "1");
        Options options = preparePRISMOptions(String.format(LEADER_SYNC_MODEL, 5, 2));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_SYNC_MODEL, 5, 2), LEADER_SYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("1.0000000000000000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.3125000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("3.1999999983029497", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_LeaderSync_5_3() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("L", "1");
        Options options = preparePRISMOptions(String.format(LEADER_SYNC_MODEL, 5, 3));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_SYNC_MODEL, 5, 3), LEADER_SYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("1.0000000000000000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.7407407407407387", result.get("Property_2"), 2.0E-7);
        assertEquals("1.3499999998541794", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_LeaderSync_5_4() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("L", "1");
        Options options = preparePRISMOptions(String.format(LEADER_SYNC_MODEL, 5, 4));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_SYNC_MODEL, 5, 4), LEADER_SYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("1.0000000000000000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.8789062500000000", result.get("Property_2"), 2.0E-7);
        assertEquals("1.1377777776843780", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_LeaderSync_5_5() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("L", "1");
        Options options = preparePRISMOptions(String.format(LEADER_SYNC_MODEL, 5, 5));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_SYNC_MODEL, 5, 5), LEADER_SYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("1.0000000000000000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.9343999999999674", result.get("Property_2"), 2.0E-7);
        assertEquals("1.0702054794279550", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_LeaderSync_5_6() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("L", "1");
        Options options = preparePRISMOptions(String.format(LEADER_SYNC_MODEL, 5, 6));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_SYNC_MODEL, 5, 6), LEADER_SYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("1.0000000000000000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.9606481481480117", result.get("Property_2"), 2.0E-7);
        assertEquals("1.0409638554156673", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_LeaderSync_5_8() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("L", "1");
        Options options = preparePRISMOptions(String.format(LEADER_SYNC_MODEL, 5, 8));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_SYNC_MODEL, 5, 8), LEADER_SYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("1.0000000000000000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.9826660156250000", result.get("Property_2"), 2.0E-7);
        assertEquals("1.0176397515523004", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_LeaderSync_6_2() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("L", "1");
        Options options = preparePRISMOptions(String.format(LEADER_SYNC_MODEL, 6, 2));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_SYNC_MODEL, 6, 2), LEADER_SYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("1.0000000000000000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.1875000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("5.3333333291726870", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_LeaderSync_6_3() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("L", "1");
        Options options = preparePRISMOptions(String.format(LEADER_SYNC_MODEL, 6, 3));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_SYNC_MODEL, 6, 3), LEADER_SYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("1.0000000000000000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.6666666666666646", result.get("Property_2"), 2.0E-7);
        assertEquals("1.4999999995698403", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_LeaderSync_6_4() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("L", "1");
        Options options = preparePRISMOptions(String.format(LEADER_SYNC_MODEL, 6, 4));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_SYNC_MODEL, 6, 4), LEADER_SYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("1.0000000000000000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.8378906250000000", result.get("Property_2"), 2.0E-7);
        assertEquals("1.1934731934093925", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_LeaderSync_6_5() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("L", "1");
        Options options = preparePRISMOptions(String.format(LEADER_SYNC_MODEL, 6, 5));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_SYNC_MODEL, 6, 5), LEADER_SYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("1.0000000000000000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.9100799999997443", result.get("Property_2"), 2.0E-7);
        assertEquals("1.0988045006652094", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_LeaderSync_6_6() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("L", "1");
        Options options = preparePRISMOptions(String.format(LEADER_SYNC_MODEL, 6, 6));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_SYNC_MODEL, 6, 6), LEADER_SYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("1.0000000000000000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.9452160493824413", result.get("Property_2"), 2.0E-7);
        assertEquals("1.0579591836689612", result.get("Property_4"), 2.0E-7);
    }

    // PRISM fails with a SIGSEGV in libdd
    @Ignore
    @Test
    public void testPRISM_LeaderSync_6_8() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("L", "1");
        Options options = preparePRISMOptions(String.format(LEADER_SYNC_MODEL, 6, 8));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(LEADER_SYNC_MODEL, 6, 8), LEADER_SYNC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("1.0000000000000000", result.get("Property_0"), 2.0E-7);
        assertEquals("", result.get("Property_2"), 2.0E-7);
        assertEquals("", result.get("Property_4"), 2.0E-7);
    }

    //No support for S yet
    @Ignore
    @Test
    public void testPRISM_KNACL() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("N1", "10");
        constants.put("N2", "10");
        constants.put("T", "0.002");
        constants.put("i", "0");
        constants.put("N3", "10");
        Options options = preparePRISMOptions(KNACL_MODEL);
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, KNACL_MODEL, KNACL_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.0000917430966457", result.get("Property_0"), 2.0E-7);
        assertEquals("0.0000000000346201", result.get("Property_2"), 2.0E-7);
        assertEquals("43.312255571305280", result.get("Property_4"), 2.0E-7);
        assertEquals("79.479410090523050", result.get("Property_6"), 2.0E-7);
        assertEquals("34.884918271728765", result.get("Property_8"), 2.0E-7);
        assertEquals("79.581494477689590", result.get("Property_10"), 2.0E-7);
    }

    //No support for S yet
    @Ignore
    @Test
    public void testPRISM_NACL() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("N1", "10");
        constants.put("N2", "10");
        constants.put("T", "0.002");
        constants.put("i", "0");
        Options options = preparePRISMOptions(NACL_MODEL);
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, NACL_MODEL, NACL_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.0006596327782790", result.get("Property_0"), 2.0E-7);
        assertEquals("35.045319159719730", result.get("Property_2"), 2.0E-7);
        assertEquals("22.622917765527824", result.get("Property_4"), 2.0E-7);
    }

    //No support for S yet
    @Ignore
    @Test
    public void testPRISM_MC() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("N1", "10");
        constants.put("N2", "10");
        constants.put("T", "0.002");
        constants.put("i", "0");
        Options options = preparePRISMOptions(MC_MODEL);
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, MC_MODEL, MC_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.0000000000000426", result.get("Property_0"), 2.0E-7);
        assertEquals("0.4618841094159586", result.get("Property_2"), 2.0E-7);
        assertEquals("0.3837632729774285", result.get("Property_4"), 2.0E-7);
        assertEquals("84.693801495010620", result.get("Property_6"), 2.0E-7);
        assertEquals("7.3358022127300440", result.get("Property_8"), 2.0E-7);
        assertEquals("7.9703962902230200", result.get("Property_10"), 2.0E-7);
        assertEquals("61.226571638208060", result.get("Property_12"), 2.0E-7);
        assertEquals("4.7815367624215380", result.get("Property_14"), 2.0E-7);
        assertEquals("33.991891599370430", result.get("Property_16"), 2.0E-7);
    }

    @Test
    public void testPRISM_Mutual_3() {
        Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions(String.format(MUTUAL_MODEL, 3));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(MUTUAL_MODEL, 3), MUTUAL_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals(false, result.get("Property_1"));
        assertEquals(false, result.get("Property_2"));
        assertEquals(false, result.get("Property_3"));
    }

    @Ignore
    @Test
    public void testPRISM_Mutual_4() {
        Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions(String.format(MUTUAL_MODEL, 4));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(MUTUAL_MODEL, 4), MUTUAL_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals(false, result.get("Property_1"));
        assertEquals(false, result.get("Property_2"));
        assertEquals(false, result.get("Property_3"));
    }

    @Ignore
    @Test
    public void testPRISM_Mutual_5() {
        Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions(String.format(MUTUAL_MODEL, 5));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(MUTUAL_MODEL, 5), MUTUAL_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals(false, result.get("Property_1"));
        assertEquals(false, result.get("Property_2"));
        assertEquals(false, result.get("Property_3"));
    }

    //Fails with OutOfMemoryError with 8G
    @Ignore
    @Test
    public void testPRISM_Mutual_8() {
        Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions(String.format(MUTUAL_MODEL, 8));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(MUTUAL_MODEL, 8), MUTUAL_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals(false, result.get("Property_1"));
        assertEquals(false, result.get("Property_2"));
        assertEquals(false, result.get("Property_3"));
    }

    //Fails with OutOfMemoryError with 8G
    @Ignore
    @Test
    public void testPRISM_Mutual_10() {
        Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions(String.format(MUTUAL_MODEL, 10));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(MUTUAL_MODEL, 10), MUTUAL_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals(false, result.get("Property_1"));
        assertEquals(false, result.get("Property_2"));
        assertEquals(false, result.get("Property_3"));
    }

    @Test
    public void testPRISM_P2P_4_4() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "1.1");
        Options options = preparePRISMOptions(String.format(PEER2PEER_MODEL, 4, 4));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PEER2PEER_MODEL, 4, 4), PEER2PEER_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.968312472221019", result.get("Property_0"), 2.0E-7);
        assertEquals("0.997522509145874", result.get("Property_2"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_P2P_4_5() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "1.1");
        Options options = preparePRISMOptions(String.format(PEER2PEER_MODEL, 4, 5));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PEER2PEER_MODEL, 4, 5), PEER2PEER_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.960548741225345", result.get("Property_0"), 2.0E-7);
        assertEquals("0.997522509142549", result.get("Property_2"), 2.0E-7);
    }

    //Fails with OutOfMemoryError with 8G
    @Ignore
    @Test
    public void testPRISM_P2P_4_6() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "1.1");
        Options options = preparePRISMOptions(String.format(PEER2PEER_MODEL, 4, 6));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PEER2PEER_MODEL, 4, 6), PEER2PEER_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.952847258251920", result.get("Property_0"), 2.0E-7);
        assertEquals("0.997522509157190", result.get("Property_2"), 2.0E-7);
    }

    //Fails with OutOfMemoryError with 8G
    @Ignore
    @Test
    public void testPRISM_P2P_4_7() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "1.1");
        Options options = preparePRISMOptions(String.format(PEER2PEER_MODEL, 4, 7));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PEER2PEER_MODEL, 4, 7), PEER2PEER_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.945207524172225", result.get("Property_0"), 2.0E-7);
        assertEquals("0.997522509153018", result.get("Property_2"), 2.0E-7);
    }

    // PRISM fails with SIGSEGV in libprismhybrid
    @Ignore
    @Test
    public void testPRISM_P2P_4_8() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "1.1");
        Options options = preparePRISMOptions(String.format(PEER2PEER_MODEL, 4, 8));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PEER2PEER_MODEL, 4, 8), PEER2PEER_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("", result.get("Property_0"), 2.0E-7);
        assertEquals("", result.get("Property_2"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_P2P_5_4() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "1.1");
        Options options = preparePRISMOptions(String.format(PEER2PEER_MODEL, 5, 4));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PEER2PEER_MODEL, 5, 4), PEER2PEER_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.982662490856506", result.get("Property_0"), 2.0E-7);
        assertEquals("0.999042710619681", result.get("Property_2"), 2.0E-7);
    }

    //Fails with OutOfMemoryError with 8G
    @Ignore
    @Test
    public void testPRISM_P2P_5_5() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "1.1");
        Options options = preparePRISMOptions(String.format(PEER2PEER_MODEL, 5, 5));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PEER2PEER_MODEL, 5, 5), PEER2PEER_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.978375285777173", result.get("Property_0"), 2.0E-7);
        assertEquals("0.9990427106169577", result.get("Property_2"), 2.0E-7);
    }

    // PRISM fails by requiring too much memory
    @Ignore
    @Test
    public void testPRISM_P2P_5_6() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "1.1");
        Options options = preparePRISMOptions(String.format(PEER2PEER_MODEL, 5, 6));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PEER2PEER_MODEL, 5, 6), PEER2PEER_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("", result.get("Property_0"), 2.0E-7);
        assertEquals("", result.get("Property_2"), 2.0E-7);
    }

    // PRISM fails by requiring too much memory
    @Ignore
    @Test
    public void testPRISM_P2P_5_7() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "1.1");
        Options options = preparePRISMOptions(String.format(PEER2PEER_MODEL, 5, 7));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PEER2PEER_MODEL, 5, 7), PEER2PEER_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("", result.get("Property_0"), 2.0E-7);
        assertEquals("", result.get("Property_2"), 2.0E-7);
    }

    // PRISM fails by requiring too much memory
    @Ignore
    @Test
    public void testPRISM_P2P_5_8() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "1.1");
        Options options = preparePRISMOptions(String.format(PEER2PEER_MODEL, 5, 8));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PEER2PEER_MODEL, 5, 8), PEER2PEER_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("", result.get("Property_0"), 2.0E-7);
        assertEquals("", result.get("Property_2"), 2.0E-7);
    }

    @Test
    public void testPRISM_Phil_3() {
        Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions(String.format(PHIL_MODEL, 3));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PHIL_MODEL, 3), PHIL_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(false, result.get("Property_0"));
    }

    @Ignore
    @Test
    public void testPRISM_Phil_4() {
        Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions(String.format(PHIL_MODEL, 4));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PHIL_MODEL, 4), PHIL_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(false, result.get("Property_0"));
    }

    @Ignore
    @Test
    public void testPRISM_Phil_5() {
        Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions(String.format(PHIL_MODEL, 5));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PHIL_MODEL, 5), PHIL_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(false, result.get("Property_0"));
    }

    @Ignore
    @Test
    public void testPRISM_Phil_6() {
        Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions(String.format(PHIL_MODEL, 6));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PHIL_MODEL, 6), PHIL_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(false, result.get("Property_0"));
    }

    //Fails with OutOfMemoryError with 8G
    @Ignore
    @Test
    public void testPRISM_Phil_7() {
        Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions(String.format(PHIL_MODEL, 7));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PHIL_MODEL, 7), PHIL_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(false, result.get("Property_0"));
    }

    //Fails with OutOfMemoryError with 8G
    @Ignore
    @Test
    public void testPRISM_Phil_8() {
        Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions(String.format(PHIL_MODEL, 8));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PHIL_MODEL, 8), PHIL_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(false, result.get("Property_0"));
    }

    // PRISM fails by out of memory
    @Ignore
    @Test
    public void testPRISM_Phil_9() {
        Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions(String.format(PHIL_MODEL, 9));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PHIL_MODEL, 9), PHIL_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(false, result.get("Property_0"));
    }

    // PRISM fails by out of memory
    @Ignore
    @Test
    public void testPRISM_Phil_10() {
        Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PHIL_MODEL, 10), PHIL_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(false, result.get("Property_0"));
    }

    // PRISM fails by out of memory
    @Ignore
    @Test
    public void testPRISM_Phil_15() {
        Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions(String.format(PHIL_MODEL, 15));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PHIL_MODEL, 15), PHIL_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(false, result.get("Property_0"));
    }

    // PRISM fails by out of memory
    @Ignore
    @Test
    public void testPRISM_Phil_20() {
        Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions(String.format(PHIL_MODEL, 20));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PHIL_MODEL, 20), PHIL_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(false, result.get("Property_0"));
    }

    // PRISM fails by out of memory
    @Ignore
    @Test
    public void testPRISM_Phil_25() {
        Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions(String.format(PHIL_MODEL, 25));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PHIL_MODEL, 25), PHIL_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(false, result.get("Property_0"));
    }

    // PRISM fails by out of memory
    @Ignore
    @Test
    public void testPRISM_Phil_30() {
        Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions(String.format(PHIL_MODEL, 30));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PHIL_MODEL, 30), PHIL_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(false, result.get("Property_0"));
    }

    @Test
    public void testPRISM_PhilNofair_3() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        Options options = preparePRISMOptions(String.format(PHIL_NOFAIR_MODEL, 3));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PHIL_NOFAIR_MODEL, 3), PHIL_NOFAIR_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("0.000000000000000", result.get("Property_1"), 2.0E-7);
        assertEquals("50.99999997907168", result.get("Property_2"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_PhilNofair_4() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        Options options = preparePRISMOptions(String.format(PHIL_NOFAIR_MODEL, 4));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PHIL_NOFAIR_MODEL, 4), PHIL_NOFAIR_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("0.000000000000000", result.get("Property_1"), 2.0E-7);
        assertEquals("88.99999997307707", result.get("Property_2"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_PhilNofair_5() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        Options options = preparePRISMOptions(String.format(PHIL_NOFAIR_MODEL, 5));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PHIL_NOFAIR_MODEL, 5), PHIL_NOFAIR_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("0.000000000000000", result.get("Property_1"), 2.0E-7);
        assertEquals("148.9999999631877", result.get("Property_2"), 2.0E-7);
    }

    //PRISM fails by out of memory
    @Ignore
    @Test
    public void testPRISM_PhilNofair_6() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        Options options = preparePRISMOptions(String.format(PHIL_NOFAIR_MODEL, 6));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PHIL_NOFAIR_MODEL, 6), PHIL_NOFAIR_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("0.000000000000000", result.get("Property_1"), 2.0E-7);
        assertEquals("", result.get("Property_2"), 2.0E-7);
    }

    //PRISM fails by out of memory
    @Ignore
    @Test
    public void testPRISM_PhilNofair_7() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        Options options = preparePRISMOptions(String.format(PHIL_NOFAIR_MODEL, 7));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PHIL_NOFAIR_MODEL, 7), PHIL_NOFAIR_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("0.000000000000000", result.get("Property_1"), 2.0E-7);
        assertEquals("", result.get("Property_2"), 2.0E-7);
    }

    //PRISM fails by out of memory
    @Ignore
    @Test
    public void testPRISM_PhilNofair_8() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        Options options = preparePRISMOptions(String.format(PHIL_NOFAIR_MODEL, 8));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PHIL_NOFAIR_MODEL, 8), PHIL_NOFAIR_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("0.000000000000000", result.get("Property_1"), 2.0E-7);
        assertEquals("", result.get("Property_2"), 2.0E-7);
    }

    //PRISM fails by out of memory
    @Ignore
    @Test
    public void testPRISM_PhilNofair_9() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        Options options = preparePRISMOptions(String.format(PHIL_NOFAIR_MODEL, 9));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PHIL_NOFAIR_MODEL, 9), PHIL_NOFAIR_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("0.000000000000000", result.get("Property_1"), 2.0E-7);
        assertEquals("", result.get("Property_2"), 2.0E-7);
    }

    //PRISM fails by out of memory
    @Ignore
    @Test
    public void testPRISM_PhilNofair_10() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        Options options = preparePRISMOptions(String.format(PHIL_NOFAIR_MODEL, 10));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PHIL_NOFAIR_MODEL, 10), PHIL_NOFAIR_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("0.000000000000000", result.get("Property_1"), 2.0E-7);
        assertEquals("", result.get("Property_2"), 2.0E-7);
    }

    @Test
    public void testPRISM_PhilLSS_3() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "3");
        constants.put("L", "1");
        Options options = preparePRISMOptions(String.format(PHIL_LSS_MODEL, 3));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PHIL_LSS_MODEL, 3), String.format(PHIL_LSS_PROPERTY, 3));

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("0.000000000000000", result.get("Property_1"), 2.0E-7);
        assertEquals("23.33333333081100", result.get("Property_2"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_PhilLSS_4() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "3");
        constants.put("L", "1");
        Options options = preparePRISMOptions(String.format(PHIL_LSS_MODEL, 4));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(PHIL_LSS_MODEL, 4), String.format(PHIL_LSS_PROPERTY, 4));

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("0.000000000000000", result.get("Property_1"), 2.0E-7);
        assertEquals("28.66666665673256", result.get("Property_2"), 2.0E-7);
    }

    //Support for S still missing
    @Ignore
    @Test
    public void testPRISM_Polling_2() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "50");
        Options options = preparePRISMOptions(String.format(POLLING_MODEL, 2));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(POLLING_MODEL, 2), POLLING_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.102393124417415", result.get("Property_0"), 2.0E-7);
        assertEquals("0.598404583684670", result.get("Property_2"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("0.500003010079941", result.get("Property_6"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_8"), 2.0E-7);
        assertEquals("4.980216688416617", result.get("Property_10"), 2.0E-7);
        assertEquals("14.73886361745613", result.get("Property_12"), 2.0E-7);
    }

    //Support for S still missing
    @Ignore
    @Test
    public void testPRISM_Polling_3() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "50");
        Options options = preparePRISMOptions(String.format(POLLING_MODEL, 3));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(POLLING_MODEL, 3), POLLING_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.130802036614216", result.get("Property_0"), 2.0E-7);
        assertEquals("0.651898472199059", result.get("Property_2"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("0.521454238012840", result.get("Property_6"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_8"), 2.0E-7);
        assertEquals("6.296134136675987", result.get("Property_10"), 2.0E-7);
        assertEquals("10.664889932481362", result.get("Property_12"), 2.0E-7);
    }

    //Support for S still missing
    @Ignore
    @Test
    public void testPRISM_Polling_4() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "50");
        Options options = preparePRISMOptions(String.format(POLLING_MODEL, 4));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(POLLING_MODEL, 4), POLLING_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.141190363935943", result.get("Property_0"), 2.0E-7);
        assertEquals("0.687047708231978", result.get("Property_2"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("0.530928583188138", result.get("Property_6"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_8"), 2.0E-7);
        assertEquals("6.730159718039973", result.get("Property_10"), 2.0E-7);
        assertEquals("8.403605962613959", result.get("Property_12"), 2.0E-7);
    }

    //Support for S still missing
    @Ignore
    @Test
    public void testPRISM_Polling_5() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "50");
        Options options = preparePRISMOptions(String.format(POLLING_MODEL, 5));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(POLLING_MODEL, 5), POLLING_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.144927093830232", result.get("Property_0"), 2.0E-7);
        assertEquals("0.712560754706577", result.get("Property_2"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("0.535740398127223", result.get("Property_6"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_8"), 2.0E-7);
        assertEquals("6.843536349797040", result.get("Property_10"), 2.0E-7);
        assertEquals("6.953211093675620", result.get("Property_12"), 2.0E-7);
    }

    //Support for S still missing
    @Ignore
    @Test
    public void testPRISM_Polling_6() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "50");
        Options options = preparePRISMOptions(String.format(POLLING_MODEL, 6));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(POLLING_MODEL, 6), POLLING_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.145731911533245", result.get("Property_0"), 2.0E-7);
        assertEquals("0.732229789559230", result.get("Property_2"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("0.538348351961578", result.get("Property_6"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_8"), 2.0E-7);
        assertEquals("6.818532250067752", result.get("Property_10"), 2.0E-7);
        assertEquals("5.939263749406717", result.get("Property_12"), 2.0E-7);
    }

    //Support for S still missing
    @Ignore
    @Test
    public void testPRISM_Polling_7() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "50");
        Options options = preparePRISMOptions(String.format(POLLING_MODEL, 7));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(POLLING_MODEL, 7), POLLING_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.145116735337698", result.get("Property_0"), 2.0E-7);
        assertEquals("0.748022855342834", result.get("Property_2"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("0.539786604167911", result.get("Property_6"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_8"), 2.0E-7);
        assertEquals("6.728566684710193", result.get("Property_10"), 2.0E-7);
        assertEquals("5.188449257306369", result.get("Property_12"), 2.0E-7);
    }

    //Support for S still missing
    @Ignore
    @Test
    public void testPRISM_Polling_8() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "50");
        Options options = preparePRISMOptions(String.format(POLLING_MODEL, 8));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(POLLING_MODEL, 8), POLLING_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.143782770331091", result.get("Property_0"), 2.0E-7);
        assertEquals("0.761081981009249", result.get("Property_2"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("0.540554297056581", result.get("Property_6"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_8"), 2.0E-7);
        assertEquals("6.607310426228707", result.get("Property_10"), 2.0E-7);
        assertEquals("4.609038905948243", result.get("Property_12"), 2.0E-7);
    }

    //Support for S still missing
    @Ignore
    @Test
    public void testPRISM_Polling_9() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "50");
        Options options = preparePRISMOptions(String.format(POLLING_MODEL, 9));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(POLLING_MODEL, 9), POLLING_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.142084805118677", result.get("Property_0"), 2.0E-7);
        assertEquals("0.772123674065661", result.get("Property_2"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("0.540917062352555", result.get("Property_6"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_8"), 2.0E-7);
        assertEquals("6.471560375370987", result.get("Property_10"), 2.0E-7);
        assertEquals("4.147757273376394", result.get("Property_12"), 2.0E-7);
    }

    //Support for S still missing
    @Ignore
    @Test
    public void testPRISM_Polling_10() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "50");
        Options options = preparePRISMOptions(String.format(POLLING_MODEL, 10));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(POLLING_MODEL, 10), POLLING_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.140213283202427", result.get("Property_0"), 2.0E-7);
        assertEquals("0.781624286322768", result.get("Property_2"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("0.541025808331040", result.get("Property_6"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_8"), 2.0E-7);
        assertEquals("6.330171853159998", result.get("Property_10"), 2.0E-7);
        assertEquals("3.771466819169002", result.get("Property_12"), 2.0E-7);
    }

    //Support for S still missing
    @Ignore
    @Test
    public void testPRISM_Polling_11() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "50");
        Options options = preparePRISMOptions(String.format(POLLING_MODEL, 11));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(POLLING_MODEL, 11), POLLING_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.138274521224589", result.get("Property_0"), 2.0E-7);
        assertEquals("0.789915020290501", result.get("Property_2"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("0.540971498802727", result.get("Property_6"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_8"), 2.0E-7);
        assertEquals("6.187974984684410", result.get("Property_10"), 2.0E-7);
        assertEquals("3.458438042669371", result.get("Property_12"), 2.0E-7);
    }

    //Support for S still missing
    @Ignore
    @Test
    public void testPRISM_Polling_12() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "50");
        Options options = preparePRISMOptions(String.format(POLLING_MODEL, 12));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(POLLING_MODEL, 12), POLLING_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.136329243693625", result.get("Property_0"), 2.0E-7);
        assertEquals("0.797234542230234", result.get("Property_2"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("0.540810676565671", result.get("Property_6"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_8"), 2.0E-7);
        assertEquals("6.047645330006080", result.get("Property_10"), 2.0E-7);
        assertEquals("3.193810697139760", result.get("Property_12"), 2.0E-7);
    }

    //Support for S still missing
    @Ignore
    @Test
    public void testPRISM_Polling_13() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "50");
        Options options = preparePRISMOptions(String.format(POLLING_MODEL, 13));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(POLLING_MODEL, 13), POLLING_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.134412372329520", result.get("Property_0"), 2.0E-7);
        assertEquals("0.803759937898064", result.get("Property_2"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("0.540580189706649", result.get("Property_6"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_8"), 2.0E-7);
        assertEquals("5.910657545016646", result.get("Property_10"), 2.0E-7);
        assertEquals("2.967067977065328", result.get("Property_12"), 2.0E-7);
    }

    //Support for S still missing
    @Ignore
    @Test
    public void testPRISM_Polling_14() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "50");
        Options options = preparePRISMOptions(String.format(POLLING_MODEL, 14));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(POLLING_MODEL, 14), POLLING_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.132543738937984", result.get("Property_0"), 2.0E-7);
        assertEquals("0.809625841629950", result.get("Property_2"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("0.540304150694250", result.get("Property_6"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_8"), 2.0E-7);
        assertEquals("5.777796754517467", result.get("Property_10"), 2.0E-7);
        assertEquals("2.770551751836221", result.get("Property_12"), 2.0E-7);
    }

    //Support for S still missing
    @Ignore
    @Test
    public void testPRISM_Polling_15() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "50");
        Options options = preparePRISMOptions(String.format(POLLING_MODEL, 15));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(POLLING_MODEL, 15), POLLING_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.130734135591949", result.get("Property_0"), 2.0E-7);
        assertEquals("0.814936745967935", result.get("Property_2"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("0.539999161805059", result.get("Property_6"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_8"), 2.0E-7);
        assertEquals("5.649443845567544", result.get("Property_10"), 2.0E-7);
        assertEquals("2.598549446485856", result.get("Property_12"), 2.0E-7);
    }

    //Support for S still missing
    @Ignore
    @Test
    public void testPRISM_Polling_16() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "50");
        Options options = preparePRISMOptions(String.format(POLLING_MODEL, 16));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(POLLING_MODEL, 16), POLLING_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.128988853132277", result.get("Property_0"), 2.0E-7);
        assertEquals("0.819775193953323", result.get("Property_2"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("0.539676351110205", result.get("Property_6"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_8"), 2.0E-7);
        assertEquals("5.525739701652952", result.get("Property_10"), 2.0E-7);
        assertEquals("2.446710789096847", result.get("Property_12"), 2.0E-7);
    }

    //Support for S still missing
    @Ignore
    @Test
    public void testPRISM_Polling_17() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "50");
        Options options = preparePRISMOptions(String.format(POLLING_MODEL, 17));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(POLLING_MODEL, 17), POLLING_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.127309796176000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.824207411969980", result.get("Property_2"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("0.539343723312115", result.get("Property_6"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_8"), 2.0E-7);
        assertEquals("5.406682133719235", result.get("Property_10"), 2.0E-7);
        assertEquals("2.311663035020443", result.get("Property_12"), 2.0E-7);
    }

    //Support for S still missing
    @Ignore
    @Test
    public void testPRISM_Polling_18() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "50");
        Options options = preparePRISMOptions(String.format(POLLING_MODEL, 18));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(POLLING_MODEL, 18), POLLING_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.127309796176000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.828287236050775", result.get("Property_2"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("0.539006638773423", result.get("Property_6"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_8"), 2.0E-7);
        assertEquals("5.292184230557818", result.get("Property_10"), 2.0E-7);
        assertEquals("2.190749957689833", result.get("Property_12"), 2.0E-7);
    }

    //Support for S still missing
    @Ignore
    @Test
    public void testPRISM_Polling_19() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "50");
        Options options = preparePRISMOptions(String.format(POLLING_MODEL, 19));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(POLLING_MODEL, 19), POLLING_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.124148459225022", result.get("Property_0"), 2.0E-7);
        assertEquals("0.832058960231840", result.get("Property_2"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("0.538669109000543", result.get("Property_6"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_8"), 2.0E-7);
        assertEquals("5.182110028102545", result.get("Property_10"), 2.0E-7);
        assertEquals("2.081850441637898", result.get("Property_12"), 2.0E-7);
    }

    //Support for S still missing
    @Ignore
    @Test
    public void testPRISM_Polling_20() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "50");
        Options options = preparePRISMOptions(String.format(POLLING_MODEL, 20));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(POLLING_MODEL, 20), POLLING_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.122663285369220", result.get("Property_0"), 2.0E-7);
        assertEquals("0.835558675185647", result.get("Property_2"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_4"), 2.0E-7);
        assertEquals("0.538333718194054", result.get("Property_6"), 2.0E-7);
        assertEquals("1.000000000000000", result.get("Property_8"), 2.0E-7);
        assertEquals("5.076296561274019", result.get("Property_10"), 2.0E-7);
        assertEquals("1.983249675258925", result.get("Property_12"), 2.0E-7);
    }

    @Test
    public void testPRISM_Rabin_3() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("k", "5");
        Options options = preparePRISMOptions(String.format(RABIN_MODEL, 3));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(RABIN_MODEL, 3), RABIN_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals(true, result.get("Property_1"));
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.030273437500000", result.get("Property_3"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_Rabin_4() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("k", "5");
        Options options = preparePRISMOptions(String.format(RABIN_MODEL, 4));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(RABIN_MODEL, 4), RABIN_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals(true, result.get("Property_1"));
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.029327392578125", result.get("Property_3"), 2.0E-7);
    }

    // PRISM fails by out of memory
    @Ignore
    @Test
    public void testPRISM_Rabin_5() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("k", "5");
        Options options = preparePRISMOptions(String.format(RABIN_MODEL, 5));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(RABIN_MODEL, 5), RABIN_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals(true, result.get("Property_1"));
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.029109418392181396", result.get("Property_3"), 2.0E-7);
    }

    // PRISM fails by out of memory
    @Ignore
    @Test
    public void testPRISM_Rabin_6() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("k", "5");
        Options options = preparePRISMOptions(String.format(RABIN_MODEL, 6));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(RABIN_MODEL, 6), RABIN_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals(true, result.get("Property_1"));
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.028432623483240604", result.get("Property_3"), 2.0E-7);
    }

    // PRISM fails by out of memory
    @Ignore
    @Test
    public void testPRISM_Rabin_7() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("k", "5");
        Options options = preparePRISMOptions(String.format(RABIN_MODEL, 7));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(RABIN_MODEL, 7), RABIN_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals(true, result.get("Property_1"));
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.027773339752457105", result.get("Property_3"), 2.0E-7);
    }

    // PRISM fails by out of memory
    @Ignore
    @Test
    public void testPRISM_Rabin_8() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("k", "5");
        Options options = preparePRISMOptions(String.format(RABIN_MODEL, 8));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(RABIN_MODEL, 8), RABIN_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals(true, result.get("Property_1"));
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.027131076829618905", result.get("Property_3"), 2.0E-7);
    }

    // PRISM fails by out of memory
    @Ignore
    @Test
    public void testPRISM_Rabin_9() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("k", "5");
        Options options = preparePRISMOptions(String.format(RABIN_MODEL, 9));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(RABIN_MODEL, 9), RABIN_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals(true, result.get("Property_1"));
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.02690346169687173", result.get("Property_3"), 2.0E-7);
    }

    // PRISM fails by out of memory
    @Ignore
    @Test
    public void testPRISM_Rabin_10() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("k", "5");
        Options options = preparePRISMOptions(String.format(RABIN_MODEL, 10));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(RABIN_MODEL, 10), RABIN_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals(true, result.get("Property_1"));
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.026345380743400343", result.get("Property_3"), 2.0E-7);
    }

    @Test
    public void testPRISM_Beauquier_3() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(BEAUQUIER_MODEL, 3));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(BEAUQUIER_MODEL, 3), BEAUQUIER_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("1.999999999985448", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.500000000000000", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_Beauquier_5() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(BEAUQUIER_MODEL, 5));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(BEAUQUIER_MODEL, 5), BEAUQUIER_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("11.91666666613991", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_Beauquier_7() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(BEAUQUIER_MODEL, 7));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(BEAUQUIER_MODEL, 7), BEAUQUIER_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("37.79922368853307", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_Beauquier_9() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(BEAUQUIER_MODEL, 9));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(BEAUQUIER_MODEL, 9), BEAUQUIER_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("84.44595732630478", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_4"), 2.0E-7);
    }

    // Fails by out of memory with 8GB
    @Ignore
    @Test
    public void testPRISM_Beauquier_11() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(BEAUQUIER_MODEL, 11));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(BEAUQUIER_MODEL, 11), BEAUQUIER_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("162.3429071530966", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_4"), 2.0E-7);
    }

    @Test
    public void testPRISM_Herman_3() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(HERMAN_MODEL, 3));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(HERMAN_MODEL, 3), HERMAN_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("1.333333333309250", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.750000000000000", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_Herman_5() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(HERMAN_MODEL, 5));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(HERMAN_MODEL, 5), HERMAN_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("3.199999998614097", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.250000000000000", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_Herman_7() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(HERMAN_MODEL, 7));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(HERMAN_MODEL, 7), HERMAN_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("6.857142853627285", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_Herman_9() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(HERMAN_MODEL, 9));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(HERMAN_MODEL, 9), HERMAN_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("11.99999999309138", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_Herman_11() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "2");
        Options options = preparePRISMOptions(String.format(HERMAN_MODEL, 11));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(HERMAN_MODEL, 11), HERMAN_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("17.45454544306863", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_4"), 2.0E-7);
    }

    //Fails by memory with 8GB
    @Ignore
    @Test
    public void testPRISM_Herman_13() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(HERMAN_MODEL, 13));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(HERMAN_MODEL, 13), HERMAN_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("24.61538459973430", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_4"), 2.0E-7);
    }

    //Fails by memory with 8GB
    @Ignore
    @Test
    public void testPRISM_Herman_15() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(HERMAN_MODEL, 15));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(HERMAN_MODEL, 15), HERMAN_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("33.33333331214026", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_4"), 2.0E-7);
    }

    //Fails by memory with 8GB
    @Ignore
    @Test
    public void testPRISM_Herman_17() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(HERMAN_MODEL, 17));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(HERMAN_MODEL, 17), HERMAN_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("42.35294114861820", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_4"), 2.0E-7);
    }

    //Fails by memory with 8GB
    @Ignore
    @Test
    public void testPRISM_Herman_19() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(HERMAN_MODEL, 19));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(HERMAN_MODEL, 19), HERMAN_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("53.05263154392826", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_4"), 2.0E-7);
    }

    //Fails by memory with 8GB
    @Ignore
    @Test
    public void testPRISM_Herman_21() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(HERMAN_MODEL, 21));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(HERMAN_MODEL, 21), HERMAN_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("65.33333328973458", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_4"), 2.0E-7);
    }

    @Test
    public void testPRISM_IJ_3() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(IJ_MODEL, 3));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(IJ_MODEL, 3), IJ_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("2.999999999068677", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_IJ_4() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(IJ_MODEL, 4));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(IJ_MODEL, 4), IJ_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("5.999999997206032", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_IJ_5() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(IJ_MODEL, 5));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(IJ_MODEL, 5), IJ_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("9.999999996169460", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_IJ_6() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(IJ_MODEL, 6));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(IJ_MODEL, 6), IJ_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("14.99999999374933", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_IJ_7() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(IJ_MODEL, 7));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(IJ_MODEL, 7), IJ_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("20.99999999114089", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_IJ_8() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(IJ_MODEL, 8));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(IJ_MODEL, 8), IJ_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("27.99999998796762", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_IJ_9() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(IJ_MODEL, 9));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(IJ_MODEL, 9), IJ_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("35.99999998528516", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_IJ_10() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(IJ_MODEL, 10));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(IJ_MODEL, 10), IJ_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("44.99999998145056", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_IJ_11() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(IJ_MODEL, 11));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(IJ_MODEL, 11), IJ_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("54.99999997711297", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_IJ_12() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(IJ_MODEL, 12));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(IJ_MODEL, 12), IJ_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("65.99999997222197", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_IJ_13() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(IJ_MODEL, 13));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(IJ_MODEL, 13), IJ_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("77.99999996713541", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_IJ_14() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(IJ_MODEL, 14));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(IJ_MODEL, 14), IJ_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("90.99999996155174", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_IJ_15() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(IJ_MODEL, 15));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(IJ_MODEL, 15), IJ_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("104.9999999554272", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_IJ_16() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(IJ_MODEL, 16));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(IJ_MODEL, 16), IJ_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("119.9999999490785", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_4"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_IJ_17() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(IJ_MODEL, 17));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(IJ_MODEL, 17), IJ_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("135.9999999432225", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_4"), 2.0E-7);
    }

    //Fails by memory with 8GB
    @Ignore
    @Test
    public void testPRISM_IJ_18() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(IJ_MODEL, 18));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(IJ_MODEL, 18), IJ_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("152.9999999358485", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_4"), 2.0E-7);
    }

    //Fails by memory with 8GB
    @Ignore
    @Test
    public void testPRISM_IJ_19() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(IJ_MODEL, 19));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(IJ_MODEL, 19), IJ_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("170.9999999282626", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_4"), 2.0E-7);
    }

    //Fails by memory with 8GB
    @Ignore
    @Test
    public void testPRISM_IJ_20() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(IJ_MODEL, 20));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(IJ_MODEL, 20), IJ_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("189.9999999201628", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_4"), 2.0E-7);
    }

    //Fails with GC overhead with 8G of memory
    @Ignore
    @Test
    public void testPRISM_IJ_21() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("K", "1");
        constants.put("k", "1");
        Options options = preparePRISMOptions(String.format(IJ_MODEL, 21));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(IJ_MODEL, 21), IJ_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("209.9999999115593", result.get("Property_1"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_3"), 2.0E-7);
        assertEquals("0.000000000000000", result.get("Property_4"), 2.0E-7);
    }

    //S not yet supported
    @Ignore
    @Test
    public void testPRISM_Tandem() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("c", "10");
        constants.put("T", "1");
        Options options = preparePRISMOptions(TANDEM_MODEL);
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, TANDEM_MODEL, TANDEM_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("10.78050546163251", result.get("Property_0"), 2.0E-7);
        assertEquals("0.000000018009113", result.get("Property_2"), 2.0E-7);
        assertEquals("0.999999855150179", result.get("Property_4"), 2.0E-7);
        assertEquals("0.981684361081183", result.get("Property_6"), 2.0E-7);
        assertEquals("10.55741515497289", result.get("Property_8"), 2.0E-7);
    }

    @Test
    public void testPRISM_WLAN_0() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("TRANS_TIME_MAX", "10");
        constants.put("k", "2");
        Options options = preparePRISMOptions(String.format(WLAN_MODEL, 0));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(WLAN_MODEL, 0), WLAN_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("1.2248803762736309", result.get("Property_4"), 2.0E-7);
        assertEquals("3791.9047618955374", result.get("Property_6"), 2.0E-7);
        assertEquals("2525.2380952289805", result.get("Property_8"), 2.0E-7);
        assertEquals("3321.5246636721968", result.get("Property_10"), 2.0E-7);
        assertEquals("28000.956937790255", result.get("Property_12"), 2.0E-7);
        assertEquals("20436.36363635464", result.get("Property_14"), 2.0E-7);
        assertEquals("25893.14159291617", result.get("Property_16"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_WLAN_1() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("TRANS_TIME_MAX", "10");
        constants.put("k", "2");
        Options options = preparePRISMOptions(String.format(WLAN_MODEL, 1));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(WLAN_MODEL, 1), WLAN_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("0.000000000000000", result.get("Property_2"), 2.0E-7);
        assertEquals("1.202368135939365", result.get("Property_4"), 2.0E-7);
        assertEquals("3865.137768814543", result.get("Property_6"), 2.0E-7);
        assertEquals("2550.554435481251", result.get("Property_8"), 2.0E-7);
        assertEquals("3352.189316859217", result.get("Property_10"), 2.0E-7);
        assertEquals("228206.3071851428", result.get("Property_12"), 2.0E-7);
        assertEquals("220592.5659311062", result.get("Property_14"), 2.0E-7);
        assertEquals("224850.5432588780", result.get("Property_16"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_WLAN_2() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("TRANS_TIME_MAX", "10");
        constants.put("k", "2");
        Options options = preparePRISMOptions(String.format(WLAN_MODEL, 2));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(WLAN_MODEL, 2), WLAN_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("0.183593750000000", result.get("Property_2"), 2.0E-7);
        assertEquals("1.201459466856799", result.get("Property_4"), 2.0E-7);
        assertEquals("3881.809882707162", result.get("Property_6"), 2.0E-7);
        assertEquals("2558.429348852985", result.get("Property_8"), 2.0E-7);
        assertEquals("3358.971261540538", result.get("Property_10"), 2.0E-7);
        assertEquals("227315.3245991839", result.get("Property_12"), 2.0E-7);
        assertEquals("219692.4904250607", result.get("Property_14"), 2.0E-7);
        assertEquals("223953.0307549974", result.get("Property_16"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_WLAN_3() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("TRANS_TIME_MAX", "10");
        constants.put("k", "2");
        Options options = preparePRISMOptions(String.format(WLAN_MODEL, 3));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(WLAN_MODEL, 3), WLAN_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("0.183593750000000", result.get("Property_2"), 2.0E-7);
        assertEquals("1.201439630215977", result.get("Property_4"), 2.0E-7);
        assertEquals("3883.421961395683", result.get("Property_6"), 2.0E-7);
        assertEquals("2559.225281008821", result.get("Property_8"), 2.0E-7);
        assertEquals("3359.609171176802", result.get("Property_10"), 2.0E-7);
        assertEquals("227297.1717118135", result.get("Property_12"), 2.0E-7);
        assertEquals("219673.4901353628", result.get("Property_14"), 2.0E-7);
        assertEquals("223934.4084838491", result.get("Property_16"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_WLAN_4() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("TRANS_TIME_MAX", "10");
        constants.put("k", "2");
        Options options = preparePRISMOptions(String.format(WLAN_MODEL, 4));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(WLAN_MODEL, 4), WLAN_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("0.183593750000000", result.get("Property_2"), 2.0E-7);
        assertEquals("1.201439405680254", result.get("Property_4"), 2.0E-7);
        assertEquals("3883.497847425573", result.get("Property_6"), 2.0E-7);
        assertEquals("2559.263109424517", result.get("Property_8"), 2.0E-7);
        assertEquals("3359.638565020956", result.get("Property_10"), 2.0E-7);
        assertEquals("227297.0264498327", result.get("Property_12"), 2.0E-7);
        assertEquals("219673.3052596715", result.get("Property_14"), 2.0E-7);
        assertEquals("223934.2427029740", result.get("Property_16"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_WLAN_5() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("TRANS_TIME_MAX", "10");
        constants.put("k", "2");
        Options options = preparePRISMOptions(String.format(WLAN_MODEL, 5));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(WLAN_MODEL, 5), WLAN_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("0.183593750000000", result.get("Property_2"), 2.0E-7);
        assertEquals("1.201439404387566", result.get("Property_4"), 2.0E-7);
        assertEquals("3883.499625409380", result.get("Property_6"), 2.0E-7);
        assertEquals("2559.263997755843", result.get("Property_8"), 2.0E-7);
        assertEquals("3359.639244131045", result.get("Property_10"), 2.0E-7);
        assertEquals("227297.0270118259", result.get("Property_12"), 2.0E-7);
        assertEquals("219673.3048957494", result.get("Property_14"), 2.0E-7);
        assertEquals("223934.2428000538", result.get("Property_16"), 2.0E-7);
    }

    //Too slow
    @Ignore
    @Test
    public void testPRISM_WLAN_6() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("TRANS_TIME_MAX", "10");
        constants.put("k", "2");
        Options options = preparePRISMOptions(String.format(WLAN_MODEL, 6));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(WLAN_MODEL, 6), WLAN_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_0"));
        assertEquals("0.183593750000000", result.get("Property_2"), 2.0E-7);
        assertEquals("1.201439404383811", result.get("Property_4"), 2.0E-7);
        assertEquals("3883.499646229621", result.get("Property_6"), 2.0E-7);
        assertEquals("2559.264008164044", result.get("Property_8"), 2.0E-7);
        assertEquals("3359.639252020041", result.get("Property_10"), 2.0E-7);
        assertEquals("227297.0270297508", result.get("Property_12"), 2.0E-7);
        assertEquals("219673.3049028378", result.get("Property_14"), 2.0E-7);
        assertEquals("223934.2428124442", result.get("Property_16"), 2.0E-7);
    }

    @Test
    public void testPRISM_WLANCollide_0() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("COL", "2");
        constants.put("TRANS_TIME_MAX", "10");
        constants.put("k", "2");
        Options options = preparePRISMOptions(String.format(WLAN_COLLIDE_MODEL, 0));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(WLAN_COLLIDE_MODEL, 0), WLAN_COLLIDE_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.183593750000000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.183593750000000", result.get("Property_1"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_WLANCollide_1() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("COL", "2");
        constants.put("TRANS_TIME_MAX", "10");
        constants.put("k", "2");
        Options options = preparePRISMOptions(String.format(WLAN_COLLIDE_MODEL, 1));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(WLAN_COLLIDE_MODEL, 1), WLAN_COLLIDE_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.183593750000000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.183593750000000", result.get("Property_1"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_WLANCollide_2() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("COL", "2");
        constants.put("TRANS_TIME_MAX", "10");
        constants.put("k", "2");
        Options options = preparePRISMOptions(String.format(WLAN_COLLIDE_MODEL, 2));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(WLAN_COLLIDE_MODEL, 2), WLAN_COLLIDE_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.183593750000000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.183593750000000", result.get("Property_1"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_WLANCollide_3() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("COL", "2");
        constants.put("TRANS_TIME_MAX", "10");
        constants.put("k", "2");
        Options options = preparePRISMOptions(String.format(WLAN_COLLIDE_MODEL, 3));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(WLAN_COLLIDE_MODEL, 3), WLAN_COLLIDE_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.183593750000000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.183593750000000", result.get("Property_1"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_WLANCollide_4() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("COL", "2");
        constants.put("TRANS_TIME_MAX", "10");
        constants.put("k", "2");
        Options options = preparePRISMOptions(String.format(WLAN_COLLIDE_MODEL, 4));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(WLAN_COLLIDE_MODEL, 4), WLAN_COLLIDE_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.183593750000000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.183593750000000", result.get("Property_1"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_WLANCollide_5() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("COL", "2");
        constants.put("TRANS_TIME_MAX", "10");
        constants.put("k", "2");
        Options options = preparePRISMOptions(String.format(WLAN_COLLIDE_MODEL, 5));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(WLAN_COLLIDE_MODEL, 5), WLAN_COLLIDE_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.183593750000000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.183593750000000", result.get("Property_1"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_WLANCollide_6() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("COL", "2");
        constants.put("TRANS_TIME_MAX", "10");
        constants.put("k", "2");
        Options options = preparePRISMOptions(String.format(WLAN_COLLIDE_MODEL, 6));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(WLAN_COLLIDE_MODEL, 6), WLAN_COLLIDE_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.183593750000000", result.get("Property_0"), 2.0E-7);
        assertEquals("0.183593750000000", result.get("Property_1"), 2.0E-7);
    }

    @Test
    public void testPRISM_WLANTimeBounded_0() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("TRANS_TIME_MAX", "10");
        constants.put("DEADLINE", "100");
        Options options = preparePRISMOptions(String.format(WLAN_TIME_BOUNDED_MODEL, 0));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(WLAN_TIME_BOUNDED_MODEL, 0), WLAN_TIME_BOUNDED_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.9090728759765625", result.get("Property_0"), 2.0E-7);
        assertEquals("0.9090728759765625", result.get("Property_1"), 2.0E-7);
        assertEquals("0.9794130921363831", result.get("Property_2"), 2.0E-7);
        assertEquals("0.9794130921363831", result.get("Property_3"), 2.0E-7);
        assertEquals("0.9363574981689453", result.get("Property_4"), 2.0E-7);
        assertEquals("0.9363574981689453", result.get("Property_5"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_WLANTimeBounded_1() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("TRANS_TIME_MAX", "10");
        constants.put("DEADLINE", "100");
        Options options = preparePRISMOptions(String.format(WLAN_TIME_BOUNDED_MODEL, 1));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(WLAN_TIME_BOUNDED_MODEL, 1), WLAN_TIME_BOUNDED_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.846221923828125", result.get("Property_0"), 2.0E-7);
        assertEquals("0.846221923828125", result.get("Property_1"), 2.0E-7);
        assertEquals("0.9844965040683746", result.get("Property_2"), 2.0E-7);
        assertEquals("0.9844965040683746", result.get("Property_3"), 2.0E-7);
        assertEquals("0.9004454463720322", result.get("Property_4"), 2.0E-7);
        assertEquals("0.9004454463720322", result.get("Property_5"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_WLANTimeBounded_2() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("TRANS_TIME_MAX", "10");
        constants.put("DEADLINE", "100");
        Options options = preparePRISMOptions(String.format(WLAN_TIME_BOUNDED_MODEL, 2));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(WLAN_TIME_BOUNDED_MODEL, 2), WLAN_TIME_BOUNDED_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.846221923828125", result.get("Property_0"), 2.0E-7);
        assertEquals("0.846221923828125", result.get("Property_1"), 2.0E-7);
        assertEquals("0.9836365208029747", result.get("Property_2"), 2.0E-7);
        assertEquals("0.9836365208029747", result.get("Property_3"), 2.0E-7);
        assertEquals("0.9002140127122402", result.get("Property_4"), 2.0E-7);
        assertEquals("0.9002140127122402", result.get("Property_5"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_WLANTimeBounded_3() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("TRANS_TIME_MAX", "10");
        constants.put("DEADLINE", "100");
        Options options = preparePRISMOptions(String.format(WLAN_TIME_BOUNDED_MODEL, 3));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(WLAN_TIME_BOUNDED_MODEL, 3), WLAN_TIME_BOUNDED_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.846221923828125", result.get("Property_0"), 2.0E-7);
        assertEquals("0.846221923828125", result.get("Property_1"), 2.0E-7);
        assertEquals("0.9836365208029747", result.get("Property_2"), 2.0E-7);
        assertEquals("0.9836365208029747", result.get("Property_3"), 2.0E-7);
        assertEquals("0.9002140127122402", result.get("Property_4"), 2.0E-7);
        assertEquals("0.9002140127122402", result.get("Property_5"), 2.0E-7);
    }

    @Ignore
    @Test
    public void testPRISM_WLANTimeBounded_4() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("TRANS_TIME_MAX", "10");
        constants.put("DEADLINE", "100");
        Options options = preparePRISMOptions(String.format(WLAN_TIME_BOUNDED_MODEL, 4));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(WLAN_TIME_BOUNDED_MODEL, 4), WLAN_TIME_BOUNDED_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.8462219238281250", result.get("Property_0"), 2.0E-7);
        assertEquals("0.8462219238281250", result.get("Property_1"), 2.0E-7);
        assertEquals("0.9836365208029747", result.get("Property_2"), 2.0E-7);
        assertEquals("0.9836365208029747", result.get("Property_3"), 2.0E-7);
        assertEquals("0.9002140127122402", result.get("Property_4"), 2.0E-7);
        assertEquals("0.9002140127122402", result.get("Property_5"), 2.0E-7);
    }

    // Fails by out of memory with 8GB
    @Ignore
    @Test
    public void testPRISM_WLANTimeBounded_5() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("TRANS_TIME_MAX", "10");
        constants.put("DEADLINE", "100");
        Options options = preparePRISMOptions(String.format(WLAN_TIME_BOUNDED_MODEL, 5));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(WLAN_TIME_BOUNDED_MODEL, 5), WLAN_TIME_BOUNDED_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.8462219238281250", result.get("Property_0"), 2.0E-7);
        assertEquals("0.8462219238281250", result.get("Property_1"), 2.0E-7);
        assertEquals("0.9836365208029747", result.get("Property_2"), 2.0E-7);
        assertEquals("0.9836365208029747", result.get("Property_3"), 2.0E-7);
        assertEquals("0.9002140127122402", result.get("Property_4"), 2.0E-7);
        assertEquals("0.9002140127122402", result.get("Property_5"), 2.0E-7);
    }

    // Fails by out of memory with 8GB
    @Ignore
    @Test
    public void testPRISM_WLANTimeBounded_6() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("TRANS_TIME_MAX", "10");
        constants.put("DEADLINE", "100");
        Options options = preparePRISMOptions(String.format(WLAN_TIME_BOUNDED_MODEL, 6));
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, String.format(WLAN_TIME_BOUNDED_MODEL, 6), WLAN_TIME_BOUNDED_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.8462219238281250", result.get("Property_0"), 2.0E-7);
        assertEquals("0.8462219238281250", result.get("Property_1"), 2.0E-7);
        assertEquals("0.9836365208029747", result.get("Property_2"), 2.0E-7);
        assertEquals("0.9836365208029747", result.get("Property_3"), 2.0E-7);
        assertEquals("0.9002140127122402", result.get("Property_4"), 2.0E-7);
        assertEquals("0.9002140127122402", result.get("Property_5"), 2.0E-7);
    }

    @Test
    public void testPRISM_Zeroconf() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("err", "0");
        constants.put("K", "4");
        constants.put("reset", "true");
        constants.put("N", "1000");
        Options options = preparePRISMOptions(ZEROCONF_MODEL);
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, ZEROCONF_MODEL, ZEROCONF_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.0000038674394349", result.get("Property_0"), 2.0E-7);
        assertEquals("0.0000368412345139", result.get("Property_2"), 2.0E-7);
        assertEquals("13.022753434298028", result.get("Property_4"), 2.0E-7);
        assertEquals("13.054331235308794", result.get("Property_6"), 2.0E-7);
    }

    @Test
    public void testPRISM_ZeroconfTimeBounded() {
        Map<String, Object> constants = new LinkedHashMap<>();
        constants.put("T", "11");
        constants.put("K", "1");
        constants.put("bound", "10");
        constants.put("reset", "true");
        constants.put("N", "1000");
        Options options = preparePRISMOptions(ZEROCONF_TIME_BOUNDED_MODEL);
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModelExported(options, ZEROCONF_TIME_BOUNDED_MODEL, ZEROCONF_TIME_BOUNDED_PROPERTY);

        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.0000234477600190", result.get("Property_0"), 2.0E-7);
        assertEquals("0.0000234477600190", result.get("Property_1"), 2.0E-7);
        assertEquals("0.0142750542031845", result.get("Property_2"), 2.0E-7);
        assertEquals("0.0142750542031845", result.get("Property_3"), 2.0E-7);
    }

}
