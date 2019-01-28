package epmc.automaton.hoa;

public final class AcceptanceAnd implements Acceptance {
    private final static String AND = "&";
    private final static String LBRACE = "(";
    private final static String RBRACE = ")";
    private final Acceptance left;
    private final Acceptance right;

    public static boolean is(Acceptance acceptance) {
        return acceptance instanceof AcceptanceAnd;
    }
    
    public static AcceptanceAnd as(Acceptance acceptance) {
        if (is(acceptance)) {
            return (AcceptanceAnd) acceptance;
        }
        return null;
    }
    
    AcceptanceAnd(Acceptance left, Acceptance right) {
        assert left != null;
        assert right != null;
        this.left = left;
        this.right = right;
    }
    
    public Acceptance getLeft() {
        return left;
    }
    
    public Acceptance getRight() {
        return right;
    }
    
    @Override
    public String toString() {
        String leftString = left.toString();
        String rightString = right.toString();
        if (AcceptanceOr.is(left)) {
            leftString = addBraces(leftString);
        }
        if (AcceptanceOr.is(right)) {
            rightString = addBraces(rightString);
        }
        return leftString + AND + rightString;
    }
    
    private static String addBraces(String string) {
        return LBRACE + string + RBRACE;
    }
}
