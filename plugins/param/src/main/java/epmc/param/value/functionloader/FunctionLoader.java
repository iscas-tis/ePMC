package epmc.param.value.functionloader;

import java.io.InputStream;
import java.util.List;

import epmc.param.value.ValueFunction;

public interface FunctionLoader {
    interface Builder {
        FunctionLoader build();
    }
    
    List<ValueFunction> readFunctions(InputStream... input);
}
