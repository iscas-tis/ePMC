package epmc.webserver.backend.worker;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import epmc.EPMCRemote;
import epmc.command.CommandTaskCheck;
import epmc.error.EPMCException;
import epmc.modelchecker.ModelCheckerResult;
import epmc.modelchecker.RawProperties;
import epmc.modelchecker.RawProperty;
import epmc.modelchecker.UtilModelChecker;
import epmc.options.OptionsEPMC;
import epmc.options.Options;
import epmc.options.OptionsTypesEPMC;
import epmc.options.UtilOptions;
import epmc.options.UtilOptionsEPMC;
import epmc.webserver.backend.BackendEngine;
import epmc.webserver.backend.DataStore;
import epmc.webserver.backend.worker.channel.PrismModelMessageChannel;
import epmc.webserver.backend.worker.task.towork.CheckModelTask;
import epmc.webserver.backend.worker.task.worked.WorkedTask;
import epmc.webserver.backend.worker.task.worked.completed.CompletedCheckModelTask;
import epmc.webserver.backend.worker.task.worked.failed.FailedAnalysisTask;
import epmc.webserver.backend.worker.task.worked.failed.FailedRuntimeTask;
import epmc.webserver.backend.worker.task.worked.failed.FailedTimedOutTask;
import epmc.webserver.backend.worker.task.worked.system.SystemFailureTask;
import epmc.webserver.backend.worker.task.worked.system.SystemIrreversibleFailureTask;
import epmc.webserver.backend.worker.task.worked.system.SystemShutdownTask;
import epmc.webserver.common.Formula;
import epmc.webserver.common.Pair;
import epmc.webserver.common.TaskOperation;
import epmc.webserver.common.TaskStatus;
import epmc.webserver.error.ProblemsWebserver;

import java.util.Arrays;

/**
 * Worker that implements a Prism Model Checker. Interact with the EPMC server to compute properties of a model
 * @author ori
 */
public class PrismModelChecker extends Worker {
	
	private final DataStore dataStore = DataStore.getDataStore();
	private final CheckModelTask model;
	private PrismModelMessageChannel channel;
	
	EPMCRemote epmcServer;
	
	/**
	 * Instantiates a new PrismModelChecker with a task to be checked
	 * @param model The task containing model and properties to check
	 */
	public PrismModelChecker(CheckModelTask model) {
		this.model = model;
		channel = null;
	}

	@Override
	public void run() {
		WorkedTask resultingModel = null;

		if (model.getModel() == null) {
			// TODO!
//			dataStore.addWorkedTask(this, new FailedParseTask(model.getUserId(), model.getTaskId(), TaskOperation.checkFormula, ProblemsWebserver.PRISM_PARSER_NO_MODULE.toString(), 0, 0, null));
			return;
		}

		
		final String host = "127.0.0.1";
		final int portNumber = dataStore.getPortNumber();
		if (portNumber < 1024) {
			dataStore.addWorkedTask(this, new SystemFailureTask(model.getUserId(), model.getTaskId(), model.getOperation(), ProblemsWebserver.WORKER_MODEL_CHECKER_START_FAILED.toString()));
			return;
		}
		
        String name = "EPMC-" + portNumber + "-" + System.currentTimeMillis();

		final Options options = UtilOptionsEPMC.newOptions();
		
		try {
			String mod_options = model.getOptions();
			if (mod_options == null) {
				mod_options = "";
			}
			options.set(OptionsEPMC.SERVER_NAME, name);
			options.set(Options.COMMAND, CommandTaskCheck.IDENTIFIER);
			options.set(OptionsEPMC.MODEL_INPUT_TYPE, model.getModelType());
			if (BackendEngine.ltl2tgbaPath() != null) {
				options.set(OptionsEPMC.LTL2BA_SPOT_LTL2TGBA_CMD, BackendEngine.ltl2tgbaPath());
			}
			
			UtilOptions.loadProperties(options, mod_options);
		} catch (Throwable ie) {
		    dataStore.addWorkedTask(this,new FailedAnalysisTask(model.getUserId(), model.getTaskId(), TaskOperation.checkFormula, ProblemsWebserver.WORKER_MODEL_CHECKER_INVALID_OPTIONS.toString(), new Object[0], null));
			return;
		}

		try {
//			String javaCmd = "/usr/bin/java";
//			String command = "on_die() { kill 0\n exit 0\n } \n trap 'on_die' TERM \n trap 'on_die' KILL \n ulimit -v " + BackendEngine.epmcMemoryLimit() + " \n " 
//								+ BackendEngine.javapath() 
//								+ " " + BackendEngine.epmcJVMOptions() 
//								+ (BackendEngine.jnaLibraryPath()== null ? "" : " -Djna.library.path=" + BackendEngine.jnaLibraryPath())
//								+ (BackendEngine.javaLibraryPath() == null ? "" : " -Djava.library.path=" + BackendEngine.javaLibraryPath())
//								+ " -classpath " + BackendEngine.classpath() 
//								+ " epmc.EPMC server " + String.valueOf(portNumber) + " --server-name " + name;

//	        String[] execArr = {javaCmd, "-jar", BackendEngine.epmc, "server", String.valueOf(portNumber), "--server-name", name, "1>/tmp/iscas-out", "2>/tmp/iscas-err"};
//			mc = Runtime.getRuntime().exec(execArr);
//			ProcessBuilder pb = new ProcessBuilder("/usr/bin/java", "-classpath", BackendEngine.classpath, "-jar", BackendEngine.epmc, "server", String.valueOf(portNumber), "--server-name", name);
//			ProcessBuilder pb = new ProcessBuilder("/usr/bin/java", "-classpath", BackendEngine.classpath + ":" + BackendEngine.epmc, "epmc.EPMC", "server", String.valueOf(portNumber), "--server-name", name);
// 			ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-i", "-c", command);

			
			List<String> command = new ArrayList<String>();
			command.add(BackendEngine.javapath());
			command.addAll(Arrays.asList(BackendEngine.epmcJVMOptions().trim().split(" ")));
			if (BackendEngine.jnaLibraryPath() != null) {
				command.add("-Djna.library.path=" + BackendEngine.jnaLibraryPath());
			}
			if (BackendEngine.javaLibraryPath() != null) {
				command.add("-Djava.library.path=" + BackendEngine.javaLibraryPath());
			}
			command.add("-classpath");
			command.add(BackendEngine.classpath());
			command.add("epmc.EPMC");
			command.add("server");
			command.add(String.valueOf(portNumber));
			command.add("--server-name");
			command.add(name);
 			ProcessBuilder pb = new ProcessBuilder(command);
			File out = new File(BackendEngine.epmcLogDirectory() + (BackendEngine.epmcLogDirectory().endsWith("/") ? "" : "/") + "EPMC-server-task-" + model.getTaskId());
			pb.redirectErrorStream(true);
			pb.redirectOutput(Redirect.appendTo(out));
			setProcess(pb.start());
			checkExitValue();
			dataStore.releasePortNumber(portNumber);
			dataStore.addWorkedTask(this, new SystemFailureTask(model.getUserId(), model.getTaskId(), model.getOperation(), ProblemsWebserver.WORKER_MODEL_CHECKER_START_FAILED.toString()));
			return;
		} catch (IOException ioe) {
			killProcess();
			dataStore.releasePortNumber(portNumber);
			dataStore.addWorkedTask(this, new SystemFailureTask(model.getUserId(), model.getTaskId(), model.getOperation(), ProblemsWebserver.WORKER_MODEL_CHECKER_START_FAILED.toString()));
			return;
		} catch (IllegalThreadStateException ite) {}
		try {
			sleep(1000);
			//just to give time to the server to be fully active
		} catch (InterruptedException ie) {
		}

		Map<RawProperty, Integer> propToId = new HashMap<RawProperty, Integer>(model.formulae().size());
		try {
			epmcServer = (EPMCRemote)LocateRegistry.getRegistry(host, portNumber).lookup(name);
			channel = new PrismModelMessageChannel(dataStore, model, propToId);
		} catch (NotBoundException | RemoteException e) {
			killProcess();
			dataStore.releasePortNumber(portNumber);
			dataStore.addWorkedTask(this, new SystemFailureTask(model.getUserId(), model.getTaskId(), model.getOperation(), ProblemsWebserver.WORKER_MODEL_CHECKER_CONNECTION_FAILED.toString()));
			return;
		} catch (Throwable t) {
			killProcess();
			dataStore.releasePortNumber(portNumber);
			dataStore.addWorkedTask(this, new FailedRuntimeTask(model.getUserId(), model.getTaskId(), model.getOperation(), ProblemsWebserver.WORKER_MODEL_CHECKER_CONNECTION_FAILED.toString(), t));
			return;
		}

		try {
			final byte[] rawModel = model.getModel().getBytes();
			final RawProperties properties = new RawProperties();
			List<Pair<Integer, RawProperty>> idToProp = new ArrayList<Pair<Integer, RawProperty>>(model.formulae().size());
			for (Formula formula : model.formulae()) {
				RawProperty prop = new RawProperty(formula.getFormula(), formula.getComment());
				idToProp.add(new Pair<Integer, RawProperty>(formula.getId(), prop));
				propToId.put(prop, formula.getId());
				properties.addProperty(prop);
			}
			
			final EPMCException[] ime = new EPMCException[1];
			ime[0] = null;
			final RemoteException[] re = new RemoteException[1];
			re[0] = null;
			final Throwable[] throwable = new Throwable[1];
			throwable[0] = null;
			final ModelCheckerResult[] result = new ModelCheckerResult[1];
			Runnable actualWorker = new Runnable() {
						@Override
						public void run() {
							try {
//								System.out.println("Ready to call the solver, but sleep before...");
//								try {
//									sleep(30000);
//								} catch (InterruptedException ie) {}
					            options.set(OptionsEPMC.RUN_MODE, OptionsTypesEPMC.RunMode.WEB);
								result[0] = epmcServer.execute(UtilModelChecker.newRawModel(new byte[][]{rawModel}), properties, options, channel);
								killProcess();
							} catch (EPMCException e1) {
								ime[0] = e1;
							} catch (RemoteException e1) {
								re[0] = e1;	
							} catch (Throwable t) {
								throwable[0] = t;
							}
						}
					};
			Thread thread = new Thread(actualWorker);
			thread.start();

			long expectedTermination = model.getTimeOutInMinutes() * 60000L + System.currentTimeMillis();
			long stillToWait = expectedTermination -  System.currentTimeMillis();
			while (thread.isAlive() && stillToWait > 0) {
				try {
					thread.join(stillToWait);
				} catch (InterruptedException ie) {}
				stillToWait = expectedTermination - System.currentTimeMillis();
			}
			if (thread.isAlive()) {
				killProcess();
				if (channel != null) {
					try {
						UnicastRemoteObject.unexportObject(channel, true);
					} catch (NoSuchObjectException ne) {
						// nevermind
					} finally {
						channel = null;
					}
				}
				resultingModel = new FailedTimedOutTask(model.getUserId(), model.getTaskId(), TaskOperation.checkFormula, ProblemsWebserver.WORKER_MODEL_CHECKER_TIMED_OUT.toString(), model.getTimeOutInMinutes());
			} else {
				if (ime[0] != null) {
					throw ime[0];
				}
				if (re[0] != null) {
					throw re[0];
				}
				if (throwable[0] != null) {
					throw throwable[0];
				}
				if (result[0] != null) {
					List<Pair<Integer, String>> values = new ArrayList<Pair<Integer, String>>(model.formulae().size());
					for (Pair<Integer, RawProperty> idp : idToProp) {
						values.add(new Pair<Integer, String>(idp.fst, result[0].getString(idp.snd)));
					}
					resultingModel = new CompletedCheckModelTask(model.getUserId(), model.getTaskId(), TaskOperation.checkFormula, values);
				} else {
					if (canRun) {
						resultingModel = new FailedAnalysisTask(model.getUserId(), model.getTaskId(), TaskOperation.checkFormula, ProblemsWebserver.WORKER_MODEL_CHECKER_GENERAL_ERROR.toString(), new Object[0], null);
					} else {
						resultingModel = new SystemShutdownTask(model.getUserId(), model.getTaskId(), TaskOperation.checkFormula);
					}
				}
			}
			// TODO Moritz: CHECK

//	     } catch (EPMCRuntimeException imre) {
//	            resultingModel = new FailedRuntimeTask(model.getUserId(), model.getTaskId(), TaskOperation.build, imre.getKey(), imre);

//		} catch (EPMCParseException impe) {
//			resultingModel = new FailedParseTask(model.getUserId(), model.getTaskId(), TaskOperation.checkFormula, impe.getKey(), impe.getErrorLine(), impe.getErrorColumn(), impe.getErrorIdentifier());
		} catch (EPMCException ime) {
		    resultingModel = new FailedAnalysisTask(model.getUserId(), model.getTaskId(), TaskOperation.checkFormula, ime.getProblemIdentifier(), ime.getArguments(), ime.getLocalizedMessage());
		} catch (RemoteException re) {
			re.printStackTrace();
			Throwable cause = re.getCause();
			if (cause instanceof OutOfMemoryError) {
				resultingModel = new SystemIrreversibleFailureTask(model.getUserId(), model.getTaskId(), TaskOperation.checkFormula, TaskStatus.failedMemory, ProblemsWebserver.WORKER_MODEL_CHECKER_MEMORY_EXHAUSTED.toString());
			} else if (cause instanceof StackOverflowError) {
				resultingModel = new SystemIrreversibleFailureTask(model.getUserId(), model.getTaskId(), TaskOperation.checkFormula, TaskStatus.failedStack, ProblemsWebserver.WORKER_MODEL_CHECKER_STACK_EXHAUSTED.toString());
			} else if (cause instanceof InternalError) {
				resultingModel = new SystemIrreversibleFailureTask(model.getUserId(), model.getTaskId(), TaskOperation.checkFormula, TaskStatus.failedInternalJVMError, ProblemsWebserver.WORKER_MODEL_CHECKER_INTERNAL_JVM_ERROR.toString());
			} else if (cause instanceof UnknownError) {
				resultingModel = new SystemIrreversibleFailureTask(model.getUserId(), model.getTaskId(), TaskOperation.checkFormula, TaskStatus.failedUnknownJVMError, ProblemsWebserver.WORKER_MODEL_CHECKER_UNKNOWN_JVM_ERROR.toString());
			} else if (cause instanceof Error) {
				resultingModel = new SystemIrreversibleFailureTask(model.getUserId(), model.getTaskId(), TaskOperation.checkFormula, TaskStatus.failedJVMError, ProblemsWebserver.WORKER_MODEL_CHECKER_JVM_ERROR.toString());
			} else {
				resultingModel = new SystemFailureTask(model.getUserId(), model.getTaskId(), TaskOperation.checkFormula, ProblemsWebserver.WORKER_MODEL_CHECKER_COMMUNICATION_FAILED.toString());
			}
		} catch (Throwable t) {
			resultingModel = new FailedRuntimeTask(model.getUserId(), model.getTaskId(), TaskOperation.checkFormula, ProblemsWebserver.WORKER_MODEL_CHECKER_GENERAL_ERROR.toString(), t);
		} finally {
//			try {
//				options.set(Option.COMMAND, Command.EXIT);
//				epmcServer.execute(rawModel, properties, options, channel);
//			} catch (RemoteException | EPMCException e) {
//				e.printStackTrace();
//			}
			killProcess();
		    if (channel != null) {
                try {
                    UnicastRemoteObject.unexportObject(channel, true);
                } catch (NoSuchObjectException e) {
                    // nevermind
                }
		    }
			dataStore.releasePortNumber(portNumber);
			dataStore.addWorkedTask(this, resultingModel);
		}
	}
}
