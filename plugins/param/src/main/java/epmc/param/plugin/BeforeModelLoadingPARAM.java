package epmc.param.plugin;

import java.util.List;
import java.util.Map;

import epmc.error.EPMCException;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.OptionsExpressionBasic;
import epmc.options.Options;
import epmc.options.UtilOptions;
import epmc.param.expressionevaluator.EvaluatorExplicitIdentifierParameter;
import epmc.param.operatorevaluator.EvaluatorSetFunctionReal;
import epmc.param.operatorevaluator.dag.EvaluatorAddInverseMultiplyInverseDag;
import epmc.param.operatorevaluator.dag.EvaluatorAddSubtractMultiplyDivideDag;
import epmc.param.operatorevaluator.dag.EvaluatorIsOneIsZeroDag;
import epmc.param.operatorevaluator.dag.EvaluatorPowDagInt;
import epmc.param.operatorevaluator.dag.EvaluatorSetDagDag;
import epmc.param.operatorevaluator.doubles.EvaluatorNextUpDownDouble;
import epmc.param.operatorevaluator.gmp.EvaluatorAddInverseMultiplyInverseMPQ;
import epmc.param.operatorevaluator.gmp.EvaluatorAddSubtractMultiplyDivideMPQ;
import epmc.param.operatorevaluator.gmp.EvaluatorCmpMPQ;
import epmc.param.operatorevaluator.gmp.EvaluatorIsZeroIsOneMPQ;
import epmc.param.operatorevaluator.gmp.EvaluatorMaxMPQ;
import epmc.param.operatorevaluator.gmp.EvaluatorPowMPQ;
import epmc.param.operatorevaluator.gmp.EvaluatorSetMPQDoubleFraction;
import epmc.param.operatorevaluator.gmp.EvaluatorSetMPQMPQ;
import epmc.param.operatorevaluator.interval.EvaluatorAddInverseInterval;
import epmc.param.operatorevaluator.interval.EvaluatorAddSubtractInterval;
import epmc.param.operatorevaluator.interval.EvaluatorMultiplicativeInverseInterval;
import epmc.param.operatorevaluator.interval.EvaluatorMultiplyDivideInterval;
import epmc.param.operatorevaluator.interval.EvaluatorPowInterval;
import epmc.param.operatorevaluator.interval.EvaluatorSetIntervalDoubleFraction;
import epmc.param.operatorevaluator.interval.EvaluatorSetIntervalInterval;
import epmc.param.operatorevaluator.interval.EvaluatorSetIntervalIntervalDoubleFraction;
import epmc.param.operatorevaluator.polynomial.EvaluatorAddInversePolynomial;
import epmc.param.operatorevaluator.polynomial.EvaluatorAddSubtractPolynomial;
import epmc.param.operatorevaluator.polynomial.EvaluatorEvaluatePolynomialReal;
import epmc.param.operatorevaluator.polynomial.EvaluatorIsOnePolynomial;
import epmc.param.operatorevaluator.polynomial.EvaluatorIsZeroPolynomial;
import epmc.param.operatorevaluator.polynomial.EvaluatorMultiplyPolynomial;
import epmc.param.operatorevaluator.polynomial.EvaluatorPowPolynomialInt;
import epmc.param.operatorevaluator.polynomial.EvaluatorSetPolynomialPolynomial;
import epmc.param.operatorevaluator.polynomialfraction.EvaluatorAddInversePolynomialFraction;
import epmc.param.operatorevaluator.polynomialfraction.EvaluatorAddSubtractPolynomialFraction;
import epmc.param.operatorevaluator.polynomialfraction.EvaluatorEvaluatePolynomialFractionReal;
import epmc.param.operatorevaluator.polynomialfraction.EvaluatorIsOnePolynomialFraction;
import epmc.param.operatorevaluator.polynomialfraction.EvaluatorIsZeroPolynomialFraction;
import epmc.param.operatorevaluator.polynomialfraction.EvaluatorMultiplyDividePolynomialFraction;
import epmc.param.operatorevaluator.polynomialfraction.EvaluatorPowPolynomialFractionInt;
import epmc.param.operatorevaluator.polynomialfraction.EvaluatorSetPolynomialFractionPolynomialFraction;
import epmc.param.operatorevaluator.rational.EvaluatorAddInverseRational;
import epmc.param.operatorevaluator.rational.EvaluatorAddSubtractRational;
import epmc.param.operatorevaluator.rational.EvaluatorCmpRational;
import epmc.param.operatorevaluator.rational.EvaluatorIsZeroIsOneRational;
import epmc.param.operatorevaluator.rational.EvaluatorMaxRational;
import epmc.param.operatorevaluator.rational.EvaluatorMultiplyInverseRational;
import epmc.param.operatorevaluator.rational.EvaluatorPowRational;
import epmc.param.operatorevaluator.rational.EvaluatorMultiplyDivideRational;
import epmc.param.operatorevaluator.rational.EvaluatorSetAlgebraRational;
import epmc.param.operatorevaluator.rational.EvaluatorSetRationalRational;
import epmc.param.options.OptionsParam;
import epmc.param.value.ParameterSet;
import epmc.param.value.TypeFunction;
import epmc.param.value.gmp.TypeMPQ;
import epmc.param.value.rational.TypeRational;
//import epmc.param.value.rational.TypeRationalBigInteger;
import epmc.plugin.BeforeModelCreation;
import epmc.value.TypeWeight;
import epmc.value.TypeWeightTransition;
import epmc.value.operatorevaluator.SimpleEvaluatorFactory;

public class BeforeModelLoadingPARAM implements BeforeModelCreation {
    public final static String PARAM_CONTEXT_VALUE_PARAM = "param-context-value-param";
    public final static String IDENTIFIER = "param-before-model-loading";
    
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }


	@Override
	public void process() throws EPMCException {
	    prepareEvaluators();
        prepareTypes();
        prepareParameters();
	}

	private void prepareEvaluators() {
	    prepareEvaluatorsPolynomial();
        prepareEvaluatorsPolynomialFraction();
        prepareEvaluatorsDag();
        prepareRationalGeneral();
        prepareRationalMPQ();
        prepareIntervals();
        SimpleEvaluatorFactory.get().add(EvaluatorSetFunctionReal.Builder.class);
    }

    private void prepareEvaluatorsPolynomial() {
        SimpleEvaluatorFactory.get().add(EvaluatorAddInversePolynomial.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorAddSubtractPolynomial.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorEvaluatePolynomialReal.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorIsOnePolynomial.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorIsZeroPolynomial.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorMultiplyPolynomial.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorPowPolynomialInt.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorSetPolynomialPolynomial.Builder.class);
    }
    
    private void prepareEvaluatorsPolynomialFraction() {
        SimpleEvaluatorFactory.get().add(EvaluatorAddInversePolynomialFraction.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorAddSubtractPolynomialFraction.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorEvaluatePolynomialFractionReal.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorIsOnePolynomialFraction.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorIsZeroPolynomialFraction.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorPowPolynomialFractionInt.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorMultiplyDividePolynomialFraction.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorSetPolynomialFractionPolynomialFraction.Builder.class);
    }

    private void prepareEvaluatorsDag() {
        SimpleEvaluatorFactory.get().add(EvaluatorAddInverseMultiplyInverseDag.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorAddSubtractMultiplyDivideDag.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorIsOneIsZeroDag.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorSetDagDag.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorPowDagInt.Builder.class);
    }

    private void prepareRationalGeneral() {
//       TypeRational.set(new TypeRationalBigInteger());
        SimpleEvaluatorFactory.get().add(EvaluatorSetRationalRational.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorSetAlgebraRational.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorAddInverseRational.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorAddSubtractRational.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorMultiplyInverseRational.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorMultiplyDivideRational.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorPowRational.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorCmpRational.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorIsZeroIsOneRational.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorMaxRational.Builder.class);
    }

    private void prepareRationalMPQ() {
        TypeRational.set(new TypeMPQ());
        SimpleEvaluatorFactory.get().add(EvaluatorAddInverseMultiplyInverseMPQ.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorAddSubtractMultiplyDivideMPQ.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorCmpMPQ.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorIsZeroIsOneMPQ.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorSetMPQMPQ.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorPowMPQ.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorMaxMPQ.Builder.class);
    }

    private void prepareIntervals() {
        TypeRational.set(new TypeMPQ());
        SimpleEvaluatorFactory.get().add(EvaluatorAddInverseInterval.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorAddSubtractInterval.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorMultiplicativeInverseInterval.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorMultiplyDivideInterval.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorSetIntervalInterval.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorPowInterval.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorSetIntervalIntervalDoubleFraction.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorSetIntervalDoubleFraction.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorNextUpDownDouble.Builder.class);
        SimpleEvaluatorFactory.get().add(EvaluatorSetMPQDoubleFraction.Builder.class);
    }
    
    private void prepareTypes() {
		Options options = Options.get();
        ParameterSet parameterSet = options.get(PARAM_CONTEXT_VALUE_PARAM);
        if (parameterSet == null) {
            parameterSet = new ParameterSet();
            options.set(PARAM_CONTEXT_VALUE_PARAM, parameterSet);
        }
        
        TypeFunction.Builder builder = UtilOptions.getInstance(OptionsParam.PARAM_FUNCTION_TYPE);
        TypeFunction typeFunction = builder
                .setParameters(parameterSet)
                .build();
        
        TypeFunction.set(typeFunction);
        TypeWeight.set(typeFunction);
        TypeWeightTransition.set(typeFunction);
	}

	private void prepareParameters() {
		Options options = Options.get();
        ParameterSet parameterSet = options.get(PARAM_CONTEXT_VALUE_PARAM);
        List<String> parameters = options.get(OptionsParam.PARAM_PARAMETER);
        assert parameters != null;
        // TODO check potential clash with constants
        for (String parameter : parameters) {
            parameterSet.addParameter(parameter);
        }
        
        Map<String,Class<? extends EvaluatorExplicit.Builder>> evaluatorsExplicit = options.get(OptionsExpressionBasic.EXPRESSION_EVALUTOR_EXPLICIT_CLASS);
        evaluatorsExplicit.put(EvaluatorExplicitIdentifierParameter.IDENTIFIER, EvaluatorExplicitIdentifierParameter.Builder.class);
	}
}
