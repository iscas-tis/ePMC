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
import static epmc.modelchecker.TestHelper.prepare;
import static epmc.modelchecker.TestHelper.prepareOptions;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.ExploreStatistics;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;

public class MarkovAutomataTest {
    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void constructionSimpleSingleModuleTest() {
        Options options = prepareOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        ExploreStatistics result;
        result = exploreModel(options, MA_SINGLEMODULE);
        System.out.println(result);
        result = exploreModel(options, MA_SINGLEMODULE_TWORATE);
        System.out.println(result);
        result = exploreModel(options, MA_TWOMODULES);
        System.out.println(result);
        result = exploreModel(options, MA_DISABLING_RATE);
        System.out.println(result);
    }
}
