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

package epmc.jani.model;

import epmc.operator.Operator;

/**
 * JANI operator.
 * 
 * @author Ernst Moritz Hahn
 */
public final class JANIOperator {
    /**
     * Builder class for JANI operator.
     * 
     * @author Ernst Moritz Hahn
     */
    public final static class Builder  {
        /** Stores whether operator has already been built. */
        private boolean built;
        /** JANI operators to which the JANI operator will be added. */
        private JANIOperators operators;
        /** Name of JANI operator. */
        private String jani;
        /** EPMC operator the operator will map to. */
        private Operator iscasMC;
        /** Arity the operator will have. */
        private int arity;

        /**
         * Construct new builder.
         * The constructions is package-dependent, as it shall only be called by
         * {@link JANIOperators#add()}.
         */
        Builder() {
        }

        /**
         * Set the JANI operators the operator will be added to.
         * 
         * @param operators
         * @return {@code this} builder, for setter method chaining
         */
        Builder setJANIOperators(JANIOperators operators) {
            assert !built;
            this.operators = operators;
            return this;
        }

        /**
         * Set JANI name of the operator.
         * 
         * @param jani JANI operator name
         * @return {@code this} builder, for setter method chaining
         */
        public Builder setJANI(String jani) {
            assert !built;
            this.jani = jani;
            return this;
        }

        /**
         * Get the name the operator will have.
         * 
         * @return name the operator will have
         */
        private String getJANI() {
            return jani;
        }

        /**
         * 
         * @param iscasMC
         * @return {@code this} builder, for setter method chaining
         */
        public Builder setEPMC(Operator iscasMC) {
            assert !built;
            this.iscasMC = iscasMC;
            return this;
        }

        /**
         * Get the name of the EPMC operator the operator will be mapped to.
         * 
         * @return name of the EPMC operator the operator will be mapped to
         */
        private Operator getEPMC() {
            return iscasMC;
        }

        /**
         * Set the arity the operator will have.
         * 
         * @param arity arity the operator will have
         * @return {@code this} builder, for setter method chaining
         */
        public Builder setArity(int arity) {
            assert !built;
            this.arity = arity;
            return this;
        }

        /**
         * Get arity the operator will have.
         * 
         * @return arity the operator will have
         */
        private int getArity() {
            return arity;
        }

        /**
         * Build the operator.
         * This method must be called exactly once.
         * 
         * @return operator built.
         */
        public JANIOperator build() {
            assert !built;
            this.built = true;
            JANIOperator operator = new JANIOperator(this);
            operators.add(operator);
            return operator;
        }
    }

    /** Name of JANI operator. */
    private final String jani;
    /** Name of the EPMC operator the operator will map to. */
    private final Operator iscasMC;
    /** Arity of the operator. */
    private final int arity;

    private JANIOperator(Builder builder) {
        assert builder != null;
        assert builder.getEPMC() != null;
        assert builder.getJANI() != null;
        assert builder.getArity() >= 0;
        this.jani = builder.getJANI();
        this.iscasMC = builder.getEPMC();
        this.arity = builder.getArity();
    }

    /**
     * Get name of the operator.
     * 
     * @return name of the operator
     */
    public String getJANI() {
        return jani;
    }

    /**
     * Get the name of the EPMC operator the operator is mapped to.
     * 
     * @return name of the EPMC operator the operator is mapped to
     */
    public Operator getEPMC() {
        return iscasMC;
    }

    /**
     * Get arity of the operator.
     * 
     * @return arity of the operator
     */
    public int getArity() {
        return arity;
    }

    /**
     * Get EPMC operator the operator is mapped to.
     * The context operator must not be {@code null}.
     * 
     * @return EPMC operator the operator is mapped to
     */
    public Operator getOperator() {
        return iscasMC;
    }
}
