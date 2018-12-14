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

import epmc.operator.Operator;
import epmc.operator.OperatorAbs;
import epmc.operator.OperatorAbsProcessor;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorAddProcessor;
import epmc.operator.OperatorDistance;
import epmc.operator.OperatorDistanceProcessor;
import epmc.operator.OperatorId;
import epmc.operator.OperatorIdProcessor;
import epmc.operator.OperatorIsNegInf;
import epmc.operator.OperatorIsNegInfProcessor;
import epmc.operator.OperatorIsOne;
import epmc.operator.OperatorIsOneProcessor;
import epmc.operator.OperatorIsPosInf;
import epmc.operator.OperatorIsPosInfProcessor;
import epmc.operator.OperatorIsZero;
import epmc.operator.OperatorIsZeroProcessor;
import epmc.operator.OperatorOverflow;
import epmc.operator.OperatorOverflowProcessor;
import epmc.operator.OperatorSet;
import epmc.operator.OperatorSetProcessor;
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
    private static Map<Class<? extends Object>, Class<? extends JANI2PRISMOperatorProcessorStrict>> strictOperatorProcessors = registerStrictOperatorProcessors();
    private static Map<Class<? extends Object>, Class<? extends JANI2PRISMOperatorProcessorExtended>> extendedOperatorProcessors = registerExtendedOperatorProcessors();
    private static Map<Class<? extends Object>, Class<? extends JANI2PRISMOperatorProcessorNonPRISM>> nonPRISMOperatorProcessors = registerNonPRISMOperatorProcessors();

    /**
     * Add a new operator processor for a JANI component in the set of known strict operator processors.
     * 
     * @param JANIComponent the JANI component to which associate the processor
     * @param JANI2PRISMOperatorProcessor the corresponding processor
     */
    public static void registerStrictOperatorProcessor(Class<? extends Object> JANIComponent, Class<? extends JANI2PRISMOperatorProcessorStrict> JANI2PRISMOperatorProcessor) {
        assert !JANI2PRISMOperatorProcessorExtended.class.isAssignableFrom(JANI2PRISMOperatorProcessor);

        strictOperatorProcessors.put(JANIComponent, JANI2PRISMOperatorProcessor);
    }

    /**
     * Add a new operator processor for a JANI component in the set of known extended operator processors.
     * 
     * @param JANIComponent the JANI component to which associate the processor
     * @param JANI2PRISMOperatorProcessor the corresponding processor
     */
    public static void registerExtendedOperatorProcessor(Class<? extends Object> JANIComponent, Class<? extends JANI2PRISMOperatorProcessorExtended> JANI2PRISMOperatorProcessor) {
        extendedOperatorProcessors.put(JANIComponent, JANI2PRISMOperatorProcessor);
    }

    /**
     * Add a new operator processor for a JANI component in the set of known non-PRISM operator processors.
     * 
     * @param JANIComponent the JANI component to which associate the processor
     * @param JANI2PRISMOperatorProcessor the corresponding processor
     */
    public static void registerNonPRISMOperatorProcessor(Class<? extends Object> JANIComponent, Class<? extends JANI2PRISMOperatorProcessorNonPRISM> JANI2PRISMOperatorProcessor) {
        nonPRISMOperatorProcessors.put(JANIComponent, JANI2PRISMOperatorProcessor);
    }

    /**
     * Return the operator processor associated to the given JANI component.
     * 
     * @param JANIComponent the JANI component for which obtain the operator processor
     * @return the corresponding operator processor
     */
    public static JANI2PRISMOperatorProcessorStrict getOperatorProcessor(Operator operator, Object JANIComponent) {
        assert JANIComponent != null;

        JANI2PRISMOperatorProcessorStrict processor = null;
        Class<? extends JANI2PRISMOperatorProcessorStrict> operatorProcessorClass = strictOperatorProcessors.get(JANIComponent.getClass());
        if (operatorProcessorClass != null) {
            processor = Util.getInstance(operatorProcessorClass)
                    .setOperatorElement(operator, JANIComponent);
        } else {
            Class<? extends JANI2PRISMOperatorProcessorExtended> extendedOperatorProcessorClass = extendedOperatorProcessors.get(JANIComponent.getClass());
            if (extendedOperatorProcessorClass != null) {
                processor = Util.getInstance(extendedOperatorProcessorClass)
                        .setOperatorElement(operator, JANIComponent);
                ensure(ProcessorRegistrar.getUseExtendedPRISMSyntax(), 
                        ProblemsPRISMExporter.PRISM_EXPORTER_ERROR_EXTENDED_SYNTAX_REQUIRED, 
                        ((JANI2PRISMOperatorProcessorExtended)processor).getUnsupportedFeature()
                            .toArray());
            } else {
                Class<? extends JANI2PRISMOperatorProcessorNonPRISM> nonPRISMOperatorProcessorClass = nonPRISMOperatorProcessors.get(JANIComponent.getClass());
                if (nonPRISMOperatorProcessorClass != null) {
                    processor = Util.getInstance(nonPRISMOperatorProcessorClass)
                            .setOperatorElement(operator, JANIComponent);
                    ensure(ProcessorRegistrar.getUseNonPRISMSyntax(), 
                            ProblemsPRISMExporter.PRISM_EXPORTER_ERROR_EXTENDED_SYNTAX_REQUIRED, 
                            ((JANI2PRISMOperatorProcessorNonPRISM)processor).getUnsupportedFeature()
                                .toArray());
                } else {
                    ensure(false, 
                            ProblemsPRISMExporter.PRISM_EXPORTER_ERROR_UNKNOWN_PROCESSOR, 
                            JANIComponent.getClass().getSimpleName());
                }
            }
        }

        return processor;
    }

    private static Map<Class<? extends Object>, Class<? extends JANI2PRISMOperatorProcessorStrict>> registerStrictOperatorProcessors() {
        Map<Class<? extends Object>, Class<? extends JANI2PRISMOperatorProcessorStrict>> operatorProcessors = new HashMap<>();
        
        operatorProcessors.put(OperatorAdd.class, OperatorAddProcessor.class);
        
        return operatorProcessors;
    }

    private static Map<Class<? extends Object>, Class<? extends JANI2PRISMOperatorProcessorExtended>> registerExtendedOperatorProcessors() {
        Map<Class<? extends Object>, Class<? extends JANI2PRISMOperatorProcessorExtended>> operatorProcessors = new HashMap<>();
        
        return operatorProcessors;
    }

    private static Map<Class<? extends Object>, Class<? extends JANI2PRISMOperatorProcessorNonPRISM>> registerNonPRISMOperatorProcessors() {
        Map<Class<? extends Object>, Class<? extends JANI2PRISMOperatorProcessorNonPRISM>> operatorProcessors = new HashMap<>();
        
        operatorProcessors.put(OperatorAbs.class, OperatorAbsProcessor.class);
        operatorProcessors.put(OperatorDistance.class, OperatorDistanceProcessor.class);
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
