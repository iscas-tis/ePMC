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

package epmc.command;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Charsets;

import epmc.error.EPMCException;
import epmc.error.UtilError;
import epmc.graph.LowLevel;
import epmc.graph.Scheduler;
import epmc.main.LogCommandLine;
import epmc.main.error.ProblemsEPMC;
import epmc.main.options.OptionsEPMC;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.CommandTask;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.RawProperty;
import epmc.options.Options;
import epmc.util.Util;

public class CommandTaskCheck implements CommandTask {
    /** String ": ".*/
    private final static String SPACE_COLON = ": ";

    public final static String IDENTIFIER = "check";
    private ModelChecker modelChecker;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        this.modelChecker = modelChecker;
    }

    @Override
    public void executeInServer() {
        modelChecker.check();
    }
    
    @Override
    public void executeInClientAfterServer() {
        Options options = Options.get();
        LogCommandLine log = options.get(OptionsMessages.LOG);
        assert options != null;
        assert log != null;
        List<String> resultOutputFiles = options.getStringList(OptionsEPMC.RESULT_OUTPUT_FILES);
        List<OutputStream> resultOutput = getResultOutputs(options);
        int index = 0;
        for (RawProperty property : log.getProperties()) {
            String exprString = property.getName();
            if (exprString == null) {
                exprString = property.getDefinition();
            }
            Object propResult = log.get(property);
            if (propResult == null) {
                index++;
                continue;
            }
            String resultString = null;
            if (propResult instanceof EPMCException) {
                EPMCException e = (EPMCException) propResult;
                String message = e.getProblem().getMessage(options.getLocale());
                MessageFormat formatter = new MessageFormat(message);
                formatter.applyPattern(message);
                resultString = formatter.format(e.getArguments());
                if (options != null && options.getBoolean(OptionsEPMC.PRINT_STACKTRACE)) {
                    e.printStackTrace();
                }
            } else {
                resultString = propResult.toString();
            }
            if (resultOutput != null && index < resultOutput.size()) {
                String filename = resultOutputFiles.get(index);
                System.out.println(exprString + SPACE_COLON + filename);
                OutputStream out = resultOutput.get(index);
                OutputStreamWriter writer = new OutputStreamWriter(out, Charsets.UTF_8);
                try {
                    writer.write(resultString);
                    writer.close();
                } catch (IOException e) {
                    UtilError.fail(ProblemsEPMC.ERROR_WRITING_RESULT_OUTPUT, e);
                }
            } else {
                System.out.println(exprString + SPACE_COLON + resultString);
            }
            Scheduler scheduler = log.getScheduler(property);
            LowLevel lowLevel = log.getLowLevel(property);
            if (scheduler != null) {
                Util.printScheduler(System.out, lowLevel, scheduler);
            }
            index++;
        }
        if (log.getCommonResult() != null) {
            System.out.println(log.getCommonResult().toString());
        }
        if (resultOutput != null) {
            for (OutputStream out : resultOutput) {
                try {
                    out.close();
                } catch (IOException e) {
                    UtilError.fail(ProblemsEPMC.ERROR_WRITING_RESULT_OUTPUT, e);
                }
            }
        }
    }
    
    private static List<OutputStream> getResultOutputs(Options options) {
        assert options != null;
        List<String> resultOutputFiles = null;
        if (options.wasSet(OptionsEPMC.RESULT_OUTPUT_FILES)) {
            resultOutputFiles = options.getStringList(OptionsEPMC.RESULT_OUTPUT_FILES);
        } else {
            return null;
        }
        List<OutputStream> result = new ArrayList<>();
        for (String filename : resultOutputFiles) {
            try {
                result.add(new FileOutputStream(filename));
            } catch (FileNotFoundException e) {
                UtilError.fail(ProblemsEPMC.NOT_CREATE_RESULT_OUTPUT, e, filename);
            }
        }
        return result;
    }
}
