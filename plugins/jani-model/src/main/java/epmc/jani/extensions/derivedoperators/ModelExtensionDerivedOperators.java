package epmc.jani.extensions.derivedoperators;

import epmc.error.EPMCException;
import epmc.jani.model.JANINode;
import epmc.jani.model.JANIOperators;
import epmc.jani.model.ModelExtension;
import epmc.jani.model.ModelJANI;
import epmc.value.OperatorGe;
import epmc.value.OperatorGt;
import epmc.value.OperatorImplies;
import epmc.value.OperatorMax;
import epmc.value.OperatorMin;

public final class ModelExtensionDerivedOperators implements ModelExtension {
	/** Identifies an IMPLIES operator. */
	private final static String OPERATOR_IMPLIES = "⇒";
	/** Identifies a greater-than operator. */
	private final static String OPERATOR_GT = ">";
	/** Identifies a greater-or-equal operator. */
	private final static String OPERATOR_GE = "≥";
	/** Identifies a minimum operator. */
	private final static String OPERATOR_MIN = "min";
	/** Identifies a maximum operator. */
	private final static String OPERATOR_MAX = "max";
	/** Identifies an absolute value operator. */
	private final static String OPERATOR_ABS = "abs";
	/** Identifies a signum operator. */
	private final static String OPERATOR_SGN = "sgn";
	/** Identifies a truncation-to-integer operator. */
	private final static String OPERATOR_TRUNCATION = "trc";

	public final static String IDENTIFIER = "derived-operators";
	private ModelJANI model;
	private JANINode node;

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void setModel(ModelJANI model) throws EPMCException {
		this.model = model;
	}

	@Override
	public void setNode(JANINode node) throws EPMCException {
		this.node = node;
	}

	@Override
	public void parseBefore() throws EPMCException {
		if (!(this.node instanceof ModelJANI)) {
			return;
		}
		JANIOperators operators = model.getJANIOperators();
		operators.add().setJANI(OPERATOR_IMPLIES).setEPMC(OperatorImplies.IDENTIFIER)
			.setArity(2).build();
		operators.add().setJANI(OPERATOR_GT).setEPMC(OperatorGt.IDENTIFIER)
			.setArity(2).build();
		operators.add().setJANI(OPERATOR_GE).setEPMC(OperatorGe.IDENTIFIER)
			.setArity(2).build();
		operators.add().setJANI(OPERATOR_MIN).setEPMC(OperatorMin.IDENTIFIER)
			.setArity(2).build();
		operators.add().setJANI(OPERATOR_MAX).setEPMC(OperatorMax.IDENTIFIER)
			.setArity(2).build();
		operators.add().setJANI(OPERATOR_ABS).setEPMC(OperatorAbs.IDENTIFIER)
			.setArity(1).build();
		operators.add().setJANI(OPERATOR_SGN).setEPMC(OperatorSgn.IDENTIFIER)
			.setArity(1).build();
		operators.add().setJANI(OPERATOR_TRUNCATION).setEPMC(OperatorTrunc.IDENTIFIER)
			.setArity(1).build();
	}
}
