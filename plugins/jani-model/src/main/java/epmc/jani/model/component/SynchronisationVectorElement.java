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

import java.util.Collections;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.jani.model.Action;
import epmc.jani.model.Automaton;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.util.UtilJSON;

public final class SynchronisationVectorElement implements JANINode {
    private final static String AUTOMATON = "automaton";
    private final static String INPUT_ENABLE = "input-enable";
    private final static String COMMENT = "comment";

    private ModelJANI model;
    private Automaton automaton;
    private String comment;
    private Set<Action> inputEnable;

    @Override
    public void setModel(ModelJANI model) {
        this.model = model;
    }

    @Override
    public ModelJANI getModel() {
        return model;
    }

    @Override
    public JANINode parse(JsonValue value) {
        assert value != null;
        JsonObject object = UtilJSON.toObject(value);
        automaton = UtilJSON.toOneOf(object, AUTOMATON, model.getAutomata());
        comment = UtilJSON.getStringOrNull(object, COMMENT);
        if (object.containsKey(INPUT_ENABLE)) {
            inputEnable = UtilJSON.toSubsetOf(object, INPUT_ENABLE, model.getActionsOrEmpty());
        }
        return this;
    }

    @Override
    public JsonValue generate() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add(AUTOMATON, automaton.getName());
        if (inputEnable != null) {
            JsonArrayBuilder inputEnableBuilder = Json.createArrayBuilder();
            for (Action action : inputEnable) {
                inputEnableBuilder.add(action.getName());
            }
            builder.add(INPUT_ENABLE, inputEnableBuilder);
        }
        UtilJSON.addOptional(builder, COMMENT, comment);
        return builder.build();
    }

    public void setAutomaton(Automaton automaton) {
        this.automaton = automaton;
    }

    public Automaton getAutomaton() {
        return automaton;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public void setInputEnable(Set<Action> inputEnable) {
        this.inputEnable = inputEnable;
    }

    public Set<Action> getInputEnable() {
        return inputEnable;
    }

    public Set<Action> getInputEnableOrEmpty() {
        if (inputEnable == null) {
            return Collections.emptySet();
        } else {
            return inputEnable;
        }
    }
}
