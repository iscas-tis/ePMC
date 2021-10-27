package epmc.petl.model;

public enum KnowledgeType {
	K("K"),
	C("C"),
	E("E"),
	D("D");
	
	private final String string;
	
	private KnowledgeType(String string)
	{
		this.string = string;
	}
	
	public String toString()
	{
		return string;
	}
}
