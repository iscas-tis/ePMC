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

package epmc.jani.type.ctmc;

import java.util.Map;

import epmc.jani.explorer.ExplorerExtension;
import epmc.jani.explorer.OptionsJANIExplorer;
import epmc.jani.model.ModelExtension;
import epmc.jani.model.ModelExtensionSemantics;
import epmc.jani.model.OptionsJANIModel;
import epmc.options.OptionTypeBoolean;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;
import epmc.util.OrderedMap;

public class AfterOptionsCreationJANICTMC implements AfterOptionsCreation {
    private final static String IDENTIFIER = "jani-ctmc";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        Map<String,Class<ModelExtension>> modelExtensions = options.get(OptionsJANIModel.JANI_MODEL_EXTENSION_CLASS);
        if (modelExtensions == null) {
            modelExtensions = new OrderedMap<>();
            options.set(OptionsJANIModel.JANI_MODEL_EXTENSION_CLASS, modelExtensions);
        }
        Map<String,Class<? extends ExplorerExtension>> explorerExtensions = options.get(OptionsJANIExplorer.JANI_EXPLORER_EXTENSION_CLASS);
        if (explorerExtensions == null) {
            explorerExtensions = new OrderedMap<>();
        }
        explorerExtensions.put(ExplorerExtensionCTMC.IDENTIFIER, ExplorerExtensionCTMC.class);
        options.set(OptionsJANIExplorer.JANI_EXPLORER_EXTENSION_CLASS, explorerExtensions);
        addSemantics(options);
    }

    private void addSemantics(Options options) {
        assert options != null;
        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();

        options.addOption().setBundleName(OptionsJANICTMC.OPTIONS_JANI_CTMC)
        .setIdentifier(OptionsJANICTMC.JANI_CTMC_ALLOW_MULTI_TRANSITION)
        .setType(typeBoolean).setDefault(true)
        .setCommandLine().setGui().setWeb()
        .setCategory(OptionsJANIModel.JANI_MODEL_CATEGORY).build();

        Map<String, Class<? extends ModelExtensionSemantics>> modelSemanticTypes =
                options.get(OptionsJANIModel.JANI_MODEL_EXTENSION_SEMANTICS);
        if (modelSemanticTypes == null) {
            modelSemanticTypes = new OrderedMap<>(true);
        }
        modelSemanticTypes.put(ModelExtensionCTMC.IDENTIFIER, ModelExtensionCTMC.class);
        options.set(OptionsJANIModel.JANI_MODEL_EXTENSION_SEMANTICS, modelSemanticTypes);
    }

}
