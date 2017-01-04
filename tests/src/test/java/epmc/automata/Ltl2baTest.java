package epmc.automata;

//import epmc.automata.BDDAutomatonState;

// TODO change to assertThat

public class Ltl2baTest {
/*
    private LtlToBa stringToBa(String string, LtlToBa.AbstractionType absType) throws EPMCException {
        string = "P=? [" + string + "];";
        PropertyList list = new PropertyList();
        list.addProperty(new Property(string, ""));
        list.expandAndCheckWithDefinedCheck();
        Expression expr = list.getPropertyExpression(list.iterator().next());
        expr = ((ExpressionQuant) expr).getPathProperty();
        return new LtlToBa(expr, absType, LtlToBa.BuildType.Explicit, null);
    }

    private List<Expression> strs2exprs(String[] strings) {
        ArrayList<Expression> result = new ArrayList<>();
        for (int strNr = 0; strNr < strings.length; strNr++) {
            result.add(new ExpressionStateQuery(strings[strNr]));
        }
        return result;
    }
    
    @Test
    public void falseTest() throws EPMCException {
        LtlToBa ba = stringToBa("false", AbstractionType.LeaveNonDet);
        assertEquals(0, ba.getNumLabels());
        BDDAutomatonState init = ba.getInitState();
        List<Expression> expressions = new ArrayList<Expression>();
        BitSet active = new BitSet();
        ba.queryState(expressions, active, init);
        assertEquals(0, ba.stateEnd() - ba.stateBegin());
    }

    @Test
    public void notFalseTest() throws EPMCException {
        LtlToBa ba = stringToBa("!false", AbstractionType.LeaveNonDet);
        assertEquals(0, ba.getNumLabels());
        BDDAutomatonState init = ba.getInitState();
        ArrayList<Expression> expressions = new ArrayList<>();
        BitSet active = new BitSet();
        ba.queryState(expressions, active, init);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        BDDAutomatonState next = ba.succState(0);
        assertEquals(init, next);
    }    
    
    @Test
    public void trueTest() throws EPMCException {
        LtlToBa ba = stringToBa("true", AbstractionType.LeaveNonDet);
        assertEquals(0, ba.getNumLabels());
        BDDAutomatonState init = ba.getInitState();
        ArrayList<Expression> expressions = new ArrayList<>();
        BitSet active = new BitSet();
        ba.queryState(expressions, active, init);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        BDDAutomatonState next = ba.succState(0);
        assertEquals(init, next);
    }

    @Test
    public void notTrueTest() throws EPMCException {
        LtlToBa ba = stringToBa("!true", AbstractionType.LeaveNonDet);
        assertEquals(0, ba.getNumLabels());
        BDDAutomatonState init = ba.getInitState();
        List<Expression> expressions = new ArrayList<Expression>();
        BitSet active = new BitSet();
        ba.queryState(expressions, active, init);
        assertEquals(0, ba.getNumLabels());
        assertEquals(0, ba.stateEnd() - ba.stateBegin());
    }

    @Test
    public void actionTest() throws EPMCException {
        LtlToBa ba = stringToBa("a", AbstractionType.LeaveNonDet);
        assertEquals(0, ba.getNumLabels());        
        BDDAutomatonState init = ba.getInitState();
        String[] exprStrings = {"a"};
        List<Expression> expressions = strs2exprs(exprStrings);
        BitSet active = new BitSet();
        ba.queryState(expressions, active, init);
        assertEquals(0, ba.stateEnd() - ba.stateBegin());
        active.set(0);
        ba.queryState(expressions, active, init);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        BDDAutomatonState next = ba.succState(0);
        assertTrue(!init.equals(next));
        active.set(0, false);
        ba.queryState(expressions, active, next);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        assertEquals(next, ba.succState(0));
        active.set(1);
        ba.queryState(expressions, active, next);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        assertEquals(next, ba.succState(0));
    }

    @Test
    public void notActionTest() throws EPMCException {
        LtlToBa ba = stringToBa("!a", AbstractionType.LeaveNonDet);
        assertEquals(0, ba.getNumLabels());        
        BDDAutomatonState init = ba.getInitState();
        String[] exprStrings = {"a"};
        List<Expression> expressions = strs2exprs(exprStrings);
        BitSet active = new BitSet();
        active.set(0);
        ba.queryState(expressions, active, init);
        assertEquals(0, ba.stateEnd() - ba.stateBegin());
        active.set(0, false);
        ba.queryState(expressions, active, init);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        BDDAutomatonState next = ba.succState(0);
        assertTrue(!init.equals(next));
        active.set(0, false);
        ba.queryState(expressions, active, next);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        assertEquals(next, ba.succState(0));
        active.set(1);
        ba.queryState(expressions, active, next);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        assertEquals(next, ba.succState(0));
    }
    
    @Test
    public void nextFalseTest() throws EPMCException {
        LtlToBa ba = stringToBa("X(false)", AbstractionType.LeaveNonDet);
        assertEquals(0, ba.getNumLabels());        
        BDDAutomatonState init = ba.getInitState();
        List<Expression> expressions = new ArrayList<>();
        BitSet active = new BitSet();
        ba.queryState(expressions, active, init);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        BDDAutomatonState next = ba.succState(0);
        ba.queryState(expressions, active, next);
        assertEquals(0, ba.stateEnd() - ba.stateBegin());        
    }

    @Test
    public void nextTrueTest() throws EPMCException {
        LtlToBa ba = stringToBa("X(true)", AbstractionType.LeaveNonDet);
        assertEquals(0, ba.getNumLabels());        
        BDDAutomatonState init = ba.getInitState();
        List<Expression> expressions = new ArrayList<>();
        BitSet active = new BitSet();
        ba.queryState(expressions, active, init);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        BDDAutomatonState next = ba.succState(0);
        ba.queryState(expressions, active, next);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());        
    }
    
    @Test
    public void notNextFalseTrue() throws EPMCException {
        LtlToBa ba = stringToBa("!(X(true))", AbstractionType.LeaveNonDet);
        assertEquals(0, ba.getNumLabels());        
        BDDAutomatonState init = ba.getInitState();
        List<Expression> expressions = new ArrayList<>();
        BitSet active = new BitSet();
        ba.queryState(expressions, active, init);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        BDDAutomatonState next = ba.succState(0);
        ba.queryState(expressions, active, next);
        assertEquals(0, ba.stateEnd() - ba.stateBegin());        
    }

    @Test
    public void notNextFalseTest() throws EPMCException {
        LtlToBa ba = stringToBa("!(X(false))", AbstractionType.LeaveNonDet);
        assertEquals(0, ba.getNumLabels());        
        BDDAutomatonState init = ba.getInitState();
        List<Expression> expressions = new ArrayList<>();
        BitSet active = new BitSet();
        ba.queryState(expressions, active, init);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        BDDAutomatonState next = ba.succState(0);
        ba.queryState(expressions, active, next);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());        
    }
    
    @Test
    public void nextNotTrueTest() throws EPMCException {
        LtlToBa ba = stringToBa("X(!true)", AbstractionType.LeaveNonDet);
        assertEquals(0, ba.getNumLabels());        
        BDDAutomatonState init = ba.getInitState();
        List<Expression> expressions = new ArrayList<>();
        BitSet active = new BitSet();
        ba.queryState(expressions, active, init);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        BDDAutomatonState next = ba.succState(0);
        ba.queryState(expressions, active, next);
        assertEquals(0, ba.stateEnd() - ba.stateBegin());        
    }

    @Test
    public void nextNotFalseTest() throws EPMCException {
        LtlToBa ba = stringToBa("X(!false)", AbstractionType.LeaveNonDet);
        assertEquals(0, ba.getNumLabels());        
        BDDAutomatonState init = ba.getInitState();
        List<Expression> expressions = new ArrayList<>();
        BitSet active = new BitSet();
        ba.queryState(expressions, active, init);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        BDDAutomatonState next = ba.succState(0);
        ba.queryState(expressions, active, next);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());        
    }


    @Test
    public void nextLabelTest() throws EPMCException {
        LtlToBa ba = stringToBa("X(a)", AbstractionType.LeaveNonDet);
        assertEquals(0, ba.getNumLabels());        
        BDDAutomatonState init = ba.getInitState();
        String[] exprStrings = {"a"};
        List<Expression> expressions = strs2exprs(exprStrings);
        BitSet active = new BitSet();
        ba.queryState(expressions, active, init);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        BDDAutomatonState next = ba.succState(0);
        active.set(0);
        ba.queryState(expressions, active, init);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        BDDAutomatonState next2 = ba.succState(0);
        assertEquals(next, next2);
        active.set(0, false);
        ba.queryState(expressions, active, next);
        assertEquals(0, ba.stateEnd() - ba.stateBegin());
        active.set(0, true);
        ba.queryState(expressions, active, next);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        next = ba.succState(0);
        active.set(0, false);
        ba.queryState(expressions, active, next);
        assertEquals(next, ba.succState(0));
        active.set(0, true);
        ba.queryState(expressions, active, next);
        assertEquals(next, ba.succState(0));
    }

    @Test
    public void notNextLabelTest() throws EPMCException {
        LtlToBa ba = stringToBa("!(X(a))", AbstractionType.LeaveNonDet);
        assertEquals(0, ba.getNumLabels());        
        BDDAutomatonState init = ba.getInitState();
        String[] exprStrings = {"a"};
        List<Expression> expressions = strs2exprs(exprStrings);
        BitSet active = new BitSet();
        active.set(0, true);
        ba.queryState(expressions, active, init);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        BDDAutomatonState next = ba.succState(0);
        active.set(0, false);
        ba.queryState(expressions, active, init);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        BDDAutomatonState next2 = ba.succState(0);
        assertEquals(next, next2);
        active.set(0, true);
        ba.queryState(expressions, active, next);
        assertEquals(0, ba.stateEnd() - ba.stateBegin());
        active.set(0, false);
        ba.queryState(expressions, active, next);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        next = ba.succState(0);
        active.set(0, true);
        ba.queryState(expressions, active, next);
        assertEquals(next, ba.succState(0));
        active.set(0, false);
        ba.queryState(expressions, active, next);
        assertEquals(next, ba.succState(0));
    }
    
    @Test
    public void nextNotLabelTest() throws EPMCException {
        LtlToBa ba = stringToBa("X(!a)", AbstractionType.LeaveNonDet);
        assertEquals(0, ba.getNumLabels());        
        BDDAutomatonState init = ba.getInitState();
        String[] exprStrings = {"a"};
        List<Expression> expressions = strs2exprs(exprStrings);
        BitSet active = new BitSet();
        active.set(0, true);
        ba.queryState(expressions, active, init);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        BDDAutomatonState next = ba.succState(0);
        active.set(0, false);
        ba.queryState(expressions, active, init);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        BDDAutomatonState next2 = ba.succState(0);
        assertEquals(next, next2);
        active.set(0, true);
        ba.queryState(expressions, active, next);
        assertEquals(0, ba.stateEnd() - ba.stateBegin());
        active.set(0, false);
        ba.queryState(expressions, active, next);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        next = ba.succState(0);
        active.set(0, true);
        ba.queryState(expressions, active, next);
        assertEquals(next, ba.succState(0));
        active.set(0, false);
        ba.queryState(expressions, active, next);
        assertEquals(next, ba.succState(0));
    }

    @Test
    public void trueUntilTrueTest() throws EPMCException {
        LtlToBa ba = stringToBa("true U true", AbstractionType.LeaveNonDet);
        assertEquals(1, ba.getNumLabels());
        BDDAutomatonState init = ba.getInitState();
        ArrayList<Expression> expressions = new ArrayList<>();
        BitSet active = new BitSet();
        ba.queryState(expressions, active, init);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        BDDAutomatonState next = ba.succState(0);
        assertTrue(ba.succLabel(0).isLabel(0));
        ba.queryState(expressions, active, next);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        assertEquals(ba.succState(0), next);
        assertTrue(ba.succLabel(0).isLabel(0));
    }

    @Test
    public void trueUntilFalseTest() throws EPMCException {
        LtlToBa ba = stringToBa("true U false", AbstractionType.LeaveNonDet);
        assertEquals(1, ba.getNumLabels());
        BDDAutomatonState init = ba.getInitState();
        ArrayList<Expression> expressions = new ArrayList<>();
        BitSet active = new BitSet();
        ba.queryState(expressions, active, init);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        BDDAutomatonState next = ba.succState(0);
        ba.queryState(expressions, active, next);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        assertEquals(ba.succState(0), next);
        assertFalse(ba.succLabel(0).isLabel(0));
    }

    @Test
    public void falseUntilTrueTest() throws EPMCException {
        LtlToBa ba = stringToBa("false U true", AbstractionType.LeaveNonDet);
        assertEquals(1, ba.getNumLabels());
        BDDAutomatonState init = ba.getInitState();
        ArrayList<Expression> expressions = new ArrayList<>();
        BitSet active = new BitSet();
        ba.queryState(expressions, active, init);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        BDDAutomatonState next = ba.succState(0);
        ba.queryState(expressions, active, next);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        assertEquals(ba.succState(0), next);
        assertTrue(ba.succLabel(0).isLabel(0));
    }

    @Test
    public void falseUntilFalseTest() throws EPMCException {
        LtlToBa ba = stringToBa("false U false", AbstractionType.LeaveNonDet);
        assertEquals(1, ba.getNumLabels());
        BDDAutomatonState init = ba.getInitState();
        ArrayList<Expression> expressions = new ArrayList<>();
        BitSet active = new BitSet();
        ba.queryState(expressions, active, init);
        assertEquals(0, ba.stateEnd() - ba.stateBegin());
    }

    @Test
    public void labelUntilLabelTest() throws EPMCException {
        LtlToBa ba = stringToBa("a U b", AbstractionType.LeaveNonDet);
        assertEquals(1, ba.getNumLabels());
        BDDAutomatonState init = ba.getInitState();
        String[] exprStrings = {"a", "b"};
        List<Expression> expressions = strs2exprs(exprStrings);
        BitSet active = new BitSet();
        ba.queryState(expressions, active, init);
        assertEquals(0, ba.stateEnd() - ba.stateBegin());
        active.set(0, true);
        ba.queryState(expressions, active, init);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        assertEquals(ba.succState(0), init);
        assertFalse(ba.succLabel(0).isLabel(0));
        active.set(0, false);
        active.set(1, true);
        ba.queryState(expressions, active, init);
        BDDAutomatonState trueState = ba.succState(0);
        assertTrue(ba.succLabel(0).isLabel(0));
        active.set(1, true);
        ba.queryState(expressions, active, init);
        BDDAutomatonState trueState2 = ba.succState(0);
        assertTrue(ba.succLabel(0).isLabel(0));
        assertEquals(trueState, trueState2);
        active.clear();
        ba.queryState(expressions, active, trueState);
        assertTrue(ba.succLabel(0).isLabel(0));
        assertEquals(trueState, ba.succState(0));
        active.set(0, true);
        ba.queryState(expressions, active, trueState);
        assertTrue(ba.succLabel(0).isLabel(0));
        assertEquals(trueState, ba.succState(0));
        active.set(0, false);
        active.set(1, true);
        ba.queryState(expressions, active, trueState);
        assertTrue(ba.succLabel(0).isLabel(0));
        assertEquals(trueState, ba.succState(0));
        active.set(0, true);
        ba.queryState(expressions, active, trueState);
        assertTrue(ba.succLabel(0).isLabel(0));
        assertEquals(trueState, ba.succState(0));
    }

    @Test
    public void labelReleaseLabelTest() throws EPMCException {
        LtlToBa ba = stringToBa("a R b", AbstractionType.LeaveNonDet);
        assertEquals(0, ba.getNumLabels());
        BDDAutomatonState init = ba.getInitState();
        String[] exprStrings = {"a", "b"};
        List<Expression> expressions = strs2exprs(exprStrings);
        BitSet active = new BitSet();
        ba.queryState(expressions, active, init);
        assertEquals(0, ba.stateEnd() - ba.stateBegin());
        active.set(0, true);
        ba.queryState(expressions, active, init);
        assertEquals(0, ba.stateEnd() - ba.stateBegin());
        active.set(0, false);
        active.set(1, true);
        ba.queryState(expressions, active, init);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        assertEquals(ba.succState(0), init);
        active.set(0, true);
        ba.queryState(expressions, active, init);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        BDDAutomatonState trueState = ba.succState(0);
        active.clear();
        ba.queryState(expressions, active, trueState);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        assertEquals(trueState, ba.succState(0));
        active.set(0, true);
        ba.queryState(expressions, active, trueState);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        assertEquals(trueState, ba.succState(0));
        active.clear();
        active.set(1, true);
        ba.queryState(expressions, active, trueState);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        assertEquals(trueState, ba.succState(0));
        active.set(0, true);
        ba.queryState(expressions, active, trueState);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        assertEquals(trueState, ba.succState(0));
    }
    
    @Test
    public void eventuallyAlwaysTest() throws EPMCException {
        LtlToBa ba = stringToBa("true U (false R a)", AbstractionType.LeaveNonDet);
        assertEquals(1, ba.getNumLabels());
        BDDAutomatonState init = ba.getInitState();
        String[] exprStrings = {"a"};
        List<Expression> expressions = strs2exprs(exprStrings);
        BitSet active = new BitSet();
        ba.queryState(expressions, active, init);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        assertEquals(init, ba.succState(0));
        assertFalse(ba.succLabel(0).isLabel(0));
        active.set(0, true);
        ba.queryState(expressions, active, init);
        assertEquals(2, ba.stateEnd() - ba.stateBegin());
        BuechiLabeling lab = ba.succLabel(0);
        BDDAutomatonState notYet = null;
        BDDAutomatonState nowAlways = null;
        BuechiLabeling toNotYet = null;
        BuechiLabeling toNowAlways = null;
        if (!lab.isLabel(0)) {
            notYet = ba.succState(0);
            toNotYet = ba.succLabel(0);
            nowAlways = ba.succState(1);
            toNowAlways = ba.succLabel(1);
        } else {
            notYet = ba.succState(1);
            toNotYet = ba.succLabel(1);
            nowAlways = ba.succState(0);
            toNowAlways = ba.succLabel(0);            
        }
        assertFalse(toNotYet.isLabel(0));
        assertTrue(toNowAlways.isLabel(0));
        active.clear();
        ba.queryState(expressions, active, notYet);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        assertEquals(init, ba.succState(0));
        assertFalse(ba.succLabel(0).isLabel(0));
        active.set(0, true);
        ba.queryState(expressions, active, notYet);
        assertEquals(2, ba.stateEnd() - ba.stateBegin());
        if (!ba.succLabel(0).isLabel(0)) {
            assertFalse(ba.succLabel(0).isLabel(0));
            assertEquals(ba.succState(0), notYet);
            assertTrue(ba.succLabel(1).isLabel(0));
            assertEquals(ba.succState(1), nowAlways);            
        } else {
            assertFalse(ba.succLabel(1).isLabel(0));
            assertEquals(ba.succState(1), notYet);
            assertTrue(ba.succLabel(0).isLabel(0));
            assertEquals(ba.succState(0), nowAlways);                        
        }
        active.clear();
        ba.queryState(expressions, active, nowAlways);
        assertEquals(0, ba.stateEnd() - ba.stateBegin());
        active.set(0, true);
        ba.queryState(expressions, active, nowAlways);
        assertEquals(1, ba.stateEnd() - ba.stateBegin());
        assertEquals(nowAlways, ba.succState(0));
        assertTrue(ba.succLabel(0).isLabel(0));
    }

    // TODO continue writing tests
    // FG(a)
    // X(a) & X(b)
    // X(a) | X(b)
    // also for under and overapproximation
    // transformation to normal negation form
     * */
     
}
