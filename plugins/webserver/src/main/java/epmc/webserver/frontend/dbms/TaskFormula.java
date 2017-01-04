package epmc.webserver.frontend.dbms;

/**
 *
 * @author ori
 */
public class TaskFormula {
	private final int id;
	private final String formula;
	private final String comment;
	private final String result;
	
	/**
	 *
	 * @param id
	 * @param formula
	 * @param comment
	 * @param result
	 */
	public TaskFormula(int id, String formula, String comment, String result) {
		this.id = id;
		this.formula = formula;
		this.comment = comment;
		this.result = result;
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
	public String getResult() {
		return result;
	}
}
