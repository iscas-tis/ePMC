package epmc.jani.model;

public interface ActionInstance extends JANINode {
	public Action getAction();

	public ActionInstance match(ActionInstance other);	
	
	public ActionInstance rename(Action to);
}
