package epmc.param;

import static epmc.param.PARAMTestHelper.preparePARAMOptions;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import epmc.modelchecker.TestHelper;
import epmc.options.Options;
import epmc.param.options.OptionsParam;
import epmc.param.points.Points;
import epmc.param.points.PointsList;
import epmc.param.points.PointsListIntervals;
import epmc.param.points.PointsRange;
import epmc.param.points.PointsRangeIntervals;
import epmc.param.value.rational.TypeRational;
import epmc.value.ContextValue;
import epmc.value.TypeInterval;
import epmc.value.UtilValue;
import epmc.value.ValueArrayInterval;

public final class PointsTest {
    @Test
    public void testPointsList() {
        Options options = preparePARAMOptions();
        List<String> parameters = new ArrayList<>();
        parameters.add("PF");
        parameters.add("badC");
        options.set(OptionsParam.PARAM_PARAMETER, parameters);
        TestHelper.processBeforeModelLoading(options);
        TypeRational typeReal = TypeRational.get();
        TypeInterval typeInterval = ContextValue.get().makeUnique(new TypeInterval(typeReal));
        Points points = new PointsList.Builder().setInput("0.3 0.4 2/3 3/4 0.12 3/7").build();
        ValueArrayInterval point = UtilValue.newArray(typeInterval.getTypeArray(), 2);
        while (points.hasNext()) {
            points.next(point);
            System.out.println(point);
        }
    }

    @Test
    public void testPointsListInterval() {
        Options options = preparePARAMOptions();
        List<String> parameters = new ArrayList<>();
        parameters.add("PF");
        parameters.add("badC");
        options.set(OptionsParam.PARAM_PARAMETER, parameters);
        TestHelper.processBeforeModelLoading(options);
        TypeRational typeReal = TypeRational.get();
        TypeInterval typeInterval = ContextValue.get().makeUnique(new TypeInterval(typeReal));
        Points points = new PointsListIntervals.Builder().setInput("0.3 0.35 0.4 0.5 2/3 0.37 3/4 0.8 0.12 0.2 3/7 4/7").build();
        ValueArrayInterval point = UtilValue.newArray(typeInterval.getTypeArray(), 2);
        while (points.hasNext()) {
            points.next(point);
            System.out.println(point);
        }
    }

    @Test
    public void testPointsRange() {
        Options options = preparePARAMOptions();
        List<String> parameters = new ArrayList<>();
        parameters.add("PF");
        parameters.add("badC");
        options.set(OptionsParam.PARAM_PARAMETER, parameters);
        TestHelper.processBeforeModelLoading(options);
        TypeRational typeReal = TypeRational.get();
        TypeInterval typeInterval = ContextValue.get().makeUnique(new TypeInterval(typeReal));
        Points points = new PointsRange.Builder().setInput("0.3:0.4:0.05,0.2:0.8:0.3").build();
        ValueArrayInterval point = UtilValue.newArray(typeInterval.getTypeArray(), 2);
        while (points.hasNext()) {
            points.next(point);
            System.out.println(point);
        }
//        options.set(OptionsParam.PARAM_POINTS_EVALUATORS, "general");
  //      Object asdf = options.get(OptionsParam.PARAM_POINTS_EVALUATORS);
    //    System.out.println(asdf);
    }
    
    @Test
    public void testPointsRangeInterval() {
        Options options = preparePARAMOptions();
        List<String> parameters = new ArrayList<>();
        parameters.add("PF");
        parameters.add("badC");
        options.set(OptionsParam.PARAM_PARAMETER, parameters);
        TestHelper.processBeforeModelLoading(options);
        TypeRational typeReal = TypeRational.get();
        TypeInterval typeInterval = ContextValue.get().makeUnique(new TypeInterval(typeReal));
        Points points = new PointsRangeIntervals.Builder().setInput("0.3:0.4:0.05,0.2:0.8:0.3").build();
        ValueArrayInterval point = UtilValue.newArray(typeInterval.getTypeArray(), 2);
        while (points.hasNext()) {
            points.next(point);
            System.out.println(point);
        }
//        options.set(OptionsParam.PARAM_POINTS_EVALUATORS, "general");
  //      Object asdf = options.get(OptionsParam.PARAM_POINTS_EVALUATORS);
    //    System.out.println(asdf);
    }
}
