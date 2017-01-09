package epmc.multiobjective;

import epmc.graph.explicit.GraphExplicit;

final class Product {
	private final GraphExplicit graph;
	private final MultiObjectiveIterationRewards rewards;
	private final int numAutomata;
	
	Product(GraphExplicit graph, MultiObjectiveIterationRewards rewards, int numAutomata) {
		assert graph != null;
		assert rewards != null;
		this.graph = graph;
		this.rewards = rewards;
		this.numAutomata = numAutomata;
	}
	
	GraphExplicit getGraph() {
		return graph;
	}
	
	MultiObjectiveIterationRewards getRewards() {
		return rewards;
	}
	
	int getNumAutomata() {
		return numAutomata;
	}
}
