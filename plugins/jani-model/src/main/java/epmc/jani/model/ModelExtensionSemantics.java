package epmc.jani.model;

import epmc.graph.Semantics;

/**
 * Model extension for a particular model semantics type.
 * 
 * @author Ernst Moritz Hahn
 */
public interface ModelExtensionSemantics extends ModelExtension {
	/**
	 * Get the semantics type this extension is responsible for.
	 * 
	 * @return semantics type this extension is responsible for
	 */
	Semantics getSemantics();
}
