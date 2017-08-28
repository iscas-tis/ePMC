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
import static epmc.modelchecker.TestHelper.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.ModelNamesPRISM;
import epmc.modelchecker.EngineDD;
import epmc.modelchecker.ExploreStatistics;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.value.Value;

public class ModelConstructionDDTest {

    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void noDDVariablesAtAllTest() {
        Options options = prepareOptions();
        Value result;
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        Map<String,Object> constants = new HashMap<>();
        constants.put("N", "0");
        constants.put("M", "0");        
        options.set(OptionsModelChecker.CONST, constants);

        result = computeResult(options, CHAIN, "Pmax=?[ F (s=0) ]");
        assertEquals(1, result, tolerance);
        close(options);
    }

    @Test
    public void clusterTest() {
        Options options = prepareOptions();
        Map<String,Object> constants = new HashMap<>();
        double tolerance = 1E-13;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        constants.put("N", "2");
        options.set(OptionsModelChecker.CONST, constants);

        ExploreStatistics result = exploreModel(options, ModelNamesPRISM.CLUSTER_MODEL);
        System.out.println(result);
        close(options);
    }

    @Test
    public void googleTest() {
        Options options = prepareOptions();
        Map<String,Object> constants = new HashMap<>();
        double tolerance = 1E-13;
        ExploreStatistics result;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);

        constants.put("M", "50");
        options.set(OptionsModelChecker.CONST, constants);
        result = exploreModel(options, RANDOM_GOOGLE);
        assertEquals(15006, result.getNumNodes());
        assertEquals(15006, result.getNumStates());

        constants.put("M", "100");
        options.set(OptionsModelChecker.CONST, constants);
        result = exploreModel(options, RANDOM_GOOGLE);
        assertEquals(60006, result.getNumNodes());
        assertEquals(60006, result.getNumStates());

        constants.put("M", "150");
        options.set(OptionsModelChecker.CONST, constants);
        result = exploreModel(options, RANDOM_GOOGLE);
        assertEquals(135006, result.getNumNodes());
        assertEquals(135006, result.getNumStates());

        close(options);
    }


}
