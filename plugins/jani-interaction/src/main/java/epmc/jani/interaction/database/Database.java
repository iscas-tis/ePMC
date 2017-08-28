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

package epmc.jani.interaction.database;

import static epmc.error.UtilError.fail;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.Properties;

import epmc.jani.interaction.error.ProblemsJANIInteractionJDBC;
import epmc.jani.interaction.options.OptionsJANIInteractionJDBC;
import epmc.options.Options;

/**
 * Permanent storage class.
 * 
 * @author Ernst Moritz Hahn
 */
public final class Database {
    @FunctionalInterface
    private static interface ToCacheNoResult {
        void execute() throws SQLException;
    }

    @FunctionalInterface
    private static interface ToCacheObjectResult {
        Object execute() throws SQLException;
    }

    @FunctionalInterface
    private static interface ToCacheBooleanResult {
        boolean execute() throws SQLException;
    }

    /** Identifier of this user manager class. */
    public final static String IDENTIFIER = "jdbc";
    /** JDBC property to identify user to connect to database. */
    private final static String JDBC_PROPERTY_USER = "user";
    /** JDBC property to identify password to connect to database. */
    private final static String JDBC_PROPERTY_PASSWORD = "password";
    /** String to prepend to filename to marks as JAR file for class loader. */
    private final static String JAR_FILE_URL_START = "jar:file:";
    /** String to append to filename to marks as JAR file for class loader. */
    private final static String JAR_FILE_URL_END = "!/";

    /** JDBC connection used. */
    private final Connection connection;
    private final String primaryKeyTypeString;

    public Database() {
        loadDriver();
        connection = establishConnection();
        primaryKeyTypeString = Options.get().getString(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_DBTYPE_PRIMARY_KEY_AUTOINCREMENT);
    }

    /**
     * Load JDBC driver if requested by user.
     * The options parameter must not be {@code null}.
     * 
     */
    private static void loadDriver() {
        String driverJAR = Options.get().getString(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_DRIVER_JAR);
        String driverClassName = Options.get().getString(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_DRIVER_CLASS);
        if (driverClassName != null) {
            return;
        }
        URL url = null;
        try {
            if (driverJAR != null) {
                url = new URL(JAR_FILE_URL_START + new File(driverJAR).getAbsolutePath() + JAR_FILE_URL_END);
            }
        } catch (MalformedURLException e) {
            /* Should not happen, because we create the URL ourselves. */
            throw new RuntimeException(e);
        }
        URLClassLoader urlClassLoader = null;
        if (url != null) {
            urlClassLoader = new URLClassLoader(new URL[] {url});
        }
        Class<?> driverClass = null;
        try {
            if (urlClassLoader != null) {
                driverClass = Class.forName(driverClassName, true, urlClassLoader);
            } else {
                driverClass = Class.forName(driverClassName);				
            }
        } catch (ClassNotFoundException e) {
            fail(ProblemsJANIInteractionJDBC.JANI_INTERACTION_JDBC_DRIVER_CLASS_NOT_FOUND, e, driverClassName);
        }
        Driver driver = null;
        try {
            driver = (Driver) driverClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            fail(ProblemsJANIInteractionJDBC.JANI_INTERACTION_JDBC_DRIVER_CLASS_INSTANTIATION_FAILED, e, driverClassName);
        }
        try {
            DriverManager.registerDriver(new DriverDelegate(driver));
        } catch (SQLException e) {
            fail(ProblemsJANIInteractionJDBC.JANI_INTERACTION_JDBC_DRIVER_CLASS_REGISTER_FAILED, e, driverClassName);
        }
    }

    /**
     * Establish connection with the database.
     * None of the parameters may be {@code null}.
     * The information to connect to the database will be read from the
     * {@link Options} provided.
     * 
     * @return connection created
     */
    private static Connection establishConnection() {
        String url = Options.get().getString(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_URL);
        String username = Options.get().getString(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_USERNAME);
        String password = Options.get().getString(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_PASSWORD);
        Properties connectionProps = new Properties();
        if (username != null) {
            connectionProps.put(JDBC_PROPERTY_USER, username);
        }
        if (password != null) {
            connectionProps.put(JDBC_PROPERTY_PASSWORD, password);
        }
        try {
            Connection result = DriverManager.getConnection(url, connectionProps);
            result.setAutoCommit(false);
            return result;
        } catch (SQLTimeoutException e) {
            fail(ProblemsJANIInteractionJDBC.JANI_INTERACTION_JDBC_CONNECTION_TIMEOUT, e, e.getMessage());
        } catch (SQLException e) {
            fail(ProblemsJANIInteractionJDBC.JANI_INTERACTION_JDBC_CONNECTION_ERROR, e, e.getMessage());
        }
        assert false;
        return null;
    }

    public Connection getConnection() {
        return connection;
    }

    public String getPrimaryKeyTypeString() {
        return primaryKeyTypeString;
    }
}
