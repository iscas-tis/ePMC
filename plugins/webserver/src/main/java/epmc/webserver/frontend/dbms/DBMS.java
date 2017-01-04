package epmc.webserver.frontend.dbms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import epmc.webserver.common.DBMSError;
import epmc.webserver.common.Formula;
import epmc.webserver.common.EPMCDBException;
import epmc.webserver.common.Pair;
import epmc.webserver.common.TaskOperation;
import epmc.webserver.common.TaskStatus;


/**
 *
 * @author ori
 */
public class DBMS {
	
	private final Connection connection;
	
	private Connection getConnection(String url, String username, String password) {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url, username, password);
		} catch (SQLException sqle) {
			try {
				if (conn != null) {
					conn.close();
					conn = null;
				}
			} catch (SQLException se) {}
		}
		return conn;
	}
	
	/**
	 *
	 * @param host
	 * @param port
	 * @param database
	 * @param username
	 * @param password
	 * @throws EPMCDBException
	 */
	public DBMS(String host, String port, String database, String username, String password) throws EPMCDBException {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException cnfe) {
			throw new EPMCDBException(DBMSError.MissingBackend);
		}
		
		String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
		
		connection = getConnection(url, username, password);
		
		if (connection == null) {
			throw new EPMCDBException(DBMSError.ConnectionFailed);
		}
	}
	
	/**
	 *
	 * @param userId
	 * @param modelId
	 * @param taskOperation
	 * @throws EPMCDBException
	 */
	public synchronized void createTask(int userId, int modelId, TaskOperation taskOperation) throws EPMCDBException {
		if (connection == null) {
			throw new EPMCDBException(DBMSError.NoConnection);
		}
		Statement st = null;
		ResultSet rs = null;
		int taskId;
		try {
			connection.setAutoCommit(false);
			st = connection.createStatement();
			st.executeUpdate("insert into task(userId, modelId, modelName, modelContent, modelComment, taskOptions, taskOperation, taskStatus ) "
					+ "select userId, modelId, modelName, modelContent, modelComment, modelOptions, '" + taskOperation.name() + "', '" + TaskStatus.pending.name() + "' "
					+ "from model "
					+ "where userId = " + userId + " and modelId = " + modelId + ";", Statement.RETURN_GENERATED_KEYS);
			rs = st.getGeneratedKeys();
			if (rs.next()) {
				taskId = rs.getInt(1);
			} else {
				throw new SQLException();
			}
			rs.close();
			rs = null;
			st.executeUpdate("insert into task_formula(taskId, formulaId, formulaContent, formulaComment) "
					+ "select " + taskId + ", formulaId, formulaContent, formulaComment "
					+ "from formula "
					+ "where modelId = " + modelId + " and formulaSelected;");
			st.close();
			connection.commit();
		} catch (SQLException sqle) {
			try {
				connection.rollback();
			} catch (SQLException se) {}
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				connection.setAutoCommit(true);
			} catch (SQLException sqle) {
			}
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException sqle) {
			}
			try {
				if (st != null) {
					st.close();
				}
			} catch (SQLException sqle) {
			}
		}
	}
	
	/**
	 *
	 * @param userId
	 * @return
	 * @throws EPMCDBException
	 */
	public synchronized List<Task> getTasks(int userId) throws EPMCDBException {
		if (connection == null) {
			throw new EPMCDBException(DBMSError.NoConnection);
		}
		List<Task> list = new ArrayList<Task>();
		Statement st = null;
		ResultSet rs = null;
		try {
			st = connection.createStatement();
			rs = st.executeQuery("select taskId, modelId, modelName, modelContent, modelComment, taskOptions, taskOperation, taskStatus, taskCreationTimestamp, taskStartElaborationTimestamp, taskCompletionTimestamp from task where userId = " + userId + ";");
			while (rs.next()) {
				Task task = new Task();
				task.setTaskId(rs.getInt("taskId"));
				task.setUserId(userId);
				task.setModelId(rs.getInt("modelId"));
				task.setModelName(rs.getString("modelName"));
				task.setModelContent(rs.getString("modelContent"));
				task.setModelComment(rs.getString("modelComment"));
				task.setTaskOptions(rs.getString("taskOptions"));
				task.setTaskOperation(TaskOperation.valueOf(rs.getString("taskOperation")));
				task.setTaskStatus(TaskStatus.valueOf(rs.getString("taskStatus")));
				task.setTaskCreationTimestamp(rs.getString("taskCreationTimestamp"));
				task.setTaskStartElaborationTimestamp(rs.getString("taskStartElaborationTimestamp"));
				task.setTaskCompletionTimestamp(rs.getString("taskCompletionTimestamp"));
				list.add(task);
			}
		} catch (SQLException sqle) {
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException sqle) {
			}
			try {
				if (st != null) {
					st.close();
				}
			} catch (SQLException sqle) {
			}
		}
		return list;
	}
	
	/**
	 *
	 * @param userId
	 * @param taskId
	 * @return
	 * @throws EPMCDBException
	 */
	public synchronized Pair<Task, List<TaskFormula>> getTask(int userId, int taskId) throws EPMCDBException {
		if (connection == null) {
			throw new EPMCDBException(DBMSError.NoConnection);
		}
		List<TaskFormula> formulae = new ArrayList<TaskFormula>();
		Task task = new Task();
		Statement st = null;
		ResultSet rs = null;
		try {
			st = connection.createStatement();
			rs = st.executeQuery("select taskId, modelId, modelName, modelContent, modelComment, taskOptions, taskOperation, taskStatus, taskCreationTimestamp, taskStartElaborationTimestamp, taskCompletionTimestamp from task where userId = " + userId + " and taskId = " + taskId + ";");
			if (rs.next()) {
				task.setTaskId(rs.getInt("taskId"));
				task.setModelId(rs.getInt("modelId"));
				task.setUserId(userId);
				task.setModelName(rs.getString("modelName"));
				task.setModelContent(rs.getString("modelContent"));
				task.setModelComment(rs.getString("modelComment"));
				task.setTaskOptions(rs.getString("taskOptions"));
				task.setTaskOperation(TaskOperation.valueOf(rs.getString("taskOperation")));
				task.setTaskStatus(TaskStatus.valueOf(rs.getString("taskStatus")));
				task.setTaskCreationTimestamp(rs.getString("taskCreationTimestamp"));
				task.setTaskStartElaborationTimestamp(rs.getString("taskStartElaborationTimestamp"));
				task.setTaskCompletionTimestamp(rs.getString("taskCompletionTimestamp"));
			} else {
				task = null;
			}
			rs.close();
			if (task != null) {
				rs = st.executeQuery("select formulaId, formulaContent, formulaComment, formulaResult from task_formula where taskId = " + taskId + ";");
				while (rs.next()) {
					formulae.add(new TaskFormula(rs.getInt("formulaId"), rs.getString("formulaContent"), rs.getString("formulaComment"), rs.getString("formulaResult")));
				}
				rs.close();
				rs = null;
			}
		} catch (SQLException sqle) {
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException sqle) {
			}
			try {
				if (st != null) {
					st.close();
				}
			} catch (SQLException sqle) {
			}
		}
		if (task != null) {
			return new Pair<Task, List<TaskFormula>>(task, formulae);
		} else {
			throw new EPMCDBException(DBMSError.NoSuchElement);
		}
	}

	/**
	 *
	 * @param userId
	 * @throws EPMCDBException
	 */
	public void deleteTasks(int userId) throws EPMCDBException {
		if (connection == null) {
			throw new EPMCDBException(DBMSError.NoConnection);
		}
		Statement st = null;
		try {
			st = connection.createStatement();
			st.executeUpdate("delete from task where userId = " + userId + ";");
			st.close();
		} catch (SQLException sqle) {
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				if (st != null) {
					st.close();
				}
			} catch (SQLException sqle) {
			}
		}
	}
	
	/**
	 *
	 * @param userId
	 * @param taskId
	 * @throws EPMCDBException
	 */
	public void deleteTask(int userId, int taskId) throws EPMCDBException {
		if (connection == null) {
			throw new EPMCDBException(DBMSError.NoConnection);
		}
		Statement st = null;
		try {
			st = connection.createStatement();
			st.executeUpdate("delete from task where userId = " + userId + " and taskId = " + taskId + ";");
			st.close();
		} catch (SQLException sqle) {
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				if (st != null) {
					st.close();
				}
			} catch (SQLException sqle) {
			}
		}
	}
	
	/**
	 *
	 * @param userId
	 * @param modelName
	 * @param modelContent
	 * @param modelComment
	 * @param modelOptions
	 * @return
	 * @throws EPMCDBException
	 */
	public synchronized int createModel(int userId, String modelName, String modelContent, String modelComment, String modelOptions) throws EPMCDBException {
		if (connection == null) {
			throw new EPMCDBException(DBMSError.NoConnection);
		}
		PreparedStatement ps = null;
		ResultSet rs = null;
		int modelId = -1;
		try {
			ps = connection.prepareStatement("insert into model (userId, modelName, modelContent, modelComment, modelOptions) values (" + userId + ", ?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, modelName);
			ps.setString(2, modelContent);
			ps.setString(3, modelComment);
			ps.setString(4, modelOptions);
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();
			if (rs.next()) {
				modelId = rs.getInt(1);
			} else {
				throw new SQLException();
			}
			rs.close();
			rs = null;
			ps.close();
		} catch (SQLException sqle) {
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException sqle) {
			}
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (SQLException sqle) {
			}
		}
		return modelId;
	}
	
	/**
	 *
	 * @param modelId
	 * @param userId
	 * @param modelName
	 * @param modelContent
	 * @param modelComment
	 * @param modelOptions
	 * @throws EPMCDBException
	 */
	public synchronized void updateModel(int modelId, int userId, String modelName, String modelContent, String modelComment, String modelOptions) throws EPMCDBException {
		if (connection == null) {
			throw new EPMCDBException(DBMSError.NoConnection);
		}
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement("update model set modelName = ?, modelContent = ?, modelComment = ?, modelOptions = ?, modelUpdateTimestamp = now() where modelId = " + modelId + " and userId = " + userId + ";");
			ps.setString(1, modelName);
			ps.setString(2, modelContent);
			ps.setString(3, modelComment);
			ps.setString(4, modelOptions);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException sqle) {
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (SQLException sqle) {
			}
		}
	}
	
	/**
	 *
	 * @param userId
	 * @return
	 * @throws EPMCDBException
	 */
	public synchronized List<Model> getModels(int userId) throws EPMCDBException {
		if (connection == null) {
			throw new EPMCDBException(DBMSError.NoConnection);
		}
		List<Model> list = new ArrayList<Model>();
		Statement st = null;
		ResultSet rs = null;
		try {
			st = connection.createStatement();
			rs = st.executeQuery("select modelId, modelName, modelContent, modelComment, modelOptions, modelCreationTimestamp, modelUpdateTimestamp from model where userId = " + userId + ";");
			while (rs.next()) {
				Model model = new Model();
				model.setUserId(userId);
				model.setModelId(rs.getInt("modelId"));
				model.setModelName(rs.getString("modelName"));
				model.setModelContent(rs.getString("modelContent"));
				model.setModelComment(rs.getString("modelComment"));
				model.setModelOptions(rs.getString("modelOptions"));
				model.setModelCreationTimestamp(rs.getString("modelCreationTimestamp"));
				model.setModelUpdateTimestamp(rs.getString("modelUpdateTimestamp"));
				list.add(model);
			}
		} catch (SQLException sqle) {
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException sqle) {
			}
			try {
				if (st != null) {
					st.close();
				}
			} catch (SQLException sqle) {
			}
		}
		return list;
	}
	
	/**
	 *
	 * @param userId
	 * @param modelId
	 * @return
	 * @throws EPMCDBException
	 */
	public synchronized Model getModel(int userId, int modelId) throws EPMCDBException {
		if (connection == null) {
			throw new EPMCDBException(DBMSError.NoConnection);
		}
		Statement st = null;
		ResultSet rs = null;
		Model model = null;
		try {
			st = connection.createStatement();
			rs = st.executeQuery("select modelId, modelName, modelContent, modelComment, modelOptions, modelCreationTimestamp, modelUpdateTimestamp from model where userId = " + userId + ";");
			if (rs.next()) {
				model = new Model();
				model.setUserId(userId);
				model.setModelId(rs.getInt("modelId"));
				model.setModelName(rs.getString("modelName"));
				model.setModelContent(rs.getString("modelContent"));
				model.setModelComment(rs.getString("modelComment"));
				model.setModelOptions(rs.getString("modelOptions"));
				model.setModelCreationTimestamp(rs.getString("modelCreationTimestamp"));
				model.setModelUpdateTimestamp(rs.getString("modelUpdateTimestamp"));
			}
		} catch (SQLException sqle) {
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException sqle) {
			}
			try {
				if (st != null) {
					st.close();
				}
			} catch (SQLException sqle) {
			}
		}
		return model;
	}
	
	/**
	 *
	 * @param userId
	 * @throws EPMCDBException
	 */
	public void deleteModels(int userId) throws EPMCDBException {
		if (connection == null) {
			throw new EPMCDBException(DBMSError.NoConnection);
		}
		Statement st = null;
		try {
			st = connection.createStatement();
			st.executeUpdate("delete from model where userId = " + userId + ";");
			st.close();
		} catch (SQLException sqle) {
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				if (st != null) {
					st.close();
				}
			} catch (SQLException sqle) {
			}
		}
	}
	
	/**
	 *
	 * @param userId
	 * @param modelId
	 * @throws EPMCDBException
	 */
	public void deleteModel(int userId, int modelId) throws EPMCDBException {
		if (connection == null) {
			throw new EPMCDBException(DBMSError.NoConnection);
		}
		Statement st = null;
		try {
			st = connection.createStatement();
			st.executeUpdate("delete from model where userId = " + userId + " and modelId = " + modelId + ";");
			st.close();
		} catch (SQLException sqle) {
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				if (st != null) {
					st.close();
				}
			} catch (SQLException sqle) {
			}
		}
	}
	
	/**
	 *
	 * @param modelId
	 * @param formulae
	 * @throws EPMCDBException
	 */
	public synchronized void createFormulas (int modelId, List<Formula> formulae) throws EPMCDBException {
		if (connection == null) {
			throw new EPMCDBException(DBMSError.NoConnection);
		}
		PreparedStatement ps = null;
		try {
			connection.setAutoCommit(false);
			ps = connection.prepareStatement("insert into formula (modelId, formulaSelected, formulaContent, formulaComment) values (" + modelId + ", ?, ?, ?);");
			for (Formula f : formulae) {
				ps.setBoolean(1, f.isSelected());
				ps.setString(2, f.getFormula());
				ps.setString(3, f.getComment());
				ps.executeUpdate();
			}
			ps.close();
			connection.commit();
		} catch (SQLException sqle) {
			try {
				connection.rollback();
			} catch (SQLException se) {}
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				connection.setAutoCommit(true);
			} catch (SQLException sqle) {
			}
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (SQLException sqle) {
			}
		}
	}
	
	/**
	 *
	 * @param modelId
	 * @param formula
	 * @throws EPMCDBException
	 */
	public synchronized void createFormula (int modelId, Formula formula) throws EPMCDBException {
		if (connection == null) {
			throw new EPMCDBException(DBMSError.NoConnection);
		}
		PreparedStatement ps = null;
		try {
			connection.setAutoCommit(false);
			ps = connection.prepareStatement("insert into formula (modelId, formulaSelected, formulaContent, formulaComment) values (" + modelId + ", ?, ?, ?);");
			ps.setBoolean(1, formula.isSelected());
			ps.setString(2, formula.getFormula());
			ps.setString(3, formula.getComment());
			ps.executeUpdate();
			ps.close();
			connection.commit();
		} catch (SQLException sqle) {
			try {
				connection.rollback();
			} catch (SQLException se) {}
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				connection.setAutoCommit(true);
			} catch (SQLException sqle) {
			}
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (SQLException sqle) {
			}
		}
	}
	
	/**
	 *
	 * @param modelId
	 * @param formulae
	 * @throws EPMCDBException
	 */
	public synchronized void updateFormulas (int modelId, List<Formula> formulae) throws EPMCDBException {
		if (connection == null) {
			throw new EPMCDBException(DBMSError.NoConnection);
		}
		PreparedStatement ps = null;
		try {
			connection.setAutoCommit(false);
			ps = connection.prepareStatement("update formula set formulaSelected = ?, formulaContent = ?, formulaComment = ?, formulaUpdateTimestamp = now() where modelId = " + modelId + " and formulaId = ?;");
			for (Formula f : formulae) {
				ps.setBoolean(1, f.isSelected());
				ps.setString(2, f.getFormula());
				ps.setString(3, f.getComment());
				ps.setInt(4, f.getId());
				ps.executeUpdate();
			}
			connection.commit();
			ps.close();
		} catch (SQLException sqle) {
			try {
				connection.rollback();
			} catch (SQLException se) {}
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				connection.setAutoCommit(true);
			} catch (SQLException sqle) {
			}
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (SQLException sqle) {
			}
		}
	}
	
	/**
	 *
	 * @param modelId
	 * @param formula
	 * @throws EPMCDBException
	 */
	public synchronized void updateFormula (int modelId, Formula formula) throws EPMCDBException {
		if (connection == null) {
			throw new EPMCDBException(DBMSError.NoConnection);
		}
		PreparedStatement ps = null;
		try {
			connection.setAutoCommit(false);
			ps = connection.prepareStatement("update formula set formulaSelected = ?, formulaContent = ?, formulaComment = ?, formulaUpdateTimestamp = now() where modelId = " + modelId + " and formulaId = ?;");
			ps.setBoolean(1, formula.isSelected());
			ps.setString(2, formula.getFormula());
			ps.setString(3, formula.getComment());
			ps.setInt(4, formula.getId());
			ps.executeUpdate();
			connection.commit();
			ps.close();
		} catch (SQLException sqle) {
			try {
				connection.rollback();
			} catch (SQLException se) {}
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				connection.setAutoCommit(true);
			} catch (SQLException sqle) {
			}
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (SQLException sqle) {
			}
		}
	}
	
	/**
	 *
	 * @param modelId
	 * @return
	 * @throws EPMCDBException
	 */
	public synchronized List<Formula> getFormulas (int modelId) throws EPMCDBException {
		if (connection == null) {
			throw new EPMCDBException(DBMSError.NoConnection);
		}
		Statement st = null;
		ResultSet rs = null;
		List<Formula> formulae = new ArrayList<Formula>();
		try {
			st = connection.createStatement();
			rs = st.executeQuery("select formulaId, formulaSelected, formulaContent, formulaComment, formulaCreationTimestamp, formulaUpdateTimestamp from formula where modelId = " + modelId + ";");
			while (rs.next()) {
				Formula f = new Formula(rs.getInt("formulaID"), rs.getString("formulaContent"), rs.getString("formulaComment"), rs.getBoolean("formulaSelected"));
				f.setCreationTimestamp(rs.getString("formulaCreationTimestamp"));
				f.setUpdateTimestamp(rs.getString("formulaUpdateTimestamp"));
				formulae.add(f);
			}
			rs.close();
			rs = null;
		} catch (SQLException sqle) {
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException sqle) {
			}
			try {
				if (st != null) {
					st.close();
				}
			} catch (SQLException sqle) {
			}
		}
		return formulae;
	}
	
	/**
	 *
	 * @param modelId
	 * @throws EPMCDBException
	 */
	public void deleteFormulas(int modelId) throws EPMCDBException {
		if (connection == null) {
			throw new EPMCDBException(DBMSError.NoConnection);
		}
		Statement st = null;
		try {
			st = connection.createStatement();
			st.executeUpdate("delete from formula where modelId = " + modelId + ";");
			st.close();
		} catch (SQLException sqle) {
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				if (st != null) {
					st.close();
				}
			} catch (SQLException sqle) {
			}
		}
	}
	
	/**
	 *
	 * @param modelId
	 * @param formulaId
	 * @throws EPMCDBException
	 */
	public void deleteFormula(int modelId, int formulaId) throws EPMCDBException {
		if (connection == null) {
			throw new EPMCDBException(DBMSError.NoConnection);
		}
		Statement st = null;
		try {
			st = connection.createStatement();
			st.executeUpdate("delete from formula where formulaId = " + formulaId + " and modelId = " + modelId + ";");
			st.close();
		} catch (SQLException sqle) {
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				if (st != null) {
					st.close();
				}
			} catch (SQLException sqle) {
			}
		}
	}
	
	/**
	 *
	 * @param username
	 * @param password
	 * @param email
	 * @param firstname
	 * @param lastname
	 * @throws EPMCDBException
	 */
	public void createUser(String username, String password, String email, String firstname, String lastname) throws EPMCDBException {
		if (connection == null) {
			throw new EPMCDBException(DBMSError.NoConnection);
		}
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement("insert into user (userUsername, userPassword, userEmail, userFirstname, userLastname) values (?, password(?), ? ? ?);");
			ps.setString(1, username);
			ps.setString(2, password);
			ps.setString(3, email);
			ps.setString(4, firstname);
			ps.setString(5, lastname);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException sqle) {
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (SQLException sqle) {
			}
		}
	}
	
	/**
	 *
	 * @param username
	 * @param password
	 * @return
	 * @throws EPMCDBException
	 */
	public int checkUserLogin(String username, String password) throws EPMCDBException {
		if (connection == null) {
			throw new EPMCDBException(DBMSError.NoConnection);
		}
		PreparedStatement ps = null;
		ResultSet rs = null;
		int userId = -1;
		try {
			ps = connection.prepareStatement("select userId from user where userUsername = ? and userPassword = password(?);");
			ps.setString(1, username);
			ps.setString(2, password);
			rs = ps.executeQuery();
			if (rs.next()) {
				userId = rs.getInt("userId");
			}
		} catch (SQLException sqle) {
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException sqle) {
			}
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (SQLException sqle) {
			}
		}
		if (userId < 0) {
			throw new EPMCDBException(DBMSError.NoSuchElement);
		}
		return userId;
	}
	
	/**
	 *
	 * @param userId
	 * @param modelOptions
	 * @throws EPMCDBException
	 */
	public void updateUserModelOptions(int userId, String modelOptions) throws EPMCDBException {
		if (connection == null) {
			throw new EPMCDBException(DBMSError.NoConnection);
		}
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement("update user set userModelOptions = ?, userUpdateTimestamp = now() where userId = " + userId + ";");
			ps.setString(1, modelOptions);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException sqle) {
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (SQLException sqle) {
			}
		}
	}
	
	/**
	 *
	 * @param userId
	 * @param firstname
	 * @param lastname
	 * @throws EPMCDBException
	 */
	public void updateUserRealName(int userId, String firstname, String lastname) throws EPMCDBException {
		if (connection == null) {
			throw new EPMCDBException(DBMSError.NoConnection);
		}
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement("update user set userFirstname = ?, userLastname = ?, userUpdateTimestamp = now() where userId = " + userId + ";");
			ps.setString(1, firstname);
			ps.setString(2, lastname);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException sqle) {
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (SQLException sqle) {
			}
		}
	}
	
	/**
	 *
	 * @param userId
	 * @param oldPassword
	 * @param newPassword
	 * @throws EPMCDBException
	 */
	public void userUpdatePassword(int userId, String oldPassword, String newPassword) throws EPMCDBException {
		if (connection == null) {
			throw new EPMCDBException(DBMSError.NoConnection);
		}
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement("update user set userPassword = password(?), userUpdateTimestamp = now() where userPassword = password(?) and userId = " + userId + ";");
			ps.setString(1, newPassword);
			ps.setString(2, oldPassword);
			ps.executeUpdate();
			ps.close();
		} catch (SQLException sqle) {
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
			} catch (SQLException sqle) {
			}
		}
	}

	/**
	 *
	 * @param userId
	 * @throws EPMCDBException
	 */
	public void deleteUser(int userId) throws EPMCDBException {
		if (connection == null) {
			throw new EPMCDBException(DBMSError.NoConnection);
		}
		Statement st = null;
		try {
			st = connection.createStatement();
			st.executeUpdate("delete from user where userId = " + userId + ";");
		} catch (SQLException sqle) {
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				if (st != null) {
					st.close();
				}
			} catch (SQLException sqle) {
			}
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		if (connection != null) {
			connection.close();
		}
		super.finalize();
	}
}
