package epmc.jani.extensions.functions;

import java.util.ArrayList;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonValue;

import epmc.jani.model.JANIIdentifier;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.util.UtilJSON;

public final class JANIFunctions implements JANINode {
    private ModelJANI model;
    private Map<String, ? extends JANIIdentifier> identifiers;
    private ArrayList<JANIFunction> functions;

    @Override
    public void setModel(ModelJANI model) {
        this.model = model;
    }

    @Override
    public ModelJANI getModel() {
        return model;
    }
    
    public void setIdentifiers(Map<String, ? extends JANIIdentifier> identifiers) {
        this.identifiers = identifiers;
    }

    @Override
    public JANINode parse(JsonValue value) {
        JsonArray array = UtilJSON.toArray(value);
        ArrayList<JANIFunction> functions = new ArrayList<>();
        for (JsonValue fn : array) {
            JANIFunction function = new JANIFunction();
            function.setIdentifiers(identifiers);
            function.setModel(model);
            if (function.parse(fn) == null) {
                return null;
            }
            functions.add(function);
        }
        this.functions = functions;
        
        return this;
    }
    @Override
    public JsonValue generate() {
        JsonArrayBuilder result = Json.createArrayBuilder();
        for (JANIFunction function : functions) {
            result.add(function.generate());
        }
        return result.build();
    }
}
