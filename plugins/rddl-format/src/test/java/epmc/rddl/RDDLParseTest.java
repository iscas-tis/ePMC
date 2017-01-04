package epmc.rddl;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.error.EPMCException;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.Model;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.rddl.model.ModelRDDL;

import static epmc.modelchecker.TestHelper.*;
import static epmc.rddl.ModelNames.*;
import static epmc.rddl.RDDLTestHelper.prepareRDDLOptions;

public final class RDDLParseTest {
    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void dbn_propTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Model model = loadModel(options, DBN_PROP);
        System.out.println(model);
        Model reparsed = loadModelFromString(options, model.toString());        
        System.out.println(reparsed);
    }
    
    @Test
    public void dbn_types_interm_poTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Model model = loadModel(options, DBN_TYPES_INTERM_PO);
        System.out.println(model);
        Model reparsed = loadModelFromString(options, model.toString());
        System.out.println(reparsed);
    }
    
    @Test
    public void game_of_life_determTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Model model = loadModelMulti(options, GAME_OF_LIFE_DETERM);
        System.out.println(model);
        Model reparsed = loadModelFromString(options, model.toString());
        System.out.println(reparsed);
    }
    
    @Test
    public void game_of_life_stochTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Model model = loadModelMulti(options, GAME_OF_LIFE_STOCH);
        System.out.println(model);
        Model reparsed = loadModelFromString(options, model.toString());
        System.out.println(reparsed);
    }

    @Test
    public void game_of_life_pomdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Model model = loadModelMulti(options, GAME_OF_LIFE_POMDP,
                GAME_OF_LIFE_INST_POMDP__1,
                GAME_OF_LIFE_INST_POMDP__2,
                GAME_OF_LIFE_INST_POMDP__3,
                GAME_OF_LIFE_INST_POMDP__4,
                GAME_OF_LIFE_INST_POMDP__5,
                GAME_OF_LIFE_INST_POMDP__6,
                GAME_OF_LIFE_INST_POMDP__7,
                GAME_OF_LIFE_INST_POMDP__8,
                GAME_OF_LIFE_INST_POMDP__9,
                GAME_OF_LIFE_INST_POMDP__10);
        System.out.println(model);
        Model reparsed = loadModelFromString(options, model.toString());
        System.out.println(reparsed);
    }

    @Test
    public void game_of_life_mdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Model model = loadModelMulti(options, GAME_OF_LIFE_MDP,
                GAME_OF_LIFE_INST_MDP__1,
                GAME_OF_LIFE_INST_MDP__2,
                GAME_OF_LIFE_INST_MDP__3,
                GAME_OF_LIFE_INST_MDP__4,
                GAME_OF_LIFE_INST_MDP__5,
                GAME_OF_LIFE_INST_MDP__6,
                GAME_OF_LIFE_INST_MDP__7,
                GAME_OF_LIFE_INST_MDP__8,
                GAME_OF_LIFE_INST_MDP__9,
                GAME_OF_LIFE_INST_MDP__10);
        System.out.println(model);
        Model reparsed = loadModelFromString(options, model.toString());
        System.out.println(reparsed);
    }

    @Test
    public void elevators_mdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Model model = loadModelMulti(options, ELEVATORS_MDP,
                ELEVATORS_INST_MDP__1,
                ELEVATORS_INST_MDP__2,
                ELEVATORS_INST_MDP__3,
                ELEVATORS_INST_MDP__4,
                ELEVATORS_INST_MDP__5,
                ELEVATORS_INST_MDP__6,
                ELEVATORS_INST_MDP__7,
                ELEVATORS_INST_MDP__8,
                ELEVATORS_INST_MDP__9,
                ELEVATORS_INST_MDP__10);
        System.out.println(model);
        Model reparsed = loadModelFromString(options, model.toString());
        System.out.println(reparsed);
    }

    @Test
    public void elevators_pomdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Model model = loadModelMulti(options, ELEVATORS_POMDP,
                ELEVATORS_INST_POMDP__1,
                ELEVATORS_INST_POMDP__2,
                ELEVATORS_INST_POMDP__3,
                ELEVATORS_INST_POMDP__4,
                ELEVATORS_INST_POMDP__5,
                ELEVATORS_INST_POMDP__6,
                ELEVATORS_INST_POMDP__7,
                ELEVATORS_INST_POMDP__8,
                ELEVATORS_INST_POMDP__9,
                ELEVATORS_INST_POMDP__10);
        System.out.println(model);
        Model reparsed = loadModelFromString(options, model.toString());
        System.out.println(reparsed);
    }

    @Test
    public void dbn_po_nsTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Model model = loadModel(options, DBN_PO_NS);
        System.out.println(model);
        Model reparsed = loadModelFromString(options, model.toString());
        System.out.println(reparsed);
    }

    @Test
    public void crossing_traffic_mdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Model model = loadModelMulti(options, CROSSING_TRAFFIC_MDP,
                CROSSING_TRAFFIC_INST_MDP__1,
                CROSSING_TRAFFIC_INST_MDP__2,
                CROSSING_TRAFFIC_INST_MDP__3,
                CROSSING_TRAFFIC_INST_MDP__4,
                CROSSING_TRAFFIC_INST_MDP__5,
                CROSSING_TRAFFIC_INST_MDP__6,
                CROSSING_TRAFFIC_INST_MDP__7,
                CROSSING_TRAFFIC_INST_MDP__8,
                CROSSING_TRAFFIC_INST_MDP__9,
                CROSSING_TRAFFIC_INST_MDP__10);
        System.out.println(model);
        Model reparsed = loadModelFromString(options, model.toString());
        System.out.println(reparsed);
    }

    @Test
    public void crossing_traffic_pomdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Model model = loadModelMulti(options, CROSSING_TRAFFIC_POMDP,
                CROSSING_TRAFFIC_INST_POMDP__1,
                CROSSING_TRAFFIC_INST_POMDP__2,
                CROSSING_TRAFFIC_INST_POMDP__3,
                CROSSING_TRAFFIC_INST_POMDP__4,
                CROSSING_TRAFFIC_INST_POMDP__5,
                CROSSING_TRAFFIC_INST_POMDP__6,
                CROSSING_TRAFFIC_INST_POMDP__7,
                CROSSING_TRAFFIC_INST_POMDP__8,
                CROSSING_TRAFFIC_INST_POMDP__9,
                CROSSING_TRAFFIC_INST_POMDP__10);
        System.out.println(model);
        Model reparsed = loadModelFromString(options, model.toString());
        System.out.println(reparsed);
    }

    @Test
    public void logisticsTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Model model = loadModel(options, LOGISTICS);
        System.out.println(model);
        Model reparsed = loadModelFromString(options, model.toString());
        System.out.println(reparsed);
    }

    @Test
    public void navigation_mdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Model model = loadModelMulti(options, NAVIGATION_MDP,
                NAVIGATION_INST_MDP__1,
                NAVIGATION_INST_MDP__2,
                NAVIGATION_INST_MDP__3,
                NAVIGATION_INST_MDP__4,
                NAVIGATION_INST_MDP__5,
                NAVIGATION_INST_MDP__6,
                NAVIGATION_INST_MDP__7,
                NAVIGATION_INST_MDP__8,
                NAVIGATION_INST_MDP__9,
                NAVIGATION_INST_MDP__10);
        System.out.println(model);
        Model reparsed = loadModelFromString(options, model.toString());
        System.out.println(reparsed);
    }

    @Test
    public void navigation_pomdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Model model = loadModelMulti(options, NAVIGATION_POMDP,
                NAVIGATION_INST_POMDP__1,
                NAVIGATION_INST_POMDP__2,
                NAVIGATION_INST_POMDP__3,
                NAVIGATION_INST_POMDP__4,
                NAVIGATION_INST_POMDP__5,
                NAVIGATION_INST_POMDP__6,
                NAVIGATION_INST_POMDP__7,
                NAVIGATION_INST_POMDP__8,
                NAVIGATION_INST_POMDP__9,
                NAVIGATION_INST_POMDP__10);
        System.out.println(model);
        Model reparsed = loadModelFromString(options, model.toString());
        System.out.println(reparsed);
    }

    @Test
    public void packageTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Model model = loadModel(options, PACKAGE);
        System.out.println(model);
        Model reparsed = loadModelFromString(options, model.toString());
        System.out.println(reparsed);
    }

    @Test
    public void pizzaTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Model model = loadModel(options, PIZZA);
        System.out.println(model);
        Model reparsed = loadModelFromString(options, model.toString());
        System.out.println(reparsed);
    }

    @Test
    public void recon_mdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Model model = loadModelMulti(options, RECON_MDP,
                RECON_INST_MDP__1,
                RECON_INST_MDP__2,
                RECON_INST_MDP__3,
                RECON_INST_MDP__4,
                RECON_INST_MDP__5,
                RECON_INST_MDP__6,
                RECON_INST_MDP__7,
                RECON_INST_MDP__8,
                RECON_INST_MDP__9,
                RECON_INST_MDP__10);
        System.out.println(model);
        Model reparsed = loadModelFromString(options, model.toString());
        System.out.println(reparsed);
    }

    @Test
    public void recon_pomdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Model model = loadModelMulti(options, RECON_POMDP,
                RECON_INST_POMDP__1,
                RECON_INST_POMDP__2,
                RECON_INST_POMDP__3,
                RECON_INST_POMDP__4,
                RECON_INST_POMDP__5,
                RECON_INST_POMDP__6,
                RECON_INST_POMDP__7,
                RECON_INST_POMDP__8,
                RECON_INST_POMDP__9,
                RECON_INST_POMDP__10);
        System.out.println(model);
        Model reparsed = loadModelFromString(options, model.toString());
        System.out.println(reparsed);
    }

    @Test
    public void sidewalkTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Model model = loadModel(options, SIDEWALK);
        System.out.println(model);
        Model reparsed = loadModelFromString(options, model.toString());
        System.out.println(reparsed);
    }

    @Test
    public void simple_mars_roverTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Model model = loadModel(options, SIMPLE_MARS_ROVER);
        System.out.println(model);
        Model reparsed = loadModelFromString(options, model.toString());
        System.out.println(reparsed);
    }
    
    @Test
    public void skill_teaching_mdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Model model = loadModelMulti(options, SKILL_TEACHING_MDP,
                SKILL_TEACHING_INST_MDP__1,
                SKILL_TEACHING_INST_MDP__2,
                SKILL_TEACHING_INST_MDP__3,
                SKILL_TEACHING_INST_MDP__4,
                SKILL_TEACHING_INST_MDP__5,
                SKILL_TEACHING_INST_MDP__6,
                SKILL_TEACHING_INST_MDP__7,
                SKILL_TEACHING_INST_MDP__8,
                SKILL_TEACHING_INST_MDP__9,
                SKILL_TEACHING_INST_MDP__10);
        System.out.println(model);
        Model reparsed = loadModelFromString(options, model.toString());
        System.out.println(reparsed);
    }

    @Test
    public void skill_teaching_pomdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Model model = loadModelMulti(options, SKILL_TEACHING_POMDP,
                SKILL_TEACHING_INST_POMDP__1,
                SKILL_TEACHING_INST_POMDP__2,
                SKILL_TEACHING_INST_POMDP__3,
                SKILL_TEACHING_INST_POMDP__4,
                SKILL_TEACHING_INST_POMDP__5,
                SKILL_TEACHING_INST_POMDP__6,
                SKILL_TEACHING_INST_POMDP__7,
                SKILL_TEACHING_INST_POMDP__8,
                SKILL_TEACHING_INST_POMDP__9,
                SKILL_TEACHING_INST_POMDP__10);
        System.out.println(model);
        Model reparsed = loadModelFromString(options, model.toString());
        System.out.println(reparsed);
    }

    @Test
    public void sysadmin_complexTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Model model = loadModel(options, SYSADMIN_COMPLEX);
        System.out.println(model);
        Model reparsed = loadModelFromString(options, model.toString());
        System.out.println(reparsed);
    }

    @Test
    public void sysadmin_mdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Model model = loadModelMulti(options, SYSADMIN_MDP,
                SYSADMIN_INST_MDP__1,
                SYSADMIN_INST_MDP__2,
                SYSADMIN_INST_MDP__3,
                SYSADMIN_INST_MDP__4,
                SYSADMIN_INST_MDP__5,
                SYSADMIN_INST_MDP__6,
                SYSADMIN_INST_MDP__7,
                SYSADMIN_INST_MDP__8,
                SYSADMIN_INST_MDP__9,
                SYSADMIN_INST_MDP__10);
        System.out.println(model);
        Model reparsed = loadModelFromString(options, model.toString());
        System.out.println(reparsed);
    }

    @Test
    public void sysadmin_pomdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Model model = loadModelMulti(options, SYSADMIN_POMDP,
                SYSADMIN_INST_POMDP__1,
                SYSADMIN_INST_POMDP__2,
                SYSADMIN_INST_POMDP__3,
                SYSADMIN_INST_POMDP__4,
                SYSADMIN_INST_POMDP__5,
                SYSADMIN_INST_POMDP__6,
                SYSADMIN_INST_POMDP__7,
                SYSADMIN_INST_POMDP__8,
                SYSADMIN_INST_POMDP__9,
                SYSADMIN_INST_POMDP__10);
        System.out.println(model);
        Model reparsed = loadModelFromString(options, model.toString());
        System.out.println(reparsed);
    }

    @Test
    public void traffic_binary_ctm_multivalueTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Model model = loadModel(options, TRAFFIC_BINARY_CTM_MULTIVALUE);
        System.out.println(model);
        Model reparsed = loadModelFromString(options, model.toString());
        System.out.println(reparsed);
    }

    @Test
    public void traffic_mdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Model model = loadModelMulti(options, TRAFFIC_MDP,
                TRAFFIC_INST_MDP__1,
                TRAFFIC_INST_MDP__2,
                TRAFFIC_INST_MDP__3,
                TRAFFIC_INST_MDP__4,
                TRAFFIC_INST_MDP__5,
                TRAFFIC_INST_MDP__6,
                TRAFFIC_INST_MDP__7,
                TRAFFIC_INST_MDP__8,
                TRAFFIC_INST_MDP__9,
                TRAFFIC_INST_MDP__10);
        System.out.println(model);
        Model reparsed = loadModelFromString(options, model.toString());
        System.out.println(reparsed);
    }

    @Test
    public void traffic_pomdpTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Model model = loadModelMulti(options, TRAFFIC_POMDP,
                TRAFFIC_INST_POMDP__1,
                TRAFFIC_INST_POMDP__2,
                TRAFFIC_INST_POMDP__3,
                TRAFFIC_INST_POMDP__4,
                TRAFFIC_INST_POMDP__5,
                TRAFFIC_INST_POMDP__6,
                TRAFFIC_INST_POMDP__7,
                TRAFFIC_INST_POMDP__8,
                TRAFFIC_INST_POMDP__9,
                TRAFFIC_INST_POMDP__10);
        System.out.println(model);
        Model reparsed = loadModelFromString(options, model.toString());
        System.out.println(reparsed);
    }

    @Test
    public void workforceTest() throws EPMCException {
        Options options = prepareRDDLOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Model model = loadModel(options, WORKFORCE);
        System.out.println(model);
        Model reparsed = loadModelFromString(options, model.toString());
        System.out.println(reparsed);
    }

}
