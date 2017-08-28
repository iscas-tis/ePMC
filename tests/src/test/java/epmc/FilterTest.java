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

import static epmc.ModelNamesOwn.*;
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
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.value.Value;

public class FilterTest {

    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void chainTest() {
        Options options = prepareOptions();
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        chainTestRun(options);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        chainTestRun(options);
        close(options);
    }

    private void chainTestRun(Options options) {
        Value result;
        double tolerance = 1E-10;
        Map<String,Object> constants = new HashMap<>();
        constants.put("N", "10");
        constants.put("M", "2");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, CHAIN, "filter(sum,s,true)");
        assertEquals(165, result, tolerance);

        constants.put("N", "10");
        constants.put("M", "1");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, CHAIN, "filter(sum,s*s,true)");
        assertEquals(770, result, tolerance);

        constants.put("N", "10");
        constants.put("M", "3");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, CHAIN, "filter(sum,s*s+s+a,true)");
        assertEquals(1826, result, tolerance);

        constants.put("N", "10");
        constants.put("M", "3");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, CHAIN, "filter(max,s*s+s+a,true)");
        assertEquals(113, result, tolerance);

        constants.put("N", "10");
        constants.put("M", "3");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, CHAIN, "filter(min,(s+1)*(s-1)+s+a,true)");
        assertEquals(-1, result, tolerance);
    }
}
