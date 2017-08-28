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

package epmc.jani.interaction.options;

/**
 * Class collecting options used for JANI interaction plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public enum OptionsJANIInteractionJDBC {
    /** Base name of resource file for options description. */
    OPTIONS_JANI_INTERACTION_JDBC,
    /** Category used for JANI interaction options. */
    JANI_INTERACTION_JDBC_CATEGORY,
    /** JAR file containing a JDBC driver to load. */
    JANI_INTERACTION_JDBC_DRIVER_JAR,
    /** JDBC driver class to load. */
    JANI_INTERACTION_JDBC_DRIVER_CLASS,
    /** URL to use to establish JDBC connection. */
    JANI_INTERACTION_JDBC_URL,
    /** User name to use to establish JDBC connection. */
    JANI_INTERACTION_JDBC_USERNAME,
    /** Password to use to establish JDBC connection. */
    JANI_INTERACTION_JDBC_PASSWORD,
    /** Type to use for auto-incrementing primary key. */
    JANI_INTERACTION_JDBC_DBTYPE_PRIMARY_KEY_AUTOINCREMENT,
    ;

    /**
     * Server type to use.
     * 
     * @author Ernst Moritz Hahn
     */
    public enum ServerType {
        /** Run analysis server in same process as EPMC .*/
        SAME_PROCESS,
        /** Start a new process to run server. */
        LOCAL
    }
}
