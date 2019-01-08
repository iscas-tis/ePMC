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

import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionOperator;
import epmc.jani.exporter.operatorprocessor.OperatorProcessor;
import epmc.jani.exporter.processor.JANIExporter_ProcessorRegistrar;
import epmc.jani.model.UtilModelParser;

public class QMCExporter_OperatorMatrix2JANIProcessor implements OperatorProcessor {
    private final static String KIND = "kind";
    private final static String MATRIX = "matrix";
    private final static String NROWS = "#rows";
    private final static String NCOLS = "#cols";
    private final static String ELEMENTS = "elements";

    private ExpressionOperator expressionOperator = null;
    
    @Override
    public OperatorProcessor setExpressionOperator(ExpressionOperator expressionOperator) {
        assert expressionOperator != null;
        assert expressionOperator.getOperator().equals(OperatorMatrix.MATRIX);
    
        this.expressionOperator = expressionOperator;

        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert expressionOperator != null;
        
        List<Expression> operands = expressionOperator.getOperands();
        
        JsonArrayBuilder array = Json.createArrayBuilder();
        for (int op = 2; op < operands.size(); op++) {
            array.add(JANIExporter_ProcessorRegistrar.getProcessor(operands.get(op))
                    .toJSON());
        }

        JsonObjectBuilder builder = Json.createObjectBuilder();
        
        builder.add(KIND, MATRIX);
        builder.add(NROWS, JANIExporter_ProcessorRegistrar.getProcessor(operands.get(0))
                .toJSON());
        builder.add(NCOLS, JANIExporter_ProcessorRegistrar.getProcessor(operands.get(1))
                .toJSON());
        builder.add(ELEMENTS, array.build());
        
        UtilModelParser.addPositional(builder, expressionOperator.getPositional());
        
        return builder.build();
    }

}
