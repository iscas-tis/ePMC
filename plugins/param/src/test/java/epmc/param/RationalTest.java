package epmc.param;

import static epmc.param.PARAMTestHelper.preparePARAMOptions;

import org.junit.Test;

import epmc.error.EPMCException;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.operator.OperatorAdd;
import epmc.options.Options;
import epmc.param.value.rational.TypeRationalBigInteger;
import epmc.param.value.rational.ValueRational;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;

public final class RationalTest {
    @Test
    public void setRationalBigIntegerTest() throws EPMCException, InterruptedException {
        Options options = preparePARAMOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        TypeRationalBigInteger type = new TypeRationalBigInteger();
        ValueRational value = type.newValue();
        value.set("0");
        System.out.println(value);
        value.set("1");
        System.out.println(value);
        value.set("53");
        System.out.println(value);
        value.set("-1");
        System.out.println(value);
        value.set("-134");
        System.out.println(value);
        value.set("134/2");
        System.out.println(value);
        value.set("-134/2");
        System.out.println(value);
        value.set("-134/-2");
        System.out.println(value);
        value.set("134/-2");
        System.out.println(value);
        value.set("123.0");
        System.out.println(value);
        value.set("-123.0");
        System.out.println(value);
        value.set("133.446");
        System.out.println(value);
        value.set("-133.446");
        System.out.println(value);
        value.set("133.446E6");
        System.out.println(value);
        value.set("-133.446e6");
        System.out.println(value);
        value.set("133.446E-5");
        System.out.println(value);
        value.set("-133.446e-5");
        System.out.println(value);
    }
    
    @Test
    public void operationsRationalBigIntegerTest() throws EPMCException, InterruptedException {
        Options options = preparePARAMOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        TestHelper.processBeforeModelLoading(options);
        TypeRationalBigInteger type = new TypeRationalBigInteger();
        ValueRational value1 = type.newValue();
        value1.set("3/4");
        ValueRational value2 = type.newValue();
        value2.set("-2/7");
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, type, type);
        ValueRational result = type.newValue();
        add.apply(result, value1, value2);
        System.out.println(result);
    }
}
