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

import javax.json.Json;
import javax.json.JsonValue;

import epmc.value.Value;
import epmc.value.ValueInteger;

public class ValueJSONInt implements ValueJSON {
    final static String ARBITRARY = "arbitrary";

    @Override
    public JsonValue convert(Value value) {
        assert value != null;
        if (!ValueInteger.is(value)) {
            return null;
        }
        return Json.createObjectBuilder().add(ARBITRARY,
                ValueInteger.as(value).getInt())
                .build().get(ARBITRARY);
    }

}
