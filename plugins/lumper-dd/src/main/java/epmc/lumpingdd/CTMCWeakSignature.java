package epmc.lumpingdd;

import java.util.List;

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.Permutation;
import epmc.dd.VariableDD;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.graph.CommonProperties;
import epmc.graph.Semantics;
import epmc.graph.SemanticsCTMC;
import epmc.graph.dd.GraphDD;
import epmc.lumpingdd.transrepresentation.DoubleRepresentation;
import epmc.lumpingdd.transrepresentation.TransitionRepresentation;

public final class CTMCWeakSignature implements Signature {

	public static class LumperDDSignatureCTMCWeak extends LumperDDSignature {
	    public final static String IDENTIFIER = "lumper-dd-signature-ctmc-weak";

	    public LumperDDSignatureCTMCWeak() {
	    	super(new CTMCWeakSignature(), IDENTIFIER);
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
		this.transRepr = new DoubleRepresentation();
		this.transRepr.setOriginal(original);
	}

	@Override
	public boolean canLump(List<Expression> validFor) {
        Semantics semantics = original.getGraphPropertyObject(CommonProperties.SEMANTICS);
        if (!SemanticsCTMC.isCTMC(semantics)) {
        	return false;
        }
		for (Expression expr: validFor) {
			if (!LumperDDSignature.collectRewards(expr).isEmpty()) {
				return false;
			}
		}
        return true;
	}

	@Override
	public void setBlockIndexVar(VariableDD blockIndex) throws EPMCException {
		// Only do this after the block index variables have been created
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
		Permutation p = original.getSwapPresNext();
        DD partitionsNext = partitions.permute(p);
        DD skipOwnPartition = contextDD.newConstant(1).subtractWith(partitions.clone());
        return transStateSpace.clone().multiplyWith(skipOwnPartition, partitionsNext)
        		.abstractSumWith(original.getNextCube().clone())
        		.addWith(pVar.multiply(partitions));
	}

}
