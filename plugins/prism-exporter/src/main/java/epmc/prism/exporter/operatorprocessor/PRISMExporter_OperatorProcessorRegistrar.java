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
import epmc.operator.PRISMExporter_OperatorAbsProcessor;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorAddInverse;
import epmc.operator.PRISMExporter_OperatorAddInverseProcessor;
import epmc.operator.PRISMExporter_OperatorAddProcessor;
import epmc.operator.OperatorAnd;
import epmc.operator.PRISMExporter_OperatorAndProcessor;
import epmc.operator.OperatorCeil;
import epmc.operator.PRISMExporter_OperatorCeilProcessor;
import epmc.operator.OperatorDistance;
import epmc.operator.PRISMExporter_OperatorDistanceProcessor;
import epmc.operator.OperatorDivide;
import epmc.operator.OperatorDivideIgnoreZero;
import epmc.operator.PRISMExporter_OperatorDivideIgnoreZeroProcessor;
import epmc.operator.PRISMExporter_OperatorDivideProcessor;
import epmc.operator.OperatorEq;
import epmc.operator.PRISMExporter_OperatorEqProcessor;
import epmc.operator.OperatorExp;
import epmc.operator.PRISMExporter_OperatorExpProcessor;
import epmc.operator.OperatorFloor;
import epmc.operator.PRISMExporter_OperatorFloorProcessor;
import epmc.operator.OperatorGe;
import epmc.operator.PRISMExporter_OperatorGeProcessor;
import epmc.operator.OperatorGt;
import epmc.operator.PRISMExporter_OperatorGtProcessor;
import epmc.operator.OperatorId;
import epmc.operator.PRISMExporter_OperatorIdProcessor;
import epmc.operator.OperatorIff;
import epmc.operator.PRISMExporter_OperatorIffProcessor;
import epmc.operator.OperatorImplies;
import epmc.operator.PRISMExporter_OperatorImpliesProcessor;
import epmc.operator.OperatorIsNegInf;
import epmc.operator.PRISMExporter_OperatorIsNegInfProcessor;
import epmc.operator.OperatorIsOne;
import epmc.operator.PRISMExporter_OperatorIsOneProcessor;
import epmc.operator.OperatorIsPosInf;
import epmc.operator.PRISMExporter_OperatorIsPosInfProcessor;
import epmc.operator.OperatorIsZero;
import epmc.operator.PRISMExporter_OperatorIsZeroProcessor;
import epmc.operator.OperatorIte;
import epmc.operator.PRISMExporter_OperatorIteProcessor;
import epmc.operator.OperatorLe;
import epmc.operator.PRISMExporter_OperatorLeProcessor;
import epmc.operator.OperatorLn;
import epmc.operator.PRISMExporter_OperatorLnProcessor;
import epmc.operator.OperatorLog;
import epmc.operator.PRISMExporter_OperatorLogProcessor;
import epmc.operator.OperatorLt;
import epmc.operator.PRISMExporter_OperatorLtProcessor;
import epmc.operator.OperatorMax;
import epmc.operator.PRISMExporter_OperatorMaxProcessor;
import epmc.operator.OperatorMin;
import epmc.operator.PRISMExporter_OperatorMinProcessor;
import epmc.operator.OperatorMod;
import epmc.operator.PRISMExporter_OperatorModProcessor;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorMultiplyInverse;
import epmc.operator.PRISMExporter_OperatorMultiplyInverseProcessor;
import epmc.operator.PRISMExporter_OperatorMultiplyProcessor;
import epmc.operator.OperatorNe;
import epmc.operator.PRISMExporter_OperatorNeProcessor;
import epmc.operator.OperatorNot;
import epmc.operator.PRISMExporter_OperatorNotProcessor;
import epmc.operator.OperatorOr;
import epmc.operator.PRISMExporter_OperatorOrProcessor;
import epmc.operator.OperatorOverflow;
import epmc.operator.PRISMExporter_OperatorOverflowProcessor;
import epmc.operator.PRISMExporter_OperatorPRISMPowProcessor;
import epmc.operator.OperatorPow;
import epmc.operator.PRISMExporter_OperatorPowProcessor;
import epmc.operator.OperatorSet;
import epmc.operator.PRISMExporter_OperatorSetProcessor;
import epmc.operator.OperatorSqrt;
import epmc.operator.PRISMExporter_OperatorSqrtProcessor;
import epmc.operator.OperatorSubtract;
import epmc.operator.PRISMExporter_OperatorSubtractProcessor;
import epmc.operator.OperatorUnderflow;
import epmc.operator.PRISMExporter_OperatorUnderflowProcessor;
import epmc.operator.OperatorWiden;
import epmc.operator.PRISMExporter_OperatorWidenProcessor;
import epmc.prism.exporter.error.ProblemsPRISMExporter;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorRegistrar;
import epmc.prism.operator.OperatorPRISMPow;
import epmc.util.Util;

/**
 * Class that is responsible for registering the JANI components and their corresponding JANI2PRISM operator processors.
 * 
 * @author Andrea Turrini
 *
 */
public class PRISMExporter_OperatorProcessorRegistrar {
    private static Map<Class<? extends Operator>, Class<? extends PRISMExporter_OperatorProcessorStrict>> strictOperatorProcessors = registerStrictOperatorProcessors();
    private static Map<Class<? extends Operator>, Class<? extends PRISMExporter_OperatorProcessorExtended>> extendedOperatorProcessors = registerExtendedOperatorProcessors();
    private static Map<Class<? extends Operator>, Class<? extends PRISMExporter_OperatorProcessorNonPRISM>> nonPRISMOperatorProcessors = registerNonPRISMOperatorProcessors();

    /**
     * Add a new operator processor for a JANI component in the set of known strict operator processors.
     * 
     * @param JANIComponent the JANI component to which associate the processor
     * @param operatorProcessor the corresponding processor
     */
    public static void registerStrictOperatorProcessor(Class<? extends Operator> operator, Class<? extends PRISMExporter_OperatorProcessorStrict> operatorProcessor) {
        assert !PRISMExporter_OperatorProcessorExtended.class.isAssignableFrom(operatorProcessor);

        strictOperatorProcessors.put(operator, operatorProcessor);
    }

    /**
     * Add a new operator processor for a JANI component in the set of known extended operator processors.
     * 
     * @param operator the JANI component to which associate the processor
     * @param operatorProcessor the corresponding processor
     */
    public static void registerExtendedOperatorProcessor(Class<? extends Operator> operator, Class<? extends PRISMExporter_OperatorProcessorExtended> operatorProcessor) {
        extendedOperatorProcessors.put(operator, operatorProcessor);
    }

    /**
     * Add a new operator processor for a JANI component in the set of known non-PRISM operator processors.
     * 
     * @param operator the JANI component to which associate the processor
     * @param operatorProcessor the corresponding processor
     */
    public static void registerNonPRISMOperatorProcessor(Class<? extends Operator> operator, Class<? extends PRISMExporter_OperatorProcessorNonPRISM> operatorProcessor) {
        nonPRISMOperatorProcessors.put(operator, operatorProcessor);
    }

    /**
     * Return the operator processor associated to the given JANI component.
     * 
     * @param expressionOperator the JANI component for which obtain the operator processor
     * @return the corresponding operator processor
     */
    public static PRISMExporter_OperatorProcessorStrict getOperatorProcessor(ExpressionOperator expressionOperator) {
        assert expressionOperator != null;

        PRISMExporter_OperatorProcessorStrict processor = null;
        Class<? extends PRISMExporter_OperatorProcessorStrict> operatorProcessorClass = strictOperatorProcessors.get(expressionOperator.getOperator().getClass());
        if (operatorProcessorClass != null) {
            processor = Util.getInstance(operatorProcessorClass)
                    .setExpressionOperator(expressionOperator);
        } else {
            Class<? extends PRISMExporter_OperatorProcessorExtended> extendedOperatorProcessorClass = extendedOperatorProcessors.get(expressionOperator.getClass());
            if (extendedOperatorProcessorClass != null) {
                processor = Util.getInstance(extendedOperatorProcessorClass)
                        .setExpressionOperator(expressionOperator);
                ensure(PRISMExporter_ProcessorRegistrar.getUseExtendedPRISMSyntax(), 
                        ProblemsPRISMExporter.PRISM_EXPORTER_ERROR_EXTENDED_SYNTAX_REQUIRED, 
                        ((PRISMExporter_OperatorProcessorExtended)processor).getUnsupportedFeature()
                            .toArray());
            } else {
                Class<? extends PRISMExporter_OperatorProcessorNonPRISM> nonPRISMOperatorProcessorClass = nonPRISMOperatorProcessors.get(expressionOperator.getClass());
                if (nonPRISMOperatorProcessorClass != null) {
                    processor = Util.getInstance(nonPRISMOperatorProcessorClass)
                            .setExpressionOperator(expressionOperator);
                    ensure(PRISMExporter_ProcessorRegistrar.getUseNonPRISMSyntax(), 
                            ProblemsPRISMExporter.PRISM_EXPORTER_ERROR_EXTENDED_SYNTAX_REQUIRED, 
                            ((PRISMExporter_OperatorProcessorNonPRISM)processor).getUnsupportedFeature()
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

    private static Map<Class<? extends Operator>, Class<? extends PRISMExporter_OperatorProcessorStrict>> registerStrictOperatorProcessors() {
        Map<Class<? extends Operator>, Class<? extends PRISMExporter_OperatorProcessorStrict>> operatorProcessors = new HashMap<>();
        
        operatorProcessors.put(OperatorAddInverse.class, PRISMExporter_OperatorAddInverseProcessor.class);
        operatorProcessors.put(OperatorAdd.class, PRISMExporter_OperatorAddProcessor.class);
        operatorProcessors.put(OperatorAnd.class, PRISMExporter_OperatorAndProcessor.class);
        operatorProcessors.put(OperatorCeil.class, PRISMExporter_OperatorCeilProcessor.class);
        operatorProcessors.put(OperatorDivide.class, PRISMExporter_OperatorDivideProcessor.class);
        operatorProcessors.put(OperatorEq.class, PRISMExporter_OperatorEqProcessor.class);
        operatorProcessors.put(OperatorExp.class, PRISMExporter_OperatorExpProcessor.class);
        operatorProcessors.put(OperatorFloor.class, PRISMExporter_OperatorFloorProcessor.class);
        operatorProcessors.put(OperatorGe.class, PRISMExporter_OperatorGeProcessor.class);
        operatorProcessors.put(OperatorGt.class, PRISMExporter_OperatorGtProcessor.class);
        operatorProcessors.put(OperatorIff.class, PRISMExporter_OperatorIffProcessor.class);
        operatorProcessors.put(OperatorImplies.class, PRISMExporter_OperatorImpliesProcessor.class);
        operatorProcessors.put(OperatorIte.class, PRISMExporter_OperatorIteProcessor.class);
        operatorProcessors.put(OperatorLe.class, PRISMExporter_OperatorLeProcessor.class);
        operatorProcessors.put(OperatorLn.class, PRISMExporter_OperatorLnProcessor.class);
        operatorProcessors.put(OperatorLog.class, PRISMExporter_OperatorLogProcessor.class);
        operatorProcessors.put(OperatorLt.class, PRISMExporter_OperatorLtProcessor.class);
        operatorProcessors.put(OperatorMax.class, PRISMExporter_OperatorMaxProcessor.class);
        operatorProcessors.put(OperatorMin.class, PRISMExporter_OperatorMinProcessor.class);
        operatorProcessors.put(OperatorMod.class, PRISMExporter_OperatorModProcessor.class);
        operatorProcessors.put(OperatorMultiplyInverse.class, PRISMExporter_OperatorMultiplyInverseProcessor.class);
        operatorProcessors.put(OperatorMultiply.class, PRISMExporter_OperatorMultiplyProcessor.class);
        operatorProcessors.put(OperatorNe.class, PRISMExporter_OperatorNeProcessor.class);
        operatorProcessors.put(OperatorNot.class, PRISMExporter_OperatorNotProcessor.class);
        operatorProcessors.put(OperatorOr.class, PRISMExporter_OperatorOrProcessor.class);
        operatorProcessors.put(OperatorPow.class, PRISMExporter_OperatorPowProcessor.class);
        operatorProcessors.put(OperatorPRISMPow.class, PRISMExporter_OperatorPRISMPowProcessor.class);
        operatorProcessors.put(OperatorSqrt.class, PRISMExporter_OperatorSqrtProcessor.class);
        operatorProcessors.put(OperatorSubtract.class, PRISMExporter_OperatorSubtractProcessor.class);
        
        return operatorProcessors;
    }

    private static Map<Class<? extends Operator>, Class<? extends PRISMExporter_OperatorProcessorExtended>> registerExtendedOperatorProcessors() {
        Map<Class<? extends Operator>, Class<? extends PRISMExporter_OperatorProcessorExtended>> operatorProcessors = new HashMap<>();
        
        return operatorProcessors;
    }

    private static Map<Class<? extends Operator>, Class<? extends PRISMExporter_OperatorProcessorNonPRISM>> registerNonPRISMOperatorProcessors() {
        Map<Class<? extends Operator>, Class<? extends PRISMExporter_OperatorProcessorNonPRISM>> operatorProcessors = new HashMap<>();
        
        operatorProcessors.put(OperatorAbs.class, PRISMExporter_OperatorAbsProcessor.class);
        operatorProcessors.put(OperatorDistance.class, PRISMExporter_OperatorDistanceProcessor.class);
        operatorProcessors.put(OperatorDivideIgnoreZero.class, PRISMExporter_OperatorDivideIgnoreZeroProcessor.class);
        operatorProcessors.put(OperatorId.class, PRISMExporter_OperatorIdProcessor.class);
        operatorProcessors.put(OperatorIsNegInf.class, PRISMExporter_OperatorIsNegInfProcessor.class);
        operatorProcessors.put(OperatorIsOne.class, PRISMExporter_OperatorIsOneProcessor.class);
        operatorProcessors.put(OperatorIsPosInf.class, PRISMExporter_OperatorIsPosInfProcessor.class);
        operatorProcessors.put(OperatorIsZero.class, PRISMExporter_OperatorIsZeroProcessor.class);
        operatorProcessors.put(OperatorOverflow.class, PRISMExporter_OperatorOverflowProcessor.class);
        operatorProcessors.put(OperatorSet.class, PRISMExporter_OperatorSetProcessor.class);
        operatorProcessors.put(OperatorUnderflow.class, PRISMExporter_OperatorUnderflowProcessor.class);
        operatorProcessors.put(OperatorWiden.class, PRISMExporter_OperatorWidenProcessor.class);
        
        return operatorProcessors;
    }
}
