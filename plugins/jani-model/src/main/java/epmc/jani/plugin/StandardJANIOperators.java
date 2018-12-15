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

package epmc.jani.plugin;

import epmc.jani.model.JANIOperators;
import epmc.jani.operator.OperatorEuler;
import epmc.jani.operator.OperatorPi;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorAnd;
import epmc.operator.OperatorCeil;
import epmc.operator.OperatorDivide;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorFloor;
import epmc.operator.OperatorIte;
import epmc.operator.OperatorLe;
import epmc.operator.OperatorLog;
import epmc.operator.OperatorLt;
import epmc.operator.OperatorMod;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorNe;
import epmc.operator.OperatorNot;
import epmc.operator.OperatorOr;
import epmc.operator.OperatorPow;
import epmc.operator.OperatorSubtract;

/**
 * Class to add JANI standard operators to JANI operator map.
 * 
 * @author Ernst Moritz Hahn
 */
public final class StandardJANIOperators {
    /** Identifies an if-then-else operator. */
    private final static String OPERATOR_ITE = "ite"; //"?:";
    /** Identifies an OR operator. */
    private final static String OPERATOR_OR = "∨";
    /** Identifies an AND operator. */
    private final static String OPERATOR_AND = "∧";
    /** Identifies an equality operator. */
    private final static String OPERATOR_EQ = "=";
    /** Identifies an inequality operator. */
    private final static String OPERATOR_NE = "≠";
    /** Identifies a less-than operator. */
    private final static String OPERATOR_LT = "<";
    /** Identifies a less-or-equal operator. */
    private final static String OPERATOR_LE = "≤";
    /** Identifies a plus operator. */
    private final static String OPERATOR_PLUS = "+";
    /** Identifies a minus operator. */
    private final static String OPERATOR_MINUS = "-";
    /** Identifies a multiplication operator. */
    private final static String OPERATOR_MULTIPLY = "*";
    /** Identifies a division operator. */
    private final static String OPERATOR_DIV = "/";
    /** Identifies a modulo operator. */
    private final static String OPERATOR_MOD = "%";
    /** Identifies a NOT operator. */
    private final static String OPERATOR_NOT = "¬";
    /** Identifies a ceiling operator. */
    private final static String OPERATOR_CEIL = "ceil"; //"⌈⌉";
    /** Identifies a floor operator. */
    private final static String OPERATOR_FLOOR = "floor"; //"⌊⌋";
    /** Identifies a power operator (a^b, a to-the b). */
    private final static String OPERATOR_POW = "pow";
    /** Identifies a logarithm operator. */
    private final static String OPERATOR_LOG = "log";
    /** Identifies a derivative operator. */
    private final static String OPERATOR_DER = "der";
    /** Identifies the Euler constant e. */
    private final static String CONSTANT_E = "e";
    /** Identifies the constant π (Pi). */
    private final static String CONSTANT_PI = "π";

    /**
     * Adds JANI standard operators and constants to map.
     * The map parameter must not be {@code null}.
     * 
     * @param operators map to add JANI standard operators to
     */
    public static void add(JANIOperators operators) {
        assert operators != null;
        operators.add().setJANI(OPERATOR_ITE).setEPMC(OperatorIte.ITE)
        .setArity(3).build();
        operators.add().setJANI(OPERATOR_OR).setEPMC(OperatorOr.OR)
        .setArity(2).build();
        operators.add().setJANI(OPERATOR_AND).setEPMC(OperatorAnd.AND)
        .setArity(2).build();
        operators.add().setJANI(OPERATOR_NOT).setEPMC(OperatorNot.NOT)
        .setArity(1).build();
        operators.add().setJANI(OPERATOR_EQ).setEPMC(OperatorEq.EQ)
        .setArity(2).build();
        operators.add().setJANI(OPERATOR_NE).setEPMC(OperatorNe.NE)
        .setArity(2).build();
        operators.add().setJANI(OPERATOR_LT).setEPMC(OperatorLt.LT)
        .setArity(2).build();
        operators.add().setJANI(OPERATOR_LE).setEPMC(OperatorLe.LE)
        .setArity(2).build();
        operators.add().setJANI(OPERATOR_PLUS).setEPMC(OperatorAdd.ADD)
        .setArity(2).build();
        operators.add().setJANI(OPERATOR_MINUS).setEPMC(OperatorSubtract.SUBTRACT)
        .setArity(2).build();
        operators.add().setJANI(OPERATOR_MULTIPLY).setEPMC(OperatorMultiply.MULTIPLY)
        .setArity(2).build();
        operators.add().setJANI(OPERATOR_MOD).setEPMC(OperatorMod.MOD)
        .setArity(2).build();
        operators.add().setJANI(OPERATOR_DIV).setEPMC(OperatorDivide.DIVIDE)
        .setArity(2).build();
        operators.add().setJANI(OPERATOR_POW).setEPMC(OperatorPow.POW)
        .setArity(2).build();
        operators.add().setJANI(OPERATOR_LOG).setEPMC(OperatorLog.LOG)
        .setArity(2).build();
        operators.add().setJANI(OPERATOR_CEIL).setEPMC(OperatorCeil.CEIL)
        .setArity(1).build();
        operators.add().setJANI(OPERATOR_FLOOR).setEPMC(OperatorFloor.FLOOR)
        .setArity(1).build();
        operators.add().setJANI(CONSTANT_E).setEPMC(OperatorEuler.EULER)
        .setArity(0).build();
        operators.add().setJANI(CONSTANT_PI).setEPMC(OperatorPi.PI)
        .setArity(0).build();
    }

}
