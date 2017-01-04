package epmc.webserver.backend;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import epmc.webserver.backend.dbms.DBMS;
import epmc.webserver.backend.dbms.DBMS_LiYi;
import epmc.webserver.backend.worker.InvalidModelWorker;
import epmc.webserver.backend.worker.PrismModelBuilder;
import epmc.webserver.backend.worker.PrismModelChecker;
import epmc.webserver.backend.worker.ResultCollector;
import epmc.webserver.backend.worker.Worker;
import epmc.webserver.backend.worker.task.towork.BuildModelTask;
import epmc.webserver.backend.worker.task.towork.CheckModelTask;
import epmc.webserver.backend.worker.task.towork.InvalidModelTask;
import epmc.webserver.backend.worker.task.towork.ModelTask;
import epmc.webserver.common.DBMSError;
import epmc.webserver.common.EPMCDBException;

/**
 * The main class of the backend engine for the EPMC service
 * @author ori
 */
public class BackendEngine {

	private static int maxWorkers;
	/**
	 * The number of workers that can run in parallel. 
	 * It is better to set it to at most the number of cores minus 1.
	 * @return the number of workers that can run in parallel.
	 */
	public static int maxWorkers() {
		return maxWorkers;
	}

	private static int timeOutInMinutes;
	/**
	 * The timeout (in minutes) for the model checking engine.
	 * @return the timeout (in minutes) for the model checking engine.
	 */
	public static int timeOutInMinutes() {
		return timeOutInMinutes;
	}

	private static int sleepTime;
	/**
	 * The sleep time (in milliseconds) of the backend before checking whether there are new task to compute; has effect only if previously there were no new tasks.
	 * @return the sleep time (in milliseconds) of the backend before checking for new tasks.
	 */
	public static int sleepTime() {
		return sleepTime;
	}

//	private static String epmc;
//	/**
//	 * The path to the jar containing the EPMC server.
//	 * Currently unused.
//	 * @return the path to the jar containing the EPMC server.
//	 */
//	public static String epmc() {
//		return epmc;
//	}
//
	private static String javapath;
	/**
	 * The path to the java program to be used for running the EPMC server.
	 * Currently the same of this backend.
	 * @return the path to the java program to be used for running the EPMC server.
	 */
	public static String javapath() {
		return javapath;
	}

	private static String epmcMcLogDirectory;
	/**
	 * The base log directory for the logs of the EPMC server.
	 * Default value: "log".
	 * @return the base log directory to be used for the logs of the EPMC server.
	 */
	public static String epmcMcLogDirectory() {
		return epmcMcLogDirectory;
	}
	
	private static String epmcJVMOptions;
	/**
	 * The options for the JVM of the EPMC server.
	 * Default value: "-Xms512m -Xmx2048m".
	 * @return the options to be passed to the JVM of the EPMC server.
	 */
	public static String epmcJVMOptions() {
		return epmcJVMOptions;
	}
	
	private static String epmcMemoryLimit;
	/**
	 * The virtual memory limit for running the EPMC server.
	 * Currently unused. 
	 * Default value: 5500000 (kB, corresponding roughly to 2GB of resident memory).
	 * @return the virtual memory limit to be used for running the EPMC server.
	 */
	public static String epmcMemoryLimit() {
		return epmcMemoryLimit;
	}

	private static String javaLibraryPath;
	/**
	 * The path to the java.library.path.
	 * Keep it unset to ignore it.
	 * Currently unset.
	 * @return the path to the java.library.path.
	 */
	public static String javaLibraryPath() {
		return javaLibraryPath;
	}

	private static String jnaLibraryPath;
	/**
	 * The path to the java.library.path.
	 * Keep it unset to ignore it.
	 * Currently unset.
	 * @return the path to the java.library.path.
	 */
	public static String jnaLibraryPath() {
		return jnaLibraryPath;
	}

	private static String classpath;
	/**
	 * The classpath to be used for running the EPMC server.
	 * Currently the same of this backend.
	 * @return the classpath to be used for running the EPMC server.
	 */
	public static String classpath() {
		return classpath;
	}

	private static String ltl2tgbaPath;
	/**
	 * The path to the ltl2tgba program.
	 * Keep it unset to look for ltl2tgba in the standard PATH.
	 * Currently unset
	 * @return the path to ltl2tgba.
	 */
	public static String ltl2tgbaPath() {
		return ltl2tgbaPath;
	}

	private static boolean sendMail = false;
	/**
	 * Whether to send email in case of runtime errors.
	 * @return whether to send email in case of runtime errors.
	 */
	public static boolean sendMail() {
		return sendMail;
	}

	private static String mailUsername;
	/**
	 * Username for the authentication with the mail server.
	 * @return the username for the authentication with the mail server.
	 */
	public static String mailUsername() {
		return mailUsername;
	}

	private static String mailPassword;
	/**
	 * Password for the authentication with the mail server.
	 * @return the password for the authentication with the mail server.
	 */
	public static String mailPassword() {
		return mailPassword;
	}

	private static String mailSender;
	/**
	 * The email address of the sender.
	 * @return the email address of the sender.
	 */
	public static String mailSender() {
		return mailSender;
	}

	private static String mailReceivers;
	/**
	 * The comma separated list of email addresses of the receivers.
	 * @return the comma separated list of email addresses of the receivers.
	 */
	public static String mailReceivers() {
		return mailReceivers;
	}
	
	private static ResultCollector resultCollector;
	private static final DataStore dataStore = DataStore.getDataStore();
	private static final boolean[] canRun = new boolean[1];

	/**
	 * The main of the backend
	 * @param args must contain the path to the configuration file for customizing the backend.
	 */
	public static void main(String args[]) {
		if (args.length != 1) {
			System.err.println("Wrong invocation. You must provide only the path to the configuration file");
			System.exit(-1);
		}
		Properties conf = new Properties();
		try {
			conf.load(new FileInputStream(args[0]));
		} catch (IOException ioe) {
			System.err.println("Error loading config file! Exiting...");
			System.exit(-1);
		}
		epmcMemoryLimit = conf.getProperty("epmcMemoryLimit", "5500000");
		epmcJVMOptions = conf.getProperty("epmcJVMOptions", "-Xms512m -Xmx2048m");
		epmcLogDirectory = conf.getProperty("epmcLogDirectory", "log");
		javapath = conf.getProperty("javapath", "/usr/bin/java");
		classpath = conf.getProperty("classpath", System.getProperty("java.class.path"));
		javaLibraryPath = conf.getProperty("javaLibraryPath");
		jnaLibraryPath = conf.getProperty("jnaLibraryPath");
		ltl2tgbaPath = conf.getProperty("ltl2tgbaPath");
		maxWorkers = Integer.parseInt(conf.getProperty("maxWorkers", "1"));
		timeOutInMinutes = Integer.parseInt(conf.getProperty("timeOutInMinutes", "30"));
		sleepTime = Integer.parseInt(conf.getProperty("sleepTime", "10000"));
		sendMail = Boolean.parseBoolean(conf.getProperty("sendMail", "false"));
		mailUsername = conf.getProperty("mailUsername");
		mailPassword = conf.getProperty("mailPassword");
		mailSender = conf.getProperty("mailSender");
		mailReceivers = conf.getProperty("mailReceivers");
		
		DBMS dbms = null;

		String db_host = conf.getProperty("db_host", "127.0.0.1");
		String db_port = conf.getProperty("db_port", "3306");
		String db_name = conf.getProperty("db_name", "iscasml");
		String db_username = conf.getProperty("db_username", "epmc");
		String db_password = conf.getProperty("db_password", "iscasmodelchecker");
		String db_dbms = conf.getProperty("db_dbms", "DBMS_LiYi");
		try {
			switch (db_dbms) {
				case "DBMS_LiYi":
					dbms = new DBMS_LiYi(db_host, db_port, db_name, db_username, db_password);
					break;
				default: 
					throw new EPMCDBException(DBMSError.UnknownDBMS);
			}
		} catch (EPMCDBException imde) {
			System.err.println("Error opening connection to database: " + imde.getError() + " Exiting...");
			System.exit(-1);
		}
		try {
 			dbms.resetFormerWorkInProgress();
		} catch (EPMCDBException imde) {
			System.err.println("Error while resetting former works in progress. Cause: " + imde.getMessage());
			System.exit(-2);
		}
		
		resultCollector = new ResultCollector(dbms);

		canRun[0] = true;
		Thread shutdown = 
				new Thread()
				{				
					@Override
					public void run() {
						System.out.println("Backend shutdown started");
						canRun[0] = false;
						dataStore.shutdownWorkers();
						resultCollector.shutdown();
						synchronized (dataStore) {
							dataStore.notifyAll();
						}
						dataStore.waitForWorkers();
						try {
							resultCollector.join();
						} catch (InterruptedException ie) {}
						System.out.println("Backend shutdown completed");
					}
				};
		Runtime.getRuntime().addShutdownHook(shutdown);
		
		resultCollector.start();
		
		while (canRun[0]) {
			try {
				if (dataStore.getWorkers() < maxWorkers) {
					EPMCDBException ime = null;
					ModelTask modelToCompute = null;
					try {
						modelToCompute = dbms.getTask();
					} catch (EPMCDBException ie) {
						ime = ie;
					} 
					if (ime == null) {
						if (modelToCompute != null) {
							Worker modelWorker;
							switch (modelToCompute.getOperation()) {
								case build:
									modelWorker = new PrismModelBuilder((BuildModelTask)modelToCompute);
									break;
								case checkFormula:
								case analyze:
									modelWorker = new PrismModelChecker((CheckModelTask)modelToCompute);
									break;
								default:
									modelWorker = new InvalidModelWorker((InvalidModelTask)modelToCompute);
									break;
							}
							dataStore.addWorker(modelWorker);
							modelWorker.start();
//						} else {
//							System.out.println("No model to work on");
						}
					} else {
						System.err.println("SQL exception occurred: ");
						ime.printStackTrace();
					}
				}
				synchronized(dataStore) {
					dataStore.wait(sleepTime);
				}
			} catch (InterruptedException ie) {}
		}
	}
}
