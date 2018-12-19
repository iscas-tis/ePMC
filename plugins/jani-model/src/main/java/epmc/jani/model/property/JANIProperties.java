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

package epmc.jani.model.property;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonValue;

import epmc.expression.Expression;
import epmc.jani.model.JANIIdentifier;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.modelchecker.Properties;
import epmc.modelchecker.RawProperty;
import epmc.util.UtilJSON;

// TODO conversion between JANI properties and general properties must be improved

/**
 * Class representing the properties of a JANI model.
 * 
 * @author Ernst Moritz Hahn
 */
public final class JANIProperties implements JANINode, Properties {
    /** String used for naming unnamed properties as DEFAULT_NAME_num */
    private final static String DEFAULT_NAME = "Property_%d";

    /** Model to which the properties belong. */
    private ModelJANI model;
    private Map<String, ? extends JANIIdentifier> validIdentifiers;
    /** Properties stored in this properties object, transformed form. */
    private final Map<String,JANIPropertyEntry> properties = new LinkedHashMap<>();

    @Override
    public void setModel(ModelJANI model) {
        this.model = model;
    }

    @Override
    public ModelJANI getModel() {
        return model;
    }

    public void setValidIdentifiers(Map<String, ? extends JANIIdentifier> identifiers) {
        this.validIdentifiers = identifiers;
    }

    @Override
    public JANINode parse(JsonValue value) {
        assert model != null;
        assert value != null;
        assert validIdentifiers != null;
        properties.clear();		
        JsonArray array = UtilJSON.toArrayObject(value);
        for (JsonValue entryValue : array) {
            JANIPropertyEntry entry = new JANIPropertyEntry();
            entry.setModel(model);
            entry.setValidIdentifiers(validIdentifiers);
            entry.parse(entryValue);
            properties.put(entry.getName(), entry);
        }
        return this;
    }

    @Override
    public JsonValue generate() {
        assert validIdentifiers != null;
        assert model != null;
        assert properties != null;
        JsonArrayBuilder result = Json.createArrayBuilder();
        for (JANIPropertyEntry property : properties.values()) {
            result.add(property.generate());
        }
        return result.build();
    }

    @Override
    public String toString() {
        return UtilModelParser.toString(this);
    }

    public void addProperty(String name, Expression property, String comment) {
        assert property != null;
        if (name == null) {
            int propertyNumber = 0;
            do {
                name = String.format(DEFAULT_NAME, propertyNumber);
                propertyNumber++;
            } while (properties.containsKey(name));
        }
        JANIPropertyEntry janiProperty = new JANIPropertyEntry();
        janiProperty.setModel(model);
        janiProperty.setValidIdentifiers(validIdentifiers);
        janiProperty.setExpression(property);
        janiProperty.setName(name);
        janiProperty.setComment(comment);
        properties.put(name, janiProperty);
    }

    @Override
    public void parseProperties(Object part, InputStream... inputs) {
        assert inputs != null;
        for (InputStream input : inputs) {
            assert input != null;
        }
        assert false;
    }

    @Override
    public List<RawProperty> getRawProperties() {
        List<RawProperty> rawProperties = new ArrayList<>();
        for (JANIPropertyEntry entry : this.properties.values()) {
            RawProperty raw = new RawProperty();
            raw.setName(entry.getName());
            rawProperties.add(raw);
        }
        return rawProperties;
    }

    List<JANIPropertyEntry> getJANIProperties() {
        List<JANIPropertyEntry> janiProperties = new ArrayList<>(properties.values().size());
        for (JANIPropertyEntry entry : this.properties.values()) {
            janiProperties.add(entry);
        }
        return janiProperties;
    }

    @Override
    public Expression getParsedProperty(RawProperty property) {
        return model.replaceConstants(properties.get(property.getName()).getExpression());
    }
}
