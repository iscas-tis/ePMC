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

package epmc.propertysolvercoalition;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import epmc.coalition.options.OptionsCoalition;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionTypeInteger;
import epmc.expression.standard.SMGPlayer;
import epmc.graph.CommonProperties;
import epmc.jani.ConvertTestConfiguration;
import epmc.jani.ConvertTestStatistics;
import epmc.jani.model.UtilModelParser;
import epmc.main.options.UtilOptionsEPMC;
import epmc.modelchecker.EngineDD;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.plugin.OptionsPlugin;
import epmc.value.Value;

import static epmc.modelchecker.TestHelper.*;
import static epmc.propertysolvercoalition.ModelNames.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ConvertTest {
    private final static String USER_DIR = TestHelper.USER_DIR;
    private final static String TARGET_CLASSES = "/target/classes/";
    private final static String PLUGIN_DIR = System.getProperty(USER_DIR) + TARGET_CLASSES;
    private final static String PLAYER = "PLAYER";

    @BeforeClass
    public static void initialise() {
        prepare();
    }

    private final static Options prepareCoalitionOptions() {
        Options options = UtilOptionsEPMC.newOptions();
        options.set(OptionsPlugin.PLUGIN, PLUGIN_DIR);
        prepareOptions(options, LogType.TRANSLATE, TestHelper.MODEL_INPUT_TYPE_PRISM);
        return options;
    }

    @Test
    public void twoInvestorsTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(TWO_INVESTORS)
                //    			.setExploreAll()
                .run();
        //  	System.out.println(statistics);
        //    	System.out.println(UtilModelParser.prettyString(statistics.getJaniModel()));
        Set<Object> nodeProperties = new HashSet<>();
        nodeProperties.add(CommonProperties.STATE);
        nodeProperties.add(newPlayer(1));
        nodeProperties.add(newPlayer(2));
        nodeProperties.add(newPlayer(3));
        //    	GraphExplicit graph = exploreToGraph(statistics.getJaniModel(), nodeProperties);
        //    	System.out.println(graph);
    }

    @Test
    public void robotsTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName("/Users/emhahn/svn/papers-iscas/working/sven/parity/experiments/robots.prism")
                .putConstant("size", 8)
                .setPrismFlatten(true)
                .setExploreJANI(true)
                .run();
        System.out.println(UtilModelParser.prettyString(statistics.getJaniModel()));
        //     	System.out.println(computeResult(statistics.getJaniModel(), "<<1>> P>=1 [ (F (\"z1\")) & (F (\"z2\")) ]"));
        //     	System.out.println(computeResult(statistics.getJaniModel(), "<<1>> P>=1 [ F true ]"));
    }


    @Test
    public void smallTest() {
        ConvertTestStatistics statistics = new ConvertTestConfiguration()
                .setModelName(ROBOTS)
                //    			.setExploreAll()
                .putConstant("size", 5)
                .run();
        System.out.println(statistics);
        Set<Object> nodeProperties = new HashSet<>();
        nodeProperties.add(CommonProperties.STATE);
        nodeProperties.add(newPlayer(1));
        nodeProperties.add(newPlayer(2));
        //    	GraphExplicit graph = exploreToGraph(statistics.getJaniModel(), nodeProperties);
        //  	System.out.println(graph);
    }

    @Test
    public void robotReachabilityTest() {
        Options options = prepareCoalitionOptions();
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        // options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Map<String,String> constants = new HashMap<>();
        Value result;

        constants.put("size", "6");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (F (\"z1\"))  ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (F (\"z1\"))  ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (F (\"z1\"))  ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (F (\"z1\")) & (F (\"z2\")) ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (F (\"z1\")) & (F (\"z2\")) ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (F (\"z1\")) & (F (\"z2\")) & (F (\"z3\")) ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (F (\"z1\")) & (F (\"z2\")) & (F (\"z3\")) ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (F (\"z1\")) & (F (\"z2\")) & (F (\"z3\")) & (F (\"z4\")) ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (F (\"z1\")) & (F (\"z2\")) & (F (\"z3\")) & (F (\"z4\")) ]");
        assertEquals(true, result);
        close(options);
    }

    @Test
    public void robotSmallRepeatedReachabilityTest() {
        Options options = prepareCoalitionOptions();
        //        options.set(Options.ENGINE, OptionsSet.Engine.DD);
        //      options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsCoalition.COALITION_SOLVER, "gadget");
        //        options.set(OptionsCoalition.COALITION_JURDZINSKY_CHOOSE_LIFT_NODES, OptionsCoalition.JurdzinskyChooseLiftNodes.ALL);
        Map<String,String> constants = new HashMap<>();
        Value result;

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (G(F (\"z1\")))  ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (G(F (\"z1\")))  ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (G(F (\"z1\"))) & (G(F (\"z2\"))) ]");
        assertEquals(true, result);
    }

    @Test
    public void robotRepeatedReachabilityTest() {
        Options options = prepareCoalitionOptions();
        //        options.set(Options.ENGINE, OptionsSet.Engine.DD);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        //        options.set(Options.ENGINE, OptionsSet.Engine.EXPLICIT);
        Map<String,String> constants = new HashMap<>();
        Value result;

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (G(F (\"z1\")))  ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (G(F (\"z1\")))  ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (G(F (\"z1\"))) & (G(F (\"z2\"))) ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (G(F (\"z1\"))) & (G(F (\"z2\"))) ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (G(F (\"z1\"))) & (G(F (\"z2\"))) & (G(F (\"z3\"))) ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (G(F (\"z1\"))) & (G(F (\"z2\"))) & (G(F (\"z3\"))) ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (G(F (\"z1\"))) & (G(F (\"z2\"))) & (G(F (\"z3\"))) & (G(F (\"z4\"))) ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (G(F (\"z1\"))) & (G(F (\"z2\"))) & (G(F (\"z3\"))) & (G(F (\"z4\"))) ]");
        assertEquals(true, result);
        close(options);
    }

    @Test
    public void robotOrderedReachabilityTest() {
        Options options = prepareCoalitionOptions();
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Map<String,Object> constants = new HashMap<>();
        Value result;

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ F (\"z1\" & (F(\"z2\")))  ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ F (\"z1\" & (F(\"z2\")))  ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (F (\"z1\" & (F(\"z2\" & (F(\"z4\"))))))  ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (F (\"z1\" & (F(\"z2\" & (F(\"z4\"))))))  ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ F (\"z1\" & (F(\"z2\" & (F(\"z4\" & (F(\"z3\")))))))  ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ F (\"z1\" & (F(\"z2\" & (F(\"z4\" & (F(\"z3\")))))))  ]");
        assertEquals(true, result);
        close(options);
    }

    @Ignore
    @Test
    public void robotRepeatedOrderedReachabilityTest() {
        Options options = prepareCoalitionOptions();
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineDD.class);
        //        options.set(OptionsDD.DD_BINARY_ENGINE, "buddy");
        Map<String,String> constants = new HashMap<>();
        Value result;

        constants.put("size", "20");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ G(F (\"z1\" & (F(\"z2\"))))  ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ G(F (\"z1\" & (F(\"z2\"))))  ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ G((F (\"z1\" & (F(\"z2\" & (F(\"z4\")))))))  ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ G((F (\"z1\" & (F(\"z2\" & (F(\"z4\")))))))  ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ G(F (\"z1\" & (F(\"z2\" & (F(\"z4\" & (F(\"z3\"))))))))  ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ G(F (\"z1\" & (F(\"z2\" & (F(\"z4\" & (F(\"z3\"))))))))  ]");
        assertEquals(true, result);

        constants.put("size", "10");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ G(F (\"z1\" & (F(\"z2\" & (F(\"z4\" & (F(\"z3\"))))))))  ]");
        assertEquals(true, result);
        close(options);
    }


    @Test
    public void robotSmallReachAvoidTest() {
        Options options = prepareCoalitionOptions();
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(OptionsCoalition.COALITION_SOLVER, "gadget");
        options.set(OptionsCoalition.COALITION_SOLVER_NON_STOCHASTIC, "jurdzinski");
        //        options.set(OptionsCoalition.COALITION_JURDZINSKY_CHOOSE_LIFT_NODES, OptionsCoalition.JurdzinskyChooseLiftNodes.ALL);

        //        options.set(Options.DD_BINARY_ENGINE, OptionsSet.DdBinaryEngine.BUDDY);
        //        options.set(Options.MDP_ENCODING_MODE, OptionsSet.MDPEncoding.STATE_DISTRIBUTION);
        Map<String,String> constants = new HashMap<>();

        Value result;

        //        constants.put("size", "2");
        //      options.set(OptionsEPMC.CONST, constants);
        //    result = computeResult(options, ROBOTS_SMALL, "<<1>> P>=1 [ true U (\"z2\") ]");
        //  assertEquals(false, result);

        //if (true) {
        //return;
        // }

        constants.put("size", "3");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS_SMALL, "<<1>> P>=1 [ true U (\"z2\") ]");
        assertEquals(false, result);

        if (true) {
            return;
        }

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS_SMALL, "<<1>> P>=1 [ (!\"z1\") U (\"z2\") ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS_SMALL, "<<1>> P>0 [ (!\"z1\") U (\"z2\") ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS_SMALL, "<<1>> P>0 [ (!\"z1\") U (\"z2\") ]");
        assertEquals(true, result);
    }

    @Test
    public void robotSmallModifiedReachAvoidTest() {
        Options options = prepareCoalitionOptions();
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        //        options.set(OptionsCoalition.COALITION_SOLVER, "gadget");
        //        options.set(OptionsCoalition.COALITION_SOLVER_NON_STOCHASTIC, "jurdzinski");
        //        options.set(OptionsCoalition.COALITION_JURDZINSKY_LIFT_ORDER, "lifo");
        //        options.set(OptionsCoalition.COALITION_JURDZINSKY_CHOOSE_LIFT_NODES, OptionsCoalition.JurdzinskyChooseLiftNodes.ALL);

        //        options.set(Options.DD_BINARY_ENGINE, OptionsSet.DdBinaryEngine.BUDDY);
        //        options.set(Options.MDP_ENCODING_MODE, OptionsSet.MDPEncoding.STATE_DISTRIBUTION);
        Map<String,String> constants = new HashMap<>();

        Value result;

        //        constants.put("size", "3");
        //      options.set(OptionsEPMC.CONST, constants);
        //        result = computeResult(options, ROBOTS_MODIFIED_SMALL, "<<1>> P>=1 [ true U (\"z2\") ]");
        //  assertEquals(false, result);

        //if (true) {
        //return;
        // }

        constants.put("size", "6");
        options.set(OptionsModelChecker.CONST, constants);
        //        result = computeResult(options, ROBOTS_MODIFIED_SMALL, "<<1>> P>=1 [ true U (\"z2\") ]");
        result = computeResult(options, ROBOTS_MODIFIED_MEDIUM, "<<1>> P>=1 [ (G(F(\"z1\"))) & (G(F(\"z2\"))) & (G(F(\"z4\"))) & (G(F(\"z3\"))) ]");

        //        assertEquals(false, result);

        if (true) {
            return;
        }

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS_SMALL, "<<1>> P>=1 [ (!\"z1\") U (\"z2\") ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS_SMALL, "<<1>> P>0 [ (!\"z1\") U (\"z2\") ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS_SMALL, "<<1>> P>0 [ (!\"z1\") U (\"z2\") ]");
        assertEquals(true, result);
    }

    @Test
    public void robotReachAvoidTest() {
        Options options = prepareCoalitionOptions();
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        //        options.set(OptionsCoalition.COALITION_SOLVER, "gadget");

        //        options.set(Options.DD_BINARY_ENGINE, OptionsSet.DdBinaryEngine.BUDDY);
        //        options.set(Options.MDP_ENCODING_MODE, OptionsSet.MDPEncoding.STATE_DISTRIBUTION);
        Map<String,String> constants = new HashMap<>();

        Value result;

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (!\"z1\") U (\"z2\") ]");
        assertEquals(false, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ (!\"z1\") U (\"z2\") ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>0 [ (!\"z1\") U (\"z2\") ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>0 [ (!\"z1\") U (\"z2\") ]");
        assertEquals(true, result);

        if (true) return;

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1,2>> P>=1 [ (!\"z1\") U (\"z2\")  ]");
        assertEquals(true, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1,2>> P>=1 [ (!\"z1\") U (\"z2\")  ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ ((!\"z1\") U (\"z2\")) & ((!\"z4\") U (\"z2\")) ]");
        assertEquals(false, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ ((!\"z1\") U (\"z2\")) & ((!\"z4\") U (\"z2\")) ]");
        assertEquals(false, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ ((!\"z1\") U (\"z2\")) & ((!\"z4\") U (\"z2\")) & ((!\"z4\") U (\"z1\")) ]");
        assertEquals(false, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ ((!\"z1\") U (\"z2\")) & ((!\"z4\") U (\"z2\")) & ((!\"z4\") U (\"z1\")) ]");
        assertEquals(false, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ ((!\"z1\") U (\"z2\")) & ((!\"z4\") U (\"z2\")) & ((!\"z4\") U (\"z1\")) & (F(\"z4\")) ]");
        assertEquals(false, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>=1 [ ((!\"z1\") U (\"z2\")) & ((!\"z4\") U (\"z2\")) & ((!\"z4\") U (\"z1\")) & (F(\"z4\")) ]");
        assertEquals(false, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>0 [ ((!\"z1\") U (\"z2\")) & ((!\"z4\") U (\"z2\")) & ((!\"z4\") U (\"z1\")) & (F(\"z4\")) ]");
        assertEquals(false, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1>> P>0 [ ((!\"z1\") U (\"z2\")) & ((!\"z4\") U (\"z2\")) & ((!\"z4\") U (\"z1\")) & (F(\"z4\")) ]");
        assertEquals(true, result);

        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1,2>> P>=1 [ ((!\"z1\") U (\"z2\")) & ((!\"z4\") U (\"z2\")) ]");
        assertEquals(false, result);

        constants.put("size", "9");
        options.set(OptionsModelChecker.CONST, constants);
        result = computeResult(options, ROBOTS, "<<1,2>> P>=1 [ ((!\"z1\") U (\"z2\")) & ((!\"z4\") U (\"z2\")) ]");
        assertEquals(true, result);
        close(options);
    }

    private static SMGPlayer newPlayer(int i) {
        return new SMGPlayer() {

            @Override
            public Expression getExpression() {
                return new ExpressionLiteral.Builder()
                        .setValue(Integer.toString(i))
                        .setType(ExpressionTypeInteger.TYPE_INTEGER)
                        .build();
            }

            @Override
            public String toString() {
                return PLAYER + i;
            }
        };
    }
}
