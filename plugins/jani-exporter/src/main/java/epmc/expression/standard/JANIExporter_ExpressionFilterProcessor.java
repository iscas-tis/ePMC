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

import epmc.jani.exporter.processor.JANIProcessor;
import epmc.jani.exporter.processor.ProcessorRegistrar;
import epmc.jani.model.UtilModelParser;

public class JANIExporter_ExpressionFilterProcessor implements JANIProcessor {
    private final static String OP = "op";
    private final static String FILTER = "filter";
    private final static String FUN = "fun";
    private final static String VALUES = "values";
    private final static String STATES = "states";

    private ExpressionFilter filter = null;

    @Override
    public JANIProcessor setElement(Object obj) {
        assert obj != null;
        assert obj instanceof ExpressionFilter; 

        filter = (ExpressionFilter) obj;
        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert filter != null;
        
        JsonObjectBuilder builder = Json.createObjectBuilder();
        
        builder.add(OP, FILTER);
        builder.add(FUN, ProcessorRegistrar.getProcessor(filter.getFilterType())
                .toJSON());
        builder.add(VALUES, ProcessorRegistrar.getProcessor(filter.getProp())
                .toJSON());
        builder.add(STATES, ProcessorRegistrar.getProcessor(filter.getStates())
                .toJSON());
        UtilModelParser.addPositional(builder, filter.getPositional());

        return builder.build();
    }
}
