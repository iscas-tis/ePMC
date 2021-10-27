package epmc.propertysolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UCTNode {

	private boolean isDecision;
	private int state;
	private String action;
	private double probability;
	private int visitedTimes = 0;
	private List<UCTNode> successors;
	private double R;
	private UCTNode bestSucc;
	private boolean isInitialized = false;
	private Map<List<FixedAction>, Double> fixedActionsToR;

	public boolean isInitialized() {
		return isInitialized;
	}

	public void setInitialized(boolean isInitialized) {
		this.isInitialized = isInitialized;
	}

	public UCTNode(int state)
	{
		this.state = state;
		successors = new ArrayList<UCTNode>();
	}
	
	public UCTNode(int state, String action, boolean isDecision)
	{
		this.state = state;
		this.action = action;
		this.isDecision = isDecision;
		successors = new ArrayList<UCTNode>();
		fixedActionsToR = new HashMap<List<FixedAction>, Double>();
	}
	
	public double getRByFixedActions(List<FixedAction> fa) {
		if(fixedActionsToR.containsKey(fa))
			return fixedActionsToR.get(fa);
		else
			return -1;
	}

	public void setRByFixedActions(List<FixedAction> fa, double r) {
		this.fixedActionsToR.put(fa, r);
	}

	public double reComputeR()
	{
		for(double res : fixedActionsToR.values())
		{
			if(res > this.R)
			{
				this.R = res;
			}
		}
		return this.R;
	}
	
	public void increaseVisitedTimes()
	{
		this.visitedTimes += 1;
	}
	
	public boolean isDecision()
	{
		return isDecision;
	}
	
	public void addSuccessor(UCTNode succNode)
	{
		this.successors.add(succNode);
	}
	
	public int getState() {
		return state;
	}

	public double getProbability() {
		return probability;
	}

	public void setProbability(double probability) {
		this.probability = probability;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public int getVisitedTimes() {
		return visitedTimes;
	}

	public void setVisitedTimes(int visitedTimes) {
		this.visitedTimes = visitedTimes;
	}

	public List<UCTNode> getSuccessors() {
		return successors;
	}

	public UCTNode getBestSucc() {
		return bestSucc;
	}

	public void setBestSucc(UCTNode bestSucc) {
		this.bestSucc = bestSucc;
	}

	public void setSuccessors(List<UCTNode> successors) {
		this.successors = successors;
	}

	public double getR() {
		return R;
	}

	public void setR(double r) {
		R = r;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(R);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result + (isDecision ? 1231 : 1237);
		temp = Double.doubleToLongBits(probability);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + state;
		result = prime * result + visitedTimes;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UCTNode other = (UCTNode) obj;
		if (Double.doubleToLongBits(R) != Double.doubleToLongBits(other.R))
			return false;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		if (isDecision != other.isDecision)
			return false;
		if (Double.doubleToLongBits(probability) != Double.doubleToLongBits(other.probability))
			return false;
		if (state != other.state)
			return false;
		if (visitedTimes != other.visitedTimes)
			return false;
		return true;
	}
	
}

class FixedAction {
	String player;
	int state;
	String action;
	
	public FixedAction(String player, int state, String action)
	{
		this.player = player;
		this.state = state;
		this.action = action;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result + ((player == null) ? 0 : player.hashCode());
		result = prime * result + state;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FixedAction other = (FixedAction) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		if (player == null) {
			if (other.player != null)
				return false;
		} else if (!player.equals(other.player))
			return false;
		if (state != other.state)
			return false;
		return true;
	}
	
}
