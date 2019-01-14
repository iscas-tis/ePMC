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

package epmc.dd;

import epmc.value.Value;
import epmc.value.ValueBoolean;

public final class Walker {
    private final ContextDD contextDD;
    private final DD node;
    private final LibraryDD lowLevel;
    private final boolean autoComplement;
    private int historySize;
    private final long[] history;
    private boolean invalid;

    Walker(DD node, LibraryDD lowLevel, long uniqueId, boolean autoComplement) {
        assert lowLevel != null;
        this.contextDD = lowLevel.getContextDD();
        this.node = node;
        this.lowLevel = lowLevel;
        this.autoComplement = autoComplement && lowLevel.hasInverterArcs();
        this.history = new long[contextDD.numVariables() + 1];
        this.history[0] = lowLevel.getWalker(uniqueId);
        historySize++;
    }

    Walker(DD node, boolean autoComplement) {
        this(node, getLowLevel(node), getUniqueId(node), autoComplement);
    }

    private static LibraryDD getLowLevel(DD node) {
        assert node != null;
        return node.getLowLevel();
    }

    private static long getUniqueId(DD node) {
        assert node != null;
        return node.uniqueId();
    }

    public void low() {
        assert assertAlive();
        assert !isLeaf();
        boolean complement = false;
        if (autoComplement) {
            complement = lowLevel.walkerIsComplement(current());
        }
        history[historySize] = lowLevel.walkerLow(lowLevel.walkerRegular(current()));
        historySize++;
        if (complement) {
            history[historySize - 1] = lowLevel.walkerComplement(history[historySize - 1]);
        }
    }

    public void high() {
        assert assertAlive();
        assert !isLeaf();
        boolean complement = false;
        if (autoComplement) {
            complement = lowLevel.walkerIsComplement(current());
        }
        history[historySize] = lowLevel.walkerHigh(lowLevel.walkerRegular(current()));
        historySize++;
        if (complement) {
            history[historySize - 1] = lowLevel.walkerComplement(history[historySize - 1]);
        }
    }

    public void back() {
        assert assertAlive();
        assert historySize > 1;
        historySize--;
    }

    public int variable() {
        assert assertAlive();
        assert !isLeaf();
        return lowLevel.walkerVariable(lowLevel.walkerRegular(current()));
    }

    public boolean isLeaf() {
        assert assertAlive();
        return lowLevel.walkerIsLeaf(lowLevel.walkerRegular(current()));
    }

    public Value value() {
        assert assertAlive();
        assert isLeaf();
        return lowLevel.walkerValue(current());
    }

    public boolean isFalse() {
        assert assertAlive();
        return isLeaf() && ValueBoolean.isFalse(value());
    }

    public boolean isTrue() {
        assert assertAlive();
        return isLeaf() && ValueBoolean.isTrue(value());
    }

    public long uniqueId() {
        assert assertAlive();
        return current();
    }

    public void regular() {
        assert assertAlive();
        if (!autoComplement) {
            history[historySize - 1] = lowLevel.walkerRegular(current());
        }
    }

    public boolean isComplement() {
        assert assertAlive();
        if (autoComplement) {
            return false;
        } else {
            return lowLevel.walkerIsComplement(current());
        }
    }

    public void complement() {
        assert assertAlive();
        if (!autoComplement) {
            history[historySize - 1] = lowLevel.walkerComplement(current());
        }
    }

    /* helper functions */

    private String buildClosedString() {
        return "DD of walker has already been closed at "
                + node.buildCloseTraceString();
    }

    private long current() {
        return history[historySize - 1];
    }

    private boolean assertAlive() {
        assert node == null || node.alive() : buildClosedString();
        assert !invalid : "walker has been invalidated, e.g. due to (possible) "
        + "variable reordering";
        return true;
    }

    void invalidate() {
        invalid = true;
    }    
}
