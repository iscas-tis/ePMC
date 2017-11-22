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

package epmc.plugin;

import java.util.List;

/**
 * Class collections names of options for plugin part of EPMC.
 * 
 * @author Ernst Moritz Hahn
 */
public enum OptionsPlugin {
    /** Resource identifier. */
    OPTIONS_PLUGIN,
    /** {@link List} of plugin filename {@link String}s. */
    PLUGIN,
    /** Filename specifying list of plugins to load. */
    PLUGIN_LIST_FILE,
    /** Stores plugin interface classes. */
    PLUGIN_INTERFACE_CLASS,
}
