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

package epmc.jani.valuejson;

import java.util.List;

import javax.json.JsonValue;

import epmc.options.Options;
import epmc.util.Util;
import epmc.value.Value;

public final class UtilValueJSON {
    public static JsonValue valueToJson(Value value) {
        assert value != null;
        Options options = Options.get();
        List<Class<? extends ValueJSON>> valueJsonClasses = options.get(OptionsJANIValueJSON.JANI_VALUEJSON_CLASS);
        assert valueJsonClasses != null;
        for (Class<? extends ValueJSON> clazz : valueJsonClasses) {
            ValueJSON valueJson = Util.getInstance(clazz);
            JsonValue result = valueJson.convert(value);
            if (result != null) {
                return result;
            }
        }
        assert false;
        return null;
    }

    private UtilValueJSON() {
    }
}
