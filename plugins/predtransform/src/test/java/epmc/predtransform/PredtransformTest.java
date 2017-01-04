package epmc.predtransform;

import org.junit.BeforeClass;
import org.junit.Test;

import epmc.error.EPMCException;
import epmc.options.OptionsEPMC;
import epmc.options.Options;
import epmc.options.OptionsTypes;
import epmc.options.UtilOptions;
import epmc.predtransform.command.CommandTaskPredtransform;
import epmc.prism.ModelPRISM;
import static epmc.modelchecker.TestHelper.*;
import static epmc.ModelNames.*;

public final class PredtransformTest {
    private final static String pluginDir = System.getProperty("user.dir") + "/target/classes/";

    @BeforeClass
    public static void initialise() {
        prepare();
    }

    private final static Options preparePredtransformOptions() throws EPMCException {
        Options options = UtilOptions.newOptions(OptionsEPMC.PROGRAM_OPTIONS);
        options.set(OptionsEPMC.PLUGIN, pluginDir);
        prepareOptions(options, ModelPRISM.IDENTIFIER);
        return options;
    }
    
    @Test
    public void test() throws EPMCException {
        Options options = preparePredtransformOptions();
        double tolerance = 1E-11;
        options.set(OptionsEPMC.INPUT_TYPE, ModelPRISM.IDENTIFIER);
        options.set(OptionsEPMC.ITERATION_TOLERANCE, Double.toString(tolerance));
        options.set(OptionsEPMC.ENGINE, OptionsTypes.Engine.EXPLICIT);
        options.set(Options.COMMAND, CommandTaskPredtransform.IDENTIFIER);
        compute(options, TWO_DICE, "Pmin=? [ F s1=7 & s2=7 & d1+d2=2 ]");
    }
}
