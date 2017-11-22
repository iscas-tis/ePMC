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

package epmc.dd;

/**
 * Library-specific permutation.
 * This class is intended to be used in combination with
 * {@link LibraryDD#newPermutation(int[])} and
 * {@link LibraryDD#permute(long, PermutationLibraryDD)} to generate and apply
 * library-specific permutations, the content of which depends on the
 * requirements of the underlying library. This is in contrast to
 * {@link Permutation}, which is library-independent and used to abstract from
 * the implementation details of DD libraries.
 * 
 * @author Ernst Moritz Hahn
 */
public interface PermutationLibraryDD {
}
