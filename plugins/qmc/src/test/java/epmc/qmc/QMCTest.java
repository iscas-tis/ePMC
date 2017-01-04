package epmc.qmc;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.error.EPMCException;
import epmc.main.options.UtilOptionsEPMC;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.plugin.OptionsPlugin;
import epmc.qmc.model.ModelPRISMQMC;
import epmc.qmc.model.PropertyPRISMQMC;
import epmc.value.ContextValue;
import epmc.value.TypeArrayAlgebra;
import epmc.value.TypeReal;
import epmc.value.Value;
import epmc.value.ValueArrayAlgebra;

import static epmc.modelchecker.TestHelper.*;
import static epmc.qmc.ModelNames.*;

public final class QMCTest {
    private final static String PLUGIN_DIR = System.getProperty("user.dir") + "/target/classes/";

    @BeforeClass
    public static void initialise() {
        prepare();
    }

    private final static Options prepareQMCOptions() throws EPMCException {
        Options options = UtilOptionsEPMC.newOptions();
        options.set(OptionsPlugin.PLUGIN, PLUGIN_DIR);
        prepareOptions(options, ModelPRISMQMC.IDENTIFIER);
        return options;
    }
    
    // TODO make this work again
    @Test
    public void keyDistributionTest() throws EPMCException {
        Options options = prepareQMCOptions();
        String tolerance = "1E-10";
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISMQMC.IDENTIFIER);
        options.set(OptionsModelChecker.PROPERTY_INPUT_TYPE, PropertyPRISMQMC.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, tolerance);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Value result1 = computeResult(options, KEY_DISTRIBUTION, "Q=0 [F (fail)]");
        assertEquals(true, result1);
        Value result2 = computeResult(options, KEY_DISTRIBUTION, "Q>=1 [F(succ | abort)]");
        assertEquals(true, result2);
        Value result3 = computeResult(options, KEY_DISTRIBUTION, "Q=0.5 [F (succ)] & Q=0.5 [F (abort)]");
        assertEquals(true, result3);
    }
    
    @Test
    public void loopTest() throws EPMCException {
        Options options = prepareQMCOptions();
        ContextValue contextValue = options.get(TestHelper.CONTEXT_VALUE);
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISMQMC.IDENTIFIER);
        options.set(OptionsModelChecker.PROPERTY_INPUT_TYPE, PropertyPRISMQMC.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Value result1 = computeResult(options, LOOP, "Q>=1 [ F (s=3) ]");
        TypeArrayAlgebra typeArray = TypeReal.get(contextValue).getTypeArray();
        ValueArrayAlgebra compare23 = typeArray.newValue();
        compare23.setDimensions(2, 2);
        compare23.set(1, 0, 0);
        assertEquals(true, result1);
        Value result2 = computeResult(options, LOOP, "qeval(Q=?[F (s=3)], |p>_2 <p|_2)");
        assertEquals(compare23, result2, tolerance);
        Value result3 = computeResult(options, LOOP, "qeval(Q=?[F (s=3)], ID(2)/2)");
        assertEquals(compare23, result3, tolerance);
    }
    
    @Test
    public void superdenseCodingTest() throws EPMCException {
        Options options = prepareQMCOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISMQMC.IDENTIFIER);
        options.set(OptionsModelChecker.PROPERTY_INPUT_TYPE, PropertyPRISMQMC.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Value result1 = computeResult(options, SUPERDENSE_CODING, "Q>=1 [F (succ)]");
        assertEquals(true, result1);
    }
}
