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

package epmc.jani.extensions.functions;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.jani.model.Automaton;
import epmc.jani.model.JANIIdentifier;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelExtension;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.expression.JANIExpression;
import epmc.util.UtilJSON;

public final class ModelExtensionFunctions implements ModelExtension {
    public final static String IDENTIFIER = "functions";
    private final static String FUNCTIONS = "functions";
    private ModelJANI model;
    private JANINode node;
    private JsonValue value;
    private JANIFunctions modelFunctions;
    private final Map<Automaton,JANIFunctions> automatonFunctions = new LinkedHashMap<>();
    private Map<String, ? extends JANIIdentifier> identifiers;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setModel(ModelJANI model) {
        this.model = model;
    }

    @Override
    public void setIdentifiers(Map<String, ? extends JANIIdentifier> identifiers) {
        this.identifiers = identifiers;
    }
    
    @Override
    public void setNode(JANINode node) {
        this.node = node;
    }

    @Override
    public void parseBefore() {
        if (model != null) {
            Map<String, Class<? extends JANIExpression>> expressionsClasses = model.getExpressionClasses();
            expressionsClasses.put(JANIExpressionOperatorCall.IDENTIFIER, JANIExpressionOperatorCall.class);
        }
    }

    @Override
    public void parseAfter() {
        if (!(node instanceof ModelJANI)
                && !(node instanceof Automaton)) {
            return;
        }
        assert value instanceof JsonObject : value;
        JsonObject object = (JsonObject) value;
        JsonArray jsonFunctions = UtilJSON.getArrayObjectOrNull(object, FUNCTIONS);
        if (jsonFunctions == null) {
            return;
        }
        JANIFunctions functions;
        functions = new JANIFunctions();
        functions.setIdentifiers(identifiers);
        functions.setModel(model);
        if (node instanceof Automaton) {
            functions.setAutomaton((Automaton) node);
        }
        functions.parse(jsonFunctions);
        if (node instanceof ModelJANI) {
            modelFunctions = functions;
        } else if (node instanceof Automaton) {
            automatonFunctions.put((Automaton) node, functions);
        } else {
            assert false;
        }
    }
    
    @Override
    public void setJsonValue(JsonValue value) {
        this.value = value;
    }
    
    @Override
    public void generate(JsonObjectBuilder generate) {
        if (node instanceof ModelJANI) {
            generate.add(FUNCTIONS, modelFunctions.generate());
        } else if (node instanceof Automaton) {
            generate.add(FUNCTIONS, automatonFunctions.get(node).generate());
        } else {
            assert false;
        }
    }
    
    public JANIFunctions getModelFunctions() {
        return modelFunctions;
    }
    
    public Map<Automaton, JANIFunctions> getAutomatonFunctions() {
        return automatonFunctions;
    }
}
