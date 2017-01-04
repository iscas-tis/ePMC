package epmc.uct;

import java.util.List;

public class UCTNode implements Node {
	
	private int partialState;         /* partial state index, state index*/
	private int action;               /* action index */
	private int numOfChosenRolloutsK; /* number of roll outs among K first rollouts where node was chosen*/
	private int expectedRewardsK;     /* expected reward estimate based on the K first rollouts */
	private List<UCTNode> children;   /* successor nodes of current node */
	

	@Override
	public void selectAction() {
		// TODO Auto-generated method stub

	}

	@Override
	public void expand() {
		// TODO Auto-generated method stub

	}

	@Override
	public Node select() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isLeaf() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double rollOut(Node n) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void updateStats(double value) {
		// TODO Auto-generated method stub

	}

	@Override
	public int arity() {
		// TODO Auto-generated method stub
		return 0;
	}

}
