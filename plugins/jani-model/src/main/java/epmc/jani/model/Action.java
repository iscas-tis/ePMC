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
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.util.UtilJSON;

/**
 * Specifies an action in a model.
 * 
 * @author Ernst Moritz Hahn
 */
public final class Action implements JANINode {
    /** Identifies the name of a given action. */
    private final static String NAME = "name";
    private final static String COMMENT = "comment";

    /** Name of the action. */
    private String name;

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
        name = UtilJSON.getString(object, NAME);
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
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add(NAME, getName()).build();
        UtilJSON.addOptional(builder, COMMENT, comment);
        return builder.build();
    }

    /**
     * Gets the name of the action.
     * 
     * @return name of the action
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return UtilModelParser.toString(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Action) {
            return name.equals(((Action)obj).getName());
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
