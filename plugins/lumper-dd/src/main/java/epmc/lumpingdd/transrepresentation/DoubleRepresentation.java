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
import epmc.graph.CommonProperties;
import epmc.graph.dd.GraphDD;

/**
 * In contrast to what the name of this class might suggest
 * the DDs produced by this class might actually be integers
 * if the original system has only integers in its transition
 * relation.
 */
public class DoubleRepresentation implements TransitionRepresentation {

    private GraphDD original;

    @Override
    public void setOriginal(GraphDD original) {
        this.original = original;
    }

    @Override
    public DD fromTransWeights() {
        // We are not interested in actions
        return original.getEdgeProperty(CommonProperties.WEIGHT)
                .abstractSum(original.getActionCube());
    }

}
