package epmc.messages;

import epmc.options.Options;

public interface TimeStampFormat {
    String getIdentifier();
    
    String toString(Options options, long timeStarted, long time);
}
