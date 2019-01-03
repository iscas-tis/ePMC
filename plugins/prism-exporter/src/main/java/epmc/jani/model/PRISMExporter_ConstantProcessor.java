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

import epmc.expression.Expression;
import epmc.prism.exporter.JANIComponentRegistrar;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorStrict;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorRegistrar;

public class PRISMExporter_ConstantProcessor implements PRISMExporter_ProcessorStrict {

    private Constant constant = null;

    @Override
    public PRISMExporter_ProcessorStrict setElement(Object obj) {
        assert obj != null;
        assert obj instanceof Constant; 

        constant = (Constant) obj;
        JANIComponentRegistrar.registerIdentifier(constant);
        return this;
    }

    @Override
    public String toPRISM() {
        assert constant != null;

        StringBuilder prism = new StringBuilder();

        String comment = constant.getComment();
        if (comment != null) {
            prism.append("// ")
                .append(comment)
                .append("\n");
        }

        prism.append("const ")
            .append(PRISMExporter_ProcessorRegistrar.getProcessor(constant.getType())
                .toPRISM())
            .append(" ")
            .append(constant.getName());

        Expression expression = constant.getValue();
        if (expression != null) {
            prism.append(" = ")
                .append(PRISMExporter_ProcessorRegistrar.getProcessor(expression)
                        .toPRISM());
        }

        prism.append(";\n");

        return prism.toString();
    }

    @Override
    public void validateTransientVariables() {
        assert constant != null;

        Expression value = constant.getValue();
        if (value != null) {
            PRISMExporter_ProcessorRegistrar.getProcessor(value)
                .validateTransientVariables();
        }
    }

    @Override
    public boolean usesTransientVariables() {
        assert constant != null;

        Expression value = constant.getValue();
        if (value != null) {
            return PRISMExporter_ProcessorRegistrar.getProcessor(value)
                .usesTransientVariables();
        } else {
            return false;
        }
    }	
}
