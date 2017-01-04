package epmc.jani.exporter.plugin;

import epmc.error.EPMCException;
import epmc.jani.exporter.command.CommandTaskJANIExporterJANIExport;
import epmc.options.Options;
import epmc.plugin.AfterServerStart;

public final class AfterServerStartJANIExporter implements AfterServerStart {
	public final static String IDENTIFIER = "after-server-start-jani-exporter";
	
	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
        String commandName = options.getString(Options.COMMAND);
        if (commandName.equals(CommandTaskJANIExporterJANIExport.IDENTIFIER)) {
//        	Log log = options.get(OptionsMessages.LOG);
//        	log.setSilent(true);
        }
	}

}
