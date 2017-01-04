package epmc.webserver.common;

/**
 * Object representing a formula
 * @author ori
 */
public class Formula {
	private final int id;
	private final String formula;
	private final String comment;
	private final boolean selected;
	private String creationTimestamp = null;
	private String updateTimestamp = null;
	
	/**
	 *
	 * @param id
	 * @param formula
	 * @param comment
	 */
	public Formula(int id, String formula, String comment) {
		this.id = id;
		this.formula = formula;
		this.comment = comment;
		this.selected = true;
	}
	
	/**
	 *
	 * @param id
	 * @param formula
	 * @param comment
	 * @param selected
	 */
	public Formula(int id, String formula, String comment, boolean selected) {
		this.id = id;
		this.formula = formula;
		this.comment = comment;
		this.selected = selected;
	}
	
	/**
	 *
	 * @return
	 */
	public int getId() {
		return id;
	}
	
	/**
	 *
	 * @return
	 */
	public String getFormula() {
		return formula;
	}
	
	/**
	 *
	 * @return
	 */
	public String getComment() {
		return comment;
	}
	
	/**
	 *
	 * @return
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 *
	 * @return
	 */
	public String getCreationTimestamp() {
		return creationTimestamp;
	}
	
	/**
	 *
	 * @param creationTimestamp
	 */
	public void setCreationTimestamp(String creationTimestamp) {
		this.creationTimestamp = creationTimestamp;
	}
	
	/**
	 *
	 * @return
	 */
	public String getUpdateTimestamp() {
		return updateTimestamp;
	}
	
	/**
	 *
	 * @param updateTimestamp
	 */
	public void setUpdateTimestamp(String updateTimestamp) {
		this.updateTimestamp = updateTimestamp;
	}
	
}
