package epmc.petl.model;

import static epmc.error.UtilError.ensure;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.graph.Semantics;
import epmc.graph.SemanticsDTMC;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.ModelJANIConverter;
import epmc.jani.model.type.JANIType;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.modelchecker.Properties;
import epmc.operator.OperatorAnd;
import epmc.operator.OperatorMultiply;
import epmc.options.Options;
import epmc.petl.error.ProblemsPETL;
import epmc.petl.relation.EquivalenceRelationsParser;
import epmc.prism.messages.MessagesPRISM;
import epmc.prism.model.Alternative;
import epmc.prism.model.Command;
import epmc.prism.model.ModelPRISM;
import epmc.prism.model.Module;
import epmc.prism.model.ModuleCommands;

/**
 * MAS(Multi Agent System) model representation.
 * Basically, it consists of two parts: a prism model and the equivalence relations.
 * 
 * @author Chen Fu
 */
public class ModelMAS implements ModelJANIConverter{
	public final static String IDENTIFIER = "mas";

	//Most parts of the prism model can be resued
	ModelPRISM modelPrism = new ModelPRISM();
	//We need a variable to store the equivalence relations
	EquivalenceRelations equivalenceRelations;
	
	ModelPRISM synModelPrism = new ModelPRISM();

	
	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void read(Object object, InputStream... inputs) {
		assert inputs != null;
        for (InputStream input : inputs) {
            assert input != null;
        }
        getLog().send(MessagesPRISM.START_PARSING);
        
        ensure(inputs.length == 2, ProblemsPETL.PETL_TWO_MODEL_FILE, inputs.length);
        //first parse the prism model
        PrismParser parser = new PrismParser(inputs[0]);   
        parser.setPart(object);
        parser.setModel(this.modelPrism);
        parser.parseModel();
        
		//then parse the equivalence relations
        EquivalenceRelationsParser equiv_rel_parser = new EquivalenceRelationsParser(inputs[1]);
        try {
        	equivalenceRelations = equiv_rel_parser.parseRelations();
		} catch (Exception e) {
			e.printStackTrace();
		}
        //we compose the modules differently with prism
        synchronizeModel();
        
        getLog().send(MessagesPRISM.DONE_PARSING);
	}
	
	private void synchronizeModel()
	{
		Semantics semantics = modelPrism.getSemantics();
		if(SemanticsDTMC.isDTMC(semantics) || (modelPrism.getModules().size() == 1))
		{
			synModelPrism = modelPrism;
			return;
		}
		
		Module module = synModules(modelPrism.getModules());
		List<Module> list = new ArrayList<Module>();
		list.add(module);
		synModelPrism.build(new ModelPRISM.Builder()
				.setFormulas(modelPrism.getFormulas())
				.setGlobalInitValues(modelPrism.getGlobalInitValues())
				.setGlobalVariables(modelPrism.getGlobalVariables())
				.setModules(list)
				.setPlayers(modelPrism.getPlayers())
				.setSemantics(modelPrism.getSemantics())
				.setRewards(modelPrism.getRewards()));
		
		//System.out.println(synModelPrism.getModules().get(0).toString());
	}
	
	private Module synModules(List<Module> modules)
	{
		if(modules.size() == 1)
			return modules.get(0);
		
		Module m1 = synModules(modules.subList(1, modules.size()));
		Module m2 = synTwoModule((ModuleCommands)modules.get(0), (ModuleCommands) m1);
		return m2;
	}
	
	private Module synTwoModule(ModuleCommands m1, ModuleCommands m2)
	{
		String name = m1.getName() + "_" + m2.getName();
		Map<Expression,JANIType> variables = new HashMap<Expression,JANIType>();
		variables.putAll(m1.getVariables());
		variables.putAll(m2.getVariables());
		Map<Expression,Expression> initValues = new HashMap<Expression,Expression>();
		initValues.putAll(m1.getInitValues());
		initValues.putAll(m2.getInitValues());

		Expression leftInvariant = m1.getInvariants();
        Expression rightInvariant = m2.getInvariants();
        Expression newInvariant = null;
        if (leftInvariant != null && rightInvariant != null) {
            newInvariant = UtilExpressionStandard.opAnd(leftInvariant, rightInvariant);
        } else if (leftInvariant != null) {
            newInvariant = leftInvariant;
        } else if (rightInvariant != null) {
            newInvariant = rightInvariant;
        } else {
            newInvariant = null;
        }
		
		List<Command> commands1 = m1.getCommands();
		List<Command> commands2 = m2.getCommands();
		ArrayList<Command> commands = new ArrayList<>();
		for(Command com1 : commands1)
		{
			for(Command com2 : commands2)
			{
				String labelName = ((ExpressionIdentifierStandard) com1.getAction()).getName() + "," + ((ExpressionIdentifierStandard) com2.getAction()).getName();
				ExpressionIdentifierStandard label = (new ExpressionIdentifierStandard.Builder()
						.setName(labelName)
//						.setScope(((ExpressionIdentifierStandard)com1.getAction()).getScope())
						)
						.build();
				Expression newGuard = and(com1.getGuard(),
                        com2.getGuard());
//				Expression guard = new ExpressionOperator.Builder()
//						.setPositional(com1.getGuard().getPositional())
//						.setOperator(OperatorAnd.AND)
//						.setOperands(com1.getGuard(),com2.getGuard())
//						.build();
				List<Alternative> alternatives = new ArrayList<>();
				for(Alternative a1: com1.getAlternatives())
				{
					for(Alternative a2 : com2.getAlternatives())
					{
						Expression weight = new ExpressionOperator.Builder()
								.setOperator(OperatorMultiply.MULTIPLY)
								.setOperands(a1.getWeight(),a2.getWeight())
								.build();
						Map<Expression,Expression> effect = new HashMap<Expression,Expression>();
						effect.putAll(a1.getEffect());
						effect.putAll(a2.getEffect());
						
						Alternative alternative = new Alternative(weight,effect,null);
						alternatives.add(alternative);
					}
				}
				
				Command command = new Command(label,newGuard,alternatives,null);
				commands.add(command);
			}
		}
		
		return new ModuleCommands(name,variables,initValues,commands,newInvariant, null);
	}

	@Override
	public Semantics getSemantics() {
		return modelPrism.getSemantics();
	}

//	@Override
//	public LowLevel newLowLevel(Engine engine, Set<Object> graphProperties, Set<Object> nodeProperties,
//			Set<Object> edgeProperties) {
//		return this.synModelPrism.newLowLevel(engine, graphProperties, nodeProperties, edgeProperties);
//	}

	@Override
	public Properties getPropertyList() {
		return modelPrism.getPropertyList();
	}

	@Override
	public ModelJANI toJANI(boolean forExporting) {
		return this.synModelPrism.toJANI(forExporting);
	}

	public ModelPRISM getSynModel() {
		return synModelPrism;
	}
	
	public EquivalenceRelations getEquivalenceRelations() {
		return equivalenceRelations;
	}

	public ModelPRISM getModelPrism() {
		return modelPrism;
	}

	public void setModelPrism(ModelPRISM modelPrism) {
		this.modelPrism = modelPrism;
	}
	
	private Expression and(Expression a, Expression b) {
        return new ExpressionOperator.Builder()
                .setOperator(OperatorAnd.AND)
                .setOperands(a, b)
                .build();
    }

	/**
     * Get log used for analysis.
     * 
     * @return log used for analysis
     */
    private Log getLog() {
        return Options.get().get(OptionsMessages.LOG);
    }
}
