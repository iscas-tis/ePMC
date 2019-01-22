package epmc.automaton.hoa;

public final class AcceptanceBoolean implements Acceptance {
    public final static String TRUE = "t";
    public final static String FALSE = "f";
    private boolean value;
    
    public static boolean is(Acceptance acceptance) {
        return acceptance instanceof AcceptanceBoolean;
    }

    public static AcceptanceBoolean as(Acceptance acceptance) {
        if (is(acceptance)) {
            return (AcceptanceBoolean) acceptance;
        }
        return null;
    }


    public AcceptanceBoolean(boolean value) {
        this.value = value;
    }

    public boolean isValue() {
        return value;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(value ? TRUE : FALSE);
        return builder.toString();
    }
}
