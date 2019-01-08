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

public class PRISMExporter_ExpressionTemporalGloballyProcessor implements PRISMExporter_ProcessorStrict {
    private ExpressionTemporalGlobally temporal = null;

    @Override
    public PRISMExporter_ProcessorStrict setElement(Object obj) {
        assert obj != null;
        assert obj instanceof ExpressionTemporalGlobally; 

        temporal = (ExpressionTemporalGlobally) obj;
        return this;
    }

    @Override
    public String toPRISM() {
        assert temporal != null;

        StringBuilder prism = new StringBuilder();

        prism.append("G")
            .append(PRISMExporter_ProcessorRegistrar.getProcessor(temporal.getTimeBound())
                    .toPRISM())
            .append("(")
            .append(PRISMExporter_ProcessorRegistrar.getProcessor(temporal.getOperand())
                    .toPRISM())
            .append(")");

        return prism.toString();
    }

    @Override
    public void validateTransientVariables() {
        assert temporal != null;

        for (Expression child : temporal.getChildren()) {
            PRISMExporter_ProcessorRegistrar.getProcessor(child)
                .validateTransientVariables();
        }
    }

    @Override
    public boolean usesTransientVariables() {
        assert temporal != null;

        boolean usesTransient = false;
        for (Expression child : temporal.getChildren()) {
            usesTransient |= PRISMExporter_ProcessorRegistrar.getProcessor(child)
                    .usesTransientVariables();
        }

        return usesTransient;
    }	
}
