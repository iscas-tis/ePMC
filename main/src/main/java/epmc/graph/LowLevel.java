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

package epmc.graph;

import java.io.Closeable;
import java.util.Set;

import epmc.expressionevaluator.ExpressionToType;
import epmc.modelchecker.Engine;
import epmc.modelchecker.Model;

/**
 * Low-level representation of a model.
 * 
 * @author Ernst Moritz Hahn
 *
 */
public interface LowLevel extends Closeable, ExpressionToType {
    public interface Builder {
        Builder setModel(Model model);
        
        Builder setEngine(Engine engine);

        Builder addGraphProperties(Set<Object> graphProperties);
        
        Builder addNodeProperties(Set<Object> nodeProperties);
        
        Builder addEdgeProperties(Set<Object> edgeProperties);
        
        LowLevel build();
    }
    
    /**
     * Create new set of initial states of this low-level model.
     * 
     * @return new set of initial states
     */
    StateSet newInitialStateSet();

    @Override
    void close();
}
