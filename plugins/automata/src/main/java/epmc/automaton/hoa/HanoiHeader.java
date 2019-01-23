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

public final class HanoiHeader {
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
        if (name.equals("Buchi")) {
            checkBuechi();
        } else if (name.equals("co-Buchi")) {
            checkCoBuechi();
        } else if (name.equals("generalized-Buchi")) {
            checkGeneralisedBuechi(false);
        } else if (name.equals("generalized-co-Buchi")) {
            checkGeneralisedBuechi(true);            
        } else if (name.equals("Streett")) {
            
        } else if (name.equals("Rabin")) {
            
        } else if (name.equals("generalized-Rabin")) {
            
        } else if (name.equals("all")) {
            
        } else if (name.equals("none")) {
            
        } else {
            
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
            } else if (AcceptanceAndOr.is(accPtr)) {
                ensureAcceptance(AcceptanceAndOr.as(accPtr).getAndOr() == AndOr.AND);
                Acceptance accRight = AcceptanceAndOr.as(accPtr).getRight();
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
            if (AcceptanceAndOr.is(accPtr)) {
                ensureAcceptance(AcceptanceAndOr.as(accPtr).getAndOr() == AndOr.AND);
                accPtr = AcceptanceAndOr.as(accPtr).getLeft();
            } else {
                accPtr = null;
            }
        }
    }

    private void checkStreett() {
        Acceptance accPtr = acceptance;
        ensureAcceptance(acceptanceName.getNumParameters() == 1);
        ensureAcceptance(acceptanceName.getParameterType(0) == AcceptanceNameParameterType.INTEGER);
        ensureAcceptance(acceptanceName.getParameterInteger(0) == numAcc);
        for (int i = numAcc - 1; i >= 0; i--) {
            ensureAcceptance(accPtr != null);
            AcceptanceAndOr acc = null;
            if (AcceptanceAndOr.is(accPtr) && AcceptanceAndOr.as(accPtr).getAndOr() == AndOr.OR) {
                acc = AcceptanceAndOr.as(accPtr);
            } else if (AcceptanceAndOr.is(accPtr)) {
                ensureAcceptance(AcceptanceAndOr.as(accPtr).getAndOr() == AndOr.AND);
                Acceptance accRight = AcceptanceAndOr.as(accPtr).getRight();
                ensureAcceptance(AcceptanceSet.is(accRight));
                acc = AcceptanceAndOr.as(accRight);
            } else {
                ensureAcceptance(false);
            }
            ensureStreettPair(acc, i);
            if (AcceptanceAndOr.is(accPtr)) {
                ensureAcceptance(AcceptanceAndOr.as(accPtr).getAndOr() == AndOr.AND);
                accPtr = AcceptanceAndOr.as(accPtr).getLeft();
            } else {
                accPtr = null;
            }
        }
    }

    private void ensureStreettPair(AcceptanceAndOr acc, int i) {
        // TODO
        
        // TODO Auto-generated method stub
        
    }

    private void ensureAcceptance(boolean condition) {
        ensure(condition, ProblemsHoa.HOA_INCONSISTENT_ACCEPTANCE_NAME,
                acceptanceName, numAcc + " " + acceptance);
    }
}
