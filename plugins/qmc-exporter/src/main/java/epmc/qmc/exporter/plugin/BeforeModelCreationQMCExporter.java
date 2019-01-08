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

package epmc.qmc.exporter.plugin;

import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.QMCExporter_ExpressionQuantifier2JANIProcessor;
import epmc.jani.exporter.operatorprocessor.JANIExporter_OperatorProcessorRegistrar;
import epmc.jani.exporter.processor.JANIExporter_ProcessorRegistrar;
import epmc.jani.extensions.quantum.ModelExtensionQMC;
import epmc.jani.type.qmc.QMCExporter_ModelExtensionQMC2JANIProcessor;
import epmc.jani.type.qmc.QMCExporter_ModelExtensionQMC2PRISMProcessor;
import epmc.plugin.BeforeModelCreation;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorRegistrar;
import epmc.prism.operator.JANIExporter_OperatorPRISMPowProcessor;
import epmc.prism.operator.OperatorPRISMPow;
import epmc.qmc.model.JANITypeArray;
import epmc.qmc.model.JANITypeSuperoperator;
import epmc.qmc.model.QMCExporter_JANITypeArray2JANIProcessor;
import epmc.qmc.model.QMCExporter_JANITypeArray2PRISMProcessor;
import epmc.qmc.model.QMCExporter_JANITypeSuperoperator2JANIProcessor;
import epmc.qmc.operator.OperatorArray;
import epmc.qmc.operator.OperatorBaseBra;
import epmc.qmc.operator.OperatorBaseKet;
import epmc.qmc.operator.OperatorBraToVector;
import epmc.qmc.operator.OperatorComplex;
import epmc.qmc.operator.OperatorConjugate;
import epmc.qmc.operator.OperatorIdentityMatrix;
import epmc.qmc.operator.OperatorKetToVector;
import epmc.qmc.operator.OperatorKronecker;
import epmc.qmc.operator.OperatorMatrix;
import epmc.qmc.operator.OperatorPhaseShift;
import epmc.qmc.operator.OperatorQeval;
import epmc.qmc.operator.OperatorQprob;
import epmc.qmc.operator.OperatorSuperOperatorList;
import epmc.qmc.operator.OperatorSuperOperatorMatrix;
import epmc.qmc.operator.OperatorTranspose;
import epmc.qmc.operator.QMCExporter_OperatorArray2JANIProcessor;
import epmc.qmc.operator.QMCExporter_OperatorBaseBra2JANIProcessor;
import epmc.qmc.operator.QMCExporter_OperatorBaseKet2JANIProcessor;
import epmc.qmc.operator.QMCExporter_OperatorBraToVector2JANIProcessor;
import epmc.qmc.operator.QMCExporter_OperatorComplex2JANIProcessor;
import epmc.qmc.operator.QMCExporter_OperatorConjugate2JANIProcessor;
import epmc.qmc.operator.QMCExporter_OperatorIdentityMatrix2JANIProcessor;
import epmc.qmc.operator.QMCExporter_OperatorKetToVector2JANIProcessor;
import epmc.qmc.operator.QMCExporter_OperatorKronecker2JANIProcessor;
import epmc.qmc.operator.QMCExporter_OperatorMatrix2JANIProcessor;
import epmc.qmc.operator.QMCExporter_OperatorPhaseShift2JANIProcessor;
import epmc.qmc.operator.QMCExporter_OperatorQeval2JANIProcessor;
import epmc.qmc.operator.QMCExporter_OperatorQprob2JANIProcessor;
import epmc.qmc.operator.QMCExporter_OperatorSuperOperatorList2JANIProcessor;
import epmc.qmc.operator.QMCExporter_OperatorSuperOperatorMatrix2JANIProcessor;
import epmc.qmc.operator.QMCExporter_OperatorTranspose2JANIProcessor;

/**
 * QMC exporter plugin class containing method to execute before model creation.
 * 
 * @author Andrea Turrini
 */
public final class BeforeModelCreationQMCExporter implements BeforeModelCreation {
    /** Identifier of this class. */
    private final static String IDENTIFIER = "before-model-creation-qmc-exporter";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process() {
        registerJANIProcessors();
        registerJANIOperatorProcessors();
        registerPRISMProcessors();
        registerPRISMOperatorProcessors();
    }
     
    private void registerJANIProcessors() {
        JANIExporter_ProcessorRegistrar.registerProcessor(ModelExtensionQMC.class, 
                QMCExporter_ModelExtensionQMC2JANIProcessor.class);
        JANIExporter_ProcessorRegistrar.registerProcessor(ExpressionQuantifier.class, 
                QMCExporter_ExpressionQuantifier2JANIProcessor.class);
        JANIExporter_ProcessorRegistrar.registerProcessor(JANITypeArray.class, 
                QMCExporter_JANITypeArray2JANIProcessor.class);
        JANIExporter_ProcessorRegistrar.registerProcessor(JANITypeSuperoperator.class, 
                QMCExporter_JANITypeSuperoperator2JANIProcessor.class);
    }
        
    private void registerJANIOperatorProcessors() {
        JANIExporter_OperatorProcessorRegistrar.registerOperatorProcessor(OperatorPRISMPow.class, 
                JANIExporter_OperatorPRISMPowProcessor.class);
        JANIExporter_OperatorProcessorRegistrar.registerOperatorProcessor(OperatorArray.class, 
                QMCExporter_OperatorArray2JANIProcessor.class);
        JANIExporter_OperatorProcessorRegistrar.registerOperatorProcessor(OperatorBaseBra.class, 
                QMCExporter_OperatorBaseBra2JANIProcessor.class);
        JANIExporter_OperatorProcessorRegistrar.registerOperatorProcessor(OperatorBaseKet.class, 
                QMCExporter_OperatorBaseKet2JANIProcessor.class);
        JANIExporter_OperatorProcessorRegistrar.registerOperatorProcessor(OperatorBraToVector.class, 
                QMCExporter_OperatorBraToVector2JANIProcessor.class);
        JANIExporter_OperatorProcessorRegistrar.registerOperatorProcessor(OperatorComplex.class, 
                QMCExporter_OperatorComplex2JANIProcessor.class);
        JANIExporter_OperatorProcessorRegistrar.registerOperatorProcessor(OperatorConjugate.class, 
                QMCExporter_OperatorConjugate2JANIProcessor.class);
        JANIExporter_OperatorProcessorRegistrar.registerOperatorProcessor(OperatorIdentityMatrix.class, 
                QMCExporter_OperatorIdentityMatrix2JANIProcessor.class);
        JANIExporter_OperatorProcessorRegistrar.registerOperatorProcessor(OperatorKetToVector.class, 
                QMCExporter_OperatorKetToVector2JANIProcessor.class);
        JANIExporter_OperatorProcessorRegistrar.registerOperatorProcessor(OperatorKronecker.class, 
                QMCExporter_OperatorKronecker2JANIProcessor.class);
        JANIExporter_OperatorProcessorRegistrar.registerOperatorProcessor(OperatorMatrix.class, 
                QMCExporter_OperatorMatrix2JANIProcessor.class);
        JANIExporter_OperatorProcessorRegistrar.registerOperatorProcessor(OperatorPhaseShift.class, 
                QMCExporter_OperatorPhaseShift2JANIProcessor.class);
        JANIExporter_OperatorProcessorRegistrar.registerOperatorProcessor(OperatorQeval.class, 
                QMCExporter_OperatorQeval2JANIProcessor.class);
        JANIExporter_OperatorProcessorRegistrar.registerOperatorProcessor(OperatorQprob.class, 
                QMCExporter_OperatorQprob2JANIProcessor.class);
        JANIExporter_OperatorProcessorRegistrar.registerOperatorProcessor(OperatorSuperOperatorList.class, 
                QMCExporter_OperatorSuperOperatorList2JANIProcessor.class);
        JANIExporter_OperatorProcessorRegistrar.registerOperatorProcessor(OperatorSuperOperatorMatrix.class, 
                QMCExporter_OperatorSuperOperatorMatrix2JANIProcessor.class);
        JANIExporter_OperatorProcessorRegistrar.registerOperatorProcessor(OperatorTranspose.class, 
                QMCExporter_OperatorTranspose2JANIProcessor.class);
    }
    
    private void registerPRISMProcessors() {
        PRISMExporter_ProcessorRegistrar.registerNonPRISMProcessor(ModelExtensionQMC.class, 
                QMCExporter_ModelExtensionQMC2PRISMProcessor.class);
        PRISMExporter_ProcessorRegistrar.registerNonPRISMProcessor(JANITypeArray.class, 
                QMCExporter_JANITypeArray2PRISMProcessor.class);
    }
    
    private void registerPRISMOperatorProcessors() {
    }
}
