package epmc.messages;

import java.util.concurrent.TimeUnit;

import epmc.options.Options;

public final class TimeStampFormatSecondsAbsolute implements TimeStampFormat {
    public static String IDENTIFIER = "seconds-absolute";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public String toString(Options options, long timeStarted, long time) {
        return Long.toString(TimeUnit.MILLISECONDS.toSeconds(time + timeStarted));
    }

}
