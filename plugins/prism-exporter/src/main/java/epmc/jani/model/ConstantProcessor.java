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

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.jani.model.type.JANIType;
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;
import epmc.prism.exporter.processor.JANIComponentRegistrar;
import epmc.prism.exporter.processor.ProcessorRegistrar;

public class ConstantProcessor implements JANI2PRISMProcessorStrict {

	private Constant constant = null;
	
	@Override
	public void setElement(Object obj) throws EPMCException {
		assert obj != null;
		assert obj instanceof Constant; 

		constant = (Constant) obj;
		JANIComponentRegistrar.registerConstant(constant);
	}

	@Override
	public StringBuilder toPRISM() throws EPMCException {
		assert constant != null;

		StringBuilder prism = new StringBuilder();
		JANI2PRISMProcessorStrict processor; 
		
		String comment = constant.getComment();
		if (comment != null) {
			prism.append("// ").append(comment).append("\n");
		}

		prism.append("const ");
		
		JANIType type = constant.getType();
		processor = ProcessorRegistrar.getProcessor(type);
		prism.append(processor.toPRISM().toString()).append(" ").append(constant.getName());
		
		Expression expression = constant.getValue();
		if (expression != null) {
			prism.append(" = ").append(expression.toString());
		}
		
		prism.append(";\n");
		
		return prism;
	}
}
