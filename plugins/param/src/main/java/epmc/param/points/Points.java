package epmc.param.points;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import com.google.common.base.Charsets;

import epmc.param.value.ParameterSet;
import epmc.value.ValueArrayInterval;

public interface Points {
    interface Builder {
        Builder setInput(Reader input);
        
        default Builder setInput(String input) {
            return setInput(new StringReader(input));
        }
        
        default Builder setInput(InputStream input) {
            return setInput(new InputStreamReader(input, Charsets.UTF_8));
        }

        Builder setParameters(ParameterSet parameters);
        
        Points build();
    }
    
    ParameterSet getParameters();
    
    boolean hasNext();
    
    void next(ValueArrayInterval point);
    
    default boolean isIntervals() {
        return false;
    }
}
