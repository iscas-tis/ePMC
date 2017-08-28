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

package epmc.util;

/**
 * Interface for objects which shall be written to a {@link BitStream}.
 * The purpose of this interface is thus to provide a way for very compact
 * serialization of objects, which do not store type information, but only the
 * current state of the object.
 * 
 * @author Ernst Moritz Hahn
 */
public interface BitStoreable {
    /**
     * Read the object content from given bit stream.
     * 
     * @param reader bit stream to read object from
     */
    void read(BitStream reader);

    /**
     * Write object to given bit stream.
     * 
     * @param writer bit stream to write object to
     */
    void write(BitStream writer);
}
