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

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;
import epmc.prism.exporter.processor.ProcessorRegistrar;

public class ExpressionRewardProcessor implements JANI2PRISMProcessorStrict {

	private ExpressionReward reward = null;
	
	@Override
	public void setElement(Object obj) throws EPMCException {
		assert obj != null;
		assert obj instanceof ExpressionReward; 
		
		reward = (ExpressionReward) obj;
	}

	@Override
	public StringBuilder toPRISM() throws EPMCException {
		assert reward != null;
		
		StringBuilder prism = new StringBuilder();
		JANI2PRISMProcessorStrict processor;
		Expression expr;
		
		RewardType type = reward.getRewardType();
        prism.append(type.toString());
        switch(type) {
        case REACHABILITY:
        	expr = reward.getRewardReachSet();
    		processor = ProcessorRegistrar.getProcessor(expr);
        	prism.append("(").append(processor.toPRISM().toString()).append(")");
        	break;
        case INSTANTANEOUS:
        case CUMULATIVE:
        	expr = reward.getTime();
    		processor = ProcessorRegistrar.getProcessor(expr);
        	prism.append("(").append(processor.toPRISM().toString()).append(")");
        	break;
        case DISCOUNTED:
        	expr = reward.getTime();
    		processor = ProcessorRegistrar.getProcessor(expr);
            prism.append(processor.toPRISM().toString()).append(",");
        	expr = reward.getDiscount();
    		processor = ProcessorRegistrar.getProcessor(expr);
            prism.append(processor.toPRISM().toString());
        	break;
    	default:
        }

		return prism;
	}
}
