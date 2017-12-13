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

package epmc.prism.model;

import static epmc.error.UtilError.ensure;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.modelchecker.Properties;
import epmc.modelchecker.Property;
import epmc.modelchecker.RawProperties;
import epmc.modelchecker.RawProperty;
import epmc.modelchecker.UtilModelChecker;
import epmc.modelchecker.error.ProblemsModelChecker;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.options.UtilOptions;
import epmc.prism.model.convert.PRISM2JANIConverter;
import epmc.value.Type;

// TODO constants should not be stored here, but in a separate place in model

public final class PropertiesImpl implements Properties {
    private final Map<RawProperty,Expression> properties = new LinkedHashMap<>();
    private final Set<String> names = new LinkedHashSet<>();
    private final Map<String,Expression> constants = new LinkedHashMap<>();
    private final Map<String,Type> constantTypes = new LinkedHashMap<>();
    private final Map<String,Expression> formulas = new LinkedHashMap<>();
    private final Map<String,Expression> labels = new LinkedHashMap<>();
    private final ModelPRISM model;

    public PropertiesImpl(ModelPRISM model) {
        assert model != null;
        this.model = model;
        // TODO check duplicates
    }

    @Override
    public void parseProperties(Object part, InputStream... inputs) {
        for (InputStream input : inputs) {
            parseProperties(part, input);
        }
    }

    private void parseProperties(Object part, InputStream input) {
        assert input != null;
        Property property = UtilOptions.getInstance(OptionsModelChecker.PROPERTY_INPUT_TYPE);
        RawProperties properties = new RawProperties();
        property.readProperties(part, properties, input);
        parseProperties(part, properties);
        expand();
    }

    private void parseProperties(Object part, RawProperties rawProperties) {
        Options options = Options.get();
        Map<String,Object> optionsConsts = options.getMap(OptionsModelChecker.CONST);
        if (optionsConsts == null) {
            optionsConsts = new LinkedHashMap<>();
        }
        for (RawProperty prop : rawProperties.getProperties()) {
            String definition = prop.getDefinition();
            if (definition == null) {
                continue;
            }
            Expression parsed = UtilModelChecker.parseExpression(definition);
            parsed = PRISM2JANIConverter.useOnlyNamedRewards(model, parsed);
            properties.put(prop, parsed);
        }
        for (Entry<String,String> entry : rawProperties.getConstants().entrySet()) {
            String name = entry.getKey();
            Object definition = entry.getValue();
            if (definition == null) {
                definition = optionsConsts.get(name);
            }
            Expression expr = null;
            if (definition != null && definition instanceof String) {
                expr = UtilModelChecker.parseExpression(((String) definition));
            } else if (definition != null && definition instanceof Expression) {
                expr = (Expression) definition;
            } else if (definition != null) {
                assert false : definition;
            }
            constants.put(name, expr);
            Type type = UtilModelChecker.parseType(part, rawProperties.getConstantType(name));
            assert type != null;
            constantTypes.put(name, type);
        }
        for (Entry<String,String> entry : rawProperties.getLabels().entrySet()) {
            String name = entry.getKey();
            String definition = entry.getValue();
            Expression expr = null;
            if (definition != null) {
                expr = UtilModelChecker.parseExpression(definition);
            }
            labels.put(name, expr);
        }
    }

    public void addProperty(RawProperty prop, Expression parsed) {
        assert prop != null;
        assert parsed != null;
        properties.put(prop, parsed);
        expand();
    }

    public void addConst(String name, Type type, Expression entry) {
        assert name != null;
        assert type != null;
        // entry might be null for undefined constants
        ensure(constants.get(name) == null, ProblemsModelChecker.DEFINED_TWICE, name);
        this.constants.put(name, entry);
        this.constantTypes.put(name, type);
        this.names.add(name);
    }

    public void addFormula(String name, Expression entry)
    {
        assert name != null;
        assert entry != null;
        ensure(!names.contains(name), ProblemsModelChecker.DEFINED_TWICE, name);
        this.formulas.put(name, entry);
        this.names.add(name);
    }

    public void addLabel(String name, Expression entry)
    {
        assert name != null;
        assert entry != null;
        ensure(!names.contains(name), ProblemsModelChecker.DEFINED_TWICE, name);
        this.labels.put(name, entry);
        this.names.add(name);
    }

    @Override
    public Expression getParsedProperty(RawProperty property) {
        return properties.get(property);
    }

    public void expandAndCheckWithDefinedCheck() {
        checkCyclic();
        expand();
        checkNonConstantConst();
        checkUndefinedConst();
    }

    // private methods

    private void expand() {
        Map<Expression,Expression> seen = new HashMap<>();
        for (Entry<RawProperty,Expression> entry : properties.entrySet()) {
            Expression newExpr = expand(entry.getValue(), seen);
            properties.put(entry.getKey(), newExpr);
        }
    }

    private Expression expand(Expression value, Map<Expression,Expression> seen) {
        if (value == null) {
            return null;
        }
        if (seen.containsKey(value)) {
            return seen.get(value);
        }

        Expression goDeeper = null;
        if (ExpressionIdentifier.is(value)) {
            ExpressionIdentifier valueId = (ExpressionIdentifier) value;
            if (goDeeper == null) {
                goDeeper = constants.get(valueId.toString());
            }
            if (goDeeper == null) {
                goDeeper = formulas.get(valueId.toString());
            }
            if (goDeeper == null) {
                goDeeper = labels.get(valueId.toString());
            }
        }        
        if (goDeeper != null) {
            Expression expanded = expand(goDeeper, seen);
            seen.put(value, expanded);
            return expanded;
        }

        List<Expression> newChildren = new ArrayList<>();
        for (Expression child : value.getChildren()) {
            newChildren.add(expand(child, seen));
        }
        Expression newValue = value.replaceChildren(newChildren);
        seen.put(value, newValue);
        return newValue;
    }


    private void checkUndefinedConst() {
        Set<String> usedConstants = new LinkedHashSet<>();
        Set<Expression> seen = new HashSet<>();
        for (Expression expr : properties.values()) {
            findUsedConstants(expr, seen, usedConstants);
        }
        for (String name : usedConstants) {
            Expression constDef = constants.get(name);
            ensure(constDef != null, ProblemsModelChecker.CONST_UNDEFINED, name);
        }
    }

    private void findUsedConstants(Expression value, Set<Expression> seen,
            Set<String> usedConstants) {
        if (value == null) {
            return;
        }
        if (seen.contains(value)) {
            return;
        }
        seen.add(value);
        if (constants.containsKey(value.toString())) {
            Expression asConst = value;
            ExpressionIdentifierStandard asConstId = (ExpressionIdentifierStandard) asConst;
            usedConstants.add(asConstId.getName());
        } else {
            for (Expression child : value.getChildren()) {
                findUsedConstants(child, seen, usedConstants);
            }
        }
    }

    /**
     * Checks for constant definitions which do not evaluate to constant values.
     * 
     */
    private void checkNonConstantConst() {
        Set<Expression> seen = new HashSet<>();
        for (Entry<String, Expression> entry : constants.entrySet()) {
            ArrayList<String> path = new ArrayList<>();
            Set<Expression> onPath = new HashSet<>();
            Expression constant = checkNonConstantConst(entry.getValue(), seen, path, onPath);
            ensure(constant == null, ProblemsModelChecker.CONST_NON_CONST, entry.getKey());
        }
    }

    /**
     * Checks whether property involves a const which is not actually const.
     * 
     * @param value property to check
     * @param seen set of already checked constants
     * @param path
     * @param onPath
     * @return non-constant entry on path which is non-constant
     */
    private Expression checkNonConstantConst(Expression value,
            Set<Expression> seen, ArrayList<String> path,
            Set<Expression> onPath) {
        if (value == null) {
            return null;
        }
        if (seen.contains(value)) {
            return null;
        }
        path.add(value.toString());
        onPath.add(value);
        seen.add(value);
        for (Expression child : value.getChildren()) {
            Expression foundInner = checkNonConstantConst(child, seen, path, onPath);
            if (foundInner != null) {
                return foundInner;
            }
        }
        if (value instanceof ExpressionIdentifier) {
            ExpressionIdentifierStandard asIdentifier = (ExpressionIdentifierStandard) value;
            String name = asIdentifier.getName();
            Expression innerProp = constants.get(name);
            if (constants.containsKey(name)) {
                Expression foundInner = checkNonConstantConst(innerProp, seen, path, onPath);
                if (foundInner != null) {
                    return foundInner;
                }
            } else {
                innerProp = formulas.get(name);
                if (innerProp != null) {
                    Expression foundInner = checkNonConstantConst(innerProp, seen, path, onPath);
                    if (foundInner != null) {
                        return foundInner;
                    }
                } else {
                    return asIdentifier;
                }
            }
        }
        path.remove(path.size() - 1);
        onPath.remove(value);
        return null;
    }

    /**
     * Checks whether there are cyclic constants or formula definitions.
     * 
     */
    private void checkCyclic() {
        for (Entry<String, Expression> entry : constants.entrySet()) {
            Set<Expression> seen = new HashSet<>();
            ArrayList<String> path = new ArrayList<>();
            Set<Expression> onPath = new HashSet<>();
            ensure(checkCyclic(entry.getValue(), seen, path, onPath),
                    ProblemsModelChecker.CONST_CYCLIC, entry.getKey(), pathToString(path));
        }
        for (Entry<String, Expression> entry : formulas.entrySet()) {
            Set<Expression> seen = new HashSet<>();
            ArrayList<String> path = new ArrayList<>();
            Set<Expression> onPath = new HashSet<>();
            ensure(checkCyclic(entry.getValue(), seen, path, onPath),
                    ProblemsModelChecker.CONST_CYCLIC, entry.getKey(), pathToString(path));
        }
    }

    private String pathToString(ArrayList<String> path) {
        StringBuilder builder = new StringBuilder();
        Iterator<String> iter = path.iterator();
        while (iter.hasNext()) {
            builder.append(iter.next());
            if (iter.hasNext()) {
                builder.append(" -> ");
            }
        }
        return builder.toString();
    }

    private boolean checkCyclic(Expression value, Set<Expression> seen,
            List<String> path, Set<Expression> onPath) {
        if (value == null) {
            return true;
        }
        if (onPath.contains(value)) {
            path.add(value.toString());
            return false;
        }
        if (seen.contains(value)) {
            return true;
        }
        path.add(value.toString());
        onPath.add(value);
        seen.add(value);
        for (Expression child : value.getChildren()) {
            if (!checkCyclic(child, seen, path, onPath)) {
                return false;
            }
        }
        if (value instanceof ExpressionIdentifier) {
            ExpressionIdentifierStandard asIdentifier = (ExpressionIdentifierStandard) value;
            String name = asIdentifier.getName();
            Expression innerProp = null;
            if (innerProp == null) {
                innerProp = constants.get(name);
            }
            if (innerProp == null) {
                innerProp = formulas.get(name);
            }
            if (innerProp == null) {
                innerProp = labels.get(name);
            }
            if (innerProp != null) {
                if (!checkCyclic(innerProp, seen, path, onPath)) {
                    return false;
                }
            }
        }
        path.remove(path.size() - 1);
        onPath.remove(value);
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("constants:\n");
        for (Entry<String, Expression> entry : constants.entrySet()) {
            builder.append(entry.getKey() + " = " + entry.getValue() + "\n");
        }
        builder.append("formulas:\n");
        for (Entry<String, Expression> entry : formulas.entrySet()) {
            builder.append(entry.getKey() + " = " + entry.getValue() + "\n");
        }
        builder.append("labels:\n");
        for (Entry<String, Expression> entry : labels.entrySet()) {
            builder.append(entry.getKey() + " = " + entry.getValue() + "\n");
        }
        builder.append("properties:\n");
        for (RawProperty prop : properties.keySet()) {
            builder.append(prop + " " + properties.get(prop) + "\n");
        }
        return builder.toString();
    }

    public void ensureNoUndefinedConstants() {
        for (Entry<String, Expression> entry : constants.entrySet()) {
            ensure(entry.getValue() != null, ProblemsModelChecker.CONST_UNDEFINED, entry.getKey());
        }
    }

    public Map<String, Expression> getConstants() {
        return constants;
    }

    public Type getConstantType(String constant) {
        assert constant != null;
        return constantTypes.get(constant);
    }

    public Expression getConstantValue(String constant) {
        assert constant != null;
        return constants.get(constant);
    }

    @Override
    public List<RawProperty> getRawProperties() {
        return Collections.list(Collections.enumeration(properties.keySet()));
    }
}
