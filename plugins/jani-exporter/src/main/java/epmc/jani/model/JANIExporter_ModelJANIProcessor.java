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

import java.util.List;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.jani.exporter.processor.JANIExporter_Processor;
import epmc.jani.exporter.processor.JANIExporter_ProcessorRegistrar;
import epmc.modelchecker.Properties;

public class JANIExporter_ModelJANIProcessor implements JANIExporter_Processor {
    /** Identifier of this model class. */
    public static final String IDENTIFIER = "jani";
    /** Identifies the part of a model where its system components are given. */
    private static final String SYSTEM = "system";
    /** Identifies the part of a model where its automata are specified. */
    private static final String AUTOMATA = "automata";
    /** Identifies the part of a model where its properties are specified. */
    private static final String PROPERTIES = "properties";
    /** Identifies the variable declaration part of a model. */
    private static final String VARIABLES = "variables";
    /** Identifies the action set of a model. */
    private static final String ACTIONS = "actions";
    /** Name of the model (e.g. filename). */
    private static final String NAME = "name";
    /** JANI version. */
    private static final String JANI_VERSION = "jani-version";
    /** Identifies the semantics type of a model. */
    private static final String TYPE = "type";
    /** String denoting model features (extensions) field. */
    private static final String FEATURES = "features";
    /** Denotes metadata field. */
    private static final String METADATA = "metadata";
    /** Initial assignment to global variables. */
    private static final String RESTRICT_INITIAL = "restrict-initial";
    /** Denotes model constants. */
    private static final String CONSTANTS = "constants";

    private ModelJANI jani = null;

    @Override
    public JANIExporter_Processor setElement(Object component) {
        assert component != null;
        assert component instanceof ModelJANI;

        jani = (ModelJANI) component;

        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert jani != null;

        JsonObjectBuilder builder = Json.createObjectBuilder();
        
        builder.add(JANI_VERSION, jani.getJaniVersion());
        
        builder.add(NAME, jani.getName());
        
        Metadata metadata = jani.getMetadata();
        if (metadata != null) {
            builder.add(METADATA, JANIExporter_ProcessorRegistrar.getProcessor(metadata)
                    .toJSON());
        }
        
        builder.add(TYPE, jani.getSemanticsIdentifier());
        
        List<ModelExtension> modelExtensions = jani.getModelExtensions();
        if (modelExtensions != null) {
            JsonArrayBuilder builderarray = Json.createArrayBuilder();
            for (ModelExtension extension : modelExtensions) {
                builderarray.add(extension.getIdentifier());
            }
            builder.add(FEATURES, builderarray);
        }

        Actions actions = jani.getActions();
        if (actions != null) {
            builder.add(ACTIONS, JANIExporter_ProcessorRegistrar.getProcessor(actions)
                    .toJSON());
        }
        
        Constants constants = jani.getModelConstants();
        if (constants != null) {
            builder.add(CONSTANTS, JANIExporter_ProcessorRegistrar.getProcessor(constants)
                    .toJSON());
        }
        
        Variables variables = jani.getGlobalVariables();
        if (variables != null) {
            builder.add(VARIABLES, JANIExporter_ProcessorRegistrar.getProcessor(variables)
                    .toJSON());
        }
        
        InitialStates restrictInitial = jani.getRestrictInitial();
        if (restrictInitial != null) {
            builder.add(RESTRICT_INITIAL, JANIExporter_ProcessorRegistrar.getProcessor(restrictInitial)
                    .toJSON());
        }
        
        Properties properties = jani.getPropertyList();
        if (properties != null) {
            builder.add(PROPERTIES, JANIExporter_ProcessorRegistrar.getProcessor(properties)
                    .toJSON());
        }
        
        builder.add(AUTOMATA, JANIExporter_ProcessorRegistrar.getProcessor(jani.getAutomata())
                .toJSON());
        
        builder.add(SYSTEM, JANIExporter_ProcessorRegistrar.getProcessor(jani.getSystem())
                .toJSON());
        
        return builder.build();      
    }
}
