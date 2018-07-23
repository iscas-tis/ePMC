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

package epmc.jani.interaction;

import java.io.InputStream;

import epmc.error.EPMCException;
import epmc.main.messages.MessagesEPMC;
import epmc.main.options.OptionsEPMC;
import epmc.modelchecker.CommandTask;
import epmc.modelchecker.Log;
import epmc.modelchecker.Model;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.ModelDummy;
import epmc.modelchecker.Properties;
import epmc.modelchecker.RawModel;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.options.UtilOptions;
import epmc.plugin.AfterCommandExecution;
import epmc.plugin.AfterModelCreation;
import epmc.plugin.AfterServerStart;
import epmc.plugin.BeforeModelCreation;
import epmc.plugin.UtilPlugin;
import epmc.util.Util;
import epmc.value.ContextValue;

public final class Analyse {
    /** string to obtain SVN revision number from Maven component */
    private final static String SCM_REVISION = "SCM-Revision";
    /** Empty string. */
    private final static String EMPTY = "";

    /**
     * Executes task on the server.
     * 
     * @param rawModel model to use, or {@code null}
     * @param options options to use
     * @param log log used
     */
    public static void execute(RawModel rawModel, Options options, Log log) {
        assert options != null;
        assert log != null;
        Options oldOptions = Options.get();
        ContextValue oldContext = ContextValue.get();
        Options.set(options);
        try {
            processAfterServerStart(options);
        } catch (EPMCException e) {
            log.send(e);
        }
        String revision = Util.getManifestEntry(SCM_REVISION);
        if (revision != null) {
            revision = revision.trim();
        }
        if (revision != null && !revision.equals(EMPTY)) {
            log.send(MessagesEPMC.RUNNING_EPMC_REVISION, revision);
        }
        sendAssertionsMessage(log);
        try {
            ContextValue.set(new ContextValue());
            processBeforeModelCreations();
        } catch (EPMCException e) {
            log.send(e);
        }
        Model model;
        try {
            model = parseModel(rawModel);
        } catch (EPMCException e) {
            log.send(e);
            Options.set(oldOptions);
            ContextValue.set(oldContext);
            return;
        }
        try {
            processAfterModelCreations();
        } catch (EPMCException e) {
            log.send(e);
        }
        CommandTask command = UtilOptions.getInstance(options,
                OptionsEPMC.COMMAND_CLASS,
                Options.COMMAND);
        ModelChecker modelChecker;
        try {
            modelChecker = new ModelChecker(model);
        } catch (EPMCException e) {
            log.send(e);
            Options.set(oldOptions);
            ContextValue.set(oldContext);
            return;
        }
        command.setModelChecker(modelChecker);
        command.executeInServer();
        modelChecker.close();
        try {
            processAfterCommandExecution();
        } catch (EPMCException e) {
            log.send(e);
        }
        Options.set(oldOptions);
	if (oldContext != null) {
	    ContextValue.set(oldContext);
	}
    }

    /**
     * Parse model.
     * The given expression context is used to construct expressions.
     * The input language etc. are decided by the options associated to the
     * value context.
     * If no model to parse is given, a dummy model is created.
     * 
     * @param context value context used
     * @param rawModel model to parse, or {@code null}
     * @return parsed model
     */
    private static Model parseModel(RawModel rawModel) {
        Model model;
        if (rawModel == null || rawModel.getModelInputStreams().length == 0) {
            model = new ModelDummy();
        } else {
            InputStream[] inputs = rawModel.getModelInputStreams();
            model = UtilOptions.getInstance(OptionsModelChecker.MODEL_INPUT_TYPE);
            model.read(rawModel.getModelInputIdentifier(), inputs);
        }
        Properties properties = model.getPropertyList();
        if (rawModel != null
                && rawModel.getPropertyInputStreams() != null
                && rawModel.getPropertyInputStreams().length != 0
                && properties != null) {
            properties.parseProperties(rawModel.getPropertyInputIdentifier(), rawModel.getPropertyInputStreams());
        }

        return model;
    }

    /**
     * Send message about whether assertions are enabled.
     * The message will be send to the log given.
     * The log parameter must not be {@code null}.
     * 
     * @param log log to send message to
     */
    private static void sendAssertionsMessage(Log log) {
        assert log != null;
        boolean assertionsEnabled = false;
        try {
            assert false;
        } catch (AssertionError e) {
            assertionsEnabled = true;
        }
        if (assertionsEnabled) {
            log.send(MessagesEPMC.ASSERTIONS_ENABLED);
        } else {
            log.send(MessagesEPMC.ASSERTIONS_DISABLED);
        }
    }

    /**
     * Process plugin classes implementing {@link AfterServerStart}.
     * The options parameter contains the plugin classes used.
     * It will also be used as a parameter when calling
     * {@link AfterServerStart#process(Options)}.
     * The options parameter must not be {@code null}.
     * 
     * @param options options to use
     */
    private static void processAfterServerStart(Options options) {
        assert options != null;
        for (Class<? extends AfterServerStart> clazz : UtilPlugin.getPluginInterfaceClasses(options, AfterServerStart.class)) {
            AfterServerStart afterModelLoading = null;
            afterModelLoading = Util.getInstance(clazz);
            afterModelLoading.process(options);
        }
    }

    /**
     * Process plugin classes implementing {@link BeforeModelCreation}.
     * The options of the context expression parameter contain the plugin
     * classes used.
     * It will also be used as a parameter when calling
     * {@link BeforeModelCreation#process()}.
     * The expression context parameter must not be {@code null}.
     * 
     */
    private static void processBeforeModelCreations()
    {
        Options options = Options.get();
        for (Class<? extends BeforeModelCreation> clazz : UtilPlugin.getPluginInterfaceClasses(options, BeforeModelCreation.class)) {
            BeforeModelCreation beforeModelLoading = null;
            beforeModelLoading = Util.getInstance(clazz);
            beforeModelLoading.process();
        }
    }

    /**
     * Process plugin classes implementing {@link AfterModelCreation}.
     * The options of the context expression parameter contain the plugin
     * classes used.
     * It will also be used as a parameter when calling
     * {@link AfterModelCreation#process()}.
     * The expression context parameter must not be {@code null}.
     * 
     */
    private static void processAfterModelCreations() {
        Options options = Options.get();
        for (Class<? extends AfterModelCreation> clazz : UtilPlugin.getPluginInterfaceClasses(options, AfterModelCreation.class)) {
            AfterModelCreation afterModelLoading = null;
            afterModelLoading = Util.getInstance(clazz);
            afterModelLoading.process();
        }
    }

    /**
     * Process plugin classes implementing {@link AfterCommandExecution}.
     * The options parameter contains the plugin classes used.
     * It will also be used as a parameter when calling
     * {@link AfterCommandExecution#process()}.
     * The value context parameter must not be {@code null}.
     * 
     */
    private static void processAfterCommandExecution() {
        for (Class<? extends AfterCommandExecution> clazz : UtilPlugin.getPluginInterfaceClasses(Options.get(), AfterCommandExecution.class)) {
            AfterCommandExecution afterCommandExecution = null;
            afterCommandExecution = Util.getInstance(clazz);
            afterCommandExecution.process();
        }
    }

    private Analyse() {
    }
}
