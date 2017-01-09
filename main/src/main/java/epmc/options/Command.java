package epmc.options;

import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;

import com.google.common.base.CaseFormat;

// TODO complete documentation

/**
 * Command to be chosen for execution from the option.
 * Note that this class only serves to allow to specify which task should be
 * executed and to print user information about this task. It is not responsible
 * for the execution of the task itself. Short and long descriptions of the
 * commands will be read from resource files.
 * 
 * @author Ernst Moritz Hahn
 */
public final class Command implements Serializable, Cloneable {
    /**
     * Builder class for {@link Command}.
     * 
     * @author Ernst Moritz Hahn
     */
    public final static class Builder {
        private Options options;
        private String bundleName;
        private String identifier;
        private boolean commandLine;
        private boolean gui;
        private boolean web;

        Builder setOptions(Options options) {
            this.options = options;
            return this;
        }
        
        private Options getOptions() {
            return options;
        }
        
        public Builder setBundleName(String bundleName) {
            this.bundleName = bundleName;
            return this;
        }
        
        public Builder setBundleName(Enum<?> bundleName) {
            this.bundleName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, bundleName.name());
            return this;
        }
        
        private String getBundleName() {
            return bundleName;
        }
        
        public Builder setIdentifier(String identifier) {
            this.identifier = identifier;
            return this;
        }
        
        private String getIdentifier() {
            return identifier;
        }
        
        public Builder setCommandLine(boolean commandLine) {
            this.commandLine = commandLine;
            return this;
        }
        
        public Builder setCommandLine() {
            commandLine = true;
            return this;
        }
        
        private boolean isCommandLine() {
            return commandLine;
        }
        
        public Builder setGui(boolean gui) {
            this.gui = gui;
            return this;
        }
        
        public Builder setGui() {
            gui = true;
            return this;
        }
        
        private boolean isGui() {
            return gui;
        }
        
        public Builder setWeb(boolean web) {
            this.web = web;
            return this;
        }
        
        public Builder setWeb() {
            web = true;
            return this;
        }
        
        private boolean isWeb() {
            return web;
        }
        
        public Command build() {
            Command result = new Command(this);
            options.addCommand(result);
            return result;
        }
    }
    
    /** Serial version UID - 1L as I don't know any better. */
    private static final long serialVersionUID = 1L;
    /** Prefix for the short description of the command. */
    private final static String SHORT_ = "short-";
    /** Prefix for the long description of the command. */
    private final static String LONG_ = "long-";
    /** {@link Options} the command is part of */
    
    private final Options options;
    /** Base name of the resource bundle containing the description. */
    private final String bundle;
    /** Identifier of the command. */
    private final String identifier;
    /** Whether the command shall be available from the command line. */
    private final boolean commandLine;
    /** Whether the command shall be available from the standalone GUI.*/
    private final boolean gui;
    /** Whether the command shall be available from public web interface. */
    private final boolean web;

    Command(Builder builder) {
        assert builder != null;
        assert builder.getOptions() != null;
        assert builder.getBundleName() != null;
        assert builder.getIdentifier() != null;
        this.options = builder.getOptions();
        this.bundle = builder.getBundleName();
        this.identifier = builder.getIdentifier();
        this.commandLine = builder.isCommandLine();
        this.gui = builder.isGui();
        this.web = builder.isWeb();
    }

    /**
     * Obtain identifier of this command.
     * This string will be the one shown to the user to specify the command to
     * be executed. Together with the prefixes {@link #SHORT_} ({@link #LONG_}),
     * the identifier specifies the key in the resource file with base name
     * {@link #bundle} to a short (long) description of the command.
     * 
     * @return identifier of the command
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Obtain short user-readable description of the command.
     * The description shall be short enough to be shown in a single line of
     * around 72 characters.
     * 
     * @return short user-readable description of the command
     */
    public String getShortDescription() {
        Locale locale = options.getLocale();
        ResourceBundle poMsg = ResourceBundle.getBundle(bundle, locale,
                Thread.currentThread().getContextClassLoader());
        return poMsg.getString(SHORT_ + identifier);
    }
    
    /**
     * Obtain long user-readable description of the command.
     * The description is not restricted in size but shall concisely describe
     * the functionality of the command.
     * 
     * @return long user-readable description of the command
     */
    public String getLongDescription() {
        Locale locale = options.getLocale();
        ResourceBundle poMsg = ResourceBundle.getBundle(bundle, locale,
                Thread.currentThread().getContextClassLoader());
        return poMsg.getString(LONG_ + identifier);
    }

    @Override
    public Command clone() {
        return new Builder()
                .setOptions(options)
                .setBundleName(bundle)
                .setIdentifier(identifier)
                .setCommandLine(commandLine)
                .setGui(gui)
                .setWeb(web)
                .build();
    }
    
    /**
     * Returns whether the command is available from the command line.
     * 
     * @return whether the command is available from the command line
     */
    public boolean isCommandLine() {
        return commandLine;
    }
    
    /**
     * Returns whether the command is available from the standalone GUI.
     * 
     * @return whether the command is available from the standalone GUI
     */
    public boolean isGUI() {
        return gui;
    }

    /**
     * Returns whether the command is available from the public web interface.
     * @return whether the command is available from the public web interface
     */
    public boolean isWeb() {
        return web;
    }
    
    /**
     * Obtain base name of resource bundle used.
     * 
     * @return base name of resource bundle used
     */
    public String getBundleName() {
        return bundle;
    }

    /**
     * Get options set of this command.
     * 
     * @return options set of this command
     */
    public Options getOptions() {
        return options;
    }
    
    Builder toBuilder() {
        return new Builder()
                .setBundleName(bundle)
                .setCommandLine(commandLine)
                .setGui(gui)
                .setIdentifier(identifier)
                .setOptions(options)
                .setWeb(web);
    }
}
