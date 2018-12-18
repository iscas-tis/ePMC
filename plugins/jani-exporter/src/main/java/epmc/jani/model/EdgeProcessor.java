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

import epmc.jani.exporter.processor.JANIProcessor;
import epmc.jani.exporter.processor.ProcessorRegistrar;

public class EdgeProcessor implements JANIProcessor {
    /** String identifying the source location. */
    private final static String LOCATION = "location";
    /** String identifying the action, if given. */
    private final static String ACTION = "action";
    /** String identifying the rate, if given. */
    private final static String RATE = "rate";
    /** String identifying the guard. */
    private final static String GUARD = "guard";
    /** String identifying the list of destinations. */
    private final static String DESTINATIONS = "destinations";
    /** String identifying comment of edge. */
    private final static String COMMENT = "comment";

    private Edge edge = null;

    @Override
    public JANIProcessor setElement(Object component) {
        assert component != null;
        assert component instanceof Edge; 

        edge = (Edge) component;
        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert edge != null;

        JsonObjectBuilder result = Json.createObjectBuilder();
        
        result.add(LOCATION, edge.getLocation().getName());
        
        Action action = edge.getAction();
        if (action != null) {
            result.add(ACTION, action.getName());
        }
        
        Rate rate = edge.getRate();
        if (rate != null) {
            result.add(RATE, ProcessorRegistrar.getProcessor(rate)
                    .toJSON());
        }

        Guard guard = edge.getGuard();
        if (guard != null) {
            result.add(GUARD, ProcessorRegistrar.getProcessor(guard)
                    .toJSON());
        }
        
        result.add(DESTINATIONS, ProcessorRegistrar.getProcessor(edge.getDestinations())
                .toJSON());
        
        String comment = edge.getComment();
        if (comment != null) {
            result.add(COMMENT, comment);
        }
        
        return result.build();
    }
}
