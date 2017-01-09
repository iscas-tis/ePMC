package epmc.lumping.lumpingexplicitsignature;

import epmc.error.EPMCException;
import epmc.graphsolver.lumping.LumperExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;

public final class LumperExplicitSignatureStrong implements LumperExplicit {
    private LumperExplicitSignature inner = new LumperExplicitSignature(EquivalenceStrong.class);
    public final static String IDENTIFIER = "lumper-explicit-signature-strong";
    
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public boolean canLump() {
        return inner.canLump();
    }

    @Override
    public void lump() throws EPMCException {
        inner.lump();
    }

	@Override
	public void setOriginal(GraphSolverObjectiveExplicit objective) {
		inner.setOriginal(objective);
	}

	@Override
	public GraphSolverObjectiveExplicit getQuotient() {
		return inner.getQuotient();
	}

	@Override
	public void quotientToOriginal() throws EPMCException {
		inner.quotientToOriginal();
	}

}
