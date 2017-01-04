package epmc.webserver.frontend.webtomodelchecker;

/**
 * Contains the information about the result of the syntax check
 * @author ori
 */
public class ParseResult {

	/**
	 * Whether the syntax check has been passed.
	 */
	public final boolean passed;

	/**
	 * The key of the message relative to the error arose during the syntax check.
	 */
	public final String key;

	/**
	 * The line where the error occurred.
	 */
	public final long line;

	/**
	 * The column where the error occurred.
	 */
	public final long column;

	/**
	 * The (possibly null) identifier/keyword where the error occurred.
	 */
	public final String identifier;

	/**
	 * The localized message constructed from the other fields.
	 */
	public final String localizedMessage;
	
	/**
	 * A new ParseResult corresponding to a passed syntax check.
	 */
	public ParseResult() {
		passed = true;
		key = null;
		line =  0;
		column = 0;
		identifier = null;
		localizedMessage = null;
	}
	
	/**
	 * A new ParseResult corresponding to a failed syntax check but where the available information is limited.
	 * @param localizedMessage the localized message for this syntax error
	 * @param key the key of the message for this syntax error
	 */
	public ParseResult(String localizedMessage, String key) {
		this.passed = false;
		this.key = key;
		this.line = 0;
		this.column = 0;
		this.identifier = null;
		this.localizedMessage = localizedMessage;
	}
	
	/**
	 * A new ParseResult corresponding to a failed syntax check where the available information is complete.
	 * @param localizedMessage the localized message for this syntax error
	 * @param key the key of the message for this syntax error
	 * @param identifier the (possibly null) identifier/keyword where the error occurred
	 * @param line the line where the error occurred
	 * @param column the column where the error occurred
	 */
	public ParseResult(String localizedMessage, String key, String identifier, long line, long column) {
		this.passed = false;
		this.key = key;
		this.line = line;
		this.column = column;
		this.identifier = identifier;
		this.localizedMessage = localizedMessage;
	}
	
	@Override
	public String toString() {
		if (passed) {
			return "Parsing passed";
		} else {
			return localizedMessage;
		}
	}
}
