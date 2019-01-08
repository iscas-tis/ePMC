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
import epmc.jani.exporter.processor.JANIExporter_ProcessorRegistrar;
import epmc.jani.model.UtilModelParser;

/**
 * @author Andrea Turrini
 *
 */
public class JANIExporter_OperatorIffProcessor implements OperatorProcessor {
    private static final String OP = "op";
    private static final String IMPLIES = "⇒";
    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    private static final String AND = "∧";
    private static final String OR = "∨";
    private static final String NOT = "¬";
    private static final String EXP = "exp";
    
    private ExpressionOperator expressionOperator = null;
    
    @Override
    public OperatorProcessor setExpressionOperator(ExpressionOperator expressionOperator) {
        assert expressionOperator != null;
        assert expressionOperator.getOperator().equals(OperatorIff.IFF);
    
        this.expressionOperator = expressionOperator;

        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert expressionOperator != null;
        
        JsonObjectBuilder builder = Json.createObjectBuilder();

        if (JANIExporter_ProcessorRegistrar.useDerivedOperators()) {
            builder.add(OP, AND);

            JsonObjectBuilder builderImplies = Json.createObjectBuilder();
            builderImplies.add(OP, IMPLIES);
            builderImplies.add(LEFT, JANIExporter_ProcessorRegistrar.getProcessor(expressionOperator.getOperand1())
                    .toJSON());
            builderImplies.add(RIGHT, JANIExporter_ProcessorRegistrar.getProcessor(expressionOperator.getOperand2())
                    .toJSON());
            
            builder.add(LEFT, builderImplies.build());

            builderImplies = Json.createObjectBuilder();
            builderImplies.add(OP, IMPLIES);
            builderImplies.add(LEFT, JANIExporter_ProcessorRegistrar.getProcessor(expressionOperator.getOperand2())
                    .toJSON());
            builderImplies.add(RIGHT, JANIExporter_ProcessorRegistrar.getProcessor(expressionOperator.getOperand1())
                    .toJSON());
            
            builder.add(RIGHT, builderImplies.build());
        } else {
            JsonObjectBuilder builderOr = Json.createObjectBuilder();
            builderOr.add(OP, OR);
            builderOr.add(LEFT, JANIExporter_ProcessorRegistrar.getProcessor(expressionOperator.getOperand1())
                    .toJSON());
            builderOr.add(RIGHT, JANIExporter_ProcessorRegistrar.getProcessor(expressionOperator.getOperand2())
                    .toJSON());
            
            JsonObjectBuilder builderNot = Json.createObjectBuilder();
            builderNot.add(OP, NOT);
            builderNot.add(EXP, builderOr.build());
            
            JsonObjectBuilder builderAnd = Json.createObjectBuilder();
            builderAnd.add(OP, AND);
            builderAnd.add(LEFT, JANIExporter_ProcessorRegistrar.getProcessor(expressionOperator.getOperand1())
                    .toJSON());
            builderAnd.add(RIGHT, JANIExporter_ProcessorRegistrar.getProcessor(expressionOperator.getOperand2())
                    .toJSON());

            builder.add(OP, OR);
            builder.add(LEFT, builderNot.build());
            builder.add(RIGHT, builderAnd.build());
        }
        
        UtilModelParser.addPositional(builder, expressionOperator.getPositional());
        
        return builder.build();
    }
}    
