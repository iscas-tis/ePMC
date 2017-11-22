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

package epmc.propertysolvercoalition;

/**
 * Name of test case models for the coalition solver (2 1/2 player) plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ModelNames {
    /** Prefix of path where models are stored. */
    private final static String PREFIX = "epmc/propertysolvercoalition/";

    /** Two robots model from our paper. */
    public final static String ROBOTS = PREFIX + "robots.prism";
    /** Small version of the robot model from our paper. */
    public final static String ROBOTS_SMALL = PREFIX + "robots-small.prism";

    public final static String ROBOTS_MODIFIED = PREFIX + "robots-modified.prism";

    public final static String ROBOTS_MODIFIED_SMALL = PREFIX + "robots-modified-small.prism";

    public final static String ROBOTS_MODIFIED_MEDIUM = PREFIX + "robots-modified-medium.prism";

    public final static String ROBOTS_QUANTITATIVE = PREFIX + "robots-quantitative.prism";

    public final static String ROBOTS_SMALL4 = PREFIX + "robots-small4.prism";

    public final static String TWO_INVESTORS = PREFIX + "two_investors.prism";

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ModelNames() {
    }
}
