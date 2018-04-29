package epmc.imdp.lump.signature;

import java.util.Comparator;

import epmc.operator.OperatorEq;
import epmc.operator.OperatorGt;
import epmc.operator.OperatorLt;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeBoolean;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.ValueBoolean;
import epmc.value.ValueInterval;
import epmc.value.ValueReal;

final class ActionSignatureIdentityComparator implements Comparator<ActionSignature>{
    private final OperatorEvaluator eq = ContextValue.get().getEvaluator(OperatorEq.EQ, TypeInterval.get(), TypeInterval.get());
    private final OperatorEvaluator lt = ContextValue.get().getEvaluator(OperatorLt.LT, TypeReal.get(), TypeReal.get());
    private final OperatorEvaluator gt = ContextValue.get().getEvaluator(OperatorGt.GT, TypeReal.get(), TypeReal.get());
    private final ValueBoolean cmp = TypeBoolean.get().newValue();
    
    @Override
    public int compare(ActionSignature o1, ActionSignature o2) {
        int o1Hash = System.identityHashCode(o1);
        int o2Hash = System.identityHashCode(o2);
        if (o1Hash < o2Hash) {
            return -1;
        }
        if (o1Hash > o2Hash) {
            return 1;
        }
        if (o1 == o2) {
            return 0;
        }

        int o1Size = o1.getSize();
        int o2Size = o2.getSize();
        if (o1Size < o2Size) {
            return -1;
        }
        if (o1Size > o2Size) {
            return 1;
        }
        for (int number = 0; number < o1Size; number++) {
            int o1Block = o1.getBlock(number);
            int o2Block = o2.getBlock(number);
            if (o1Block != o2Block) {
                return Integer.compare(o1Block, o2Block);
            }
        }
        for (int number = 0; number < o1Size; number++) {
            ValueInterval o1Value = o1.getValue(number);
            ValueInterval o2Value = o2.getValue(number);
            ValueReal o1ValueL = o1Value.getIntervalLower();
            ValueReal o1ValueU = o1Value.getIntervalUpper();
            ValueReal o2ValueL = o2Value.getIntervalLower();
            ValueReal o2ValueU = o2Value.getIntervalUpper();
            eq.apply(cmp, o1Value, o2Value);
            if (cmp.getBoolean()) {
                return 0;
            }
            lt.apply(cmp, o1ValueL, o2ValueL);
            if (cmp.getBoolean()) {
                return -1;
            }
            lt.apply(cmp, o1ValueU, o2ValueU);
            if (cmp.getBoolean()) {
                return -1;
            }
            gt.apply(cmp, o1ValueL, o2ValueL);
            if (cmp.getBoolean()) {
                return 1;
            }
            gt.apply(cmp, o1ValueU, o2ValueU);
            if (cmp.getBoolean()) {
                return 1;
            }            
        }
        return 0;
    }

}
