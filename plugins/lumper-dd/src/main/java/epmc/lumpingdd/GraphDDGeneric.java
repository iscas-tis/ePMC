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

package epmc.lumpingdd;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.Permutation;
import epmc.graph.dd.GraphDD;
import epmc.graph.dd.GraphDDProperties;
import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.ValueObject;

public final class GraphDDGeneric implements GraphDD {

    public final static class Builder {
        private DD initialNodes;
        private final Map<Object,Value> graphProperties = new LinkedHashMap<>();
        private final Map<Object,DD> nodeProperties = new LinkedHashMap<>();
        private final Map<Object,DD> edgeProperties = new LinkedHashMap<>();
        private List<DD> presVars;
        private List<DD> nextVars;
        private DD nodeSpace;
        private Permutation swapPresNext;
        private DD transitions;

        public GraphDDGeneric build() {
            return new GraphDDGeneric(this);
        }

        public Builder setInitialNodes(DD initialNodes) {
            this.initialNodes = initialNodes;
            return this;
        }

        private DD getInitialNodes() {
            return initialNodes;
        }

        public Builder registerGraphProperty(Object key, Type type) {
            graphProperties.put(key, type.newValue());
            return this;
        }

        public Builder setGraphProperty(Object key, Value value) {
            OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, value.getType(), graphProperties.get(key).getType());
            set.apply(graphProperties.get(key), value);
            return this;
        }

        public Builder setGraphPropertyObject(Object key, Object value) {
            ValueObject.as(graphProperties.get(key)).set(value);
            return this;
        }

        public Builder registerNodeProperty(Object key, DD value) {
            nodeProperties.put(key, value);
            return this;
        }

        public Builder registerEdgeProperty(Object key, DD value) {
            edgeProperties.put(key, value);
            return this;
        }

        public Builder setPresVars(List<DD> presVars) {
            this.presVars = presVars;
            return this;
        }

        public Builder setNextVars(List<DD> nextVars) {
            this.nextVars = nextVars;
            return this;
        }

        public Builder setNodeSpace(DD nodeSpace) {
            this.nodeSpace = nodeSpace;
            return this;
        }

        public Builder setSwapPresNext(Permutation swapPresNext) {
            this.swapPresNext =  swapPresNext;
            return this;
        }

        private Permutation getSwapPresNext() {
            return swapPresNext;
        }

        public Builder setTransitions(DD transitions) {
            this.transitions = transitions;
            return this;
        }

        private DD getTransitions() {
            return transitions;
        }
    }

    private final DD initialNodes;
    private final DD transitions;
    private final Permutation swapPresNext;
    private boolean closed;

    private GraphDDGeneric(Builder builder) {
        assert builder != null;
        assert builder.getInitialNodes() != null;
        assert builder.getTransitions() != null;
        this.initialNodes = builder.getInitialNodes().clone();
        this.transitions = builder.getTransitions().clone();
        this.swapPresNext = builder.getSwapPresNext();
        // TODO Auto-generated constructor stub
    }

    @Override
    public DD getInitialNodes() {
        return initialNodes;
    }

    @Override
    public DD getTransitions() {
        return transitions;
    }

    @Override
    public DD getPresCube() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DD getNextCube() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DD getActionCube() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Permutation getSwapPresNext() {
        return swapPresNext;
    }

    @Override
    public DD getNodeSpace() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        initialNodes.dispose();
        closed = true;
        // TODO Auto-generated method stub

    }

    @Override
    public GraphDDProperties getProperties() {
        // TODO Auto-generated method stub
        return null;
    }

}
