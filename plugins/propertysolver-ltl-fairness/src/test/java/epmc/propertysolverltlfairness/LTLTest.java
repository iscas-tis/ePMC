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

package epmc.propertysolverltlfairness;

import java.io.FileNotFoundException;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.main.options.UtilOptionsEPMC;
import epmc.modelchecker.RawProperties;
import epmc.modelchecker.RawProperty;
import epmc.options.Options;
import epmc.value.ContextValue;

public class LTLTest {

    public static void main(String[] args) throws FileNotFoundException {
        String file = "/home/liyong/projects/test/mdptt.pctl";
        Options options = UtilOptionsEPMC.newOptions();
        RawProperties props = LTLHelper.readProperties(options, file);
        ContextValue.set(new ContextValue());

        //        PropertiesImpl pList = new PropertiesImpl(context);
        //      pList.parseProperties(props);
        Expression expression = null;

        for (RawProperty prop : props.getProperties()) {
            // expression = pList.getParsedProperty(prop);
            System.out.println(expression);
            ExpressionQuantifier expressionQuantifier = (ExpressionQuantifier) expression;
            Expression opera = expressionQuantifier.getQuantified();
            //            Set<Expression> labels = opera.collectIdentifiers();
            //            if (PropertySolverExplicitLTLFairness.canHandleProp(opera)) {
            //                Expression normForm = PropertySolverExplicitLTLFairness.getNormForm(opera, labels);
            //                System.out.println("normal form:\n" + normForm);
            //                System.out.println("flatten: \n" + PropertySolverExplicitLTLFairness.flatten(normForm, labels));
            //            }else {
            //                System.out.println("can not handle");
            //            }
            //            


        }
    }

}
