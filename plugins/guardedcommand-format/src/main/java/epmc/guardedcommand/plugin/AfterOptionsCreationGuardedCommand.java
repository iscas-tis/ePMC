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

package epmc.guardedcommand.plugin;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.guardedcommand.model.ModelGuardedCommand;
import epmc.guardedcommand.model.PropertyGuardedCommand;
import epmc.guardedcommand.model.convert.UtilGuardedCommandConverter;
import epmc.guardedcommand.options.OptionsGuardedCommand;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeMap;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public final class AfterOptionsCreationGuardedCommand implements AfterOptionsCreation {
	public final static String IDENTIFIER = "after-options-creation-guardedcommand";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
		OptionTypeMap<Class<?>> modelInputType = options.getType(OptionsModelChecker.MODEL_INPUT_TYPE);
		assert modelInputType != null;
		modelInputType.put(ModelGuardedCommand.IDENTIFIER, ModelGuardedCommand.class);
		Map<String,Class<?>> propertyClasses = options.get(OptionsModelChecker.PROPERTY_CLASS);
		assert propertyClasses != null;
		propertyClasses.put(PropertyGuardedCommand.IDENTIFIER, PropertyGuardedCommand.class);
        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();
        options.addOption().setBundleName(OptionsGuardedCommand.GUARDEDCOMMAND_OPTIONS)
        	.setIdentifier(OptionsGuardedCommand.GUARDEDCOMMAND_FLATTEN)
        	.setType(typeBoolean).setDefault(true)
        	.setCommandLine().setGui().setWeb().build();
        UtilGuardedCommandConverter.addOptions(options);
	}
}
