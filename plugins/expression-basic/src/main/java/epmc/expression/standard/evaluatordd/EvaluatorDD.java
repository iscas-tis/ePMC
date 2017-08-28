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

package epmc.expression.standard.evaluatordd;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

import epmc.dd.DD;
import epmc.dd.VariableDD;
import epmc.expression.Expression;

public interface EvaluatorDD extends Closeable {
    String getIdentifier();

    void setVariables(Map<Expression,VariableDD> variables);

    void setExpression(Expression expression);

    boolean canHandle();

    void build();

    DD getDD();

    List<DD> getVector();

    @Override
    void close();
}
