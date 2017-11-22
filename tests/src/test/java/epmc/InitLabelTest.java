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

import static epmc.modelchecker.TestHelper.assertEquals;
import static epmc.modelchecker.TestHelper.close;
import static epmc.modelchecker.TestHelper.computeResult;
import static epmc.modelchecker.TestHelper.prepare;
import static epmc.modelchecker.TestHelper.prepareOptions;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.modelchecker.EngineDD;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.value.Value;

public class InitLabelTest {

    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void herman03Test() {
        Options options = prepareOptions();
        Value result;
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        Map<String,Object> constants = new HashMap<>();

        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 3), "filter(forall, \"init\" => P>=1 [ F (\"stable\") ])");
        assertEquals(true, result);
        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 3), "filter(count, \"init\")");
        assertEquals(8, result, tolerance);
        result = computeResult(options, String.format(ModelNamesPRISM.HERMAN_MODEL, 3), "filter(count, \"deadlock\")");
        assertEquals(0, result, tolerance);
        close(options);
    }
}
