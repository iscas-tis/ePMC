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

package epmc.command;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.command.OptionsCommandExpression2Automaton;
import epmc.error.EPMCException;
import epmc.main.options.UtilOptionsEPMC;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.plugin.OptionsPlugin;

import static epmc.modelchecker.TestHelper.*;

import java.util.HashMap;
import java.util.Map;

public final class Expression2CommandTest {
	private final static String USER_DIR = TestHelper.USER_DIR;
    private final static String PLUGIN_DIR = System.getProperty(USER_DIR) + "/target/classes/";

    @BeforeClass
    public static void initialise() {
        prepare();
    }

    private final static Options prepareExpression2CommandOptions() throws EPMCException {
        Options options = UtilOptionsEPMC.newOptions();
        options.set(OptionsPlugin.PLUGIN, PLUGIN_DIR);
        prepareOptions(options, LogType.TRANSLATE, TestHelper.MODEL_INPUT_TYPE_PRISM);
        return options;
    }
    
    @Test
    public void asdfTest() throws EPMCException {
        Options options = prepareExpression2CommandOptions();
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        Map<String,String> constants = new HashMap<>();
        options.set(OptionsModelChecker.CONST, constants);
        options.set(Options.COMMAND, OptionsCommandExpression2Automaton.EXPRESSION2AUTOMATON);
        options.set(OptionsCommandExpression2Automaton.AUTOMATON_EXPRESSION2TYPE, "schewe-rabin");
 //       execute(options, "Pmax=? [ (!\"z1\") U (\"z2\")  ] ");
//        execute(options, "P>=1 [ (F (\"z1\"))  ]");
//        execute(options, "P>=1 [ (G(F (\"z1\")))  ]");
//        execute(options, "P>=1 [ (G(F (\"z1\"))) & (G(F (\"z2\"))) ]");
//        execute(options, "P>=1 [ (G(F (\"z1\"))) & (G(F (\"z2\"))) & (G(F (\"z3\"))) & (G(F (\"z4\"))) ]");
//        execute(options, "P>=1 [ F (\"z1\" & (F(\"z2\")))  ]");
//        execute(options, "P>=1 [ G(F (\"z1\" & (F(\"z2\"))))  ]");
//        execute(options, "P>=1 [ ((!\"z1\") U (\"z2\")) & ((!\"z4\") U (\"z2\")) ]");
//        execute(options, "P>=1 [ (G(a)) & (F(b)) ]");
//        execute(options, "P>=1 [ (G(a)) & (G(F(b))) ]");
//        execute(options, "P>=1 [ (G(a)) & (F(G(b))) ]");
//        execute(options, "P>=1 [ (G(F (\"z1\" & (F(\"z2\" & (F(\"z4\" & (F(\"z3\"))))))))) ]");
//        execute(options, "P>=1 [ !((F(G (a))) & (G(F(!a)))) ]");

//        assertEquals("0.96484375", result, 1E-8);
    }
}
