package epmc.webserver.backend.dbms;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import epmc.webserver.backend.BackendEngine;
import epmc.webserver.backend.worker.task.towork.BuildModelTask;
import epmc.webserver.backend.worker.task.towork.CheckModelTask;
import epmc.webserver.backend.worker.task.towork.InvalidModelTask;
import epmc.webserver.backend.worker.task.towork.ModelTask;
import epmc.webserver.backend.worker.task.worked.completed.CompletedCheckModelTask;
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
import epmc.webserver.common.Formula;
import epmc.webserver.common.EPMCDBException;
import epmc.webserver.common.Pair;
import epmc.webserver.common.TaskOperation;
import epmc.webserver.common.TaskStatus;


/**
 *
 * @author ori
 */
public class DBMS_LiYi extends DBMS {
	
	/**
	 * The constructor just loads the mysql connector driver by calling the constructor of the superclass
	 * @param host the host name/ip address where mysql is running
	 * @param port the corresponding port
	 * @param database the database to use
	 * @param username the username for the authentication
	 * @param password the corresponding password
	 * @throws EPMCDBException in case of problems with the database in case of problems with loading the mysql jdbc connector
	 */
	public DBMS_LiYi(String host, String port, String database, String username, String password) throws EPMCDBException {
		super(host, port, database, username, password);
	}
	
	@Override
	public synchronized void resetFormerWorkInProgress() throws EPMCDBException {
		establishConnection();

		Statement st = null;
		try {
			connection.setAutoCommit(false);
			st = connection.createStatement();
			st.executeUpdate("update task set start_elaboration_time = null, status = '" + TaskStatus.pending.name() + "' where status = '" + TaskStatus.computing.name() +"' and id in (select taskid from task_worker where host = '" + hostId + "');");
			st.executeUpdate("delete from task_worker where host = '" + hostId + "';");
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
			} finally {
				try {
					if (st != null) {
						st.close();
					}
				} catch (SQLException se) {}
			}
		}
		System.out.println("Reset all former works in progress");
	}
	
	@Override
	public synchronized ModelTask getTask() throws EPMCDBException {
		establishConnection();

		ModelTask task = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			connection.setAutoCommit(false);
			st = connection.createStatement();
//			rs = st.executeQuery("select task.id as id, user.id as userId, model_content, modelname, model_options, model_type, type, status from task inner join user on task.username = user.username where status = '" + TaskStatus.pending.name() + "' order by user.authority asc, task.id desc;");
			rs = st.executeQuery("select task.id as id, user.id as userId, model_content, modelname, model_options, model_type, type, status from task inner join user on task.username = user.username where status = '" + TaskStatus.pending.name() + "';");
			if (rs.next()) {
				int taskId = rs.getInt("id");
				int userId = rs.getInt("userId");
				String modelContent = rs.getString("model_content");
				String taskOptions = rs.getString("model_options");
				String taskOriginalOperation = rs.getString("type");
				String modelType = rs.getString("model_type");
				TaskOperation taskOperation;
				try {
					taskOperation = TaskOperation.valueOf(taskOriginalOperation);
				} catch (IllegalArgumentException iae) {
					taskOperation = TaskOperation.invalidOperation;
				}
				rs.close();
				switch (taskOperation) {
					case build:
						task = new BuildModelTask(userId, taskId, modelType, modelContent, taskOptions);
						break;
					case checkFormula:
					case analyze:
						rs = st.executeQuery("select formulaid, formula_content, formula_comment from task_formula where taskid = " + taskId + ";");
						List<Formula> formulae = new ArrayList<Formula>();
						while (rs.next()) {
							String content = rs.getString("formula_content");
							if (content == null) {
								content = "";
							}
							formulae.add(new Formula(rs.getInt("formulaid"), content, rs.getString("formula_comment")));
						}
						task = new CheckModelTask(userId, taskId, modelType, modelContent, formulae, BackendEngine.timeOutInMinutes(), taskOptions);
						break;
					default:
						task = new InvalidModelTask(userId, taskId, taskOriginalOperation);
						break;
				}
				if (task instanceof InvalidModelTask) {
					st.executeUpdate("update task set status = '" + TaskStatus.failedInvalidOptions.name() + "', start_elaboration_time = now(), termination_time = now() where id = " + taskId + ";");
				} else {
					st.executeUpdate("insert into task_worker(taskid, host) values (" + taskId + ", '" + hostId + "');");
					st.executeUpdate("update task set status = '" + TaskStatus.computing.name() + "', start_elaboration_time = now() where id = " + taskId + ";");
				}
			}
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
		}
		return task;
	}
	
	@Override
	public synchronized void putCompletedModel(CompletedTask task) throws EPMCDBException {
		establishConnection();

		Statement st = null;
		PreparedStatement ps = null;
		try {
			connection.setAutoCommit(false);
			switch (task.getOperation()) {
				case checkFormula:
					ps = connection.prepareStatement("update task_formula set formula_result = ? where taskid = " + task.getTaskId() + " and formulaid = ?;");
					for (Pair<Integer, String> pair : ((CompletedCheckModelTask)task).getResults()) {
						ps.setString(1, "<pre style=\"font-family:monospace\">" + pair.snd + "</pre>");
						ps.setInt(2, pair.fst);
						ps.executeUpdate();
					}
					ps.close();
					break;
				case build:
					// nothing to do...
					break;
			}
			st = connection.createStatement();
			st.executeUpdate("update task set status = '" + TaskStatus.completed.name() + "', termination_time = now() where id = " + task.getTaskId() +";");
			st.executeUpdate("delete from task_worker where taskid=" + task.getTaskId() + " and host = '" + hostId + "';");
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
			} finally {
				try {
					if (ps != null) {
						ps.close();
					}
				} catch (SQLException se) {}
				try {
					if (st != null) {
						st.close();
					}
				} catch (SQLException se) {}
			}
		}
		System.out.println("Completed " + task.toString());
	}
	
	@Override
	public synchronized void putFailedAnalysisModel(FailedAnalysisTask task) throws EPMCDBException {
		establishConnection();

		Statement st = null;
		PreparedStatement ps = null;
		try {
			connection.setAutoCommit(false);
			ps = connection.prepareStatement("insert into error(taskid, errkey, errarguments, timemark) values (" + task.getTaskId() + ", ?, ?, now());");
			ps.setString(1, task.getErrorKey());
			ps.setString(2, task.getErrorArgument());
			ps.executeUpdate();
			st = connection.createStatement();
			st.executeUpdate("update task set status = '" + TaskStatus.failedCheck.name() + "' where id = " + task.getTaskId() +";");
			st.executeUpdate("delete from task_worker where taskid=" + task.getTaskId() + " and host = '" + hostId + "';");
			connection.commit();
		} catch (SQLException sqle) {
			try {
				connection.rollback();
			} catch (SQLException e) {}
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				connection.setAutoCommit(true);
			} catch (SQLException sqle) {
			} finally {
				try {
					if (st != null) {
						st.close();
					}
				} catch (SQLException se) {}
				try {
					if (ps != null) {
						ps.close();
					}
				} catch (SQLException se) {}
			}
		}
		System.out.println("Completed but failed " + task.toString());
	}
	
	@Override
	public synchronized void putFailedParseModel(FailedParseTask task) throws EPMCDBException {
		establishConnection();

		Statement st = null;
		PreparedStatement ps = null;
		try {
			connection.setAutoCommit(false);
			ps = connection.prepareStatement("insert into error(taskid, errkey, errarguments, timemark) values (" + task.getTaskId() +", ?, ?, now());");
			ps.setString(1, task.getError());
			String identifier = task.getIdentifier();
			if (identifier == null) {
				ps.setString(2, "0 = " + task.getLine() + "\n1 = " + task.getColumn());
			} else {
				ps.setString(2, "0 = " + task.getLine() + "\n1 = " + task.getColumn() + "\n2 = " + identifier);
			}
			ps.executeUpdate();
			st = connection.createStatement();
			st.executeUpdate("update task set status = '" + TaskStatus.failedParse.name() + "', termination_time = now() where id = " + task.getTaskId() +";");
			st.executeUpdate("delete from task_worker where taskid=" + task.getTaskId() + " and host = '" + hostId + "';");
			connection.commit();
		} catch (SQLException sqle) {
			try {
				connection.rollback();
			} catch (SQLException e) {}
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				connection.setAutoCommit(true);
			} catch (SQLException sqle) {
			} finally {
				try {
					if (st != null) {
						st.close();
					}
				} catch (SQLException se) {}
				try {
					if (ps != null) {
						ps.close();
					}
				} catch (SQLException se) {}
			}
		}
		System.out.println("Completed but failed parsing " + task.toString());
	}
	
	@Override
	public synchronized void putFailedTimedOutModel(FailedTimedOutTask task) throws EPMCDBException {
		establishConnection();

		Statement st = null;
		PreparedStatement ps = null;
		try {
			connection.setAutoCommit(false);
			ps = connection.prepareStatement("insert into error(taskid, errkey, errarguments, timemark) values (" + task.getTaskId() +", ?, ?, now());");
			ps.setString(1, task.getError());
			ps.setString(2, "0 = " + task.getTimeOut());
			st = connection.createStatement();
			st.executeUpdate("update task set status = '" + TaskStatus.failedTimedOut.name() + "', termination_time = now() where id = " + task.getTaskId() +";");
			st.executeUpdate("delete from task_worker where taskid=" + task.getTaskId() + " and host = '" + hostId + "';");
			connection.commit();
		} catch (SQLException sqle) {
			try {
				connection.rollback();
			} catch (SQLException e) {}
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				connection.setAutoCommit(true);
			} catch (SQLException sqle) {
			} finally {
				try {
					if (st != null) {
						st.close();
					}
				} catch (SQLException se) {}
				try {
					if (ps != null) {
						ps.close();
					}
				} catch (SQLException se) {}
			}
		}
		System.out.println("Timed out while checking " + task.toString());
	}
	
	@Override
	public synchronized void putSystemFailure(SystemFailureTask task) throws EPMCDBException {
		establishConnection();

		int taskId = task.getTaskId();
		//system failure, revert status to the initial one;
		Statement st = null;
		PreparedStatement ps = null;
		try {
			connection.setAutoCommit(false);
			ps = connection.prepareStatement("insert into system_error(error, taskId, timemark) values (?, ?, now());");
			ps.setString(1, task.toString());
			ps.setInt(2, taskId);
			ps.executeUpdate();
			st = connection.createStatement();
			st.executeUpdate("update task set status = '" + TaskStatus.pending.name() + "', start_elaboration_time = null where id = " + taskId +";");
			st.executeUpdate("delete from task_worker where taskid=" + task.getTaskId() + " and host = '" + hostId + "';");
			connection.commit();
		} catch (SQLException sqle) {
			try {
				connection.rollback();
			} catch (SQLException e) {}
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				connection.setAutoCommit(true);
			} catch (SQLException sqle) {
			} finally {
				try {
					if (st != null) {
						st.close();
					}
				} catch (SQLException se) {}
				try {
					if (ps != null) {
						ps.close();
					}
				} catch (SQLException se) {}
			}
		}
		System.out.println("SystemFailure: " + task.toString());
	}

	@Override
	public synchronized void putSystemIrreversibleFailure(SystemIrreversibleFailureTask task) throws EPMCDBException {
		establishConnection();

		int taskId = task.getTaskId();
		//system failure, revert status to the initial one;
		Statement st = null;
		PreparedStatement ps = null;
		try {
			connection.setAutoCommit(false);
			ps = connection.prepareStatement("insert into system_error_task(taskId, modelContent, modelOptions, type) select id, model_content, model_options, type from task where id = ?;");
			ps.setInt(1, taskId);
			ps.executeUpdate();
			ps.close();
			ps = connection.prepareStatement("insert into system_error_task_formula (formulaId, formulaContent, taskId) select formulaid, formula_content, taskid from task_formula where taskid = ?;");
			ps.setInt(1, taskId);
			ps.executeUpdate();
			ps.close();
			ps = connection.prepareStatement("insert into system_error(error, taskId, timemark) values (?, ?, now());");
			ps.setString(1, task.toString());
			ps.setInt(2, taskId);
			ps.executeUpdate();
			st = connection.createStatement();
			st.executeUpdate("update task set status = '" + task.getStatus().name() + "', termination_time = now() where id = " + taskId +";");
			st.executeUpdate("delete from task_worker where taskid=" + task.getTaskId() + " and host = '" + hostId + "';");
			connection.commit();
		} catch (SQLException sqle) {
			try {
				connection.rollback();
			} catch (SQLException e) {}
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				connection.setAutoCommit(true);
			} catch (SQLException sqle) {
			} finally {
				try {
					if (st != null) {
						st.close();
					}
				} catch (SQLException se) {}
				try {
					if (ps != null) {
						ps.close();
					}
				} catch (SQLException se) {}
			}
		}
		System.out.println("SystemIrreversibleFailure: " + task.toString());
	}

	@Override
	public synchronized void putUnknownTask(FailedTask task) throws EPMCDBException {
		establishConnection();

		int taskId = task.getTaskId();
		//system failure, revert status to the initial one;
		Statement st = null;
		PreparedStatement ps = null;
		try {
			connection.setAutoCommit(false);
			ps = connection.prepareStatement("insert into system_error_task(taskId, modelContent, modelOptions, type) select id, model_content, model_options, type from task where id = ?;");
			ps.setInt(1, taskId);
			ps.executeUpdate();
			ps.close();
			ps = connection.prepareStatement("insert into system_error_task_formula (formulaId, formulaContent, taskId) select formulaid, formula_content, taskid from task_formula where taskid = ?;");
			ps.setInt(1, taskId);
			ps.executeUpdate();
			ps.close();
			ps = connection.prepareStatement("insert into system_error(error, taskId, timemark) values (?, ?, now());");
			ps.setString(1, task.toString());
			ps.setInt(2, taskId);
			ps.executeUpdate();
			st = connection.createStatement();
			st.executeUpdate("update task set status = '" + TaskStatus.unknownFailed.name() + "', start_elaboration_time = null where id = " + task.getTaskId() +";");
			st.executeUpdate("delete from task_worker where taskid=" + task.getTaskId() + " and host = '" + hostId + "';");
			connection.commit();
		} catch (SQLException sqle) {
			try {
				connection.rollback();
			} catch (SQLException e) {}
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				connection.setAutoCommit(true);
			} catch (SQLException sqle) {
			} finally {
				try {
					if (st != null) {
						st.close();
					}
				} catch (SQLException se) {}
				try {
					if (ps != null) {
						ps.close();
					}
				} catch (SQLException se) {}
			}
		}
		System.out.println("UnknownTask: " + task.toString());
	}

	@Override
	public synchronized void putPartialModel(PartialTask task) throws EPMCDBException {
		establishConnection();

		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement("insert into checker_message(taskid, content) values (" + task.getTaskId() + ", ?);");
//			ps = connection.prepareStatement("update task set checker_messages = concat (checker_messages, '\n', ?) where id = " + task.getTaskId() + ";");
			ps.setString(1, task.getPartialStatus());
			ps.executeUpdate();
		} catch (SQLException sqle) {
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				} catch (SQLException se) {}
		}
		System.out.println("Partial result: " + task.toString());
	}

	@Override
	public synchronized void putSystemShutdown(SystemShutdownTask task) throws EPMCDBException {
		establishConnection();
		
		//system shutdown, revert status to the initial one;
		Statement st = null;
		try {
			connection.setAutoCommit(false);
			st = connection.createStatement();
			st.executeUpdate("update task set status = '" + TaskStatus.pending.name() + "', start_elaboration_time = null where id = " + task.getTaskId() +";");
			st.executeUpdate("delete from task_worker where taskid=" + task.getTaskId() + " and host = '" + hostId + "';");
			connection.commit();
		} catch (SQLException sqle) {
			try {
				connection.rollback();
			} catch (SQLException e) {}
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				connection.setAutoCommit(true);
			} catch (SQLException sqle) {
			} finally {
				try {
					if (st != null) {
						st.close();
					}
				} catch (SQLException se) {}
			}
		}
		System.out.println("SystemShutdown: " + task.toString());
	}

	@Override
	public synchronized void putFailedRuntimeModel(FailedRuntimeTask task) throws EPMCDBException {
		establishConnection();

		int taskId = task.getTaskId();
		//system failure, revert status to the initial one;
		Statement st = null;
		PreparedStatement ps = null;
		try {
			connection.setAutoCommit(false);
			ps = connection.prepareStatement("insert into system_error(error, taskId, timemark) values (?, ?, now());");
			ps.setString(1, task.getStackTrace());
			ps.setInt(2, taskId);
			ps.executeUpdate();
			ps.close();
			ps = connection.prepareStatement("insert into error(taskid, errkey, errarguments, timemark) values (" + taskId + ",? , '', now());");
			ps.setString(1, task.getError());
			ps.executeUpdate();
			st = connection.createStatement();
			st.executeUpdate("update task set status = '" + TaskStatus.failedCheck.name() + "', termination_time = now() where id = " + taskId +";");
			st.executeUpdate("delete from task_worker where taskid=" + task.getTaskId() + " and host = '" + hostId + "';");
			connection.commit();
		} catch (SQLException sqle) {
			try {
				connection.rollback();
			} catch (SQLException e) {}
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				connection.setAutoCommit(true);
			} catch (SQLException sqle) {
			} finally {
				try {
					if (st != null) {
						st.close();
					}
				} catch (SQLException se) {}
				try {
					if (ps != null) {
						ps.close();
					}
				} catch (SQLException se) {}
			}
		}
		System.err.println("EPMC Runtime error: " + task.toString());
	}

	@Override
	public void putFailedInvalidModel(FailedInvalidTask task) throws EPMCDBException {
		establishConnection();

		int taskId = task.getTaskId();
		//system failure, revert status to the initial one;
		Statement st = null;
		PreparedStatement ps = null;
		try {
			connection.setAutoCommit(false);
			ps = connection.prepareStatement("insert into system_error(error, taskId, timemark) values (?, ?, now());");
			ps.setString(1, task.toString());
			ps.setInt(2, taskId);
			ps.executeUpdate();
			ps.close();
			ps = connection.prepareStatement("insert into error(taskid, errkey, errarguments, timemark) values (" + taskId + ",? , '', now());");
			ps.setString(1, task.getError());
			ps.executeUpdate();
			st = connection.createStatement();
			st.executeUpdate("update task set status = '" + TaskStatus.failedInternalError.name() + "', termination_time = now() where id = " + taskId +";");
			st.executeUpdate("delete from task_worker where taskid=" + task.getTaskId() + " and host = '" + hostId + "';");
			connection.commit();
		} catch (SQLException sqle) {
			try {
				connection.rollback();
			} catch (SQLException e) {}
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				connection.setAutoCommit(true);
			} catch (SQLException sqle) {
			} finally {
				try {
					if (st != null) {
						st.close();
					}
				} catch (SQLException se) {}
				try {
					if (ps != null) {
						ps.close();
					}
				} catch (SQLException se) {}
			}
		}
		System.err.println("Invalid operation: " + task);
	}

	@Override
	public synchronized void putSingleFormulaModel(SingleFormulaTask task) throws EPMCDBException {
		establishConnection();

		PreparedStatement ps = null;
		try {
			connection.setAutoCommit(false);
			ps = connection.prepareStatement("update task_formula set formula_result = ? where taskid = " + task.getTaskId() + " and formulaid = ?;");
			ps.setString(1, task.getValue());
			ps.setInt(2, task.getFormulaId());
			ps.executeUpdate();
			ps = connection.prepareStatement("insert into checker_message(taskid, content) values (" + task.getTaskId() + ", ?);");
//			ps = connection.prepareStatement("update task set checker_messages = concat (checker_messages, '\n', ?) where id = " + task.getTaskId() + ";");
			ps.setString(1, task.getMessage());
			ps.executeUpdate();
			connection.commit();
		} catch (SQLException sqle) {
			try {
				connection.rollback();
			} catch (SQLException e) {}
			throw new EPMCDBException(DBMSError.SQLFailed, sqle);
		} finally {
			try {
				connection.setAutoCommit(true);
			} catch (SQLException sqle) {
			} finally {
				try {
					if (ps != null) {
						ps.close();
					}
				} catch (SQLException se) {}
			}
		}
		System.out.println("Partial result: " + task.toString());
	}
}
