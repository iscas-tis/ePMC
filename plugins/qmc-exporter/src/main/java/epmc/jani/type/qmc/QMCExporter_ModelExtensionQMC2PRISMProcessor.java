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

package epmc.jani.type.qmc;

import java.util.LinkedList;
import java.util.List;

import epmc.prism.exporter.processor.PRISMExporter_ProcessorNonPRISM;
import epmc.qmc.exporter.messages.NonPRISMFeaturesQMCExporter;

public final class QMCExporter_ModelExtensionQMC2PRISMProcessor implements PRISMExporter_ProcessorNonPRISM {

    @Override
    public PRISMExporter_ProcessorNonPRISM setElement(Object obj) {
        assert obj instanceof ModelExtensionQMC;
        return this;
    }

    @Override
    public String toPRISM() {
        return "qmc\n";
    }

    @Override
    public void validateTransientVariables() {
    }

    @Override
    public boolean usesTransientVariables() {
        return false;
    }	

    @Override
    public List<String> getUnsupportedFeature() {
        List<String> ll = new LinkedList<>();
        ll.add(NonPRISMFeaturesQMCExporter.QMC_EXPORTER_NONPRISM_FEATURE_SEMANTIC_TYPE_QMC);
        return ll;
    }
}
