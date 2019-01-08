package epmc.qmc;

import epmc.plugin.BeforeModelCreation;
import epmc.qmc.value.OperatorEvaluatorAddComplex;
import epmc.qmc.value.OperatorEvaluatorAddInverseComplex;
import epmc.qmc.value.OperatorEvaluatorAddInverseMatrix;
import epmc.qmc.value.OperatorEvaluatorAddInverseSuperOperator;
import epmc.qmc.value.OperatorEvaluatorAddMatrix;
import epmc.qmc.value.OperatorEvaluatorAddSuperOperator;
import epmc.qmc.value.OperatorEvaluatorArray;
import epmc.qmc.value.OperatorEvaluatorBaseBra;
import epmc.qmc.value.OperatorEvaluatorBaseKet;
import epmc.qmc.value.OperatorEvaluatorBraToVector;
import epmc.qmc.value.OperatorEvaluatorComplex;
import epmc.qmc.value.OperatorEvaluatorConjugate;
import epmc.qmc.value.OperatorEvaluatorDistanceComplex;
import epmc.qmc.value.OperatorEvaluatorDistanceMatrix;
import epmc.qmc.value.OperatorEvaluatorDistanceSuperOperator;
import epmc.qmc.value.OperatorEvaluatorDivideComplex;
import epmc.qmc.value.OperatorEvaluatorDivideMatrix;
import epmc.qmc.value.OperatorEvaluatorDivideSuperOperator;
import epmc.qmc.value.OperatorEvaluatorEqComplex;
import epmc.qmc.value.OperatorEvaluatorEqMatrix;
import epmc.qmc.value.OperatorEvaluatorCompareSuperOperator;
import epmc.qmc.value.OperatorEvaluatorIdentityMatrix;
import epmc.qmc.value.OperatorEvaluatorIsOneComplex;
import epmc.qmc.value.OperatorEvaluatorIsOneMatrix;
import epmc.qmc.value.OperatorEvaluatorIsOneSuperOperator;
import epmc.qmc.value.OperatorEvaluatorIsZeroComplex;
import epmc.qmc.value.OperatorEvaluatorIsZeroMatrix;
import epmc.qmc.value.OperatorEvaluatorIsZeroSuperOperator;
import epmc.qmc.value.OperatorEvaluatorKetToVector;
import epmc.qmc.value.OperatorEvaluatorKronecker;
import epmc.qmc.value.OperatorEvaluatorLtComplex;
import epmc.qmc.value.OperatorEvaluatorMatrix;
import epmc.qmc.value.OperatorEvaluatorMultiplyComplex;
import epmc.qmc.value.OperatorEvaluatorMultiplyInverseComplex;
import epmc.qmc.value.OperatorEvaluatorMultiplyMatrix;
import epmc.qmc.value.OperatorEvaluatorMultiplySuperOperator;
import epmc.qmc.value.OperatorEvaluatorPhaseShift;
import epmc.qmc.value.OperatorEvaluatorQeval;
import epmc.qmc.value.OperatorEvaluatorQprob;
import epmc.qmc.value.OperatorEvaluatorSetComplexComplex;
import epmc.qmc.value.OperatorEvaluatorSetComplexInt;
import epmc.qmc.value.OperatorEvaluatorSetComplexReal;
import epmc.qmc.value.OperatorEvaluatorSetMatrixAlgebra;
import epmc.qmc.value.OperatorEvaluatorSetMatrixMatrix;
import epmc.qmc.value.OperatorEvaluatorSetSuperoperatorAlgebra;
import epmc.qmc.value.OperatorEvaluatorSetSuperoperatorSuperoperator;
import epmc.qmc.value.OperatorEvaluatorSubtractComplex;
import epmc.qmc.value.OperatorEvaluatorSubtractMatrix;
import epmc.qmc.value.OperatorEvaluatorSubtractSuperOperator;
import epmc.qmc.value.OperatorEvaluatorSuperOperatorList;
import epmc.qmc.value.OperatorEvaluatorSuperOperatorMatrix;
import epmc.qmc.value.OperatorEvaluatorTranspose;
import epmc.qmc.value.TypeComplex;
import epmc.qmc.value.TypeSuperOperator;
import epmc.value.TypeReal;
import epmc.value.TypeWeight;
import epmc.value.TypeWeightTransition;
import epmc.value.operatorevaluator.SimpleEvaluatorFactory;

public final class BeforeModelLoadingQMC implements BeforeModelCreation {
    public final static String IDENTIFIER = "before-model-loading-qmc";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process() {
     // TODO these actions should actually take place after the model is
        // created, but there seems to be an issue with constant declaration
        // to be solved first
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorArray.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorMatrix.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorBaseBra.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorBaseKet.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorBraToVector.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorConjugate.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorIdentityMatrix.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorKetToVector.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorKronecker.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorPhaseShift.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorQeval.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorQprob.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorSuperOperatorMatrix.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorSuperOperatorList.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorTranspose.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorComplex.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorDivideMatrix.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorDivideSuperOperator.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorMultiplyInverseComplex.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorAddInverseComplex.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorAddInverseMatrix.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorAddInverseSuperOperator.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorDivideComplex.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorAddComplex.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorAddMatrix.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorAddSuperOperator.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorSubtractComplex.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorSubtractMatrix.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorSubtractSuperOperator.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorDistanceComplex.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorDistanceMatrix.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorDistanceSuperOperator.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorEqComplex.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorEqMatrix.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorLtComplex.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorIsOneComplex.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorIsOneMatrix.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorIsOneSuperOperator.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorIsZeroComplex.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorIsZeroMatrix.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorIsZeroSuperOperator.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorCompareSuperOperator.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorMultiplyComplex.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorMultiplyMatrix.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorMultiplySuperOperator.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorSetComplexInt.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorSetComplexReal.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorSetComplexComplex.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorSetMatrixAlgebra.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorSetMatrixMatrix.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorSetSuperoperatorAlgebra.Builder.class);
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorSetSuperoperatorSuperoperator.Builder.class);
        TypeComplex.set(new TypeComplex(TypeReal.get()));
        TypeSuperOperator.set(new TypeSuperOperator());
        TypeWeight.set(TypeSuperOperator.get());
        TypeWeightTransition.set(TypeSuperOperator.get());
    }
}
