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

package epmc.jani.exporter.plugin;

import java.util.Map;

import epmc.jani.exporter.command.CommandTaskJANIExporterJANIExport;
import epmc.jani.exporter.options.OptionsJANIExporter;
import epmc.main.options.OptionsEPMC;
import epmc.modelchecker.CommandTask;
import epmc.options.Category;
import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeString;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

/**
 * JANI exporter plugin class containing method to execute after options creation.
 * 
 * @author Andrea Turrini
 */
public final class AfterOptionsCreationJANIExporter implements AfterOptionsCreation {
    /** Identifier of this class. */
    private static final String IDENTIFIER = "after-options-creation-jani-exporter";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        assert options != null;
        addOptionsAndCommands(options);
    }

    private void addOptionsAndCommands(Options options) {
        assert options != null;
        Category category = options.addCategory()
                .setBundleName(OptionsJANIExporter.OPTIONS_JANI_EXPORTER)
                .setIdentifier(OptionsJANIExporter.JANI_EXPORTER_CATEGORY)
                .build();

        Map<String,Class<? extends CommandTask>> commandTaskClasses = options.get(OptionsEPMC.COMMAND_CLASS);
        assert commandTaskClasses != null;

        options.addCommand()
            .setBundleName(OptionsJANIExporter.OPTIONS_JANI_EXPORTER)
            .setIdentifier(CommandTaskJANIExporterJANIExport.IDENTIFIER)
            .setCommandLine()
            .build();

        commandTaskClasses.put(CommandTaskJANIExporterJANIExport.IDENTIFIER, CommandTaskJANIExporterJANIExport.class);

        OptionTypeString typeString = OptionTypeString.getInstance();
        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();

        options.addOption()
            .setBundleName(OptionsJANIExporter.OPTIONS_JANI_EXPORTER)
            .setIdentifier(OptionsJANIExporter.JANI_EXPORTER_JANI_FILE_NAME)
            .setType(typeString)
            .setCommandLine()
            .setCategory(category)
            .build();

        options.addOption()
            .setBundleName(OptionsJANIExporter.OPTIONS_JANI_EXPORTER)
            .setIdentifier(OptionsJANIExporter.JANI_EXPORTER_OVERWRITE_JANI_FILE)
            .setType(typeBoolean)
            .setDefault(false)
            .setCommandLine()
            .setCategory(category)
            .build();

        options.addOption()
            .setBundleName(OptionsJANIExporter.OPTIONS_JANI_EXPORTER)
            .setIdentifier(OptionsJANIExporter.JANI_EXPORTER_JANI_MODEL_NAME)
            .setType(typeString)
            .setCommandLine()
            .setCategory(category)
            .build();

        options.addOption()
            .setBundleName(OptionsJANIExporter.OPTIONS_JANI_EXPORTER)
            .setIdentifier(OptionsJANIExporter.JANI_EXPORTER_REWARD_NAME_PREFIX)
            .setType(typeString)
            .setDefault("reward_")
            .setCommandLine()
            .setCategory(category)
            .build();

        options.addOption()
            .setBundleName(OptionsJANIExporter.OPTIONS_JANI_EXPORTER)
            .setIdentifier(OptionsJANIExporter.JANI_EXPORTER_USE_NEW_EXPORTER)
            .setType(typeBoolean)
            .setDefault(true)
            .setCommandLine()
            .setCategory(category)
            .build();

        options.addOption()
            .setBundleName(OptionsJANIExporter.OPTIONS_JANI_EXPORTER)
            .setIdentifier(OptionsJANIExporter.JANI_EXPORTER_USE_DERIVED_OPERATORS)
            .setType(typeBoolean)
            .setDefault(true)
            .setCommandLine()
            .setCategory(category)
            .build();

        options.addOption()
            .setBundleName(OptionsJANIExporter.OPTIONS_JANI_EXPORTER)
            .setIdentifier(OptionsJANIExporter.JANI_EXPORTER_SYNCHRONISE_SILENT)
            .setType(typeBoolean)
            .setDefault(false)
            .setCommandLine()
            .setCategory(category)
            .build();
    }

}
