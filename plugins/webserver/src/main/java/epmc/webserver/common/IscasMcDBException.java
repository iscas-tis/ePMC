package epmc.webserver.common;

/**
 *
 * @author ori
 */
public class EPMCDBException extends Exception {
	private final DBMSError error;
	
	/**
	 *
	 * @param error
	 */
	public EPMCDBException(DBMSError error) {
		this.error = error;
	}
	
	/**
	 *
	 * @param error
	 * @param t
	 */
	public EPMCDBException(DBMSError error, Throwable t) {
		super(t);
		this.error = error;
	}
	
	/**
	 *
	 * @return
	 */
	public DBMSError getError() {
		return error;
	}
}
