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

package epmc.jani.model.property;

import epmc.prism.exporter.processor.PRISMExporter_ProcessorStrict;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorRegistrar;

public class PRISMExporter_JANIPropertyEntryProcessor implements PRISMExporter_ProcessorStrict {

    private JANIPropertyEntry property = null;

    @Override
    public PRISMExporter_ProcessorStrict setElement(Object obj) {
        assert obj != null;
        assert obj instanceof JANIPropertyEntry; 

        property = (JANIPropertyEntry) obj;
        return this;
    }

    @Override
    public String toPRISM() {
        assert property != null;

        StringBuilder prism = new StringBuilder();

        String comment = property.getComment();
        if (comment != null) {
            prism.append("// ")
                .append(comment)
                .append("\n");
        }

        prism.append(PRISMExporter_ProcessorRegistrar.getProcessor(property.getExpression())
                .toPRISM())
            .append("\n");

        return prism.toString();
    }

    @Override
    public void validateTransientVariables() {
        assert property != null;

        PRISMExporter_ProcessorRegistrar.getProcessor(property.getExpression())
            .validateTransientVariables();
    }

    @Override
    public boolean usesTransientVariables() {
        assert property != null;

        return PRISMExporter_ProcessorRegistrar.getProcessor(property.getExpression())
                .usesTransientVariables();
    }	
}
