package epmc.rddl;

import static epmc.modelchecker.TestHelper.prepareOptions;

import epmc.error.EPMCException;
import epmc.main.options.UtilOptionsEPMC;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.plugin.OptionsPlugin;
import epmc.rddl.model.ModelRDDL;

final class RDDLTestHelper {
    private final static String pluginDir = System.getProperty("user.dir") + "/target/classes/";

    final static Options prepareRDDLOptions() throws EPMCException {
        Options options = UtilOptionsEPMC.newOptions();
        options.set(OptionsPlugin.PLUGIN, pluginDir);
        prepareOptions(options, ModelRDDL.IDENTIFIER);
        options.set(OptionsModelChecker.MODEL_INPUT_TYPE, ModelRDDL.IDENTIFIER);
        return options;
    }

    private RDDLTestHelper() {
    }
}
