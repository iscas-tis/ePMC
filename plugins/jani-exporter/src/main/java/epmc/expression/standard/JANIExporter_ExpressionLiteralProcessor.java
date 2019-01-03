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

import javax.json.JsonValue;

import epmc.jani.exporter.processor.JANIExporter_Processor;
import epmc.jani.exporter.processor.JANIExporter_ProcessorRegistrar;
import epmc.util.UtilJSON;

public class JANIExporter_ExpressionLiteralProcessor implements JANIExporter_Processor {

    private ExpressionLiteral literal = null;

    @Override
    public JANIExporter_Processor setElement(Object obj) {
        assert obj != null;
        assert obj instanceof ExpressionLiteral; 

        literal = (ExpressionLiteral) obj;
        return this;
    }

    @Override
    public JsonValue toJSON() {
        assert literal != null;
        
        if (JANIExporter_ProcessorRegistrar.isBooleanType(literal.getType())) {
            return UtilJSON.toBooleanValue(literal.getValue());
        } else if (JANIExporter_ProcessorRegistrar.isNumericType(literal.getType())) {
            return UtilJSON.toNumberValue(literal.getValue());
        } else {
            return UtilJSON.toStringValue(literal.getValue());
        }
    }
}
