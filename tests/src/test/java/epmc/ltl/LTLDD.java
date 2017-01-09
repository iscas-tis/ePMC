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
        options.set(OptionsExpression.CONTEXT_EXPRESSION, context);
        options.set(OptionsValue.CONTEXT_VALUE, context.getContextValue());
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
        ContextDD contextDD = context.getContextDD();
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
