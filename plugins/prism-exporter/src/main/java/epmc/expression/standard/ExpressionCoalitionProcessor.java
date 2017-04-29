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

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.prism.exporter.messages.ExtendedFeaturesPRISMExporter;
import epmc.prism.exporter.processor.JANI2PRISMProcessorExtended;
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;
import epmc.prism.exporter.processor.ProcessorRegistrar;

public class ExpressionCoalitionProcessor implements JANI2PRISMProcessorExtended {

	private ExpressionCoalition coalition = null;

	@Override
	public JANI2PRISMProcessorStrict setElement(Object obj) throws EPMCException {
		assert obj != null;
		assert obj instanceof ExpressionCoalition; 
		
		coalition = (ExpressionCoalition) obj;
		return this;
	}

	@Override
	public String toPRISM() throws EPMCException {
		assert coalition != null;
		
		StringBuilder prism = new StringBuilder();
		
        prism.append("<<");
        
        boolean remaining = false;
        for (SMGPlayer player : coalition.getPlayers()) {
        	if (remaining) {
        		prism.append(", ");
        	} else {
        		remaining = true;
        	}
    		prism.append(ProcessorRegistrar.getProcessor(player.getExpression())
    									   .toPRISM());
        }
        prism.append(">>")
        	 .append(ProcessorRegistrar.getProcessor(coalition.getInner())
        			 				   .toPRISM());

        return prism.toString();
	}
	
	@Override
	public void validateTransientVariables() throws EPMCException {
		assert coalition != null;
		
		for (Expression child : coalition.getChildren()) {
			ProcessorRegistrar.getProcessor(child)
							  .validateTransientVariables();
		}
	}

	@Override
	public boolean usesTransientVariables() throws EPMCException {
		assert coalition != null;
		
		boolean usesTransient = false;
		for (Expression child : coalition.getChildren()) {
			usesTransient |= ProcessorRegistrar.getProcessor(child)
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
