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

import epmc.expression.Expression;
import epmc.prism.exporter.JANIComponentRegistrar;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorStrict;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorRegistrar;
import epmc.time.TypeClock;

public class PRISMExporter_VariableProcessor implements PRISMExporter_ProcessorStrict {

    private Variable variable = null;
    private String prefix = null;
    private boolean forDefinition = false;

    @Override
    public PRISMExporter_ProcessorStrict setElement(Object obj) {
        assert obj != null;
        assert obj instanceof Variable; 

        variable = (Variable) obj;
        return this;
    }

    @Override
    public PRISMExporter_ProcessorStrict setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public PRISMExporter_ProcessorStrict setForDefinition(boolean forDefinition) {
        this.forDefinition = forDefinition;
        return this;
    }

    @Override
    public String toPRISM() {
        assert variable != null;

        JANIComponentRegistrar.variableRenaming(variable, prefix);
        if (forDefinition) {
            return toPRISMForDefinition();
        } else {
            return variable.getName();
        }
    }

    private String toPRISMForDefinition() {
        StringBuilder prism = new StringBuilder();

        String comment = variable.getComment();
        if (comment != null) {
            if (prefix != null) {
                prism.append(prefix);
            }
            prism.append("// ")
                .append(comment)
                .append("\n");
        }

        if (prefix != null)	{
            prism.append(prefix);
        }
        prism.append(JANIComponentRegistrar.getIdentifierNameByIdentifier(variable))
            .append(" : ")
            .append(PRISMExporter_ProcessorRegistrar.getProcessor(variable.getType())
                .toPRISM());

        if (!JANIComponentRegistrar.areInitialConditionsUsed()) {
            if (!(variable.toType() instanceof TypeClock)) {
                Expression initial = variable.getInitialValueOrNull();
                if (initial != null) {
                    prism.append(" init ")
                        .append(PRISMExporter_ProcessorRegistrar.getProcessor(initial)
                            .toPRISM());
                }
            }
        }

        prism.append(";\n");

        return prism.toString();
    }

    @Override
    public void validateTransientVariables() {
        assert variable != null;
    }

    @Override
    public boolean usesTransientVariables() {
        assert variable != null;

        return variable.isTransient();
    }	
}
