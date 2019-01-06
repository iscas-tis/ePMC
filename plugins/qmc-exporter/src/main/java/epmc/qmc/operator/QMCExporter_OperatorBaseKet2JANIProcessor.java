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
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.expression.standard.ExpressionOperator;
import epmc.jani.exporter.operatorprocessor.OperatorProcessor;
import epmc.jani.exporter.processor.JANIExporter_ProcessorRegistrar;
import epmc.jani.model.UtilModelParser;

public class QMCExporter_OperatorBaseKet2JANIProcessor implements OperatorProcessor {
    private static final String OP = "op";
    private static final String BASE_KET = "base-ket";
    private static final String DIMENSION = "dimension";
    private static final String EXP = "exp";

    private ExpressionOperator expressionOperator = null;
    
    @Override
    public OperatorProcessor setExpressionOperator(ExpressionOperator expressionOperator) {
        assert expressionOperator != null;
        assert expressionOperator.getOperator().equals(OperatorBaseKet.BASE_KET);
    
        this.expressionOperator = expressionOperator;

        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert expressionOperator != null;
        
        JsonObjectBuilder builder = Json.createObjectBuilder();
        
        builder.add(OP, BASE_KET);
        builder.add(DIMENSION, JANIExporter_ProcessorRegistrar.getProcessor(expressionOperator.getOperand2())
                .toJSON());
        builder.add(EXP, JANIExporter_ProcessorRegistrar.getProcessor(expressionOperator.getOperand1())
                .toJSON());
        
        UtilModelParser.addPositional(builder, expressionOperator.getPositional());
        
        return builder.build();
    }

}
