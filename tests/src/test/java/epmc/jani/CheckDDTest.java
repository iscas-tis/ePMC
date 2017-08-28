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

import epmc.jani.model.ModelJANI;
import epmc.main.options.UtilOptionsEPMC;
import epmc.modelchecker.EngineDD;
import epmc.modelchecker.Model;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.plugin.OptionsPlugin;
import epmc.value.Value;

import static epmc.jani.ModelNames.*;
import static epmc.modelchecker.TestHelper.*;

import java.util.Map;

/**
 * Tests for model checking of JANI models.
 * 
 * @author Ernst Moritz Hahn
 */
public final class CheckDDTest {
    private final static String USER_DIR = "user.dir";
    private final static String TARGET_CLASSES = "/target/classes/";

    /** Location of plugin directory in file system. */
    private final static String PLUGIN_DIR = System.getProperty(USER_DIR) + TARGET_CLASSES;

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
     */
    private final static Options prepareJANIOptions() {
        Options options = UtilOptionsEPMC.newOptions();
        options.set(OptionsPlugin.PLUGIN, PLUGIN_DIR);
        prepareOptions(options, ModelJANI.IDENTIFIER);
        return options;
    }

    /**
     * Test for BEB model from Arnd Hartmanns.
     * 
     */
    @Test
    public void bebTest() {
        Options options = prepareJANIOptions();
        prepareOptions(options, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        Model model = null;
        model = loadModel(options, BEB);
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("0.9166259765625", result.get("LineSeized"), 1E-15);
        assertEquals("0.0833740234375", result.get("GaveUp"), 1E-15);
    }

    @Test
    public void diceTest() {
        Options options = prepareJANIOptions();
        prepareOptions(options, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelJANI.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        Model model = null;
        model = loadModel(options, DICE);
        Map<String, Value> result = computeResultsMapName(model);
        assertEquals("1/6", result.get("ProbThrowSix"), 1E-15);
        assertEquals("11/3", result.get("StepsUntilReach"), 1E-15);
    }
}
