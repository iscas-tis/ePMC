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

package epmc.jani.model.type;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.expression.Expression;
import epmc.jani.exporter.processor.JANIProcessor;
import epmc.jani.exporter.processor.ProcessorRegistrar;

public final class JANITypeBoundedProcessor implements JANIProcessor {
    public final static String IDENTIFIER = "bounded";
    /** Identifies the type of variable type specification. */
    private final static String KIND = "kind";
    /** Identifier for bounded type. */
    private final static String BOUNDED = "bounded";
    /** Identifier for base type of bounded type. */
    private final static String BASE = "base";
    /** Identifier for integer base type of bounded type. */
    private final static String INT = "int";
    /** Identifier of lower bound of bounded type. */
    private final static String LOWER_BOUND = "lower-bound";
    /** Identifier for upper bond of bounded type. */
    private final static String UPPER_BOUND = "upper-bound";


    private JANITypeBounded bounded = null;

    @Override
    public JANIProcessor setElement(Object component) {
        assert component != null;
        assert component instanceof JANITypeBounded;

        bounded = (JANITypeBounded) component;
        return this;
    }
    
    @Override
    public JsonValue toJSON() {
        assert bounded != null;

        JsonObjectBuilder result = Json.createObjectBuilder();
        
        result.add(KIND, BOUNDED)
            .add(BASE, INT);

        Expression lowerBound = bounded.getLowerBound(); 
        if (lowerBound != null) {
            result.add(LOWER_BOUND, ProcessorRegistrar.getExpressionProcessor(lowerBound)
                    .toJSON());
//                    ExpressionParser.generateExpression(model, lowerBound));
        }

        Expression upperBound = bounded.getUpperBound(); 
        if (upperBound != null) {
            result.add(UPPER_BOUND, ProcessorRegistrar.getExpressionProcessor(upperBound)
                    .toJSON());
//                    ExpressionParser.generateExpression(model, upperBound));
        }
        
        return result.build();
    }
}
