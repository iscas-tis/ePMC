package epmc.param;

import static epmc.modelchecker.TestHelper.loadModel;
import static epmc.modelchecker.TestHelper.processAfterModelLoading;
import static epmc.param.ModelNames.CROWDS;
import static epmc.ModelNamesOwn.PAUL_GAINER_MEDIUM;
import static epmc.ModelNamesOwn.PAUL_GAINER_SMALL;
import static epmc.param.PARAMTestHelper.preparePARAMOptions;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import epmc.error.EPMCException;
import epmc.main.options.UtilOptionsEPMC;
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
import epmc.param.options.OptionsParam;
import epmc.param.value.dag.TypeDag;
import epmc.param.value.dag.exporter.ExporterC;
import epmc.param.value.dag.exporter.ExporterEntryNumber;
import epmc.param.value.dag.exporter.ExporterGinsh;
import epmc.param.value.dag.exporter.ExporterGraphviz;
import epmc.util.StopWatch;
import epmc.value.TypeDouble;
import epmc.value.TypeWeight;
import epmc.value.TypeWeightTransition;
import epmc.value.Value;

public final class ModelCheckerPaulTest {
    @Test
    public void PaulSmallTest() throws EPMCException, InterruptedException {
        Options options = preparePARAMOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        List<String> parameters = new ArrayList<>();
        parameters.add("ML");
        options.set(OptionsParam.PARAM_PARAMETER, parameters);
        options.set(OptionsParam.PARAM_FUNCTION_TYPE, TypeDag.IDENTIFIER);
        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderNumNew.IDENTIFIER);
        options.set(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_BITS, "16");
        options.set(OptionsParam.PARAM_DAG_USE_PROB_SIMPLIFIER, true);
        options.set(OptionsParam.PARAM_ELIMINATION_SELF_LOOP_METHOD, NodeEliminator.WeighterMethod.SUM);
//        options.set(OptionsParam.PARAM_DAG_EXPORTER, ExporterC.IDENTIFIER);
        options.set(OptionsParam.PARAM_DAG_EXPORTER, ExporterGinsh.IDENTIFIER);

//        options.set(OptionsParam.PARAM_CANCELLATOR, "cocoalib");
//        options.set(OptionsParam.PARAM_CANCELLATOR, "ginac");
        Map<String,String> constants = new HashMap<>();
//        constants.put("ML", "0.5");
        options.set(OptionsModelChecker.CONST, constants);
        Model model = loadModel(options, PAUL_GAINER_SMALL);
        processAfterModelLoading(options);
//        Value result = TestHelper.computeResult(model, "P=?[F synchronised]");
        Value result = TestHelper.computeResult(model, "R{\"power_consumption\"}=?[F synchronised]");
        try (PrintWriter out = new PrintWriter("/Users/emhahn/a.txt")) {
            StopWatch watch = new StopWatch(true);
            out.println(result);
        }  catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
