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

package epmc.expression.standard;

import java.util.LinkedList;
import java.util.List;

import epmc.expression.Expression;
import epmc.prism.exporter.messages.ExtendedFeaturesPRISMExporter;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorExtended;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorStrict;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorRegistrar;

public class PRISMExporter_ExpressionCoalitionProcessor implements PRISMExporter_ProcessorExtended {

    private ExpressionCoalition coalition = null;

    @Override
    public PRISMExporter_ProcessorStrict setElement(Object obj) {
        assert obj != null;
        assert obj instanceof ExpressionCoalition; 

        coalition = (ExpressionCoalition) obj;
        return this;
    }

    @Override
    public String toPRISM() {
        assert coalition != null;

        StringBuilder prism = new StringBuilder();

        prism.append("<<");

        boolean notFirst = false;
        for (SMGPlayer player : coalition.getPlayers()) {
            if (notFirst) {
                prism.append(", ");
            } else {
                notFirst = true;
            }
            prism.append(PRISMExporter_ProcessorRegistrar.getProcessor(player.getExpression())
                    .toPRISM());
        }
        prism.append(">>")
            .append(PRISMExporter_ProcessorRegistrar.getProcessor(coalition.getInner())
                    .toPRISM());

        return prism.toString();
    }

    @Override
    public void validateTransientVariables() {
        assert coalition != null;

        for (Expression child : coalition.getChildren()) {
            PRISMExporter_ProcessorRegistrar.getProcessor(child)
                .validateTransientVariables();
        }
    }

    @Override
    public boolean usesTransientVariables() {
        assert coalition != null;

        boolean usesTransient = false;
        for (Expression child : coalition.getChildren()) {
            usesTransient |= PRISMExporter_ProcessorRegistrar.getProcessor(child)
                    .usesTransientVariables();
        }

        return usesTransient;
    }	

    @Override
    public List<String> getUnsupportedFeature() {
        List<String> ll = new LinkedList<>();
        ll.add(ExtendedFeaturesPRISMExporter.PRISM_EXPORTER_EXTENDED_FEATURE_SMG_COALITION);
        return ll;
    }
}
