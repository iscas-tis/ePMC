package epmc.util;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;

public final class UtilEscape {
    private final static Escaper ESCAPE_JAVA = Escapers.builder()
            .addEscape('\t', "\\t")
            .addEscape('\b', "\\b")
            .addEscape('\n', "\\n")
            .addEscape('\r', "\\r")
            .addEscape('\f', "\\f")
            .addEscape('\'', "\\'")
            .addEscape('\"', "\\\"")
            .addEscape('\\', "\\\\")
            .build();
    private final static Escaper ESCAPE_C = Escapers.builder()
            .addEscape('\t', "\\t")
            .addEscape('\u0007', "\\a")
            .addEscape('\u000c', "\\f")
            .addEscape('\u000b', "\\v")
            .addEscape('\u001b', "\\e")
            .addEscape('?', "\\?")
            .addEscape('\b', "\\b")
            .addEscape('\n', "\\n")
            .addEscape('\r', "\\r")
            .addEscape('\f', "\\f")
            .addEscape('\'', "\\'")
            .addEscape('\"', "\\\"")
            .addEscape('\\', "\\\\")
            .build();

    public static String escapeJava(String format) {
        return ESCAPE_JAVA.escape(format);
    }
    
    public static String escapeC(String format) {
        return ESCAPE_C.escape(format);
    }

    private UtilEscape() {
    }
}
