package epmc.param.points;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import epmc.options.Options;
import epmc.options.UtilOptions;
import epmc.param.options.OptionsParam;
import epmc.param.plugin.TypeProvider;
import epmc.param.value.FunctionEvaluator;
import epmc.param.value.FunctionEvaluator.Builder;
import epmc.param.value.rational.ValueRational;
import epmc.param.value.ValueFunction;
import epmc.util.Util;
import epmc.value.TypeAlgebra;
import epmc.value.ValueDouble;
import epmc.value.ValueReal;

public final class UtilPoints {
    private final static int DECIMAL_DIGITS_REQUIRED = 34;
    private final static MathContext MIDDLE = new MathContext(DECIMAL_DIGITS_REQUIRED);
    private final static MathContext LEFT = new MathContext(DECIMAL_DIGITS_REQUIRED, RoundingMode.FLOOR);
    private final static MathContext RIGHT = new MathContext(DECIMAL_DIGITS_REQUIRED, RoundingMode.CEILING);
    private final static Map<Side, MathContext> ROUNDING_MAP;
    static {
        EnumMap<Side, MathContext> roundingMap = new EnumMap<>(Side.class);
        roundingMap.put(Side.LEFT, LEFT);
        roundingMap.put(Side.MIDDLE, MIDDLE);
        roundingMap.put(Side.RIGHT, RIGHT);
        ROUNDING_MAP = java.util.Collections.unmodifiableMap(roundingMap);
    }

    public static FunctionEvaluator getFunctionEvaluator(Points points, List<ValueFunction> functions) {
        return getFunctionEvaluator(points.isIntervals(), functions);
    }
    
    public static FunctionEvaluator getFunctionEvaluator(boolean intervals, List<ValueFunction> functions) {
        assert functions != null;
        Set<Class<FunctionEvaluator.Builder>> evaluators = Options.get().get(OptionsParam.PARAM_POINTS_EVALUATORS);
        TypeProvider provider = UtilOptions.getInstance(OptionsParam.PARAM_POINTS_EVALUATOR_RESULT_TYPE);
        TypeAlgebra type = provider.provide();
        for (Class<FunctionEvaluator.Builder> clazz : evaluators) {
            assert clazz != null;
            Builder builder = Util.getInstance(clazz);
            assert builder != null;
            for (ValueFunction function : functions) {
                builder.setResultType(type);
                builder.setPointsUseIntervals(intervals).addFunction(function);
            }
            FunctionEvaluator result = builder.build();
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public static Points getPoints() {
        Points.Builder builder = UtilOptions.getInstance(OptionsParam.PARAM_POINTS_TYPE);
        assert builder != null;
        String pointsSpec = Options.get().get(OptionsParam.PARAM_POINTS);
        return builder.setInput(pointsSpec).build();
    }

    public static PointResultsExporter getExporter(PointResults results) {
        PointResultsExporter.Builder builder = UtilOptions.getInstance(OptionsParam.PARAM_POINTS_EXPORTER);
        builder.setPointResults(results);
        PointResultsExporter result = builder.build();
        assert result != null : builder.getClass();
        return result;
    }
    
    public static PointResultsExporter getExporter(List<ValueFunction> functions) {
        assert functions != null;
        for (ValueFunction function : functions) {
            assert function != null;
        }
        Points points = UtilPoints.getPoints();
        FunctionEvaluator functionEvaluator = UtilPoints.getFunctionEvaluator(points, functions);
        PointResults results = new PointResultsFunctionEvaluator.Builder()
                .setFunctionEvaluator(functionEvaluator)
                .setPoints(points)
                .build();
        assert results != null;
        return getExporter(results);
    }

    public static double rationalToDouble(ValueRational value, Side side) {
        assert value != null;
        assert side != null;
        BigInteger num = value.getNumerator();
        BigInteger den = value.getDenominator();
        return fractionToDouble(num, den, side);
    }

    public static double fractionToDouble(BigInteger num, BigInteger den, Side side) {
        assert num != null;
        assert den != null;
        assert side != null;
        BigDecimal bigNum = new BigDecimal(num);
        BigDecimal bigDen = new BigDecimal(den);
        MathContext rounding = ROUNDING_MAP.get(side);
        BigDecimal divided = bigNum.divide(bigDen, rounding);
        double dividedDouble = divided.doubleValue();
        return correctRounding(dividedDouble, divided, side);
    }

    private static double correctRounding(double dividedDouble, BigDecimal divided, Side side) {
        if (side == Side.MIDDLE) {
            return dividedDouble;
        }
        BigDecimal dividedDoubleBigDecimal = new BigDecimal(dividedDouble);
        if (side == Side.LEFT && dividedDoubleBigDecimal.compareTo(divided) > 0) {
            dividedDouble = Math.nextDown(dividedDouble);
        } else if (side == Side.RIGHT  && dividedDoubleBigDecimal.compareTo(divided) < 0) {
            dividedDouble = Math.nextUp(dividedDouble);            
        }
        dividedDoubleBigDecimal = new BigDecimal(dividedDouble);
        assert side == Side.LEFT && dividedDoubleBigDecimal.compareTo(divided) <= 0
                || side == Side.RIGHT && dividedDoubleBigDecimal.compareTo(divided) >= 0;
        return dividedDouble;
    }
    
    public static double realToDouble(ValueReal value, Side side) {
        double doubleValue;
        if (ValueRational.is(value)) {
            doubleValue = rationalToDouble(ValueRational.as(value), side);
        } else if (ValueDouble.is(value)) {
            doubleValue = value.getDouble();
        } else {
            doubleValue = value.getDouble();
            if (side == Side.LEFT) {
                doubleValue = Math.nextDown(doubleValue);
            } else if (side == Side.RIGHT) {
                doubleValue = Math.nextUp(doubleValue);
            }            
        }
        return doubleValue;
    }
    
    private UtilPoints() {
    }
}
