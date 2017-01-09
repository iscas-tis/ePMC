package epmc.messages;

import epmc.options.Options;

public final class TimeStampFormatNone implements TimeStampFormat {
    public static String IDENTIFIER = "none";
    private final static String EMPTY = "";
    
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public String toString(Options options, long timeStarted, long time) {
        return EMPTY;
    }

}
