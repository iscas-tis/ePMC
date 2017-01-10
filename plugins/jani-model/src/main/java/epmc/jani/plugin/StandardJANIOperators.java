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
import epmc.jani.value.OperatorEuler;
import epmc.jani.value.OperatorPi;
import epmc.value.OperatorAdd;
import epmc.value.OperatorAnd;
import epmc.value.OperatorCeil;
import epmc.value.OperatorDivide;
import epmc.value.OperatorEq;
import epmc.value.OperatorFloor;
import epmc.value.OperatorIte;
import epmc.value.OperatorLe;
import epmc.value.OperatorLog;
import epmc.value.OperatorLt;
import epmc.value.OperatorMod;
import epmc.value.OperatorMultiply;
import epmc.value.OperatorNe;
import epmc.value.OperatorNot;
import epmc.value.OperatorOr;
import epmc.value.OperatorPow;
import epmc.value.OperatorSubtract;

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
		operators.add().setJANI(OPERATOR_ITE).setEPMC(OperatorIte.IDENTIFIER)
			.setArity(3).build();
		operators.add().setJANI(OPERATOR_OR).setEPMC(OperatorOr.IDENTIFIER)
			.setArity(2).build();
		operators.add().setJANI(OPERATOR_AND).setEPMC(OperatorAnd.IDENTIFIER)
			.setArity(2).build();
		operators.add().setJANI(OPERATOR_NOT).setEPMC(OperatorNot.IDENTIFIER)
		.setArity(1).build();
		operators.add().setJANI(OPERATOR_EQ).setEPMC(OperatorEq.IDENTIFIER)
			.setArity(2).build();
		operators.add().setJANI(OPERATOR_NE).setEPMC(OperatorNe.IDENTIFIER)
			.setArity(2).build();
		operators.add().setJANI(OPERATOR_LT).setEPMC(OperatorLt.IDENTIFIER)
			.setArity(2).build();
		operators.add().setJANI(OPERATOR_LE).setEPMC(OperatorLe.IDENTIFIER)
			.setArity(2).build();
		operators.add().setJANI(OPERATOR_PLUS).setEPMC(OperatorAdd.IDENTIFIER)
			.setArity(2).build();
		operators.add().setJANI(OPERATOR_MINUS).setEPMC(OperatorSubtract.IDENTIFIER)
			.setArity(2).build();
		operators.add().setJANI(OPERATOR_MULTIPLY).setEPMC(OperatorMultiply.IDENTIFIER)
			.setArity(2).build();
		operators.add().setJANI(OPERATOR_MOD).setEPMC(OperatorMod.IDENTIFIER)
			.setArity(2).build();
		operators.add().setJANI(OPERATOR_DIV).setEPMC(OperatorDivide.IDENTIFIER)
			.setArity(2).build();
		operators.add().setJANI(OPERATOR_POW).setEPMC(OperatorPow.IDENTIFIER)
			.setArity(2).build();
		operators.add().setJANI(OPERATOR_LOG).setEPMC(OperatorLog.IDENTIFIER)
		.setArity(1).build();
		operators.add().setJANI(OPERATOR_CEIL).setEPMC(OperatorCeil.IDENTIFIER)
			.setArity(1).build();
		operators.add().setJANI(OPERATOR_FLOOR).setEPMC(OperatorFloor.IDENTIFIER)
			.setArity(1).build();
		operators.add().setJANI(CONSTANT_E).setEPMC(OperatorEuler.IDENTIFIER)
			.setArity(0).build();
		operators.add().setJANI(CONSTANT_PI).setEPMC(OperatorPi.IDENTIFIER)
			.setArity(0).build();
	}

}
