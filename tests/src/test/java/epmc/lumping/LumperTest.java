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

package epmc.lumping;

import static epmc.ModelNamesOwn.*;
import static epmc.graph.TestHelperGraph.*;
import static epmc.modelchecker.TestHelper.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.ModelNamesPRISM;
import epmc.graph.explicit.GraphExplicit;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.ExploreStatistics;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;

public final class LumperTest {

    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void diceTest() {
        Options options = prepareOptions();
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        GraphExplicit result1 = TestHelperLump.computeQuotient(options, ModelNamesPRISM.DICE_MODEL, "P=?[(F((s=7)&(d=1)))]");
        System.out.println(result1.computeNumStates());
        GraphExplicit result2 = TestHelperLump.computeQuotient(options, ModelNamesPRISM.DICE_MODEL, "P=?[(F((s=7)&(d=2)))]");
        System.out.println(result2.computeNumStates());
        GraphExplicit result3 = TestHelperLump.computeQuotient(options, ModelNamesPRISM.DICE_MODEL, "P=?[(F((s=7)&(d=3)))]");
        System.out.println(result3.computeNumStates());
        GraphExplicit result4 = TestHelperLump.computeQuotient(options, ModelNamesPRISM.DICE_MODEL, "P=?[(F((s=7)&(d=4)))]");
        System.out.println(result4.computeNumStates());
        GraphExplicit result5 = TestHelperLump.computeQuotient(options, ModelNamesPRISM.DICE_MODEL, "P=?[(F((s=7)&(d=5)))]");
        System.out.println(result5.computeNumStates());
        GraphExplicit result6 = TestHelperLump.computeQuotient(options, ModelNamesPRISM.DICE_MODEL, "P=?[(F((s=7)&(d=6)))]");
        System.out.println(result6.computeNumStates());
        GraphExplicit shouldBeTrue = TestHelperLump.computeQuotient(options, ModelNamesPRISM.DICE_MODEL, "P>=1 [F (s=7) ]");
        System.out.println(shouldBeTrue.computeNumStates());
        close(options);
    }

    @Test
    public void cellTest() {
        Options options = prepareOptions();
        double tolerance = 1E-10;
        GraphExplicit result;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        Map<String,Object> constants = new HashMap<>();

        constants.put("N", "50");
        constants.put("T", "1");
        options.set(OptionsModelChecker.CONST, constants);
        result = TestHelperLump.computeQuotient(options, ModelNamesPRISM.CELL_MODEL, "P=?[ true U<=T (n=N) {n<N}{max} ]");
        System.out.println(result.computeNumStates());

        constants.put("N", "50");
        constants.put("T", "10");
        options.set(OptionsModelChecker.CONST, constants);
        result = TestHelperLump.computeQuotient(options, ModelNamesPRISM.CELL_MODEL, "P=?[ true U<=T (n=N) {n<N}{max} ]");
        System.out.println(result.computeNumStates());

        constants.put("N", "50");
        constants.put("T", "100");
        options.set(OptionsModelChecker.CONST, constants);
        result = TestHelperLump.computeQuotient(options, ModelNamesPRISM.CELL_MODEL, "P=?[ true U<=T (n=N) {n<N}{max} ]");
        System.out.println(result.computeNumStates());

        constants.put("N", "50");
        constants.put("T", "1000");
        options.set(OptionsModelChecker.CONST, constants);
        result = TestHelperLump.computeQuotient(options, ModelNamesPRISM.CELL_MODEL, "P=?[ true U<=T (n=N) {n<N}{max} ]");
        System.out.println(result.computeNumStates());
        close(options);
    }

    @Test
    public void clusterTest() {
        Options options = prepareOptions();
        GraphExplicit result;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Map<String,Object> constants = new HashMap<>();
        constants.put("N", "1");
        options.set(OptionsModelChecker.CONST, constants);
        result = TestHelperLump.computeQuotient(options, ModelNamesPRISM.CLUSTER_MODEL, "filter(min, P=? [ (\"minimum\") U<=0.5 (\"premium\")  ], \"minimum\");");
        System.out.println(result.computeNumStates());

        ExploreStatistics statistics = exploreModel(options, ModelNamesPRISM.CLUSTER_MODEL);
        System.out.println(statistics.getNumStates());


        constants.put("N", "128");
        options.set(OptionsModelChecker.CONST, constants);
        result = TestHelperLump.computeQuotient(options, ModelNamesPRISM.CLUSTER_MODEL, "filter(min, P=? [ (\"minimum\") U<=0.5 (\"premium\")  ], \"minimum\");");
        System.out.println(result.computeNumStates());

        /*
        constants.put("N", "256");
        options.set(OptionsModelChecker.CONST, constants);
        result = TestHelperLump.computeQuotient(options, CLUSTER_MODEL, "filter(min, P=? [ (\"minimum\") U<=0.5 (\"premium\")  ], \"minimum\");");
        System.out.println(result.computeNumStates());
         */

        close(options);
    }

    @Test
    public void cyclinTest() {
        Options options = prepareOptions();
        GraphExplicit result;
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Map<String,Object> constants = new HashMap<>();
        constants.put("N", "3");
        constants.put("k", "10");
        options.set(OptionsModelChecker.CONST, constants);
        result = TestHelperLump.computeQuotient(options, ModelNamesPRISM.CYCLIN_MODEL, "P=? [ true U[1,1] cyclin_bound=k ];");
        System.out.println(result.computeNumStates());
        ExploreStatistics statistics = exploreModel(options, ModelNamesPRISM.CYCLIN_MODEL);
        System.out.println(statistics.getNumStates());
        close(options);
    }        

    @Test
    public void er12_1Test() {
        Options options = prepareOptions();
        double tolerance = 1E-13;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.PRISM_FLATTEN, false);

        GraphExplicit result = TestHelperLump.computeQuotient(options, ER12_1, "P=?[G<=10 num_affected<=4]");
        ExploreStatistics statistics = exploreModel(options, ER12_1);
        System.out.println(result.computeNumStates());
        System.out.println(statistics.getNumStates());
        close(options);
    }

}
