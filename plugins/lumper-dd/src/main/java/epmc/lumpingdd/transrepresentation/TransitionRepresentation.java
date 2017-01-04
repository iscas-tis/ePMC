package epmc.lumpingdd.transrepresentation;

import epmc.dd.DD;
import epmc.error.EPMCException;
import epmc.graph.dd.GraphDD;

public interface TransitionRepresentation {
	
	/**
	 * Set the original graph that the lumper works on
	 * @throws EPMCException thrown in case of problems
	 */
	public void setOriginal(GraphDD original) throws EPMCException;
	/**
	 * Convert the transition weights DD of the original
	 * graph to the right representation.
	 */
	public DD fromTransWeights() throws EPMCException;
}
