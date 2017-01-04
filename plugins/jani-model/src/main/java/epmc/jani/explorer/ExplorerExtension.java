package epmc.jani.explorer;

import epmc.error.EPMCException;
import epmc.graph.explorer.ExplorerEdgeProperty;
import epmc.graph.explorer.ExplorerNodeProperty;
import epmc.jani.model.ModelExtension;
import epmc.value.Value;

public interface ExplorerExtension {
	String getIdentifier();
	
	default void setExplorer(ExplorerJANI explorer) throws EPMCException {
	}
	
	default boolean isUsedGetNodeProperty() {
		return false;
	}
	
	default Value getGraphProperty(Object property) {
		return null;
	}
	
	default ExplorerNodeProperty getNodeProperty(Object property) throws EPMCException {
		return null;
	}
	
	default ExplorerEdgeProperty getEdgeProperty(Object property) throws EPMCException {
		return null;
	}
	
	default void handleNoSuccessors(NodeJANI node) throws EPMCException {
	}
	
	default void handleSelfLoop(NodeJANI node) throws EPMCException {
	}
	
	default void beforeQuerySystem(NodeJANI nodeJANI) {
	}
	
	default void afterQuerySystem(NodeJANI node) throws EPMCException {
	}
	
	default void afterQueryAutomaton(ExplorerComponentAutomaton automaton) throws EPMCException {
	}

	default void setModelExtension(ModelExtension modelExtension) throws EPMCException {
	}
}
