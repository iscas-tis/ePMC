package epmc.lumpingdd.transrepresentation;

import epmc.dd.DD;
import epmc.error.EPMCException;
import epmc.graph.CommonProperties;
import epmc.graph.dd.GraphDD;

/**
 * In contrast to what the name of this class might suggest
 * the DDs produced by this class might actually be integers
 * if the original system has only integers in its transition
 * relation.
 */
public class DoubleRepresentation implements TransitionRepresentation {

	private GraphDD original;

	@Override
	public void setOriginal(GraphDD original) {
		this.original = original;
	}
	
	@Override
	public DD fromTransWeights() throws EPMCException {
		// We are not interested in actions
		return original.getEdgeProperty(CommonProperties.WEIGHT)
    			.abstractSum(original.getActionCube());
	}

}
