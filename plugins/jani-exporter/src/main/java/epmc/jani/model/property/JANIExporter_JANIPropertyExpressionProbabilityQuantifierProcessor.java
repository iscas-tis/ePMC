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

package epmc.jani.model.property;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.expression.standard.ExpressionQuantifier;
import epmc.jani.exporter.processor.JANIProcessor;
import epmc.jani.exporter.processor.ProcessorRegistrar;
import epmc.jani.model.UtilModelParser;
import epmc.jani.model.property.JANIPropertyExpressionProbabilityQuantifier;

public class JANIExporter_JANIPropertyExpressionProbabilityQuantifierProcessor implements JANIProcessor {
    private final static String OP = "op";
    private final static String PMAX = "Pmax";
    private final static String PMIN = "Pmin";
    private final static String EXP = "exp";

    private JANIPropertyExpressionProbabilityQuantifier expressionProbabilityQuantifier = null;

    @Override
    public JANIProcessor setElement(Object obj) {
        assert obj != null;
        assert obj instanceof ExpressionQuantifier; 

        expressionProbabilityQuantifier = (JANIPropertyExpressionProbabilityQuantifier) obj;
        
        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert expressionProbabilityQuantifier != null;
        
        JsonObjectBuilder builder = Json.createObjectBuilder();
        
        ExpressionQuantifier exp = (ExpressionQuantifier) expressionProbabilityQuantifier.getExpression();
        if (exp.isDirMin()) {
            builder.add(OP, PMIN);
        } else {
            builder.add(OP, PMAX);
        }
        builder.add(EXP, ProcessorRegistrar.getProcessor(exp)
                .toJSON());

        UtilModelParser.addPositional(builder, expressionProbabilityQuantifier.getPositional());

        return builder.build();
    }
}
