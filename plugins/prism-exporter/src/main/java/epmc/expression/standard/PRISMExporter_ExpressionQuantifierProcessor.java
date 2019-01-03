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
import epmc.prism.exporter.JANIComponentRegistrar;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorStrict;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorRegistrar;

public class PRISMExporter_ExpressionQuantifierProcessor implements PRISMExporter_ProcessorStrict {

    private ExpressionQuantifier quantifier = null;

    @Override
    public PRISMExporter_ProcessorStrict setElement(Object obj) {
        assert obj != null;
        assert obj instanceof ExpressionQuantifier; 

        quantifier = (ExpressionQuantifier) obj;
        return this;
    }

    @Override
    public String toPRISM() {
        assert quantifier != null;

        StringBuilder prism = new StringBuilder();

        Expression quantified = quantifier.getQuantified();
        if (quantified instanceof ExpressionSteadyState) {
            prism.append("S");
        } else if (quantified instanceof ExpressionReward) {
            prism.append("R")
                .append("{")
                .append(PRISMExporter_ProcessorRegistrar.getProcessor(((ExpressionReward) quantified).getReward()
                        .getExpression())
                        .toPRISM())
                .append("}");
        } else {
            prism.append("P");
        }

        if (JANIComponentRegistrar.isNonDeterministicModel()) {
            prism.append(quantifier.getDirType().toString());
        }

        CmpType cmpType = quantifier.getCompareType();
        prism.append(cmpType.toString());
        if (cmpType != CmpType.IS) {
            prism.append(PRISMExporter_ProcessorRegistrar.getProcessor(quantifier.getCompare())
                    .toPRISM());
        }

        prism.append("[")
            .append(PRISMExporter_ProcessorRegistrar.getProcessor(quantified)
                    .toPRISM())
            .append("]");

        return prism.toString();
    }

    @Override
    public void validateTransientVariables() {
        assert quantifier != null;

        for (Expression child : quantifier.getChildren()) {
            PRISMExporter_ProcessorRegistrar.getProcessor(child)
                .validateTransientVariables();
        }
    }

    @Override
    public boolean usesTransientVariables() {
        assert quantifier != null;

        boolean usesTransient = false;
        for (Expression child : quantifier.getChildren()) {
            usesTransient |= PRISMExporter_ProcessorRegistrar.getProcessor(child)
                    .usesTransientVariables();
        }

        return usesTransient;
    }	
}
