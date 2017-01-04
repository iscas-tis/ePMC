package epmc.uct;

public interface Node {
	
	public void selectAction();
	
	public void expand();
	// select next node 
	public Node select();
	// whether it is a leaf node to win or lose 
	public boolean isLeaf();
	// roll out tree to evaluate node n
	public double rollOut(Node n);
	
	public void updateStats(double value);
	
	public int arity();
	
}
