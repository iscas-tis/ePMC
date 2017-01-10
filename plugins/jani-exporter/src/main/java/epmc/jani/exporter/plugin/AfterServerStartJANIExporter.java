/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

*****************************************************************************/

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
