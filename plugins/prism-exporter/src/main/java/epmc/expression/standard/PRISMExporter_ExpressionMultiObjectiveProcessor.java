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

package epmc.expression.standard;

import epmc.expression.Expression;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorStrict;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorRegistrar;

public class PRISMExporter_ExpressionMultiObjectiveProcessor implements PRISMExporter_ProcessorStrict {

    private ExpressionMultiObjective multiObjective = null;

    @Override
    public PRISMExporter_ProcessorStrict setElement(Object obj) {
        assert obj != null;
        assert obj instanceof ExpressionMultiObjective; 

        multiObjective = (ExpressionMultiObjective) obj;
        return this;
    }

    @Override
    public String toPRISM() {
        assert multiObjective != null;

        StringBuilder prism = new StringBuilder();

        prism.append("multi(");

        boolean notFirst = false;
        for (Expression operand : multiObjective.getOperands()) {
            if (notFirst) {
                prism.append(", ");
            } else {
                notFirst = true;
            }
            prism.append(PRISMExporter_ProcessorRegistrar.getProcessor(operand)
                    .toPRISM());
        }
        prism.append(")");

        return prism.toString();
    }
    @Override
    public void validateTransientVariables() {
        assert multiObjective != null;

        for (Expression child : multiObjective.getOperands()) {
            PRISMExporter_ProcessorRegistrar.getProcessor(child)
                .validateTransientVariables();
        }
    }

    @Override
    public boolean usesTransientVariables() {
        assert multiObjective != null;

        boolean usesTransient = false;
        for (Expression child : multiObjective.getChildren()) {
            usesTransient |= PRISMExporter_ProcessorRegistrar.getProcessor(child)
                    .usesTransientVariables();
        }

        return usesTransient;
    }	
}
