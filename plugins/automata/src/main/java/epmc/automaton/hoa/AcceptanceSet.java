package epmc.automaton.hoa;

public final class AcceptanceSet implements Acceptance {
    public final static String FIN = "fin(";
    public final static String INF = "inf(";
    public final static String NEG = "!";
    public final static String END = ")";
    private final InfFin infFin;
    private final boolean negated;
    private final int set;
    

    public static boolean is(Acceptance acceptance) {
        return acceptance instanceof AcceptanceSet;
    }

    public static AcceptanceSet as(Acceptance acceptance) {
        if (is(acceptance)) {
            return (AcceptanceSet) acceptance;
        }
        return null;
    }

    public static boolean isFin(Acceptance acceptance) {
        if (!is(acceptance)) {
            return false;
        }
        AcceptanceSet acceptanceSet = as(acceptance);
        return acceptanceSet.getInfFin() == InfFin.FIN;
    }

    public static boolean isInf(Acceptance acceptance) {
        if (!is(acceptance)) {
            return false;
        }
        AcceptanceSet acceptanceSet = as(acceptance);
        return acceptanceSet.getInfFin() == InfFin.INF;
    }

    public static boolean isNegated(Acceptance acceptance) {
        if (!is(acceptance)) {
            return false;
        }
        AcceptanceSet acceptanceSet = as(acceptance);
        return acceptanceSet.isNegated();
    }

    public static int getSet(Acceptance acceptance) {
        if (!is(acceptance)) {
            return -1;
        }
        return as(acceptance).getSet();
    }
    
    public AcceptanceSet(InfFin infFin, boolean negated, int set) {
        assert infFin != null;
        assert set >= 0 : set;
        this.infFin = infFin;
        this.negated = negated;
        this.set = set;
    }
    
    public InfFin getInfFin() {
        return infFin;
    }
    
    public boolean isNegated() {
        return negated;
    }
    
    public int getSet() {
        return set;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (infFin == InfFin.FIN) {
            builder.append(FIN);
        } else if (infFin == InfFin.INF) {
            builder.append(INF);            
        }
        if (negated) {
            builder.append(NEG);
        }
        builder.append(set);
        builder.append(END);
        return builder.toString();
    }
}
