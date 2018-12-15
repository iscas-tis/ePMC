/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

package epmc.prism.exporter.operatorprocessor;

import static epmc.error.UtilError.ensure;

import java.util.HashMap;
import java.util.Map;

import epmc.expression.standard.ExpressionOperator;
import epmc.operator.Operator;
import epmc.operator.OperatorAbs;
import epmc.operator.OperatorAbsProcessor;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorAddInverse;
import epmc.operator.OperatorAddInverseProcessor;
import epmc.operator.OperatorAddProcessor;
import epmc.operator.OperatorAnd;
import epmc.operator.OperatorAndProcessor;
import epmc.operator.OperatorCeil;
import epmc.operator.OperatorCeilProcessor;
import epmc.operator.OperatorDistance;
import epmc.operator.OperatorDistanceProcessor;
import epmc.operator.OperatorDivide;
import epmc.operator.OperatorDivideIgnoreZero;
import epmc.operator.OperatorDivideIgnoreZeroProcessor;
import epmc.operator.OperatorDivideProcessor;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorEqProcessor;
import epmc.operator.OperatorExp;
import epmc.operator.OperatorExpProcessor;
import epmc.operator.OperatorFloor;
import epmc.operator.OperatorFloorProcessor;
import epmc.operator.OperatorGe;
import epmc.operator.OperatorGeProcessor;
import epmc.operator.OperatorGt;
import epmc.operator.OperatorGtProcessor;
import epmc.operator.OperatorId;
import epmc.operator.OperatorIdProcessor;
import epmc.operator.OperatorIff;
import epmc.operator.OperatorIffProcessor;
import epmc.operator.OperatorImplies;
import epmc.operator.OperatorImpliesProcessor;
import epmc.operator.OperatorIsNegInf;
import epmc.operator.OperatorIsNegInfProcessor;
import epmc.operator.OperatorIsOne;
import epmc.operator.OperatorIsOneProcessor;
import epmc.operator.OperatorIsPosInf;
import epmc.operator.OperatorIsPosInfProcessor;
import epmc.operator.OperatorIsZero;
import epmc.operator.OperatorIsZeroProcessor;
import epmc.operator.OperatorIte;
import epmc.operator.OperatorIteProcessor;
import epmc.operator.OperatorLe;
import epmc.operator.OperatorLeProcessor;
import epmc.operator.OperatorLt;
import epmc.operator.OperatorLtProcessor;
import epmc.operator.OperatorMax;
import epmc.operator.OperatorMaxProcessor;
import epmc.operator.OperatorMin;
import epmc.operator.OperatorMinProcessor;
import epmc.operator.OperatorMod;
import epmc.operator.OperatorModProcessor;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorMultiplyInverse;
import epmc.operator.OperatorMultiplyInverseProcessor;
import epmc.operator.OperatorMultiplyProcessor;
import epmc.operator.OperatorNe;
import epmc.operator.OperatorNeProcessor;
import epmc.operator.OperatorNot;
import epmc.operator.OperatorNotProcessor;
import epmc.operator.OperatorOr;
import epmc.operator.OperatorOrProcessor;
import epmc.operator.OperatorOverflow;
import epmc.operator.OperatorOverflowProcessor;
import epmc.operator.OperatorPow;
import epmc.operator.OperatorPowProcessor;
import epmc.operator.OperatorSet;
import epmc.operator.OperatorSetProcessor;
import epmc.operator.OperatorSqrt;
import epmc.operator.OperatorSqrtProcessor;
import epmc.operator.OperatorSubtract;
import epmc.operator.OperatorSubtractProcessor;
import epmc.operator.OperatorUnderflow;
import epmc.operator.OperatorUnderflowProcessor;
import epmc.operator.OperatorWiden;
import epmc.operator.OperatorWidenProcessor;
import epmc.prism.exporter.error.ProblemsPRISMExporter;
import epmc.prism.exporter.processor.ProcessorRegistrar;
import epmc.util.Util;

/**
 * Class that is responsible for registering the JANI components and their corresponding JANI2PRISM operator processors.
 * 
 * @author Andrea Turrini
 *
 */
public class OperatorProcessorRegistrar {
    private static Map<Class<? extends Operator>, Class<? extends JANI2PRISMOperatorProcessorStrict>> strictOperatorProcessors = registerStrictOperatorProcessors();
    private static Map<Class<? extends Operator>, Class<? extends JANI2PRISMOperatorProcessorExtended>> extendedOperatorProcessors = registerExtendedOperatorProcessors();
    private static Map<Class<? extends Operator>, Class<? extends JANI2PRISMOperatorProcessorNonPRISM>> nonPRISMOperatorProcessors = registerNonPRISMOperatorProcessors();

    /**
     * Add a new operator processor for a JANI component in the set of known strict operator processors.
     * 
     * @param JANIComponent the JANI component to which associate the processor
     * @param operatorProcessor the corresponding processor
     */
    public static void registerStrictOperatorProcessor(Class<? extends Operator> operator, Class<? extends JANI2PRISMOperatorProcessorStrict> operatorProcessor) {
        assert !JANI2PRISMOperatorProcessorExtended.class.isAssignableFrom(operatorProcessor);

        strictOperatorProcessors.put(operator, operatorProcessor);
    }

    /**
     * Add a new operator processor for a JANI component in the set of known extended operator processors.
     * 
     * @param operator the JANI component to which associate the processor
     * @param operatorProcessor the corresponding processor
     */
    public static void registerExtendedOperatorProcessor(Class<? extends Operator> operator, Class<? extends JANI2PRISMOperatorProcessorExtended> operatorProcessor) {
        extendedOperatorProcessors.put(operator, operatorProcessor);
    }

    /**
     * Add a new operator processor for a JANI component in the set of known non-PRISM operator processors.
     * 
     * @param operator the JANI component to which associate the processor
     * @param operatorProcessor the corresponding processor
     */
    public static void registerNonPRISMOperatorProcessor(Class<? extends Operator> operator, Class<? extends JANI2PRISMOperatorProcessorNonPRISM> operatorProcessor) {
        nonPRISMOperatorProcessors.put(operator, operatorProcessor);
    }

    /**
     * Return the operator processor associated to the given JANI component.
     * 
     * @param expressionOperator the JANI component for which obtain the operator processor
     * @return the corresponding operator processor
     */
    public static JANI2PRISMOperatorProcessorStrict getOperatorProcessor(Operator operator, ExpressionOperator expressionOperator) {
        assert expressionOperator != null;

        JANI2PRISMOperatorProcessorStrict processor = null;
        Class<? extends JANI2PRISMOperatorProcessorStrict> operatorProcessorClass = strictOperatorProcessors.get(operator.getClass());
        if (operatorProcessorClass != null) {
            processor = Util.getInstance(operatorProcessorClass)
                    .setExpressionOperator(expressionOperator);
        } else {
            Class<? extends JANI2PRISMOperatorProcessorExtended> extendedOperatorProcessorClass = extendedOperatorProcessors.get(expressionOperator.getClass());
            if (extendedOperatorProcessorClass != null) {
                processor = Util.getInstance(extendedOperatorProcessorClass)
                        .setExpressionOperator(expressionOperator);
                ensure(ProcessorRegistrar.getUseExtendedPRISMSyntax(), 
                        ProblemsPRISMExporter.PRISM_EXPORTER_ERROR_EXTENDED_SYNTAX_REQUIRED, 
                        ((JANI2PRISMOperatorProcessorExtended)processor).getUnsupportedFeature()
                            .toArray());
            } else {
                Class<? extends JANI2PRISMOperatorProcessorNonPRISM> nonPRISMOperatorProcessorClass = nonPRISMOperatorProcessors.get(expressionOperator.getClass());
                if (nonPRISMOperatorProcessorClass != null) {
                    processor = Util.getInstance(nonPRISMOperatorProcessorClass)
                            .setExpressionOperator(expressionOperator);
                    ensure(ProcessorRegistrar.getUseNonPRISMSyntax(), 
                            ProblemsPRISMExporter.PRISM_EXPORTER_ERROR_EXTENDED_SYNTAX_REQUIRED, 
                            ((JANI2PRISMOperatorProcessorNonPRISM)processor).getUnsupportedFeature()
                                .toArray());
                } else {
                    ensure(false, 
                            ProblemsPRISMExporter.PRISM_EXPORTER_ERROR_UNKNOWN_PROCESSOR, 
                            expressionOperator.getClass().getSimpleName());
                }
            }
        }

        return processor;
    }

    private static Map<Class<? extends Operator>, Class<? extends JANI2PRISMOperatorProcessorStrict>> registerStrictOperatorProcessors() {
        Map<Class<? extends Operator>, Class<? extends JANI2PRISMOperatorProcessorStrict>> operatorProcessors = new HashMap<>();
        
        operatorProcessors.put(OperatorAddInverse.class, OperatorAddInverseProcessor.class);
        operatorProcessors.put(OperatorAdd.class, OperatorAddProcessor.class);
        operatorProcessors.put(OperatorAnd.class, OperatorAndProcessor.class);
        operatorProcessors.put(OperatorCeil.class, OperatorCeilProcessor.class);
        operatorProcessors.put(OperatorDivide.class, OperatorDivideProcessor.class);
        operatorProcessors.put(OperatorEq.class, OperatorEqProcessor.class);
        operatorProcessors.put(OperatorExp.class, OperatorExpProcessor.class);
        operatorProcessors.put(OperatorFloor.class, OperatorFloorProcessor.class);
        operatorProcessors.put(OperatorGe.class, OperatorGeProcessor.class);
        operatorProcessors.put(OperatorGt.class, OperatorGtProcessor.class);
        operatorProcessors.put(OperatorIff.class, OperatorIffProcessor.class);
        operatorProcessors.put(OperatorImplies.class, OperatorImpliesProcessor.class);
        operatorProcessors.put(OperatorIte.class, OperatorIteProcessor.class);
        operatorProcessors.put(OperatorLe.class, OperatorLeProcessor.class);
//        TODO: re-enable only after the issue about the meaning of the log operator is fixed
//        operatorProcessors.put(OperatorLog.class, OperatorLogProcessor.class);
        operatorProcessors.put(OperatorLt.class, OperatorLtProcessor.class);
        operatorProcessors.put(OperatorMax.class, OperatorMaxProcessor.class);
        operatorProcessors.put(OperatorMin.class, OperatorMinProcessor.class);
        operatorProcessors.put(OperatorMod.class, OperatorModProcessor.class);
        operatorProcessors.put(OperatorMultiplyInverse.class, OperatorMultiplyInverseProcessor.class);
        operatorProcessors.put(OperatorMultiply.class, OperatorMultiplyProcessor.class);
        operatorProcessors.put(OperatorNe.class, OperatorNeProcessor.class);
        operatorProcessors.put(OperatorNot.class, OperatorNotProcessor.class);
        operatorProcessors.put(OperatorOr.class, OperatorOrProcessor.class);
        operatorProcessors.put(OperatorPow.class, OperatorPowProcessor.class);
        operatorProcessors.put(OperatorSqrt.class, OperatorSqrtProcessor.class);
        operatorProcessors.put(OperatorSubtract.class, OperatorSubtractProcessor.class);
        
        return operatorProcessors;
    }

    private static Map<Class<? extends Operator>, Class<? extends JANI2PRISMOperatorProcessorExtended>> registerExtendedOperatorProcessors() {
        Map<Class<? extends Operator>, Class<? extends JANI2PRISMOperatorProcessorExtended>> operatorProcessors = new HashMap<>();
        
        return operatorProcessors;
    }

    private static Map<Class<? extends Operator>, Class<? extends JANI2PRISMOperatorProcessorNonPRISM>> registerNonPRISMOperatorProcessors() {
        Map<Class<? extends Operator>, Class<? extends JANI2PRISMOperatorProcessorNonPRISM>> operatorProcessors = new HashMap<>();
        
        operatorProcessors.put(OperatorAbs.class, OperatorAbsProcessor.class);
        operatorProcessors.put(OperatorDistance.class, OperatorDistanceProcessor.class);
        operatorProcessors.put(OperatorDivideIgnoreZero.class, OperatorDivideIgnoreZeroProcessor.class);
        operatorProcessors.put(OperatorId.class, OperatorIdProcessor.class);
        operatorProcessors.put(OperatorIsNegInf.class, OperatorIsNegInfProcessor.class);
        operatorProcessors.put(OperatorIsOne.class, OperatorIsOneProcessor.class);
        operatorProcessors.put(OperatorIsPosInf.class, OperatorIsPosInfProcessor.class);
        operatorProcessors.put(OperatorIsZero.class, OperatorIsZeroProcessor.class);
        operatorProcessors.put(OperatorOverflow.class, OperatorOverflowProcessor.class);
        operatorProcessors.put(OperatorSet.class, OperatorSetProcessor.class);
        operatorProcessors.put(OperatorUnderflow.class, OperatorUnderflowProcessor.class);
        operatorProcessors.put(OperatorWiden.class, OperatorWidenProcessor.class);
        
        return operatorProcessors;
    }
}
