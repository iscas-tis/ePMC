package epmc.qmc;

import epmc.error.EPMCException;
import epmc.plugin.BeforeModelCreation;
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
import epmc.value.ContextValue;

public final class BeforeModelLoadingQMC implements BeforeModelCreation {
    public final static String IDENTIFIER = "before-model-loading-qmc";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(ContextValue contextValue) throws EPMCException {
        assert contextValue != null;
        contextValue.addOrSetOperator(OperatorArray.class);
        contextValue.addOrSetOperator(OperatorBaseBra.class);
        contextValue.addOrSetOperator(OperatorBaseKet.class);
        contextValue.addOrSetOperator(OperatorBraToVector.class);
        contextValue.addOrSetOperator(OperatorConjugate.class);
        contextValue.addOrSetOperator(OperatorIdentityMatrix.class);
        contextValue.addOrSetOperator(OperatorKetToVector.class);
        contextValue.addOrSetOperator(OperatorKronecker.class);
        contextValue.addOrSetOperator(OperatorPhaseShift.class);
        contextValue.addOrSetOperator(OperatorQeval.class);
        contextValue.addOrSetOperator(OperatorQprob.class);
        contextValue.addOrSetOperator(OperatorSuperOperator.class);
        contextValue.addOrSetOperator(OperatorTranspose.class);
        contextValue.addOrSetOperator(OperatorComplex.class);
    }
}
