package epmc.jani.extensions.functions;

import java.util.ArrayList;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonValue;

import epmc.jani.model.Automaton;
import epmc.jani.model.JANIIdentifier;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.util.UtilJSON;

public final class JANIFunctions implements JANINode {
    private ModelJANI model;
    private Map<String, ? extends JANIIdentifier> identifiers;
    private ArrayList<JANIFunction> functions;
    private Automaton automaton;

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
    
    public void setAutomaton(Automaton automaton) {
        this.automaton = automaton;
    }

    @Override
    public JANINode parse(JsonValue value) {
        JsonArray array = UtilJSON.toArray(value);
        ArrayList<JANIFunction> functions = new ArrayList<>();
        for (JsonValue fn : array) {
            JANIFunction function = new JANIFunction();
            function.setIdentifiers(identifiers);
            function.setModel(model);
            function.setAutomaton(automaton);
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
    
    public ArrayList<JANIFunction> getFunctions() {
        return functions;
    }
    
    @Override
    public String toString() {
        return UtilModelParser.toString(this);
    }
}
