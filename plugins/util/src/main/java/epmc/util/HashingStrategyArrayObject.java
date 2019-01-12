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

import java.util.Arrays;

import it.unimi.dsi.fastutil.Hash;

/**
 * Hashing strategy for object arrays.
 * This strategy is used to allow hashing arrays in fastutil hash maps without
 * having to use a wrapper object for the arrays. The {@link Object#hashCode()}
 * and {@link Object#equals(Object)} methods of arrays the ones of
 * {@link Object}, which are based on object identity, not object content. This
 * hash function provides hash and equality functions based on the content of
 * object arrays.
 * 
 * @author Ernst Moritz Hahn
 */
public final class HashingStrategyArrayObject implements Hash.Strategy<Object[]> {
    /** We only need a single instance of the hash strategy. */
    private static final HashingStrategyArrayObject INSTANCE
    = new HashingStrategyArrayObject();

    @Override
    public int hashCode(Object[] arg0) {
        assert arg0 != null;
        return Arrays.hashCode(arg0);
    }

    @Override
    public boolean equals(Object[] arg0, Object[] arg1) {
        return Arrays.equals(arg0, arg1);
    }

    /**
     * Get instance of the hashing strategy.
     * 
     * @return instance of the hashing strategy
     */
    public static HashingStrategyArrayObject getInstance() {
        return INSTANCE;
    }

    /**
     * Private constructor to ensure that only one instance is created.
     */
    private HashingStrategyArrayObject() {
    }
}
