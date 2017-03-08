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

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import epmc.error.EPMCException;
import epmc.main.options.UtilOptionsEPMC;
import epmc.messages.OptionsMessages;
import epmc.messages.TimeStampFormatSecondsStarted;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.Model;
import epmc.modelchecker.ModelCheckerResults;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.prism.model.ModelPRISM;
import epmc.value.Value;

import static epmc.ModelNamesPRISM.*;
import static epmc.modelchecker.TestHelper.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tests for model checking of PRISM models; based on the one for JANI models
 * 
 * @author Andrea Turrini
 * @author Ernst Moritz Hahn
 */
public final class CheckExplicitTest {
	/** Location of plugin directory in file system. */
//    private final static String PLUGIN_DIR = System.getProperty("user.dir") + "/target/classes/";

    /**
     * Set up the tests.
     */
    @BeforeClass
    public static void initialise() {
        prepare();
    }

    /**
     * Prepare options including loading PRISM plugin.
     * 
     * @return options usable for PRISM model analysis
     * @throws EPMCException thrown in case problem occurs
     */
    private final static Options preparePRISMOptions() throws EPMCException {
        Options options = UtilOptionsEPMC.newOptions();
        prepareOptions(options, LogType.TRANSLATE, ModelPRISM.IDENTIFIER);
//        options.set(OptionsPlugin.PLUGIN, PLUGIN_DIR);
        options.set(OptionsMessages.TIME_STAMPS, TimeStampFormatSecondsStarted.class);
        options.set(OptionsMessages.TRANSLATE_MESSAGES, "false");
        return options;
    }
    
    
    @Test
    public void testPRISMTest() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("COL", "2");
		constants.put("TRANS_TIME_MAX", "10");
    	constants.put("k", "2");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, "prism");
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, System.getProperty("user.home") + "/test.prism", System.getProperty("user.home") + "/test.prop");
        
        ModelCheckerResults result = computeResults(model);
        int i = 0;
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    @Test
    public void testPRISM_BRP() throws EPMCException {
    	// TODO suppport "deadlock" label
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("MAX", "4");
    	constants.put("N", "64");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, BRP_MODEL, BRP_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals("0.0000000000000000", result.get("P=?[ F srep=1 & rrep=3 & recv ]"), 1E-9);
        assertEquals("0.0000000000000000", result.get("P=?[ F srep=3 & !(rrep=3) & recv ]"), 1E-9);
        assertEquals("0.0000015032933912", result.get("P=?[ F s=5 ]"), 1E-9);
        assertEquals("0.0000000227728170", result.get("P=?[ F s=5 & srep=2 ]"), 1E-9);
        assertEquals("0.0000012918248850", result.get("P=?[ F s=5 & srep=1 & i>8 ]"), 1E-9);
        assertEquals("0.0000000032000000", result.get("P=?[ F !(srep=0) & !recv ]"), 1E-9);
    }

    @Test
    public void testPRISM_Cell() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "0.5");
    	constants.put("N", "50");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, CELL_MODEL, CELL_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals("0.0000000000000000", result.get("P=?[ F srep=1 & rrep=3 & recv ]"), 1E-9);
    }

    @Test
    public void testPRISM_Cluster() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "10");
    	constants.put("N", "20");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, CLUSTER_MODEL, CLUSTER_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals("0.9995511026598302", result.get("S=? [ \"premium\" ]"), 1E-9);
        assertEquals("0.0000020960524843", result.get("S=? [ !\"minimum\" ]"), 1E-9);
        assertEquals(true, result.get("P>=1 [ true U \"premium\" ]"));
        assertEquals("0.0000032542950557", result.get("P=? [ true U<=T !\"minimum\" ]"), 1E-9);
        assertEquals("0.9841068485565170", result.get("P=? [ true U[T,T] !\"minimum\" {!\"minimum\"}{max} ]"), 1E-9);
        assertEquals("0.3438476666230433", result.get("P=? [ true U<=T \"premium\" {\"minimum\"}{min} ]"), 1E-9);
        assertEquals("0.3101282255567485", result.get("P=? [ \"minimum\" U<=T \"premium\" {\"minimum\"}{min} ]"), 1E-9);
        assertEquals("0.9840380764831946", result.get("P=? [ !\"minimum\" U>=T \"minimum\" {!\"minimum\"}{max} ]"), 1E-9);
        assertEquals("6.5535853675079330", result.get("R{\"percent_op\"}=? [ I=T {!\"minimum\"}{min} ]"), 1E-9);
        assertEquals("0.0000071664386130", result.get("R{\"time_not_min\"}=? [ C<=T ]"), 1E-9);
        assertEquals("0.7522776563572369", result.get("R{\"num_repairs\"}=? [ C<=T ]"), 1E-9);
    }

    @Test
    public void testPRISM_Coin_2() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "2");
    	constants.put("k", "10");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(COIN_MODEL, 2), COIN_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(true, result.get("P>=1 [ F \"finished\" ]"));
        assertEquals("0.3828124943782572", result.get("Pmin=? [ F \"finished\"&\"all_coins_equal_0\" ]"), 1E-9);
        assertEquals("0.3828124943782572", result.get("Pmin=? [ F \"finished\"&\"all_coins_equal_1\" ]"), 1E-9);
        assertEquals("0.1083333275562509", result.get("Pmax=? [ F \"finished\"&!\"agree\" ]"), 1E-9);
        assertEquals("0.0000000000000000", result.get("Pmin=? [ F<=k \"finished\" ]"), 1E-9);
        assertEquals("0.0000000000000000", result.get("Pmax=? [ F<=k \"finished\" ]"), 1E-9);
        assertEquals("47.999999984292444", result.get("R{\"steps\"}min=? [ F \"finished\" ]"), 1E-9);
        assertEquals("74.99999997338813", result.get("R{\"steps\"}max=? [ F \"finished\" ]"), 1E-9);
    }

    @Test
    public void testPRISM_Coin_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "2");
    	constants.put("k", "10");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(COIN_MODEL, 4), COIN_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(true, result.get("P>=1 [ F \"finished\" ]"));
        assertEquals("0.3173827923614849", result.get("Pmin=? [ F \"finished\"&\"all_coins_equal_0\" ]"), 1E-9);
        assertEquals("0.3173827907363523", result.get("Pmin=? [ F \"finished\"&\"all_coins_equal_1\" ]"), 1E-9);
        assertEquals("0.2944318155449189", result.get("Pmax=? [ F \"finished\"&!\"agree\" ]"), 1E-9);
        assertEquals("0.0000000000000000", result.get("Pmin=? [ F<=k \"finished\" ]"), 1E-9);
        assertEquals("0.0000000000000000", result.get("Pmax=? [ F<=k \"finished\" ]"), 1E-9);
        assertEquals("191.99999993151675", result.get("R{\"steps\"}min=? [ F \"finished\" ]"), 1E-9);
        assertEquals("362.9999998576933", result.get("R{\"steps\"}max=? [ F \"finished\" ]"), 1E-9);
    }

    @Test
    public void testPRISM_Coin_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "2");
    	constants.put("k", "10");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(COIN_MODEL, 6), COIN_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(true, result.get("P>=1 [ F \"finished\" ]"));
        assertEquals("0.2943502615405403", result.get("Pmin=? [ F \"finished\"&\"all_coins_equal_0\" ]"), 1E-9);
        assertEquals("0.2943502601795070", result.get("Pmin=? [ F \"finished\"&\"all_coins_equal_1\" ]"), 1E-9);
        assertEquals("0.3636446641239390", result.get("Pmax=? [ F \"finished\"&!\"agree\" ]"), 1E-9);
        assertEquals("0.0000000000000000", result.get("Pmin=? [ F<=k \"finished\" ]"), 1E-9);
        assertEquals("0.0000000000000000", result.get("Pmax=? [ F<=k \"finished\" ]"), 1E-9);
        assertEquals("431.99999984048554", result.get("R{\"steps\"}min=? [ F \"finished\" ]"), 1E-9);
        assertEquals("866.9999996574281", result.get("R{\"steps\"}max=? [ F \"finished\" ]"), 1E-9);
    }
    
    //PRISM fails in generating the results
    @Ignore
    @Test
    public void testPRISM_Coin_8() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "2");
    	constants.put("k", "10");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(COIN_MODEL, 8), COIN_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(true, result.get("P>=1 [ F \"finished\" ]"));
        assertEquals("", result.get("Pmin=? [ F \"finished\"&\"all_coins_equal_0\" ]"), 1E-9);
        assertEquals("", result.get("Pmin=? [ F \"finished\"&\"all_coins_equal_1\" ]"), 1E-9);
        assertEquals("", result.get("Pmax=? [ F \"finished\"&!\"agree\" ]"), 1E-9);
        assertEquals("0.0000000000000000", result.get("Pmin=? [ F<=k \"finished\" ]"), 1E-9);
        assertEquals("0.0000000000000000", result.get("Pmax=? [ F<=k \"finished\" ]"), 1E-9);
        assertEquals("", result.get("R{\"steps\"}min=? [ F \"finished\" ]"), 1E-9);
        assertEquals("", result.get("R{\"steps\"}max=? [ F \"finished\" ]"), 1E-9);
    }

    //PRISM fails in generating the results
    @Ignore
    @Test
    public void testPRISM_Coin_10() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "2");
    	constants.put("k", "10");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(COIN_MODEL, 10), COIN_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(true, result.get("P>=1 [ F \"finished\" ]"));
        assertEquals("", result.get("Pmin=? [ F \"finished\"&\"all_coins_equal_0\" ]"), 1E-9);
        assertEquals("", result.get("Pmin=? [ F \"finished\"&\"all_coins_equal_1\" ]"), 1E-9);
        assertEquals("", result.get("Pmax=? [ F \"finished\"&!\"agree\" ]"), 1E-9);
        assertEquals("0.0000000000000000", result.get("Pmin=? [ F<=k \"finished\" ]"), 1E-9);
        assertEquals("0.0000000000000000", result.get("Pmax=? [ F<=k \"finished\" ]"), 1E-9);
        assertEquals("", result.get("R{\"steps\"}min=? [ F \"finished\" ]"), 1E-9);
        assertEquals("", result.get("R{\"steps\"}max=? [ F \"finished\" ]"), 1E-9);
    }

    @Test
    public void testPRISM_CSMA_2_2() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(CSMA_MODEL,2,2), CSMA_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals("66.999322859407130", result.get("R{\"time\"}min=? [ F \"all_delivered\" ]"), 1E-9);
        assertEquals("70.665759761897790", result.get("R{\"time\"}max=? [ F \"all_delivered\" ]"), 1E-9);
        assertEquals("34.999999997097290", result.get("Rmin=? [ F \"one_delivered\" ]"), 1E-9);
        assertEquals("36.666666662763300", result.get("Rmax=? [ F \"one_delivered\" ]"), 1E-9);
        assertEquals("0.5000000000000000", result.get("Pmin=? [ F min_backoff_after_success<=k ]"), 1E-9);
        assertEquals("0.5000000000000000", result.get("Pmax=? [ F min_backoff_after_success<=k ]"), 1E-9);
        assertEquals("0.8750000000000000", result.get("Pmin=? [ !\"collision_max_backoff\" U \"all_delivered\" ]"), 1E-9);
        assertEquals("0.8750000000000000", result.get("Pmax=? [ !\"collision_max_backoff\" U \"all_delivered\" ]"), 1E-9);
        assertEquals("1.0000000000000000", result.get("Pmin=? [ F max_collisions>=k ]"), 1E-9);
        assertEquals("1.0000000000000000", result.get("Pmax=? [ F max_collisions>=k ]"), 1E-9);
    }

    @Test
    public void testPRISM_CSMA_2_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "1");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(CSMA_MODEL,2,4), CSMA_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals("75.650783290506550", result.get("R{\"time\"}min=? [ F \"all_delivered\" ]"), 1E-9);
        assertEquals("78.971274954375760", result.get("R{\"time\"}max=? [ F \"all_delivered\" ]"), 1E-9);
        assertEquals("35.366666666423505", result.get("Rmin=? [ F \"one_delivered\" ]"), 1E-9);
        assertEquals("37.008333332911190", result.get("Rmax=? [ F \"one_delivered\" ]"), 1E-9);
        assertEquals("0.5000000000000000", result.get("Pmin=? [ F min_backoff_after_success<=k ]"), 1E-9);
        assertEquals("0.5000000000000000", result.get("Pmax=? [ F min_backoff_after_success<=k ]"), 1E-9);
        assertEquals("0.9990234375000000", result.get("Pmin=? [ !\"collision_max_backoff\" U \"all_delivered\" ]"), 1E-9);
        assertEquals("0.9990234375000000", result.get("Pmax=? [ !\"collision_max_backoff\" U \"all_delivered\" ]"), 1E-9);
        assertEquals("1.0000000000000000", result.get("Pmin=? [ F max_collisions>=k ]"), 1E-9);
        assertEquals("1.0000000000000000", result.get("Pmax=? [ F max_collisions>=k ]"), 1E-9);
    }

    @Test
    public void testPRISM_CSMA_2_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(CSMA_MODEL,2,6), CSMA_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals("84.590412972822500", result.get("R{\"time\"}min=? [ F \"all_delivered\" ]"), 1E-9);
        assertEquals("89.263941682646360", result.get("R{\"time\"}max=? [ F \"all_delivered\" ]"), 1E-9);
        assertEquals("35.377666170634626", result.get("Rmin=? [ F \"one_delivered\" ]"), 1E-9);
        assertEquals("37.0192987351186", result.get("Rmax=? [ F \"one_delivered\" ]"), 1E-9);
        assertEquals("0.5000000000000000", result.get("Pmin=? [ F min_backoff_after_success<=k ]"), 1E-9);
        assertEquals("0.5000000000000000", result.get("Pmax=? [ F min_backoff_after_success<=k ]"), 1E-9);
        assertEquals("0.9999995231628418", result.get("Pmin=? [ !\"collision_max_backoff\" U \"all_delivered\" ]"), 1E-9);
        assertEquals("0.9999995231628418", result.get("Pmax=? [ !\"collision_max_backoff\" U \"all_delivered\" ]"), 1E-9);
        assertEquals("1.0000000000000000", result.get("Pmin=? [ F max_collisions>=k ]"), 1E-9);
        assertEquals("1.0000000000000000", result.get("Pmax=? [ F max_collisions>=k ]"), 1E-9);
    }

    @Test
    public void testPRISM_CSMA_3_2() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "1");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(CSMA_MODEL,3,2), CSMA_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals("93.624118012828090", result.get("R{\"time\"}min=? [ F \"all_delivered\" ]"), 1E-9);
        assertEquals("105.21135383451656", result.get("R{\"time\"}max=? [ F \"all_delivered\" ]"), 1E-9);
        assertEquals("30.000000000000000", result.get("Rmin=? [ F \"one_delivered\" ]"), 1E-9);
        assertEquals("36.232181777496060", result.get("Rmax=? [ F \"one_delivered\" ]"), 1E-9);
        assertEquals("0.5859375000000000", result.get("Pmin=? [ F min_backoff_after_success<=k ]"), 1E-9);
        assertEquals("1.0000000000000000", result.get("Pmax=? [ F min_backoff_after_success<=k ]"), 1E-9);
        assertEquals("0.4349666248670221", result.get("Pmin=? [ !\"collision_max_backoff\" U \"all_delivered\" ]"), 1E-9);
        assertEquals("0.8596150364756961", result.get("Pmax=? [ !\"collision_max_backoff\" U \"all_delivered\" ]"), 1E-9);
        assertEquals("1.0000000000000000", result.get("Pmin=? [ F max_collisions>=k ]"), 1E-9);
        assertEquals("1.0000000000000000", result.get("Pmax=? [ F max_collisions>=k ]"), 1E-9);
    }

    @Test
    public void testPRISM_CSMA_3_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "1");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(CSMA_MODEL,3,4), CSMA_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals("107.31147849546767", result.get("R{\"time\"}min=? [ F \"all_delivered\" ]"), 1E-9);
        assertEquals("116.81825582915883", result.get("R{\"time\"}max=? [ F \"all_delivered\" ]"), 1E-9);
        assertEquals("30.000000000000000", result.get("Rmin=? [ F \"one_delivered\" ]"), 1E-9);
        assertEquals("36.288596458474790", result.get("Rmax=? [ F \"one_delivered\" ]"), 1E-9);
        assertEquals("0.5859375000000000", result.get("Pmin=? [ F min_backoff_after_success<=k ]"), 1E-9);
        assertEquals("1.0000000000000000", result.get("Pmax=? [ F min_backoff_after_success<=k ]"), 1E-9);
        assertEquals("0.9046914309266432", result.get("Pmin=? [ !\"collision_max_backoff\" U \"all_delivered\" ]"), 1E-9);
        assertEquals("0.9324469287782889", result.get("Pmax=? [ !\"collision_max_backoff\" U \"all_delivered\" ]"), 1E-9);
        assertEquals("1.0000000000000000", result.get("Pmin=? [ F max_collisions>=k ]"), 1E-9);
        assertEquals("1.0000000000000000", result.get("Pmax=? [ F max_collisions>=k ]"), 1E-9);
    }

    @Test
    public void testPRISM_CSMA_3_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "1");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(CSMA_MODEL,3,6), CSMA_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals("136.85667366738778", result.get("R{\"time\"}min=? [ F \"all_delivered\" ]"), 1E-9);
        assertEquals("151.80342150757490", result.get("R{\"time\"}max=? [ F \"all_delivered\" ]"), 1E-9);
        assertEquals("30.000000000000000", result.get("Rmin=? [ F \"one_delivered\" ]"), 1E-9);
        assertEquals("36.291320298493020", result.get("Rmax=? [ F \"one_delivered\" ]"), 1E-9);
        assertEquals("0.5859375000000000", result.get("Pmin=? [ F min_backoff_after_success<=k ]"), 1E-9);
        assertEquals("1.0000000000000000", result.get("Pmax=? [ F min_backoff_after_success<=k ]"), 1E-9);
        assertEquals("", result.get("Pmin=? [ !\"collision_max_backoff\" U \"all_delivered\" ]"), 1E-9);
        assertEquals("", result.get("Pmax=? [ !\"collision_max_backoff\" U \"all_delivered\" ]"), 1E-9);
        assertEquals("1.0000000000000000", result.get("Pmin=? [ F max_collisions>=k ]"), 1E-9);
        assertEquals("1.0000000000000000", result.get("Pmax=? [ F max_collisions>=k ]"), 1E-9);
    }

    @Test
    public void testPRISM_CSMA_4_2() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "1");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(CSMA_MODEL,4,2), CSMA_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    @Test
    public void testPRISM_CSMA_4_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(CSMA_MODEL,4,4), CSMA_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    @Test
    public void testPRISM_CSMA_4_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(CSMA_MODEL,4,6), CSMA_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Dice() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("x", "3");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, DICE_MODEL, DICE_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_TwoDice() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("x", "5");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, TWO_DICE_MODEL, TWO_DICE_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    @Test
    public void testPRISM_DiningCrypt_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "0");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(DINING_CRYPT_MODEL, 3), DINING_CRYPT_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(true, result.get("Property_dining_crypt3_0"));
        assertEquals(true, result.get("Property_dining_crypt3_1"));
    }

    @Test
    public void testPRISM_DiningCrypt_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "0");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(DINING_CRYPT_MODEL, 4), DINING_CRYPT_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(true, result.get("Property_dining_crypt4_0"));
        assertEquals(true, result.get("Property_dining_crypt4_1"));
    }

    @Test
    public void testPRISM_DiningCrypt_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "0");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(DINING_CRYPT_MODEL, 5), DINING_CRYPT_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(true, result.get("Property_dining_crypt5_0"));
        assertEquals(true, result.get("Property_dining_crypt5_1"));
    }

    @Test
    public void testPRISM_DiningCrypt_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "0");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(DINING_CRYPT_MODEL, 6), DINING_CRYPT_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(true, result.get("Property_dining_crypt6_0"));
        assertEquals(true, result.get("Property_dining_crypt6_1"));
    }

    @Test
    public void testPRISM_DiningCrypt_7() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "0");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(DINING_CRYPT_MODEL, 7), DINING_CRYPT_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(true, result.get("Property_dining_crypt7_0"));
        assertEquals(true, result.get("Property_dining_crypt7_1"));
    }

    @Test
    public void testPRISM_DiningCrypt_8() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "0");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(DINING_CRYPT_MODEL, 8), DINING_CRYPT_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(true, result.get("Property_dining_crypt8_0"));
        assertEquals(true, result.get("Property_dining_crypt8_1"));
    }

    @Ignore
    @Test
    public void testPRISM_DiningCrypt_9() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "0");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(DINING_CRYPT_MODEL, 9), DINING_CRYPT_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(true, result.get("Property_dining_crypt9_0"));
        assertEquals(true, result.get("Property_dining_crypt9_1"));
    }

    @Ignore
    @Test
    public void testPRISM_DiningCrypt_10() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "0");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(DINING_CRYPT_MODEL, 10), DINING_CRYPT_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(true, result.get("Property_dining_crypt10_0"));
        assertEquals(true, result.get("Property_dining_crypt10_1"));
    }

    @Ignore
    @Test
    public void testPRISM_DiningCrypt_15() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "0");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(DINING_CRYPT_MODEL, 15), DINING_CRYPT_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(true, result.get("Property_dining_crypt15_0"));
        assertEquals(true, result.get("Property_dining_crypt15_1"));
    }

    @Test
    public void testPRISM_FireweireAbs() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("delay", "36");
    	constants.put("fast", "0.5");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, FIREWIRE_ABST_MODEL, FIREWIRE_ABST_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(true, result.get("Property_firewire_0"));
    }

    @Test
    public void testPRISM_FireweireImpl() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("delay", "36");
    	constants.put("fast", "0.5");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, FIREWIRE_IMPL_MODEL, FIREWIRE_IMPL_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(true, result.get("Property_firewire_1"));
    }

//    Fails on computing steady state probabilities
    @Ignore
    @Test
    public void testPRISM_FMS() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("n", "5");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, FMS_MODEL, FMS_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails on computing steady state probabilities
    @Ignore
    @Test
    public void testPRISM_Kanban() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("t", "4");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, KANBAN_MODEL, KANBAN_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_LeaderAsync_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_ASYNC_MODEL, 3), LEADER_ASYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_LeaderAsync_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_ASYNC_MODEL, 4), LEADER_ASYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_LeaderAsync_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_ASYNC_MODEL, 5), LEADER_ASYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_LeaderAsync_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_ASYNC_MODEL, 6), LEADER_ASYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_LeaderAsync_7() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_ASYNC_MODEL, 7), LEADER_ASYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_LeaderAsync_8() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_ASYNC_MODEL, 8), LEADER_ASYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_LeaderAsync_9() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_ASYNC_MODEL, 9), LEADER_ASYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_LeaderAsync_10() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_ASYNC_MODEL, 10), LEADER_ASYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
//    @Ignore
    @Test
    public void testPRISM_LeaderSync_3_2() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_SYNC_MODEL, 3, 2), LEADER_SYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_LeaderSync_3_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_SYNC_MODEL, 3, 3), LEADER_SYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_LeaderSync_3_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_SYNC_MODEL, 3, 4), LEADER_SYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_LeaderSync_3_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_SYNC_MODEL, 3, 5), LEADER_SYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_LeaderSync_3_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_SYNC_MODEL, 3, 6), LEADER_SYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_LeaderSync_3_8() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_SYNC_MODEL, 3, 8), LEADER_SYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_LeaderSync_4_2() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_SYNC_MODEL, 4, 2), LEADER_SYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_LeaderSync_4_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_SYNC_MODEL, 4, 3), LEADER_SYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_LeaderSync_4_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_SYNC_MODEL, 4, 4), LEADER_SYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_LeaderSync_4_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_SYNC_MODEL, 4, 5), LEADER_SYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_LeaderSync_4_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_SYNC_MODEL, 4, 6), LEADER_SYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_LeaderSync_4_8() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_SYNC_MODEL, 4, 8), LEADER_SYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_LeaderSync_5_2() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_SYNC_MODEL, 5, 2), LEADER_SYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_LeaderSync_5_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_SYNC_MODEL, 5, 3), LEADER_SYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_LeaderSync_5_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_SYNC_MODEL, 5, 4), LEADER_SYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_LeaderSync_5_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_SYNC_MODEL, 5, 5), LEADER_SYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_LeaderSync_5_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_SYNC_MODEL, 5, 6), LEADER_SYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_LeaderSync_5_8() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_SYNC_MODEL, 5, 8), LEADER_SYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_LeaderSync_6_2() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_SYNC_MODEL, 6, 2), LEADER_SYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_LeaderSync_6_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_SYNC_MODEL, 6, 3), LEADER_SYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_LeaderSync_6_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_SYNC_MODEL, 6, 4), LEADER_SYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_LeaderSync_6_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_SYNC_MODEL, 6, 5), LEADER_SYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_LeaderSync_6_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_SYNC_MODEL, 6, 6), LEADER_SYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_LeaderSync_6_8() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(LEADER_SYNC_MODEL, 6, 8), LEADER_SYNC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_KNACL() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("N1", "10");
    	constants.put("N2", "10");
    	constants.put("T", "0.002");
    	constants.put("i", "0");
    	constants.put("N3", "10");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, KNACL_MODEL, KNACL_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_NACL() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("N1", "10");
    	constants.put("N2", "10");
    	constants.put("T", "0.002");
    	constants.put("i", "0");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, NACL_MODEL, NACL_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_MC() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("N1", "10");
    	constants.put("N2", "10");
    	constants.put("T", "0.002");
    	constants.put("i", "0");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, MC_MODEL, MC_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    @Test
    public void testPRISM_Mutual_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(MUTUAL_MODEL, 3), MUTUAL_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(true, result.get("Property_mutual3_0"));
        assertEquals(false, result.get("Property_mutual3_1"));
        assertEquals(false, result.get("Property_mutual3_2"));
        assertEquals(false, result.get("Property_mutual3_3"));
    }

    @Test
    public void testPRISM_Mutual_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(MUTUAL_MODEL, 4), MUTUAL_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(true, result.get("Property_mutual4_0"));
        assertEquals(false, result.get("Property_mutual4_1"));
        assertEquals(false, result.get("Property_mutual4_2"));
        assertEquals(false, result.get("Property_mutual4_3"));
    }

    @Test
    public void testPRISM_Mutual_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(MUTUAL_MODEL, 5), MUTUAL_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(true, result.get("Property_mutual5_0"));
        assertEquals(false, result.get("Property_mutual5_1"));
        assertEquals(false, result.get("Property_mutual5_2"));
        assertEquals(false, result.get("Property_mutual5_3"));
    }

    @Ignore
    @Test
    public void testPRISM_Mutual_8() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(MUTUAL_MODEL, 8), MUTUAL_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(true, result.get("Property_mutual8_0"));
        assertEquals(false, result.get("Property_mutual8_1"));
        assertEquals(false, result.get("Property_mutual8_2"));
        assertEquals(false, result.get("Property_mutual8_3"));
    }

    @Ignore
    @Test
    public void testPRISM_Mutual_10() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(MUTUAL_MODEL, 10), MUTUAL_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(true, result.get("Property_mutual10_0"));
        assertEquals(false, result.get("Property_mutual10_1"));
        assertEquals(false, result.get("Property_mutual10_2"));
        assertEquals(false, result.get("Property_mutual10_3"));
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_P2P_4_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "1.1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PEER2PEER_MODEL, 4, 4), PEER2PEER_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_P2P_4_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "1.1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PEER2PEER_MODEL, 4, 5), PEER2PEER_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_P2P_4_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "1.1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PEER2PEER_MODEL, 4, 6), PEER2PEER_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_P2P_4_7() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "1.1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PEER2PEER_MODEL, 4, 7), PEER2PEER_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_P2P_4_8() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "1.1");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PEER2PEER_MODEL, 4, 8), PEER2PEER_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_P2P_5_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "1.1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PEER2PEER_MODEL, 5, 4), PEER2PEER_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_P2P_5_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "1.1");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PEER2PEER_MODEL, 5, 5), PEER2PEER_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_P2P_5_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "1.1");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PEER2PEER_MODEL, 5, 6), PEER2PEER_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_P2P_5_7() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "1.1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PEER2PEER_MODEL, 5, 7), PEER2PEER_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_P2P_5_8() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "1.1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PEER2PEER_MODEL, 5, 8), PEER2PEER_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    @Test
    public void testPRISM_Phil_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PHIL_MODEL, 3), PHIL_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(false, result.get("Property_phil3_0"));
    }

    @Test
    public void testPRISM_Phil_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PHIL_MODEL, 4), PHIL_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(false, result.get("Property_phil4_0"));
    }

    @Test
    public void testPRISM_Phil_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PHIL_MODEL, 5), PHIL_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(false, result.get("Property_phil5_0"));
    }

    @Test
    public void testPRISM_Phil_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PHIL_MODEL, 6), PHIL_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(false, result.get("Property_phil6_0"));
    }

    @Test
    public void testPRISM_Phil_7() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PHIL_MODEL, 7), PHIL_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(false, result.get("Property_phil7_0"));
    }

    @Ignore
    @Test
    public void testPRISM_Phil_8() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PHIL_MODEL, 8), PHIL_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(false, result.get("Property_phil8_0"));
    }

    @Ignore
    @Test
    public void testPRISM_Phil_9() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PHIL_MODEL, 9), PHIL_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(false, result.get("Property_phil9_0"));
    }

    @Ignore
    @Test
    public void testPRISM_Phil_10() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PHIL_MODEL, 10), PHIL_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(false, result.get("Property_phil10_0"));
    }

    @Ignore
    @Test
    public void testPRISM_Phil_15() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PHIL_MODEL, 15), PHIL_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(false, result.get("Property_phil15_0"));
    }

    @Ignore
    @Test
    public void testPRISM_Phil_20() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PHIL_MODEL, 20), PHIL_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(false, result.get("Property_phil20_0"));
    }

    @Ignore
    @Test
    public void testPRISM_Phil_25() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PHIL_MODEL, 25), PHIL_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(false, result.get("Property_phil25_0"));
    }

    @Ignore
    @Test
    public void testPRISM_Phil_30() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PHIL_MODEL, 30), PHIL_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(false, result.get("Property_phil30_0"));
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_PhilNofair_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PHIL_NOFAIR_MODEL, 3), PHIL_NOFAIR_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_PhilNofair_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PHIL_NOFAIR_MODEL, 4), PHIL_NOFAIR_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_PhilNofair_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PHIL_NOFAIR_MODEL, 5), PHIL_NOFAIR_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_PhilNofair_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PHIL_NOFAIR_MODEL, 6), PHIL_NOFAIR_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_PhilNofair_7() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PHIL_NOFAIR_MODEL, 7), PHIL_NOFAIR_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_PhilNofair_8() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PHIL_NOFAIR_MODEL, 8), PHIL_NOFAIR_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_PhilNofair_9() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PHIL_NOFAIR_MODEL, 9), PHIL_NOFAIR_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_PhilNofair_10() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PHIL_NOFAIR_MODEL, 10), PHIL_NOFAIR_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_PhilLSS_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "3");
    	constants.put("L", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PHIL_LSS_MODEL, 3), PHIL_LSS_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_PhilLSS_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "3");
    	constants.put("L", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(PHIL_LSS_MODEL, 4), PHIL_LSS_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Polling_2() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(POLLING_MODEL, 2), POLLING_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Polling_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(POLLING_MODEL, 3), POLLING_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Polling_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(POLLING_MODEL, 4), POLLING_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Polling_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(POLLING_MODEL, 5), POLLING_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Polling_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(POLLING_MODEL, 6), POLLING_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Polling_7() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(POLLING_MODEL, 7), POLLING_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Polling_8() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(POLLING_MODEL, 8), POLLING_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Polling_9() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(POLLING_MODEL, 9), POLLING_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Polling_10() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(POLLING_MODEL, 10), POLLING_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Polling_11() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(POLLING_MODEL, 11), POLLING_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Polling_12() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(POLLING_MODEL, 12), POLLING_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Polling_13() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(POLLING_MODEL, 13), POLLING_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Polling_14() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(POLLING_MODEL, 14), POLLING_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Polling_15() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(POLLING_MODEL, 15), POLLING_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Polling_16() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(POLLING_MODEL, 16), POLLING_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Polling_17() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(POLLING_MODEL, 17), POLLING_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Polling_18() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(POLLING_MODEL, 18), POLLING_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Polling_19() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(POLLING_MODEL, 19), POLLING_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Polling_20() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(POLLING_MODEL, 20), POLLING_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    @Test
    public void testPRISM_Rabin_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "5");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(RABIN_MODEL, 3), RABIN_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(true, result.get("Property_rabin3_0"));
        assertEquals(true, result.get("Property_rabin3_1"));
        assertEquals("0.0", result.get("Property_rabin3_2"), 1E-9);
        assertEquals("0.0302734375", result.get("Property_rabin3_3"), 1E-9);
    }

    @Test
    public void testPRISM_Rabin_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "5");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(RABIN_MODEL, 4), RABIN_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(true, result.get("Property_rabin4_0"));
        assertEquals(true, result.get("Property_rabin4_1"));
        assertEquals("0.0", result.get("Property_rabin4_2"), 1E-9);
        assertEquals("0.029327392578125", result.get("Property_rabin4_3"), 1E-9);
    }

    //Out-of-memory with 8GB
    @Ignore
    @Test
    public void testPRISM_Rabin_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "5");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(RABIN_MODEL, 5), RABIN_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(true, result.get("Property_rabin5_0"));
        assertEquals(true, result.get("Property_rabin5_1"));
        assertEquals("0.0", result.get("Property_rabin5_2"), 1E-9);
        assertEquals("0.029109418392181396", result.get("Property_rabin5_3"), 1E-9);
    }

    //Out-of-memory with 8GB
    @Ignore
    @Test
    public void testPRISM_Rabin_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "5");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(RABIN_MODEL, 6), RABIN_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(true, result.get("Property_rabin6_0"));
        assertEquals(true, result.get("Property_rabin6_1"));
        assertEquals("0.0", result.get("Property_rabin6_2"), 1E-9);
        assertEquals("0.028432623483240604", result.get("Property_rabin6_3"), 1E-9);
    }

    //Out-of-memory with 8GB
    @Ignore
    @Test
    public void testPRISM_Rabin_7() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "5");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(RABIN_MODEL, 7), RABIN_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(true, result.get("Property_rabin7_0"));
        assertEquals(true, result.get("Property_rabin7_1"));
        assertEquals("0.0", result.get("Property_rabin7_2"), 1E-9);
        assertEquals("0.027773339752457105", result.get("Property_rabin7_3"), 1E-9);
    }

    //Out-of-memory with 8GB
    @Ignore
    @Test
    public void testPRISM_Rabin_8() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "5");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(RABIN_MODEL, 8), RABIN_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(true, result.get("Property_rabin8_0"));
        assertEquals(true, result.get("Property_rabin8_1"));
        assertEquals("0.0", result.get("Property_rabin8_2"), 1E-9);
        assertEquals("0.027131076829618905", result.get("Property_rabin8_3"), 1E-9);
    }

    //Out-of-memory with 8GB
    @Ignore
    @Test
    public void testPRISM_Rabin_9() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "5");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(RABIN_MODEL, 9), RABIN_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(true, result.get("Property_rabin9_0"));
        assertEquals(true, result.get("Property_rabin9_1"));
        assertEquals("0.0", result.get("Property_rabin9_2"), 1E-9);
        assertEquals("0.02690346169687173", result.get("Property_rabin9_3"), 1E-9);
    }

    //Out-of-memory with 8GB
    @Ignore
    @Test
    public void testPRISM_Rabin_10() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "5");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(RABIN_MODEL, 10), RABIN_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(true, result.get("Property_rabin10_0"));
        assertEquals(true, result.get("Property_rabin10_1"));
        assertEquals("0.0", result.get("Property_rabin10_2"), 1E-9);
        assertEquals("0.026345380743400343", result.get("Property_rabin10_3"), 1E-9);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Beauquier_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(BEAUQUIER_MODEL, 3), BEAUQUIER_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals(true, result.get("Property_beauquier3_0"));
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Beauquier_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(BEAUQUIER_MODEL, 5), BEAUQUIER_PROPERTY);
        
        //Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Beauquier_7() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(BEAUQUIER_MODEL, 7), BEAUQUIER_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Beauquier_9() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(BEAUQUIER_MODEL, 9), BEAUQUIER_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Beauquier_11() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(BEAUQUIER_MODEL, 11), BEAUQUIER_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Herman_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(HERMAN_MODEL, 3), HERMAN_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Herman_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(HERMAN_MODEL, 5), HERMAN_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Herman_7() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(HERMAN_MODEL, 7), HERMAN_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Herman_9() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(HERMAN_MODEL, 9), HERMAN_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Herman_11() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(HERMAN_MODEL, 11), HERMAN_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Herman_13() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(HERMAN_MODEL, 13), HERMAN_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Herman_15() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(HERMAN_MODEL, 15), HERMAN_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Herman_17() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(HERMAN_MODEL, 17), HERMAN_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Herman_19() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(HERMAN_MODEL, 19), HERMAN_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    //Fails while computing rewards
   @Ignore
    @Test
    public void testPRISM_Herman_21() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(HERMAN_MODEL, 21), HERMAN_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_IJ_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(IJ_MODEL, 3), IJ_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_IJ_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(IJ_MODEL, 4), IJ_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_IJ_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(IJ_MODEL, 5), IJ_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_IJ_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(IJ_MODEL, 6), IJ_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_IJ_7() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(IJ_MODEL, 7), IJ_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_IJ_8() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(IJ_MODEL, 8), IJ_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_IJ_9() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(IJ_MODEL, 9), IJ_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_IJ_10() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(IJ_MODEL, 10), IJ_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_IJ_11() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(IJ_MODEL, 11), IJ_PROPERTY);
        
        //Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_IJ_12() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(IJ_MODEL, 12), IJ_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_IJ_13() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(IJ_MODEL, 13), IJ_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_IJ_14() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(IJ_MODEL, 14), IJ_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }
    
    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_IJ_15() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(IJ_MODEL, 15), IJ_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_IJ_16() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(IJ_MODEL, 16), IJ_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_IJ_17() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(IJ_MODEL, 17), IJ_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_IJ_18() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(IJ_MODEL, 18), IJ_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_IJ_19() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(IJ_MODEL, 19), IJ_PROPERTY);
        
        //Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_IJ_20() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(IJ_MODEL, 20), IJ_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_IJ_21() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(IJ_MODEL, 21), IJ_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Tandem() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("c", "10");
    	constants.put("T", "1");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, TANDEM_MODEL, TANDEM_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_WLAN_0() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("TRANS_TIME_MAX", "10");
    	constants.put("k", "2");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(WLAN_MODEL, 0), WLAN_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_WLAN_1() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("TRANS_TIME_MAX", "10");
    	constants.put("k", "2");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(WLAN_MODEL, 1), WLAN_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_WLAN_2() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("TRANS_TIME_MAX", "10");
    	constants.put("k", "2");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(WLAN_MODEL, 2), WLAN_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_WLAN_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("TRANS_TIME_MAX", "10");
    	constants.put("k", "2");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(WLAN_MODEL, 3), WLAN_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_WLAN_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("TRANS_TIME_MAX", "10");
    	constants.put("k", "2");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(WLAN_MODEL, 4), WLAN_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_WLAN_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("TRANS_TIME_MAX", "10");
    	constants.put("k", "2");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(WLAN_MODEL, 5), WLAN_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_WLAN_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("TRANS_TIME_MAX", "10");
    	constants.put("k", "2");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(WLAN_MODEL, 6), WLAN_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    @Test
    public void testPRISM_WLANCollide_0() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("COL", "2");
		constants.put("TRANS_TIME_MAX", "10");
    	constants.put("k", "2");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.CONST, constants);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        Model model = null;
        model = loadModel(options, String.format(WLAN_COLLIDE_MODEL, 0), WLAN_COLLIDE_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals("0.18359375", result.get("Property_wlan0_collide_0"), 1E-9);
        assertEquals("0.18359375", result.get("Property_wlan0_collide_1"), 1E-9);
    }

    @Test
    public void testPRISM_WLANCollide_1() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("COL", "2");
		constants.put("TRANS_TIME_MAX", "10");
    	constants.put("k", "2");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(WLAN_COLLIDE_MODEL, 1), WLAN_COLLIDE_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals("0.18359375", result.get("Property_wlan1_collide_0"), 1E-9);
        assertEquals("0.18359375", result.get("Property_wlan1_collide_1"), 1E-9);
    }

    @Test
    public void testPRISM_WLANCollide_2() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("COL", "2");
		constants.put("TRANS_TIME_MAX", "10");
    	constants.put("k", "2");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(WLAN_COLLIDE_MODEL, 2), WLAN_COLLIDE_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals("0.18359375", result.get("Property_wlan2_collide_0"), 1E-9);
        assertEquals("0.18359375", result.get("Property_wlan2_collide_1"), 1E-9);
    }

    @Test
    public void testPRISM_WLANCollide_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("COL", "2");
		constants.put("TRANS_TIME_MAX", "10");
		constants.put("k", "2");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(WLAN_COLLIDE_MODEL, 3), WLAN_COLLIDE_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals("0.18359375", result.get("Property_wlan3_collide_0"), 1E-9);
        assertEquals("0.18359375", result.get("Property_wlan3_collide_1"), 1E-9);
    }

    @Test
    public void testPRISM_WLANCollide_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("COL", "2");
		constants.put("TRANS_TIME_MAX", "10");
    	constants.put("k", "2");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(WLAN_COLLIDE_MODEL, 4), WLAN_COLLIDE_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals("0.18359375", result.get("Property_wlan4_collide_0"), 1E-9);
        assertEquals("0.18359375", result.get("Property_wlan4_collide_1"), 1E-9);
    }

    @Test
    public void testPRISM_WLANCollide_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("COL", "2");
		constants.put("TRANS_TIME_MAX", "10");
    	constants.put("k", "2");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(WLAN_COLLIDE_MODEL, 5), WLAN_COLLIDE_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals("0.18359375", result.get("Property_wlan5_collide_0"), 1E-9);
        assertEquals("0.18359375", result.get("Property_wlan5_collide_1"), 1E-9);
    }

    @Test
    public void testPRISM_WLANCollide_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("COL", "2");
		constants.put("TRANS_TIME_MAX", "10");
    	constants.put("k", "2");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(WLAN_COLLIDE_MODEL, 6), WLAN_COLLIDE_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals("0.18359375", result.get("Property_wlan6_collide_0"), 1E-9);
        assertEquals("0.18359375", result.get("Property_wlan6_collide_1"), 1E-9);
    }

    @Test
    public void testPRISM_WLANTimeBounded_0() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
		constants.put("TRANS_TIME_MAX", "10");
    	constants.put("DEADLINE", "100");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(WLAN_TIME_BOUNDED_MODEL, 0), WLAN_TIME_BOUNDED_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals("0.9090728759765625", result.get("Property_wlan0_time_bounded_0"), 1E-9);
        assertEquals("0.9090728759765625", result.get("Property_wlan0_time_bounded_1"), 1E-9);
        assertEquals("0.9794130921363831", result.get("Property_wlan0_time_bounded_2"), 1E-9);
        assertEquals("0.9794130921363831", result.get("Property_wlan0_time_bounded_3"), 1E-9);
        assertEquals("0.9363574981689453", result.get("Property_wlan0_time_bounded_4"), 1E-9);
        assertEquals("0.9363574981689453", result.get("Property_wlan0_time_bounded_5"), 1E-9);
    }

    @Test
    public void testPRISM_WLANTimeBounded_1() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
		constants.put("TRANS_TIME_MAX", "10");
    	constants.put("DEADLINE", "100");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(WLAN_TIME_BOUNDED_MODEL, 1), WLAN_TIME_BOUNDED_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals("0.846221923828125", result.get("Property_wlan1_time_bounded_0"), 1E-9);
        assertEquals("0.846221923828125", result.get("Property_wlan1_time_bounded_1"), 1E-9);
        assertEquals("0.9844965040683746", result.get("Property_wlan1_time_bounded_2"), 1E-9);
        assertEquals("0.9844965040683746", result.get("Property_wlan1_time_bounded_3"), 1E-9);
        assertEquals("0.9004454463720322", result.get("Property_wlan1_time_bounded_4"), 1E-9);
        assertEquals("0.9004454463720322", result.get("Property_wlan1_time_bounded_5"), 1E-9);
    }

    @Test
    public void testPRISM_WLANTimeBounded_2() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
		constants.put("TRANS_TIME_MAX", "10");
    	constants.put("DEADLINE", "100");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(WLAN_TIME_BOUNDED_MODEL, 2), WLAN_TIME_BOUNDED_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals("0.846221923828125", result.get("Property_wlan2_time_bounded_0"), 1E-9);
        assertEquals("0.846221923828125", result.get("Property_wlan2_time_bounded_1"), 1E-9);
        assertEquals("0.9836365208029747", result.get("Property_wlan2_time_bounded_2"), 1E-9);
        assertEquals("0.9836365208029747", result.get("Property_wlan2_time_bounded_3"), 1E-9);
        assertEquals("0.9002140127122402", result.get("Property_wlan2_time_bounded_4"), 1E-9);
        assertEquals("0.9002140127122402", result.get("Property_wlan2_time_bounded_5"), 1E-9);
    }

    @Test
    public void testPRISM_WLANTimeBounded_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
		constants.put("TRANS_TIME_MAX", "10");
    	constants.put("DEADLINE", "100");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(WLAN_TIME_BOUNDED_MODEL, 3), WLAN_TIME_BOUNDED_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals("0.846221923828125", result.get("Property_wlan3_time_bounded_0"), 1E-9);
        assertEquals("0.846221923828125", result.get("Property_wlan3_time_bounded_1"), 1E-9);
        assertEquals("0.9836365208029747", result.get("Property_wlan3_time_bounded_2"), 1E-9);
        assertEquals("0.9836365208029747", result.get("Property_wlan3_time_bounded_3"), 1E-9);
        assertEquals("0.9002140127122402", result.get("Property_wlan3_time_bounded_4"), 1E-9);
        assertEquals("0.9002140127122402", result.get("Property_wlan3_time_bounded_5"), 1E-9);
    }

    @Ignore
    @Test
    public void testPRISM_WLANTimeBounded_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
		constants.put("TRANS_TIME_MAX", "10");
    	constants.put("DEADLINE", "100");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(WLAN_TIME_BOUNDED_MODEL, 4), WLAN_TIME_BOUNDED_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals("0.846221923828125", result.get("Property_wlan4_time_bounded_0"), 1E-9);
        assertEquals("0.846221923828125", result.get("Property_wlan4_time_bounded_1"), 1E-9);
        assertEquals("0.9836365208029747", result.get("Property_wlan4_time_bounded_2"), 1E-9);
        assertEquals("0.9836365208029747", result.get("Property_wlan4_time_bounded_3"), 1E-9);
        assertEquals("0.9002140127122402", result.get("Property_wlan4_time_bounded_4"), 1E-9);
        assertEquals("0.9002140127122402", result.get("Property_wlan4_time_bounded_5"), 1E-9);
    }

    @Ignore
    @Test
    public void testPRISM_WLANTimeBounded_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
		constants.put("TRANS_TIME_MAX", "10");
    	constants.put("DEADLINE", "100");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(WLAN_TIME_BOUNDED_MODEL, 5), WLAN_TIME_BOUNDED_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals("0.846221923828125", result.get("Property_wlan5_time_bounded_0"), 1E-9);
        assertEquals("0.846221923828125", result.get("Property_wlan5_time_bounded_1"), 1E-9);
        assertEquals("0.9836365208029747", result.get("Property_wlan5_time_bounded_2"), 1E-9);
        assertEquals("0.9836365208029747", result.get("Property_wlan5_time_bounded_3"), 1E-9);
        assertEquals("0.9002140127122402", result.get("Property_wlan5_time_bounded_4"), 1E-9);
        assertEquals("0.9002140127122402", result.get("Property_wlan5_time_bounded_5"), 1E-9);
    }

    @Ignore
    @Test
    public void testPRISM_WLANTimeBounded_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
		constants.put("TRANS_TIME_MAX", "10");
    	constants.put("DEADLINE", "100");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, String.format(WLAN_TIME_BOUNDED_MODEL, 6), WLAN_TIME_BOUNDED_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals("0.846221923828125", result.get("Property_wlan6_time_bounded_0"), 1E-9);
        assertEquals("0.846221923828125", result.get("Property_wlan6_time_bounded_1"), 1E-9);
        assertEquals("0.9836365208029747", result.get("Property_wlan6_time_bounded_2"), 1E-9);
        assertEquals("0.9836365208029747", result.get("Property_wlan6_time_bounded_3"), 1E-9);
        assertEquals("0.9002140127122402", result.get("Property_wlan6_time_bounded_4"), 1E-9);
        assertEquals("0.9002140127122402", result.get("Property_wlan6_time_bounded_5"), 1E-9);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISM_Zeroconf() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("err", "0");
    	constants.put("K", "4");
    	constants.put("reset", "true");
    	constants.put("N", "1000");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, ZEROCONF_MODEL, ZEROCONF_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-9);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-9);
    }

    @Test
    public void testPRISM_ZeroconfTimeBounded() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "11");
    	constants.put("K", "1");
    	constants.put("bound", "10");
    	constants.put("reset", "true");
    	constants.put("N", "1000");
        Options options = preparePRISMOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-9");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, ZEROCONF_TIME_BOUNDED_MODEL, ZEROCONF_TIME_BOUNDED_PROPERTY);
        
        Map<String, Value> result = computeResultsMapDefinition(model);
        assertEquals("2.3447760329422196E-5", result.get("Property_zeroconf_time_bounded_0"), 1E-9);
        assertEquals("2.3447760329422196E-5", result.get("Property_zeroconf_time_bounded_1"), 1E-9);
        assertEquals("0.014275054203184579", result.get("Property_zeroconf_time_bounded_2"), 1E-9);
        assertEquals("0.014275054203184579", result.get("Property_zeroconf_time_bounded_3"), 1E-9);
    }

}
