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

package epmc.messages;

import epmc.options.Options;

/**
 * Options messages part of EPMC.
 * 
 * @author Ernst Moritz Hahn
 */
public enum OptionsMessages {
    /** Base name of resource bundle. */
    OPTIONS_MESSAGES,
    /** Key for boolean parameter whether to translate messages. */
    TRANSLATE_MESSAGES,
    /** Format of time stamps shown alongside messages sent. */
    TIME_STAMPS,
    /** Key to store {@link Log} in {@link Options}. */
    LOG,
}
