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
import epmc.jani.exporter.processor.JANIProcessor;
import epmc.jani.exporter.processor.ProcessorRegistrar;
import epmc.jani.model.type.JANIType;
import epmc.jani.valuejson.UtilValueJSON;
import epmc.util.UtilJSON;

public class VariableProcessor implements JANIProcessor {
    /** Identifies a variable. */
    private final static String TYPE = "type";
    /** Identifies the name of a variable. */
    private final static String NAME = "name";
    /** Identifies whether the variable is transient. */
    private final static String TRANSIENT = "transient";
    /** Identifier for initial value of the variable. */
    private final static String INITIAL_VALUE = "initial-value";
    /** String identifying comment of this variable. */
    private final static String COMMENT = "comment";

    private Variable variable = null;

    @Override
    public JANIProcessor setElement(Object component) {
        assert component != null;
        assert component instanceof Variable; 

        variable = (Variable) component;
        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert variable != null;

        JsonObjectBuilder result = Json.createObjectBuilder();
        result.add(NAME, variable.getName());

        JANIType type = variable.getType();
        assert type != null;
        result.add(TYPE, type.generate());
        
        Expression initialValue = variable.getInitialValueOrNull();
        if (initialValue != null) {
            result.add(INITIAL_VALUE, ProcessorRegistrar.getProcessor(initialValue)
                    .toJSON());
        } else {
            if (variable.isTransientAssigned() && variable.isTransient()) {
                result.add(INITIAL_VALUE, UtilValueJSON.valueToJson(type.getDefaultValue()));
            }
        }
        if (variable.isTransientAssigned()) {
            result.add(TRANSIENT, variable.isTransient());
        }
        UtilJSON.addOptional(result, COMMENT, variable.getComment());

        return result.build();
    }
}
