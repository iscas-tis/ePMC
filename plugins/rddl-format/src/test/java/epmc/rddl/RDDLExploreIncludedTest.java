package epmc.rddl;

import static epmc.graph.TestHelperGraph.exploreModelGraph;
import static epmc.modelchecker.TestHelper.loadModelMulti;
import static epmc.modelchecker.TestHelper.prepare;
import static epmc.rddl.ModelNames.*;
import static epmc.rddl.RDDLTestHelper.prepareRDDLOptions;

import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import epmc.error.EPMCException;
import epmc.graph.explicit.GraphExplicit;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.Model;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.rddl.model.ModelRDDL;
import epmc.rddl.options.OptionsRDDL;
import epmc.rddl.options.RDDLIntRange;

public class RDDLExploreIncludedTest {
    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Ignore
    @Test
    // Status: works. Fast enough.
    public void dbn_propTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        GraphExplicit result;
        result = exploreModelGraph(options, DBN_PROP);
        System.out.println(result);
    }
    
    @Ignore
    @Test
    // Status: works. Fast enough.
    public void dbn_types_interm_poTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        GraphExplicit result = exploreModelGraph(options, DBN_TYPES_INTERM_PO);
        System.out.println(result);
    }
    
    @Ignore
    @Test
    // Status: works. Speed OK, could be somewhat faster given number of states.
    public void game_of_life_determTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        GraphExplicit result = exploreModelGraph(options, GAME_OF_LIFE_DETERM);
        System.out.println(result);
    }

    @Ignore
    @Test
    // Status: works. Should be faster.
    public void game_of_life_stochTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        GraphExplicit result = exploreModelGraph(options, GAME_OF_LIFE_STOCH);
        System.out.println(result);
    }
    
    @Ignore
    @Test
    // Status: seems to work. Quite slow, especially for larger instances.
    public void game_of_life_pomdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        String[] instances = new String[]{
                GAME_OF_LIFE_INST_POMDP__1,
                GAME_OF_LIFE_INST_POMDP__2,
                GAME_OF_LIFE_INST_POMDP__3,
                GAME_OF_LIFE_INST_POMDP__4,
                GAME_OF_LIFE_INST_POMDP__5,
                GAME_OF_LIFE_INST_POMDP__6,
                GAME_OF_LIFE_INST_POMDP__7,
                GAME_OF_LIFE_INST_POMDP__8,
                GAME_OF_LIFE_INST_POMDP__9,
                GAME_OF_LIFE_INST_POMDP__10
        };
        String[] parameters = buildParameters(GAME_OF_LIFE_POMDP, instances);
        Model model = loadModelMulti(options, parameters);
        for (String instance : instances) {
        	System.out.println(instance);
        	options.set(OptionsRDDL.RDDL_INSTANCE_NAME, instance);
            GraphExplicit result = exploreModelGraph(model);
            System.out.println(result);
        }
    }
    
    @Ignore
    @Test
    // Status: works. Too slow for larger instances.
    public void game_of_life_mdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        String[] instances = new String[]{
                GAME_OF_LIFE_INST_MDP__1,
                GAME_OF_LIFE_INST_MDP__2,
                GAME_OF_LIFE_INST_MDP__3,
                GAME_OF_LIFE_INST_MDP__4,
                GAME_OF_LIFE_INST_MDP__5,
                GAME_OF_LIFE_INST_MDP__6,
                GAME_OF_LIFE_INST_MDP__7,
                GAME_OF_LIFE_INST_MDP__8,
                GAME_OF_LIFE_INST_MDP__9,
                GAME_OF_LIFE_INST_MDP__10
        };
        String[] parameters = buildParameters(GAME_OF_LIFE_MDP, instances);
        Model model = loadModelMulti(options, parameters);
        for (String instance : instances) {
        	System.out.println(instance);
        	options.set(OptionsRDDL.RDDL_INSTANCE_NAME, instance);
            GraphExplicit result = exploreModelGraph(model);
            System.out.println(result);
        }
    }

    @Ignore
    @Test
    // Status: works. Speeds seems kind of OK even for larger instances.
    public void elevators_mdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        String[] instances = new String[]{
        		ELEVATORS_INST_MDP__1,
        		ELEVATORS_INST_MDP__2,
        		ELEVATORS_INST_MDP__3,
        		ELEVATORS_INST_MDP__4,
        		ELEVATORS_INST_MDP__5,
        		ELEVATORS_INST_MDP__6,
        		ELEVATORS_INST_MDP__7,
        		ELEVATORS_INST_MDP__8,
        		ELEVATORS_INST_MDP__9,
        		ELEVATORS_INST_MDP__10
        };
        String[] parameters = buildParameters(ELEVATORS_MDP, instances);
        Model model = loadModelMulti(options, parameters);
        for (String instance : instances) {
        	System.out.println(instance);
        	options.set(OptionsRDDL.RDDL_INSTANCE_NAME, instance);
            GraphExplicit result = exploreModelGraph(model);
            System.out.println(result);
        }
    }

    @Ignore
    @Test
    // Status: seems to work. Also larger instances can be generated.
    // Would be nice if a bit faster.
    public void elevators_pomdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        String[] instances = new String[]{
        		ELEVATORS_INST_POMDP__1,
        		ELEVATORS_INST_POMDP__2,
        		ELEVATORS_INST_POMDP__3,
        		ELEVATORS_INST_POMDP__4,
        		ELEVATORS_INST_POMDP__5,
        		ELEVATORS_INST_POMDP__6,
        		ELEVATORS_INST_POMDP__7,
        		ELEVATORS_INST_POMDP__8,
        		ELEVATORS_INST_POMDP__9,
        		ELEVATORS_INST_POMDP__10
        };
        String[] parameters = buildParameters(ELEVATORS_POMDP, instances);
        Model model = loadModelMulti(options, parameters);
        for (String instance : instances) {
        	System.out.println(instance);
        	options.set(OptionsRDDL.RDDL_INSTANCE_NAME, instance);
            GraphExplicit result = exploreModelGraph(model);
            System.out.println(result);
        }
    }

    @Ignore
    @Test
    // Status: works. Fast enough.
    public void dbn_po_nsTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        GraphExplicit result = exploreModelGraph(options, DBN_PO_NS);
        System.out.println(result);
    }

    @Ignore
    @Test
    // Status: works. Too slow for larger instances.
    public void crossing_traffic_mdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        String[] instances = new String[]{
        		CROSSING_TRAFFIC_INST_MDP__1,
        		CROSSING_TRAFFIC_INST_MDP__2,
        		CROSSING_TRAFFIC_INST_MDP__3,
        		CROSSING_TRAFFIC_INST_MDP__4,
        		CROSSING_TRAFFIC_INST_MDP__5,
        		CROSSING_TRAFFIC_INST_MDP__6,
        		CROSSING_TRAFFIC_INST_MDP__7,
        		CROSSING_TRAFFIC_INST_MDP__8,
        		CROSSING_TRAFFIC_INST_MDP__9,
        		CROSSING_TRAFFIC_INST_MDP__10
        };
        String[] parameters = buildParameters(CROSSING_TRAFFIC_MDP, instances);
        Model model = loadModelMulti(options, parameters);
        for (String instance : instances) {
        	System.out.println(instance);
        	options.set(OptionsRDDL.RDDL_INSTANCE_NAME, instance);
            GraphExplicit result = exploreModelGraph(model);
            System.out.println(result);
        }
    }

    @Ignore
    @Test
    // Status: works. too slow for larger instances.
    public void crossing_traffic_pomdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        String[] instances = new String[]{
        		CROSSING_TRAFFIC_INST_POMDP__1,
        		CROSSING_TRAFFIC_INST_POMDP__2,
        		CROSSING_TRAFFIC_INST_POMDP__3,
        		CROSSING_TRAFFIC_INST_POMDP__4,
        		CROSSING_TRAFFIC_INST_POMDP__5,
        		CROSSING_TRAFFIC_INST_POMDP__6,
        		CROSSING_TRAFFIC_INST_POMDP__7,
        		CROSSING_TRAFFIC_INST_POMDP__8,
        		CROSSING_TRAFFIC_INST_POMDP__9,
        		CROSSING_TRAFFIC_INST_POMDP__10
        };
        String[] parameters = buildParameters(CROSSING_TRAFFIC_POMDP, instances);
        Model model = loadModelMulti(options, parameters);
        for (String instance : instances) {
        	System.out.println(instance);
        	options.set(OptionsRDDL.RDDL_INSTANCE_NAME, instance);
            GraphExplicit result = exploreModelGraph(model);
            System.out.println(result);
        }
    }

    @Ignore
    @Test
    // Status: seems to work now, but too slow.
    public void logisticsTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        GraphExplicit result = exploreModelGraph(options, LOGISTICS);
        System.out.println(result);
    }

    @Ignore
    @Test
    // Status: works. Speed seems kind of OK even for larger instances.
    public void navigation_mdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        String[] instances = new String[]{
        		NAVIGATION_INST_MDP__1,
        		NAVIGATION_INST_MDP__2,
        		NAVIGATION_INST_MDP__3,
        		NAVIGATION_INST_MDP__4,
        		NAVIGATION_INST_MDP__5,
        		NAVIGATION_INST_MDP__6,
        		NAVIGATION_INST_MDP__7,
        		NAVIGATION_INST_MDP__8,
        		NAVIGATION_INST_MDP__9,
        		NAVIGATION_INST_MDP__10
        };
        String[] parameters = buildParameters(NAVIGATION_MDP, instances);
        Model model = loadModelMulti(options, parameters);
        for (String instance : instances) {
        	System.out.println(instance);
        	options.set(OptionsRDDL.RDDL_INSTANCE_NAME, instance);
            GraphExplicit result = exploreModelGraph(model);
            System.out.println(result);
        }
    }

    @Ignore
    @Test
    // Status: works. Speed too slow for the later instances.
    public void navigation_pomdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        String[] instances = new String[]{
        		NAVIGATION_INST_POMDP__1,
        		NAVIGATION_INST_POMDP__2,
        		NAVIGATION_INST_POMDP__3,
        		NAVIGATION_INST_POMDP__4,
        		NAVIGATION_INST_POMDP__5,
        		NAVIGATION_INST_POMDP__6,
        		NAVIGATION_INST_POMDP__7,
        		NAVIGATION_INST_POMDP__8,
        		NAVIGATION_INST_POMDP__9,
        		NAVIGATION_INST_POMDP__10
        };
        String[] parameters = buildParameters(NAVIGATION_POMDP, instances);
        Model model = loadModelMulti(options, parameters);
        for (String instance : instances) {
        	System.out.println(instance);
        	options.set(OptionsRDDL.RDDL_INSTANCE_NAME, instance);
            GraphExplicit result = exploreModelGraph(model);
            System.out.println(result);
        }
    }

    @Ignore
    @Test
    // Status: ignore! This case study is incorrect for a number of reasons e.g.
    // - in "if(exists_{?a : person} ...) then nextXPos(?a) ..." scoping of ?a is incorrect
    // - in "EXCHANGER(person) : { non-fluent, int, default = false };" typing is incorrect (as far as I understood, even in the relaxed typing system of RDDL)
    public void packageTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        GraphExplicit result = exploreModelGraph(options, PACKAGE);
        System.out.println(result);
    }

    @Test
    // Status: works in principle, but too slow and uses too much memory.
    // TODO check constraints stuff and whether state space is generated correctly
    public void pizzaTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        GraphExplicit result = exploreModelGraph(options, PIZZA);
        System.out.println(result);
    }

    @Ignore
    @Test
    // Status: seems to work. However, too slow.
    public void recon_mdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        String[] instances = new String[]{
        		RECON_INST_MDP__1,
        		RECON_INST_MDP__2,
        		RECON_INST_MDP__3,
        		RECON_INST_MDP__4,
        		RECON_INST_MDP__5,
        		RECON_INST_MDP__6,
        		RECON_INST_MDP__7,
        		RECON_INST_MDP__8,
        		RECON_INST_MDP__9,
        		RECON_INST_MDP__10
        };
        String[] parameters = buildParameters(RECON_MDP, instances);
        Model model = loadModelMulti(options, parameters);
        for (String instance : instances) {
        	System.out.println(instance);
        	options.set(OptionsRDDL.RDDL_INSTANCE_NAME, instance);
            GraphExplicit result = exploreModelGraph(model);
            System.out.println(result);
        }
    }

    @Ignore
    @Test
    // Status: seems to work. However, too slow.
    public void recon_pomdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        String[] instances = new String[]{
        		RECON_INST_POMDP__1,
        		RECON_INST_POMDP__2,
        		RECON_INST_POMDP__3,
        		RECON_INST_POMDP__4,
        		RECON_INST_POMDP__5,
        		RECON_INST_POMDP__6,
        		RECON_INST_POMDP__7,
        		RECON_INST_POMDP__8,
        		RECON_INST_POMDP__9,
        		RECON_INST_POMDP__10
        };
        String[] parameters = buildParameters(RECON_POMDP, instances);
        Model model = loadModelMulti(options, parameters);
        for (String instance : instances) {
        	System.out.println(instance);
        	options.set(OptionsRDDL.RDDL_INSTANCE_NAME, instance);
            GraphExplicit result = exploreModelGraph(model);
            System.out.println(result);
        }
    }

    @Ignore
    @Test
    // Status: works, fast enough
    public void sidewalkTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        GraphExplicit result = exploreModelGraph(options, SIDEWALK);
        System.out.println(result);
    }

    @Ignore
    @Test
    // Status: Seems to work now. Too slow.
    public void simple_mars_roverTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        GraphExplicit result = exploreModelGraph(options, SIMPLE_MARS_ROVER);
        System.out.println(result);
    }

    @Ignore
    @Test
    // Status: works. Too slow for larger instances.
    public void skill_teaching_mdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        String[] instances = new String[]{
        		SKILL_TEACHING_INST_MDP__1,
        		SKILL_TEACHING_INST_MDP__2,
        		SKILL_TEACHING_INST_MDP__3,
        		SKILL_TEACHING_INST_MDP__4,
        		SKILL_TEACHING_INST_MDP__5,
        		SKILL_TEACHING_INST_MDP__6,
        		SKILL_TEACHING_INST_MDP__7,
        		SKILL_TEACHING_INST_MDP__8,
        		SKILL_TEACHING_INST_MDP__9,
        		SKILL_TEACHING_INST_MDP__10
        };
        String[] parameters = buildParameters(SKILL_TEACHING_MDP, instances);
        Model model = loadModelMulti(options, parameters);
        for (String instance : instances) {
        	System.out.println(instance);
        	options.set(OptionsRDDL.RDDL_INSTANCE_NAME, instance);
            GraphExplicit result = exploreModelGraph(model);
            System.out.println(result);
        }
    }

    @Ignore
    @Test
    // Status: works. Too slow for larger instances.
    public void skill_teaching_pomdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        String[] instances = new String[]{
        		SKILL_TEACHING_INST_POMDP__1,
        		SKILL_TEACHING_INST_POMDP__2,
        		SKILL_TEACHING_INST_POMDP__3,
        		SKILL_TEACHING_INST_POMDP__4,
        		SKILL_TEACHING_INST_POMDP__5,
        		SKILL_TEACHING_INST_POMDP__6,
        		SKILL_TEACHING_INST_POMDP__7,
        		SKILL_TEACHING_INST_POMDP__8,
        		SKILL_TEACHING_INST_POMDP__9,
        		SKILL_TEACHING_INST_POMDP__10
        };
        String[] parameters = buildParameters(SKILL_TEACHING_POMDP, instances);
        Model model = loadModelMulti(options, parameters);
        for (String instance : instances) {
        	System.out.println(instance);
        	options.set(OptionsRDDL.RDDL_INSTANCE_NAME, instance);
            GraphExplicit result = exploreModelGraph(model);
            System.out.println(result);
        }
    }

    @Ignore
    @Test
    // Status: works, fast enough
    public void sysadmin_complexTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        GraphExplicit result = exploreModelGraph(options, SYSADMIN_COMPLEX);
        System.out.println(result);
    }

    @Ignore
    @Test
    // Status: works. Too slow for larger instances.
    public void sysadmin_mdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        String[] instances = new String[]{
        		SYSADMIN_INST_MDP__1,
        		SYSADMIN_INST_MDP__2,
        		SYSADMIN_INST_MDP__3,
        		SYSADMIN_INST_MDP__4,
        		SYSADMIN_INST_MDP__5,
        		SYSADMIN_INST_MDP__6,
        		SYSADMIN_INST_MDP__7,
        		SYSADMIN_INST_MDP__8,
        		SYSADMIN_INST_MDP__9,
        		SYSADMIN_INST_MDP__10
        };
        String[] parameters = buildParameters(SYSADMIN_MDP, instances);
        Model model = loadModelMulti(options, parameters);
        for (String instance : instances) {
        	System.out.println(instance);
        	options.set(OptionsRDDL.RDDL_INSTANCE_NAME, instance);
            GraphExplicit result = exploreModelGraph(model);
            System.out.println(result);
        }
    }

    @Ignore
    @Test
    // Status: works. Larger instances too slow.
    public void sysadmin_pomdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        String[] instances = new String[]{
        		SYSADMIN_INST_POMDP__1,
        		SYSADMIN_INST_POMDP__2,
        		SYSADMIN_INST_POMDP__3,
        		SYSADMIN_INST_POMDP__4,
        		SYSADMIN_INST_POMDP__5,
        		SYSADMIN_INST_POMDP__6,
        		SYSADMIN_INST_POMDP__7,
        		SYSADMIN_INST_POMDP__8,
        		SYSADMIN_INST_POMDP__9,
        		SYSADMIN_INST_POMDP__10
        };
        String[] parameters = buildParameters(SYSADMIN_POMDP, instances);
        Model model = loadModelMulti(options, parameters);
        for (String instance : instances) {
        	System.out.println(instance);
        	options.set(OptionsRDDL.RDDL_INSTANCE_NAME, instance);
            GraphExplicit result = exploreModelGraph(model);
            System.out.println(result);
        }
    }

    @Ignore
    @Test
    // Status: fails
    // TODO support non-object parameters
    public void traffic_binary_ctm_multivalueTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        GraphExplicit result = exploreModelGraph(options, TRAFFIC_BINARY_CTM_MULTIVALUE);
        System.out.println(result);
    }

    @Ignore
    @Test
    // Status: seems to work. But takes way too long.
    public void traffic_mdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        String[] instances = new String[]{
        		TRAFFIC_INST_MDP__1,
        		TRAFFIC_INST_MDP__2,
        		TRAFFIC_INST_MDP__3,
        		TRAFFIC_INST_MDP__4,
        		TRAFFIC_INST_MDP__5,
        		TRAFFIC_INST_MDP__6,
        		TRAFFIC_INST_MDP__7,
        		TRAFFIC_INST_MDP__8,
        		TRAFFIC_INST_MDP__9,
        		TRAFFIC_INST_MDP__10
        };
        String[] parameters = buildParameters(TRAFFIC_MDP, instances);
        Model model = loadModelMulti(options, parameters);
        for (String instance : instances) {
        	System.out.println(instance);
        	options.set(OptionsRDDL.RDDL_INSTANCE_NAME, instance);
            GraphExplicit result = exploreModelGraph(model);
            System.out.println(result);
        }
    }

	@Ignore
    @Test
    // Status: seems to work, but way too slow.
    public void traffic_pomdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        String[] instances = new String[]{
        		TRAFFIC_INST_POMDP__1,
        		TRAFFIC_INST_POMDP__2,
        		TRAFFIC_INST_POMDP__3,
        		TRAFFIC_INST_POMDP__4,
        		TRAFFIC_INST_POMDP__5,
        		TRAFFIC_INST_POMDP__6,
        		TRAFFIC_INST_POMDP__7,
        		TRAFFIC_INST_POMDP__8,
        		TRAFFIC_INST_POMDP__9,
        		TRAFFIC_INST_POMDP__10
        };
        String[] parameters = buildParameters(TRAFFIC_POMDP, instances);
        Model model = loadModelMulti(options, parameters);
        for (String instance : instances) {
        	System.out.println(instance);
        	options.set(OptionsRDDL.RDDL_INSTANCE_NAME, instance);
            GraphExplicit result = exploreModelGraph(model);
            System.out.println(result);
        }
    }

    @Test
    @Ignore
    // Status: not supported at the moment, has continuous state space
    public void workforceTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Map<String,RDDLIntRange> ranges = options.get(OptionsRDDL.RDDL_INT_RANGE);
        ranges.put("train", new RDDLIntRange(0,15));
        ranges.put("month", new RDDLIntRange(0,15));
        ranges.put("training", new RDDLIntRange(0,15));
        ranges.put("labor", new RDDLIntRange(0,15));
        ranges.put("ATTRITIONAMOUNT", new RDDLIntRange(0,15));
        ranges.put("SEASONFREQ", new RDDLIntRange(0,15));
        GraphExplicit result = exploreModelGraph(options, WORKFORCE);
        System.out.println(result);
    }
    
    private String[] buildParameters(String modelName, String[] instances) {
        String[] parameters = new String[instances.length + 1];
        parameters[0] = modelName;
        System.arraycopy(instances, 0, parameters, 1, instances.length);        
        for (int instNr = 0; instNr < instances.length; instNr++) {
        	String[] split = instances[instNr].split("/");
        	instances[instNr] = split[split.length - 1];
        	split = instances[instNr].split("\\.");
        	instances[instNr] = split[0];
        }
        return parameters;
	}
}
