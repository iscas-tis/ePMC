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
import epmc.value.Value;
import epmc.value.ValueBoolean;

public class ExpressionTemporalProcessor implements JANI2PRISMProcessorStrict {

	private ExpressionTemporal temporal = null;
	
	@Override
	public void setElement(Object obj) throws EPMCException {
		assert obj != null;
		assert obj instanceof ExpressionTemporal; 
		
		temporal = (ExpressionTemporal) obj;
	}

	@Override
	public StringBuilder toPRISM() throws EPMCException {
		assert temporal != null;
		
		StringBuilder prism = new StringBuilder();
		JANI2PRISMProcessorStrict processor; 

		TemporalType type = temporal.getTemporalType();
        switch (type) {
        case NEXT: case FINALLY: case GLOBALLY: {
            prism.append(type.toString());
            
            TimeBound timeBound = temporal.getTimeBound(null);
    		processor = ProcessorRegistrar.getProcessor(timeBound);
    		prism.append(processor.toPRISM().toString());
    		
    		Expression child = temporal.getOperand1();
    		processor = ProcessorRegistrar.getProcessor(child);
            prism.append("(").append(processor.toPRISM().toString()).append(")");
            break;
        }
        case UNTIL: case RELEASE:
            if (type == TemporalType.UNTIL && temporal.getNumOps() == 2 && isTrue(temporal.getOperand1())) {
        		Expression child = temporal.getOperand2();
        		processor = ProcessorRegistrar.getProcessor(child);
                prism.append("F(").append(processor.toPRISM().toString()).append(")");
            } else if (type == TemporalType.RELEASE && temporal.getNumOps() == 2 && isFalse(temporal.getOperand2())) {
        		Expression child = temporal.getOperand1();
        		processor = ProcessorRegistrar.getProcessor(child);
                prism.append("G(").append(processor.toPRISM().toString()).append(")");
            } else {
            	boolean remaining = false;
                int timeBoundIndex = 0;
                for (Expression child : temporal.getOperands()) {
            		if (remaining) {
                        prism.append(type);
                        
                        TimeBound timeBound = temporal.getTimeBound(null, timeBoundIndex);
                		processor = ProcessorRegistrar.getProcessor(timeBound);
                		prism.append(processor.toPRISM().toString());
                		
                        timeBoundIndex++;
                	} else {
                		remaining = true;
                	}

            		processor = ProcessorRegistrar.getProcessor(child);
                    prism.append("(").append(processor.toPRISM().toString()).append(")");
                }
            }
            break;
        default:
            assert (false);
        }
		
		return prism;
	}

    private static boolean isTrue(Expression expression) {
        assert expression != null;
        if (!(expression instanceof ExpressionLiteral)) {
            return false;
        }
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return ValueBoolean.isTrue(getValue(expressionLiteral));
    }
    
    private static boolean isFalse(Expression expression) {
        assert expression != null;
        if (!(expression instanceof ExpressionLiteral)) {
            return false;
        }
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return ValueBoolean.isFalse(getValue(expressionLiteral));
    }  
    
    private static Value getValue(Expression expression) {
        assert expression != null;
        assert expression instanceof ExpressionLiteral;
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return expressionLiteral.getValue();
    }
}
