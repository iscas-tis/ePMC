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

package epmc.jani.type.smg;

import static epmc.error.UtilError.ensure;

import epmc.error.EPMCException;
import epmc.prism.exporter.error.ProblemsPRISMExporter;
import epmc.prism.exporter.processor.JANI2PRISMProcessorExtended;
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;
import epmc.prism.exporter.processor.ProcessorRegistrar;

public final class ModelExtensionSMGProcessor implements JANI2PRISMProcessorExtended {
	
	private ModelExtensionSMG smg;

	@Override
	public void setElement(Object obj) throws EPMCException {
		if (!(obj instanceof ModelExtensionSMG)) {
			ensure(false, ProblemsPRISMExporter.PRISM_EXPORTER_UNSUPPORTED_INPUT_FEATURE);
		}
		
		smg = (ModelExtensionSMG) obj;
	}

	@Override
	public StringBuilder toPRISM() throws EPMCException {
		assert smg != null;
		
		StringBuilder prism = new StringBuilder();
		JANI2PRISMProcessorStrict processor; 

		prism.append(ModelExtensionSMG.IDENTIFIER).append("\n");
		
		PlayersJANI players = smg.getPlayers();
		processor = ProcessorRegistrar.getProcessor(players);
		prism.append(processor.toPRISM().toString());
		
		return prism;
	}
}
