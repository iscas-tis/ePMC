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

package epmc.algorithms;

import epmc.options.OptionTypeEnum;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public class AfterOptionsCreationAlgorithm implements AfterOptionsCreation {
    private final static String IDENTIFIER = "after-object-creation-algorithm";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        assert options != null;
        options.addOption().setIdentifier(OptionsAlgorithm.DD_SCC_ALGORITHM)
        .setBundleName(OptionsAlgorithm.OPTIONS_ALGORITHM)
        .setType(new OptionTypeEnum(DdSccAlgorithm.class))
        .setDefault(DdSccAlgorithm.BLOEM)
        .setCommandLine().setGui().setWeb().build();
    }
}
