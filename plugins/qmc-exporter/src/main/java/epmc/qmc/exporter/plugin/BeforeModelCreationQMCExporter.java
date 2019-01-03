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

import epmc.jani.exporter.processor.JANIExporter_ProcessorRegistrar;
import epmc.jani.model.type.QMCExporter_JANITypeArray2JANIProcessor;
import epmc.jani.model.type.QMCExporter_JANITypeArray2PRISMProcessor;
import epmc.plugin.BeforeModelCreation;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorRegistrar;
import epmc.qmc.model.JANITypeArray;

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
        JANIExporter_ProcessorRegistrar.registerProcessor(JANITypeArray.class, 
                QMCExporter_JANITypeArray2JANIProcessor.class);
        
        PRISMExporter_ProcessorRegistrar.registerNonPRISMProcessor(JANITypeArray.class, 
                QMCExporter_JANITypeArray2PRISMProcessor.class);        
    }
}
