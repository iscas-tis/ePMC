package epmc.messages;

import epmc.options.Options;

public final class TimeStampFormatMillisecondsStarted implements TimeStampFormat {
    public static String IDENTIFIER = "milliseconds-started";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public String toString(Options options, long timeStarted, long time) {
        return Long.toString(time);
    }

}
