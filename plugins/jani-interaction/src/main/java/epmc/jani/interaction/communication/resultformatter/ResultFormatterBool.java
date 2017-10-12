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

package epmc.jani.interaction.communication.resultformatter;

import javax.json.JsonValue;

import epmc.util.UtilJSON;
import epmc.value.Value;
import epmc.value.ValueBoolean;

public final class ResultFormatterBool implements ResultFormatter {
    public final static String IDENTIFIER = "bool";
    private final static String BOOL = "bool";
    private final static JsonValue BOOL_VALUE = UtilJSON.toStringValue(BOOL);
    private Object result;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setResult(Object result) {
        assert result != null;
        this.result = result;
    }

    @Override
    public boolean canHandle() {
        if (!(result instanceof Value)) {
            return false;
        }
        Value valueResult = (Value) result;
        if (!ValueBoolean.is(valueResult)) {
            return false;
        }
        return true;
    }

    @Override
    public String getLabel() {
        assert canHandle();
        return null;
    }

    @Override
    public JsonValue getType() {
        assert canHandle();
        return BOOL_VALUE;
    }

    @Override
    public JsonValue getValue() {
        assert canHandle();
        Value valueResult = (Value) result;
        return UtilJSON.toBooleanValue(ValueBoolean.as(valueResult).getBoolean());
    }

    @Override
    public String getFormattedValue() {
        assert canHandle();
        return result.toString();
    }

}
