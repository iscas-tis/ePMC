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

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

/**
 * Represents a permutation of DD variables.
 * In contrast to {@link PermutationLibraryDD}, this class represents
 * permutations independent of the DD libraries used by the DD context.
 * 
 * @author Ernst Moritz Hahn
 */
public final class Permutation  {
    /** Context the permutation belongs to. */
    private final ContextDD contextDD;
    /** Maps DD libraries to according library permutation. */
    private final Map<LibraryDD,PermutationLibraryDD> libraryPermutations = new IdentityHashMap<>();
    /** Maps each index to the new variable to which it is permuted. */
    private int[] array;

    /**
     * Create new permutation.
     * The array parameter must represent a permutation. The length of this
     * array must be equal to {@link ContextDD#numVariables()}. None of the
     * parameters may be {@code null}.
     * 
     * @param contextDD context to which this permutation will belong
     * @param array array representing a permutation
     */
    Permutation(ContextDD contextDD, int[] array) {
        assert contextDD != null;
        assert assertPermutation(array);
        assert array.length == contextDD.numVariables();
        this.array = array;
        this.contextDD = contextDD;
        createLowLevelPermutations();
    }

    /**
     * Assert that an array represents a permutation.
     * If an array is a permutation, this means that each array element contains
     * an integer i with 0 &le; i &lt; array.length, and these integers are
     * unique. This function will throw an {@Link AssertionError} if assertions
     * are enabled and the array does not represent a permutation. If the array
     * is {@code null}, this will also result in an assertion error. Otherwise,
     * it will return {@code true}.
     * 
     * @param array array to check for being a permutation
     * @return {@code true} if the array is a permutation
     */
    private static boolean assertPermutation(int[] array) {
        assert array != null;
        IntOpenHashSet seen = new IntOpenHashSet();
        for (int entry : array) {
            assert entry >= 0;
            assert entry < array.length;
            assert !seen.contains(entry);
            seen.add(entry);
        }
        assert seen.size() == array.length;
        return true;
    }

    /**
     * Create the low-level permutations for the DD libraries used.
     * In addition, the permutation array will be extended if necessary. This
     * needs to be done if after the creation of the permutations new DD
     * variables have been created.
     * 
     */
    private void createLowLevelPermutations() {
        if (array.length != contextDD.numVariables()) {
            int[] newArray = Arrays.copyOf(array, contextDD.numVariables());
            for (int variable = array.length; variable < contextDD.numVariables(); variable++) {
                newArray[variable] = variable;
            }
            array = newArray;
        }
        libraryPermutations.clear();
        for (LibraryDD lowLevel : contextDD.getLowLevels()) {
            PermutationLibraryDD permutationLowLevel = lowLevel.newPermutation(array);
            libraryPermutations.put(lowLevel, permutationLowLevel);
        }
    }

    /**
     * Get context to which this permutation belongs.
     * 
     * @return context to which this permutation belongs
     */
    public ContextDD getContextDD() {
        return contextDD;
    }

    /**
     * Get variable to which the variable is permuted.
     * 
     * @param variable variable of which to get permuted version
     * @return variable to which the variable is permuted.
     */
    public int getPermuted(int variable) {
        assert variable >= 0;
        assert variable < contextDD.numVariables();
        if (variable < array.length) {
            return array[variable];
        } else {
            return variable;
        }
    }

    /**
     * Get a DD library permutation for the given library.
     * The library parameter must not be {@code null}. The DD library must be a
     * library for the context of the permutation.
     * 
     * @param libraryDD library to get DD library permutation of
     * @return DD library permutation for the given DD library
     */
    PermutationLibraryDD getLowLevel(LibraryDD libraryDD) {
        assert libraryDD != null;
        assert libraryDD.getContextDD() == contextDD;
        if (array.length != contextDD.numVariables()) {
            createLowLevelPermutations();
        }
        assert libraryPermutations.containsKey(libraryDD);
        return libraryPermutations.get(libraryDD);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(Arrays.toString(array));
        return builder.toString();
    }
}
