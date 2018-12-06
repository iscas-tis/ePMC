package epmc.jani.extensions.functions;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonValue;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.jani.model.Automaton;
import epmc.jani.model.JANIIdentifier;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.jani.model.Variable;
import epmc.jani.model.expression.ExpressionParser;
import epmc.jani.model.type.JANIType;
import epmc.jani.model.type.TypeParser;
import epmc.util.UtilJSON;

public final class JANIFunction implements JANINode {
    private final static String FUNCTION_NAME = "name";
    private final static String FUNCTION_TYPE = "type";
    private final static String PARAMETERS = "parameters";
    private final static String PARAMETER_NAME = "name";
    private final static String PARAMETER_TYPE = "type";
    private final static String BODY = "body";
    private ModelJANI model;
    private Map<String, ? extends JANIIdentifier> identifiers;
    
    private String name;
    private JANIType type;
    private Map<String, JANIIdentifier> parameters;
    private Expression body;
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
    public JANINode parse(JsonValue fn) {
        JsonObject function = UtilJSON.toObject(fn);
        name = UtilJSON.getIdentifier(function, FUNCTION_NAME);
        JsonValue jsonType = UtilJSON.get(function, FUNCTION_TYPE);
        TypeParser typeParser = new TypeParser();
        typeParser.setModel(model);
        typeParser.parse(jsonType);
        type = typeParser.getType();
        Map<String, JANIIdentifier> identifiers = new LinkedHashMap<>();
        identifiers.putAll(this.identifiers);
        parameters = parseParameters(function, this.identifiers);
        identifiers.putAll(parameters);
        body = ExpressionParser.parseExpression(model, UtilJSON.get(function, BODY), identifiers);
        return this;
    }

    private Map<String, JANIIdentifier> parseParameters(JsonObject function,
            Map<String, ? extends JANIIdentifier> otherIdentifiers) {
        Map<String, JANIIdentifier> result = new LinkedHashMap<>();
        TypeParser typeParser = new TypeParser();
        typeParser.setModel(model);
        JsonArray parameters = UtilJSON.getArrayObject(function, PARAMETERS);
        for (JsonValue p : parameters) {
            JsonObject parameter = (JsonObject) p;
            String paramName = UtilJSON.getString(parameter, PARAMETER_NAME);
            assert !otherIdentifiers.containsKey(paramName); // TODO should ensure
            assert !result.containsKey(paramName); // TODO should ensure
            JsonValue jsonType = UtilJSON.get(parameter, PARAMETER_TYPE);
            typeParser.parse(jsonType);
            JANIType pType = typeParser.getType();
            Variable variable = new Variable();
            variable.setName(paramName);
            variable.setType(pType);
            variable.setIdentifier(new ExpressionIdentifierStandard.Builder()
                    .setName(paramName)
                    .setScope(automaton)
                    .build());
            result.put(paramName, variable);
        }
        return result;
    }

    @Override
    public JsonValue generate() {
        return Json.createObjectBuilder()
                .add(FUNCTION_NAME, name)
                .add(FUNCTION_TYPE, type.generate())
                .add(PARAMETERS, generateParameters())
                .add(BODY, generateBody())
                .build();
    }

    private JsonValue generateParameters() {
        JsonArrayBuilder result = Json.createArrayBuilder();
        for (JANIIdentifier entry : parameters.values()) {
            result.add(Json.createObjectBuilder().add(PARAMETER_NAME, entry.getName())
                    .add(PARAMETER_TYPE, entry.getType().generate()));
        }
        return result.build();
    }
    
    private JsonValue generateBody() {
        return ExpressionParser.generateExpression(model, body);
    }
    
    public String getName() {
        return name;
    }
    
    public JANIType getType() {
        return type;
    }
    
    public Map<String, JANIIdentifier> getParameters() {
        return parameters;
    }

    public Expression getBody() {
        return body;
    }
    
    @Override
    public String toString() {
        return UtilModelParser.toString(this);
    }
}
