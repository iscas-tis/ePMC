package epmc.jani.interaction.commandline;

import java.util.Locale;
import java.util.ResourceBundle;

import com.google.common.base.CaseFormat;

public final class CommandLineCommand {
    private final static String SHORT_PREFIX = "short-";

    public final static class Builder {
        private String bundleName;
        private String identifier;

        public Builder setIdentifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder setBundleName(String bundleName) {
            this.bundleName = bundleName;
            return this;
        }

        public Builder setBundleName(Enum<?> bundleName) {
            this.bundleName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, bundleName.name());
            return this;
        }

        public Builder setIdentifier(Enum<?> identifier) {
            this.identifier = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, identifier.name());
            return this;
        }
        
        public CommandLineCommand build() {
            return new CommandLineCommand(this);
        }
    }

    private final String identifier;
    private String name;
    
    private CommandLineCommand(Builder builder) {
        assert builder != null;
        identifier = builder.identifier;
        name = getResourceString(SHORT_PREFIX + builder.identifier,
                builder.bundleName);
    }

    public String getIdentifier() {
        return identifier;
    }
    
    public String getShortDescription() {
        return name;
    }
    
    private static String getResourceString(String string, String bundleName) {
        ResourceBundle poMsg = getBundle(bundleName);
        return poMsg.getString(string);
    }
    
    private static ResourceBundle getBundle(String bundleName) {
        ResourceBundle poMsg = null;
        Locale locale = Locale.getDefault();
        poMsg = ResourceBundle.getBundle(bundleName, locale, Thread.currentThread().getContextClassLoader());
        return poMsg;
    }
}
