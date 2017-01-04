package epmc.webserver.common;

/**
 * Enumeration for representing the errors generated when working with the database
 * @author ori
 */
public enum DBMSError {

	/**
	 * Used to represent the request of usage of a DMBS class that is unknown
	 *//**
	 * Used to represent the request of usage of a DMBS class that is unknown
	 */
	UnknownDBMS,

	/**
	 * Used in case the mysql jdbc driver can not be loaded
	 */
	MissingBackend,

	/**
	 * Used in case it is not possible to connect to the database
	 */
	ConnectionFailed,

	/**
	 * Used in case a database action is tried even if the connection has not been established
	 */
	NoConnection,

	/**
	 * Used in case an SQL query is failed 
	 */
	SQLFailed,

	/**
	 * Used in case the required item is not in the database
	 */
	NoSuchElement,
	
	/**
	 * Used in case it is not possible to identify the host
	 */
	UnidentifiedHost
}
