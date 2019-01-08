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

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.jani.exporter.processor.JANIExporter_Processor;
import epmc.jani.exporter.processor.JANIExporter_ProcessorRegistrar;
import epmc.jani.model.UtilModelParser;

public class JANIExporter_ExpressionFilterProcessor implements JANIExporter_Processor {
    private static final String OP = "op";
    private static final String FILTER = "filter";
    private static final String FUN = "fun";
    private static final String VALUES = "values";
    private static final String STATES = "states";

    private ExpressionFilter expressionFilter = null;

    @Override
    public JANIExporter_Processor setElement(Object obj) {
        assert obj != null;
        assert obj instanceof ExpressionFilter; 

        expressionFilter = (ExpressionFilter) obj;
        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert expressionFilter != null;
        
        JsonObjectBuilder builder = Json.createObjectBuilder();
        
        builder.add(OP, FILTER);
        builder.add(FUN, JANIExporter_ProcessorRegistrar.getProcessor(expressionFilter.getFilterType())
                .toJSON());
        builder.add(VALUES, JANIExporter_ProcessorRegistrar.getProcessor(expressionFilter.getProp())
                .toJSON());
        builder.add(STATES, JANIExporter_ProcessorRegistrar.getProcessor(expressionFilter.getStates())
                .toJSON());
        UtilModelParser.addPositional(builder, expressionFilter.getPositional());

        return builder.build();
    }
}
