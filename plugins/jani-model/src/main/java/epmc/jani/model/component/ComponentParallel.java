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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.jani.model.Action;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.util.UtilJSON;

public final class ComponentParallel implements Component {
    public final static String IDENTIFIER = "parallel";
    /** String identifying the composition type. */
    private final static String COMPOSITION = "composition";
    /** String denoting a parallel composition. */
    private final static String PARALLEL = "parallel";
    /** String identifying left element of the parallel composition. */
    private final static String LEFT = "left";
    /** String identifying right element of the parallel composition. */
    private final static String RIGHT = "right";
    /** String identifying the alphabet of the parallel composition. */
    private final static String ALPHABET = "alphabet";
    /** String identifying comment of this parallel component. */
    private final static String COMMENT = "comment";

    /** Left component of the composition. */
    private Component left;
    /** Right component of the composition. */
    private Component right;
    /** List of actions of the parallel composition */
    private Set<Action> actions = new LinkedHashSet<>();
    /** Unmodifiable list of actions of the parallel composition */
    private Set<Action> actionsExternal = Collections.unmodifiableSet(actions);
    /** Model to which this composition belongs. */
    private ModelJANI model;
    /** Optional comment for this composition. */
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
        UtilJSON.ensureEquals(object, COMPOSITION, PARALLEL);
        SystemParser system = new SystemParser();
        system.setModel(model);
        system.parse(UtilJSON.get(object, LEFT));
        left = system.getSystemComponent();
        system = new SystemParser();
        system.setModel(model);
        system.parse(UtilJSON.get(object, RIGHT));
        right = system.getSystemComponent();
        actions = UtilJSON.toSubsetOf(object, ALPHABET, model.getActionsOrEmpty());
        comment = UtilJSON.getStringOrNull(object, COMMENT);
        return this;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public JsonValue generate() {
        JsonObjectBuilder result = Json.createObjectBuilder();
        result.add(COMPOSITION, PARALLEL);
        result.add(LEFT, left.generate());
        result.add(RIGHT, right.generate());
        JsonArrayBuilder alphabB = Json.createArrayBuilder();
        for (Action action : actions) {
            assert action != null : actions;
            alphabB.add(action.getName());
        }
        result.add(ALPHABET, alphabB.build());
        UtilJSON.addOptional(result, COMMENT, comment);
        return result.build();
    }

    public void addAction(Action action) {
        assert action != null;
        actions.add(action);
    }

    public void addActions(Collection<Action> actions) {
        for (Action action : actions) {
            assert action != null;
        }
        this.actions.addAll(actions);
    }

    public void setLeft(Component left) {
        this.left = left;
    }

    public void setRight(Component right) {
        this.right = right;
    }

    public Component getLeft() {
        return left;
    }

    public Component getRight() {
        return right;
    }

    public Set<Action> getActions() {
        return actionsExternal;
    }

    @Override
    public String toString() {
        return UtilModelParser.toString(this);
    }
}
