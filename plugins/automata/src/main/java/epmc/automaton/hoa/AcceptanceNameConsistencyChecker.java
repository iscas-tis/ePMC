package epmc.automaton.hoa;

import static epmc.error.UtilError.ensure;

import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.options.Options;

final class AcceptanceNameConsistencyChecker {
    private final static String SPACE = " ";
    private final int numAcc;
    private final Acceptance acceptance;
    private final AcceptanceName acceptanceName;

    AcceptanceNameConsistencyChecker(int numAcc, Acceptance acceptance, AcceptanceName acceptanceName) {
        this.numAcc = numAcc;
        this.acceptance = acceptance;
        this.acceptanceName = acceptanceName;
        if (acceptanceName == null) {
            return;
        }
        String name = acceptanceName.getName();
        if (name.equals(HanoiHeader.BUCHI)) {
            checkBuechi();
        } else if (name.equals(HanoiHeader.CO_BUCHI)) {
            checkCoBuechi();
        } else if (name.equals(HanoiHeader.GENERALIZED_BUCHI)) {
            checkGeneralisedBuechi(false);
        } else if (name.equals(HanoiHeader.GENERALIZED_CO_BUCHI)) {
            checkGeneralisedBuechi(true);
        } else if (name.equals(HanoiHeader.STREETT)) {
            checkStreett();
        } else if (name.equals(HanoiHeader.RABIN)) {
            checkRabin();
        } else if (name.equals(HanoiHeader.GENERALIZED_RABIN)) {
            checkGeneralizedRabin();
        } else if (name.equals(HanoiHeader.PARITY)) {
            checkParity();
        } else if (name.equals(HanoiHeader.ALL)) {
            checkAll();
        } else if (name.equals(HanoiHeader.NONE)) {
            checkNone();
        } else {
            getLog().send(MessagesHoa.HOA_UNKNOWN_ACC_NAME,
                    acceptanceName, acceptance);
        }
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
        if (numAcc == 0) {
            if (coBuechi) {
                ensureAcceptance(AcceptanceBoolean.isFalse(acceptance));
            } else {
                ensureAcceptance(AcceptanceBoolean.isTrue(acceptance));
            }
        }
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
        if (numAcc == 0) {
            ensureAcceptance(AcceptanceBoolean.isTrue(acceptance));
        }
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
        if (numAcc == 0) {
            ensureAcceptance(AcceptanceBoolean.isFalse(acceptance));
        }
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
        ensureAcceptance(AcceptanceSet.getSet(acc.getLeft()) == i - 1);
        ensureAcceptance(AcceptanceSet.isInf(acc.getRight()));
        ensureAcceptance(!AcceptanceSet.isNegated(acc.getRight()));
        ensureAcceptance(AcceptanceSet.getSet(acc.getRight()) == i);
    }

    private void checkGeneralizedRabin() {
        Acceptance accPtr = acceptance;
        ensureAcceptance(acceptanceName.getNumParameters() >= 1);
        for (int paramNr = 0; paramNr < acceptanceName.getNumParameters(); paramNr++) {
            ensureAcceptance(acceptanceName.getParameterType(paramNr)
                    == AcceptanceNameParameterType.INTEGER);
        }
        int numPairs = acceptanceName.getParameterInteger(0);
        ensureAcceptance(acceptanceName.getNumParameters() - 1 == numPairs);
        int[] pairSizes = new int[numPairs];
        int startSet = 0;
        for (int pairNr = 0; pairNr < numPairs; pairNr++) {
            int pairSize = acceptanceName.getParameterInteger(pairNr + 1);
            pairSizes[pairNr] = pairSize;
            startSet += pairSize + 1;
        }
        startSet--;
        ensureAcceptance(acceptanceName.getParameterInteger(0) * 2 == numAcc);
        if (numAcc == 0) {
            ensureAcceptance(AcceptanceBoolean.isFalse(acceptance));
        }
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

    private void ensureGeneralizedRabinPair(AcceptanceAnd acc, int pairSize, int startSet) {
        while (pairSize > 1) {
            ensureAcceptance(AcceptanceSet.isInf(acc.getRight()));
            ensureAcceptance(!AcceptanceSet.isNegated(acc.getRight()));
            ensureAcceptance(AcceptanceSet.getSet(acc.getRight()) == startSet);
            acc = AcceptanceAnd.as(acc.getLeft());
            pairSize--;
        }
        ensureAcceptance(AcceptanceSet.isFin(acc.getLeft()));
        ensureAcceptance(!AcceptanceSet.isNegated(acc.getLeft()));
        ensureAcceptance(AcceptanceSet.getSet(acc.getLeft()) == startSet - 1);
        ensureAcceptance(AcceptanceSet.isInf(acc.getRight()));
        ensureAcceptance(!AcceptanceSet.isNegated(acc.getRight()));
        ensureAcceptance(AcceptanceSet.getSet(acc.getRight()) == startSet);
    }

    private void checkAll() {
        ensureAcceptance(acceptanceName.getNumParameters() == 0);
        ensureAcceptance(AcceptanceBoolean.isTrue(acceptance));
    }
    
    private void checkParity() {
        ensureAcceptance(acceptanceName.getNumParameters() == 3);
        ensureAcceptance(acceptanceName.getParameterType(0) == AcceptanceNameParameterType.IDENTIFIER);
        ensureAcceptance(acceptanceName.getParameterIdentifier(0).equals(HanoiHeader.PARITY_MIN)
                || acceptanceName.getParameterIdentifier(0).equals(HanoiHeader.PARITY_MAX));
        ensureAcceptance(acceptanceName.getParameterType(1) == AcceptanceNameParameterType.IDENTIFIER);
        ensureAcceptance(acceptanceName.getParameterIdentifier(1).equals(HanoiHeader.PARITY_ODD)
                || acceptanceName.getParameterIdentifier(1).equals(HanoiHeader.PARITY_EVEN));
        ensureAcceptance(acceptanceName.getParameterType(2) == AcceptanceNameParameterType.INTEGER);
        ensureAcceptance(acceptanceName.getParameterInteger(2) == numAcc);

        boolean minParity = acceptanceName.getParameterIdentifier(0).equals(HanoiHeader.PARITY_MIN);
        boolean oddParity = acceptanceName.getParameterIdentifier(1).equals(HanoiHeader.PARITY_ODD);
        
        if (numAcc == 0) {
            if (minParity == oddParity) {
                ensureAcceptance(AcceptanceBoolean.isFalse(acceptance));
            } else {
                ensureAcceptance(AcceptanceBoolean.isTrue(acceptance));
            }
        }
        
        boolean expectedFin = oddParity;
        boolean expectedAnd = minParity == oddParity;
        int expectedSet = minParity ? 0 : (numAcc - 1);
        Acceptance accPtr = acceptance;
        for (int i = 0; i < numAcc; i++) {
            Acceptance acc;
            if (i < numAcc - 1) {
                ensureAcceptance(!expectedAnd || AcceptanceAnd.is(accPtr));
                ensureAcceptance(expectedAnd || AcceptanceOr.is(accPtr));
                if (expectedAnd) {
                    acc = AcceptanceAnd.as(accPtr).getLeft();
                } else {
                    acc = AcceptanceOr.as(accPtr).getLeft();                    
                }
            } else {
                acc = acceptance;
            }
            ensureAcceptance(AcceptanceSet.is(acc));
            ensureAcceptance(expectedFin == AcceptanceSet.isFin(acc));
            ensureAcceptance(!AcceptanceSet.isNegated(acc));
            ensureAcceptance(AcceptanceSet.getSet(acc) == expectedSet);
            if (expectedAnd) {
                accPtr = AcceptanceAnd.as(accPtr).getRight();
            } else {
                accPtr = AcceptanceOr.as(accPtr).getRight();                    
            }
            expectedAnd = !expectedAnd;
            expectedFin = !expectedFin;
            expectedSet += minParity ? 1 : -1;
        }
    }

    private void checkNone() {
        ensureAcceptance(acceptanceName.getNumParameters() == 0);
        ensureAcceptance(AcceptanceBoolean.isFalse(acceptance));
    }
    
    private void ensureAcceptance(boolean condition) {
        ensure(condition, ProblemsHoa.HOA_INCONSISTENT_ACCEPTANCE_NAME,
                acceptanceName, numAcc + SPACE + acceptance);
    }
    
    private static Log getLog() {
        return Options.get().get(OptionsMessages.LOG);
    }
}
