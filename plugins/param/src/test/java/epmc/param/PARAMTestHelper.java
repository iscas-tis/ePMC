package epmc.param;

import static epmc.modelchecker.TestHelper.prepareOptions;

import epmc.error.EPMCException;
import epmc.main.options.UtilOptionsEPMC;
import epmc.options.Options;
import epmc.plugin.OptionsPlugin;

public final class PARAMTestHelper {
    private final static String PLUGIN_DIR = System.getProperty("user.dir") + "/target/classes/";

    public final static Options preparePARAMOptions() throws EPMCException {
        Options options = UtilOptionsEPMC.newOptions();
        options.set(OptionsPlugin.PLUGIN, PLUGIN_DIR);
        prepareOptions(options);
        return options;
    }
}
