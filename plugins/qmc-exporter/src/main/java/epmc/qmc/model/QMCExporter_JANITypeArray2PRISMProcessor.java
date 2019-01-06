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

package epmc.qmc.model;

import java.util.LinkedList;
import java.util.List;

import epmc.prism.exporter.processor.PRISMExporter_ProcessorNonPRISM;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorRegistrar;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorStrict;
import epmc.qmc.exporter.messages.NonPRISMFeaturesQMCExporter;

public final class QMCExporter_JANITypeArray2PRISMProcessor implements PRISMExporter_ProcessorNonPRISM {

    private JANITypeArray array = null;

    @Override
    public PRISMExporter_ProcessorStrict setElement(Object obj) {
        assert obj instanceof JANITypeArray;

        array = (JANITypeArray) obj;
        return this;
    }

    @Override
    public String toPRISM() {
        assert array != null;
        
        StringBuilder prism = new StringBuilder();
        prism.append("[")
            .append(PRISMExporter_ProcessorRegistrar.getProcessor(array)
                    .toPRISM())
            .append("..")
            .append(PRISMExporter_ProcessorRegistrar.getProcessor(array)
                    .toPRISM())
            .append("]");
        
        return prism.toString();
    }

    @Override
    public void validateTransientVariables() {
        assert array != null;
    }

    @Override
    public boolean usesTransientVariables() {
        assert array != null;

        return false;
    }	

    @Override
    public List<String> getUnsupportedFeature() {
        List<String> ll = new LinkedList<>();
        ll.add(NonPRISMFeaturesQMCExporter.QMC_EXPORTER_NONPRISM_FEATURE_TYPE_ARRAY);
        return ll;
    }
}
