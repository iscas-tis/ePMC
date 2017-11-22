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

package epmc.example;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.modelchecker.CommandTask;
import epmc.options.Options;
import epmc.options.OptionsEPMC;
import epmc.plugin.*;

public final class AfterOptionsCreationExample implements AfterOptionsCreation {
	private final static String IDENTIFIER = "after-options-creation-example";
	public String getIdentifier() { 
		return IDENTIFIER;
	}

	public void process(Options options) {
		assert options != null;
		System.out.println("processing the after-options-creation operation of plugin epmc-example");
		// now try to load the CommandTask
		Map<String,Class<? extends CommandTask>> commandTaskClasses = options.get(OptionsEPMC.COMMAND_TASK_CLASS);
		 assert commandTaskClasses != null;
	     options.addCommand(null, ExampleCommandTask.IDENTIFIER, true, false, false);
	     commandTaskClasses.put(ExampleCommandTask.IDENTIFIER, ExampleCommandTask.class);
	     App.run();
	}

}
