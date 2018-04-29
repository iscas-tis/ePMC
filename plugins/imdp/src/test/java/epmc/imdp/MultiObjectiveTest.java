package epmc.imdp;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import epmc.graph.CommonProperties;
import epmc.graph.SchedulerPrinter;
import epmc.graphsolver.OptionsGraphsolver;
import epmc.imdp.IntervalPlayer;
import epmc.imdp.model.ModelIMDP;
import epmc.imdp.model.PropertyIMDP;
import epmc.imdp.options.OptionsIMDP;
import epmc.imdp.options.OptionsIMDPLump;
import epmc.imdp.robot.ModelIMDPRobot;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.Log;
import epmc.modelchecker.Model;
import epmc.modelchecker.ModelCheckerResults;
import epmc.modelchecker.RawProperty;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.prism.model.convert.OptionsPRISMConverter;
import epmc.util.Util;
import epmc.value.Value;

import static epmc.imdp.ModelNames.*;
import static epmc.modelchecker.TestHelper.*;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class MultiObjectiveTest {
    /**
     * Set up the tests.
     */
    @BeforeClass
    public static void initialise() {
        prepare();
    }

    @Test
    public void toyTest() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        options.set(OptionsModelChecker.PROPERTY_INPUT_TYPE, PropertyIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        Set<Object> nodeProperties = new LinkedHashSet<>();
        nodeProperties.add(CommonProperties.STATE);
        Value result = TestHelper.computeResult(options, TOY, "multi(Pmax=?[(pc1=0 | pc1=3) U (pc1=2)])");
        System.out.println(result);
    }

    @Test
    public void coin2Test() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        options.set(OptionsIMDP.IMDP_INTERVAL_PLAYER, IntervalPlayer.ANTAGONISTIC);
        Log log = prepareLog(options, LogType.TRANSLATE);
        options.set(OptionsMessages.LOG, log);
        options.set(OptionsModelChecker.PROPERTY_INPUT_TYPE, PropertyIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        Set<Object> nodeProperties = new LinkedHashSet<>();
        nodeProperties.add(CommonProperties.STATE);
        options.set(OptionsGraphsolver.GRAPHSOLVER_LUMP_BEFORE_GRAPH_SOLVING, true);
        Value result = TestHelper.computeResult(options, String.format(COIN, 2), "multi(Pmin=? [ F ((pc1=3 & pc2=3) & (coin1=0 & coin2=0)) ])");
        System.out.println(result);
    }

    @Test
    public void coin4Test() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        Log log = prepareLog(options, LogType.TRANSLATE);
        options.set(OptionsMessages.LOG, log);
        options.set(OptionsModelChecker.PROPERTY_INPUT_TYPE, PropertyIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-12");
        Set<Object> nodeProperties = new LinkedHashSet<>();
        nodeProperties.add(CommonProperties.STATE);

        Value result = TestHelper.computeResult(options, String.format(COIN, 4), "multi(Pmin=? [ F ((pc1=3 & pc2=3 & pc3=3 & pc4=3) & (coin1=0 & coin2=0 & coin3=0 & coin4=0)) ])");
        System.out.println(result);
    }

    @Test
    public void coin5Test() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        Log log = prepareLog(options, LogType.TRANSLATE);
        options.set(OptionsMessages.LOG, log);
        options.set(OptionsGraphsolver.GRAPHSOLVER_LUMP_BEFORE_GRAPH_SOLVING, true);
        options.set(OptionsModelChecker.PROPERTY_INPUT_TYPE, PropertyIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-12");
        Set<Object> nodeProperties = new LinkedHashSet<>();
        nodeProperties.add(CommonProperties.STATE);

        Value result = TestHelper.computeResult(options, String.format(COIN, 5), "multi(Pmin=? [ F ((pc1=3 & pc2=3 & pc3=3 & pc4=3 & pc5=3) & (coin1=0 & coin2=0 & coin3=0 & coin4=0 & coin5=0)) ])");
        System.out.println(result);
    }

    @Test
    public void coin6Test() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        Log log = prepareLog(options, LogType.TRANSLATE);
        options.set(OptionsMessages.LOG, log);
        options.set(OptionsModelChecker.PROPERTY_INPUT_TYPE, PropertyIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        Set<Object> nodeProperties = new LinkedHashSet<>();
        nodeProperties.add(CommonProperties.STATE);

        Value result = TestHelper.computeResult(options, String.format(COIN, 6), "multi(Pmin=? [ F ((pc1=3 & pc2=3 & pc3=3 & pc4=3 & pc5=3 & pc6=3) & (coin1=0 & coin2=0 & coin3=0 & coin4=0 & coin5=0 & coin6=0)) ])");
        System.out.println(result);
    }

    @Test
    public void zeroconf100Test() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        Log log = prepareLog(options, LogType.TRANSLATE);
        options.set(OptionsMessages.LOG, log);
        options.set(OptionsModelChecker.PROPERTY_INPUT_TYPE, PropertyIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        Set<Object> nodeProperties = new LinkedHashSet<>();
        nodeProperties.add(CommonProperties.STATE);
        Value result = TestHelper.computeResult(options, String.format(ZEROCONF_DL, 100), "multi(Pmax=? [ (!(l=4 & ip=2)) U (t>=100) ])");
        System.out.println(result);
    }

    @Test
    public void zeroconf25Test() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        Log log = prepareLog(options, LogType.TRANSLATE);
        options.set(OptionsMessages.LOG, log);
        options.set(OptionsModelChecker.PROPERTY_INPUT_TYPE, PropertyIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsIMDPLump.IMDP_SHORTCUT_SINGLE_ACTION_BEFORE_NORMALISATION, true);
        Set<Object> nodeProperties = new LinkedHashSet<>();
        nodeProperties.add(CommonProperties.STATE);

        Value result = TestHelper.computeResult(options, String.format(ZEROCONF_DL, 25), "multi(Pmax=? [ (!(l=4 & ip=2)) U (t>=25) ])");
        System.out.println(result);
    }

    @Test
    public void philNofair6E() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        Log log = prepareLog(options, LogType.TRANSLATE);
        options.set(OptionsMessages.LOG, log);
        options.set(OptionsModelChecker.PROPERTY_INPUT_TYPE, PropertyIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsIMDPLump.IMDP_SHORTCUT_SINGLE_ACTION_BEFORE_NORMALISATION, true);

        Value result = TestHelper.computeResult(options, String.format(PHIL_NOFAIR, 6), "multi(Pmax=? [ (F (((p1>=8)&(p1<=9))|((p2>=8)&(p2<=9))|((p3>=8)&(p3<=9))|((p4>=8)&(p4<=9))|((p5>=8)&(p5<=9))|((p6>=8)&(p6<=9)))) ])");
        System.out.println(result);
    }

    @Ignore
    @Test
    public void philNofair7E() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        Log log = prepareLog(options, LogType.TRANSLATE);
        options.set(OptionsMessages.LOG, log);
        options.set(OptionsModelChecker.PROPERTY_INPUT_TYPE, PropertyIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDP.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(TestHelper.ITERATION_TOLERANCE, "1.0E-15");
        options.set(OptionsIMDPLump.IMDP_SHORTCUT_SINGLE_ACTION_BEFORE_NORMALISATION, true);
        Set<Object> nodeProperties = new LinkedHashSet<>();
        nodeProperties.add(CommonProperties.STATE);

        Value result = TestHelper.computeResult(options, String.format(PHIL_NOFAIR, 7), "multi(Pmax=? [ (F (((p1>=8)&(p1<=9))|((p2>=8)&(p2<=9))|((p3>=8)&(p3<=9))|((p4>=8)&(p4<=9))|((p5>=8)&(p5<=9))|((p6>=8)&(p6<=9))|((p7>=8)&(p7<=9)))) ])");
        System.out.println(result);
    }

    @Test
    public void robotTest() {
        Options options = UtilTestIMDP.prepareIMDPOptions();
        options.set(OptionsIMDP.IMDP_INTERVAL_PLAYER, IntervalPlayer.ANTAGONISTIC);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelIMDPRobot.IDENTIFIER);
        options.set(OptionsPRISMConverter.PRISM_CONVERTER_REWARD_METHOD, "external");
        //        options.set(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_METHOD, "jacobi");
        Model model = null;
        model = loadModel(options, "epmc/imdp/robot_IMDP.txt");
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
}
