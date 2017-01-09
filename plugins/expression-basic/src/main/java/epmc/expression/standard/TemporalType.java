package epmc.expression.standard;

public enum TemporalType {
    NEXT("X"),
    UNTIL("U"),
    RELEASE("R"),
    GLOBALLY("G"),
    FINALLY("F")
    ;

    private final String string;

    private TemporalType(String string) {
        this.string = string;
    }

    @Override
    public String toString() {
        return string;
    }
}
