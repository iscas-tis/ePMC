package epmc.lumpingdd;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import epmc.dd.DD;
import epmc.dd.Permutation;
import epmc.error.EPMCException;
import epmc.graph.dd.GraphDD;
import epmc.graph.dd.GraphDDProperties;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.ValueObject;

public final class GraphDDGeneric implements GraphDD {

	public final static class Builder {
		private ContextValue contextValue;
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

		public Builder setContextValue(ContextValue contextValue) {
			this.contextValue = contextValue;
			return this;
		}
		
		private ContextValue getContextValue() {
			return contextValue;
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
			graphProperties.get(key).set(value);
			return this;
		}

		public Builder setGraphPropertyObject(Object key, Object value) {
			ValueObject.asObject(graphProperties.get(key)).set(value);
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

	private final ContextValue contextValue;
	private final DD initialNodes;
	private final DD transitions;
	private final Permutation swapPresNext;
	private boolean closed;

	private GraphDDGeneric(Builder builder) {
		assert builder != null;
		assert builder.getContextValue() != null;
		assert builder.getInitialNodes() != null;
		assert builder.getTransitions() != null;
		this.contextValue = builder.getContextValue();
		this.initialNodes = builder.getInitialNodes().clone();
		this.transitions = builder.getTransitions().clone();
		this.swapPresNext = builder.getSwapPresNext();
		// TODO Auto-generated constructor stub
	}

	@Override
	public ContextValue getContextValue() {
		return contextValue;
	}

	@Override
	public DD getInitialNodes() throws EPMCException {
		return initialNodes;
	}

	@Override
	public DD getTransitions() throws EPMCException {
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
	public DD getNodeSpace() throws EPMCException {
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
