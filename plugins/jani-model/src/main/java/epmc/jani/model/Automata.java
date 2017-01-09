package epmc.jani.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.util.UtilJSON;
import epmc.value.ContextValue;
import epmc.value.Type;

/**
 * Specification of the list of automata of this model.
 * 
 * @author Ernst Moritz Hahn
 */
public final class Automata implements JANINode, Map<String,Automaton>, Iterable<Automaton>, ExpressionToType {
	/** Maps names of automata to automata. */
	private final Map<String,Automaton> automata = new LinkedHashMap<>();
	/** Unmodifiable map from names of automata to automata. */
	private final Map<String,Automaton> automataExternal = Collections.unmodifiableMap(automata);
	/** Model to which these automata belong. */
	private ModelJANI model;
	
	@Override
	public void setModel(ModelJANI model) {
		this.model = model;
	}

	@Override
	public ModelJANI getModel() {
		return model;
	}
	
	@Override
	public JANINode parse(JsonValue value) throws EPMCException {
		assert model != null;
		assert value != null;
		JsonArray array = UtilJSON.toArrayObject(value);
		int automatonNr = 0;
		for (JsonValue autV : array) {
			Automaton automaton = new Automaton();
			automaton.setModel(model);
			automaton.setNumber(automatonNr);
			automaton.parse(autV);
			UtilJSON.ensureUnique(automaton.getName(), automata);
			automata.put(automaton.getName(), automaton);
			automatonNr++;
		}
		return this;
	}
	
	public void addAutomaton(Automaton automaton) {
		assert automaton != null;
		automata.put(automaton.getName(), automaton);
	}
	
	@Override
	public JsonValue generate() throws EPMCException {
		JsonArrayBuilder result = Json.createArrayBuilder();
		for (Automaton automaton : automata.values()) {
			result.add(automaton.generate());
		}
		return result.build();
	}

	/**
	 * Get map of automata names to automata.
	 * This method must not be called before the object has been parsed.
	 * 
	 * @return map of automata names to automata
	 */
	public Map<String, Automaton> getAutomata() {
		return automataExternal;
	}
	
	@Override
	public String toString() {
		return UtilModelParser.toString(this);
	}

	@Override
	public int size() {
		return automata.size();
	}

	@Override
	public boolean isEmpty() {
		return automata.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return automata.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return automata.containsValue(value);
	}

	@Override
	public Automaton get(Object key) {
		return automata.get(key);
	}

	@Override
	public Automaton put(String key, Automaton value) {
		assert false;
		return null;
	}

	@Override
	public Automaton remove(Object key) {
		assert false;
		return null;
	}

	@Override
	public void putAll(Map<? extends String, ? extends Automaton> m) {
		assert false;		
	}

	@Override
	public void clear() {
		assert false;
	}

	@Override
	public Set<String> keySet() {
		return automataExternal.keySet();
	}

	@Override
	public Collection<Automaton> values() {
		return automataExternal.values();
	}

	@Override
	public Set<java.util.Map.Entry<String, Automaton>> entrySet() {
		return automataExternal.entrySet();
	}

	@Override
	public Iterator<Automaton> iterator() {
		return automata.values().iterator();
	}

	@Override
	public Type getType(Expression expression) throws EPMCException {
		assert expression != null;
		for (Automaton automaton : automata.values()) {
			Type type = automaton.getType(expression);
			if (type != null) {
				return type;
			}
		}
		return null;
	}

	@Override
	public ContextValue getContextValue() {
		return model.getContextValue();
	}
}
