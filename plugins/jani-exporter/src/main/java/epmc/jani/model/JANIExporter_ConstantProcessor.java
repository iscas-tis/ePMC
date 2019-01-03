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

public class JANIExporter_ConstantProcessor implements JANIExporter_Processor {
    /** Identifier for the name of the constant. */
    private static final String NAME = "name";
    /** Identifier for the type of the constant. */
    private static final String TYPE = "type";
    /** Identifier for the value of the constant. */
    private static final String VALUE = "value";
    /** Identifier for the comment of the constant. */
    private static final String COMMENT = "comment";

    private Constant constant = null;

    @Override
    public JANIExporter_Processor setElement(Object component) {
        assert component != null;
        assert component instanceof Constant; 

        constant = (Constant) component;
        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert constant != null;

        JsonObjectBuilder builder = Json.createObjectBuilder();
        
        builder.add(NAME, constant.getName());

        builder.add(TYPE, JANIExporter_ProcessorRegistrar.getProcessor(constant.getType())
                .toJSON());
        
        Expression value = constant.getValue();
        if (value != null) {
            builder.add(VALUE, JANIExporter_ProcessorRegistrar.getProcessor(value)
                    .toJSON());
        }
        
        String comment = constant.getComment();
        if (comment != null) {
            builder.add(COMMENT, comment);
        }
        
        return builder.build();
    }
}
