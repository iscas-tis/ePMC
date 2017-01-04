package epmc.jani.type.qmc;

import static epmc.error.UtilError.ensure;

import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.graph.Semantics;
import epmc.graph.SemanticsQMC;
import epmc.jani.model.Edge;
import epmc.jani.model.JANINode;
import epmc.jani.model.JANIOperators;
import epmc.jani.model.Location;
import epmc.jani.model.ModelExtensionSemantics;
import epmc.jani.model.ModelJANI;
import epmc.qmc.value.OperatorArray;
import epmc.qmc.value.OperatorBaseBra;
import epmc.qmc.value.OperatorBaseKet;
import epmc.qmc.value.OperatorBraToVector;
import epmc.qmc.value.OperatorComplex;
import epmc.qmc.value.OperatorConjugate;
import epmc.qmc.value.OperatorIdentityMatrix;
import epmc.qmc.value.OperatorKetToVector;
import epmc.qmc.value.OperatorKronecker;
import epmc.qmc.value.OperatorPhaseShift;
import epmc.qmc.value.OperatorQeval;
import epmc.qmc.value.OperatorQprob;
import epmc.qmc.value.OperatorSuperOperator;
import epmc.qmc.value.OperatorTranspose;

public final class ModelExtensionQMC implements ModelExtensionSemantics {
	public final static String IDENTIFIER = "qmc";
	private JANINode node;
	private ModelJANI model;

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void setModel(ModelJANI model) throws EPMCException {
		this.model = model;
		// TODO
//		model.getContextValue().setTypeWeight(model.getContextValue().getTypeInterval());
	}

	@Override
	public void setNode(JANINode node) throws EPMCException {
		this.node = node;
	}

	@Override
	public void setJsonValue(JsonValue value) throws EPMCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void parseBefore() throws EPMCException {
		JANIOperators operators = model.getJANIOperators();
		assert operators != null;
		operators.add().setJANI(OperatorArray.IDENTIFIER)
			.setEPMC(OperatorArray.IDENTIFIER)
			.build();
		operators.add().setJANI(OperatorBaseBra.IDENTIFIER)
			.setEPMC(OperatorBaseBra.IDENTIFIER)
			.build();
		operators.add().setJANI(OperatorBaseKet.IDENTIFIER)
			.setEPMC(OperatorBaseKet.IDENTIFIER)
			.build();
		operators.add().setJANI(OperatorBraToVector.IDENTIFIER)
			.setEPMC(OperatorBraToVector.IDENTIFIER)
			.build();
		operators.add().setJANI(OperatorConjugate.IDENTIFIER)
			.setEPMC(OperatorConjugate.IDENTIFIER)
			.build();
		operators.add().setJANI(OperatorIdentityMatrix.IDENTIFIER)
			.setEPMC(OperatorIdentityMatrix.IDENTIFIER)
			.build();
		operators.add().setJANI(OperatorKetToVector.IDENTIFIER)
			.setEPMC(OperatorKetToVector.IDENTIFIER)
			.build();
		operators.add().setJANI(OperatorKronecker.IDENTIFIER)
			.setEPMC(OperatorKronecker.IDENTIFIER)
			.build();
		operators.add().setJANI(OperatorPhaseShift.IDENTIFIER)
			.setEPMC(OperatorPhaseShift.IDENTIFIER)
			.build();
		operators.add().setJANI(OperatorQeval.IDENTIFIER)
			.setEPMC(OperatorQeval.IDENTIFIER)
			.build();
		operators.add().setJANI(OperatorQprob.IDENTIFIER)
			.setEPMC(OperatorQprob.IDENTIFIER)
			.build();
		operators.add().setJANI(OperatorSuperOperator.IDENTIFIER)
			.setEPMC(OperatorSuperOperator.IDENTIFIER)
			.build();
		operators.add().setJANI(OperatorTranspose.IDENTIFIER)
			.setEPMC(OperatorTranspose.IDENTIFIER)
			.build();
		operators.add().setJANI(OperatorComplex.IDENTIFIER)
			.setEPMC(OperatorComplex.IDENTIFIER)
			.build();
	}

	@Override
	public void parseAfter() throws EPMCException {
		if (node instanceof Edge) {
			Edge edge = (Edge) node;
			ensure(edge.getRate() == null, ProblemsJANIQMC.JANI_QMC_EDGE_FORBIDS_RATE);
		}
		if (node instanceof Location) {
			Location location = (Location) node;
			ensure(location.getTimeProgress() == null, ProblemsJANIQMC.JANI_QMC_DISALLOWED_TIME_PROGRESSES);
		}
	}

	@Override
	public Semantics getSemantics() {
		return SemanticsQMC.QMC;
	}
}
