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

package epmc.value.plugin;

import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeString;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;
import epmc.value.OptionsValue;

public final class AfterOptionsCreationValueBasic implements AfterOptionsCreation {
    private final static String IDENTIFIER = "after-options-creation-value-basic";
    private final static String VALUE_FLOATING_POINT_DEFAULT = "%.7f";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        assert options != null;
        OptionTypeString typeString = OptionTypeString.getInstance();
        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();
        options.addOption().setBundleName(OptionsValue.OPTIONS_VALUE)
        .setIdentifier(OptionsValue.VALUE_FLOATING_POINT_OUTPUT_FORMAT)
        .setType(typeString).setDefault(VALUE_FLOATING_POINT_DEFAULT)
        .setCommandLine().setGui().setWeb().build();

        options.addOption().setBundleName(OptionsValue.OPTIONS_VALUE)
        .setIdentifier(OptionsValue.VALUE_FLOATING_POINT_OUTPUT_NATIVE)
        .setType(typeBoolean)
        .setDefault(false)
        .setCommandLine().setGui().setWeb()
        .build();
    }


}
