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

/**
 * This plugins supports the JANI high-level model description.
 * JANI stands for JSON Automata Network Interchange format.
 * This specification is intended to be used as
 * <ul>
 * <li>an exchange format between EPMC and tool chains developed by
 * Arnd Hartmanns and others,</li>
 * <li>potentially used internally in EPMC by transforming other high-level
 * model descriptions to this format to ease supporting them,</li>
 * <li>a specification mechanism for which we can support a graphical model
 * editor.</li>
 * </ul>
 * 
 * A specification of the format (work in progress) can be found at
 * <a href="https://docs.google.com/document/d/1BDQIzPBtscxJFFlDUEPIo8ivKHgXT8_X6hz5quq7jK0/">
 * https://docs.google.com/document/d/1BDQIzPBtscxJFFlDUEPIo8ivKHgXT8_X6hz5quq7jK0/</a>
 * 
 * @author Ernst Moritz Hahn
 */
package epmc.jani;
