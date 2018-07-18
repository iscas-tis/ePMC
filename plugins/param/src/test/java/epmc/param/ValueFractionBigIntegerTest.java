package epmc.param;

import static epmc.modelchecker.TestHelper.prepare;
import static epmc.modelchecker.TestHelper.processAfterModelLoading;
import static epmc.param.PARAMTestHelper.preparePARAMOptions;
import static org.junit.Assert.*;

import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.error.EPMCException;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.value.TypeDouble;
import epmc.value.TypeReal;
import epmc.value.ValueDouble;

public final class ValueFractionBigIntegerTest {
    @BeforeClass
    public static void initialise() {
        prepare();
    }
    
    @Test
    public void valueFractionBigIntegerTest() throws EPMCException {
        Options options = preparePARAMOptions();
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        double delta = 1E-10;
        
        processAfterModelLoading(options);

        TypeReal type = TypeReal.get();

        checkDouble(type, 0, delta);
        checkDouble(type, 1, delta);
        checkDouble(type, 12534, delta);
        checkDouble(type, 2E10, delta);
        checkDouble(type, 3E10, delta);
        checkDouble(type, 2.0E10, delta);
        checkDouble(type, 3.0E10, delta);
        checkDouble(type, 2.12E10, delta);
        checkDouble(type, 3.3425E10, delta);
        checkDouble(type, 2.12E23, delta);
        checkDouble(type, 3.3425E23, delta);
        checkDouble(type, 5.4729390828736143654784616907615912850815220735967159271240234375e-06, delta);
        
        // TODO fix for negative numbers
        checkDouble(type, -0, delta);
        checkDouble(type, -1, delta);
        checkDouble(type, -12534, delta);
        Random random = new Random();
        for (int i = 0; i < 1000; i++) {
        	checkDouble(type, random.nextDouble(), delta);
        }
    }
    
    private static void checkDouble(TypeReal type, double dValue, double delta) throws EPMCException {
        ValueDouble value = TypeDouble.get().newValue();
        value.set(dValue);
        assertEquals(dValue, value.getDouble(), delta);
    }
}
