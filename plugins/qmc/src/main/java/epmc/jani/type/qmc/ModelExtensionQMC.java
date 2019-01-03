package epmc.jani.type.qmc;

import static epmc.error.UtilError.ensure;

import javax.json.JsonValue;

import epmc.graph.Semantics;
import epmc.graph.SemanticsQMC;
import epmc.jani.model.Edge;
import epmc.jani.model.JANINode;
import epmc.jani.model.JANIOperators;
import epmc.jani.model.Location;
import epmc.jani.model.ModelExtensionSemantics;
import epmc.jani.model.ModelJANI;
import epmc.qmc.operator.OperatorArray;
import epmc.qmc.operator.OperatorBaseBra;
import epmc.qmc.operator.OperatorBaseKet;
import epmc.qmc.operator.OperatorBraToVector;
import epmc.qmc.operator.OperatorComplex;
import epmc.qmc.operator.OperatorConjugate;
import epmc.qmc.operator.OperatorIdentityMatrix;
import epmc.qmc.operator.OperatorKetToVector;
import epmc.qmc.operator.OperatorKronecker;
import epmc.qmc.operator.OperatorPhaseShift;
import epmc.qmc.operator.OperatorQeval;
import epmc.qmc.operator.OperatorQprob;
import epmc.qmc.operator.OperatorSuperOperatorList;
import epmc.qmc.operator.OperatorSuperOperatorMatrix;
import epmc.qmc.operator.OperatorTranspose;

public final class ModelExtensionQMC implements ModelExtensionSemantics {
    public final static String IDENTIFIER = "qmc";
    private JANINode node;
    private ModelJANI model;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setModel(ModelJANI model) {
        this.model = model;
        // TODO
    }

    @Override
    public void setNode(JANINode node) {
        this.node = node;
    }

    @Override
    public void setJsonValue(JsonValue value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void parseBefore() {
        JANIOperators operators = model.getJANIOperators();
        assert operators != null;
        operators.add().setJANI(OperatorArray.ARRAY.toString())
        .setEPMC(OperatorArray.ARRAY)
        .build();
        operators.add().setJANI(OperatorBaseBra.BASE_BRA.toString())
        .setEPMC(OperatorBaseBra.BASE_BRA)
        .build();
        operators.add().setJANI(OperatorBaseKet.BASE_KET.toString())
        .setEPMC(OperatorBaseKet.BASE_KET)
        .build();
        operators.add().setJANI(OperatorBraToVector.BRA_TO_VECTOR.toString())
        .setEPMC(OperatorBraToVector.BRA_TO_VECTOR)
        .build();
        operators.add().setJANI(OperatorConjugate.CONJUGATE.toString())
        .setEPMC(OperatorConjugate.CONJUGATE)
        .build();
        operators.add().setJANI(OperatorIdentityMatrix.IDENTITY_MATRIX.toString())
        .setEPMC(OperatorIdentityMatrix.IDENTITY_MATRIX)
        .build();
        operators.add().setJANI(OperatorKetToVector.KET_TO_VECTOR.toString())
        .setEPMC(OperatorKetToVector.KET_TO_VECTOR)
        .build();
        operators.add().setJANI(OperatorKronecker.KRONECKER.toString())
        .setEPMC(OperatorKronecker.KRONECKER)
        .build();
        operators.add().setJANI(OperatorPhaseShift.PHASE_SHIFT.toString())
        .setEPMC(OperatorPhaseShift.PHASE_SHIFT)
        .build();
        operators.add().setJANI(OperatorQeval.QEVAL.toString())
        .setEPMC(OperatorQeval.QEVAL)
        .build();
        operators.add().setJANI(OperatorQprob.QPROB.toString())
        .setEPMC(OperatorQprob.QPROB)
        .build();
        operators.add().setJANI(OperatorSuperOperatorMatrix.SUPEROPERATOR_MATRIX.toString())
        .setEPMC(OperatorSuperOperatorMatrix.SUPEROPERATOR_MATRIX)
        .build();
        operators.add().setJANI(OperatorSuperOperatorList.SUPEROPERATOR_LIST.toString())
        .setEPMC(OperatorSuperOperatorList.SUPEROPERATOR_LIST)
        .build();
        operators.add().setJANI(OperatorTranspose.TRANSPOSE.toString())
        .setEPMC(OperatorTranspose.TRANSPOSE)
        .build();
        operators.add().setJANI(OperatorComplex.COMPLEX.toString())
        .setEPMC(OperatorComplex.COMPLEX)
        .build();
    }

    @Override
    public void parseAfter() {
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
