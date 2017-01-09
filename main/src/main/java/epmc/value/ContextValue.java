package epmc.value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import epmc.error.EPMCException;
import epmc.options.Options;
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
    /** Maps each operator to a unique and consecutive number. */
    private final transient HashMap<String,Integer> operatorToNumber = new LinkedHashMap<>();
    /** Maps each number to an operator. */
    private final transient List<Operator> numberToOperator = new ArrayList<>();
    /** Map from operator identifier to according operator. */
    private final transient Map<String,Operator> identifierToOperator = new LinkedHashMap<>();
    /** Unmodifiable map from operator identifier to according operator. */
    private final transient Map<String,Operator> identifierToOperatorExternal = Collections.unmodifiableMap(identifierToOperator);
    /** Options used by this context. */
    private final Options options;
    /** Map from identifying objects to types. */
    private final Map<Object,Type> types = new HashMap<>();
    /** Map used to make types unique. */
    private final Map<Type,Type> typesUnique = new HashMap<>();

    /**
     * Create a new context value.
     * The options parameter may not be {@code null}.
     * 
     * @param options parameter of options used by the value context
     * @throws EPMCException thrown in case of problems during construction
     */
    public ContextValue(Options options) throws EPMCException {
        this.options = options;
    }

    /**
     * Get options used for this context.
     * 
     * @return options used for this context
     */
    public Options getOptions() {
        return options;
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
    
    /**
     * Get an operator by its identifier.
     * The identifier of an operator is obtained by
     * {@link Operator#getIdentifier()}. 
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

    /**
     * Get operator with the given number.
     * The operator number must be nonnegative and strictly smaller than the
     * number of operators known to the value context.
     * 
     * @param number number the operator to obtain
     * @return operator with the given number
     */
    public Operator getOperator(int number) {
        assert number >= 0;
        assert number < this.numberToOperator.size();
        return this.numberToOperator.get(number);
    }
    
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
    
    /**
     * Get the number of a given operator.
     * The operator parameter must not be {@code null}. Also, it must be known
     * to this value context. The number returned will be nonnegative and
     * strictly smaller than the number of operators known by this value
     * context.
     * 
     * @param operator operator to get number of
     * @return number of the operator requested
     */
    public int getOperatorNumber(Operator operator) {
        assert operator != null;
        return operatorToNumber.get(operator.getIdentifier());
    }

    /**
     * Add or replace operator.
     * If an operator with the same identifier
     * (as by {@link Operator#getIdentifier()}) already exists, it will be
     * replaced by the new operator. It will also be assigned the same number
     * (as returned by {@link #getOperatorNumber(Operator)}). If an operator
     * with the same identifier does not already exist, it will be added to the
     * list of known operators. Its associated number will then become the
     * number of previously known operators.
     * The parameter of this method must not be {@code null}.
     * 
     * @param operator operator to add or replace existing operator with
     */
    public void addOrSetOperator(Operator operator) {
        assert operator != null;
        String name = operator.getIdentifier();
        if (this.identifierToOperator.containsKey(name)) {
            Operator oldOperator = this.identifierToOperator.get(name);
            int number = this.operatorToNumber.get(oldOperator.getIdentifier());
            this.operatorToNumber.put(operator.getIdentifier(), number);
            this.numberToOperator.set(number, operator);
            this.identifierToOperator.put(name, operator);
        } else {
            this.operatorToNumber.put(operator.getIdentifier(), operatorToNumber.size());
            assert this.numberToOperator != null;
            this.numberToOperator.add(operator);
            this.identifierToOperator.put(name, operator);
        }
    }
    
    /**
     * Get operator number by operator identifier.
     * The identifier of an operator is obtained by
     * {@link Operator#getIdentifier()}.
     * The number returned will be nonnegative and strictly smaller than the
     * number of operators known by this value context.
     * The identifier parameter must not be {@code null}. The identifier must
     * be one an identifier of one of the operators known to the value context.
     * 
     * @param identifier identifier of operator to get number of
     * @return number of operator with given identifier
     */
    public int getOperatorNumber(String identifier) {
        assert identifier != null;
        assert identifierToOperator.containsKey(identifier) : identifier;
        return getOperatorNumber(getOperator(identifier));
    }
    
    
    /**
     * Add or replace operator.
     * A new operator will be instantiated from the class paramter using
     * {@link #newOperator(Class, ContextValue)}.
     * Afterwards, the method
     * {@link #addOrSetOperator(Operator)}
     * will be called on this new operator object.
     * 
     * @param clazz class of operator to add or replace
     * @see {@link #addOrSetOperator(Operator)}
     */
    public void addOrSetOperator(Class<? extends Operator> clazz) {
        Operator operator = newOperator(clazz, this);
        addOrSetOperator(operator);
    }

    /**
     * Instantiate given operator class.
     * After instantiation, the given context value will be set for the operator
     * using {@link Operator#setContext(ContextValue)}.
     * The operator class parameter and the value context parameter must not be
     * {@code null}.
     * 
     * @param operatorClass operator class to instantiate
     * @param contextValue value context to set for operator
     * @return instantiated operator with value context set
     */
    private Operator newOperator(Class<? extends Operator> operatorClass, ContextValue contextValue) {
        assert operatorClass != null;
        assert contextValue != null;
        Operator operator = Util.getInstance(operatorClass);
        operator.setContext(contextValue);
        return operator;
    }
}
