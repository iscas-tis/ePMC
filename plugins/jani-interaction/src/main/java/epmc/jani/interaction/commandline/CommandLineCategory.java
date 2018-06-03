package epmc.jani.interaction.commandline;

public final class CommandLineCategory {
    private final String identifier;
    private final String shortDescription;
    private final CommandLineCategory parent;

    public CommandLineCategory(String identifier, String shortDescription, 
            CommandLineCategory parent) {
        assert identifier != null;
        assert shortDescription != null;
        this.identifier = identifier;
        this.shortDescription = shortDescription;
        this.parent = parent;
    }
    
    public String getIdentifier() {
        return identifier;
    }
    
    public String getShortDescription() {
        return shortDescription;
    }
    
    public CommandLineCategory getParent() {
        return parent;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CommandLineCategory)) {
            return false;
        }
        CommandLineCategory other = (CommandLineCategory) obj;
        if (!identifier.equals(other.identifier)) {
            return false;
        }
        if (!shortDescription.equals(other.shortDescription)) {
            return false;
        }
        if ((parent == null) != (other.parent == null)) {
            return false;
        }
        if (parent != null && !parent.equals(other.parent)) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = identifier.hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = shortDescription.hashCode() + (hash << 6) + (hash << 16) - hash;
        if (parent != null) {
            hash = parent.hashCode() + (hash << 6) + (hash << 16) - hash;            
        }
        return hash;
    }
}
