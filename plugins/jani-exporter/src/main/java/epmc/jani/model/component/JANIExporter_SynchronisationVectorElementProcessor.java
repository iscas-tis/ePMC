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

import java.util.Set;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.jani.exporter.processor.JANIExporter_Processor;
import epmc.jani.exporter.processor.JANIExporter_ProcessorRegistrar;
import epmc.jani.model.Action;
import epmc.jani.model.Automaton;

public class JANIExporter_SynchronisationVectorElementProcessor implements JANIExporter_Processor {
    private static final String AUTOMATON = "automaton";
    private static final String INPUT_ENABLE = "input-enable";
    private static final String COMMENT = "comment";

    private SynchronisationVectorElement syncVectorElement = null;

    @Override
    public JANIExporter_Processor setElement(Object component) {
        assert component != null;
        assert component instanceof SynchronisationVectorElement; 

        syncVectorElement = (SynchronisationVectorElement) component;
        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert syncVectorElement != null;

        JsonObjectBuilder builder = Json.createObjectBuilder();
        
        Automaton automaton = syncVectorElement.getAutomaton();
        assert automaton != null;
        builder.add(AUTOMATON, automaton.getName());
        
        Set<Action> inputEnable = syncVectorElement.getInputEnable();
        if (inputEnable != null) {
            JsonArrayBuilder inputEnableBuilder = Json.createArrayBuilder();
            
            for (Action action : inputEnable) {
                assert action != null;
                assert !JANIExporter_ProcessorRegistrar.isSilentAction(action);
                inputEnableBuilder.add(action.getName());
            }
            builder.add(INPUT_ENABLE, inputEnableBuilder);
        }
        
        String comment = syncVectorElement.getComment();
        if (comment != null) {
            builder.add(COMMENT, comment);
        }
        
        return builder.build();
    }
}
