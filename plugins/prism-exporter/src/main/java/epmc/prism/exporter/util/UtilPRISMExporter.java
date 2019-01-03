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

package epmc.prism.exporter.util;

import epmc.expression.Expression;
import epmc.prism.exporter.operatorprocessor.PRISMExporter_OperatorProcessorStrict;
import epmc.prism.exporter.processor.PRISMExporter_ProcessorRegistrar;

/**
 * @author Andrea Turrini
 *
 */
public final class UtilPRISMExporter {
    public static void appendWithParenthesesIfNeeded(PRISMExporter_OperatorProcessorStrict operatorProcessor, Expression operand, StringBuilder prism) {
        String operandToPRISM = PRISMExporter_ProcessorRegistrar.getProcessor(operand).toPRISM();
        if (operatorProcessor.needsParentheses(operand)) {
            prism.append("(")
                .append(operandToPRISM)
                .append(")");
        } else {
            prism.append(operandToPRISM);
        }
    }
}
