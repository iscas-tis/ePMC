/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

package epmc.jani.interaction.remote;

import static epmc.error.UtilError.fail;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.main.EPMC;
import epmc.messages.Message;
import epmc.messages.OptionsMessages;
import epmc.options.Options;
import epmc.plugin.OptionsPlugin;

/**
 * Represents a task server running on the local machine.
 * 
 * @author Ernst Moritz Hahn
 */
public final class TaskServerLocal implements TaskServer {
    /** name of system property to obtain Java home */
    private final static String JAVA_HOME = "java.home";
    /** subdirectory for Java binaries within Java home */
    private final static String BIN = "bin";
    /** name of command to start Java */
    private final static String JAVA = "java";
    /** name of system property to obtain Java class path */
    private final static String JAVA_CLASS_PATH = "java.class.path";
    /** Java VM parameter to enable assertions */
    private final static String ENABLE_ASSERTIONS = "-ea";
    /** Java VM parameter to enable extended object serialisation debugging */
    private final static String EXTENDED_SERIALIZATION_DEBUG = "-Dsun.io.serialization.extendedDebugInfo=true";
    /** Java VM parameter to set the class path */
    private final static String CLASSPATH = "-cp";
    /** EPMC parameter to start as task server */
    private final static String COMMAND_SERVER = "server";
    /** string containing comma */
    private final static String COMMA = ",";
    /** string containing a sequence of two minuses, for EPMC parameters */
    private final static String DOUBLE_MINUS = "--";
    /** string containing "false", for EPMC parameters  */
    private final static String FALSE = "false";
    /** contains the locally running Java VM running EPMC task server */
    private Process process;
    /** RMI connection to EPMC task server to send commands to */
    private JANIRemote server;
    /** whether the server has been started */
    private boolean started;
    /** whether the server has been stopped */
    private boolean stopped;

    @Override
    public void start() {
        assert !started;
        started = true;
        Class<EPMC> mainClass = epmc.main.EPMC.class;
        String javaHome = System.getProperty(JAVA_HOME);
        String javaBin = javaHome +
                File.separator + BIN +
                File.separator + JAVA;
        String classpath = System.getProperty(JAVA_CLASS_PATH);
        String className = mainClass.getCanonicalName();
        List<String> plugins = Options.get().get(OptionsPlugin.PLUGIN);
        assert plugins != null;
        String pluginString = String.join(COMMA, plugins);
        ProcessBuilder builder = new ProcessBuilder();
        List<String> command = new ArrayList<>();
        command.add(javaBin);
        try {
            assert false;
        } catch (AssertionError e) {
            command.add(ENABLE_ASSERTIONS);
            command.add(EXTENDED_SERIALIZATION_DEBUG);
        }
        command.add(CLASSPATH);
        command.add(classpath);
        command.add(className);
        command.add(COMMAND_SERVER);
        command.add(DOUBLE_MINUS + OptionsMessages.TRANSLATE_MESSAGES);
        command.add(FALSE);
        if (plugins.size() > 0) {
            command.add(DOUBLE_MINUS + OptionsPlugin.PLUGIN);
            command.add(pluginString);
        }
        builder.command(command);
        try {
            assert false;
        } catch (AssertionError e) {
            builder.redirectError(Redirect.INHERIT);
        }
        Process process = null;
        try {
            process = builder.start();
        } catch (IOException e) {
            fail(ProblemsRemote.REMOTE_PROCESS_CREATE_IO_EXCEPTION, e.getMessage(), e);
        }
        RMIConnectionData rmi = UtilRemote.readServerStatus(process.getInputStream());
        Registry registry = null;
        try {
            registry = LocateRegistry.getRegistry(rmi.getPort());
        } catch (RemoteException e) {
            fail(ProblemsRemote.REMOTE_GET_REGISTRY_FAILED, e.getMessage(), e);
        }
        JANIRemote iscasMcServer = null;
        try {
            iscasMcServer = (JANIRemote) registry.lookup(rmi.getName());
        } catch (RemoteException e) {
            fail(ProblemsRemote.REMOTE_REGISTRY_LOOKUP_REMOTE_EXCEPTION, e.getMessage(), e);
        } catch (NotBoundException e) {
            fail(ProblemsRemote.REMOTE_NOT_BOUND_EXCEPTION, e.getMessage(), e);
        }
        this.process = process;
        this.server = iscasMcServer;
    }

    @Override
    public JANIRemote getServer() {
        return server;
    }

    @Override
    public void stop() {
        assert started;
        assert !stopped;
        stopped = true;
        /* if the server has already been terminated before, we are done */
        if (!process.isAlive()) {
            return;
        }
        /* Try to terminate server by exit command. Start new thread, otherwise
         * will block in case the command does not work as expected and the call
         * thus does not terminate. */
        new Thread(() -> {
            try {
                EPMCChannel channel = new EPMCChannel() {
                    @Override
                    public void setTimeStarted(long time) throws RemoteException {
                    }
                    @Override
                    public void send(long time, Message key, String... arguments) throws RemoteException {
                    }
                    @Override
                    public void send(EPMCException exception) throws RemoteException {
                    }
                    @Override
                    public void send(String name, JsonValue result) throws RemoteException {
                    }
                };
                Options userOptions = Options.get().clone();
                userOptions.set(Options.COMMAND, JANIServer.EXIT);
                execute(userOptions, channel, null, true);
                UnicastRemoteObject.unexportObject(channel, true);
            } catch (EPMCException e) {
                /* we don't care about exceptions thrown at this point */
            } catch (NoSuchObjectException e) {
                /* we don't care about exceptions thrown at this point */
            }
        });
        /* If the exit command worked immediately, return. Otherwise, wait and
         * check again. */
        if (!process.isAlive()) {
            return;
        }
        try {
            process.waitFor(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }
        if (!process.isAlive()) {
            return;
        }
        /* If the exit command did not work, destroy the process. First, try to
         * do so in a nice way. If this does not work, force termination. */
        process.destroy();
        if (!process.isAlive()) {
            return;
        }
        try {
            process.waitFor(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }
        if (!process.isAlive()) {
            return;
        }
        process.destroyForcibly();
        if (!process.isAlive()) {
            return;
        }
        try {
            process.waitFor(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }
        if (!process.isAlive()) {
            return;
        }
        try {
            process.waitFor(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }
        /* If the process did not terminate even after trying to force to
         * terminate it and waiting quite a while, we have a problem and must
         * ask the user to terminate it manually. */
        if (process.isAlive()) {
            fail(ProblemsRemote.REMOTE_FAILED_TERMINATE_PROCESS);
        }
    }
}
