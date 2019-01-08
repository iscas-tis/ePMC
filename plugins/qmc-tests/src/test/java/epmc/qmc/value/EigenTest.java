package epmc.qmc.value;

import static epmc.modelchecker.TestHelper.prepare;
import static epmc.modelchecker.TestHelper.prepareOptions;

import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.main.options.UtilOptionsEPMC;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.operator.OperatorSet;
import epmc.options.Options;
import epmc.plugin.OptionsPlugin;
import epmc.qmc.model.ModelPRISMQMC;
import epmc.qmc.value.Eigen;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeInteger;
import epmc.value.Value;
import epmc.value.ValueInteger;
import epmc.value.ValueSetString;

public class EigenTest {
    private final static String PLUGIN_DIR = System.getProperty("user.dir") + "/target/classes/";

    @BeforeClass
    public static void initialise() {
        prepare();
    }

    private final static Options prepareQMCOptions() {
        Options options = UtilOptionsEPMC.newOptions();
        options.set(OptionsPlugin.PLUGIN, PLUGIN_DIR);
        prepareOptions(options, ModelPRISMQMC.IDENTIFIER);
        return options;
    }
    
    @Test
    public void eigenTest() {
        Options options = prepareQMCOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISMQMC.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        TypeMatrix typeArray = TypeMatrix.get(TypeComplex.get());
        ValueMatrix matrix = typeArray.newValue();
        matrix.setDimensions(3, 3);
        set(matrix, "2", 0, 0);
        set(matrix, "2+i", 0, 1);
        set(matrix, "4", 0, 2);
        set(matrix, "2-i", 1, 0);
        set(matrix, "3", 1, 1);
        set(matrix, "i", 1, 2);
        set(matrix, "4", 2, 0);
        set(matrix, "-i", 2, 1);
        set(matrix, "1", 2, 2);
        System.out.println(matrix);
        Value[] result = Eigen.eigenvalues(matrix);
        System.out.println(Arrays.toString(result));
        //        [6.3120458, 2.8530793, -3.1651251]

        matrix = typeArray.newValue();
        matrix.setDimensions(2,2);
        set(matrix, 0, 0, 0);
        set(matrix, 2, 0, 1);
        set(matrix, 2, 1, 0);
        set(matrix, 1, 1, 1);
        result = Eigen.eigenvalues(matrix);
        System.out.println(Arrays.toString(result));
        // [-1.5615528, 2.5615528]

        //        assertEquals(true, result1);
    }

    private static void set(ValueMatrix valueArray, String entry, int row, int col) {
        Value valueEntry = valueArray.getType().getEntryType().newValue();
        ValueSetString.as(valueEntry).set(entry);
        valueArray.set(valueEntry, row, col);
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
