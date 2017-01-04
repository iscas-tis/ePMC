package epmc.options;

import static epmc.error.UtilError.ensure;

import java.util.Map;

import com.google.common.base.CaseFormat;

import epmc.error.EPMCException;
import epmc.util.Util;

/**
 * Several static auxiliary method to work with options and option sets.
 * Because this class provides only static methods, it is protected from being
 * instantiated.
 * 
 * @author Ernst Moritz Hahn
 */
public final class UtilOptions {
    
    // TODO the options part of EPMC might not the most appropriate place for the static methods here
    
    // TODO documentation
    public static <T> T getInstance(Options options, String identifier) throws EPMCException {
        assert options != null;
        assert identifier != null;
        Class<T> clazz = options.get(identifier);
        ensure(clazz != null, ProblemsOptions.OPTIONS_OPTION_NOT_SET, identifier);
        return Util.getInstance(clazz);
    }

    public static <T> T getInstance(Options options, Enum<?> identifier) throws EPMCException {
        assert options != null;
        assert identifier != null;
        String identifierString = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, identifier.name());
        return getInstance(options, identifierString);
    }

    public static <T> T getSingletonInstance(Options options, String identifier) throws EPMCException {
        assert options != null;
        assert identifier != null;
        Class<T> clazz = options.get(identifier);
        ensure(clazz != null, ProblemsOptions.OPTIONS_OPTION_NOT_SET, identifier);
        return Util.getSingletonInstance(clazz);
    }
    
    public static <T> T  getInstance(Options options,
            Enum<?> identifier, String command) {
        String identifierString = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, identifier.name());
        return getInstance(options, identifierString, command);
    }

    public static <T> T getSingletonInstance(Options options,
            Enum<?> identifier) throws EPMCException {
        assert options != null;
        assert identifier != null;
        String identifierString = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, identifier.name());
        return getSingletonInstance(options, identifierString);
    }

    public static <T> T getInstance(Options options,
            String commandTaskClass, String command) {
        Map<String,Class<T>> map = options.get(commandTaskClass);
        String string = options.get(command);
        return Util.getInstance(map, string);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private UtilOptions() {
    }
}
