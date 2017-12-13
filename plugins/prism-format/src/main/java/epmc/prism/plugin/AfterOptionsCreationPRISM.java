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

package epmc.prism.plugin;

import java.util.Map;

import epmc.graph.LowLevel;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeMap;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;
import epmc.prism.model.LowLevelPRISMBuilder;
import epmc.prism.model.ModelPRISM;
import epmc.prism.model.PropertyPRISM;
import epmc.prism.model.convert.UtilPrismConverter;
import epmc.prism.options.OptionsPRISM;

public final class AfterOptionsCreationPRISM implements AfterOptionsCreation {
    public final static String IDENTIFIER = "after-options-creation-prism";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        assert options != null;
        OptionTypeMap<Class<?>> modelInputType = options.getType(OptionsModelChecker.MODEL_INPUT_TYPE);
        assert modelInputType != null;
        modelInputType.put(ModelPRISM.IDENTIFIER, ModelPRISM.class);
        Map<String,Class<?>> propertyClasses = options.get(OptionsModelChecker.PROPERTY_CLASS);
        assert propertyClasses != null;
        propertyClasses.put(PropertyPRISM.IDENTIFIER, PropertyPRISM.class);
        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();
        options.addOption().setBundleName(OptionsPRISM.PRISM_OPTIONS)
        .setIdentifier(OptionsPRISM.PRISM_FLATTEN)
        .setType(typeBoolean).setDefault(true)
        .setCommandLine().setGui().setWeb().build();
        
        Map<String,Class<? extends LowLevel.Builder>> map = 
                options.get(OptionsModelChecker.LOW_LEVEL_ENGINE_CLASS);
        map.put(LowLevelPRISMBuilder.IDENTIFIER, LowLevelPRISMBuilder.class);
        UtilPrismConverter.addOptions(options);
    }
}
