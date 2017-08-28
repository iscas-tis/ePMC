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

import epmc.expression.Expression;
import epmc.prism.exporter.processor.JANI2PRISMProcessorStrict;
import epmc.prism.exporter.processor.ProcessorRegistrar;
import epmc.value.Value;
import epmc.value.ValueBoolean;

public class ExpressionTemporalProcessor implements JANI2PRISMProcessorStrict {
	private ExpressionTemporal temporal = null;
	
	@Override
	public JANI2PRISMProcessorStrict setElement(Object obj) {
		assert obj != null;
		assert obj instanceof ExpressionTemporal; 
		
		temporal = (ExpressionTemporal) obj;
		return this;
	}

	@Override
	public String toPRISM() {
		assert temporal != null;
		
		StringBuilder prism = new StringBuilder();

		TemporalType type = temporal.getTemporalType();
        switch (type) {
        case NEXT: 
        case FINALLY: 
        case GLOBALLY: {
            prism.append(type.toString())
                 .append(ProcessorRegistrar.getProcessor(temporal.getTimeBound())
                		 				   .toPRISM())
                 .append("(")
                 .append(ProcessorRegistrar.getProcessor(temporal.getOperand1())
                 						   .toPRISM())
                 .append(")");
            break;
        }
        case UNTIL: 
        case RELEASE:
            if (type == TemporalType.UNTIL && temporal.getNumOps() == 2 && isTrue(temporal.getOperand1())) {
                prism.append("F(")
                     .append(ProcessorRegistrar.getProcessor(temporal.getOperand2())
                    		 				   .toPRISM())
                     .append(")");
            } else if (type == TemporalType.RELEASE && temporal.getNumOps() == 2 && isFalse(temporal.getOperand2())) {
                prism.append("G(")
                     .append(ProcessorRegistrar.getProcessor(temporal.getOperand1())
                    		 				   .toPRISM())
                     .append(")");
            } else {
            	boolean remaining = false;
                int timeBoundIndex = 0;
                for (Expression child : temporal.getOperands()) {
            		if (remaining) {
                        prism.append(type)
                        	 .append(ProcessorRegistrar.getProcessor(temporal.getTimeBound(timeBoundIndex))
                        			                   .toPRISM());
                        timeBoundIndex++;
                	} else {
                		remaining = true;
                	}
                    prism.append("(")
                    	 .append(ProcessorRegistrar.getProcessor(child)
                    			 				   .toPRISM())
                    	 .append(")");
                }
            }
            break;
        default:
            assert (false);
        }
		
		return prism.toString();
	}

	@Override
	public void validateTransientVariables() {
		assert temporal != null;
		
		for (Expression child : temporal.getChildren()) {
			ProcessorRegistrar.getProcessor(child)
							  .validateTransientVariables();
		}
	}
	
	@Override
	public boolean usesTransientVariables() {
		assert temporal != null;
		
		boolean usesTransient = false;
		for (Expression child : temporal.getChildren()) {
			usesTransient |= ProcessorRegistrar.getProcessor(child)
											   .usesTransientVariables();
		}
		
		return usesTransient;
	}	

	private static boolean isTrue(Expression expression) {
        assert expression != null;
        
        if (!(expression instanceof ExpressionLiteral)) {
            return false;
        }
        return ValueBoolean.isTrue(getValue(expression));
    }
    
    private static boolean isFalse(Expression expression) {
        assert expression != null;
        
        if (!(expression instanceof ExpressionLiteral)) {
            return false;
        }
        return ValueBoolean.isFalse(getValue(expression));
    }  
    
    private static Value getValue(Expression expression) {
        assert expression != null;
        assert expression instanceof ExpressionLiteral;

        return ((ExpressionLiteral) expression).getValue();
    }
}
