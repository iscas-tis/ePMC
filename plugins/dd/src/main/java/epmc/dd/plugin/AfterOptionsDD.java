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

package epmc.dd.plugin;

import java.util.Map;

import epmc.dd.LibraryDD;
import epmc.dd.OptionsDD;
import epmc.options.Category;
import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeMap;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;
import epmc.util.OrderedMap;

public final class AfterOptionsDD implements AfterOptionsCreation {
    private final static String IDENTIFIER = "after-options-creation-dd";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        assert options != null;
        Map<String,Class<? extends LibraryDD>> ddLibraryClasses = new OrderedMap<>();
        OptionTypeMap<?> ddBinaryEngineType = new OptionTypeMap<>(ddLibraryClasses);
        Category category = options.addCategory().setBundleName(OptionsDD.OPTIONS_DD)
                .setIdentifier(OptionsDD.DD_CATEGORY)
                .   build();
        options.set(OptionsDD.DD_LIBRARY_CLASS, ddLibraryClasses);
        options.addOption().setBundleName(OptionsDD.OPTIONS_DD)
        .setIdentifier(OptionsDD.DD_BINARY_ENGINE)
        .setType(ddBinaryEngineType)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
        Map<String,Class<? extends LibraryDD>> ddMtLibraryClasses = new OrderedMap<>();
        OptionTypeMap<?> ddMultiEngineType = new OptionTypeMap<>(ddMtLibraryClasses);
        options.set(OptionsDD.DD_MT_LIBRARY_CLASS, ddMtLibraryClasses);
        options.addOption().setBundleName(OptionsDD.OPTIONS_DD)
        .setIdentifier(OptionsDD.DD_MULTI_ENGINE)
        .setType(ddMultiEngineType)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();
        options.addOption().setBundleName(OptionsDD.OPTIONS_DD)
        .setIdentifier(OptionsDD.DD_DEBUG)
        .setType(typeBoolean)
        .setCommandLine().setGui()
        .setCategory(category).build();
        options.addOption().setBundleName(OptionsDD.OPTIONS_DD)
        .setIdentifier(OptionsDD.DD_AND_EXIST)
        .setType(typeBoolean)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
        options.addOption().setBundleName(OptionsDD.OPTIONS_DD)
        .setIdentifier(OptionsDD.DD_LEAK_CHECK)
        .setType(typeBoolean)
        .setCommandLine().setGui()
        .setCategory(category).build();
    }

}
