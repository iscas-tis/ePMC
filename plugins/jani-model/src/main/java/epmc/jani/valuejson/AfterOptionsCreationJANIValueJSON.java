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

import java.util.ArrayList;
import java.util.List;

import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public class AfterOptionsCreationJANIValueJSON implements AfterOptionsCreation {
    private final static String IDENTIFIER = "jani-valuejson";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        assert options != null;
        List<Class<? extends ValueJSON>> valueJsonClasses = new ArrayList<>();
        valueJsonClasses.add(0, ValueJSONGeneral.class);
        valueJsonClasses.add(0, ValueJSONBoolean.class);
        valueJsonClasses.add(0, ValueJSONInt.class);
        valueJsonClasses.add(0, ValueJSONDouble.class);
        options.set(OptionsJANIValueJSON.JANI_VALUEJSON_CLASS, valueJsonClasses);
    }
}
