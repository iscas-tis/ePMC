package epmc.util;

import it.unimi.dsi.fastutil.chars.Char2CharOpenHashMap;
import static epmc.error.UtilError.ensure;

public final class Unescaper {
    private final static char SLASH = '\\';
    private final Char2CharOpenHashMap escapedToUnescaped = new Char2CharOpenHashMap();
    
    public Unescaper addEscape(char original, String escaped) {
        assert escaped.length() == 2;
        assert escaped.charAt(0) == SLASH;
        escapedToUnescaped.put(escaped.charAt(1), original);
        return this;
    }

    public String unescape(String escaped) {
        StringBuilder result = new StringBuilder();
        boolean readSlash = false;
        for (int i = 0; i < escaped.length(); i++) {
            char charRead = escaped.charAt(i);
            if (readSlash) {
                ensure(escapedToUnescaped.containsKey(charRead),
                        ProblemsUtil.CANNOT_UNESCAPE, escaped);
                charRead = escapedToUnescaped.get(charRead);
                readSlash = false;
                result.append(charRead);
            } else {
                if (charRead == SLASH) {
                    readSlash = true;
                } else {
                    result.append(charRead);
                }
            }
        }
        ensure(!readSlash, ProblemsUtil.CANNOT_UNESCAPE, escaped);
        return result.toString();
    }
}
