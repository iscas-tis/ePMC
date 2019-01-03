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
import javax.json.JsonArrayBuilder;
import javax.json.JsonValue;

import epmc.jani.exporter.processor.JANIExporter_Processor;
import epmc.jani.exporter.processor.JANIExporter_ProcessorRegistrar;

public class JANIExporter_JANIPropertiesProcessor implements JANIExporter_Processor {

    private JANIProperties properties = null;

    @Override
    public JANIExporter_Processor setElement(Object component) {
        assert component != null;
        assert component instanceof JANIProperties; 

        properties = (JANIProperties) component;

        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert properties != null;
        
        JsonArrayBuilder builder = Json.createArrayBuilder();

        for (JANIPropertyEntry property : properties.getJANIProperties()) {
            builder.add(JANIExporter_ProcessorRegistrar.getProcessor(property)
                    .toJSON());
        }
        
        return builder.build();
    }
}
