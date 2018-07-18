package epmc.param.value;

import epmc.options.Options;
import epmc.param.options.OptionsParam;
import epmc.value.Type;

/**
 * Type which can send statistical informations.
 * 
 * @author Ernst Moritz Hahn
 */
public interface TypeStatistics extends Type {
    default void sendStatistics() {
    }
    
    static boolean is(Type type) {
        return type instanceof TypeStatistics;
    }

    static TypeStatistics as(Type type) {
        if (is(type)) {
            return (TypeStatistics) type;
        } else {
            return null;
        }
    }
    
    static void sendStatistics(Type type) {
        if (!is(type)) {
            return;
        }
        if (!Options.get().getBoolean(OptionsParam.PARAM_STATISTICS_SEND)) {
            return;
        }
        as(type).sendStatistics();
    }
}
