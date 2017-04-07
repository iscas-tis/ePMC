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

import java.util.LinkedList;
import java.util.List;

import epmc.error.EPMCException;
import epmc.prism.exporter.messages.ExtendedFeaturesPRISMExporter;
import epmc.prism.exporter.processor.JANI2PRISMProcessorExtended;
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;
import epmc.prism.exporter.processor.ProcessorRegistrar;

public class PlayersJANIProcessor implements JANI2PRISMProcessorExtended {

	private PlayersJANI players = null;
	
	@Override
	public void setElement(Object obj) throws EPMCException {
		assert obj != null;
		assert obj instanceof PlayersJANI; 
		
		players = (PlayersJANI) obj;
	}

	@Override
	public StringBuilder toPRISM() throws EPMCException {
		assert players != null;
		
		StringBuilder prism = new StringBuilder();
		JANI2PRISMProcessorStrict processor; 

		for (PlayerJANI player : players) {
			processor = ProcessorRegistrar.getProcessor(player);
			prism.append("\n").append(processor.toPRISM().toString());			
		}
		
		return prism;
	}
	
	
	@Override
	public List<String> getUnsupportedFeature() {
		assert players != null;
		
		List<String> ll = new LinkedList<>();
		ll.add(ExtendedFeaturesPRISMExporter.PRISM_EXPORTER_EXTENDED_FEATURE_PLAYER_DEFINITION);
		return ll;
	}
	
	@Override
	public void validateTransientVariables() throws EPMCException {
		assert players != null;
		
		for (PlayerJANI player : players) {
			ProcessorRegistrar.getProcessor(player).validateTransientVariables();
		}
	}

	@Override
	public boolean usesTransientVariables() throws EPMCException {
		assert players != null;
		
		boolean usesTransient = false;
		
		for (PlayerJANI player : players) {
			usesTransient |= ProcessorRegistrar.getProcessor(player).usesTransientVariables();
		}
		
		return usesTransient;
	}	
}
