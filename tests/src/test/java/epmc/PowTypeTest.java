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

import static epmc.modelchecker.TestHelper.*;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.graph.TestHelperGraph;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.ExploreStatistics;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;

/**
 * Checks whether runtime errors occur when using pow operator.
 * 
 * @author Ernst Moritz Hahn
 */
public final class PowTypeTest {

    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void powTypeTest() {
        Options options = prepareOptions();
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.PRISM_FLATTEN, false);
        ExploreStatistics statistics = TestHelperGraph.exploreModel(options, ModelNamesOwn.POW_TYPE);
        System.out.println(statistics);
        close(options);
    }
}
