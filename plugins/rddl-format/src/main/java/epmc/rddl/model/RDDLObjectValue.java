package epmc.rddl.model;

public final class RDDLObjectValue {
	private final String name;
	private final int number;
	private RDDLObject object;
	
	RDDLObjectValue(String name, int number) {
		assert name != null;
		assert number >= 0;
		this.name = name;
		this.number = number;		
	}
	
	@Override
	public int hashCode() {
		int hash = 0;
        hash = name.hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = number + (hash << 6) + (hash << 16) - hash;
        return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		assert obj != null;
		if (!(obj instanceof RDDLObjectValue)) {
			return false;
		}
		RDDLObjectValue other = (RDDLObjectValue) obj;
		if (!this.name.equals(other.name)) {
			return false;
		}
		if (this.number != other.number) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("(");
		builder.append(this.name);
		builder.append(",");
		builder.append(this.number);
		builder.append(")");
		return builder.toString();
	}
	
	public String getName() {
		return name;
	}
	
	public int getNumber() {
		return number;
	}

	public void setObject(RDDLObject object) {
		assert object != null;
		this.object = object;
	}
	
	public RDDLObject getObject() {
		return object;
	}
}
