package epmc.automaton.hoa;

public final class AcceptanceOr implements Acceptance {
    private final static String OR = "|";
    private final Acceptance left;
    private final Acceptance right;

    public static boolean is(Acceptance acceptance) {
        return acceptance instanceof AcceptanceOr;
    }
    
    public static AcceptanceOr as(Acceptance acceptance) {
        if (is(acceptance)) {
            return (AcceptanceOr) acceptance;
        }
        return null;
    }
    
    AcceptanceOr(Acceptance left, Acceptance right) {
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
        return leftString + OR + rightString;
    }
}
