package epmc.param.points;

import epmc.operator.OperatorSet;
import epmc.param.value.rational.TypeRational;
import epmc.param.value.rational.ValueRational;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeReal;
import epmc.value.ValueReal;

public final class ValueFormatFraction implements ValueFormat {
    public final static String IDENTIFIER = "fraction";
    private final static String DIVIDE = "/";
    private TypeReal lastType;
    private OperatorEvaluator set;
    private final ValueRational valueRation = TypeRational.get().newValue();

    @Override
    public String format(ValueReal value, Side side) {
        assert value != null;
        assert side != null;
        if (ValueRational.is(value)) {
            return formatRational(ValueRational.as(value));
        } else {
            return formatGeneral(value);
        }
    }

    private String formatGeneral(ValueReal value) {
        assert value != null;
        if (lastType != value.getType()) {
            lastType = value.getType();
            set = ContextValue.get().getEvaluator(OperatorSet.SET, lastType, TypeRational.get());
        }
        set.apply(valueRation, value);
        return formatRational(valueRation);
    }

    private String formatRational(ValueRational value) {
        assert value != null;
        return value.getNumerator() + DIVIDE + value.getDenominator();
    }
}
