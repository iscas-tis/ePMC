/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

package epmc.automaton.hoa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.util.BitSet;
import epmc.util.BitSetUnboundedLongArray;
import static epmc.error.UtilError.ensure;

// http://adl.github.io/hoaf/
public final class HanoiHeader {
    public final static String BUCHI = "Buchi";
    public final static String CO_BUCHI = "co-Buchi";
    public final static String GENERALIZED_BUCHI = "generalized-Buchi";
    public final static String GENERALIZED_CO_BUCHI = "generalized-co-Buchi";
    public final static String STREETT = "Streett";
    public final static String RABIN = "Rabin";
    public final static String GENERALIZED_RABIN = "generalized-Rabin";
    public final static String PARITY = "parity";
    public final static String PARITY_MIN = "min";
    public final static String PARITY_MAX = "max";
    public final static String PARITY_ODD = "odd";
    public final static String PARITY_EVEN = "even";
    public final static String ALL = "all";
    public final static String NONE = "none";
    private final static String SPACE = " ";
    
    private String name;
    private String toolName;
    private String toolVersion;
    private final Map<String,Expression> ap2expr;
    private final Map<String,Expression> anameToExpr = new LinkedHashMap<>();
    private int numStates = -1;
    private final BitSet startStates = new BitSetUnboundedLongArray();
    private int numAPs;
    private final List<Expression> aps = new ArrayList<>();
    private int numAcc;
    private final LinkedHashSet<String> properties = new LinkedHashSet<>();
    private final Set<String> propertiesReadonly = Collections.unmodifiableSet(properties);
    private Acceptance acceptance;
    private AcceptanceName acceptanceName;

    HanoiHeader(Map<String,Expression> ap2expr) {
        this.ap2expr = ap2expr;
    }

    void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getToolName() {
        return toolName;
    }

    void setToolVersion(String toolVersion) {
        this.toolVersion = toolVersion;
    }

    public String getToolVersion() {
        return toolVersion;
    }
    
    void addProperty(String property) {
        properties.add(property);
    }

    void setNumAccSets(int numAccSets) {
        this.numAcc = numAccSets;
    }
    
    public int getNumAccSets() {
        return numAcc;
    }
    
    void setAcceptance(Acceptance acceptance) {
        this.acceptance = acceptance;
    }
    
    public Acceptance getAcceptance() {
        return acceptance;
    }
    
    void setAcceptanceName(AcceptanceName acceptanceName) {
        this.acceptanceName = acceptanceName;
    }
    
    public AcceptanceName getAcceptanceName() {
        return acceptanceName;
    }
    
    public Set<String> getProperties() {
        return propertiesReadonly;
    }
    
    void setNumStates(int numStates) {
        this.numStates = numStates;
    }

    int getNumStates() {
        return numStates;
    }

    void setStartState(int startState) {
        startStates.set(startState);
    }

    BitSet getStartStates() {
        return startStates;
    }

    void setNumAPs(int numAPs) {
        this.numAPs = numAPs;
    }
    
    public int getNumAPs() {
        return numAPs;
    }
    
    void addAP(String name) {
        assert name != null;
        assert ap2expr == null || ap2expr.containsKey(name);
        if (ap2expr != null) {
            aps.add(ap2expr.get(name));
        } else {
            aps.add(new ExpressionIdentifierStandard.Builder()
                    .setName(name).build());
        }
    }

    Expression numberToIdentifier(int number) {
        assert number >= 0;
        assert number < aps.size();
        return aps.get(number);
    }

    void setNumAcc(int numAcc) {
        this.numAcc = numAcc;
    }

    public int getNumAcc() {
        return numAcc;
    }
    
    public List<Expression> getAps() {
        return aps;
    }
    
    void putAname(String aname, Expression expr) {
        assert aname != null;
        assert expr != null;
        anameToExpr.put(name, expr);
    }
    
    boolean containsAname(String name) {
        assert name != null;
        return anameToExpr.containsKey(name);
    }
    
    Expression aname2expr(String name) {
        return anameToExpr.get(name);
    }
    
    void checkAcceptanceName() {
        if (acceptanceName == null) {
            return;
        }
        String name = acceptanceName.getName();
        if (name.equals(BUCHI)) {
            checkBuechi();
        } else if (name.equals(CO_BUCHI)) {
            checkCoBuechi();
        } else if (name.equals(GENERALIZED_BUCHI)) {
            checkGeneralisedBuechi(false);
        } else if (name.equals(GENERALIZED_CO_BUCHI)) {
            checkGeneralisedBuechi(true);
        } else if (name.equals(STREETT)) {
            checkStreett();
        } else if (name.equals(RABIN)) {
            checkRabin();
        } else if (name.equals(GENERALIZED_RABIN)) {
            checkGeneralizedRabin();
        } else if (name.equals(PARITY)) {
            // TODO check parity
        } else if (name.equals(ALL)) {
            // TODO check all
        } else if (name.equals(NONE)) {
            // TODO check non
        } else {
            // TODO write warning about unknown automaton type
        }
        // TODO
    }

    private void checkBuechi() {
        ensureAcceptance(numAcc == 1);
        ensureAcceptance(acceptanceName.getNumParameters() == 0);
        ensureAcceptance(AcceptanceSet.is(acceptance));
        ensureAcceptance(!AcceptanceSet.as(acceptance).isNegated());
        ensureAcceptance(AcceptanceSet.as(acceptance).getInfFin() == InfFin.INF);
    }

    private void checkCoBuechi() {
        ensureAcceptance(numAcc == 1);
        ensureAcceptance(acceptanceName.getNumParameters() == 0);
        ensureAcceptance(AcceptanceSet.is(acceptance));
        ensureAcceptance(!AcceptanceSet.as(acceptance).isNegated());
        ensureAcceptance(AcceptanceSet.as(acceptance).getInfFin() == InfFin.FIN);            
    }

    private void checkGeneralisedBuechi(boolean coBuechi) {
        Acceptance accPtr = acceptance;
        ensureAcceptance(acceptanceName.getNumParameters() == 1);
        ensureAcceptance(acceptanceName.getParameterType(0) == AcceptanceNameParameterType.INTEGER);
        ensureAcceptance(acceptanceName.getParameterInteger(0) == numAcc);
        for (int i = numAcc - 1; i >= 0; i--) {
            ensureAcceptance(accPtr != null);
            AcceptanceSet acc = null;
            if (AcceptanceSet.is(accPtr)) {
                acc = AcceptanceSet.as(accPtr);
            } else if (AcceptanceAnd.is(accPtr)) {
                Acceptance accRight = AcceptanceAnd.as(accPtr).getRight();
                ensureAcceptance(AcceptanceSet.is(accRight));
                acc = AcceptanceSet.as(accRight);
            } else {
                ensureAcceptance(false);
            }
            ensureAcceptance(!acc.isNegated());
            ensureAcceptance(i == acc.getSet());
            if (!coBuechi) {
                ensureAcceptance(acc.getInfFin() == InfFin.INF);
            } else {
                ensureAcceptance(acc.getInfFin() == InfFin.FIN);
            }
            if (AcceptanceAnd.is(accPtr)) {
                accPtr = AcceptanceAnd.as(accPtr).getLeft();
            } else {
                accPtr = null;
            }
        }
    }

    private void checkStreett() {
        Acceptance accPtr = acceptance;
        ensureAcceptance(acceptanceName.getNumParameters() == 1);
        ensureAcceptance(acceptanceName.getParameterType(0) == AcceptanceNameParameterType.INTEGER);
        ensureAcceptance(acceptanceName.getParameterInteger(0) * 2 == numAcc);
        for (int i = numAcc - 1; i >= 0; i-= 2) {
            ensureAcceptance(accPtr != null);
            AcceptanceOr acc = null;
            if (AcceptanceOr.is(accPtr)) {
                acc = AcceptanceOr.as(accPtr);
            } else if (AcceptanceAnd.is(accPtr)) {
                Acceptance accRight = AcceptanceAnd.as(accPtr).getRight();
                ensureAcceptance(AcceptanceOr.is(accRight));
                acc = AcceptanceOr.as(accRight);
            } else {
                ensureAcceptance(false);
            }
            ensureStreettPair(acc, i);
            if (AcceptanceAnd.is(accPtr)) {
                accPtr = AcceptanceAnd.as(accPtr).getLeft();
            } else {
                accPtr = null;
            }
        }
    }

    private void ensureStreettPair(AcceptanceOr acc, int i) {
        ensureAcceptance(AcceptanceSet.isFin(acc.getLeft()));
        ensureAcceptance(!AcceptanceSet.isNegated(acc.getLeft()));
        ensureAcceptance(AcceptanceSet.as(acc.getLeft()).getSet() == i - 1);
        ensureAcceptance(AcceptanceSet.isInf(acc.getRight()));
        ensureAcceptance(!AcceptanceSet.isNegated(acc.getRight()));
        ensureAcceptance(AcceptanceSet.as(acc.getRight()).getSet() == i);
    }

    private void checkRabin() {
        Acceptance accPtr = acceptance;
        ensureAcceptance(acceptanceName.getNumParameters() == 1);
        ensureAcceptance(acceptanceName.getParameterType(0) == AcceptanceNameParameterType.INTEGER);
        ensureAcceptance(acceptanceName.getParameterInteger(0) * 2 == numAcc);
        for (int i = numAcc - 1; i >= 0; i-= 2) {
            ensureAcceptance(accPtr != null);
            AcceptanceAnd acc = null;
            if (AcceptanceAnd.is(accPtr)) {
                acc = AcceptanceAnd.as(accPtr);
            } else if (AcceptanceOr.is(accPtr)) {
                Acceptance accRight = AcceptanceOr.as(accPtr).getRight();
                ensureAcceptance(AcceptanceAnd.is(accRight));
                acc = AcceptanceAnd.as(accRight);
            } else {
                ensureAcceptance(false);
            }
            ensureRabinPair(acc, i);
            if (AcceptanceOr.is(accPtr)) {
                accPtr = AcceptanceOr.as(accPtr).getLeft();
            } else {
                accPtr = null;
            }
        }
    }

    private void ensureRabinPair(AcceptanceAnd acc, int i) {
        ensureAcceptance(AcceptanceSet.isFin(acc.getLeft()));
        ensureAcceptance(!AcceptanceSet.isNegated(acc.getLeft()));
        ensureAcceptance(AcceptanceSet.as(acc.getLeft()).getSet() == i - 1);
        ensureAcceptance(AcceptanceSet.isInf(acc.getRight()));
        ensureAcceptance(!AcceptanceSet.isNegated(acc.getRight()));
        ensureAcceptance(AcceptanceSet.as(acc.getRight()).getSet() == i);
    }

    private void checkGeneralizedRabin() {
        Acceptance accPtr = acceptance;
        ensureAcceptance(acceptanceName.getNumParameters() >= 1);
        for (int paramNr = 0; paramNr < acceptanceName.getNumParameters(); paramNr++) {
            ensureAcceptance(acceptanceName.getParameterType(paramNr)
                    == AcceptanceNameParameterType.INTEGER);
        }
        int numPairs = acceptanceName.getParameterInteger(0);
        ensureAcceptance(acceptanceName.getNumParameters() == numPairs);
        int[] pairSizes = new int[numPairs];
        int startSet = 0;
        for (int pairNr = 0; pairNr < numPairs; pairNr++) {
            int pairSize = acceptanceName.getParameterInteger(pairNr + 1);
            pairSizes[pairNr] = pairSize;
            startSet += pairSize + 1;
        }
        startSet--;
        ensureAcceptance(acceptanceName.getParameterInteger(0) * 2 == numAcc);
        for (int i = numPairs - 1; i >= 0; i--) {
            ensureAcceptance(accPtr != null);
            AcceptanceAnd acc = null;
            if (AcceptanceAnd.is(accPtr)) {
                acc = AcceptanceAnd.as(accPtr);
            } else if (AcceptanceOr.is(accPtr)) {
                Acceptance accRight = AcceptanceOr.as(accPtr).getRight();
                ensureAcceptance(AcceptanceAnd.is(accRight));
                acc = AcceptanceAnd.as(accRight);
            } else {
                ensureAcceptance(false);
            }
            ensureGeneralizedRabinPair(acc, pairSizes[i], startSet);
            startSet -= pairSizes[i] + 1;
            if (AcceptanceOr.is(accPtr)) {
                accPtr = AcceptanceOr.as(accPtr).getLeft();
            } else {
                accPtr = null;
            }
        }
    }

    // TODO adapt this to generalised rabin
    private void ensureGeneralizedRabinPair(AcceptanceAnd acc, int pairSize, int startSet) {
        ensureAcceptance(AcceptanceSet.isFin(acc.getLeft()));
        ensureAcceptance(!AcceptanceSet.isNegated(acc.getLeft()));
        ensureAcceptance(AcceptanceSet.as(acc.getLeft()).getSet() == pairSize - 1);
        ensureAcceptance(AcceptanceSet.isInf(acc.getRight()));
        ensureAcceptance(!AcceptanceSet.isNegated(acc.getRight()));
        ensureAcceptance(AcceptanceSet.as(acc.getRight()).getSet() == pairSize);
    }

    private void ensureAcceptance(boolean condition) {
        ensure(condition, ProblemsHoa.HOA_INCONSISTENT_ACCEPTANCE_NAME,
                acceptanceName, numAcc + SPACE + acceptance);
    }
}
