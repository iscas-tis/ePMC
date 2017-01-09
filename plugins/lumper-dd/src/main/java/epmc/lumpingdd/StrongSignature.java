package epmc.lumpingdd;

import java.util.List;

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.VariableDD;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.RewardSpecification;
import epmc.graph.CommonProperties;
import epmc.graph.Semantics;
import epmc.graph.SemanticsMarkovChain;
import epmc.graph.dd.GraphDD;
import epmc.lumpingdd.transrepresentation.MultisetIndexRepresentation;
import epmc.lumpingdd.transrepresentation.TransitionRepresentation;

public class StrongSignature implements Signature {

	public static class LumperDDSignatureStrong extends LumperDDSignature {

	    public final static String IDENTIFIER = "lumper-dd-signature-strong";

	    public LumperDDSignatureStrong() {
	    	super(new StrongSignature(), IDENTIFIER);
	    }
	}
	
	private GraphDD original;
	private ContextDD contextDD;
	private TransitionRepresentation transRepr;
	private DD pVar;
	private DD transStateSpace;

	@Override
	public void setOriginal(GraphDD original) throws EPMCException {
		this.original = original;
		this.contextDD = original.getContextDD();
		this.transRepr = new MultisetIndexRepresentation();
		this.transRepr.setOriginal(original);
	}

	@Override
	public boolean canLump(List<Expression> validFor) throws EPMCException {
        Semantics semantics = original.getGraphPropertyObject(CommonProperties.SEMANTICS);
        if (!SemanticsMarkovChain.isMarkovChain(semantics)) {
        	return false;
        }
        DD zeroDD = contextDD.newConstant(0);
		for(Expression expr: validFor) {
			for (RewardSpecification r : LumperDDSignature.collectRewards(expr)) {
				if(!original.getEdgeProperty(r).equals(zeroDD)) {
					return false;
				}
			}
		}
		zeroDD.dispose();
        return true;
	}

	@Override
	public void setBlockIndexVar(VariableDD blockIndex) throws EPMCException {
        DD stateSpace = original.getNodeSpace().toMT();
		transStateSpace = transRepr.fromTransWeights().multiply(stateSpace);
		stateSpace.dispose();
    	pVar = contextDD.newBoolean("p", 1).getValueEncoding(0).toMT();
	}

	@Override
	public TransitionRepresentation getTransitionRepresentation() {
		return transRepr;
	}

	@Override
	public DD computeSignatures(DD partitions) throws EPMCException {
        DD partitionsNext = partitions.permute(original.getSwapPresNext());
        return transStateSpace.clone().multiplyWith(partitionsNext)
        		.abstractSumWith(original.getNextCube().clone())
        		.addWith(pVar.multiply(partitions));
	}

}