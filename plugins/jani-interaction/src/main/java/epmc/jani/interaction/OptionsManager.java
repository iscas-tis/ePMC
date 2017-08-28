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

package epmc.jani.interaction;

import static epmc.error.UtilError.fail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;

import epmc.error.EPMCException;
import epmc.jani.interaction.database.Database;
import epmc.jani.interaction.error.ProblemsJANIInteractionJDBC;
import epmc.jani.interaction.options.OptionsJANIInteractionJDBC;
import epmc.options.Option;
import epmc.options.Options;

public final class OptionsManager {
    private final static String SQL_CREATE_OPTIONS_TABLE = "CREATE TABLE IF NOT EXISTS options (id {0}, user INTEGER, option VARCHAR, value VARCHAR);";
    private final static String SQL_DELETE_BY_USER = "DELETE FROM options WHERE user = ?;";
    private final static String SQL_INSERT_BY_USER = "INSERT INTO options (user,option,value) VALUES (?,?,?);";
    private final static String SQL_SELECT_BY_USER = "SELECT option,value FROM options WHERE user = ?;";

    private final Connection connection;
    private final PreparedStatement deleteByUser;
    private final PreparedStatement insertByUser;
    private final PreparedStatement selectByUser;

    public OptionsManager(Database storage) {
        assert storage != null;
        connection = storage.getConnection();
        String createOptionsTableString = MessageFormat.format(SQL_CREATE_OPTIONS_TABLE, 
                new Object[]{Options.get().getString(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_DBTYPE_PRIMARY_KEY_AUTOINCREMENT)});
        try {
            PreparedStatement createOptionsTable = connection.prepareStatement(createOptionsTableString);
            createOptionsTable.execute();
            connection.commit();
            deleteByUser = connection.prepareStatement(SQL_DELETE_BY_USER);
            insertByUser = connection.prepareStatement(SQL_INSERT_BY_USER);
            selectByUser = connection.prepareStatement(SQL_SELECT_BY_USER);
        } catch (SQLException e) {
            fail(ProblemsJANIInteractionJDBC.JANI_INTERACTION_JDBC_SQL_ERROR, e, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void write(int user, boolean admin, Options options) {
        assert options != null;
        try {
            deleteByUser.setInt(1, user);
            deleteByUser.execute();
            for (Option option : options.getAllOptions().values()) {
                if (!option.isAlreadyParsed()) {
                    continue;
                }
                if (!admin && !option.isWeb()) {
                    continue;
                }
                if (!option.isGUI() || option.isWeb()) {
                    continue;
                }
                insertByUser.setInt(1, user);
                insertByUser.setString(2, option.getIdentifier());
                insertByUser.setString(3, option.getUnparsed());
                insertByUser.execute();
            }
            connection.commit();
        } catch (SQLException e) {
            fail(ProblemsJANIInteractionJDBC.JANI_INTERACTION_JDBC_SQL_ERROR, e, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void read(int user, boolean admin, Options options) {
        assert options != null;
        for (Option option : options.getAllOptions().values()) {
            if (!admin && !option.isWeb()) {
                continue;
            }
            if (!option.isGUI() || option.isWeb()) {
                continue;
            }
            option.unset();
        }
        try {
            ResultSet result = selectByUser.executeQuery();
            while (result.next()) {
                String identifier = result.getString(1);
                String value = result.getString(2);
                Option option = options.getOption(identifier);
                if (option == null) {
                    /* Options might change, this ignore invalid options. */
                    continue;
                }
                if (!admin && !option.isWeb()) {
                    continue;
                }
                if (!option.isGUI() || option.isWeb()) {
                    continue;
                }
                try {
                    option.parse(value);
                } catch (EPMCException e) {
                    /* invalid values in the database should be ignored,
                     * because validity and invalidity might change over time */
                }
            }
        } catch (SQLException e) {
            fail(ProblemsJANIInteractionJDBC.JANI_INTERACTION_JDBC_SQL_ERROR, e, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
