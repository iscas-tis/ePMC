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

import epmc.jani.exporter.processor.JANIExporter_Processor;
import epmc.jani.exporter.processor.JANIExporter_ProcessorRegistrar;

public class JANIExporter_DestinationProcessor implements JANIExporter_Processor {
    /** String indicating the probability of this destination. */
    private static final String PROBABILITY = "probability";
    /** String indicating the source location of this edge. */
    private static final String LOCATION = "location";
    /** String indicating the assignments of this edge. */
    private static final String ASSIGNMENTS = "assignments";
    /** String indicating comment of this destination. */
    private static final String COMMENT = "comment";

    private Destination destination = null;

    @Override
    public JANIExporter_Processor setElement(Object component) {
        assert component != null;
        assert component instanceof Destination; 

        destination = (Destination) component;
        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert destination != null;
        
        JsonObjectBuilder builder = Json.createObjectBuilder();

        Location location = destination.getLocation();
        assert location != null;
        builder.add(LOCATION, location.getName());
        
        Probability probability = destination.getProbability();
        if (probability != null) {
            builder.add(PROBABILITY, JANIExporter_ProcessorRegistrar.getProcessor(probability)
                    .toJSON());
        }
        
        Assignments assignments = destination.getAssignments();
        if (assignments != null) {
            builder.add(ASSIGNMENTS, JANIExporter_ProcessorRegistrar.getProcessor(assignments)
                    .toJSON());
        }
        
        String comment = destination.getComment();
        if (comment != null) {
            builder.add(COMMENT, comment);
        }

        return builder.build();
    }	
}
