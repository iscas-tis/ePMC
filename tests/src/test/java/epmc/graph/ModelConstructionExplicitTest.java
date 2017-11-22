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

package epmc.graph;

import static epmc.ModelNamesOwn.*;
import static epmc.graph.TestHelperGraph.exploreModel;
import static epmc.modelchecker.TestHelper.assertEquals;
import static epmc.modelchecker.TestHelper.close;
import static epmc.modelchecker.TestHelper.prepare;
import static epmc.modelchecker.TestHelper.prepareOptions;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.ModelNamesPRISM;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.ExploreStatistics;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;

public class ModelConstructionExplicitTest {
    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void labelsTypeTest() {
        Options options = prepareOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        ExploreStatistics result;
        result = exploreModel(options, SIMPLE_QUEUE);
        assertEquals("61", result.getNumStates());
        assertEquals("61", result.getNumNodes());
        assertEquals("120", result.getNumTransitions());
        close(options);
    }

    @Test
    public void er12_1Test() {
        Options options = prepareOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.PRISM_FLATTEN, false);
        ExploreStatistics result;
        result = exploreModel(options, ER12_1);
        assertEquals("6747", result.getNumStates());
        assertEquals("6747", result.getNumNodes());
        assertEquals("165206", result.getNumTransitions());
        close(options);
    }

    @Test
    public void thinkteamRetrialTest() {
        Options options = prepareOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.PRISM_FLATTEN, false);
        ExploreStatistics result;
        result = exploreModel(options, ModelNamesPRISM.THINKTEAM_RETRIAL_MODEL);
        assertEquals("19", result.getNumStates());
        assertEquals("19", result.getNumNodes());
        assertEquals("57", result.getNumTransitions());
        options.set(TestHelper.PRISM_FLATTEN, true);
        result = exploreModel(options, ModelNamesPRISM.THINKTEAM_RETRIAL_MODEL);
        assertEquals("19", result.getNumStates());
        assertEquals("19", result.getNumNodes());
        assertEquals("57", result.getNumTransitions());
        close(options);
    }

    @Test
    public void hermanTest() {
        ExploreStatistics result;
        Options options = prepareOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);        
        options.set(TestHelper.PRISM_FLATTEN, false);

        result = exploreModel(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 3));
        assertEquals("8", result.getNumStates());
        assertEquals("8", result.getNumNodes());
        assertEquals("28", result.getNumTransitions());

        options.set(TestHelper.PRISM_FLATTEN, true);
        result = exploreModel(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 3));
        assertEquals("8", result.getNumStates());
        assertEquals("8", result.getNumNodes());
        assertEquals("28", result.getNumTransitions());

        options.set(TestHelper.PRISM_FLATTEN, true);
        result = exploreModel(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 5));
        assertEquals("32", result.getNumStates());
        assertEquals("32", result.getNumNodes());
        assertEquals("244", result.getNumTransitions());

        options.set(TestHelper.PRISM_FLATTEN, false);
        result = exploreModel(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 7));
        assertEquals("128", result.getNumStates());
        assertEquals("128", result.getNumNodes());
        assertEquals("2188", result.getNumTransitions());

        options.set(TestHelper.PRISM_FLATTEN, true);
        result = exploreModel(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 7));
        assertEquals("128", result.getNumStates());
        assertEquals("128", result.getNumNodes());
        assertEquals("2188", result.getNumTransitions());

        options.set(TestHelper.PRISM_FLATTEN, false);
        result = exploreModel(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 9));
        assertEquals("512", result.getNumStates());
        assertEquals("512", result.getNumNodes());
        assertEquals("19684", result.getNumTransitions());

        options.set(TestHelper.PRISM_FLATTEN, true);
        result = exploreModel(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 9));
        assertEquals("512", result.getNumStates());
        assertEquals("512", result.getNumNodes());
        assertEquals("19684", result.getNumTransitions());

        options.set(TestHelper.PRISM_FLATTEN, false);
        result = exploreModel(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 11));
        assertEquals("2048", result.getNumStates());
        assertEquals("2048", result.getNumNodes());
        assertEquals("177148", result.getNumTransitions());

        options.set(TestHelper.PRISM_FLATTEN, true);
        result = exploreModel(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 11));
        assertEquals("2048", result.getNumStates());
        assertEquals("2048", result.getNumNodes());
        assertEquals("177148", result.getNumTransitions());

        /*
        options.set(Options.FLATTEN, false);
        result = exploreModel(options, HERMAN13);
        assertEquals("8192", result.getNumStates());
        assertEquals("8192", result.getNumNodes());
        assertEquals("1594324", result.getNumTransitions());

        options.set(Options.FLATTEN, true);
        result = exploreModel(options, HERMAN13);
        assertEquals("8192", result.getNumStates());
        assertEquals("8192", result.getNumNodes());
        assertEquals("1594324", result.getNumTransitions());

        options.set(Options.FLATTEN, false);
        result = exploreModel(options, HERMAN15);
        assertEquals("32768", result.getNumStates());
        assertEquals("32768", result.getNumNodes());
        assertEquals("14348908", result.getNumTransitions());

        options.set(Options.FLATTEN, true);
        result = exploreModel(options, HERMAN15);
        assertEquals("32768", result.getNumStates());
        assertEquals("32768", result.getNumNodes());
        assertEquals("14348908", result.getNumTransitions());

        options.set(Options.FLATTEN, false);
        result = exploreModel(options, HERMAN17);
        assertEquals("131072", result.getNumStates());
        assertEquals("131072", result.getNumNodes());
        assertEquals("129140164", result.getNumTransitions());

        options.set(Options.FLATTEN, true);
        result = exploreModel(options, HERMAN17);
        assertEquals("131072", result.getNumStates());
        assertEquals("131072", result.getNumNodes());
        assertEquals("129140164", result.getNumTransitions());

        options.set(Options.FLATTEN, false);
        result = exploreModel(options, HERMAN19);
        assertEquals("524288", result.getNumStates());
        assertEquals("524288", result.getNumNodes());
        assertEquals("1162261468", result.getNumTransitions());

        options.set(Options.FLATTEN, true);
        result = exploreModel(options, HERMAN19);
        assertEquals("524288", result.getNumStates());
        assertEquals("524288", result.getNumNodes());
        assertEquals("1162261468", result.getNumTransitions());

        options.set(Options.FLATTEN, false);
        result = exploreModel(options, HERMAN21);
        assertEquals("2097152", result.getNumStates());
        assertEquals("2097152", result.getNumNodes());
        assertEquals("10460353204", result.getNumTransitions());

        options.set(Options.FLATTEN, true);
        result = exploreModel(options, HERMAN21);
        assertEquals("2097152", result.getNumStates());
        assertEquals("2097152", result.getNumNodes());
        assertEquals("10460353204", result.getNumTransitions());
         */
        close(options);
    }

    @Test
    public void ijTest() {
        ExploreStatistics result;
        Options options = prepareOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);

        options.set(TestHelper.PRISM_FLATTEN, false);
        result = exploreModel(options, String.format(ModelNamesPRISM.IJ_MODEL, 3));
        assertEquals("7", result.getNumStates());
        assertEquals("19", result.getNumNodes());
        assertEquals("36", result.getNumTransitions());

        options.set(TestHelper.PRISM_FLATTEN, true);
        result = exploreModel(options, String.format(ModelNamesPRISM.IJ_MODEL, 3));
        assertEquals("7", result.getNumStates());
        assertEquals("19", result.getNumNodes());
        assertEquals("36", result.getNumTransitions());

        options.set(TestHelper.PRISM_FLATTEN, false);
        result = exploreModel(options, String.format(ModelNamesPRISM.IJ_MODEL, 7));
        assertEquals("127", result.getNumStates());
        assertEquals("575", result.getNumNodes());
        assertEquals("1344", result.getNumTransitions());

        options.set(TestHelper.PRISM_FLATTEN, true);
        result = exploreModel(options, String.format(ModelNamesPRISM.IJ_MODEL, 7));
        assertEquals("127", result.getNumStates());
        assertEquals("575", result.getNumNodes());
        assertEquals("1344", result.getNumTransitions());
        close(options);
    }

    @Test
    public void testAndSetTest() {
        Options options = prepareOptions();
        ExploreStatistics result;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);

        options.set(TestHelper.PRISM_FLATTEN, false);
        result = exploreModel(options, ModelNamesPRISM.TEST_AND_SET_MODEL);
        assertEquals("196", result.getNumStates());
        assertEquals("650", result.getNumNodes());
        assertEquals("920", result.getNumTransitions());

        options.set(TestHelper.PRISM_FLATTEN, true);
        result = exploreModel(options, ModelNamesPRISM.TEST_AND_SET_MODEL);
        assertEquals("196", result.getNumStates());
        assertEquals("650", result.getNumNodes());
        assertEquals("920", result.getNumTransitions());
        close(options);
    }

    @Test
    public void brpTest() {
        Options options = prepareOptions();
        ExploreStatistics result;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);

        Map<String,Object> constants = new HashMap<>();

        constants.put("N", "200");
        constants.put("MAX", "512");
        options.set(OptionsModelChecker.CONST, constants);
        options.set(TestHelper.PRISM_FLATTEN, false);
        result = exploreModel(options, ModelNamesPRISM.BRP_MODEL);
        System.out.println(result);
        close(options);
    }

    @Test
    public void clusterGTest() {
        Options options = prepareOptions();
        Map<String,Object> constants = new HashMap<>();
        double tolerance = 1E-13;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set("prism-flatten", true);
        constants.put("N", "256");
        options.set(OptionsModelChecker.CONST, constants);

        ExploreStatistics result = exploreModel(options, ModelNamesPRISM.CLUSTER_MODEL);
        System.out.println(result);
        close(options);
    }

}
