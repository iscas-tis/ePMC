package epmc.messages;

import epmc.options.Options;

public final class TimeStampFormatMillisecondsAbsolute implements TimeStampFormat {
    public static String IDENTIFIER = "milliseconds-absolute";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public String toString(Options options, long timeStarted, long time) {
        return Long.toString(time + timeStarted);
    }

}
