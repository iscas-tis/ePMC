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

package epmc.operator;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.expression.standard.ExpressionOperator;
import epmc.jani.exporter.operatorprocessor.OperatorProcessor;
import epmc.jani.exporter.options.OptionsJANIExporter;
import epmc.jani.exporter.processor.ProcessorRegistrar;
import epmc.jani.model.UtilModelParser;
import epmc.options.Options;

/**
 * @author Andrea Turrini
 *
 */
public class JANIExporter_OperatorAbsProcessor implements OperatorProcessor {
    private final static String OP = "op";
    private final static String ABS = "abs";
    private final static String EXP = "exp";
    private final static String ITE = "ite";
    private final static String IF = "if";
    private final static String THEN = "then";
    private final static String ELSE = "else";
    private final static String SUBTRACT = "-";
    private final static String LT = "<";
    private final static String LEFT = "left";
    private final static String RIGHT = "right";

    private ExpressionOperator expressionOperator = null;
    
    @Override
    public OperatorProcessor setExpressionOperator(ExpressionOperator expressionOperator) {
        assert expressionOperator != null;
        assert expressionOperator.getOperator().equals(OperatorAbs.ABS);
    
        this.expressionOperator = expressionOperator;

        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert expressionOperator != null;
        
        JsonObjectBuilder builder = Json.createObjectBuilder();

        if (Options.get().getBoolean(OptionsJANIExporter.JANI_EXPORTER_USE_DERIVED_OPERATORS)) {
            builder.add(OP, ABS);
            builder.add(EXP, ProcessorRegistrar.getProcessor(expressionOperator.getOperand1())
                    .toJSON());
        } else {
            JsonObjectBuilder builderLt = Json.createObjectBuilder();
            builderLt.add(OP, LT);
            builderLt.add(LEFT, ProcessorRegistrar.getProcessor(expressionOperator.getOperand1())
                    .toJSON());
            builderLt.add(RIGHT, 0);
            
            JsonObjectBuilder builderNegative = Json.createObjectBuilder();
            builderNegative.add(OP, SUBTRACT);
            builderNegative.add(LEFT, 0);
            builderNegative.add(RIGHT, ProcessorRegistrar.getProcessor(expressionOperator.getOperand1())
                    .toJSON());

            builder.add(OP, ITE);
            builder.add(IF, builderLt.build());
            builder.add(THEN, builderNegative.build());
            builder.add(ELSE, ProcessorRegistrar.getProcessor(expressionOperator.getOperand1())
                    .toJSON());
        }
        
        UtilModelParser.addPositional(builder, expressionOperator.getPositional());
        
        return builder.build();
    }
}    
