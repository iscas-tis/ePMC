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

import static epmc.error.UtilError.ensure;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionType;
import epmc.expression.standard.ExpressionTypeBoolean;
import epmc.expression.standard.ExpressionTypeInteger;
import epmc.expression.standard.ExpressionTypeReal;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.expressionevaluator.ExpressionToType;
import epmc.graph.Semantics;
import epmc.jani.error.ProblemsJANI;
import epmc.jani.model.component.Component;
import epmc.jani.model.component.SystemParser;
import epmc.jani.model.expression.JANIExpression;
import epmc.jani.model.expression.JANIExpressionBool;
import epmc.jani.model.expression.JANIExpressionIdentifier;
import epmc.jani.model.expression.JANIExpressionInt;
import epmc.jani.model.expression.JANIExpressionOperatorBinary;
import epmc.jani.model.expression.JANIExpressionOperatorConstant;
import epmc.jani.model.expression.JANIExpressionOperatorGeneric;
import epmc.jani.model.expression.JANIExpressionOperatorIfThenElse;
import epmc.jani.model.expression.JANIExpressionOperatorUnary;
import epmc.jani.model.expression.JANIExpressionReal;
import epmc.jani.model.property.JANIProperties;
import epmc.jani.model.property.JANIPropertyDeadlock;
import epmc.jani.model.property.JANIPropertyExpressionFilter;
import epmc.jani.model.property.JANIPropertyExpressionPathQuantifier;
import epmc.jani.model.property.JANIPropertyExpressionProbabilityQuantifier;
import epmc.jani.model.property.JANIPropertyExpressionRewardQuantifier;
import epmc.jani.model.property.JANIPropertyExpressionSteadyStateQuantifier;
import epmc.jani.model.property.JANIPropertyExpressionTemporalOperator;
import epmc.jani.model.property.JANIPropertyInitial;
import epmc.jani.model.property.JANIPropertyTimelock;
import epmc.jani.model.type.JANIType;
import epmc.jani.model.type.JANITypeBool;
import epmc.jani.model.type.JANITypeBounded;
import epmc.jani.model.type.JANITypeInt;
import epmc.jani.model.type.JANITypeReal;
import epmc.jani.plugin.StandardJANIOperators;
import epmc.modelchecker.Model;
import epmc.modelchecker.Properties;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.util.OrderedMap;
import epmc.util.Util;
import epmc.util.UtilJSON;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.UtilValue;

/**
 * Model class for JANI models.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ModelJANI implements Model, JANINode, ExpressionToType {
    /** Identifier of this model class. */
    public final static String IDENTIFIER = "jani";
    /** Identifies the part of a model where its system components are given. */
    private final static String SYSTEM = "system";
    /** Identifies the part of a model where its automata are specified. */
    private final static String AUTOMATA = "automata";
    /** Identifies the part of a model where its properties are specified. */
    private final static String PROPERTIES = "properties";
    /** Identifies the variable declaration part of a model. */
    private final static String VARIABLES = "variables";
    /** Identifies the action set of a model. */
    private final static String ACTIONS = "actions";
    /** Name of the model (e.g. filename). */
    private final static String NAME = "name";
    /** JANI version. */
    private final static String JANI_VERSION = "jani-version";
    /** Identifies the semantics type of a model. */
    private final static String TYPE = "type";
    /** Empty string. */
    private final static String EMPTY = "";
    /** String denoting model features (extensions) field. */
    private final static String FEATURES = "features";
    /** Denotes metadata field. */
    private final static String METADATA = "metadata";
    /** Initial assignment to global variables. */
    private final static String RESTRICT_INITIAL = "restrict-initial";
    /** Denotes model constants. */
    private final static String CONSTANTS = "constants";

    /** Map from semantics identifiers to their semantics extension classes. */
    private Map<String, Class<? extends ModelExtensionSemantics>> janiToSemantics;
    /** Model extensions used, excluding the one handling the model type. */
    private List<ModelExtension> modelExtensions;
    /** Semantics type of the model. */
    private ModelExtensionSemantics semantics;
    /** Properties contained in the model. */
    private JANIProperties properties;
    /** Actions used in this model. */
    private Actions actions;
    /** Global variables of this model. */
    private Variables variables;
    /** Automata specification of this model. */
    private Automata automata;
    ;
    /** System specification of this model. */
    private Component system;
    /** Silent action used for this model. */
    private final Action silentAction = prepareSilentAction();
    /** Name of the model. */
    private String name;
    /** JANI version of the model. */
    private int janiVersion;
    /** Known JANI model semantic types. */
    private Map<String, Class<? extends JANIType>> types = new LinkedHashMap<>();
    /** Known JANI general expression types. */
    private Map<String, Class<? extends JANIExpression>> expressionsClasses = new OrderedMap<>(true);
    /** Known JANI property expression types. */
    private Map<String, Class<? extends JANIExpression>> propertyClasses = new LinkedHashMap<>();
    /** Metadata of the model. */
    private Metadata metadata;
    /** Expression about possible initial values of global variables. */
    private InitialStates restrictInitial;
    /** Constants (defined and undefined) of the JANI model. */
    private Constants modelConstants;
    /** Map from defined constants to their value. */
    private Map<Expression,Expression> constants = new LinkedHashMap<>();
    /** Operators available for model. */
    private final JANIOperators operators = new JANIOperators();

    public ModelJANI() {
        StandardJANIOperators.add(operators);
        Options options = Options.get();
        janiToSemantics = options.get(OptionsJANIModel.JANI_MODEL_EXTENSION_SEMANTICS);
        prepareStandardTypes();
        prepareStandardExpressions();
        prepareStandardProperties();
    }

    private Action prepareSilentAction() {
        Action silentAction = new Action();
        silentAction.setModel(this);
        silentAction.setName(EMPTY);
        return silentAction;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    /**
     * Prepare types available in JANI model without extensions.
     * This also excludes types only available for specific semantic types, such as the
     * clocks in timed automata.
     */
    private void prepareStandardTypes() {
        types.put(JANITypeBool.IDENTIFIER, JANITypeBool.class);
        types.put(JANITypeInt.IDENTIFIER, JANITypeInt.class);
        types.put(JANITypeReal.IDENTIFIER, JANITypeReal.class);
        types.put(JANITypeBounded.IDENTIFIER, JANITypeBounded.class);
    }

    /**
     * Prepare standard expression types in JANI models without extensions.
     * This excludes expression types only available in certain semantical
     * types, such as the sampling expressions in stochastic hybrid automata.
     * The expressions for properties are prepared in
     * {@link #prepareStandardProperties()} instead.
     */
    private void prepareStandardExpressions() {
        expressionsClasses.put(JANIExpressionOperatorGeneric.IDENTIFIER, JANIExpressionOperatorGeneric.class);
        expressionsClasses.put(JANIExpressionOperatorIfThenElse.IDENTIFIER, JANIExpressionOperatorIfThenElse.class);
        expressionsClasses.put(JANIExpressionOperatorUnary.IDENTIFIER, JANIExpressionOperatorUnary.class);
        expressionsClasses.put(JANIExpressionOperatorBinary.IDENTIFIER, JANIExpressionOperatorBinary.class);
        expressionsClasses.put(JANIExpressionOperatorConstant.IDENTIFIER, JANIExpressionOperatorConstant.class);
        expressionsClasses.put(JANIExpressionBool.IDENTIFIER, JANIExpressionBool.class);
        expressionsClasses.put(JANIExpressionInt.IDENTIFIER, JANIExpressionInt.class);
        expressionsClasses.put(JANIExpressionReal.IDENTIFIER, JANIExpressionReal.class);
        expressionsClasses.put(JANIExpressionIdentifier.IDENTIFIER, JANIExpressionIdentifier.class);
    }

    /**
     * Prepare property expressions of JANI model without extensions.
     */
    private void prepareStandardProperties() {
        propertyClasses.put(JANIPropertyInitial.IDENTIFIER, JANIPropertyInitial.class);
        propertyClasses.put(JANIPropertyDeadlock.IDENTIFIER, JANIPropertyDeadlock.class);
        propertyClasses.put(JANIPropertyTimelock.IDENTIFIER, JANIPropertyTimelock.class);
        propertyClasses.put(JANIPropertyExpressionRewardQuantifier.IDENTIFIER, JANIPropertyExpressionRewardQuantifier.class);
        propertyClasses.put(JANIPropertyExpressionFilter.IDENTIFIER, JANIPropertyExpressionFilter.class);
        propertyClasses.put(JANIPropertyExpressionPathQuantifier.IDENTIFIER, JANIPropertyExpressionPathQuantifier.class);
        propertyClasses.put(JANIPropertyExpressionProbabilityQuantifier.IDENTIFIER, JANIPropertyExpressionProbabilityQuantifier.class);
        propertyClasses.put(JANIPropertyExpressionSteadyStateQuantifier.IDENTIFIER, JANIPropertyExpressionSteadyStateQuantifier.class);
        propertyClasses.put(JANIPropertyExpressionTemporalOperator.IDENTIFIER, JANIPropertyExpressionTemporalOperator.class);
        propertyClasses.putAll(expressionsClasses);
    }

    @Override
    public void read(Object identifier, InputStream... inputs) {
        assert inputs != null;
        for (InputStream input : inputs) {
            assert input != null;
        }
        ensure(inputs.length == 1, ProblemsJANI.JANI_ONE_MODEL_FILE, inputs.length);
        setModel(this);
        parse(UtilJSON.read(inputs[0]));
    }

    @Override
    public void setModel(ModelJANI model) {
        assert model == this;
    }

    @Override
    public ModelJANI getModel() {
        return this;
    }

    @Override
    public JANINode parse(JsonValue value) {
        assert value != null;

        JsonObject object = UtilJSON.toObject(value);
        parseType(object);
        parseFeatures(object);

        /*
        new ParserSimple<Actions>(null, () -> {
            Actions actions = new Actions();
            actions.setModel(this);
            actions.parse(UtilJSON.get(object, "FFF"));
            this.actions = actions;
        });
        */
        
        parseBeforeModelNodeExtensions(this, value, null);
        name = UtilJSON.getString(object, NAME);
        janiVersion = UtilJSON.getInteger(object, JANI_VERSION);
        ensure(janiVersion == 1, ProblemsJANIParser.JANI_PARSER_VERSION_NUMBER_WRONG,
                janiVersion);

        metadata = UtilModelParser.parseOptional(this, Metadata.class, object, METADATA);
        actions = UtilModelParser.parseOptional(this, Actions.class, object, ACTIONS);
        // the silent action is always available
//        actions.addAction(silentAction);

        modelConstants = UtilModelParser.parseOptional(this, Constants.class, object, CONSTANTS);
        constants = computeConstants();
        Map<String, JANIIdentifier> validIdentifiers = new LinkedHashMap<>();
        if (modelConstants != null) {
            validIdentifiers.putAll(modelConstants.getConstants());
        }

        variables = UtilModelParser.parseOptional(this, Variables.class, object, VARIABLES);
        ensure(variables == null || constants == null
                || Collections.disjoint(constants.keySet(), variables.keySet()),
                ProblemsJANIParser.JANI_PARSER_DISJOINT_GLOBALS_CONSTANTS);
        if (variables != null) {
            validIdentifiers.putAll(variables);
        }

        restrictInitial = UtilModelParser.parseOptional(this, () -> {
            InitialStates initialStates = new InitialStates();
            initialStates = new InitialStates();
            initialStates.setIdentifier(validIdentifiers);
            return initialStates;
        }, object, RESTRICT_INITIAL);
        
        properties = UtilModelParser.parseOptional(this, () -> {
                    JANIProperties props = new JANIProperties();
                    props.setModel(this);
                    props.setValidIdentifiers(validIdentifiers);
                    return props;
                }, object, PROPERTIES);
        automata = UtilModelParser.parse(this, Automata.class, object, AUTOMATA);
        SystemParser system = UtilModelParser.parse(this, SystemParser.class, object, SYSTEM);
        this.system = system.getSystemComponent();
        parseAfterModelNodeExtensions(this, value, validIdentifiers);
        return this;
    }

    public Map<Expression, Expression> computeConstants() {
        Map<Expression,Expression> result = new LinkedHashMap<>();
        Map<Expression,Expression> externalConstants = computeExternalConstants();
        if (modelConstants != null) {
            for (Constant constant : modelConstants) {
                String name = constant.getName();
                Expression identifier = new ExpressionIdentifierStandard.Builder()
                        .setName(name)
                        .build();
                Expression internalValue = constant.getValue();			
                Expression externalValue = externalConstants.get(identifier);
                // TODO
                //				assert (internalValue != null) != (externalValue != null)
                //					: constant.getName();
                if (internalValue != null) {
                    result.put(identifier, internalValue);
                } else if (externalConstants != null) {
                    result.put(identifier, externalValue);
                }
            }
        }

        return result;
    }

    private static Map<Expression, Expression> computeExternalConstants() {
        Options options = Options.get();
        Map<String,Object> optionsConsts = options.getMap(OptionsModelChecker.CONST);
        Map<Expression, Expression> result = new LinkedHashMap<>();
        for (Entry<String, Object> entry : optionsConsts.entrySet()) {
            Expression identifier = new ExpressionIdentifierStandard.Builder()
                    .setName(entry.getKey())
                    .build();
            Expression value = null;
            if (entry.getValue() instanceof Expression) {
                value = (Expression) entry.getValue();
            } else if (entry.getValue() instanceof String) {
                value = parseConstant((String) entry.getValue());
            } else {
                assert false;
            }
            result.put(identifier, value);
        }
        return result;
    }

    private static Expression parseConstant(String valueString) {
        assert valueString != null;
        ExpressionType type = null;
        try {
            UtilValue.newValue(TypeBoolean.get(), valueString);
            type = ExpressionTypeBoolean.TYPE_BOOLEAN;
        } catch (EPMCException e) {
        }
        if (type == null) {
            try {
                UtilValue.newValue(TypeInteger.get(), valueString);
                type = ExpressionTypeInteger.TYPE_INTEGER;
            } catch (EPMCException e) {
            }
        }
        if (type == null) {
            try {
                UtilValue.newValue(TypeReal.get(), valueString);
                type = ExpressionTypeReal.TYPE_REAL;
            } catch (EPMCException e) {
            }
        }
        assert type != null; // TODO throw exception rather than assertion
        return new ExpressionLiteral.Builder()
                .setValue(valueString)
                .setType(type)
                .build();
    }

    public void setRestrictInitial(InitialStates initialStates) {
        this.restrictInitial = initialStates;
    }

    public InitialStates getRestrictInitial() {
        return restrictInitial;
    }

    public Expression getInitialStatesExpressionOrTrue() {
        Expression initial;
        if (restrictInitial == null) {
            initial = ExpressionLiteral.getTrue();
        } else {
            initial = restrictInitial.getExp();
        }
        for (Variable variable : getGlobalVariablesOrEmpty()) {
            Expression varInitValue = variable.getInitialValueOrNull();
            if (varInitValue == null || variable.isTransient()) {
                continue;
            }
            Expression varInit = UtilExpressionStandard.opEq(
                    variable.getIdentifier(),
                    varInitValue);
            initial = UtilExpressionStandard.opAnd(initial, varInit);
        }
        return initial;
    }

    private void parseType(JsonObject object) {
        UtilJSON.ensureString(object, TYPE);
        Class<? extends ModelExtensionSemantics> clazz = UtilJSON.toOneOf(object, TYPE, janiToSemantics);
        semantics = Util.getInstance(clazz);
    }

    private void parseFeatures(JsonObject object) {
        assert object != null;
        JsonArray array = UtilJSON.getArrayStringOrNull(object, FEATURES);
        if (array == null) {
            this.modelExtensions = null;
            return;
        }
        this.modelExtensions = new ArrayList<>();
        Map<String,Class<ModelExtension>> modelExtensions =
                Options.get().get(OptionsJANIModel.JANI_MODEL_EXTENSION_CLASS);
        for (JsonValue identifier : array) {
            Class<ModelExtension> extension =
                    UtilJSON.toOneOf(identifier, modelExtensions,
                            ProblemsJANIParser.JANI_PARSER_UNSUPPORTED_FEATURE);
            ModelExtension instance = Util.getInstance(extension);
            instance.setModel(this);
            this.modelExtensions.add(instance);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public JsonValue generate() {
        JsonObjectBuilder result = Json.createObjectBuilder();
        result.add(JANI_VERSION, janiVersion);
        result.add(NAME, name);
        UtilModelParser.addOptional(result, METADATA, metadata);
        result.add(TYPE, semantics.getIdentifier());
        generateExtensions(result);
        UtilModelParser.addOptional(result, ACTIONS, actions);
        UtilModelParser.addOptional(result, CONSTANTS, modelConstants);
        UtilModelParser.addOptional(result, VARIABLES, variables);
        UtilModelParser.addOptional(result, RESTRICT_INITIAL, restrictInitial);
        if (properties != null) {
            result.add(PROPERTIES, properties.generate());
        }
        result.add(AUTOMATA, automata.generate());
        result.add(SYSTEM, system.generate());
        semantics.generate(result);
        if (modelExtensions != null) {
            for (ModelExtension extension : modelExtensions) {
                extension.setNode(this);
                extension.generate(result);
            }
        }
        return result.build();		
    }

    private void generateExtensions(JsonObjectBuilder result) {
        assert result != null;
        if (modelExtensions == null) {
            return;
        }
        JsonArrayBuilder array = Json.createArrayBuilder();
        for (ModelExtension extension : modelExtensions) {
            array.add(extension.getIdentifier());
        }
        result.add(FEATURES, array);
    }

    @Override
    public Semantics getSemantics() {
        return semantics.getSemantics();
    }

    public ModelExtensionSemantics getSemanticsExtension() {
        return semantics;
    }

    public String getSemanticsIdentifier() {
        return semantics.getIdentifier();
    }

    public void setSemantics(String semantics) {
        if (semantics == null) {
            this.semantics = null;
        } else {
            Class<? extends ModelExtensionSemantics> clazz = this.janiToSemantics.get(semantics.toLowerCase());
            assert clazz != null : semantics;
            this.semantics = Util.getInstance(clazz);
            this.semantics.setModel(this);
        }
    }

    @Override
    public Properties getPropertyList() {
        JANIProperties props;
        if (properties == null) {
            props = new JANIProperties();
            props.setModel(this);
        } else {
            props = properties;
        }
        return props;
    }

    public void setProperties(JANIProperties properties) {
        this.properties = properties;
    }

    /**
     * Gets the actions for this model.
     * 
     * @return actions of this model
     */
    public Actions getActionsOrEmpty() {
        Actions result;
        if (actions == null) {
            result = new Actions();
            result.setModel(this);
        } else {
            result = actions;
        }
        return result;
    }

    public Actions getActions() {
        return actions;
    }

    public void setActions(Actions actions) {
        this.actions = actions;
    }

    /**
     * Get the global variables of this model.
     * 
     * @return global variables of this model
     */
    public Variables getGlobalVariablesOrEmpty() {
        Variables result;
        if (variables == null) {
            result = new Variables();
            result.setModel(this);
        } else {
            result = variables;
        }
        return result;
    }

    public Variables getGlobalVariablesNonTransient() {
        Variables result = new Variables();
        for (Variable variable : getGlobalVariablesOrEmpty()) {
            if (!variable.isTransient()) {
                result.addVariable(variable);
            }
        }
        return result;
    }

    public Variables getGlobalVariablesTransient() {
        Variables result = new Variables();
        for (Variable variable : getGlobalVariablesOrEmpty()) {
            if (variable.isTransient()) {
                result.addVariable(variable);
            }
        }
        return result;
    }


    public Variables getGlobalVariables() {
        return variables;
    }

    public void setGlobalVariables(Variables variables) {
        this.variables = variables;
    }

    /**
     * Get automata specification of this model.
     * 
     * @return automata specification of this model
     */
    public Automata getAutomata() {
        return automata;
    }

    public void setAutomata(Automata automata) {
        this.automata = automata;
    }

    public void setSystem(Component system) {
        this.system = system;
    }

    /**
     * Get system specification of this model.
     * 
     * @return system specification of this model
     */
    public Component getSystem() {
        return system;
    }

    @Override
    public String toString() {
        return UtilModelParser.toString(this);
    }

    /**
     * Obtain the silent action of this model.
     * 
     * @return silent action of this model
     */
    public Action getSilentAction() {
        return silentAction;
    }

    public void parseBeforeModelNodeExtensions(JANINode node, JsonValue value,
            Map<String, JANIIdentifier> identifiers) {
        assert node != null;
        semantics.setNode(node);
        semantics.setIdentifiers(identifiers);
        semantics.setJsonValue(value);
        semantics.parseBefore();
        if (modelExtensions == null) {
            return;
        }
        for (ModelExtension extension : modelExtensions) {
            extension.setNode(node);
            extension.setModel(this);
            extension.setIdentifiers(identifiers);
            extension.setJsonValue(value);
            extension.parseBefore();
        }
    }

    public void parseAfterModelNodeExtensions(JANINode node, JsonValue value,
            Map<String, JANIIdentifier> identifiers) {
        assert node != null;
        assert value != null;
        semantics.setNode(node);
        semantics.setIdentifiers(identifiers);
        semantics.setJsonValue(value);
        semantics.parseAfter();
        if (modelExtensions == null) {
            return;
        }
        for (ModelExtension extension : modelExtensions) {
            extension.setNode(node);
            extension.setIdentifiers(identifiers);
            extension.setJsonValue(value);
            extension.parseAfter();
        }
    }

    public Map<String, Class<? extends JANIType>> getTypes() {
        return types;
    }

    public JANIOperators getJANIOperators() {
        return operators;
    }

    public Map<String, Class<? extends JANIExpression>> getExpressionClasses() {
        return expressionsClasses;
    }

    public Map<String, Class<? extends JANIExpression>> getPropertyClasses() {
        return propertyClasses;
    }

    public List<ModelExtension> getModelExtensionsOrEmpty() {
        if (modelExtensions == null) {
            return Collections.emptyList();
        } else {
            return modelExtensions;
        }
    }

    public List<ModelExtension> getModelExtensions() {
        return modelExtensions;
    }

    public void setModelExtensions(List<ModelExtension> modelExtensions) {
        this.modelExtensions = modelExtensions;
    }

    public Map<Expression, Expression> getConstants() {
        return constants;
    }

    public Constants getModelConstants() {
        return modelConstants;
    }

    public Constants getModelConstantsOrEmpty() {
        Constants result;
        if (modelConstants == null) {
            result = new Constants();
            result.setModel(this);
        } else {
            result = modelConstants;
        }
        return result;
    }

    public void setModelConstants(Constants modelConstants) {
        this.modelConstants = modelConstants;
    }

    public Expression replaceConstantsOrNull(Expression expression) {
        if (expression == null) {
            return null;
        }
        return replaceConstants(expression);
    }

    
    public Expression replaceConstants(Expression expression) {
        assert expression != null;
        return UtilExpressionStandard.replace(expression, constants);
    }

    public void setVersion(int janiVersion) {
        this.janiVersion = janiVersion;
    }

    public int getJaniVersion() {
        return janiVersion;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    @Override
    public ModelJANI clone() {
        ModelJANI clone = new ModelJANI();
        JsonValue generated = generate();
        clone.parse(generated);
        return clone;
    }

    public boolean containsUndefinedConstants() {
        for (Constant constant : getModelConstantsOrEmpty()) {
            if (constants.get(constant.getIdentifier()) == null) {
                return true;
            }
        }
        return false;
    }

    public List<String> findUndefinedConstants() {
        ArrayList<String> result = new ArrayList<>();
        for (Constant constant : getModelConstantsOrEmpty()) {
            if (constants.get(constant.getIdentifier()) == null) {
                result.add(constant.getIdentifier().getName());
            }
        }
        return result;
    }
    
    public String findSomeUndefinedConstant() {
        for (Constant constant : getModelConstantsOrEmpty()) {
            if (!constants.containsKey(constant.getIdentifier())) {
                return constant.getName();
            }
        }
        return null;		
    }

    @Override
    public Type getType(Expression expression) {
        assert expression != null;
        Type type = null;
        if (variables != null) {
            type = variables.getType(expression);
        }
        if (type != null) {
            return type;
        }
        if (modelConstants != null) {
            type = modelConstants.getType(expression);
        }
        if (type != null) {
            return type;
        }
        /*
		type = getTypeExternalConstant(expression);
		if (type != null) {
			return type;
		}
         */
        if (automata != null) {
            type = automata.getType(expression);
        }
        if (type != null) {
            return type;
        }
        return null;
    }

    /*
	private Type getTypeExternalConstant(Expression expression) {
		assert expression != null;
		ExpressionIdentifierStandard identifier = ExpressionIdentifierStandard.asIdentifierStandard(expression);
		if (identifier == null) {
			return null;
		}
		if (identifier.getScope() != null) {
			return null;
		}
		Constant constant = modelConstants.getConstants().get(identifier.getName());
		if (constant == null) {
			return null;
		}
		if (constant.gett)
		// TODO Auto-generated method stub
		return null;
	}
     */
}
