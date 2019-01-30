package epmc.util;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.google.common.escape.Escapers.Builder;

import it.unimi.dsi.fastutil.chars.Char2ObjectMap.Entry;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;

public final class UtilEscape {
    private final static Char2ObjectOpenHashMap<String> JAVA_ESCAPE_CHARS = new Char2ObjectOpenHashMap<>();
    static {
        JAVA_ESCAPE_CHARS.put('\t', "\\t");
        JAVA_ESCAPE_CHARS.put('\b', "\\b");
        JAVA_ESCAPE_CHARS.put('\n', "\\n");
        JAVA_ESCAPE_CHARS.put('\r', "\\r");
        JAVA_ESCAPE_CHARS.put('\f', "\\f");
        JAVA_ESCAPE_CHARS.put('\'', "\\'");
        JAVA_ESCAPE_CHARS.put('\"', "\\\"");
        JAVA_ESCAPE_CHARS.put('\\', "\\\\");
    }
    private final static Char2ObjectOpenHashMap<String> C_ESCAPE_CHARS = new Char2ObjectOpenHashMap<>();
    static {
        C_ESCAPE_CHARS.put('\t', "\\t");
        C_ESCAPE_CHARS.put('\u0007', "\\a");
        C_ESCAPE_CHARS.put('\u000c', "\\f");
        C_ESCAPE_CHARS.put('\u000b', "\\v");
        C_ESCAPE_CHARS.put('\u001b', "\\e");
        C_ESCAPE_CHARS.put('?', "\\?");
        C_ESCAPE_CHARS.put('\b', "\\b");
        C_ESCAPE_CHARS.put('\n', "\\n");
        C_ESCAPE_CHARS.put('\r', "\\r");
        C_ESCAPE_CHARS.put('\f', "\\f");
        C_ESCAPE_CHARS.put('\'', "\\'");
        C_ESCAPE_CHARS.put('\"', "\\\"");
        C_ESCAPE_CHARS.put('\\', "\\\\");
    }
    
    private final static Escaper ESCAPE_JAVA;
    static {
        Builder builder = Escapers.builder();
        for (Entry<String> entry : JAVA_ESCAPE_CHARS.char2ObjectEntrySet()) {
            builder.addEscape(entry.getCharKey(), entry.getValue());
        }
        ESCAPE_JAVA = builder.build();
    }
    private final static Unescaper UNESCAPE_JAVA = new Unescaper();
    static {
        for (Entry<String> entry : JAVA_ESCAPE_CHARS.char2ObjectEntrySet()) {
            UNESCAPE_JAVA.addEscape(entry.getCharKey(), entry.getValue());
        }
    }

    private final static Escaper ESCAPE_C;
    static {
        Builder builder = Escapers.builder();
        for (Entry<String> entry : C_ESCAPE_CHARS.char2ObjectEntrySet()) {
            builder.addEscape(entry.getCharKey(), entry.getValue());
        }
        ESCAPE_C = builder.build();
    }
    private final static Unescaper UNESCAPE_C = new Unescaper();
    static {
        for (Entry<String> entry : JAVA_ESCAPE_CHARS.char2ObjectEntrySet()) {
            UNESCAPE_C.addEscape(entry.getCharKey(), entry.getValue());
        }
    }
    
    public static String escapeJava(String format) {
        return ESCAPE_JAVA.escape(format);
    }
    
    public static String escapeC(String format) {
        return ESCAPE_C.escape(format);
    }

    public static String unescapeJava(String format) {
        return UNESCAPE_JAVA.unescape(format);
    }
    
    public static String unescapeC(String format) {
        return UNESCAPE_C.unescape(format);
    }

    private UtilEscape() {
    }
}
