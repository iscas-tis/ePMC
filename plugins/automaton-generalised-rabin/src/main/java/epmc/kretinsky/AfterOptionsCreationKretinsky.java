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

package epmc.kretinsky;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.kretinsky.automaton.AutomatonKretinskyProduct;
import epmc.kretinsky.options.KretinskyOptimiseMojmir;
import epmc.kretinsky.options.OptionsKretinsky;
import epmc.kretinsky.propertysolver.PropertySolverDDGeneralisedRabin;
import epmc.kretinsky.propertysolver.PropertySolverExplicitGeneralisedRabin;
import epmc.kretinsky.propertysolver.PropertySolverExplicitGeneralisedRabinIncremental;
import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeEnum;
import epmc.options.OptionTypeString;
import epmc.options.Options;
import epmc.options.UtilOptions;
import epmc.plugin.AfterOptionsCreation;

public class AfterOptionsCreationKretinsky implements AfterOptionsCreation {
    private final static String IDENTIFIER = "after-options-creation-kretinsky";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        assert options != null;
        OptionTypeString typeString = UtilOptions.getTypeString();
        OptionTypeBoolean typeBoolean = UtilOptions.getTypeBoolean();
        OptionTypeEnum typeMojmir = UtilOptions.newTypeEnum(KretinskyOptimiseMojmir.class);
        options.addProgramOption(OptionsKretinsky.KRETINSKY_LTLFILT_CMD, typeString, "ltlfilt", true, true, false);
        options.addProgramOption(OptionsKretinsky.KRETINSKY_OPTIONS, OptionsKretinsky.KRETINSKY_DISABLE_UNUSED_SLAVES, typeBoolean, true, true, true, true);
        options.addProgramOption(OptionsKretinsky.KRETINSKY_OPTIONS, OptionsKretinsky.KRETINSKY_GFFG_OPTIMISATION, typeBoolean, true, true, true, true);
        options.addProgramOption(OptionsKretinsky.KRETINSKY_OPTIONS, OptionsKretinsky.KRETINSKY_OPTIMISE_MOJMIR, typeMojmir, true, true, true, true);
        options.addProgramOption(OptionsKretinsky.KRETINSKY_OPTIONS, OptionsKretinsky.KRETINSKY_PREPROCESS_SLAVES, typeString, "run-100", true, true, true);
        Map<String, Class<?>> automatonMap = options.get();
        assert automatonMap != null;
        automatonMap.put(AutomatonKretinskyProduct.IDENTIFIER, AutomatonKretinskyProduct.class);
        Map<String, Class<?>> propertySolverList = options.get();
        assert propertySolverList != null;
        propertySolverList.put(PropertySolverDDGeneralisedRabin.IDENTIFIER, PropertySolverDDGeneralisedRabin.class);
        propertySolverList.put(PropertySolverExplicitGeneralisedRabin.IDENTIFIER, PropertySolverExplicitGeneralisedRabin.class);
        propertySolverList.put(PropertySolverExplicitGeneralisedRabinIncremental.IDENTIFIER, PropertySolverExplicitGeneralisedRabinIncremental.class);
    }

}
