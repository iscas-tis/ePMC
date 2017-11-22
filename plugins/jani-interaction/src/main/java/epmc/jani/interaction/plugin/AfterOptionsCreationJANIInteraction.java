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

package epmc.jani.interaction.plugin;

import java.util.LinkedHashMap;
import java.util.Map;

import epmc.jani.interaction.InteractionExtension;
import epmc.jani.interaction.command.CommandTaskJANIInteractionAddUser;
import epmc.jani.interaction.command.CommandTaskJANIInteractionDeleteUser;
import epmc.jani.interaction.command.CommandTaskJANIInteractionModifyUser;
import epmc.jani.interaction.command.CommandTaskJaniInteractionStartServer;
import epmc.jani.interaction.communication.resultformatter.ResultFormatter;
import epmc.jani.interaction.communication.resultformatter.ResultFormatterBool;
import epmc.jani.interaction.communication.resultformatter.ResultFormatterDouble;
import epmc.jani.interaction.communication.resultformatter.ResultFormatterGeneral;
import epmc.jani.interaction.communication.resultformatter.ResultFormatterInt;
import epmc.jani.interaction.options.JANIInteractionIO;
import epmc.jani.interaction.options.OptionsJANIInteraction;
import epmc.jani.interaction.options.OptionsJANIInteractionJDBC;
import epmc.jani.interaction.permanentstorage.InteractionExtensionPermanentStorage;
import epmc.main.options.OptionsEPMC;
import epmc.modelchecker.CommandTask;
import epmc.options.Category;
import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeEnum;
import epmc.options.OptionTypeIntegerNonNegative;
import epmc.options.OptionTypeString;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;
import epmc.util.OrderedMap;

/**
 * JANI interaction plugin class containing method to execute after options creation.
 * 
 * @author Ernst Moritz Hahn
 */
public final class AfterOptionsCreationJANIInteraction implements AfterOptionsCreation {
    private final static String JANI_INTERACTION_JDBC_DRIVER_CLASS_DEFAULT = "org.sqlite.JDBC";
    private final static String JANI_INTERACTION_JDBC_URL_DEFAULT = "jdbc:sqlite:jani-default.db";
    private final static String JANI_INTERACTION_JDBC_USERNAME_DEFAULT = "user";
    private final static String JANI_INTERACTION_JDBC_PASSWORD_DEFAULT = "password";
    /** Identifier of this class. */
    private final static String IDENTIFIER = "after-options-creation-jani-interaction";
    /** Default WebSocket server port. */
    private final static int WEBSOCKET_SERVER_PORT_DEFAULT = 15291;
    private final static String DEFAULT_DB_TYPE_PRIMARY_KEY_AUTOINCREMENT = "INTEGER PRIMARY KEY AUTOINCREMENT";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        assert options != null;
        addOptionsAndCommands(options);
        addResultFormatters(options);
        processJDBC(options);
        Map<String,Class<? extends InteractionExtension>> extensions = options.get(OptionsJANIInteraction.JANI_INTERACTION_EXTENSION_CLASS);
        if (extensions == null) {
            extensions = new LinkedHashMap<>();
        }
        extensions.put(InteractionExtensionPermanentStorage.IDENTIFIER, InteractionExtensionPermanentStorage.class);
    }

    private void addOptionsAndCommands(Options options) {
        assert options != null;
        Category category = options.addCategory()
                .setBundleName(OptionsJANIInteraction.OPTIONS_JANI_INTERACTION)
                .setIdentifier(OptionsJANIInteraction.JANI_INTERACTION_CATEGORY)
                .build();

        OptionTypeIntegerNonNegative typeIntegerNonNegative = OptionTypeIntegerNonNegative.getInstance();
        options.addOption().setBundleName(OptionsJANIInteraction.OPTIONS_JANI_INTERACTION)
        .setIdentifier(OptionsJANIInteraction.JANI_INTERACTION_WEBSOCKET_SERVER_PORT)
        .setType(typeIntegerNonNegative).setDefault(WEBSOCKET_SERVER_PORT_DEFAULT)
        .setCommandLine()
        .setCategory(category).build();
        OptionTypeEnum typeJANIInteraction = new OptionTypeEnum(JANIInteractionIO.class);
        options.addOption().setBundleName(OptionsJANIInteraction.OPTIONS_JANI_INTERACTION)
        .setIdentifier(OptionsJANIInteraction.JANI_INTERACTION_TYPE)
        .setType(typeJANIInteraction).setDefault(JANIInteractionIO.STDIO)
        .setCommandLine()
        .setCategory(category).build();

        Map<String,Class<? extends CommandTask>> commandTaskClasses = options.get(OptionsEPMC.COMMAND_CLASS);
        assert commandTaskClasses != null;
        options.addCommand()
        .setBundleName(OptionsJANIInteraction.OPTIONS_JANI_INTERACTION)
        .setIdentifier(CommandTaskJaniInteractionStartServer.IDENTIFIER)
        .setCommandLine()
        .build();
        commandTaskClasses.put(CommandTaskJaniInteractionStartServer.IDENTIFIER, CommandTaskJaniInteractionStartServer.class);
        commandTaskClasses.put(CommandTaskJANIInteractionAddUser.IDENTIFIER, CommandTaskJANIInteractionAddUser.class);
        commandTaskClasses.put(CommandTaskJANIInteractionModifyUser.IDENTIFIER, CommandTaskJANIInteractionModifyUser.class);
        commandTaskClasses.put(CommandTaskJANIInteractionDeleteUser.IDENTIFIER, CommandTaskJANIInteractionDeleteUser.class);

        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();
        OptionTypeString typeString = OptionTypeString.getInstance();

        options.addOption().setBundleName(OptionsJANIInteraction.OPTIONS_JANI_INTERACTION)
        .setIdentifier(OptionsJANIInteraction.JANI_INTERACTION_WEBSOCKET_ANONYMOUS_LOGINS)
        .setType(typeBoolean).setDefault(false)
        .setCommandLine()
        .setCategory(category).build();

        OptionTypeEnum typeServer = new OptionTypeEnum(OptionsJANIInteraction.ServerType.class);
        options.addOption().setBundleName(OptionsJANIInteraction.OPTIONS_JANI_INTERACTION)
        .setIdentifier(OptionsJANIInteraction.JANI_INTERACTION_ANALYSIS_SERVER_TYPE)
        .setType(typeServer).setDefault(OptionsJANIInteraction.ServerType.SAME_PROCESS)
        .setCommandLine().setWeb()
        .setCategory(category).build();

        options.addOption().setBundleName(OptionsJANIInteraction.OPTIONS_JANI_INTERACTION)
        .setIdentifier(OptionsJANIInteraction.JANI_INTERACTION_START_GUI)
        .setType(typeBoolean).setDefault(false)
        .setCommandLine().setCategory(category).build();

        options.addOption().setBundleName(OptionsJANIInteraction.OPTIONS_JANI_INTERACTION)
        .setIdentifier(OptionsJANIInteraction.JANI_INTERACTION_MODIFIED_USERNAME)
        .setType(typeString).setCommandLine().setCategory(category).build();

        options.addOption().setBundleName(OptionsJANIInteraction.OPTIONS_JANI_INTERACTION)
        .setIdentifier(OptionsJANIInteraction.JANI_INTERACTION_MODIFIED_PASSWORD)
        .setType(typeString).setCommandLine().setCategory(category).build();        

        options.addOption().setBundleName(OptionsJANIInteraction.OPTIONS_JANI_INTERACTION)
        .setIdentifier(OptionsJANIInteraction.JANI_INTERACTION_PRINT_MESSAGES)
        .setType(typeBoolean).setDefault(false)
        .setCommandLine().setCategory(category).build();
    }

    private void addResultFormatters(Options options) {
        assert options != null;
        Map<String,Class<? extends ResultFormatter>> resultFormatter = new OrderedMap<>(true);
        resultFormatter.put(ResultFormatterGeneral.IDENTIFIER, ResultFormatterGeneral.class);
        resultFormatter.put(ResultFormatterBool.IDENTIFIER, ResultFormatterBool.class);
        resultFormatter.put(ResultFormatterDouble.IDENTIFIER, ResultFormatterDouble.class);
        resultFormatter.put(ResultFormatterInt.IDENTIFIER, ResultFormatterInt.class);
        options.set(OptionsJANIInteraction.JANI_INTERACTION_RESULT_FORMATTER_CLASS, resultFormatter);
    }

    private void processJDBC(Options options) {
        assert options != null;
        OptionTypeString optionTypeString = OptionTypeString.getInstance();
        Category category = options.addCategory()
                .setBundleName(OptionsJANIInteractionJDBC.OPTIONS_JANI_INTERACTION_JDBC)
                .setParent(OptionsJANIInteraction.JANI_INTERACTION_CATEGORY)
                .setIdentifier(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_CATEGORY)
                .build();

        options.addOption()
        .setCategory(category)
        .setBundleName(OptionsJANIInteractionJDBC.OPTIONS_JANI_INTERACTION_JDBC)
        .setIdentifier(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_DRIVER_JAR)
        .setType(optionTypeString)
        .setCommandLine().setGui().build();

        options.addOption()
        .setCategory(category)
        .setBundleName(OptionsJANIInteractionJDBC.OPTIONS_JANI_INTERACTION_JDBC)
        .setIdentifier(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_DRIVER_CLASS)
        .setDefault(JANI_INTERACTION_JDBC_DRIVER_CLASS_DEFAULT)
        .setType(optionTypeString)
        .setCommandLine().setGui().build();

        options.addOption()
        .setCategory(category)
        .setBundleName(OptionsJANIInteractionJDBC.OPTIONS_JANI_INTERACTION_JDBC)
        .setIdentifier(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_URL)
        .setDefault(JANI_INTERACTION_JDBC_URL_DEFAULT)
        .setType(optionTypeString)
        .setCommandLine().setGui().build();

        options.addOption()
        .setCategory(category)
        .setBundleName(OptionsJANIInteractionJDBC.OPTIONS_JANI_INTERACTION_JDBC)
        .setIdentifier(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_USERNAME)
        .setDefault(JANI_INTERACTION_JDBC_USERNAME_DEFAULT)
        .setType(optionTypeString)
        .setCommandLine().setGui().build();

        options.addOption()
        .setCategory(category)
        .setBundleName(OptionsJANIInteractionJDBC.OPTIONS_JANI_INTERACTION_JDBC)
        .setIdentifier(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_PASSWORD)
        .setDefault(JANI_INTERACTION_JDBC_PASSWORD_DEFAULT)
        .setType(optionTypeString)
        .setCommandLine().setGui().build();

        options.addOption()
        .setCategory(category)
        .setBundleName(OptionsJANIInteractionJDBC.OPTIONS_JANI_INTERACTION_JDBC)
        .setIdentifier(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_DBTYPE_PRIMARY_KEY_AUTOINCREMENT)
        .setType(optionTypeString)
        .setDefault(DEFAULT_DB_TYPE_PRIMARY_KEY_AUTOINCREMENT)
        .setCommandLine().setGui().build();
    }

}
