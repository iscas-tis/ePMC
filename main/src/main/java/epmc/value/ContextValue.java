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

package epmc.value;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import epmc.util.Util;

/**
 * Value context.
 * This class is used to obtain and store {@link Type}s, to maintain shortcuts
 * to create {@link Value} objects of frequently used types, etc.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ContextValue {
    /** String to indicate unchecked method. */
    private final static String UNCHECKED = "unchecked";
    /** Map from operator identifier to according operator. */
    private final Map<String,Operator> identifierToOperator = new LinkedHashMap<>();
    /** Unmodifiable map from operator identifier to according operator. */
    private final Map<String,Operator> identifierToOperatorExternal = Collections.unmodifiableMap(identifierToOperator);
    private final List<OperatorEvaluator> operatorEvaluators = new LinkedList<>();
    private final List<OperatorEvaluator> operatorEvaluatorsReversed = Lists.reverse(operatorEvaluators);
    private final List<OperatorEvaluator> operatorEvaluatorsExternal = Collections.unmodifiableList(operatorEvaluatorsReversed);
    /** Map from identifying objects to types. */
    private final Map<Object,Type> types = new HashMap<>();
    /** Map used to make types unique. */
    private final Map<Type,Type> typesUnique = new HashMap<>();
    /** The value context used in the model checking process. */
	private static ContextValue contextValue;

    public static void set(ContextValue contextValue) {
    	ContextValue.contextValue = contextValue;
    }
    
    public static ContextValue get() {
    	return contextValue;
    }
    
    /**
     * Get type identified by object.
     * Obtain the type previously set by
     * {@link #setType(Object, Type)}
     * using an object for which {@link Object#equals(Object)}
     * returns {@code true}.
     * This way, certain types e.g. for integers, reals, etc. can be stored
     * in the value context and later retrieved.
     * The object key parameter must not be {@code null}.
     * 
     * @param key object identifying the type to get
     * @return type identified by object, or {@code null}
     */
    public <T extends Type> T getType(Object key) {
        assert key != null;
        @SuppressWarnings(UNCHECKED)
        T result = (T) types.get(key);
        return result;
    }
    
    /**
     * Sets an object to identify a given type.
     * The type can later be obtained using {@link #getType(Object)}.
     * This way, certain types e.g. for integers, reals, etc. can be stored
     * in the value context and later retrieved.
     * The object key and type parameter must not be {@code null}.
     * 
     * @param key object identifying the type to set
     * @param type type to be identified by the given object key
     */
    public <T extends Type> void setType(Object key, T type) {
        assert key != null;
        assert type != null;
        types.put(key, makeUnique(type));
    }
    
    /**
     * Returns a unique instance of the given type object.
     * The method checks whether there already exists a type for which
     * {@link #makeUnique(Type)}
     * was called which has the same value for
     * {@link Type#hashCode()}
     * and for which
     * {@link Type#equals(Object)}
     * returns {@code true}.
     * In this case, the type object already known is returned.
     * Otherwise, the type parameter is stored and returned.
     * The type parameter must not be {@code null}.
     * 
     * @param type type to unify
     * @return unified type
     */
    public <T extends Type> T makeUnique(T type) {
        assert type != null;
        @SuppressWarnings(UNCHECKED)
        T result = (T) typesUnique.get(type);
        if (result == null) {
            typesUnique.put(type, type);
            result = type;
        }
        return result;
    }
    
    // TODO get rid of this function
    /**
     * Get an operator by its identifier. 
     * The identifier parameter must not be {@code null}. The identifier must
     * be one an identifier of one of the operators known to the value context.
     * 
     * @param identifier identifier to obtain operator of
     * @return operator with the given identifier
     */
    public Operator getOperator(String identifier) {
        assert identifier != null;
        assert identifierToOperator.containsKey(identifier) : identifier;
        Operator result = identifierToOperator.get(identifier);
        return result;
    }
    
    // TODO get rid of this function
    /**
     * Get a map from operator identifiers to operators.
     * The map returned is write protected, any attempt to modify it will lead
     * to an exception.
     * 
     * @return map from operator identifiers to operators
     */
    public Map<String, Operator> getOperators() {
        return identifierToOperatorExternal;
    }

    // TODO get rid of this function
    /**
     * Add or replace operator.
     * 
     * @param clazz class of operator to add or replace
     */
    public void addOrSetOperator(String name, Class<? extends Operator> clazz) {
    	assert clazz != null;
        Operator operator = Util.getInstance(clazz);
        if (this.identifierToOperator.containsKey(name)) {
            this.identifierToOperator.put(name, operator);
        } else {
            this.identifierToOperator.put(name, operator);
        }
    }
    
    public void addOperatorEvaluator(Class<? extends OperatorEvaluator> clazz) {
    	assert clazz != null;
    	OperatorEvaluator evaluator = Util.getInstance(clazz);
    	operatorEvaluators.add(evaluator);
    }
    
    public List<OperatorEvaluator> getOperatorEvaluators() {
		return operatorEvaluatorsExternal;
	}
    
    public OperatorEvaluator findOperatorEvaluator(String operator, Type...types) {
    	assert operator != null;
    	assert types != null;
    	for (Type type : types) {
    		assert type != null;
    	}
    	for (OperatorEvaluator evaluator : operatorEvaluatorsReversed) {
    		if (evaluator.canApply(operator, types)) {
    			return evaluator;
    		}
    	}
    	return null;
    }
}
