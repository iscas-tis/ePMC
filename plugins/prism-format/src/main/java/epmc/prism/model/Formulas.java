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

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.jani.model.type.JANIType;
import epmc.jani.model.type.JANITypeBool;
import epmc.prism.error.ProblemsPRISM;
import epmc.prism.expression.PrismExpressionParser;

/**
 * Class to collect the formulas, constants, and labels of a PRISM model during parsing.
 * This class is not meant for general storage of non-PRISM models.
 *
 * @author Ernst Moritz Hahn
 */
public final class Formulas {
    private Map<Expression,Expression> formulas;
    private Map<Expression,Expression> constants;
    private Map<Expression,JANIType> constantTypes;
    private Map<Expression,Expression> labels;

    public Formulas() {
        formulas = new HashMap<>();
        constants = new HashMap<>();
        constantTypes = new HashMap<>();
        labels = new HashMap<>();
    }

    Map<Expression,Expression> getFormulas() {
        return Collections.unmodifiableMap(formulas);
    }

    Map<Expression,Expression> getConstants() {
        return Collections.unmodifiableMap(constants);
    }

    Map<Expression,JANIType> getConstantTypes() {
        return Collections.unmodifiableMap(constantTypes);
    }

    public Map<Expression,Expression> getLabels() {
        return Collections.unmodifiableMap(labels);
    }

    Map<Expression,Expression> getAll() {
        Map<Expression,Expression> result = new HashMap<>();
        result.putAll(formulas);
        result.putAll(constants);
        result.putAll(labels);
        return result;
    }

    public void addFormula(String name, Expression content) {
        assert name != null;
        assert content != null;
        formulas.put(new ExpressionIdentifierStandard.Builder()
                .setName(name)
                .setPositional(content.getPositional())
                .build(), content);
    }

    void addFormula(String name, String content) {
        assert name != null;
        assert content != null;
        formulas.put(new ExpressionIdentifierStandard.Builder()
                .setName(name)
                .build(), parse(content));
    }

    public void addConstant(String variable, Expression value, JANIType type) {
        assert variable != null;
        assert type != null;
        Positional positional = value != null ? value.getPositional() : null;
        Expression constant = new ExpressionIdentifierStandard.Builder()
                .setName(variable)
                .setPositional(positional)
                .build();
        constants.put(constant, value);
        constantTypes.put(constant, type);
    }

    void addConstant(String variable, Object value, JANIType type) {
        assert variable != null;
        assert value != null;
        assert type != null;
        if (value instanceof String) {
            addConstant(variable, parse( (String) value), type);
        } else if (value instanceof Expression) {
            addConstant(variable, (Expression) value, type);            
        } else {
            assert false;
        }
    }

    void addConstant(String variable, String value, JANIType type)
    {
        assert variable != null;
        assert value != null;
        assert type != null;
        addConstant(variable, parse(value), type);
    }

    public JANIType getConstantType(String variable) {
        assert variable != null;
        Expression constant = new ExpressionIdentifierStandard.Builder()
                .setName(variable)
                .build();
        return constantTypes.get(constant);
    }

    public void addLabel(String label, Expression value) {
        assert label != null;
        assert value != null;
        JANITypeBool typeBool = new JANITypeBool();
        constantTypes.put(new ExpressionIdentifierStandard.Builder()
                .setName(label)
                .build(), typeBool);
        labels.put(new ExpressionIdentifierStandard.Builder()
                .setName(label)
                .setPositional(value.getPositional())
                .build(), value);
    }

    void addLabel(String label, String value) {
        assert label != null;
        assert value != null;
        JANITypeBool typeBool = new JANITypeBool();
        constantTypes.put(new ExpressionIdentifierStandard.Builder()
                .setName(label)
                .build(), typeBool);
        labels.put(new ExpressionIdentifierStandard.Builder()
                .setName(label)
                .build(), parse(value));
    }

    void check() {
        checkCyclic();
        checkNonConstantConst();
    }

    /**
     * Checks whether there are cyclic const or formula definitions.
     * 
     */
    private void checkCyclic() {
        for (Entry<Expression, Expression> entry : constants.entrySet()) {
            Set<Expression> seen = new HashSet<>();
            ArrayList<Expression> path = new ArrayList<>();
            Set<Expression> onPath = new HashSet<>();
            ensure(checkCyclic(entry.getValue(), seen, path, onPath), 
                    ProblemsPRISM.CONST_CYCLIC,
                    entry.getKey(), pathToString(path));
        }
        for (Entry<Expression, Expression> entry : formulas.entrySet()) {
            Set<Expression> seen = new HashSet<>();
            ArrayList<Expression> path = new ArrayList<>();
            Set<Expression> onPath = new HashSet<>();
            ensure(checkCyclic(entry.getValue(), seen, path, onPath),
                    ProblemsPRISM.CONST_CYCLIC,
                    new Object[]{entry.getKey(), pathToString(path)});
        }
    }

    private boolean checkCyclic(Expression value, Set<Expression> seen,
            List<Expression> path, Set<Expression> onPath) {
        if (value == null) {
            return true;
        }
        if (onPath.contains(value)) {
            path.add(value);
            return false;
        }
        if (seen.contains(value)) {
            return true;
        }
        path.add(value);
        onPath.add(value);
        seen.add(value);
        for (Expression child : value.getChildren()) {
            if (!checkCyclic(child, seen, path, onPath)) {
                return false;
            }
        }
        if (value instanceof ExpressionIdentifier) {
            Expression innerProp = constants.get(value);            
            if (innerProp == null) {
                innerProp = formulas.get(value);
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

    private String pathToString(ArrayList<Expression> path) {
        StringBuilder builder = new StringBuilder();
        Iterator<Expression> iter = path.iterator();
        while (iter.hasNext()) {
            builder.append(iter.next());
            if (iter.hasNext()) {
                builder.append(" -> ");
            }
        }
        return builder.toString();
    }

    /**
     * Checks for constant definitions which do not evaluate to constant values.
     * 
     */
    private void checkNonConstantConst() {
        Set<Expression> seen = new HashSet<>();
        for (Entry<Expression, Expression> entry : constants.entrySet()) {
            ArrayList<Expression> path = new ArrayList<>();
            Set<Expression> onPath = new HashSet<>();
            Expression constant = checkNonConstantConst(entry.getValue(), seen, path, onPath);
            ensure(constant == null, ProblemsPRISM.CONST_NON_CONST, entry.getKey());
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
            Set<Expression> seen, ArrayList<Expression> path,
            Set<Expression> onPath) {
        if (value == null) {
            return null;
        }
        if (seen.contains(value)) {
            return null;
        }
        path.add(value);
        onPath.add(value);
        seen.add(value);
        for (Expression child : value.getChildren()) {
            Expression foundInner = checkNonConstantConst(child, seen, path, onPath);
            if (foundInner != null) {
                return foundInner;
            }
        }
        if (value instanceof ExpressionIdentifier) {
            if (constants.containsKey(value)) {
                if (constants.get(value) != null) {
                    for (Expression child : constants.get(value).getChildren()) {
                        Expression foundInner = checkNonConstantConst(child, seen, path, onPath);
                        if (foundInner != null) {
                            return foundInner;
                        }
                    }
                }
            } else {
                return value;
            }
        }
        path.remove(path.size() - 1);
        onPath.remove(value);
        return null;
    }

    void expandFormulas() {
        expand(formulas);
    }

    void expandConstants() {
        Map<Expression,Expression> definedConstants = new HashMap<>();
        for (Entry<Expression,Expression> entry : constants.entrySet()) {
            if (entry.getValue() != null) {
                definedConstants.put(entry.getKey(), entry.getValue());
            }
        }
        expand(definedConstants);
    }

    private void expand(Map<Expression, Expression> map) {
        Map<Expression,Expression> seen = new HashMap<>();
        for (Entry<Expression,Expression> entry : formulas.entrySet()) {
            Expression newExpr = expand(entry.getValue(), seen, map);
            entry.setValue(newExpr);
        }
        for (Entry<Expression,Expression> entry : constants.entrySet()) {
            Expression newExpr = expand(entry.getValue(), seen, map);
            entry.setValue(newExpr);
        }
        for (Entry<Expression,Expression> entry : labels.entrySet()) {
            Expression newExpr = expand(entry.getValue(), seen, map);
            entry.setValue(newExpr);
        }
    }

    private Expression expand(Expression value, Map<Expression,Expression> seen, Map<Expression, Expression> map) {
        if (value == null) {
            return null;
        }
        if (seen.containsKey(value)) {
            return seen.get(value);
        }

        Expression goDeeper = null;
        if (value instanceof ExpressionIdentifier) {
            goDeeper = map.get(value);
        }
        if (goDeeper != null) {
            Expression expanded = expand(goDeeper, seen, map);
            seen.put(value, expanded);
            return expanded;
        }

        ArrayList<Expression> newChildren = new ArrayList<>();
        for (Expression child : value.getChildren()) {
            newChildren.add(expand(child, seen, map));
        }
        Expression newValue = value.replaceChildren(newChildren);
        seen.put(value, newValue);
        return newValue;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("formulas:\n");
        for (Entry<Expression,Expression> entry : formulas.entrySet()) {
            builder.append("  " + entry.getKey() + "=" + entry.getValue() + "\n");
        }
        builder.append("constants:\n");
        for (Entry<Expression,Expression> entry : constants.entrySet()) {
            builder.append("  " + entry.getKey() + "=" + entry.getValue() + "\n");
        }
        builder.append("labels:\n");
        for (Entry<Expression,Expression> entry : labels.entrySet()) {
            builder.append("  " + entry.getKey() + "=" + entry.getValue() + "\n");
        }

        return builder.toString();
    }

    public boolean isConstantDefined(Expression constant) {
        assert constant != null;
        return constants.get(constant) != null;
    }

    public boolean isConstantDefined(String constant) {
        assert constant != null;
        Expression constantExpr = new ExpressionIdentifierStandard.Builder()
                .setName(constant)
                .build();
        return isConstantDefined(constantExpr);
    }

    public void ensureNoUndefinedConstants() {
        for (Entry<Expression,Expression> entry : constants.entrySet()) {
            ExpressionIdentifierStandard key = (ExpressionIdentifierStandard) entry.getKey();
            ensure(entry.getValue() != null, ProblemsPRISM.CONST_UNDEFINED, key.getName());
        }
    }

    private Expression parse(Reader reader, String string) {
        assert reader != null;
        PrismExpressionParser parser = new PrismExpressionParser(reader);
        return parser.parseExpressionAsProperty(null, 1, 1, string);
    }

    private Expression parse(String string) {
        assert string != null;
        StringReader reader = new StringReader(string);
        return parse(reader, string);
    }
}
