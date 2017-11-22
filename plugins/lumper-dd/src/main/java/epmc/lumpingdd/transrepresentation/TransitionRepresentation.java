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

package epmc.lumpingdd.transrepresentation;

import epmc.dd.DD;
import epmc.graph.dd.GraphDD;

public interface TransitionRepresentation {

    /**
     * Set the original graph that the lumper works on
     */
    public void setOriginal(GraphDD original);
    /**
     * Convert the transition weights DD of the original
     * graph to the right representation.
     */
    public DD fromTransWeights();
}
