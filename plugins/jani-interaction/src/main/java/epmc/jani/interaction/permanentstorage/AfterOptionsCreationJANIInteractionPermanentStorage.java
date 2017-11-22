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

package epmc.jani.interaction.permanentstorage;

import java.util.LinkedHashMap;
import java.util.Map;

import epmc.jani.interaction.InteractionExtension;
import epmc.jani.interaction.options.OptionsJANIInteraction;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public final class AfterOptionsCreationJANIInteractionPermanentStorage implements AfterOptionsCreation {
    public final static String IDENTIFIER = "after-options-creation-jani-interaction-permanent-storage";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        assert options != null;
        Map<String,Class<? extends InteractionExtension>> extensions = options.get(OptionsJANIInteraction.JANI_INTERACTION_EXTENSION_CLASS);
        if (extensions == null) {
            extensions = new LinkedHashMap<>();
        }
        extensions.put(InteractionExtensionPermanentStorage.IDENTIFIER, InteractionExtensionPermanentStorage.class);
        options.set(OptionsJANIInteraction.JANI_INTERACTION_EXTENSION_CLASS, extensions);
    }

}
