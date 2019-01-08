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

package epmc.jani.model;

import static epmc.error.UtilError.ensure;

import epmc.prism.exporter.error.ProblemsPRISMExporter;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorStrict;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorRegistrar;

public class PRISMExporter_AssignmentSimpleProcessor implements PRISMExporter_ProcessorStrict {

    private AssignmentSimple assignment = null;
    private String prefix = null;

    @Override
    public PRISMExporter_ProcessorStrict setElement(Object obj) {
        assert obj != null;
        assert obj instanceof AssignmentSimple; 

        assignment = (AssignmentSimple) obj;
        return this;
    }

    @Override
    public PRISMExporter_ProcessorStrict setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String toPRISM() {
        assert assignment != null;

        StringBuilder prism = new StringBuilder();

        if (prefix != null)	{
            prism.append(prefix);
        }
        prism.append("(")
            .append(PRISMExporter_ProcessorRegistrar.getProcessor(assignment.getRef())
                .toPRISM())
            .append("'=")
            .append(PRISMExporter_ProcessorRegistrar.getProcessor(assignment.getValue())
                .toPRISM())
            .append(")");
        return prism.toString();
    }

    @Override
    public void validateTransientVariables() {
        assert assignment != null;

        ensure(!PRISMExporter_ProcessorRegistrar.getProcessor(assignment.getValue())
                    .usesTransientVariables(), 
                ProblemsPRISMExporter.PRISM_EXPORTER_UNSUPPORTED_FEATURE_TRANSIENT_VARIABLE_FOR_NORMAL_VARIABLE, 
                assignment.getRef()
                    .getName());
    }

    @Override
    public boolean usesTransientVariables() {
        assert assignment != null;

        boolean usesTransient = false;
        usesTransient |= PRISMExporter_ProcessorRegistrar.getProcessor(assignment.getRef())
                .usesTransientVariables();
        usesTransient |= PRISMExporter_ProcessorRegistrar.getProcessor(assignment.getValue())
                .usesTransientVariables();

        return usesTransient;
    }	
}
