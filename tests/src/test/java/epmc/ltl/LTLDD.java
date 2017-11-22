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

package epmc.ltl;

public class LTLDD {

    /*
    public static void main(String[] args) throws FileNotFoundException,
            EPMCException {
        String mfile = "./examples/dice/dice.prism";
        String file = "./examples/dice/pie.pctl";
        Properties props = LTLHelper.readProperties(file);
        PropertyList properties = null;

        PrismParser parser = new PrismParser(new FileInputStream(mfile));
        Model model = parser.parseModel();
        Options options = UtilOptionsEPMC.newOptions();
        ContextExpression context = UtilExpression.newContextExpression(options);
        properties = new PropertyList(context, props);
        properties.addConstants(options.getDefList(OptionsEPMC.CONST));
        if (properties != null) {
            model.getPropertyList().add(properties);
        }
        model.getPropertyList().expandAndCheckWithDefinedCheck();
        String modelInputType = options
                .getString(OptionsEPMC.INPUT_TYPE);
        System.out.println(model);
        System.out.println(properties);
        ContextDD contextDD = ContextDD.get();
        GraphDD modelGraph = model.newGraphDD(null, null, null);

        System.out.println("dd -----------------");
//        ExpressionToDD exprdd = modelGraph.getExpressionToDD();

        Expression expression = model.getPropertyList().getPropertyExpression(
                properties.iterator().next());

        DD sta = modelGraph.getInitial();
        DD pred = contextDD.newConstant(false);
        long nano = System.nanoTime();
        while (!sta.equals(pred)) {
            // only exploring new states important for Rabin semi-symbolic mtd
            System.out.println(pred);
            DD tmp = pred;
            pred = sta;
            sta = sta.or(modelGraph.next(sta.andNot(tmp)));
        }
        modelGraph.getTransitionsBoolean();
        nano = System.nanoTime() - nano;
        modelGraph.close();
        context.close();
    }
     */
}
