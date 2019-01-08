package epmc.qmc.dd;

import static epmc.modelchecker.TestHelper.*;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.OptionsDD;
import epmc.dd.VariableDD;
import epmc.main.options.UtilOptionsEPMC;
import epmc.options.Options;
import epmc.plugin.OptionsPlugin;
import epmc.qmc.model.ModelPRISMQMC;
import epmc.qmc.operator.OperatorArray;
import epmc.qmc.value.OperatorEvaluatorArray;
import epmc.value.ContextValue;

public final class QMCDDTest {
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
    public void ddTest() {
        Options options = prepareQMCOptions();
        prepareOptions(options, ModelPRISMQMC.IDENTIFIER);
        options.set(OptionsDD.DD_DEBUG, true);
        options.set(OptionsDD.DD_LEAK_CHECK, true);
        ContextValue.set(new ContextValue());
//        ContextValue.get().add(OperatorEvaluatorArray.INSTANCE);
        ContextDD contextDD = new ContextDD();
        VariableDD var = contextDD.newInteger("var", 1, 0, 2);
        DD dimensions = contextDD.newConstant(2);
        DD numRows = contextDD.newConstant(2);
        DD numCols = contextDD.newConstant(2);

        DD entry1 = var.newVariableValue(0, 0).iteWith(contextDD.newConstant(1), contextDD.newConstant(-1));
        DD entry2 = var.newVariableValue(0, 0).iteWith(contextDD.newConstant(2), contextDD.newConstant(-2));
        DD entry3 = contextDD.newConstant(3);
        DD entry4 = contextDD.newConstant(4);

        DD result = contextDD.applyWith(OperatorArray.ARRAY,
                dimensions, numRows, numCols, entry1, entry2, entry3, entry4);

        System.out.println(result);
        var.close();
        result.dispose();
        contextDD.close();
    }    
}
