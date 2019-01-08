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

package epmc.qmc.exporter.plugin;

import java.util.Map;

import epmc.jani.exporter.options.OptionsJANIExporter;
import epmc.main.options.OptionsEPMC;
import epmc.modelchecker.CommandTask;
import epmc.options.Category;
import epmc.options.OptionTypeEnum;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;
import epmc.prism.exporter.options.OptionsPRISMExporter;
import epmc.qmc.exporter.command.CommandTaskQMCExporterQMCExport;
import epmc.qmc.exporter.options.ExportTo;
import epmc.qmc.exporter.options.OptionsQMCExporter;

/**
 * QMC exporter plugin class containing method to execute after options creation.
 * 
 * @author Andrea Turrini
 */
public final class AfterOptionsCreationQMCExporter implements AfterOptionsCreation {
    /** Identifier of this class. */
    private final static String IDENTIFIER = "after-options-creation-qmc-exporter";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        assert options != null;
        
        Map<String,Class<? extends CommandTask>> commandTaskClasses = options.get(OptionsEPMC.COMMAND_CLASS);
        assert commandTaskClasses != null;

        options.addCommand()
            .setBundleName(OptionsQMCExporter.OPTIONS_QMC_EXPORTER)
            .setIdentifier(CommandTaskQMCExporterQMCExport.IDENTIFIER)
            .setCommandLine()
            .build();

        commandTaskClasses.put(CommandTaskQMCExporterQMCExport.IDENTIFIER, 
                CommandTaskQMCExporterQMCExport.class);

        Category category = options.addCategory()
                .setBundleName(OptionsQMCExporter.OPTIONS_QMC_EXPORTER)
                .setIdentifier(OptionsQMCExporter.QMC_EXPORTER_CATEGORY)
                .build();

        options.addOption()
            .setBundleName(OptionsQMCExporter.OPTIONS_QMC_EXPORTER)
            .setIdentifier(OptionsQMCExporter.QMC_EXPORTER_EXPORT_TO)
            .setType(new OptionTypeEnum(ExportTo.class))
            .setDefault(ExportTo.JANI)
            .setCommandLine()
            .setCategory(category)
            .build();
    }
}
