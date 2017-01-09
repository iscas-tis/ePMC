package epmc.lumpingdd;

import java.util.List;

import epmc.dd.DD;
import epmc.dd.VariableDD;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.graph.dd.GraphDD;
import epmc.lumpingdd.transrepresentation.TransitionRepresentation;

public interface Signature {
	/**
	 * Set the original graph that the lumper works on
	 * The signature can use this to decide whether it can
	 * lump.
	 * @throws EPMCException thrown in case of problems 
	 */
	public void setOriginal(GraphDD original) throws EPMCException;
	
	/**
	 * Decides whether this class can lump the original graph
	 * @throws EPMCException 
	 */
	public boolean canLump(List<Expression> validFor) throws EPMCException;
	
	/**
	 * Set the variable that defines the block indices
	 */
	public void setBlockIndexVar(VariableDD partitionsVar) throws EPMCException;
	
	/**
	 * Compute the signatures for the given partition
	 * @param partitions The current partition
	 * @return A DD defining the new signature for each state
	 */
	public DD computeSignatures(DD partitions) throws EPMCException;

	/**
	 * Get the transition representation associated with
	 * this signature.
	 */
	public TransitionRepresentation getTransitionRepresentation();
}
