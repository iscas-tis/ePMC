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

package epmc.prism.model.convert;

import java.util.Map;

import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.OptionTypeEnum;
import epmc.options.Options;
import epmc.prism.model.PropertyPRISM;

public final class UtilPrismConverter {
    public static void addOptions(Options options) {
        assert options != null;
        Map<String,Class<?>> propertyClasses = options.get(OptionsModelChecker.PROPERTY_CLASS);
        assert propertyClasses != null;
        propertyClasses.put(PropertyPRISM.IDENTIFIER, PropertyPRISM.class);
        OptionTypeEnum typeRewardMethod = new OptionTypeEnum(RewardMethod.class);
        options.addOption().setBundleName(OptionsPRISMConverter.PRISM_CONVERTER_OPTIONS)
        .setIdentifier(OptionsPRISMConverter.PRISM_CONVERTER_REWARD_METHOD)
        .setType(typeRewardMethod).setDefault(RewardMethod.INTEGRATE)
        .setCommandLine().setGui().setWeb().build();
        OptionTypeEnum typeSystemMethod = new OptionTypeEnum(SystemType.class);
        options.addOption().setBundleName(OptionsPRISMConverter.PRISM_CONVERTER_OPTIONS)
        .setIdentifier(OptionsPRISMConverter.PRISM_CONVERTER_SYSTEM_METHOD)
        .setType(typeSystemMethod).setDefault(SystemType.SYNCHRONISATION_VECTORS)
        .setCommandLine().setGui().setWeb().build();
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private UtilPrismConverter() {
    }
}
