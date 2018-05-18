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

package epmc.jani.interaction.command;

import epmc.jani.interaction.UserManager;
import epmc.jani.interaction.database.Database;
import epmc.jani.interaction.options.OptionsJANIInteraction;
import epmc.modelchecker.CommandTask;
import epmc.options.Options;

public final class CommandTaskJANIInteractionAddUser implements CommandTask {
    /** Unique identifier of JANI interaction add user command. */
    public final static String IDENTIFIER = "jani-interaction-add-user";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void executeInClientBeforeServer() {
        Database storage = new Database();
        UserManager userManager = new UserManager(storage);
        String username = Options.get().get(OptionsJANIInteraction.JANI_INTERACTION_MODIFIED_USERNAME);
        String password = Options.get().get(OptionsJANIInteraction.JANI_INTERACTION_MODIFIED_PASSWORD);
        userManager.createUser(username, password);
    }
}
