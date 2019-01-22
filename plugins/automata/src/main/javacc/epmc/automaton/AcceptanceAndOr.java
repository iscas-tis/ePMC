package epmc.automaton;

public final class AcceptanceAndOr implements Acceptance {
    private final static String AND = "&";
    private final static String OR = "|";
    private final static String LBRACE = "(";
    private final static String RBRACE = ")";
    private final AndOr andOr;
    private final Acceptance left;
    private final Acceptance right;

    public static boolean is(Acceptance acceptance) {
        return acceptance instanceof AcceptanceAndOr;
    }
    
    public static AcceptanceAndOr as(Acceptance acceptance) {
        if (is(acceptance)) {
            return (AcceptanceAndOr) acceptance;
        }
        return null;
    }
    
    AcceptanceAndOr(AndOr andOr, Acceptance left, Acceptance right) {
        assert andOr != null;
        assert left != null;
        assert right != null;
        this.andOr = andOr;
        this.left = left;
        this.right = right;
    }
    
    public AndOr getAndOr() {
        return andOr;
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
        String operatorString = andOr == AndOr.OR ? OR : AND;
        if (andOr == AndOr.AND
                && AcceptanceAndOr.is(left)
                && AcceptanceAndOr.as(left).getAndOr() == AndOr.OR) {
            leftString = addBraces(leftString);
        }
        if (andOr == AndOr.AND
                && AcceptanceAndOr.is(right)
                && AcceptanceAndOr.as(right).getAndOr() == AndOr.OR) {
            rightString = addBraces(rightString);
        }
        return leftString + operatorString + rightString;
    }
    
    private static String addBraces(String string) {
        return LBRACE + string + RBRACE;
    }
}
