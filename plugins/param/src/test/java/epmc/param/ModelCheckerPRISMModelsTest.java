package epmc.param;

import static epmc.modelchecker.TestHelper.loadModel;
import static epmc.modelchecker.TestHelper.prepare;
import static epmc.modelchecker.TestHelper.processAfterModelLoading;
import epmc.param.ModelNames;
import static epmc.param.PARAMTestHelper.preparePARAMOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.error.EPMCException;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.Model;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.param.algorithm.NodeEliminator;
import epmc.param.graphsolver.eliminationorder.EliminationOrderFromTargets;
import epmc.param.graphsolver.eliminationorder.EliminationOrderMinProdPredSucc;
import epmc.param.graphsolver.eliminationorder.EliminationOrderNodeNumbersAscending;
import epmc.param.graphsolver.eliminationorder.EliminationOrderNodeNumbersDescending;
import epmc.param.graphsolver.eliminationorder.EliminationOrderNumNew;
import epmc.param.graphsolver.eliminationorder.EliminationOrderNumNew;
import epmc.param.graphsolver.eliminationorder.EliminationOrderQuickTarget;
import epmc.param.graphsolver.eliminationorder.EliminationOrderRandom;
import epmc.param.graphsolver.eliminationorder.EliminationOrderSameStructure;
import epmc.param.options.OptionsParam;
import epmc.param.plugin.TypeProviderIntervalDouble;
import epmc.param.plugin.TypeProviderIntervalRational;
import epmc.param.plugin.TypeProviderRational;
import epmc.param.points.PointResultsExporterData;
import epmc.param.points.PointsRange;
import epmc.param.points.PointsRangeIntervals;
import epmc.param.value.dag.NodeLookupBoundedHashMap;
import epmc.param.value.dag.NodeLookupHashMap;
import epmc.param.value.dag.NodeStoreDisk;
import epmc.param.value.dag.TypeDag;
import epmc.param.value.dag.ValueArrayDag;
import epmc.param.value.dag.ValueDag;
import epmc.param.value.dag.exporter.ExporterC;
import epmc.param.value.dag.exporter.ExporterCInterval;
import epmc.param.value.dag.exporter.ExporterEntryNumber;
import epmc.param.value.dag.exporter.ExporterGinsh;
import epmc.param.value.dag.exporter.ExporterGraphviz;
import epmc.param.value.dag.exporter.ExporterJson;
import epmc.param.value.dag.exporter.ExporterPoints;
import epmc.param.value.dag.exporter.ExporterSimple;
import epmc.param.value.dag.simplifier.DoubleLookupBoundedHashMap;
import epmc.param.value.dag.simplifier.DoubleStoreDisk;
import epmc.param.value.dag.simplifier.EvaluatorDouble;
import epmc.param.value.dag.simplifier.SimplifierProbabilistic;
import epmc.param.value.polynomialfraction.TypePolynomialFraction;
import epmc.param.value.polynomialfraction.exporter.PolynomialFractionExporterDag;
import epmc.param.value.polynomialfraction.exporter.PolynomialFractionExporterPoints;
import epmc.param.value.rational.TypeRational;
import epmc.value.Value;

public final class ModelCheckerPRISMModelsTest {
    @BeforeClass
    public static void initialise() {
        prepare();
    }

    // works great
    @Test
    public void crowdsTest() throws EPMCException, InterruptedException {
        Options options = preparePARAMOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(OptionsParam.PARAM_DAG_USE_PROB_SIMPLIFIER, true);
        List<String> parameters = new ArrayList<>();
        parameters.add("PF");
        parameters.add("badC");
        options.set(OptionsParam.PARAM_PARAMETER, parameters);
//
        //options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderNumNew.IDENTIFIER);
//        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderRandom.IDENTIFIER);
//        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderMinProdPredSucc.IDENTIFIER);
  //    options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderSameStructure.IDENTIFIER);
        Map<String,String> constants = new HashMap<>();
        constants.put("MaxGood", "10");
        constants.put("TotalRuns", "7");
        constants.put("CrowdSize", "5");
//        constants.put("CrowdSize", "15");
        options.set(OptionsModelChecker.CONST, constants);
        options.set(OptionsParam.PARAM_PARAMETER, parameters);
        options.set(OptionsParam.PARAM_FUNCTION_TYPE, TypeDag.IDENTIFIER);
        options.set(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_BITS, "16");
//        options.set(OptionsParam.PARAM_DAG_EXPORTER, ExporterPoints.IDENTIFIER);
      options.set(OptionsParam.PARAM_DAG_EXPORTER, ExporterC.IDENTIFIER);

//        options.set(OptionsParam.PARAM_DAG_EXPORTER, ExporterGraphviz.IDENTIFIER);
//        options.set(OptionsParam.PARAM_DAG_NODE_STORE, NodeStoreDisk.IDENTIFIER);

        options.set(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_DOUBLE_PROB_LOOKUP, DoubleLookupBoundedHashMap.IDENTIFIER);
        options.set(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_DOUBLE_PROB_STORAGE, DoubleStoreDisk.IDENTIFIER);

        options.set(OptionsParam.PARAM_ELIMINATION_SELF_LOOP_METHOD, NodeEliminator.WeighterMethod.SELF_LOOP);
//        options.set(OptionsParam.PARAM_FRACTION_EXPORTER, PolynomialFractionExporterPoints.IDENTIFIER);
//        options.set(OptionsParam.PARAM_DAG_EXPORTER, ExporterPoints.IDENTIFIER);
        
        Model model = loadModel(options, ModelNames.CROWDS);
        processAfterModelLoading(options);
        options.set(OptionsParam.PARAM_POINTS, "0.5:0.51:0.002,0.5:0.51:0.002");
        options.set(OptionsParam.PARAM_POINTS_TYPE, PointsRangeIntervals.IDENTIFIER);
        options.set(OptionsParam.PARAM_POINTS_EVALUATOR_RESULT_TYPE, TypeProviderIntervalDouble.IDENTIFIER);
        options.set(OptionsParam.PARAM_POINTS_EXPORTER, PointResultsExporterData.IDENTIFIER);
        
        Value result = TestHelper.computeResult(model, "Pmax=?[true U (new & runCount=0 & observe0 > observe1 & observe0 > observe2 & observe0 > observe3 & observe0 > observe4 & observe0 > observe5 & observe0 > observe6 & observe0 > observe7 & observe0 > observe8 & observe0 > observe9 & observe0 > observe10 & observe0 > observe11 & observe0 > observe12 & observe0 > observe13 & observe0 > observe14 & observe0 > observe15 & observe0 > observe16 & observe0 > observe17 & observe0 > observe18 & observe0 > observe19)]");
        System.out.println(result);
    }
    
    // works ok, dag gets quite large for larger models
    /**
     * Experiments for herman self stabilising protocol, see
     * http://www.prismmodelchecker.org/casestudies/self-stabilisation.php
     * 
     * @throws EPMCException
     * @throws InterruptedException
     * @throws IOException 
     */
    @Test
    public void hermanTest() throws EPMCException, InterruptedException, IOException {
        Options options = preparePARAMOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(OptionsParam.PARAM_DAG_USE_PROB_SIMPLIFIER, true);

        List<String> parameters = new ArrayList<>();
        parameters.add("p");
        options.set(OptionsParam.PARAM_PARAMETER, parameters);
//
        //options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderNumNew.IDENTIFIER);
//        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderRandom.IDENTIFIER);
//       options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderMinProdPredSucc.IDENTIFIER);
//        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderSameStructure.IDENTIFIER);

        options.set(OptionsParam.PARAM_PARAMETER, parameters);
        options.set(OptionsParam.PARAM_FUNCTION_TYPE, TypeDag.IDENTIFIER);
        options.set(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_BITS, "16");
        options.set(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_NUMBER_TYPE, EvaluatorDouble.IDENTIFIER);
        options.set(OptionsParam.PARAM_DAG_EXPORTER, ExporterC.IDENTIFIER);
        
        options.set(OptionsParam.PARAM_ELIMINATION_SELF_LOOP_METHOD, NodeEliminator.WeighterMethod.SUM);
        Model model = loadModel(options, String.format(ModelNames.HERMAN, 9));
        processAfterModelLoading(options);
        ValueArrayDag result = ValueArrayDag.as(TestHelper.computeResult(model, "R=? [ F \"stable\" ]"));
//        ValueArrayDag result = ValueArrayDag.as(TestHelper.computeResult(model, "P=? [ F \"stable\" ]"));
        System.out.println(result);
//        ValueDag r0 = result.getType().getEntryType().newValue();
  //      result.get(r0, 0);
    //    System.out.println(r0);
    }
    
    // http://www.prismmodelchecker.org/casestudies/cluster.php
    // doesn't scale well, might try out other elimination orders
    @Test
    public void clusterTest() throws EPMCException, InterruptedException {
        Options options = preparePARAMOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        List<String> parameters = new ArrayList<>();
        parameters.add("ws_fail");
        parameters.add("switch_fail");
        parameters.add("line_fail");
//        parameters.add("inspect_rate");
//        parameters.add("repair_cluster_rate");
  //      parameters.add("repair_switch_rate");
    //    parameters.add("repair_backbone_rate");
        options.set(OptionsParam.PARAM_PARAMETER, parameters);
//
//        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderNodeNumbersAscending.IDENTIFIER);
        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderSameStructure.IDENTIFIER);

//        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderNumNew.IDENTIFIER);
//        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderRandom.IDENTIFIER);
//        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderMinProdPredSucc.IDENTIFIER);
    //    options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderNodeNumbersDescending.IDENTIFIER);
      options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderSameStructure.IDENTIFIER);

        Map<String,String> constants = new HashMap<>();
        constants.put("N", "4");
        options.set(OptionsModelChecker.CONST, constants);
        options.set(OptionsParam.PARAM_PARAMETER, parameters);
        options.set(OptionsParam.PARAM_FUNCTION_TYPE, TypeDag.IDENTIFIER);
        options.set(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_DOUBLE_CUTOFF_BIN_DIGITS, 10);
        options.set(OptionsParam.PARAM_DAG_USE_PROB_SIMPLIFIER, true);
        options.set(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_BITS, "16");
//        options.set(OptionsParam.PARAM_DAG_EXPORTER, ExporterGinsh.IDENTIFIER);
        options.set(OptionsParam.PARAM_DAG_EXPORTER, ExporterC.IDENTIFIER);
        options.set(OptionsParam.PARAM_DAG_NODE_STORE, NodeStoreDisk.IDENTIFIER);
        options.set(OptionsParam.PARAM_DAG_NODE_LOOKUP, NodeLookupBoundedHashMap.IDENTIFIER);
//        options.set(OptionsParam.PARAM_DAG_EXPORTER, ExporterC.IDENTIFIER);

        options.set(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_DOUBLE_PROB_STORAGE, DoubleStoreDisk.IDENTIFIER);
        options.set(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_DOUBLE_PROB_LOOKUP, DoubleLookupBoundedHashMap.IDENTIFIER);

        options.set(OptionsParam.PARAM_ELIMINATION_SELF_LOOP_METHOD, NodeEliminator.WeighterMethod.SUM);
        Model model = loadModel(options, ModelNames.CLUSTER);
        processAfterModelLoading(options);
        ValueDag result = ValueDag.as(TestHelper.computeResult(model, "S=? [ \"premium\" ]"));

        System.out.println(result);
    }

    /**
     * Note: not appropriate for structural parametrisation.
     * 
     * http://www.prismmodelchecker.org/casestudies/peer2peer.php
     * 
     * Model construction slow, would be better suited for BDD-based exploration.
     * State elimination scales well. Interestingly, the DAG gets very large, but
     * the resulting function in the end requires very few nodes, if the
     * probabilistic simplifier is used.
     * 
     * @throws EPMCException
     * @throws InterruptedException
     */
    @Test
    public void peer2peerTest() throws EPMCException, InterruptedException {
        Options options = preparePARAMOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(OptionsParam.PARAM_DAG_USE_PROB_SIMPLIFIER, true);
        List<String> parameters = new ArrayList<>();
        parameters.add("mu");
        options.set(OptionsParam.PARAM_PARAMETER, parameters);
        
        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderNodeNumbersAscending.IDENTIFIER);
        options.set(OptionsParam.PARAM_PARAMETER, parameters);
        options.set(OptionsParam.PARAM_FUNCTION_TYPE, TypeDag.IDENTIFIER);
        options.set(OptionsParam.PARAM_DAG_EXPORTER, ExporterGinsh.IDENTIFIER);
        
        options.set(OptionsParam.PARAM_ELIMINATION_SELF_LOOP_METHOD, NodeEliminator.WeighterMethod.SELF_LOOP);
        options.set(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_DOUBLE_PROB_LOOKUP, DoubleLookupBoundedHashMap.IDENTIFIER);
//        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderSameStructure.IDENTIFIER);

        Model model = loadModel(options, String.format(ModelNames.PEER2PEER, 4, 4));
        processAfterModelLoading(options);
        Value result = TestHelper.computeResult(model, "R{\"time\"}=? [ F \"done\" ]");
        System.out.println(result);
    }

//    http://www.prismmodelchecker.org/casestudies/dice.php
    // model is too small to make performance measurements
    @Test
    public void diceTest() throws EPMCException, InterruptedException {
        Options options = preparePARAMOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(OptionsParam.PARAM_DAG_USE_PROB_SIMPLIFIER, true);
        List<String> parameters = new ArrayList<>();
        parameters.add("head");
        options.set(OptionsParam.PARAM_PARAMETER, parameters);
//
        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderNumNew.IDENTIFIER);
//        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderRandom.IDENTIFIER);
//        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderMinProdPredSucc.IDENTIFIER);
        options.set(OptionsParam.PARAM_PARAMETER, parameters);
        options.set(OptionsParam.PARAM_FUNCTION_TYPE, TypePolynomialFraction.IDENTIFIER);
        options.set(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_BITS, "16");
        options.set(OptionsParam.PARAM_DAG_EXPORTER, ExporterGraphviz.IDENTIFIER);
        
        options.set(OptionsParam.PARAM_ELIMINATION_SELF_LOOP_METHOD, NodeEliminator.WeighterMethod.SELF_LOOP);
        Model model = loadModel(options, ModelNames.DICE);
        processAfterModelLoading(options);
//        Value result = TestHelper.computeResult(model, "R{\"coin_flips\"}=? [ F s=7 ]");
      Value result = TestHelper.computeResult(model, "P=? [F s=7 & d=6  ]");
        System.out.println(result);
    }

    // http://www.prismmodelchecker.org/casestudies/brp.php
    // scales well, already worked fine without the dag representation
    @Test
    public void brpTest() throws EPMCException, InterruptedException {
        Options options = preparePARAMOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        options.set(OptionsParam.PARAM_DAG_USE_PROB_SIMPLIFIER, true);
        Map<String,String> constants = new HashMap<>();
        constants.put("N", "1024");
        constants.put("MAX", "20");
        options.set(OptionsModelChecker.CONST, constants);
        List<String> parameters = new ArrayList<>();
        parameters.add("pK");
        parameters.add("pL");
        options.set(OptionsParam.PARAM_PARAMETER, parameters);
//
        //options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderNumNew.IDENTIFIER);
//        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderRandom.IDENTIFIER);
//        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderMinProdPredSucc.IDENTIFIER);
        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderSameStructure.IDENTIFIER);
        options.set(OptionsParam.PARAM_PARAMETER, parameters);
        options.set(OptionsParam.PARAM_FUNCTION_TYPE, TypeDag.IDENTIFIER);
        options.set(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_BITS, "16");
        options.set(OptionsParam.PARAM_DAG_EXPORTER, ExporterGraphviz.IDENTIFIER);
        
        options.set(OptionsParam.PARAM_ELIMINATION_SELF_LOOP_METHOD, NodeEliminator.WeighterMethod.SELF_LOOP);
        Model model = loadModel(options, ModelNames.BRP);
        processAfterModelLoading(options);
        Value result;
//        result = TestHelper.computeResult(model, "P=?[ F srep=1 & rrep=3 & recv ]");
//        result = TestHelper.computeResult(model, "P=?[ F srep=3 & !(rrep=3) & recv ]");
//        result = TestHelper.computeResult(model, "P=?[ F s=5 ]");
        result = TestHelper.computeResult(model, "P=?[ F s=5 & srep=2 ]");
//        result = TestHelper.computeResult(model, "P=?[ F s=5 & srep=1 & i>8 ]");
     //   result = TestHelper.computeResult(model, "P=?[ F !(srep=0) & !recv ]");

        System.out.println(result);
    }
    
    // http://www.prismmodelchecker.org/casestudies/kanban.php
    // does not work well, even for t=2 takes too long
    @Test
    public void kanbanTest() throws EPMCException, InterruptedException {
        Options options = preparePARAMOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Map<String,String> constants = new HashMap<>();
        constants.put("t", "2");
        options.set(OptionsModelChecker.CONST, constants);
        List<String> parameters = new ArrayList<>();
        parameters.add("in1");
        parameters.add("out4");
        parameters.add("synch123");
        parameters.add("synch234");
        parameters.add("back");
        parameters.add("redo1");
        parameters.add("redo2");
        parameters.add("redo3");
        parameters.add("redo4");
        parameters.add("ok1");
        parameters.add("ok2");
        parameters.add("ok3");
        parameters.add("ok4");
        options.set(OptionsParam.PARAM_PARAMETER, parameters);
        options.set(OptionsParam.PARAM_DAG_USE_PROB_SIMPLIFIER, true);

//        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderNumNew.IDENTIFIER);
//        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderRandom.IDENTIFIER);
//        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderMinProdPredSucc.IDENTIFIER);
//        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderNodeNumbersDescending.IDENTIFIER);

        options.set(OptionsParam.PARAM_PARAMETER, parameters);
        options.set(OptionsParam.PARAM_FUNCTION_TYPE, TypeDag.IDENTIFIER);
        options.set(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_BITS, "16");
//        options.set(OptionsParam.PARAM_DAG_EXPORTER, ExporterC.IDENTIFIER);
        options.set(OptionsParam.PARAM_DAG_EXPORTER, ExporterGinsh.IDENTIFIER);
        
        options.set(OptionsParam.PARAM_ELIMINATION_SELF_LOOP_METHOD, NodeEliminator.WeighterMethod.SUM);
        Model model = loadModel(options, ModelNames.KANBAN);
        processAfterModelLoading(options);
        Value result;
        result = TestHelper.computeResult(model, "R{\"throughput\"}=? [ S ]");

        System.out.println(result);
    }

    // http://www.prismmodelchecker.org/casestudies/polling.php
    // works OK, but not great
    @Test
    public void pollingTest() throws EPMCException, InterruptedException {
        Options options = preparePARAMOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Map<String,String> constants = new HashMap<>();
        options.set(OptionsModelChecker.CONST, constants);
        List<String> parameters = new ArrayList<>();
        parameters.add("mu");
        parameters.add("gamma");
        options.set(OptionsParam.PARAM_PARAMETER, parameters);
        options.set(OptionsParam.PARAM_DAG_USE_PROB_SIMPLIFIER, true);

    //    options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderNumNew.IDENTIFIER);
//        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderRandom.IDENTIFIER);
//        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderMinProdPredSucc.IDENTIFIER);
//        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderNodeNumbersDescending.IDENTIFIER);
//        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderSameStructure.IDENTIFIER);

        options.set(OptionsParam.PARAM_PARAMETER, parameters);
        options.set(OptionsParam.PARAM_FUNCTION_TYPE, TypeDag.IDENTIFIER);
        options.set(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_BITS, "16");
//        options.set(OptionsParam.PARAM_DAG_EXPORTER, ExporterC.IDENTIFIER);
        options.set(OptionsParam.PARAM_DAG_EXPORTER, ExporterGinsh.IDENTIFIER);

        
        options.set(OptionsParam.PARAM_ELIMINATION_SELF_LOOP_METHOD, NodeEliminator.WeighterMethod.SELF_LOOP);
        Model model = loadModel(options, String.format(ModelNames.POLLING, 6));
        processAfterModelLoading(options);
        Value result;
        result = TestHelper.computeResult(model, "S=? [ s1=0 ] ");

        System.out.println(result);
    }

    // http://www.prismmodelchecker.org/casestudies/robot.php
    // works ok for smaller models but does not scale great
    @Test
    public void robotTest() throws EPMCException, InterruptedException {
        Options options = preparePARAMOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Map<String,String> constants = new HashMap<>();
        constants.put("n", "6");
        options.set(OptionsModelChecker.CONST, constants);
        List<String> parameters = new ArrayList<>();
        parameters.add("mr");
        parameters.add("mj");
        parameters.add("cr1");
        parameters.add("cr2");
        options.set(OptionsParam.PARAM_PARAMETER, parameters);
        options.set(OptionsParam.PARAM_DAG_USE_PROB_SIMPLIFIER, true);

//        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderNumNew.IDENTIFIER);
//        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderRandom.IDENTIFIER);
//        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderMinProdPredSucc.IDENTIFIER);
//        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderNodeNumbersDescending.IDENTIFIER);
      options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderSameStructure.IDENTIFIER);

        options.set(OptionsParam.PARAM_PARAMETER, parameters);
        options.set(OptionsParam.PARAM_FUNCTION_TYPE, TypeDag.IDENTIFIER);
        options.set(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_BITS, "16");
//        options.set(OptionsParam.PARAM_DAG_EXPORTER, ExporterC.IDENTIFIER);
        options.set(OptionsParam.PARAM_DAG_EXPORTER, ExporterGinsh.IDENTIFIER);
      
        options.set(OptionsParam.PARAM_ELIMINATION_SELF_LOOP_METHOD, NodeEliminator.WeighterMethod.SUM);
        Model model = loadModel(options, ModelNames.ROBOT);
        processAfterModelLoading(options);
        ValueDag result;
        result = ValueDag.as(TestHelper.computeResult(model, "R{\"steps\"}=?[F x1=n & y1=n] "));
        
        System.out.println(result);
    }

    // http://www.prismmodelchecker.org/casestudies/thinkteam.php
    // nice but too small
    @Test
    public void thinkteamTest() throws EPMCException, InterruptedException {
        Options options = preparePARAMOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Map<String,String> constants = new HashMap<>();
//        constants.put("n", "4");
        options.set(OptionsModelChecker.CONST, constants);
        List<String> parameters = new ArrayList<>();
        parameters.add("lambda");
        parameters.add("mu");
        parameters.add("theta");
        options.set(OptionsParam.PARAM_PARAMETER, parameters);
        options.set(OptionsParam.PARAM_DAG_USE_PROB_SIMPLIFIER, true);

//        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderNumNew.IDENTIFIER);
//        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderRandom.IDENTIFIER);
//        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderMinProdPredSucc.IDENTIFIER);
        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderNodeNumbersDescending.IDENTIFIER);

        options.set(OptionsParam.PARAM_PARAMETER, parameters);
        options.set(OptionsParam.PARAM_FUNCTION_TYPE, TypeDag.IDENTIFIER);
        options.set(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_BITS, "16");
        options.set(OptionsParam.PARAM_DAG_EXPORTER, ExporterC.IDENTIFIER);
//        options.set(OptionsParam.PARAM_DAG_EXPORTER, ExporterGinsh.IDENTIFIER);
      
        options.set(OptionsParam.PARAM_ELIMINATION_SELF_LOOP_METHOD, NodeEliminator.WeighterMethod.SUM);
        Model model = loadModel(options, ModelNames.THINKTEAM_RETRIAL);
        processAfterModelLoading(options);
        ValueDag result;
        result = ValueDag.as(TestHelper.computeResult(model, "S=? [User_STATE = 2]"));
        
        System.out.println(result);
    }


    // http://www.prismmodelchecker.org/casestudies/prob_broadcast.php
    
    // http://www.prismmodelchecker.org/casestudies/contract_egl.php
    
    // http://www.prismmodelchecker.org/casestudies/cyclin.php
    
//    http://www.prismmodelchecker.org/casestudies/fgf.php
    
    // http://www.prismmodelchecker.org/casestudies/mapk_cascade.php
    
    // http://www.prismmodelchecker.org/casestudies/dna_walkers.php
    
    // http://www.prismmodelchecker.org/casestudies/molecules.php
    
    // http://www.prismmodelchecker.org/tutorial/circadian.php
    
    // http://www.prismmodelchecker.org/casestudies/power.php
            
    // http://www.prismmodelchecker.org/casestudies/negotiation.php
    
    // http://www.prismmodelchecker.org/casestudies/thinkteam.php
    
    // http://www.prismmodelchecker.org/casestudies/embedded.php
    
    // http://www.prismmodelchecker.org/casestudies/nand.php
    
    // http://www.prismmodelchecker.org/casestudies/cell.php
            
    // http://www.prismmodelchecker.org/casestudies/fms.php
        
    // http://www.prismmodelchecker.org/casestudies/tandem.php
}
