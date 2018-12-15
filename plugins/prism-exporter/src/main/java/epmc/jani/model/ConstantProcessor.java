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
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;
import epmc.prism.exporter.processor.ProcessorRegistrar;

public class ConstantProcessor implements JANI2PRISMProcessorStrict {

    private Constant constant = null;

    @Override
    public JANI2PRISMProcessorStrict setElement(Object obj) {
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
            .append(ProcessorRegistrar.getProcessor(constant.getType())
                .toPRISM())
            .append(" ")
            .append(constant.getName());

        Expression expression = constant.getValue();
        if (expression != null) {
            prism.append(" = ")
                .append(ProcessorRegistrar.getProcessor(expression)
                        .toPRISM());
        }

        prism.append(";\n");

        return prism.toString();
    }

    @Override
    public void validateTransientVariables() {
        assert constant != null;

        ProcessorRegistrar.getProcessor(constant.getValue())
            .validateTransientVariables();
    }

    @Override
    public boolean usesTransientVariables() {
        assert constant != null;

        return ProcessorRegistrar.getProcessor(constant.getValue())
                .usesTransientVariables();
    }	
}
