package epmc.param;

import static epmc.param.PARAMTestHelper.preparePARAMOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import epmc.error.EPMCException;
import epmc.jani.interaction.Analyse;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.Log;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.param.algorithm.NodeEliminator;
import epmc.param.command.CommandTaskLoadFunction;
import epmc.param.graphsolver.eliminationorder.EliminationOrderNumNew;
import epmc.param.options.OptionsParam;
import epmc.param.plugin.TypeProviderIntervalDouble;
import epmc.param.plugin.TypeProviderIntervalRational;
import epmc.param.points.PointResultsExporterData;
import epmc.param.points.PointResultsExporterPgfplots;
import epmc.param.points.PointsRange;
import epmc.param.points.PointsRangeIntervals;
import epmc.param.points.ValueFormatDouble;
import epmc.param.points.ValueFormatDoubleHex;
import epmc.param.value.dag.TypeDag;
import epmc.param.value.dag.exporter.ExporterPoints;

public final class LoadFunctionTest {
    @Test
    public void loadFunctionTest() throws EPMCException, InterruptedException {
        Options options = preparePARAMOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        List<String> parameters = new ArrayList<>();
        options.set(OptionsParam.PARAM_PARAMETER, parameters);
        options.set(OptionsParam.PARAM_FUNCTION_TYPE, TypeDag.IDENTIFIER);
        options.set(OptionsParam.PARAM_ELIMINATION_ORDER, EliminationOrderNumNew.IDENTIFIER);
        options.set(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_BITS, "16");
        options.set(OptionsParam.PARAM_DAG_USE_PROB_SIMPLIFIER, true);
        options.set(OptionsParam.PARAM_ELIMINATION_SELF_LOOP_METHOD, NodeEliminator.WeighterMethod.SUM);
        options.set(OptionsParam.PARAM_DAG_EXPORTER, ExporterPoints.IDENTIFIER);
//        options.set(OptionsParam.PARAM_POINTS_EVALUATOR_RESULT_TYPE, "rational");
//        options.set(OptionsParam.PARAM_POINTS_EXPORTER_POINT_FORCE_INTERVAL, true);
//        options.set(OptionsParam.PARAM_POINTS_EXPORTER_RESULT_FORCE_INTERVAL, true);
//        options.set(OptionsParam.PARAM_POINTS_EXPORTER_POINT_FORMAT, ValueFormatDoubleHex.IDENTIFIER);
//        options.set(OptionsParam.PARAM_POINTS_EXPORTER_RESULT_FORMAT, ValueFormatDoubleHex.IDENTIFIER);
        options.set(OptionsParam.PARAM_POINTS_EXPORTER, PointResultsExporterData.IDENTIFIER);
        
//        points.append("0.3 0.3");
        options.set(OptionsParam.PARAM_POINTS_TYPE, PointsRangeIntervals.IDENTIFIER);
//        String points = "0.2:0.20025:0.000001,0.8:0.80025:0.000001";
        String points = "0:1:0.02,0:1:0.02";
        options.set(OptionsParam.PARAM_POINTS, points);
        Map<String,String> constants = new HashMap<>();
        options.set(OptionsModelChecker.CONST, constants);
        Log log = TestHelper.prepareLog(options);
        options.set(OptionsParam.PARAM_FUNCTION_INPUT_FILENAME, "/Users/emhahn/Documents/workspace/ePMC/plugins/param/src/test/resources/epmc/param/dag.json");
        options.set(OptionsParam.PARAM_POINTS_EVALUATOR_RESULT_TYPE, TypeProviderIntervalDouble.IDENTIFIER);
        options.set(Options.COMMAND, CommandTaskLoadFunction.IDENTIFIER);
//        options.set(OptionsParam.PARAM_FUNCTION_INPUT_FILENAME, value);
        Analyse.execute(null, options, log);
    }
}
