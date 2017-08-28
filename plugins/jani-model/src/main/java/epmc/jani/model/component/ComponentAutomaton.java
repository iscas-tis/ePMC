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
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.jani.model.Automaton;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.util.UtilJSON;

public final class ComponentAutomaton implements Component {
    public final static String IDENTIFIER = "leaf";
    private final static String AUTOMATON = "automaton";
    private final static String COMMENT = "comment";
    private final static String COMPOSITION = "composition";
    private final static String LEAF = "leaf";

    /** Automaton to which this component refers. */
    private Automaton automaton;
    private ModelJANI model;
    private String comment;

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
        assert model != null;
        assert value != null;
        JsonObject object = UtilJSON.toObject(value);
        automaton = UtilJSON.toOneOf(object, AUTOMATON, model.getAutomata());
        comment = UtilJSON.getStringOrNull(object, COMMENT);
        return this;
    }

    @Override
    public JsonValue generate() {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add(COMPOSITION, LEAF);
        builder.add(AUTOMATON, automaton.getName());
        UtilJSON.addOptional(builder, COMMENT, comment);
        return builder.build();
    }

    public void setAutomaton(Automaton automaton) {
        this.automaton = automaton;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    /**
     * Get automaton to which this component refers.
     * This method must not be called before parsing.
     * 
     * @return automaton to which this component refers
     */
    public Automaton getAutomaton() {
        return automaton;
    }

    @Override
    public String toString() {
        return UtilModelParser.toString(this);
    }
}
