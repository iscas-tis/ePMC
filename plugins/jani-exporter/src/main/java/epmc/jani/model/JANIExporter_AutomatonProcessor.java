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
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.jani.exporter.processor.JANIExporter_Processor;
import epmc.jani.exporter.processor.JANIExporter_ProcessorRegistrar;

public class JANIExporter_AutomatonProcessor implements JANIExporter_Processor {
    /** String identifying the name of an automaton. */
    private static final String NAME = "name";
    /** String identifying the variables of an automaton. */
    private static final String VARIABLES = "variables";
    /** String identifying the locations of an automaton. */
    private static final String LOCATIONS = "locations";
    /** String identifying the initial location of an automaton. */
    private static final String INITIAL_LOCATIONS = "initial-locations";
    /** String identifying the edges of an automaton. */
    private static final String EDGES = "edges";
    /** String identifying comment for this automaton. */
    private static final String COMMENT = "comment";
    /** String identifying initial variable values of this automaton. */
    private static final String RESTRICT_INITIAL = "restrict-initial";

    private Automaton automaton = null;

    @Override
    public JANIExporter_Processor setElement(Object component) {
        assert component != null;
        assert component instanceof Automaton;

        automaton = (Automaton) component;
        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert automaton != null;

        JsonObjectBuilder builder = Json.createObjectBuilder();
        
        builder.add(NAME, automaton.getName());
        
        Variables variables = automaton.getVariables();
        if (variables != null) {
            builder.add(VARIABLES, JANIExporter_ProcessorRegistrar.getProcessor(variables)
                    .toJSON());
        }

        InitialStates initialStates = automaton.getInitialStates();
        if (initialStates != null) {
            builder.add(RESTRICT_INITIAL, JANIExporter_ProcessorRegistrar.getProcessor(initialStates)
                    .toJSON());
        }
        
        builder.add(LOCATIONS, JANIExporter_ProcessorRegistrar.getProcessor(automaton.getLocations())
                .toJSON());
        
        JsonArrayBuilder initialLocations = Json.createArrayBuilder();
        for (Location initialLocation : automaton.getInitialLocations()) {
            initialLocations.add(initialLocation.getName());
        }
        builder.add(INITIAL_LOCATIONS, initialLocations);
        
        builder.add(EDGES, JANIExporter_ProcessorRegistrar.getProcessor(automaton.getEdges())
                .toJSON());
        
        String comment = automaton.getComment();
        if (comment != null) {
            builder.add(COMMENT, comment);
        }
        
        return builder.build();
    }	
}
