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

import epmc.jani.exporter.processor.JANIProcessor;
import epmc.jani.exporter.processor.ProcessorRegistrar;
import epmc.jani.model.Action;

public class SynchronisationVectorSyncProcessor implements JANIProcessor {
    private final static String SYNCHRONISE = "synchronise";
    private final static String RESULT = "result";
    private final static String COMMENT = "comment";

    private SynchronisationVectorSync syncVectorSync = null;

    @Override
    public JANIProcessor setElement(Object component) {
        assert component != null;
        assert component instanceof SynchronisationVectorSync; 

        syncVectorSync = (SynchronisationVectorSync) component;
        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert syncVectorSync != null;

        JsonObjectBuilder builder = Json.createObjectBuilder();

        JsonArrayBuilder synchronise = Json.createArrayBuilder();
        for (Action sync : syncVectorSync.getSynchronise()) {
            if (sync == null) {
                synchronise.addNull();
            } else {
                synchronise.add(sync.getName());
            }
        }
        builder.add(SYNCHRONISE, synchronise);
        
        Action result = syncVectorSync.getResult();
        if (result != null && !ProcessorRegistrar.isSilentAction(result)) {
            builder.add(RESULT, result.getName());
        }
        
        String comment = syncVectorSync.getComment();
        if (comment != null) {
            builder.add(COMMENT, comment);
        }
        
        return builder.build();
    }
}
