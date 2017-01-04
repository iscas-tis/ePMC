package epmc.qmc.value;

import static epmc.modelchecker.TestHelper.prepareOptions;

import java.util.Arrays;

import org.junit.Test;

import epmc.error.EPMCException;
import epmc.main.options.UtilOptionsEPMC;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.TestHelper;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.plugin.OptionsPlugin;
import epmc.qmc.model.ModelPRISMQMC;
import epmc.qmc.value.ContextValueQMC;
import epmc.qmc.value.Eigen;
import epmc.qmc.value.TypeComplex;
import epmc.value.ContextValue;
import epmc.value.TypeArrayAlgebra;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;

public class EigenTest {
    private final static String pluginDir = System.getProperty("user.dir") + "/plugins/qmc/target/classes/";

    private final static Options prepareQMCOptions() throws EPMCException {
        Options options = UtilOptionsEPMC.newOptions();
        options.set(OptionsPlugin.PLUGIN, pluginDir);
        prepareOptions(options, ModelPRISMQMC.IDENTIFIER);
        return options;
    }
    
    @Test
    public void eigenTest() throws EPMCException {
        Options options = prepareQMCOptions();
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelPRISMQMC.IDENTIFIER);
        options.set(OptionsModelChecker.ENGINE, EngineExplicit.class);
        ContextValue contextValue = options.get(TestHelper.CONTEXT_VALUE);
        ContextValueQMC contextValueQMC = new ContextValueQMC(contextValue);
        TypeComplex typeComplex = contextValueQMC.getTypeComplex();
        TypeArrayAlgebra typeArray = typeComplex.getTypeArray();
        ValueArrayAlgebra matrix = typeArray.newValue();
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
        matrix.set(0, 0, 0);
        matrix.set(2, 0, 1);
        matrix.set(2, 1, 0);
        matrix.set(1, 1, 1);
        result = Eigen.eigenvalues(matrix);
        System.out.println(Arrays.toString(result));
// [-1.5615528, 2.5615528]

//        assertEquals(true, result1);
    }
    
    private static void set(ValueArray valueArray, String entry, int... pos) throws EPMCException {
    	Value valueEntry = valueArray.getType().getEntryType().newValue();
    	valueEntry.set(entry);
    	valueArray.set(valueEntry, pos);
    }
    


}
