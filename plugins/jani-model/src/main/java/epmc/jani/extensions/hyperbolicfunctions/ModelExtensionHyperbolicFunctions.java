package epmc.jani.extensions.hyperbolicfunctions;

import epmc.error.EPMCException;
import epmc.jani.model.JANINode;
import epmc.jani.model.JANIOperators;
import epmc.jani.model.ModelExtension;
import epmc.jani.model.ModelJANI;

public final class ModelExtensionHyperbolicFunctions implements ModelExtension {
	public final static String IDENTIFIER = "hyperbolic-functions";
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
		operators.add()
			.setArity(1)
			.setEPMC(OperatorSinh.IDENTIFIER)
			.setJANI(OperatorSinh.IDENTIFIER)
			.build();
		operators.add()
			.setArity(1)
			.setEPMC(OperatorCosh.IDENTIFIER)
			.setJANI(OperatorCosh.IDENTIFIER)
			.build();
		operators.add()
			.setArity(1)
			.setEPMC(OperatorTanh.IDENTIFIER)
			.setJANI(OperatorTanh.IDENTIFIER)
			.build();
		
		operators.add()
			.setArity(1)
			.setEPMC(OperatorAsinh.IDENTIFIER)
			.setJANI(OperatorAsinh.IDENTIFIER)
			.build();
		operators.add()
			.setArity(1)
			.setEPMC(OperatorAcosh.IDENTIFIER)
			.setJANI(OperatorAcosh.IDENTIFIER)
			.build();
		operators.add()
			.setArity(1)
			.setEPMC(OperatorAtanh.IDENTIFIER)
			.setJANI(OperatorAtanh.IDENTIFIER)
			.build();
	}
}
