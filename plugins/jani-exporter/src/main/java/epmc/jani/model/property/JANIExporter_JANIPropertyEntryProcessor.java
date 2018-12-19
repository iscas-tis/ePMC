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

import epmc.jani.exporter.processor.JANIProcessor;
import epmc.jani.exporter.processor.ProcessorRegistrar;

public class JANIExporter_JANIPropertyEntryProcessor implements JANIProcessor {
    private final static String NAME = "name";
    private final static String EXPRESSION = "expression";
    private final static String COMMENT = "comment";

    private JANIPropertyEntry propertyEntry = null;

    @Override
    public JANIProcessor setElement(Object component) {
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
        builder.add(EXPRESSION, ProcessorRegistrar.getProcessor(propertyEntry.getExpression())
                .toJSON());
        
        String comment = propertyEntry.getComment();
        if (comment != null) {
            builder.add(COMMENT, comment);
        }

        return builder.build();
    }
}
