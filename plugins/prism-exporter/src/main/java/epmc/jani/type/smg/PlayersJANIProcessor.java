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

import epmc.prism.exporter.messages.ExtendedFeaturesPRISMExporter;
import epmc.prism.exporter.processor.JANI2PRISMProcessorExtended;
import epmc.prism.exporter.processor.ProcessorRegistrar;

public class PlayersJANIProcessor implements JANI2PRISMProcessorExtended {

	private PlayersJANI players = null;
	
	@Override
	public JANI2PRISMProcessorExtended setElement(Object obj) {
		assert obj != null;
		assert obj instanceof PlayersJANI; 
		
		players = (PlayersJANI) obj;
		return this;
	}

	@Override
	public String toPRISM() {
		assert players != null;
		
		StringBuilder prism = new StringBuilder();

		for (PlayerJANI player : players) {
			prism.append("\n")
				 .append(ProcessorRegistrar.getProcessor(player).toPRISM());			
		}
		
		return prism.toString();
	}
	
	
	@Override
	public List<String> getUnsupportedFeature() {
		assert players != null;
		
		List<String> ll = new LinkedList<>();
		ll.add(ExtendedFeaturesPRISMExporter.PRISM_EXPORTER_EXTENDED_FEATURE_PLAYER_DEFINITION);
		return ll;
	}
	
	@Override
	public void validateTransientVariables() {
		assert players != null;
		
		for (PlayerJANI player : players) {
			ProcessorRegistrar.getProcessor(player).validateTransientVariables();
		}
	}

	@Override
	public boolean usesTransientVariables() {
		assert players != null;
		
		boolean usesTransient = false;
		
		for (PlayerJANI player : players) {
			usesTransient |= ProcessorRegistrar.getProcessor(player).usesTransientVariables();
		}
		
		return usesTransient;
	}	
}
