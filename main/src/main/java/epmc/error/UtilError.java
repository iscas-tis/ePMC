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

package epmc.error;

/**
 * Utility methods for the {@link epmc.main.error error} package.
 * Several static auxiliary methods are contained. The method family
 * {@code ensure} allows to specify conditions which should be fulfilled, and
 * will throw and {@link EPMCException} otherwise. The methods {@code fail}
 * will generate an {@link EPMCException} with the given properties.
 * Because this class only contains static method, it does not allow
 * instantiation.
 * 
 * @author Ernst Moritz Hahn
 */
public final class UtilError {
    /**
     * Throw new {@link EPMCException} if a condition does not hold.
     * 
     * @param condition condition to check
     * @param problem problem description to use
     * @param arguments
     */
    public static void ensure(boolean condition, Problem problem,
            Object... arguments) {
        if (!condition) {
            throw new EPMCException.Builder()
            .setProblem(problem)
            .setArguments(arguments)
            .build();
        }
    }

    /**
     * Throw new {@link EPMCException} if a condition does not hold.
     * 
     * @param condition condition to check
     * @param problem problem description to use
     * @param cause
     * @param arguments
     */
    public static void ensure(boolean condition, Problem problem,
            Throwable cause, Object... arguments) {
        if (!condition) {
            throw new EPMCException.Builder()
            .setProblem(problem)
            .setCause(cause)
            .setArguments(arguments)
            .build();
        }
    }

    /**
     * Throw new {@link EPMCException} if a condition does not hold.
     * 
     * @param condition condition to check
     * @param problem problem description to use
     * @param positional
     * @param arguments
     */
    public static void ensure(boolean condition, Problem problem,
            Positional positional, Object... arguments) {
        if (!condition) {
            throw new EPMCException.Builder()
            .setProblem(problem)
            .setPositional(positional)
            .setArguments(arguments)
            .build();
        }
    }

    /**
     * Throw new {@link EPMCException}.
     * 
     * @param problem problem description to use
     * @param arguments
     */
    public static void fail(Problem problem, Object... arguments) {
        throw new EPMCException.Builder()
        .setProblem(problem)
        .setArguments(arguments)
        .build();
    }

    /**
     * Throw new {@link EPMCException}.
     * 
     * @param problem problem description to use
     * @param cause
     * @param arguments
     */
    public static void fail(Problem problem, Throwable cause,
            Object... arguments) {
        throw new EPMCException.Builder()
        .setProblem(problem)
        .setCause(cause)
        .setArguments(arguments)
        .build();
    }

    /**
     * Throw new {@link EPMCException}.
     * 
     * @param problem problem description to use
     * @param positional
     * @param arguments
     */
    public static void fail(Problem problem, Positional positional,
            Object... arguments) {
        throw new EPMCException.Builder()
        .setProblem(problem)
        .setPositional(positional)
        .setArguments(arguments)
        .build();
    }    

    /**
     * Creates a new {@link Problem} description.
     * The problem is described the key {@code name} of the resource bundle
     * {@code resourceBundle}. The class loader used is the one obtained by
     * {@link Thread#getContextClassLoader(). None of the parameters may be
     * {@null}. The resource bundle specified must be able to be loaded by the
     * class loader and must contain an entry with key {@code name}.
     * 
     * @param resourceBundle resource bundle containing problem description
     * @param name entry of object description within resource bundle
     * @return problem description created
     */
    public static Problem newProblem(String resourceBundle, String name) {
        assert resourceBundle != null;
        assert name != null;
        return new Problem(resourceBundle, name);
    }    

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private UtilError() {
    }
}
