package epmc.util;

import org.junit.Test;

import epmc.error.EPMCException;
import epmc.main.options.UtilOptionsEPMC;
import epmc.options.Options;
import epmc.plugin.UtilPlugin;

public class OptionsTest {

    @Test
    public void testOptions() throws EPMCException {
        Options options;

        // had some crashes when no arguments were present
        String[] noArgs = {};
        options = UtilOptionsEPMC.newOptions();
        options.parseOptions(noArgs, true);
        options.reset();
        UtilPlugin.loadPlugins(options);
        options.parseOptions(noArgs, false);

        String[] helpArgs = {"help"};
        
        options = UtilOptionsEPMC.newOptions();
        options.parseOptions(helpArgs, true);
        options.reset();
        UtilPlugin.loadPlugins(options);
        options.parseOptions(helpArgs, false);

        options.getShortUsage();
        // check whether we forgot to specify resource strings
//        options.getUsage();
    }

}
