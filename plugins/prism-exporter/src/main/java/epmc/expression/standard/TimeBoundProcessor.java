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

public class TimeBoundProcessor implements JANI2PRISMProcessorStrict {

    private TimeBound timeBound = null;

    @Override
    public JANI2PRISMProcessorStrict setElement(Object obj) {
        assert obj != null;
        assert obj instanceof TimeBound; 

        timeBound = (TimeBound) obj;
        return this;
    }

    @Override
    public String toPRISM() {
        assert timeBound != null;

        StringBuilder prism = new StringBuilder();

        Expression left = timeBound.getLeft();
        Expression right = timeBound.getRight();

        if (timeBound.isUnbounded()) {
            //nothing to do
        } else if (!timeBound.isLeftBounded()) {
            prism.append(timeBound.isRightOpen() ? "<" : "<=")
                .append(leftBraceIfNeeded(right))
                .append(ProcessorRegistrar.getProcessor(right)
                        .toPRISM())
                .append(rightBraceIfNeeded(right));        	
        } else if (!timeBound.isRightBounded()) {
            prism.append(timeBound.isLeftOpen() ? ">" : ">=")
                .append(leftBraceIfNeeded(left))
                .append(ProcessorRegistrar.getProcessor(left)
                        .toPRISM())
                .append(rightBraceIfNeeded(left));
        } else if (left.equals(right)) {
            prism.append("=")
                .append(leftBraceIfNeeded(left))
                .append(ProcessorRegistrar.getProcessor(left)
                        .toPRISM())
                .append(rightBraceIfNeeded(left));
        } else {
            prism.append(timeBound.isLeftOpen() ? "]" : "[")
                .append(left)
                .append(ProcessorRegistrar.getProcessor(left)
                        .toPRISM())
                .append(",")
                .append(ProcessorRegistrar.getProcessor(right)
                        .toPRISM())
                .append(timeBound.isRightOpen() ? "[" : "]");
        }

        return prism.toString();
    }

    @Override
    public void validateTransientVariables() {
        assert timeBound != null;

        ProcessorRegistrar.getProcessor(timeBound.getLeft())
            .validateTransientVariables();
        ProcessorRegistrar.getProcessor(timeBound.getRight())
            .validateTransientVariables();
    }


    @Override
    public boolean usesTransientVariables() {
        assert timeBound != null;

        boolean usesTransient = false;
        usesTransient |= ProcessorRegistrar.getProcessor(timeBound.getLeft())
                .usesTransientVariables();
        usesTransient |= ProcessorRegistrar.getProcessor(timeBound.getRight())
                .usesTransientVariables();

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
