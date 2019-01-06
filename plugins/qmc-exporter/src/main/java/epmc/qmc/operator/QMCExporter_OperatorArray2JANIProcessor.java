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

package epmc.qmc.operator;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonValue;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionOperator;
import epmc.jani.exporter.operatorprocessor.OperatorProcessor;
import epmc.jani.exporter.processor.JANIExporter_ProcessorRegistrar;

public class QMCExporter_OperatorArray2JANIProcessor implements OperatorProcessor {

    private ExpressionOperator expressionOperator = null;
    
    @Override
    public OperatorProcessor setExpressionOperator(ExpressionOperator expressionOperator) {
        assert expressionOperator != null;
        assert expressionOperator.getOperator().equals(OperatorArray.ARRAY);
    
        this.expressionOperator = expressionOperator;

        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert expressionOperator != null;
        
        JsonArrayBuilder array = Json.createArrayBuilder();
        
        boolean first = true;
        for(Expression operand : expressionOperator.getOperands()) {
            //the first operand is a number telling how many other elements are actually in the array
            if (first) {
                first = false;
            } else {
                array.add(JANIExporter_ProcessorRegistrar.getProcessor(operand)
                        .toJSON());
            }
        }
        
        return array.build();
    }

}
