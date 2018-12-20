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

package epmc.jani.model;

import java.util.Map.Entry;

import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;

public class PRISMExporter_MetadataProcessor implements JANI2PRISMProcessorStrict {

    private Metadata metadata = null;

    @Override
    public JANI2PRISMProcessorStrict setElement(Object obj) {
        assert obj != null;
        assert obj instanceof Metadata; 

        metadata = (Metadata) obj;
        return this;
    }

    @Override
    public String toPRISM() {
        assert metadata != null;

        StringBuilder prism = new StringBuilder();

        for (Entry<String, String> entry : metadata.getValues().entrySet()) {
            prism.append("// ")
                .append(entry.getKey())
                .append(": ")
                .append(entry.getValue())
                .append("\n");
        }

        return prism.toString();
    }

    @Override
    public void validateTransientVariables() {
        assert metadata != null;
    }

    @Override
    public boolean usesTransientVariables() {
        assert metadata != null;

        return false;
    }	
}
