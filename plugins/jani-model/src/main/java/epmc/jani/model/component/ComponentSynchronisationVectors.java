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

import static epmc.error.UtilError.ensure;

import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.ProblemsJANIParser;
import epmc.jani.model.UtilModelParser;
import epmc.util.UtilJSON;

public final class ComponentSynchronisationVectors implements Component {
    public final static String IDENTIFIER = "synchronisation-vectors";
    private final static String ELEMENTS = "elements";
    private final static String SYNCS = "syncs";
    private final static String COMMENT = "comment";

    /** Model to which these synchronisation vectors belong. */
    private ModelJANI model;
    /** Elements involved in the synchronisation. */
    private List<SynchronisationVectorElement> elements;
    /** Synchronisation vector list. */
    private List<SynchronisationVectorSync> syncs;
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
        this.elements = new ArrayList<>();
        JsonArray elements = UtilJSON.getArrayObject(object, ELEMENTS);
        for (JsonValue elementValue : elements) {
            SynchronisationVectorElement element = new SynchronisationVectorElement();
            element.setModel(model);
            element.parse(elementValue);
            this.elements.add(element);
        }
        this.syncs = new ArrayList<>();
        JsonArray syncs = UtilJSON.getArrayObjectOrEmpty(object, SYNCS);
        for (JsonValue syncValue : syncs) {
            SynchronisationVectorSync sync = new SynchronisationVectorSync();
            sync.setModel(model);
            sync.parse(syncValue);
            ensure(this.elements.size() == sync.getSynchronise().size(),
                    ProblemsJANIParser.JANI_PARSER_COMPOSITION_DIFFERENT_SIZES);
            this.syncs.add(sync);
        }		
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
        JsonArrayBuilder elements = Json.createArrayBuilder();
        for (SynchronisationVectorElement element : this.elements) {
            elements.add(element.generate());
        }
        result.add(ELEMENTS, elements);
        JsonArrayBuilder syncs = Json.createArrayBuilder();
        for (SynchronisationVectorSync sync : this.syncs) {
            syncs.add(sync.generate());
        }
        result.add(SYNCS, syncs);
        return result.build();
    }

    public void setElements(List<SynchronisationVectorElement> elements) {
        this.elements = elements;
    }

    public List<SynchronisationVectorElement> getElements() {
        return elements;
    }

    public void setSyncs(List<SynchronisationVectorSync> syncs) {
        this.syncs = syncs;
    }

    public List<SynchronisationVectorSync> getSyncs() {
        return syncs;
    }

    @Override
    public String toString() {
        return UtilModelParser.toString(this);
    }
}
