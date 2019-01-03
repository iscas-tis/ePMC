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

package epmc.jani.model;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.expression.Expression;
import epmc.jani.exporter.processor.JANIExporter_Processor;
import epmc.jani.exporter.processor.JANIExporter_ProcessorRegistrar;
import epmc.jani.model.type.JANIType;
import epmc.jani.valuejson.UtilValueJSON;

public class JANIExporter_VariableProcessor implements JANIExporter_Processor {
    /** Identifies a variable. */
    private static final String TYPE = "type";
    /** Identifies the name of a variable. */
    private static final String NAME = "name";
    /** Identifies whether the variable is transient. */
    private static final String TRANSIENT = "transient";
    /** Identifier for initial value of the variable. */
    private static final String INITIAL_VALUE = "initial-value";
    /** String identifying comment of this variable. */
    private static final String COMMENT = "comment";

    private Variable variable = null;

    @Override
    public JANIExporter_Processor setElement(Object component) {
        assert component != null;
        assert component instanceof Variable; 

        variable = (Variable) component;
        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert variable != null;

        JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add(NAME, variable.getName());

        JANIType type = variable.getType();
        builder.add(TYPE, JANIExporter_ProcessorRegistrar.getProcessor(type)
                .toJSON());
        
        Expression initialValue = variable.getInitialValueOrNull();
        if (initialValue != null) {
            builder.add(INITIAL_VALUE, JANIExporter_ProcessorRegistrar.getProcessor(initialValue)
                    .toJSON());
        } else {
            if (variable.isTransientAssigned() && variable.isTransient()) {
                builder.add(INITIAL_VALUE, UtilValueJSON.valueToJson(type.getDefaultValue()));
            }
        }
        
        if (variable.isTransientAssigned()) {
            builder.add(TRANSIENT, variable.isTransient());
        }

        String comment = variable.getComment();
        if (comment != null) {
            builder.add(COMMENT, comment);
        }
        
        return builder.build();
    }
}
