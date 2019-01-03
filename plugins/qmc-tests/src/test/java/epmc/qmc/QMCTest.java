package epmc.qmc;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.main.options.UtilOptionsEPMC;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.operator.OperatorSet;
import epmc.options.Options;
import epmc.plugin.OptionsPlugin;
import epmc.qmc.model.ModelPRISMQMC;
import epmc.qmc.model.PropertyPRISMQMC;
import epmc.qmc.value.TypeMatrix;
import epmc.qmc.value.ValueMatrix;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.Value;
import epmc.value.ValueInteger;

import static epmc.modelchecker.TestHelper.*;
import static epmc.qmc.ModelNames.*;

import java.util.ArrayList;
import java.util.List;

// TODO check why the tests have such a long set up time
public final class QMCTest {

    @BeforeClass
    public static void initialise() {
        prepare();
    }

    private final static Options prepareQMCOptions() {
        List<String> qmcPlugins = new ArrayList<>();
        qmcPlugins.add("epmc/qmc/target/classes/");
        qmcPlugins.add("epmc/qmc-exporter/target/classes/");
        qmcPlugins.add("epmc/qmc-tests/target/classes/");
        
        Options options = UtilOptionsEPMC.newOptions();
        prepareOptions(options, ModelPRISMQMC.IDENTIFIER);
        options.set(OptionsPlugin.PLUGIN, qmcPlugins);
        return options;
    }

    @Test
    public void keyDistributionTest() {
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
    public void loopTest() {
        Options options = prepareQMCOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISMQMC.IDENTIFIER);
        options.set(OptionsModelChecker.PROPERTY_INPUT_TYPE, PropertyPRISMQMC.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Value result1 = computeResult(options, LOOP, "Q>=1 [ F (s=3) ]");
        TypeMatrix typeArray = TypeMatrix.get(TypeReal.get());
        ValueMatrix compare23 = typeArray.newValue();
        compare23.setDimensions(2, 2);
        set(compare23, 1, 0, 0);
        assertEquals(true, result1);
        Value result2 = computeResult(options, LOOP, "qeval(Q=?[F (s=3)], |p>_2 <p|_2)");
        assertEquals(compare23, result2, tolerance);
        Value result3 = computeResult(options, LOOP, "qeval(Q=?[F (s=3)], ID(2)/2)");
        assertEquals(compare23, result3, tolerance);
    }

    /**
     * Alternative version of loop program.
     * We use values from the original model in the theory paper at
     * <a href="https://arxiv.org/pdf/1205.2187.pdf">https://arxiv.org/pdf/1205.2187.pdf</a>
     * rather than the values from the tool paper.
     * Model checking results must be the same.
     */
    @Test
    public void loopAlternativeTest() {
        Options options = prepareQMCOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISMQMC.IDENTIFIER);
        options.set(OptionsModelChecker.PROPERTY_INPUT_TYPE, PropertyPRISMQMC.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Value result1 = computeResult(options, LOOP_ALTERNATIVE, "Q>=1 [ F (s=3) ]");
        TypeMatrix typeArray = TypeMatrix.get(TypeReal.get());
        ValueMatrix compare23 = typeArray.newValue();
        compare23.setDimensions(2, 2);
        set(compare23, 1, 0, 0);
        assertEquals(true, result1);
        Value result2 = computeResult(options, LOOP_ALTERNATIVE, "qeval(Q=?[F (s=3)], |p>_2 <p|_2)");
        assertEquals(compare23, result2, tolerance);
        Value result3 = computeResult(options, LOOP_ALTERNATIVE, "qeval(Q=?[F (s=3)], ID(2)/2)");
        assertEquals(compare23, result3, tolerance);
    }

    @Test
    public void superdenseCodingTest() {
        Options options = prepareQMCOptions();
        double tolerance = 1E-10;
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISMQMC.IDENTIFIER);
        options.set(OptionsModelChecker.PROPERTY_INPUT_TYPE, PropertyPRISMQMC.IDENTIFIER);
        options.set(TestHelper.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        Value result1 = computeResult(options, SUPERDENSE_CODING, "Q>=1 [F (succ)]");
        assertEquals(true, result1);
    }

    private static void set(ValueMatrix valueArray, int entry, int row, int col) {
        Value valueEntry = valueArray.getType().getEntryType().newValue();
        ValueInteger valueInt = TypeInteger.get().newValue();
        valueInt.set(entry);
        OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeInteger.get(), valueArray.getType().getEntryType());
        set.apply(valueEntry, valueInt);
        valueArray.set(valueEntry, row, col);
    }
}
