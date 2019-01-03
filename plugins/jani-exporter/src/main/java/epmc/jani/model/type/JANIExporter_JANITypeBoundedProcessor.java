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
import epmc.jani.exporter.processor.JANIExporter_Processor;
import epmc.jani.exporter.processor.JANIExporter_ProcessorRegistrar;

public final class JANIExporter_JANITypeBoundedProcessor implements JANIExporter_Processor {
    public static final String IDENTIFIER = "bounded";
    /** Identifies the type of variable type specification. */
    private static final String KIND = "kind";
    /** Identifier for bounded type. */
    private static final String BOUNDED = "bounded";
    /** Identifier for base type of bounded type. */
    private static final String BASE = "base";
    /** Identifier for integer base type of bounded type. */
    private static final String INT = "int";
    /** Identifier of lower bound of bounded type. */
    private static final String LOWER_BOUND = "lower-bound";
    /** Identifier for upper bond of bounded type. */
    private static final String UPPER_BOUND = "upper-bound";


    private JANITypeBounded bounded = null;

    @Override
    public JANIExporter_Processor setElement(Object component) {
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
            result.add(LOWER_BOUND, JANIExporter_ProcessorRegistrar.getProcessor(lowerBound)
                    .toJSON());
        }

        Expression upperBound = bounded.getUpperBound(); 
        if (upperBound != null) {
            result.add(UPPER_BOUND, JANIExporter_ProcessorRegistrar.getProcessor(upperBound)
                    .toJSON());
        }
        
        return result.build();
    }
}
