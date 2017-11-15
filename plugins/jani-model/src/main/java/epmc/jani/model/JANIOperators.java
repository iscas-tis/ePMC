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

import java.util.LinkedHashMap;
import java.util.Map;

import epmc.operator.Operator;

public final class JANIOperators {
    private Map<String,JANIOperator> janiToOperator = new LinkedHashMap<>();
    private Map<Operator,JANIOperator> iscasMCToOperator = new LinkedHashMap<>();

    public JANIOperator.Builder add() {
        JANIOperator.Builder builder = new JANIOperator.Builder();
        builder.setJANIOperators(this);
        return builder;
    }

    void add(JANIOperator operator) {
        assert operator != null;
        assert !janiToOperator.containsKey(operator.getJANI()) : operator.getJANI();
        assert !iscasMCToOperator.containsKey(operator.getEPMC());
        janiToOperator.put(operator.getJANI(), operator);
        iscasMCToOperator.put(operator.getEPMC(), operator);
    }

    public Operator janiToEPMCName(String jani) {
        assert jani != null;
        assert janiToOperator.containsKey(jani);
        return janiToOperator.get(jani).getEPMC();
    }

    public boolean containsOperatorByJANI(String jani) {
        assert jani != null;
        return janiToOperator.containsKey(jani);
    }

    public JANIOperator getOperatorByJANI(String jani) {
        assert jani != null;
        assert janiToOperator.containsKey(jani);
        return janiToOperator.get(jani);
    }

    public JANIOperator getOperator(Operator operator) {
        assert operator != null;
        assert iscasMCToOperator.containsKey(operator) :
            operator;
        return iscasMCToOperator.get(operator);
    }
}
