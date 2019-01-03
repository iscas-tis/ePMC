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

public class PRISMExporter_ExpressionFilterProcessor implements PRISMExporter_ProcessorStrict {

    private ExpressionFilter filter = null;

    @Override
    public PRISMExporter_ProcessorStrict setElement(Object obj) {
        assert obj != null;
        assert obj instanceof ExpressionFilter; 

        filter = (ExpressionFilter) obj;
        return this;
    }

    @Override
    public String toPRISM() {
        assert filter != null;

        StringBuilder prism = new StringBuilder();

        prism.append("filter(")
            .append(filter.getFilterType().toString())
            .append(", ")
            .append(PRISMExporter_ProcessorRegistrar.getProcessor(filter.getProp())
                    .toPRISM());

        Expression states = filter.getStates();
        if (states != null) {
            prism.append(", ")
                .append(PRISMExporter_ProcessorRegistrar.getProcessor(states)
                        .toPRISM());
        }
        prism.append(")");

        return prism.toString();
    }
    @Override
    public void validateTransientVariables() {
        assert filter != null;

        for (Expression child : filter.getChildren()) {
            PRISMExporter_ProcessorRegistrar.getProcessor(child)
                .validateTransientVariables();
        }
    }

    @Override
    public boolean usesTransientVariables() {
        assert filter != null;

        boolean usesTransient = false;
        for (Expression child : filter.getChildren()) {
            usesTransient |= PRISMExporter_ProcessorRegistrar.getProcessor(child)
                    .usesTransientVariables();
        }

        return usesTransient;
    }	
}
