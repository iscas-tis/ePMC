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

public class TimeBoundProcessor implements JANI2PRISMProcessorStrict {

	private TimeBound timeBound = null;
	
	@Override
	public void setElement(Object obj) throws EPMCException {
		assert obj != null;
		assert obj instanceof TimeBound; 
		
		timeBound = (TimeBound) obj;
	}

	@Override
	public StringBuilder toPRISM() throws EPMCException {
		assert timeBound != null;
		
		StringBuilder prism = new StringBuilder();
		JANI2PRISMProcessorStrict processor; 
		
		Expression left = timeBound.getLeft();
		Expression right = timeBound.getRight();
		
        if (timeBound.isUnbounded()) {
        } else if (!timeBound.isLeftBounded()) {
    		processor = ProcessorRegistrar.getProcessor(right);
    		prism.append(timeBound.isRightOpen() ? "<" : "<=")
    		     .append(leftBraceIfNeeded(right))
    			 .append(processor.toPRISM().toString())
    			 .append(rightBraceIfNeeded(right));        	
        } else if (!timeBound.isRightBounded()) {
    		processor = ProcessorRegistrar.getProcessor(left);
    		prism.append(timeBound.isLeftOpen() ? ">" : ">=")
    		     .append(leftBraceIfNeeded(left))
			     .append(processor.toPRISM().toString())
			     .append(rightBraceIfNeeded(left));
        } else if (left.equals(right)) {
    		processor = ProcessorRegistrar.getProcessor(left);
            prism.append("=")
            	 .append(leftBraceIfNeeded(left))
			     .append(processor.toPRISM().toString())
            	 .append(rightBraceIfNeeded(left));
        } else {
    		processor = ProcessorRegistrar.getProcessor(left);
            prism.append(timeBound.isLeftOpen() ? "]" : "[")
            	 .append(left)
            	 .append(processor.toPRISM().toString());
            prism.append(",");
            
    		processor = ProcessorRegistrar.getProcessor(right);
            prism.append(processor.toPRISM().toString())
            	 .append(timeBound.isRightOpen() ? "[" : "]");
        }
		
		return prism;
	}
	
	@Override
	public void validateTransientVariables() throws EPMCException {
		assert timeBound != null;
		
		ProcessorRegistrar.getProcessor(timeBound.getLeft()).validateTransientVariables();
		ProcessorRegistrar.getProcessor(timeBound.getRight()).validateTransientVariables();
	}
	
	
	@Override
	public boolean usesTransientVariables() throws EPMCException {
		assert timeBound != null;
		
		boolean usesTransient = false;
		usesTransient |= ProcessorRegistrar.getProcessor(timeBound.getLeft()).usesTransientVariables();
		usesTransient |= ProcessorRegistrar.getProcessor(timeBound.getRight()).usesTransientVariables();
		
		return usesTransient;
	}	
    private static boolean needBracesForInequation(Expression expr) {
        return (!(expr instanceof ExpressionIdentifierStandard
                || expr instanceof ExpressionLiteral));
    }
    
    private String leftBraceIfNeeded(Expression expr) {
        if (needBracesForInequation(expr)) {
            return "(";
        } else {
            return "";
        }
    }

    private String rightBraceIfNeeded(Expression expr) {
        if (needBracesForInequation(expr)) {
            return ")";
        } else {
            return "";
        }
    }

}
