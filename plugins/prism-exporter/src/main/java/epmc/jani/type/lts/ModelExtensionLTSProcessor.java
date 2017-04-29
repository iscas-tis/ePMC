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

package epmc.jani.type.lts;

import java.util.LinkedList;
import java.util.List;

import epmc.error.EPMCException;
import epmc.prism.exporter.messages.ExtendedFeaturesPRISMExporter;
import epmc.prism.exporter.processor.JANI2PRISMProcessorNonPRISM;

public final class ModelExtensionLTSProcessor implements JANI2PRISMProcessorNonPRISM {

	@Override
	public JANI2PRISMProcessorNonPRISM setElement(Object obj) throws EPMCException {
		assert obj instanceof ModelExtensionLTS;
		return this;
	}

	@Override
	public String toPRISM() {
		return "lts\n";
	}
	
	@Override
	public List<String> getUnsupportedFeature() {
		List<String> ll = new LinkedList<>();
		ll.add(ExtendedFeaturesPRISMExporter.PRISM_EXPORTER_EXTENDED_FEATURE_SEMANTIC_TYPE_LTS);
		return ll;
	}
	
	@Override
	public void validateTransientVariables() throws EPMCException {
	}

	@Override
	public boolean usesTransientVariables() throws EPMCException {
		return false;
	}	
}
