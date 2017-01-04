package epmc.webserver.backend.dbms;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;

import epmc.webserver.backend.worker.task.towork.ModelTask;
import epmc.webserver.backend.worker.task.worked.completed.CompletedTask;
import epmc.webserver.backend.worker.task.worked.failed.FailedAnalysisTask;
import epmc.webserver.backend.worker.task.worked.failed.FailedInvalidTask;
import epmc.webserver.backend.worker.task.worked.failed.FailedParseTask;
import epmc.webserver.backend.worker.task.worked.failed.FailedRuntimeTask;
import epmc.webserver.backend.worker.task.worked.failed.FailedTask;
import epmc.webserver.backend.worker.task.worked.failed.FailedTimedOutTask;
import epmc.webserver.backend.worker.task.worked.intermediate.PartialTask;
import epmc.webserver.backend.worker.task.worked.intermediate.SingleFormulaTask;
import epmc.webserver.backend.worker.task.worked.system.SystemFailureTask;
import epmc.webserver.backend.worker.task.worked.system.SystemIrreversibleFailureTask;
import epmc.webserver.backend.worker.task.worked.system.SystemShutdownTask;
import epmc.webserver.common.DBMSError;
import epmc.webserver.common.EPMCDBException;

/**
 *
 * @author ori
 */
public abstract class DBMS {

	/**
	 * The connection to the database, kept active
	 */
	protected Connection connection;
	protected final String hostId;
	private final String url;
	private final String username;
	private final String password;
	
	/**
	 * The constructor just loads the mysql connector driver
	 * @param host the host name/ip address where mysql is running
	 * @param port the corresponding port
	 * @param database the database to use
	 * @param username the username for the authentication
	 * @param password the corresponding password
	 * @throws EPMCDBException in case of problems with loading the mysql jdbc connector
	 */
	protected DBMS(String host, String port, String database, String username, String password) throws EPMCDBException {
		String hid = null;
		try {
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
			while (en.hasMoreElements()) {
				NetworkInterface nic = en.nextElement();
				if (nic.isLoopback()) {
					continue;
				}
				byte[] mac = nic.getHardwareAddress();
				StringBuilder sb = new StringBuilder(String.format("%02X", mac[0]));
				for (int i = 1; i < mac.length; i++) {
					sb.append(String.format(":%02X", mac[i]));
				}
				hid = sb.toString();
				break;
			}
		} catch (SocketException ex) {
			throw new EPMCDBException(DBMSError.UnidentifiedHost);
		}
		this.hostId = hid;
		if (hid == null) {
			throw new EPMCDBException(DBMSError.UnidentifiedHost);
		}
		this.url = "jdbc:mysql://" + host + ":" + port + "/" + database;
		this.username = username;
		this.password = password;
		this.connection = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException cnfe) {
			throw new EPMCDBException(DBMSError.MissingBackend);
		}
	}
	
	/**
	 * Ensures that the connection is established and active;
	 * @throws EPMCDBException in case of problems with the database
	 */
	protected void establishConnection() throws EPMCDBException {
		boolean isValid = true;
		if (connection != null){
			Statement st = null;
			ResultSet rs = null;
			try {
				st = connection.createStatement();
				rs = st.executeQuery("select 1;");
				connection.setAutoCommit(true);
			} catch (SQLException se) {
				isValid = false;
			} finally {
				try {
					if (rs != null) {
						rs.close();
					}
				} catch (SQLException se) {}
				try {
					if (st != null) {
						st.close();
					}
				} catch (SQLException se) {}
			}
			if (isValid) {
				return;
			}
		}
		try {
			connection = DriverManager.getConnection(url, username, password);
		} catch (SQLException sqle) {
			try {
				if (connection != null) {
					connection.close();
					connection = null;
				}
			} catch (SQLException se) {}
			throw new EPMCDBException(DBMSError.ConnectionFailed, sqle);
		}
	}

	
	@Override
	protected void finalize() throws Throwable {
		if (connection != null) {
			connection.close();
		}
		super.finalize();
	}
	
	/**
	 * Extracts a task to be computed from the database
	 * @return the task to be computed
	 * @throws EPMCDBException in case of problems with the database
	 */
	public abstract ModelTask getTask() throws EPMCDBException;

	/**
	 * Inserts a task that has been successfully computed
	 * @param task the task to be stored 
	 * @throws EPMCDBException in case of problems with the database
	 */
	public abstract void putCompletedModel(CompletedTask task) throws EPMCDBException;

	/**
	 * Inserts a task that has not passed the analysis of the model checker
	 * @param task the task to be stored
	 * @throws EPMCDBException in case of problems with the database
	 */
	public abstract void putFailedAnalysisModel(FailedAnalysisTask task) throws EPMCDBException;

	/**
	 * Inserts a task that has been recognized as  invalid
	 * @param task the task to be stored
	 * @throws EPMCDBException in case of problems with the database
	 */
	public abstract void putFailedInvalidModel(FailedInvalidTask task) throws EPMCDBException;

	/**
	 * Inserts a task that has not passed the syntactic check 
	 * @param task the task to be stored
	 * @throws EPMCDBException in case of problems with the database
	 */
	public abstract void putFailedParseModel(FailedParseTask task) throws EPMCDBException;

	/**
	 * Inserts a task that has generated a runtime error in the model checker
	 * @param task the task to be stored
	 * @throws EPMCDBException in case of problems with the database
	 */
	public abstract void putFailedRuntimeModel(FailedRuntimeTask task) throws EPMCDBException;

	/**
	 * Inserts a task that has been terminated since it was taking too long to complete
	 * @param task the task to be stored
	 * @throws EPMCDBException in case of problems with the database
	 */
	public abstract void putFailedTimedOutModel(FailedTimedOutTask task) throws EPMCDBException;

	/**
	 * Inserts a partial result from a task that is still under computation
	 * @param task the task to be stored
	 * @throws EPMCDBException in case of problems with the database
	 */
	public abstract void putPartialModel(PartialTask task) throws EPMCDBException;

	/**
	 * Inserts a formula computed by a task that is still under computation
	 * @param task the task to be stored
	 * @throws EPMCDBException in case of problems with the database
	 */
	public abstract void putSingleFormulaModel(SingleFormulaTask task) throws EPMCDBException;

	/**
	 * Inserts a task that has failed due to a system failure
	 * @param task the task to be stored
	 * @throws EPMCDBException in case of problems with the database
	 */
	public abstract void putSystemFailure(SystemFailureTask task) throws EPMCDBException;

	/**
	 * Inserts a task that has failed due to a system irreversible failure, like OutOfMemoryError
	 * @param task the task to be stored
	 * @throws EPMCDBException in case of problems with the database
	 */
	public abstract void putSystemIrreversibleFailure(SystemIrreversibleFailureTask task) throws EPMCDBException;

	/**
	 * Inserts a failed task that is unknown, should never be invoked
	 * @param task the task to be stored
	 * @throws EPMCDBException in case of problems with the database
	 */
	public abstract void putUnknownTask(FailedTask task) throws EPMCDBException;

	/**
	 * Inserts a task that has been terminated since the backend has been required to shutdown
	 * @param task the task to be stored
	 * @throws EPMCDBException in case of problems with the database
	 */
	public abstract void putSystemShutdown(SystemShutdownTask task) throws EPMCDBException;

	/**
	 * Reset all tasks that are under computation at the termination the previous run of the backend without being correctly terminated
	 * @throws EPMCDBException in case of problems with the database
	 */
	public abstract void resetFormerWorkInProgress() throws EPMCDBException;
	
}
