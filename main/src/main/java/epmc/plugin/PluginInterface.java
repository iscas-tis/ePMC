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

/**
 * Interface of classes to be loaded by plugin loader.
 * Other interfaces may extend this interface to allow to plugins to execute
 * tasks at specific occasions.
 * 
 * @author Ernst Moritz Hahn
 */
public interface PluginInterface {
    /**
     * Obtain unique identifier for the particular plugin interface
     * @return unique identifier for the particular plugin interface
     */
    String getIdentifier();
}
