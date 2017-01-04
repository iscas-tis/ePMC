package epmc.prism.model.convert;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import epmc.graph.SemanticsSMG;
import epmc.jani.model.Action;
import epmc.jani.model.Automaton;
import epmc.jani.model.ModelJANI;
import epmc.jani.type.smg.ModelExtensionSMG;
import epmc.jani.type.smg.PlayerJANI;
import epmc.jani.type.smg.PlayersJANI;
import epmc.prism.model.ModelPRISM;
import epmc.prism.model.PlayerDefinition;

final class PlayerConverter {
	private ModelPRISM modelPRISM;
	private ModelJANI modelJANI;

	void setPRISMModel(ModelPRISM modelPrism) {
		this.modelPRISM = modelPrism;
	}
	
	void setJANIModel(ModelJANI modelJani) {
		this.modelJANI = modelJani;
	}
	
	void attachPlayers() {
		if (!SemanticsSMG.isSMG(modelPRISM.getSemantics())) {
			return;
		}
		PlayersJANI players = convertPlayers(modelPRISM.getPlayers());
		ModelExtensionSMG extension = (ModelExtensionSMG) modelJANI.getSemanticsExtension();
		extension.setPlayers(players);
	}

	private PlayersJANI convertPlayers(List<PlayerDefinition> playersPRISM) {
		PlayersJANI players = new PlayersJANI();
		players.setModel(modelJANI);
		for (PlayerDefinition playerPRISM : playersPRISM) {
			PlayerJANI playerJANI = new PlayerJANI();
			playerJANI.setModel(modelJANI);
			playerJANI.setName(playerPRISM.getName());
			Set<Action> actions = new HashSet<>();
			for (String actionName : playerPRISM.getLabels()) {
				actions.add(modelJANI.getActions().get(actionName));
			}
			playerJANI.setActions(actions);
			Set<Automaton> automata = new HashSet<>();
			for (String moduleName : playerPRISM.getModules()) {
				Automaton automaton = modelJANI.getAutomata().get(moduleName);
				assert automaton != null : moduleName + " " + modelJANI.getAutomata().getAutomata().keySet();
				automata.add(automaton);
			}
			playerJANI.setAutomata(automata);
			players.add(playerJANI);
		}
		return players;
	}

}
