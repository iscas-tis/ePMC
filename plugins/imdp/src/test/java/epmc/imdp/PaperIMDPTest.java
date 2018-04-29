package epmc.imdp;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionTypeInteger;
import epmc.expression.standard.RewardSpecificationImpl;
import epmc.graph.CommonProperties;
import epmc.graph.SchedulerPrinter;
import epmc.imdp.model.ModelIMDP;
import epmc.imdp.model.PropertyIMDP;
import epmc.imdp.options.OptionsIMDP;
import epmc.imdp.robot.ModelIMDPRobot;
import epmc.imdp.robot.OptionsRobot;
import epmc.modelchecker.ExploreStatistics;
import epmc.modelchecker.Model;
import epmc.modelchecker.ModelCheckerResults;
import epmc.modelchecker.RawProperty;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.prism.model.convert.OptionsPRISMConverter;
import epmc.util.Util;
import epmc.value.Value;
import epmc.value.ValueReal;

import static epmc.graph.TestHelperGraph.*;
import static epmc.imdp.ModelNames.*;
import static epmc.modelchecker.TestHelper.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PaperIMDPTest {
    /**
     * Set up the tests.
     */
    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void robotExploreTest() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDPRobot.IDENTIFIER);
        Model model = loadModel(options, ROBOT);
        ExploreStatistics statistics = exploreModel(model);
        System.out.println(statistics);
        System.out.println(exploreModelGraph(model));
    }

    @Test
    public void robotRewardMCTest() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDPRobot.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-10");
        options.set(OptionsIMDP.IMDP_INTERVAL_PLAYER, IntervalPlayer.ANTAGONISTIC);
        Model model = loadModel(options, ROBOT);
    //      Value result = TestHelper.computeResult(model, "multi(R{1}max=?[C])");
        Value result = TestHelper.computeResult(model, "R{1}max=?[C]");
  //      Value result = TestHelper.computeResult(model, String.format("multi(R{1}max=?[C], R{2}<=%d [ C ])", 30));
        System.out.println(result);
    }

    /**
     * Plot Pareto curve used in to-be-submitted QEST 2017 paper.
     * 
     */
    @Test
    public void runningExampleQEST2017Test() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDPRobot.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-10");
        options.set(OptionsIMDP.IMDP_INTERVAL_PLAYER, IntervalPlayer.ANTAGONISTIC);
        Model model = loadModel(options, RUNNING_EXAMPLE_QEST_2017);
        //  Value result = TestHelper.computeResult(model, "multi(R{1}max=?[C])");
        //    	  Value result = TestHelper.computeResult(model, "R{1}max=?[C]");
        Value result = TestHelper.computeResult(model, "multi(R{1}max=? [C], R{2}>=0.335 [ C ])");
        //        Value result = TestHelper.computeResult(model, "multi(R{2}max=? [ C ], R{1}>=1 [C])");
        System.out.println(result);
    }

    @Test
    public void runningExampleQEST2017GraphTest() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDPRobot.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-10");
        options.set(OptionsIMDP.IMDP_INTERVAL_PLAYER, IntervalPlayer.ANTAGONISTIC);
        Model model = loadModel(options, RUNNING_EXAMPLE_QEST_2017);

        List<Value> results = new ArrayList<>();
        for (int i = 0; i <= 100; i++) {
            model = loadModel(options, RUNNING_EXAMPLE_QEST_2017);
            Value result = TestHelper.computeResult(model, String.format("multi(R{1}max=? [C], R{2}>=%f [ C ])", 0.3 + i*0.001));
            results.add(result);
        }

        for (int i = 0; i <= 100; i++) {
            System.out.println(0.3 + i*0.001 + " " + results.get(i));
        }
    }

    @Test
    public void tourGuideGraphTest() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDP.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-10");
        options.set(OptionsIMDP.IMDP_INTERVAL_PLAYER, IntervalPlayer.ANTAGONISTIC);
        Map<String,Object> constants = new HashMap<>();
        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        UtilTestIMDP.showGraph(options, TOUR_GUIDE, "multi(R{\"obstacle\"}min=?[C], R{\"steps\"}<=%f [ C ])",
                19, 40, 50);
    }

    @Test
    public void tourGuideConfiguration1GraphTest() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDP.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-10");
        options.set(OptionsIMDP.IMDP_INTERVAL_PLAYER, IntervalPlayer.ANTAGONISTIC);
        Map<String,Object> constants = new HashMap<>();
        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        UtilTestIMDP.showGraph(options, TOUR_GUIDE_CONFIGURATION_1, "multi(R{\"obstacle\"}min=?[C], R{\"steps\"}<=%f [ C ])",
                31, 40, 20);
    }

    @Test
    public void tourGuideConfiguration2GraphTest() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDP.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-10");
        options.set(OptionsIMDP.IMDP_INTERVAL_PLAYER, IntervalPlayer.ANTAGONISTIC);
        Map<String,Object> constants = new HashMap<>();
        constants.put("size", "14");
        options.set(OptionsModelChecker.CONST, constants);
        //        UtilTestIMDP.showGraph(options, TOUR_GUIDE_CONFIGURATION_2, "multi(R{\"obstacle\"}min=?[C], R{\"steps\"}<=%f [ C ])",
        //        		31, 70, 200);
        UtilTestIMDP.exportGraph("tour_guide_configuration_2", options, TOUR_GUIDE_CONFIGURATION_2, "multi(R{\"obstacle\"}min=?[C], R{\"steps\"}<=%f [ C ])",
                31, 70, 500);
    }

    @Test
    public void tourGuideConfiguration2SchedulerTest() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDP.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-10");
        options.set(OptionsIMDP.IMDP_INTERVAL_PLAYER, IntervalPlayer.ANTAGONISTIC);
        options.set(OptionsModelChecker.COMPUTE_SCHEDULER, true);
        Map<String,Object> constants = new HashMap<>();
        constants.put("size", "14");
        options.set(OptionsModelChecker.CONST, constants);
        Map<String,Class<? extends SchedulerPrinter>> schedulerPrinters = options.get(OptionsModelChecker.SCHEDULER_PRINTER_CLASS); 
        schedulerPrinters.put(SchedulerPrinterSimpleTourGuide.IDENTIFIER, SchedulerPrinterSimpleTourGuide.class);
        schedulerPrinters.put(SchedulerPrinterInitialRandomisedTourGuide.IDENTIFIER, SchedulerPrinterInitialRandomisedTourGuide.class);
        options.set(OptionsModelChecker.SCHEDULER_PRINTER_CLASS, schedulerPrinters);
        Model model = loadModel(options, TOUR_GUIDE_CONFIGURATION_2);
        ModelCheckerResults results = TestHelper.computeResults(model, String.format("multi(R{\"obstacle\"}min=?[C], R{\"steps\"}<=%f [ C ])", 76.8658133));
        //        ModelCheckerResults results = TestHelper.computeResults(model, String.format("multi(R{\"steps\"}min=? [ C ], R{\"obstacle\"}<=%f[C])", 200.0));
        RawProperty property = results.getProperties().iterator().next();
        Util.printScheduler(System.out, results.getLowLevel(property), results.getScheduler(property));
        System.out.println(results.getResult(property));
    }

    @Test
    public void tourGuideExploreTest() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        options.set(OptionsModelChecker.PROPERTY_INPUT_TYPE, PropertyIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDP.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-10");
        options.set(OptionsIMDP.IMDP_INTERVAL_PLAYER, IntervalPlayer.ANTAGONISTIC);
        Map<String,Object> constants = new HashMap<>();
        constants.put("size", "4");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = loadModel(options, TOUR_GUIDE);
        ExploreStatistics statistics = exploreModel(model);
        System.out.println(statistics);
        Set<Object> nodeProps = new HashSet<>();
        nodeProps.add(new RewardSpecificationImpl(new ExpressionLiteral.Builder()
                .setValue("1")
                .setType(ExpressionTypeInteger.TYPE_INTEGER).build()));
        nodeProps.add(new RewardSpecificationImpl(new ExpressionLiteral.Builder()
                .setValue("2")
                .setType(ExpressionTypeInteger.TYPE_INTEGER)
                .build()));
        nodeProps.add(new ExpressionIdentifierStandard.Builder()
                .setName("x").build());
        nodeProps.add(new ExpressionIdentifierStandard.Builder()
                .setName("y").build());
        nodeProps.add(CommonProperties.STATE);
        nodeProps.add(CommonProperties.NODE_EXPLORER);
        Set<Object> edgeProps = new HashSet<>();
        edgeProps.add(new RewardSpecificationImpl(new ExpressionLiteral.Builder()
                .setValue("1")
                .setType(ExpressionTypeInteger.TYPE_INTEGER).build()));
        edgeProps.add(CommonProperties.DECISION);
        Set<Object> graphProps = new HashSet<>();
        System.out.println(exploreModelGraph(model, graphProps, nodeProps, edgeProps));
    }

    @Test
    public void tourGuideMCTest() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        options.set(OptionsModelChecker.PROPERTY_INPUT_TYPE, PropertyIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDP.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-12");
        options.set(OptionsIMDP.IMDP_INTERVAL_PLAYER, IntervalPlayer.ANTAGONISTIC);
        Map<String,Object> constants = new HashMap<>();
        constants.put("size", "8");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = loadModel(options, TOUR_GUIDE);
        Value result = TestHelper.computeResult(model, String.format("multi(R{\"obstacle\"}min=?[C], R{\"steps\"}<=18 [ C ])"));
        //        Value result = TestHelper.computeResult(model, String.format("multi(R{\"steps\"}min=? [ C ])"));
        //      Value result = TestHelper.computeResult(model, String.format("multi(R{\"obstacle\"}min=? [ C ], P>=1[F target])"));
        System.out.println(result);
    }    

    @Test
    public void robotSchedulerTest() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        options.set(OptionsIMDP.IMDP_INTERVAL_PLAYER, IntervalPlayer.ANTAGONISTIC);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDPRobot.IDENTIFIER);
        options.set(OptionsPRISMConverter.PRISM_CONVERTER_REWARD_METHOD, "external");
        //        options.set(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_METHOD, "jacobi");
        Model model = null;
        model = loadModel(options, ROBOT);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-10");
        options.set(OptionsModelChecker.COMPUTE_SCHEDULER, true);
        Map<String,Class<? extends SchedulerPrinter>> schedulerPrinters = options.get(OptionsModelChecker.SCHEDULER_PRINTER_CLASS); 
        schedulerPrinters.put(SchedulerPrinterSimpleRobot.IDENTIFIER, SchedulerPrinterSimpleRobot.class);
        schedulerPrinters.put(SchedulerPrinterInitialRandomisedRobot.IDENTIFIER, SchedulerPrinterInitialRandomisedRobot.class);
        options.set(OptionsModelChecker.SCHEDULER_PRINTER_CLASS, schedulerPrinters);
        //  	  Value result = TestHelper.computeResult(model, "multi(R{1}max=?[C])");
        //    	  Value result = TestHelper.computeResult(model, "R{1}max=?[C]");
        //  	  Value result = TestHelper.computeResult(model, String.format("multi(R{1}max=?[C], R{2}<=%d [ C ])", 30));
        //	  System.out.println(result);
        ModelCheckerResults results = TestHelper.computeResults(model, String.format("multi(R{1}max=?[C], R{2}<=%d [ C ])", 30));
        RawProperty property = results.getProperties().iterator().next();
        Util.printScheduler(System.out, results.getLowLevel(property), results.getScheduler(property));
        System.out.println(results.getResult(property));
        //        Value result = TestHelper.computeResult(model, "multi(Pmax=? [ F (s=167)], P>=0.7 [ F (s=179)])");
        //      Value result;
        /*
      List<Value> results = new ArrayList<>();
      for (int i = 0; i <= 20; i++) {
          model = loadModel(options, "/Users/emhahn/robot_IMDP.prism");
    	  Value result = TestHelper.computeResult(model, String.format("multi(R{1}max=?[C], R{2}<=%d [ C ])", i*5));
    	  results.add(result);
      }
//        Value result = TestHelper.computeResult(model, "multi(R{2}min=? [ C ], R{1}>=0.90[C])");
//        System.out.println(result);
//        ExploreStatistics statistics = exploreModel(model);
 //       System.out.println(statistics);
   //     System.out.println(exploreModelGraph(model));
      for (int i = 0; i <= 20; i++) {
      	System.out.println(i*5 + " " + results.get(i));
      }
         */
    }

    @Test
    public void robotRewardMCJournalTest() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        options.set(OptionsRobot.IMDP_ROBOT_INITIAL_STATE, 508);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDPRobot.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-10");
        options.set(OptionsIMDP.IMDP_INTERVAL_PLAYER, IntervalPlayer.ANTAGONISTIC);
        Model model = loadModel(options, ROBOT_JOURNAL);
//        Value result = TestHelper.computeResult(model, "R{1}max=?[C]");
        Value result = TestHelper.computeResult(model, "multi(R{1}max=?[C])");
//        Value result = TestHelper.computeResult(model, "R{2}max=?[C]");
//        Value result = TestHelper.computeResult(model, String.format("R{1}max=?[C]"));
//        Value result = TestHelper.computeResult(model, String.format("multi(R{1}max=?[C], R{2}>=%f [ C ])", 60.2));
        System.out.println(result);
    }
    
    @Test
    public void robotRewardMCJournalGraphTest() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        options.set(OptionsRobot.IMDP_ROBOT_INITIAL_STATE, 508);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDP.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-10");
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDPRobot.IDENTIFIER);
        options.set(OptionsIMDP.IMDP_INTERVAL_PLAYER, IntervalPlayer.ANTAGONISTIC);
//        UtilTestIMDP.showGraph(options, ROBOT_JOURNAL, "multi(R{1}max=?[C], R{2}<=%f [ C ])",
  //              50, 400, 10);
//        ModelCheckerResults results = TestHelper.computeResults(ROBOT_JOURNAL, String.format("multi(R{1} max=?[C])"));

        
        
        Model model = loadModel(options, ROBOT_JOURNAL);
        ValueReal result = (ValueReal) TestHelper.computeResult(model, "multi(R{1}max=?[C])");

        UtilTestIMDP.exportGraph("/Users/emhahn/Desktop/asdf/robot_IMDP_journal", options, ROBOT_JOURNAL, "multi(R{2}min=?[C], R{1}>=%f [ C ])",
                0.1, result.getDouble(), 100);
    }

    
    
    @Test
    public void robotJournalSchedulerTest() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        options.set(OptionsRobot.IMDP_ROBOT_INITIAL_STATE, 508);
        options.set(OptionsIMDP.IMDP_INTERVAL_PLAYER, IntervalPlayer.ANTAGONISTIC);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDPRobot.IDENTIFIER);
        options.set(OptionsPRISMConverter.PRISM_CONVERTER_REWARD_METHOD, "external");
        //        options.set(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_METHOD, "jacobi");
        Model model = null;
        model = loadModel(options, ROBOT_JOURNAL);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-10");
        options.set(OptionsModelChecker.COMPUTE_SCHEDULER, true);
        Map<String,Class<? extends SchedulerPrinter>> schedulerPrinters = options.get(OptionsModelChecker.SCHEDULER_PRINTER_CLASS); 
        schedulerPrinters.put(SchedulerPrinterSimpleRobot.IDENTIFIER, SchedulerPrinterSimpleRobot.class);
        schedulerPrinters.put(SchedulerPrinterInitialRandomisedRobot.IDENTIFIER, SchedulerPrinterInitialRandomisedRobot.class);
        options.set(OptionsModelChecker.SCHEDULER_PRINTER_CLASS, schedulerPrinters);

        ModelCheckerResults results = TestHelper.computeResults(model, String.format("multi(R{2}min=?[C], R{1}>=0.41 [ C ])"));
        RawProperty property = results.getProperties().iterator().next();
        Util.printScheduler(System.out, results.getLowLevel(property), results.getScheduler(property));
        System.out.println(results.getResult(property));
    }

    /**
     * Create new figures and schedulers for journal version of paper.
     * 
     * @throws FileNotFoundException
     */
    @Test
    public void robotRewardWarehouseTest() throws FileNotFoundException {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDP.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-12");
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDPRobot.IDENTIFIER);
        options.set(OptionsIMDP.IMDP_INTERVAL_PLAYER, IntervalPlayer.ANTAGONISTIC);
        options.set(OptionsModelChecker.COMPUTE_SCHEDULER, true);
        Map<String,Class<? extends SchedulerPrinter>> schedulerPrinters = options.get(OptionsModelChecker.SCHEDULER_PRINTER_CLASS); 
        schedulerPrinters.put(SchedulerPrinterSimpleRobot.IDENTIFIER, SchedulerPrinterSimpleRobot.class);
        schedulerPrinters.put(SchedulerPrinterInitialRandomisedRobot.IDENTIFIER, SchedulerPrinterInitialRandomisedRobot.class);
        options.set(OptionsModelChecker.SCHEDULER_PRINTER_CLASS, schedulerPrinters);

        final String dir = "/Users/emhahn/Desktop/robot/";
        final String maxR1 = "multi(R{1}max=?[C])";
        final String resMaxR1 = "multi(R{1}max=?[C],R{2}<=%f[C])";
        final double start = 0.0;
        final int numSteps = 50;
        
        options.set(OptionsRobot.IMDP_ROBOT_INITIAL_STATE, 508);
        drawGraph(dir + "robot_IMDP_journal", ROBOT_WAREHOUSE_P1, options,
                start, numSteps);
        genSched(dir + "robot_IMDP_journal.txt", ROBOT_JOURNAL, options, maxR1);
        
        options.set(OptionsRobot.IMDP_ROBOT_INITIAL_STATE, 508);
        drawGraph(dir + "robot_warehouse_p1", ROBOT_WAREHOUSE_P1, options,
                start, numSteps);
        genSched(dir + "robot_warehouse_p1_sched.txt", ROBOT_WAREHOUSE_P1, options, maxR1);
        genScheds(dir + "robot_warehouse_p1_sched_%f.txt", ROBOT_WAREHOUSE_P1, options,
                resMaxR1, new double[] {18, 23, 40, 50});

        options.set(OptionsRobot.IMDP_ROBOT_INITIAL_STATE, 2029);
        drawGraph(dir +  "robot_warehouse_p1p2p3_anyorder", ROBOT_WAREHOUSE_P1P2P3_ANYORDER, options,
                start, numSteps);
        genSched(dir + "robot_warehouse_p1p2p3_anyorder_sched.txt", ROBOT_WAREHOUSE_P1P2P3_ANYORDER, options, maxR1);
        genScheds(dir + "robot_warehouse_p1p2p3_anyorder_sched_%f.txt", ROBOT_WAREHOUSE_P1P2P3_ANYORDER, options,
                resMaxR1, new double[] {20, 26, 30, 40, 73, 80});
        
        options.set(OptionsRobot.IMDP_ROBOT_INITIAL_STATE, 846);
        drawGraph(dir +  "robot_warehouse_p1p2p3_strict", ROBOT_WAREHOUSE_P1P2P3_STRICT, options,
                start, numSteps);
        genSched(dir + "robot_warehouse_p1p2p3_strict.txt", ROBOT_WAREHOUSE_P1P2P3_STRICT, options, maxR1);
        genScheds(dir + "robot_warehouse_p1p2p3_strict_sched_%f.txt", ROBOT_WAREHOUSE_P1P2P3_STRICT, options,
                resMaxR1, new double[] {20, 26, 30, 40, 73, 80});

    }

    private void drawGraph(String output, String modelName, Options options, double start, int numSteps) {
        Model model = loadModel(options, modelName);
        final String pareto = "multi(R{2}min=?[C], R{1}>=%f [ C ])";
        final String maxR1 = "multi(R{1}max=?[C])";
        ValueReal result = ValueReal.as(TestHelper.computeResult(model, maxR1));
        UtilTestIMDP.exportGraph(output, options,
                modelName, pareto,
                start, result.getDouble(), numSteps, true);
    }

    private void genScheds(String path, String model, Options options, String prop, double[] bounds) throws FileNotFoundException {
        for (double bound : bounds) {
            genSched(String.format(path, bound),
                    model, options, String.format(prop, bound));
        }
    }

    private void genSched(String path, String modelFn, Options options, String prop) throws FileNotFoundException {
        Model model = loadModel(options, modelFn);
        ModelCheckerResults results = TestHelper.computeResults(model, prop);
        RawProperty property = results.getProperties().iterator().next();
        PrintStream out = new PrintStream(new File(path));
        Util.printScheduler(out, results.getLowLevel(property), results.getScheduler(property));
        out.close();
    }

}
