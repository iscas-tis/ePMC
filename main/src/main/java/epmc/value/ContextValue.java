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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import epmc.operator.Operator;

import static epmc.error.UtilError.ensure;

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
    
    private final List<OperatorEvaluatorFactory> operatorEvaluatorFactories = new LinkedList<>();
    private final List<OperatorEvaluatorFactory> operatorEvaluatorFactoriesReversed = Lists.reverse(operatorEvaluatorFactories);
    /** Map from identifying objects to types. */
    private final Map<Object,Type> types = new HashMap<>();
    /** Map used to make types unique. */
    private final Map<Type,Type> typesUnique = new HashMap<>();
    /** The value context used in the model checking process. */
    private static ContextValue CONTEXT_VALUE;

    public static void set(ContextValue contextValue) {
        ContextValue.CONTEXT_VALUE = contextValue;
    }

    public static ContextValue get() {
        return CONTEXT_VALUE;
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

    public void addEvaluatorFactory(OperatorEvaluatorFactory factory) {
        assert factory != null;
        operatorEvaluatorFactories.add(factory);
    }
    
    public OperatorEvaluator getEvaluator(Operator operator, Type...types) {
        OperatorEvaluator result = getEvaluatorOrNull(operator, types);
        ensure(result != null, ProblemsValue.OPTIONS_NO_OPERATOR_AVAILABLE, operator, Arrays.toString(types));
        return result;
    }

    public OperatorEvaluator getEvaluatorOrNull(Operator operator, Type...types) {
        assert operator != null;
        assert types != null;
        for (Type type : types) {
            assert type != null;
        }
        for (OperatorEvaluatorFactory factory : operatorEvaluatorFactoriesReversed) {
            OperatorEvaluator evaluator = factory.getEvaluator(operator, types);
            if (evaluator != null) {
                return evaluator;
            }
        }
        return null;
    }
}
