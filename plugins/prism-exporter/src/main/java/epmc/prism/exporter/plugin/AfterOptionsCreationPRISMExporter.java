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

package epmc.prism.exporter.plugin;

import java.util.Map;

import epmc.main.options.OptionsEPMC;
import epmc.modelchecker.CommandTask;
import epmc.options.Category;
import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeString;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;
import epmc.prism.exporter.command.CommandTaskPRISMExporterPRISMExport;
import epmc.prism.exporter.options.OptionsPRISMExporter;
import epmc.value.OptionsValue;

/**
 * JANI exporter plugin class containing method to execute after options creation.
 * 
 * @author Andrea Turrini
 */
public final class AfterOptionsCreationPRISMExporter implements AfterOptionsCreation {
    /** Identifier of this class. */
    private final static String IDENTIFIER = "after-options-creation-prism-exporter";

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
                .setBundleName(OptionsPRISMExporter.OPTIONS_PRISM_EXPORTER)
                .setIdentifier(OptionsPRISMExporter.PRISM_EXPORTER_CATEGORY)
                .build();

        Map<String,Class<? extends CommandTask>> commandTaskClasses = options.get(OptionsEPMC.COMMAND_CLASS);
        assert commandTaskClasses != null;

        options.addCommand()
            .setBundleName(OptionsPRISMExporter.OPTIONS_PRISM_EXPORTER)
            .setIdentifier(CommandTaskPRISMExporterPRISMExport.IDENTIFIER)
            .setCommandLine()
            .build();

        commandTaskClasses.put(CommandTaskPRISMExporterPRISMExport.IDENTIFIER, CommandTaskPRISMExporterPRISMExport.class);

        OptionTypeString typeString = OptionTypeString.getInstance();
        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();

        options.addOption()
            .setBundleName(OptionsPRISMExporter.OPTIONS_PRISM_EXPORTER)
            .setIdentifier(OptionsPRISMExporter.PRISM_EXPORTER_PRISM_MODEL_NAME)
            .setType(typeString)
            .setCommandLine()
            .setCategory(category)
            .build();

        options.addOption()
            .setBundleName(OptionsPRISMExporter.OPTIONS_PRISM_EXPORTER)
            .setIdentifier(OptionsPRISMExporter.PRISM_EXPORTER_PRISM_MODEL_FILE_NAME)
            .setType(typeString)
            .setCommandLine()
            .setCategory(category)
            .build();

        options.addOption()
            .setBundleName(OptionsPRISMExporter.OPTIONS_PRISM_EXPORTER)
            .setIdentifier(OptionsPRISMExporter.PRISM_EXPORTER_PRISM_PROPERTIES_FILE_NAME)
            .setType(typeString)
            .setCommandLine()
            .setCategory(category)
            .build();

        options.addOption()
            .setBundleName(OptionsPRISMExporter.OPTIONS_PRISM_EXPORTER)
            .setIdentifier(OptionsPRISMExporter.PRISM_EXPORTER_EXTENDED_PRISM)
            .setType(typeBoolean)
            .setDefault(false)
            .setCommandLine()
            .setCategory(category)
            .build();

        options.addOption()
            .setBundleName(OptionsPRISMExporter.OPTIONS_PRISM_EXPORTER)
            .setIdentifier(OptionsPRISMExporter.PRISM_EXPORTER_NON_OFFICIAL_PRISM)
            .setType(typeBoolean)
            .setDefault(false)
            .setCommandLine()
            .setCategory(category)
            .build();

        options.addOption()
            .setBundleName(OptionsPRISMExporter.OPTIONS_PRISM_EXPORTER)
            .setIdentifier(OptionsPRISMExporter.PRISM_EXPORTER_ALLOW_MULTIPLE_LOCATIONS)
            .setType(typeBoolean)
            .setDefault(false)
            .setCommandLine()
            .setCategory(category)
            .build();

        options.set(OptionsValue.VALUE_FLOATING_POINT_OUTPUT_FORMAT, "%f");
    }

}
