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

package epmc.jani.exporter.operatorprocessor;

import static epmc.error.UtilError.ensure;

import java.util.HashMap;
import java.util.Map;

import epmc.expression.standard.ExpressionOperator;
import epmc.jani.exporter.error.ProblemsJANIExporter;
import epmc.operator.JANIExporter_OperatorAddProcessor;
import epmc.operator.JANIExporter_OperatorAndProcessor;
import epmc.operator.JANIExporter_OperatorEqProcessor;
import epmc.operator.JANIExporter_OperatorGtProcessor;
import epmc.operator.JANIExporter_OperatorIteProcessor;
import epmc.operator.JANIExporter_OperatorNeProcessor;
import epmc.operator.JANIExporter_OperatorNotProcessor;
import epmc.operator.JANIExporter_OperatorOrProcessor;
import epmc.operator.Operator;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorAnd;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorGt;
import epmc.operator.OperatorIte;
import epmc.operator.OperatorNe;
import epmc.operator.OperatorNot;
import epmc.operator.OperatorOr;
import epmc.util.Util;

/**
 * Class that is responsible for registering the operators and their corresponding operator processors.
 * 
 * @author Andrea Turrini
 *
 */
public class OperatorProcessorRegistrar {
    private static Map<Class<? extends Operator>, Class<? extends OperatorProcessor>> operatorProcessors = registerStrictOperatorProcessors();

    /**
     * Add a new operator processor for a JANI component in the set of known strict operator processors.
     * 
     * @param JANIComponent the JANI component to which associate the processor
     * @param operatorProcessor the corresponding processor
     */
    public static void registerStrictOperatorProcessor(Class<? extends Operator> operator, Class<? extends OperatorProcessor> operatorProcessor) {

        operatorProcessors.put(operator, operatorProcessor);
    }

    /**
     * Return the operator processor associated to the given JANI component.
     * 
     * @param expressionOperator the JANI component for which obtain the operator processor
     * @return the corresponding operator processor
     */
    public static OperatorProcessor getOperatorProcessor(ExpressionOperator expressionOperator) {
        assert expressionOperator != null;

        OperatorProcessor processor = null;
        Class<? extends OperatorProcessor> operatorProcessorClass = operatorProcessors.get(expressionOperator.getOperator().getClass());
        if (operatorProcessorClass != null) {
            processor = Util.getInstance(operatorProcessorClass)
                    .setExpressionOperator(expressionOperator);
        } else {
            ensure(false, 
                    ProblemsJANIExporter.JANI_EXPORTER_ERROR_UNKNOWN_PROCESSOR, 
                    expressionOperator.getClass().getSimpleName());
        }

        return processor;
    }

    private static Map<Class<? extends Operator>, Class<? extends OperatorProcessor>> registerStrictOperatorProcessors() {
        Map<Class<? extends Operator>, Class<? extends OperatorProcessor>> operatorProcessors = new HashMap<>();
        
//        operatorProcessors.put(OperatorAddInverse.class, JANIExporter_OperatorAddInverseProcessor.class);
        operatorProcessors.put(OperatorAdd.class, JANIExporter_OperatorAddProcessor.class);
        operatorProcessors.put(OperatorAnd.class, JANIExporter_OperatorAndProcessor.class);
//        operatorProcessors.put(OperatorCeil.class, JANIExporter_OperatorCeilProcessor.class);
//        operatorProcessors.put(OperatorDivide.class, JANIExporter_OperatorDivideProcessor.class);
        operatorProcessors.put(OperatorEq.class, JANIExporter_OperatorEqProcessor.class);
//        operatorProcessors.put(OperatorExp.class, JANIExporter_OperatorExpProcessor.class);
//        operatorProcessors.put(OperatorFloor.class, JANIExporter_OperatorFloorProcessor.class);
//        operatorProcessors.put(OperatorGe.class, JANIExporter_OperatorGeProcessor.class);
        operatorProcessors.put(OperatorGt.class, JANIExporter_OperatorGtProcessor.class);
//        operatorProcessors.put(OperatorIff.class, JANIExporter_OperatorIffProcessor.class);
//        operatorProcessors.put(OperatorImplies.class, JANIExporter_OperatorImpliesProcessor.class);
        operatorProcessors.put(OperatorIte.class, JANIExporter_OperatorIteProcessor.class);
//        operatorProcessors.put(OperatorLe.class, JANIExporter_OperatorLeProcessor.class);
//        operatorProcessors.put(OperatorLn.class, JANIExporter_OperatorLnProcessor.class);
//        operatorProcessors.put(OperatorLog.class, JANIExporter_OperatorLogProcessor.class);
//        operatorProcessors.put(OperatorLt.class, JANIExporter_OperatorLtProcessor.class);
//        operatorProcessors.put(OperatorMax.class, JANIExporter_OperatorMaxProcessor.class);
//        operatorProcessors.put(OperatorMin.class, JANIExporter_OperatorMinProcessor.class);
//        operatorProcessors.put(OperatorMod.class, JANIExporter_OperatorModProcessor.class);
//        operatorProcessors.put(OperatorMultiplyInverse.class, JANIExporter_OperatorMultiplyInverseProcessor.class);
//        operatorProcessors.put(OperatorMultiply.class, JANIExporter_OperatorMultiplyProcessor.class);
        operatorProcessors.put(OperatorNe.class, JANIExporter_OperatorNeProcessor.class);
        operatorProcessors.put(OperatorNot.class, JANIExporter_OperatorNotProcessor.class);
        operatorProcessors.put(OperatorOr.class, JANIExporter_OperatorOrProcessor.class);
//        operatorProcessors.put(OperatorPow.class, JANIExporter_OperatorPowProcessor.class);
//        operatorProcessors.put(OperatorSqrt.class, JANIExporter_OperatorSqrtProcessor.class);
//        operatorProcessors.put(OperatorSubtract.class, JANIExporter_OperatorSubtractProcessor.class);
        
        return operatorProcessors;
    }
}
