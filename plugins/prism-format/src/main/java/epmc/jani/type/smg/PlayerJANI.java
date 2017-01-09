package epmc.jani.type.smg;

import java.util.Collections;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.jani.model.Action;
import epmc.jani.model.Actions;
import epmc.jani.model.Automata;
import epmc.jani.model.Automaton;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.util.UtilJSON;

public final class PlayerJANI implements JANINode {
	private final static String NAME = "name";
	private final static String AUTOMATA = "automata";
	private final static String ACTIONS = "actions";
	
	private ModelJANI model;
	private Actions validActions;
	private Automata validAutomata;
	private String name;
	private Set<Automaton> automata;
	private Set<Action> actions;

	@Override
	public void setModel(ModelJANI model) {
		this.model = model;
	}

	@Override
	public ModelJANI getModel() {
		return model;
	}
	
	public void setValidActions(Actions actions) {
		this.validActions = actions;
	}

	public void setValidAutomata(Automata automata) {
		this.validAutomata = automata;
	}
	
	@Override
	public JANINode parse(JsonValue value) throws EPMCException {
		JsonObject object = UtilJSON.toObject(value);
		name = UtilJSON.getString(object, NAME);
		this.automata = UtilJSON.toSubsetOfOrNull(object, AUTOMATA, validAutomata);
		this.actions = UtilJSON.toSubsetOfOrNull(object, ACTIONS, validActions);
		return this;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setAutomata(Set<Automaton> automata) {
		this.automata = automata;
	}
	
	public Set<Automaton> getAutomata() {
		return automata;
	}
	
	public Set<Automaton> getAutomataOrEmpty() {
		if (automata == null) {
			return Collections.emptySet();
		} else {
			return automata;
		}
	}
	
	
	public void setActions(Set<Action> actions) {
		this.actions = actions;
	}
	
	public Set<Action> getActions() {
		return actions;
	}

	public Set<Action> getActionsOrEmpty() {
		if (actions == null) {
			return Collections.emptySet();
		} else {
			return actions;
		}
	}
	
	
	@Override
	public JsonValue generate() {
		JsonObjectBuilder result = Json.createObjectBuilder();
		result.add(NAME, name);
		if (automata != null) {
			JsonArrayBuilder automataBuilder = Json.createArrayBuilder();
			for (Automaton automaton : automata) {
				automataBuilder.add(automaton.getName());
			}
			result.add(AUTOMATA, automataBuilder);
		}
		if (actions != null) {
			JsonArrayBuilder actionsBuilder = Json.createArrayBuilder();
			for (Action action : actions) {
				actionsBuilder.add(action.getName());
			}
			result.add(ACTIONS, actionsBuilder);
		}
		
		return result.build();
	}

	@Override
	public String toString() {
		return UtilModelParser.toString(this);
	}
}
