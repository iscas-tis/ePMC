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

package epmc.jani.model.component;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.jani.exporter.options.OptionsJANIExporter;
import epmc.jani.exporter.processor.JANIExporter_Processor;
import epmc.jani.exporter.processor.JANIExporter_ProcessorRegistrar;
import epmc.jani.model.Action;
import epmc.options.Options;

public class JANIExporter_ComponentSynchronisationVectorsProcessor implements JANIExporter_Processor {
    public static final String IDENTIFIER = "synchronisation-vectors";
    private static final String ELEMENTS = "elements";
    private static final String SYNCS = "syncs";
    private static final String COMMENT = "comment";

    private ComponentSynchronisationVectors syncVectors = null;

    @Override
    public JANIExporter_Processor setElement(Object component) {
        assert component != null;
        assert component instanceof ComponentSynchronisationVectors; 

        syncVectors = (ComponentSynchronisationVectors) component;
        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert syncVectors != null;

        JsonObjectBuilder builder = Json.createObjectBuilder();
        
        JsonArrayBuilder elements = Json.createArrayBuilder();
        for (SynchronisationVectorElement element : syncVectors.getElements()) {
            elements.add(JANIExporter_ProcessorRegistrar.getProcessor(element)
                    .toJSON());
        }
        builder.add(ELEMENTS, elements);
        
        int nsyncs = 0;
        JsonArrayBuilder syncs = Json.createArrayBuilder();
        for (SynchronisationVectorSync sync : syncVectors.getSyncs()) {
            if (hasToBeAdded(sync)) {
                syncs.add(JANIExporter_ProcessorRegistrar.getProcessor(sync)
                        .toJSON());
                nsyncs++;
            }
        }
        if (nsyncs > 0) {
            builder.add(SYNCS, syncs);
        }

        String comment = syncVectors.getComment();
        if (comment != null) {
            builder.add(COMMENT, comment);
        }
        
        return builder.build();
    }
    
    private boolean hasToBeAdded(SynchronisationVectorSync sync) {
        if (Options.get().getBoolean(OptionsJANIExporter.JANI_EXPORTER_SYNCHRONISE_SILENT)) {
            return true;
        } else {
            if (JANIExporter_ProcessorRegistrar.isSilentAction(sync.getResult())) {
                int nactions = 0;
                boolean onlySilent = true;
                for (Action action : sync.getSynchronise()) {
                    if (action != null) {
                        nactions++;
                        onlySilent &= JANIExporter_ProcessorRegistrar.isSilentAction(action);
                    }
                }
                assert nactions > 0;
                return (nactions > 1) || !onlySilent;
            } else {
                return true;
            }
        }
    }
}
