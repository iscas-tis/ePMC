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

package epmc.jani.extensions.derivedoperators;

import epmc.jani.model.JANINode;
import epmc.jani.model.JANIOperators;
import epmc.jani.model.ModelExtension;
import epmc.jani.model.ModelJANI;
import epmc.operator.OperatorAbs;
import epmc.operator.OperatorGe;
import epmc.operator.OperatorGt;
import epmc.operator.OperatorImplies;
import epmc.operator.OperatorMax;
import epmc.operator.OperatorMin;

public final class ModelExtensionDerivedOperators implements ModelExtension {
    /** Identifies an IMPLIES operator. */
    private final static String OPERATOR_IMPLIES = "⇒";
    /** Identifies a greater-than operator. */
    private final static String OPERATOR_GT = ">";
    /** Identifies a greater-or-equal operator. */
    private final static String OPERATOR_GE = "≥";
    /** Identifies a minimum operator. */
    private final static String OPERATOR_MIN = "min";
    /** Identifies a maximum operator. */
    private final static String OPERATOR_MAX = "max";
    /** Identifies an absolute value operator. */
    private final static String OPERATOR_ABS = "abs";
    /** Identifies a signum operator. */
    private final static String OPERATOR_SGN = "sgn";
    /** Identifies a truncation-to-integer operator. */
    private final static String OPERATOR_TRUNCATION = "trc";

    public final static String IDENTIFIER = "derived-operators";
    private ModelJANI model;
    private JANINode node;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setModel(ModelJANI model) {
        this.model = model;
    }

    @Override
    public void setNode(JANINode node) {
        this.node = node;
    }

    @Override
    public void parseBefore() {
        if (!(this.node instanceof ModelJANI)) {
            return;
        }
        JANIOperators operators = model.getJANIOperators();
        operators.add().setJANI(OPERATOR_IMPLIES).setEPMC(OperatorImplies.IMPLIES)
        .setArity(2).build();
        operators.add().setJANI(OPERATOR_GT).setEPMC(OperatorGt.GT)
        .setArity(2).build();
        operators.add().setJANI(OPERATOR_GE).setEPMC(OperatorGe.GE)
        .setArity(2).build();
        operators.add().setJANI(OPERATOR_MIN).setEPMC(OperatorMin.MIN)
        .setArity(2).build();
        operators.add().setJANI(OPERATOR_MAX).setEPMC(OperatorMax.MAX)
        .setArity(2).build();
        operators.add().setJANI(OPERATOR_ABS).setEPMC(OperatorAbs.ABS)
        .setArity(1).build();
        operators.add().setJANI(OPERATOR_SGN).setEPMC(OperatorSgn.SGN)
        .setArity(1).build();
        operators.add().setJANI(OPERATOR_TRUNCATION).setEPMC(OperatorTrunc.TRUNC)
        .setArity(1).build();
    }
}
