package epmc.imdp;

import static epmc.modelchecker.TestHelper.prepareOptions;

import epmc.error.EPMCException;
import epmc.imdp.model.ModelIMDP;
import epmc.main.options.UtilOptionsEPMC;
import epmc.options.Options;
import epmc.plugin.OptionsPlugin;

final class UtilTestIMDP {
	/** Location of plugin directory in file system. */
    private final static String PLUGIN_DIR = System.getProperty("user.dir") + "/target/classes/";
    private final static String PLUGIN_MULTIOBJECTIVE_DIR = System.getProperty("user.dir") + "/../iscasmc-propertysolver-multiobjective/target/classes/";

    /**
     * Prepare options including loading JANI plugin.
     * 
     * @return options usable for JANI model analysis
     * @throws EPMCException thrown in case problem occurs
     */
	final static Options prepareIMDPOptions() throws EPMCException {
        Options options = UtilOptionsEPMC.newOptions();
        options.parse(OptionsPlugin.PLUGIN, PLUGIN_MULTIOBJECTIVE_DIR);
        options.parse(OptionsPlugin.PLUGIN, PLUGIN_DIR);
        prepareOptions(options, ModelIMDP.IDENTIFIER);
        return options;
    }

	private UtilTestIMDP() {
	}
}
