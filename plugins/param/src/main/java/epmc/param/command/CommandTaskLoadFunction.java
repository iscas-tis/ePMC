package epmc.param.command;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import epmc.modelchecker.CommandTask;
import epmc.modelchecker.ModelChecker;
import epmc.options.Options;
import epmc.options.UtilOptions;
import epmc.param.options.OptionsParam;
import epmc.param.value.ValueFunction;
import epmc.param.value.functionloader.FunctionLoader;

public final class CommandTaskLoadFunction implements CommandTask {
    public final static String IDENTIFIER = "param-load-function";
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
        FunctionLoader.Builder builder = UtilOptions.getInstance(OptionsParam.PARAM_FUNCTION_LOADER);
        FunctionLoader functionLoader = builder
                .build();
        String functionFilename = Options.get().getString(OptionsParam.PARAM_FUNCTION_INPUT_FILENAME);
        if (functionFilename == null) {
            // TODO
            return;
        }
        List<ValueFunction> functions = null;
        try {
            FileInputStream input = new FileInputStream(functionFilename);
            functions = functionLoader.readFunctions(input);
        } catch (FileNotFoundException e) {            
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        for (ValueFunction function : functions) {
            System.out.println(function);
        }
    }
}
