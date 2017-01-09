package epmc.messages;

import java.util.Date;

import epmc.options.Options;

public final class TimeStampFormatJavaDate implements TimeStampFormat {
    public static String IDENTIFIER = "java-date";
    
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }
    
    @Override
    public String toString(Options options, long timeStarted, long time) {
        return new Date(timeStarted + time).toString();
    }
}
