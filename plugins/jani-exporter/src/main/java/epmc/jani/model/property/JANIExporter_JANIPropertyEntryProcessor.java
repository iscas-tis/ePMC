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

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionFilter;
import epmc.jani.exporter.processor.JANIExporter_Processor;
import epmc.jani.exporter.processor.JANIExporter_ProcessorRegistrar;

public class JANIExporter_JANIPropertyEntryProcessor implements JANIExporter_Processor {
    private static final String NAME = "name";
    private static final String EXPRESSION = "expression";
    private static final String COMMENT = "comment";

    private static final String OP = "op";
    private static final String FILTER = "filter";
    private static final String FUN = "fun";
    private static final String FUN_VALUES = "values";
    private static final String VALUES = "values";
    private static final String STATES = "states";
    private static final ExpressionInitial INITIAL = ExpressionInitial.getExpressionInitial();

    private JANIPropertyEntry propertyEntry = null;

    @Override
    public JANIExporter_Processor setElement(Object component) {
        assert component != null;
        assert component instanceof JANIPropertyEntry; 

        propertyEntry = (JANIPropertyEntry) component;

        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert propertyEntry != null;
        
        JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add(NAME, propertyEntry.getName());
        JsonValue filteredExpression = addTopLevelFilter(propertyEntry.getExpression());
        builder.add(EXPRESSION, filteredExpression);
        
        String comment = propertyEntry.getComment();
        if (comment != null) {
            builder.add(COMMENT, comment);
        }

        return builder.build();
    }
    
    private JsonValue addTopLevelFilter(Expression expression) {
        if (expression instanceof ExpressionFilter) {
            return JANIExporter_ProcessorRegistrar.getProcessor(expression)
                    .toJSON();
        } else {
            JsonObjectBuilder builder = Json.createObjectBuilder();
            
            builder.add(OP, FILTER);
            builder.add(FUN, FUN_VALUES);
            builder.add(VALUES, JANIExporter_ProcessorRegistrar.getProcessor(expression)
                    .toJSON());
            builder.add(STATES, JANIExporter_ProcessorRegistrar.getProcessor(INITIAL)
                    .toJSON());

            return builder.build();
        }
    }
}
