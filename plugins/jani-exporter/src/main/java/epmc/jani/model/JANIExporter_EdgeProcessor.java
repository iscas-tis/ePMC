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

public class JANIExporter_EdgeProcessor implements JANIExporter_Processor {
    /** String identifying the source location. */
    private static final String LOCATION = "location";
    /** String identifying the action, if given. */
    private static final String ACTION = "action";
    /** String identifying the rate, if given. */
    private static final String RATE = "rate";
    /** String identifying the guard. */
    private static final String GUARD = "guard";
    /** String identifying the list of destinations. */
    private static final String DESTINATIONS = "destinations";
    /** String identifying comment of edge. */
    private static final String COMMENT = "comment";

    private Edge edge = null;

    @Override
    public JANIExporter_Processor setElement(Object component) {
        assert component != null;
        assert component instanceof Edge; 

        edge = (Edge) component;
        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert edge != null;

        JsonObjectBuilder result = Json.createObjectBuilder();

        Location location = edge.getLocation();
        assert location != null;
        result.add(LOCATION, location.getName());
        
        Action action = edge.getAction();
        if (action != null) {
            result.add(ACTION, action.getName());
        }
        
        Rate rate = edge.getRate();
        if (rate != null) {
            result.add(RATE, JANIExporter_ProcessorRegistrar.getProcessor(rate)
                    .toJSON());
        }

        Guard guard = edge.getGuard();
        if (guard != null) {
            result.add(GUARD, JANIExporter_ProcessorRegistrar.getProcessor(guard)
                    .toJSON());
        }
        
        result.add(DESTINATIONS, JANIExporter_ProcessorRegistrar.getProcessor(edge.getDestinations())
                .toJSON());
        
        String comment = edge.getComment();
        if (comment != null) {
            result.add(COMMENT, comment);
        }
        
        return result.build();
    }
}
