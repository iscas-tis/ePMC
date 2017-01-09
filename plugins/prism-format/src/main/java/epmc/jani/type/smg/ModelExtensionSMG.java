package epmc.jani.type.smg;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.graph.Semantics;
import epmc.graph.SemanticsSMG;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelExtensionSemantics;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.util.UtilJSON;

// TODO not quite clear where to finally put this stuff

public final class ModelExtensionSMG implements ModelExtensionSemantics {
	public final static String IDENTIFIER = "smg";
	private final static String PLAYERS = "players";
	
	private ModelJANI model;
	private JsonValue value;
	private JANINode node;
	private PlayersJANI players;

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public Semantics getSemantics() {
		return SemanticsSMG.SMG;
	}

	@Override
	public void setModel(ModelJANI model) throws EPMCException {
		this.model = model;
	}
	
	@Override
	public ModelJANI getModel() {
		return model;
	}
	
	@Override
	public void setJsonValue(JsonValue value) throws EPMCException {
		this.value = value;
	}
	
	@Override
	public void setNode(JANINode node) throws EPMCException {
		this.node = node;
	}
	
	@Override
	public void parseAfter() throws EPMCException {
		if (!(node instanceof ModelJANI)) {
			return;
		}
		ModelJANI model = (ModelJANI) node;
		JsonObject object = UtilJSON.toObject(value);
		
		players = UtilModelParser.parse(model, () -> {
			PlayersJANI result = new PlayersJANI();
			result.setValidActions(model.getActionsOrEmpty());
			result.setValidAutomata(model.getAutomata());
			return result;
		}, object, PLAYERS);
	}
	
	public void setPlayers(PlayersJANI players) {
		this.players = players;
	}
	
	@Override
	public void generate(JsonObjectBuilder generate) throws EPMCException {
		assert generate != null;
		generate.add(PLAYERS, players.generate());
	}

	public PlayersJANI getPlayers() {
		return players;
	}
}
