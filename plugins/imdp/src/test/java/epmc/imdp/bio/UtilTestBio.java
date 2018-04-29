package epmc.imdp.bio;

import static epmc.modelchecker.TestHelper.prepareOptions;

import epmc.main.options.UtilOptionsEPMC;
import epmc.options.Options;
import epmc.plugin.OptionsPlugin;

final class UtilTestBio {
    private final static String PLUGIN_MULTIOBJECTIVE_DIR = System.getProperty("user.dir") + "/../ePMC/plugins/propertysolver-multiobjective/target/classes/";
    private final static String PLUGIN_DIR = System.getProperty("user.dir") + "/target/classes/";

    /**
     * Prepare options including loading JANI plugin.
     * 
     * @return options usable for JANI model analysis
     */
    final static Options prepareBioOptions() {
        Options options = UtilOptionsEPMC.newOptions();
        options.parse(OptionsPlugin.PLUGIN, PLUGIN_MULTIOBJECTIVE_DIR);
        options.parse(OptionsPlugin.PLUGIN, PLUGIN_DIR);
        prepareOptions(options, ModelBio.IDENTIFIER);
        return options;
    }

    private UtilTestBio() {
    }
}
