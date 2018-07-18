package epmc.param.value.gmp;

import static epmc.param.PARAMTestHelper.preparePARAMOptions;

import org.junit.Test;

import com.sun.jna.Pointer;

import epmc.modelchecker.TestHelper;
import epmc.operator.OperatorAdd;
import epmc.options.Options;
import epmc.param.value.gmp.GMP;
import epmc.param.value.gmp.MPQSingleMemory;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;

public final class GMPTests {
    @Test
    public void testGMP() {
        Options options = preparePARAMOptions();
        TestHelper.processBeforeModelLoading(options);

        MPQSingleMemory gmp = new MPQSingleMemory();
        GMP.__gmpq_set_str(gmp, "-1/0", 10);
//        GMP.__gmpq_add(gmp, gmp, gmp);
        Pointer p = GMP.__gmpq_get_str(null, 10, gmp);
        System.out.println(p.getString(0));
        GMP.gmp_util_free_string(p);
        TypeMPQ type = new TypeMPQ();
        ValueMPQ v = type.newValue();
        v.set("10/11");
        System.out.println(v.getDenominator());
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, type, type);
        add.apply(v, v, v);
        System.out.println(v);
        
//        System.out.println(GMP.size_of_mpq());
//        GMP.mpq_init(gmp);
    }
}
