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
import org.junit.Ignore;
import org.junit.Test;

import epmc.error.EPMCException;
import epmc.jani.model.ModelJANI;
import epmc.main.options.UtilOptionsEPMC;
import epmc.messages.OptionsMessages;
import epmc.messages.TimeStampFormatSecondsStarted;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.Model;
import epmc.modelchecker.ModelCheckerResults;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.value.Value;

import static epmc.ModelNamesPRISM.*;
import static epmc.jani.ModelNames.*;
import static epmc.modelchecker.TestHelper.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tests for model checking of JANI models.
 * 
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
     * Prepare options including loading JANI plugin.
     * 
     * @return options usable for JANI model analysis
     * @throws EPMCException thrown in case problem occurs
     */
    private final static Options prepareJANIOptions() throws EPMCException {
        Options options = UtilOptionsEPMC.newOptions();
        prepareOptions(options, LogType.TRANSLATE, ModelJANI.IDENTIFIER);
//        options.set(OptionsPlugin.PLUGIN, PLUGIN_DIR);
        options.set(OptionsMessages.TIME_STAMPS, TimeStampFormatSecondsStarted.class);
        options.set(OptionsMessages.TRANSLATE_MESSAGES, "false");
        return options;
    }
    
    
    @Test
    public void testPRISMExportedTest() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("COL", "2");
		constants.put("TRANS_TIME_MAX", "10");
    	constants.put("k", "2");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, System.getProperty("user.home") + "/test.jani");
        
        ModelCheckerResults result = computeResults(model);
        int i = 0;
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    @Test
    public void testPRISMTest() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("COL", "2");
		constants.put("TRANS_TIME_MAX", "10");
    	constants.put("k", "2");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, "prism");
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, System.getProperty("user.home") + "/test.prism", System.getProperty("user.home") + "/test.prop");
        
        ModelCheckerResults result = computeResults(model);
        int i = 0;
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    @Test
    public void testPRISMExportedBRP() throws EPMCException {
    	// TODO suppport "deadlock" label
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("MAX", "4");
    	constants.put("N", "64");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(BRP_MODEL));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.0000000000000000", result.get("Property_brp_0"), 1E-15);
        assertEquals("0.0000000000000000", result.get("Property_brp_1"), 1E-15);
        assertEquals("0.0000000000000000", result.get("Property_brp_2"), 1E-15);
        assertEquals("0.0000000000000000", result.get("Property_brp_3"), 1E-15);
        assertEquals("0.0000015040454933", result.get("Property_brp_4"), 1E-15);
        assertEquals("0.0000015040454933", result.get("Property_brp_5"), 1E-15);
        assertEquals("0.0000000235006928", result.get("Property_brp_6"), 1E-15);
        assertEquals("0.0000000235006928", result.get("Property_brp_7"), 1E-15);
        assertEquals("0.0000012925389895", result.get("Property_brp_8"), 1E-15);
        assertEquals("0.0000012925389895", result.get("Property_brp_9"), 1E-15);
        assertEquals("0.0000000032000000", result.get("Property_brp_10"), 1E-15);
        assertEquals("0.0000000032000000", result.get("Property_brp_11"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedCell() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "0.5");
    	constants.put("N", "200");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(CELL_MODEL));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedCluster() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "10");
    	constants.put("N", "20");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(CLUSTER_MODEL));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedCoin_2() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "2");
    	constants.put("k", "10");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(COIN_MODEL, 2)));
        
//        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedCoin_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "2");
    	constants.put("k", "10");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(COIN_MODEL, 4)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedCoin_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "2");
    	constants.put("k", "10");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(COIN_MODEL, 6)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedCoin_8() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "2");
    	constants.put("k", "10");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(COIN_MODEL, 8)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedCoin_10() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "2");
    	constants.put("k", "10");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(COIN_MODEL, 10)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedCSMA_2_2() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(CSMA_MODEL,2,2)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedCSMA_2_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "1");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(CSMA_MODEL,2,4)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedCSMA_2_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(CSMA_MODEL,2,6)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedCSMA_3_2() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "1");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(CSMA_MODEL,3,2)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedCSMA_3_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "1");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(CSMA_MODEL,3,4)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedCSMA_3_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "1");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(CSMA_MODEL,3,6)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedCSMA_4_2() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "1");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(CSMA_MODEL,4,2)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedCSMA_4_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(CSMA_MODEL,4,4)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedCSMA_4_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(CSMA_MODEL,4,6)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedDice() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("x", "3");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(DICE_MODEL));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedTwoDice() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("x", "5");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(TWO_DICE_MODEL));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    @Test
    public void testPRISMExportedDiningCrypt_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "0");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(DINING_CRYPT_MODEL, 3)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_dining_crypt3_0"));
        assertEquals(true, result.get("Property_dining_crypt3_1"));
    }

    @Test
    public void testPRISMExportedDiningCrypt_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "0");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(DINING_CRYPT_MODEL, 4)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_dining_crypt4_0"));
        assertEquals(true, result.get("Property_dining_crypt4_1"));
    }

    @Test
    public void testPRISMExportedDiningCrypt_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "0");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(DINING_CRYPT_MODEL, 5)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_dining_crypt5_0"));
        assertEquals(true, result.get("Property_dining_crypt5_1"));
    }

    @Test
    public void testPRISMExportedDiningCrypt_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "0");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(DINING_CRYPT_MODEL, 6)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_dining_crypt6_0"));
        assertEquals(true, result.get("Property_dining_crypt6_1"));
    }

    @Test
    public void testPRISMExportedDiningCrypt_7() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "0");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(DINING_CRYPT_MODEL, 7)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_dining_crypt7_0"));
        assertEquals(true, result.get("Property_dining_crypt7_1"));
    }

    @Test
    public void testPRISMExportedDiningCrypt_8() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "0");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(DINING_CRYPT_MODEL, 8)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_dining_crypt8_0"));
        assertEquals(true, result.get("Property_dining_crypt8_1"));
    }

    @Ignore
    @Test
    public void testPRISMExportedDiningCrypt_9() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "0");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(DINING_CRYPT_MODEL, 9)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_dining_crypt9_0"));
        assertEquals(true, result.get("Property_dining_crypt9_1"));
    }

    @Ignore
    @Test
    public void testPRISMExportedDiningCrypt_10() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "0");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(DINING_CRYPT_MODEL, 10)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_dining_crypt10_0"));
        assertEquals(true, result.get("Property_dining_crypt10_1"));
    }

    @Ignore
    @Test
    public void testPRISMExportedDiningCrypt_15() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "0");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(DINING_CRYPT_MODEL, 15)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_dining_crypt15_0"));
        assertEquals(true, result.get("Property_dining_crypt15_1"));
    }

    @Test
    public void testPRISMExportedFireweireAbs() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("delay", "36");
    	constants.put("fast", "0.5");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "firewire_abs" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_firewire_0"));
    }

    @Test
    public void testPRISMExportedFireweireImpl() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("delay", "36");
    	constants.put("fast", "0.5");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "firewire_impl" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_firewire_1"));
    }

//    Fails on computing steady state probabilities
    @Ignore
    @Test
    public void testPRISMExportedFMS() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("n", "5");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(FMS_MODEL));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails on computing steady state probabilities
    @Ignore
    @Test
    public void testPRISMExportedKanban() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("t", "4");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(KANBAN_MODEL));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedLeaderAsync_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
       Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_async_3" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedLeaderAsync_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_async_4" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedLeaderAsync_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_async_5" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedLeaderAsync_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_async_6" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedLeaderAsync_7() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_async_7" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedLeaderAsync_8() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_async_8" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedLeaderAsync_9() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_async_9" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedLeaderAsync_10() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_async_10" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
//    @Ignore
    @Test
    public void testPRISMExportedLeaderSync_3_2() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_sync_3_2" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedLeaderSync_3_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_sync_3_3" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedLeaderSync_3_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_sync_3_4" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedLeaderSync_3_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_sync_3_5" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedLeaderSync_3_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_sync_3_6" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedLeaderSync_3_8() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_sync_3_8" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedLeaderSync_4_2() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_sync_4_2" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedLeaderSync_4_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_sync_4_3" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedLeaderSync_4_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_sync_4_4" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedLeaderSync_4_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_sync_4_5" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedLeaderSync_4_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_sync_4_6" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedLeaderSync_4_8() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_sync_4_8" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedLeaderSync_5_2() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_sync_5_2" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedLeaderSync_5_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_sync_5_3" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedLeaderSync_5_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_sync_5_4" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedLeaderSync_5_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_sync_5_5" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedLeaderSync_5_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_sync_5_6" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedLeaderSync_5_8() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_sync_5_8" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedLeaderSync_6_2() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_sync_6_2" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedLeaderSync_6_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_sync_6_3" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedLeaderSync_6_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_sync_6_4" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedLeaderSync_6_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_sync_6_5" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedLeaderSync_6_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_sync_6_6" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedLeaderSync_6_8() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("L", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, JANI_EXPORT_DIR + "leader_sync_6_8" + JANI_EXTENSION);
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedKNACL() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("N1", "10");
    	constants.put("N2", "10");
    	constants.put("T", "0.002");
    	constants.put("i", "0");
    	constants.put("N3", "10");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(KNACL_MODEL));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedNACL() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("N1", "10");
    	constants.put("N2", "10");
    	constants.put("T", "0.002");
    	constants.put("i", "0");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(NACL_MODEL));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedMC() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("N1", "10");
    	constants.put("N2", "10");
    	constants.put("T", "0.002");
    	constants.put("i", "0");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(MC_MODEL));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    @Test
    public void testPRISMExportedMutual_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(MUTUAL_MODEL, 3)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_mutual3_0"));
        assertEquals(false, result.get("Property_mutual3_1"));
        assertEquals(false, result.get("Property_mutual3_2"));
        assertEquals(false, result.get("Property_mutual3_3"));
    }

    @Test
    public void testPRISMExportedMutual_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(MUTUAL_MODEL, 4)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_mutual4_0"));
        assertEquals(false, result.get("Property_mutual4_1"));
        assertEquals(false, result.get("Property_mutual4_2"));
        assertEquals(false, result.get("Property_mutual4_3"));
    }

    @Test
    public void testPRISMExportedMutual_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(MUTUAL_MODEL, 5)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_mutual5_0"));
        assertEquals(false, result.get("Property_mutual5_1"));
        assertEquals(false, result.get("Property_mutual5_2"));
        assertEquals(false, result.get("Property_mutual5_3"));
    }

    @Ignore
    @Test
    public void testPRISMExportedMutual_8() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(MUTUAL_MODEL, 8)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_mutual8_0"));
        assertEquals(false, result.get("Property_mutual8_1"));
        assertEquals(false, result.get("Property_mutual8_2"));
        assertEquals(false, result.get("Property_mutual8_3"));
    }

    @Ignore
    @Test
    public void testPRISMExportedMutual_10() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(MUTUAL_MODEL, 10)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_mutual10_0"));
        assertEquals(false, result.get("Property_mutual10_1"));
        assertEquals(false, result.get("Property_mutual10_2"));
        assertEquals(false, result.get("Property_mutual10_3"));
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedP2P_4_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "1.1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PEER2PEER_MODEL, 4, 4)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedP2P_4_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "1.1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PEER2PEER_MODEL, 4, 5)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedP2P_4_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "1.1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PEER2PEER_MODEL, 4, 6)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedP2P_4_7() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "1.1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PEER2PEER_MODEL, 4, 7)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedP2P_4_8() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "1.1");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PEER2PEER_MODEL, 4, 8)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedP2P_5_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "1.1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PEER2PEER_MODEL, 5, 4)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedP2P_5_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "1.1");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PEER2PEER_MODEL, 5, 5)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedP2P_5_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "1.1");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PEER2PEER_MODEL, 5, 6)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedP2P_5_7() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "1.1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PEER2PEER_MODEL, 5, 7)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedP2P_5_8() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "1.1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PEER2PEER_MODEL, 5, 8)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    @Test
    public void testPRISMExportedPhil_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PHIL_MODEL, 3)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(false, result.get("Property_phil3_0"));
    }

    @Test
    public void testPRISMExportedPhil_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PHIL_MODEL, 4)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(false, result.get("Property_phil4_0"));
    }

    @Test
    public void testPRISMExportedPhil_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PHIL_MODEL, 5)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(false, result.get("Property_phil5_0"));
    }

    @Test
    public void testPRISMExportedPhil_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PHIL_MODEL, 6)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(false, result.get("Property_phil6_0"));
    }

    @Test
    public void testPRISMExportedPhil_7() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PHIL_MODEL, 7)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(false, result.get("Property_phil7_0"));
    }

    @Ignore
    @Test
    public void testPRISMExportedPhil_8() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PHIL_MODEL, 8)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(false, result.get("Property_phil8_0"));
    }

    @Ignore
    @Test
    public void testPRISMExportedPhil_9() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PHIL_MODEL, 9)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(false, result.get("Property_phil9_0"));
    }

    @Ignore
    @Test
    public void testPRISMExportedPhil_10() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PHIL_MODEL, 10)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(false, result.get("Property_phil10_0"));
    }

    @Ignore
    @Test
    public void testPRISMExportedPhil_15() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PHIL_MODEL, 15)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(false, result.get("Property_phil15_0"));
    }

    @Ignore
    @Test
    public void testPRISMExportedPhil_20() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PHIL_MODEL, 20)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(false, result.get("Property_phil20_0"));
    }

    @Ignore
    @Test
    public void testPRISMExportedPhil_25() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PHIL_MODEL, 25)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(false, result.get("Property_phil25_0"));
    }

    @Ignore
    @Test
    public void testPRISMExportedPhil_30() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PHIL_MODEL, 30)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(false, result.get("Property_phil30_0"));
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedPhilNofair_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PHIL_NOFAIR_MODEL, 3)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedPhilNofair_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PHIL_NOFAIR_MODEL, 4)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedPhilNofair_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PHIL_NOFAIR_MODEL, 5)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedPhilNofair_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PHIL_NOFAIR_MODEL, 6)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedPhilNofair_7() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PHIL_NOFAIR_MODEL, 7)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedPhilNofair_8() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PHIL_NOFAIR_MODEL, 8)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedPhilNofair_9() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PHIL_NOFAIR_MODEL, 9)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedPhilNofair_10() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PHIL_NOFAIR_MODEL, 10)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedPhilLSS_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "3");
    	constants.put("L", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PHIL_LSS_MODEL, 3)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedPhilLSS_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "3");
    	constants.put("L", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(PHIL_LSS_MODEL, 4)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedPolling_2() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(POLLING_MODEL, 2)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedPolling_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(POLLING_MODEL, 3)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedPolling_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(POLLING_MODEL, 4)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedPolling_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(POLLING_MODEL, 5)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedPolling_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(POLLING_MODEL, 6)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedPolling_7() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(POLLING_MODEL, 7)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedPolling_8() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(POLLING_MODEL, 8)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedPolling_9() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(POLLING_MODEL, 9)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedPolling_10() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(POLLING_MODEL, 10)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedPolling_11() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(POLLING_MODEL, 11)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedPolling_12() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(POLLING_MODEL, 12)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedPolling_13() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(POLLING_MODEL, 13)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedPolling_14() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(POLLING_MODEL, 14)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedPolling_15() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(POLLING_MODEL, 15)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedPolling_16() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(POLLING_MODEL, 16)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedPolling_17() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(POLLING_MODEL, 17)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedPolling_18() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(POLLING_MODEL, 18)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedPolling_19() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(POLLING_MODEL, 19)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedPolling_20() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "50");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(POLLING_MODEL, 20)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    @Test
    public void testPRISMExportedRabin_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "5");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(RABIN_MODEL, 3)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_rabin3_0"));
        assertEquals(true, result.get("Property_rabin3_1"));
        assertEquals("0.0", result.get("Property_rabin3_2"), 1E-15);
        assertEquals("0.0302734375", result.get("Property_rabin3_3"), 1E-15);
    }

    @Test
    public void testPRISMExportedRabin_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "5");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(RABIN_MODEL, 4)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_rabin4_0"));
        assertEquals(true, result.get("Property_rabin4_1"));
        assertEquals("0.0", result.get("Property_rabin4_2"), 1E-15);
        assertEquals("0.029327392578125", result.get("Property_rabin4_3"), 1E-15);
    }

    //Out-of-memory with 8GB
    @Ignore
    @Test
    public void testPRISMExportedRabin_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "5");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(RABIN_MODEL, 5)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_rabin5_0"));
        assertEquals(true, result.get("Property_rabin5_1"));
        assertEquals("0.0", result.get("Property_rabin5_2"), 1E-15);
        assertEquals("0.029109418392181396", result.get("Property_rabin5_3"), 1E-15);
    }

    //Out-of-memory with 8GB
    @Ignore
    @Test
    public void testPRISMExportedRabin_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "5");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(RABIN_MODEL, 6)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_rabin6_0"));
        assertEquals(true, result.get("Property_rabin6_1"));
        assertEquals("0.0", result.get("Property_rabin6_2"), 1E-15);
        assertEquals("0.028432623483240604", result.get("Property_rabin6_3"), 1E-15);
    }

    //Out-of-memory with 8GB
    @Ignore
    @Test
    public void testPRISMExportedRabin_7() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "5");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(RABIN_MODEL, 7)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_rabin7_0"));
        assertEquals(true, result.get("Property_rabin7_1"));
        assertEquals("0.0", result.get("Property_rabin7_2"), 1E-15);
        assertEquals("0.027773339752457105", result.get("Property_rabin7_3"), 1E-15);
    }

    //Out-of-memory with 8GB
    @Ignore
    @Test
    public void testPRISMExportedRabin_8() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "5");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(RABIN_MODEL, 8)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_rabin8_0"));
        assertEquals(true, result.get("Property_rabin8_1"));
        assertEquals("0.0", result.get("Property_rabin8_2"), 1E-15);
        assertEquals("0.027131076829618905", result.get("Property_rabin8_3"), 1E-15);
    }

    //Out-of-memory with 8GB
    @Ignore
    @Test
    public void testPRISMExportedRabin_9() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "5");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(RABIN_MODEL, 9)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_rabin9_0"));
        assertEquals(true, result.get("Property_rabin9_1"));
        assertEquals("0.0", result.get("Property_rabin9_2"), 1E-15);
        assertEquals("0.02690346169687173", result.get("Property_rabin9_3"), 1E-15);
    }

    //Out-of-memory with 8GB
    @Ignore
    @Test
    public void testPRISMExportedRabin_10() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("k", "5");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(RABIN_MODEL, 10)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_rabin10_0"));
        assertEquals(true, result.get("Property_rabin10_1"));
        assertEquals("0.0", result.get("Property_rabin10_2"), 1E-15);
        assertEquals("0.026345380743400343", result.get("Property_rabin10_3"), 1E-15);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedBeauquier_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(BEAUQUIER_MODEL, 3)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals(true, result.get("Property_beauquier3_0"));
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedBeauquier_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(BEAUQUIER_MODEL, 5)));
        
        //Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedBeauquier_7() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(BEAUQUIER_MODEL, 7)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedBeauquier_9() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(BEAUQUIER_MODEL, 9)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedBeauquier_11() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(BEAUQUIER_MODEL, 11)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedHerman_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(HERMAN_MODEL, 3)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedHerman_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(HERMAN_MODEL, 5)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedHerman_7() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(HERMAN_MODEL, 7)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedHerman_9() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(HERMAN_MODEL, 9)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedHerman_11() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(HERMAN_MODEL, 11)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedHerman_13() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(HERMAN_MODEL, 13)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedHerman_15() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(HERMAN_MODEL, 15)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedHerman_17() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(HERMAN_MODEL, 17)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedHerman_19() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(HERMAN_MODEL, 19)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    //Fails while computing rewards
   @Ignore
    @Test
    public void testPRISMExportedHerman_21() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(HERMAN_MODEL, 21)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedIJ_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(IJ_MODEL, 3)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedIJ_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(IJ_MODEL, 4)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedIJ_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(IJ_MODEL, 5)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedIJ_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(IJ_MODEL, 6)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedIJ_7() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(IJ_MODEL, 7)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedIJ_8() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(IJ_MODEL, 8)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedIJ_9() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(IJ_MODEL, 9)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedIJ_10() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(IJ_MODEL, 10)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedIJ_11() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(IJ_MODEL, 11)));
        
        //Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedIJ_12() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(IJ_MODEL, 12)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedIJ_13() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(IJ_MODEL, 13)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedIJ_14() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
    	Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(IJ_MODEL, 14)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }
    
    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedIJ_15() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(IJ_MODEL, 15)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedIJ_16() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(IJ_MODEL, 16)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedIJ_17() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(IJ_MODEL, 17)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedIJ_18() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(IJ_MODEL, 18)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedIJ_19() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(IJ_MODEL, 19)));
        
        //Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedIJ_20() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(IJ_MODEL, 20)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    //Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedIJ_21() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("K", "0");
    	constants.put("k", "0");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(IJ_MODEL, 21)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedTandem() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("c", "10");
    	constants.put("T", "1");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(TANDEM_MODEL));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedWLAN_0() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("TRANS_TIME_MAX", "10");
    	constants.put("k", "2");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(WLAN_MODEL, 0)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedWLAN_1() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("TRANS_TIME_MAX", "10");
    	constants.put("k", "2");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(WLAN_MODEL, 1)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedWLAN_2() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("TRANS_TIME_MAX", "10");
    	constants.put("k", "2");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(WLAN_MODEL, 2)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedWLAN_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("TRANS_TIME_MAX", "10");
    	constants.put("k", "2");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(WLAN_MODEL, 3)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedWLAN_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("TRANS_TIME_MAX", "10");
    	constants.put("k", "2");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(WLAN_MODEL, 4)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedWLAN_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("TRANS_TIME_MAX", "10");
    	constants.put("k", "2");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(WLAN_MODEL, 5)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedWLAN_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("TRANS_TIME_MAX", "10");
    	constants.put("k", "2");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(WLAN_MODEL, 6)));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    @Test
    public void testPRISMExportedWLANCollide_0() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("COL", "2");
		constants.put("TRANS_TIME_MAX", "10");
    	constants.put("k", "2");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.CONST, constants);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(WLAN_COLLIDE_MODEL, 0)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.18359375", result.get("Property_wlan0_collide_0"), 1E-15);
        assertEquals("0.18359375", result.get("Property_wlan0_collide_1"), 1E-15);
    }

    @Test
    public void testPRISMExportedWLANCollide_1() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("COL", "2");
		constants.put("TRANS_TIME_MAX", "10");
    	constants.put("k", "2");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(WLAN_COLLIDE_MODEL, 1)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.18359375", result.get("Property_wlan1_collide_0"), 1E-15);
        assertEquals("0.18359375", result.get("Property_wlan1_collide_1"), 1E-15);
    }

    @Test
    public void testPRISMExportedWLANCollide_2() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("COL", "2");
		constants.put("TRANS_TIME_MAX", "10");
    	constants.put("k", "2");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(WLAN_COLLIDE_MODEL, 2)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.18359375", result.get("Property_wlan2_collide_0"), 1E-15);
        assertEquals("0.18359375", result.get("Property_wlan2_collide_1"), 1E-15);
    }

    @Test
    public void testPRISMExportedWLANCollide_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("COL", "2");
		constants.put("TRANS_TIME_MAX", "10");
		constants.put("k", "2");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(WLAN_COLLIDE_MODEL, 3)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.18359375", result.get("Property_wlan3_collide_0"), 1E-15);
        assertEquals("0.18359375", result.get("Property_wlan3_collide_1"), 1E-15);
    }

    @Test
    public void testPRISMExportedWLANCollide_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("COL", "2");
		constants.put("TRANS_TIME_MAX", "10");
    	constants.put("k", "2");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(WLAN_COLLIDE_MODEL, 4)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.18359375", result.get("Property_wlan4_collide_0"), 1E-15);
        assertEquals("0.18359375", result.get("Property_wlan4_collide_1"), 1E-15);
    }

    @Test
    public void testPRISMExportedWLANCollide_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("COL", "2");
		constants.put("TRANS_TIME_MAX", "10");
    	constants.put("k", "2");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(WLAN_COLLIDE_MODEL, 5)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.18359375", result.get("Property_wlan5_collide_0"), 1E-15);
        assertEquals("0.18359375", result.get("Property_wlan5_collide_1"), 1E-15);
    }

    @Test
    public void testPRISMExportedWLANCollide_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("COL", "2");
		constants.put("TRANS_TIME_MAX", "10");
    	constants.put("k", "2");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(WLAN_COLLIDE_MODEL, 6)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.18359375", result.get("Property_wlan6_collide_0"), 1E-15);
        assertEquals("0.18359375", result.get("Property_wlan6_collide_1"), 1E-15);
    }

    @Test
    public void testPRISMExportedWLANTimeBounded_0() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
		constants.put("TRANS_TIME_MAX", "10");
    	constants.put("DEADLINE", "100");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(WLAN_TIME_BOUNDED_MODEL, 0)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.9090728759765625", result.get("Property_wlan0_time_bounded_0"), 1E-15);
        assertEquals("0.9090728759765625", result.get("Property_wlan0_time_bounded_1"), 1E-15);
        assertEquals("0.9794130921363831", result.get("Property_wlan0_time_bounded_2"), 1E-15);
        assertEquals("0.9794130921363831", result.get("Property_wlan0_time_bounded_3"), 1E-15);
        assertEquals("0.9363574981689453", result.get("Property_wlan0_time_bounded_4"), 1E-15);
        assertEquals("0.9363574981689453", result.get("Property_wlan0_time_bounded_5"), 1E-15);
    }

    @Test
    public void testPRISMExportedWLANTimeBounded_1() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
		constants.put("TRANS_TIME_MAX", "10");
    	constants.put("DEADLINE", "100");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(WLAN_TIME_BOUNDED_MODEL, 1)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.846221923828125", result.get("Property_wlan1_time_bounded_0"), 1E-15);
        assertEquals("0.846221923828125", result.get("Property_wlan1_time_bounded_1"), 1E-15);
        assertEquals("0.9844965040683746", result.get("Property_wlan1_time_bounded_2"), 1E-15);
        assertEquals("0.9844965040683746", result.get("Property_wlan1_time_bounded_3"), 1E-15);
        assertEquals("0.9004454463720322", result.get("Property_wlan1_time_bounded_4"), 1E-15);
        assertEquals("0.9004454463720322", result.get("Property_wlan1_time_bounded_5"), 1E-15);
    }

    @Test
    public void testPRISMExportedWLANTimeBounded_2() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
		constants.put("TRANS_TIME_MAX", "10");
    	constants.put("DEADLINE", "100");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(WLAN_TIME_BOUNDED_MODEL, 2)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.846221923828125", result.get("Property_wlan2_time_bounded_0"), 1E-15);
        assertEquals("0.846221923828125", result.get("Property_wlan2_time_bounded_1"), 1E-15);
        assertEquals("0.9836365208029747", result.get("Property_wlan2_time_bounded_2"), 1E-15);
        assertEquals("0.9836365208029747", result.get("Property_wlan2_time_bounded_3"), 1E-15);
        assertEquals("0.9002140127122402", result.get("Property_wlan2_time_bounded_4"), 1E-15);
        assertEquals("0.9002140127122402", result.get("Property_wlan2_time_bounded_5"), 1E-15);
    }

    @Test
    public void testPRISMExportedWLANTimeBounded_3() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
		constants.put("TRANS_TIME_MAX", "10");
    	constants.put("DEADLINE", "100");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(WLAN_TIME_BOUNDED_MODEL, 3)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.846221923828125", result.get("Property_wlan3_time_bounded_0"), 1E-15);
        assertEquals("0.846221923828125", result.get("Property_wlan3_time_bounded_1"), 1E-15);
        assertEquals("0.9836365208029747", result.get("Property_wlan3_time_bounded_2"), 1E-15);
        assertEquals("0.9836365208029747", result.get("Property_wlan3_time_bounded_3"), 1E-15);
        assertEquals("0.9002140127122402", result.get("Property_wlan3_time_bounded_4"), 1E-15);
        assertEquals("0.9002140127122402", result.get("Property_wlan3_time_bounded_5"), 1E-15);
    }

    @Ignore
    @Test
    public void testPRISMExportedWLANTimeBounded_4() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
		constants.put("TRANS_TIME_MAX", "10");
    	constants.put("DEADLINE", "100");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(WLAN_TIME_BOUNDED_MODEL, 4)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.846221923828125", result.get("Property_wlan4_time_bounded_0"), 1E-15);
        assertEquals("0.846221923828125", result.get("Property_wlan4_time_bounded_1"), 1E-15);
        assertEquals("0.9836365208029747", result.get("Property_wlan4_time_bounded_2"), 1E-15);
        assertEquals("0.9836365208029747", result.get("Property_wlan4_time_bounded_3"), 1E-15);
        assertEquals("0.9002140127122402", result.get("Property_wlan4_time_bounded_4"), 1E-15);
        assertEquals("0.9002140127122402", result.get("Property_wlan4_time_bounded_5"), 1E-15);
    }

    @Ignore
    @Test
    public void testPRISMExportedWLANTimeBounded_5() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
		constants.put("TRANS_TIME_MAX", "10");
    	constants.put("DEADLINE", "100");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(WLAN_TIME_BOUNDED_MODEL, 5)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.846221923828125", result.get("Property_wlan5_time_bounded_0"), 1E-15);
        assertEquals("0.846221923828125", result.get("Property_wlan5_time_bounded_1"), 1E-15);
        assertEquals("0.9836365208029747", result.get("Property_wlan5_time_bounded_2"), 1E-15);
        assertEquals("0.9836365208029747", result.get("Property_wlan5_time_bounded_3"), 1E-15);
        assertEquals("0.9002140127122402", result.get("Property_wlan5_time_bounded_4"), 1E-15);
        assertEquals("0.9002140127122402", result.get("Property_wlan5_time_bounded_5"), 1E-15);
    }

    @Ignore
    @Test
    public void testPRISMExportedWLANTimeBounded_6() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
		constants.put("TRANS_TIME_MAX", "10");
    	constants.put("DEADLINE", "100");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(String.format(WLAN_TIME_BOUNDED_MODEL, 6)));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.846221923828125", result.get("Property_wlan6_time_bounded_0"), 1E-15);
        assertEquals("0.846221923828125", result.get("Property_wlan6_time_bounded_1"), 1E-15);
        assertEquals("0.9836365208029747", result.get("Property_wlan6_time_bounded_2"), 1E-15);
        assertEquals("0.9836365208029747", result.get("Property_wlan6_time_bounded_3"), 1E-15);
        assertEquals("0.9002140127122402", result.get("Property_wlan6_time_bounded_4"), 1E-15);
        assertEquals("0.9002140127122402", result.get("Property_wlan6_time_bounded_5"), 1E-15);
    }

//    Fails while computing rewards
    @Ignore
    @Test
    public void testPRISMExportedZeroconf() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("err", "0");
    	constants.put("K", "4");
    	constants.put("reset", "true");
    	constants.put("N", "1000");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(ZEROCONF_MODEL));
        
        Map<String, Value> result = computeResultsMapName(model);
//        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
//        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }

    @Test
    public void testPRISMExportedZeroconfTimeBounded() throws EPMCException {
    	Map<String, Object> constants = new LinkedHashMap<>();
    	constants.put("T", "11");
    	constants.put("K", "1");
    	constants.put("bound", "10");
    	constants.put("reset", "true");
    	constants.put("N", "1000");
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = null;
        model = loadModel(options, getJANIFilenameFromPRISMFilename(ZEROCONF_TIME_BOUNDED_MODEL));
        
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("2.3447760329422196E-5", result.get("Property_zeroconf_time_bounded_0"), 1E-15);
        assertEquals("2.3447760329422196E-5", result.get("Property_zeroconf_time_bounded_1"), 1E-15);
        assertEquals("0.014275054203184579", result.get("Property_zeroconf_time_bounded_2"), 1E-15);
        assertEquals("0.014275054203184579", result.get("Property_zeroconf_time_bounded_3"), 1E-15);
    }


//    Old tests, based on an incompatible version of JANI models
    /**
     * Test for BEB model from Arnd Hartmanns.
     * 
     * @throws EPMCException thrown in case of problems
     */
    @Ignore
    @Test
    public void bebTest() throws EPMCException {
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        Model model = null;
        model = loadModel(options, BEB);
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.9166259765625", result.get("LineSeized"), 1E-15);
        assertEquals("0.0833740234375", result.get("GaveUp"), 1E-15);
    }
    
    @Ignore
    @Test
    public void diceTest() throws EPMCException {
        Options options = prepareJANIOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        Model model = null;
        model = loadModel(options, DICE);
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }
}
