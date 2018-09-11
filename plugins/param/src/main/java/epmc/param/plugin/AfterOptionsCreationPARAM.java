package epmc.param.plugin;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.graphsolver.GraphSolverExplicit;
import epmc.graphsolver.OptionsGraphsolver;
import epmc.main.options.OptionsEPMC;
import epmc.modelchecker.CommandTask;
import epmc.options.Category;
import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeEnum;
import epmc.options.OptionTypeInteger;
import epmc.options.OptionTypeIntegerNonNegative;
import epmc.options.OptionTypeMap;
import epmc.options.OptionTypeString;
import epmc.options.OptionTypeStringList;
import epmc.options.OptionTypeStringListSubsetDirect;
import epmc.options.Options;
import epmc.param.algorithm.NodeEliminator;
import epmc.param.command.CommandTaskLoadFunction;
import epmc.param.graphsolver.GraphSolverEliminator;
import epmc.param.graphsolver.eliminationorder.EliminationOrder;
import epmc.param.graphsolver.eliminationorder.EliminationOrderFromTargets;
import epmc.param.graphsolver.eliminationorder.EliminationOrderMinProdPredSucc;
import epmc.param.graphsolver.eliminationorder.EliminationOrderNodeNumbersAscending;
import epmc.param.graphsolver.eliminationorder.EliminationOrderNodeNumbersDescending;
import epmc.param.graphsolver.eliminationorder.EliminationOrderNumNew;
import epmc.param.graphsolver.eliminationorder.EliminationOrderQuickTarget;
import epmc.param.graphsolver.eliminationorder.EliminationOrderRandom;
import epmc.param.graphsolver.eliminationorder.EliminationOrderSameStructure;
import epmc.param.graphsolver.eliminationorder.EliminationOrderWhenFullyExplored;
import epmc.param.options.OptionsParam;
import epmc.param.points.IntervalFormat;
import epmc.param.points.IntervalFormatInterval;
import epmc.param.points.IntervalFormatTwoValues;
import epmc.param.points.PointResultsExporter;
import epmc.param.points.PointResultsExporterData;
import epmc.param.points.PointResultsExporterPgfplots;
import epmc.param.points.Points;
import epmc.param.points.PointsList;
import epmc.param.points.PointsListIntervals;
import epmc.param.points.PointsRange;
import epmc.param.points.PointsRangeIntervals;
import epmc.param.points.ValueFormat;
import epmc.param.points.ValueFormatDouble;
import epmc.param.points.ValueFormatDoubleHex;
import epmc.param.points.ValueFormatFraction;
import epmc.param.value.FunctionEvaluator;
import epmc.param.value.TypeFunction;
import epmc.param.value.cancellator.Cancellator;
import epmc.param.value.cancellator.CancellatorCoCoALib;
import epmc.param.value.cancellator.CancellatorGiNaC;
import epmc.param.value.cancellator.CancellatorJAS;
import epmc.param.value.dag.NodeLookup;
import epmc.param.value.dag.NodeLookupBoundedHashMap;
import epmc.param.value.dag.NodeLookupHashMap;
import epmc.param.value.dag.NodeStore;
import epmc.param.value.dag.NodeStoreArray;
import epmc.param.value.dag.NodeStoreDisk;
import epmc.param.value.dag.TypeDag;
import epmc.param.value.dag.evaluator.EvaluatorDagDoubleIntervalJava;
import epmc.param.value.dag.evaluator.EvaluatorDagDoubleJava;
import epmc.param.value.dag.evaluator.EvaluatorDagGMPNative;
import epmc.param.value.dag.evaluator.EvaluatorDagSimpleGeneral;
import epmc.param.value.dag.exporter.DagExporter;
import epmc.param.value.dag.exporter.ExporterC;
import epmc.param.value.dag.exporter.ExporterCInterval;
import epmc.param.value.dag.exporter.ExporterEntryNumber;
import epmc.param.value.dag.exporter.ExporterGinsh;
import epmc.param.value.dag.exporter.ExporterGraphviz;
import epmc.param.value.dag.exporter.ExporterJson;
import epmc.param.value.dag.exporter.ExporterPoints;
import epmc.param.value.dag.exporter.ExporterSimple;
import epmc.param.value.dag.simplifier.DoubleLookup;
import epmc.param.value.dag.simplifier.DoubleLookupBoundedHashMap;
import epmc.param.value.dag.simplifier.DoubleLookupHashMap;
import epmc.param.value.dag.simplifier.DoubleLookupSearchStore;
import epmc.param.value.dag.simplifier.DoubleLookupTreeMap;
import epmc.param.value.dag.simplifier.DoubleStore;
import epmc.param.value.dag.simplifier.DoubleStoreArray;
import epmc.param.value.dag.simplifier.DoubleStoreDisk;
import epmc.param.value.dag.simplifier.Evaluator;
import epmc.param.value.dag.simplifier.EvaluatorDouble;
import epmc.param.value.dag.simplifier.EvaluatorRational;
import epmc.param.value.functionloader.FunctionLoader;
import epmc.param.value.functionloader.FunctionLoaderDagJson;
import epmc.param.value.polynomial.PolynomialFractionExporter;
import epmc.param.value.polynomialfraction.TypePolynomialFraction;
import epmc.param.value.polynomialfraction.evaluator.EvaluatorPolynomialFractionSimpleGeneral;
import epmc.param.value.polynomialfraction.exporter.PolynomialFractionExporterDag;
import epmc.param.value.polynomialfraction.exporter.PolynomialFractionExporterPoints;
import epmc.param.value.polynomialfraction.exporter.PolynomialFractionExporterSimple;
import epmc.plugin.AfterOptionsCreation;
import epmc.util.OrderedMap;

public final class AfterOptionsCreationPARAM implements AfterOptionsCreation {
	private final static String IDENTIFIER = "after-options-creation-param";
	private final static String PARAMETERS = "parameters";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
		
		Category category = options.addCategory()
		        .setBundleName(OptionsParam.PARAM_OPTIONS)
		        .setIdentifier(OptionsParam.PARAM_CATEGORY)
		        .build();
		
		OptionTypeStringList typeParamList = new OptionTypeStringList(PARAMETERS);
		options.addOption()
			.setBundleName(OptionsParam.PARAM_OPTIONS)
			.setIdentifier(OptionsParam.PARAM_PARAMETER)
			.setType(typeParamList)
			.setCategory(category)
			.setCommandLine().setGui().setWeb().build();

		addGraphSolvers(options);
        addCancellators(options, category);
        addEliminationOrders(options, category);
        addRandomEvaluators(options, category);
        
        OptionTypeEnum selfLoopMethod = new OptionTypeEnum(NodeEliminator.WeighterMethod.class);
        options.addOption().setBundleName(OptionsParam.PARAM_OPTIONS)
            .setIdentifier(OptionsParam.PARAM_ELIMINATION_SELF_LOOP_METHOD)
            .setDefault(NodeEliminator.WeighterMethod.SELF_LOOP)
            .setType(selfLoopMethod)
            .setCategory(category)
            .setCommandLine().setGui().setWeb()
            .build();
        
        addDagOptions(options, category);
        addFunctionTypes(options, category);
        
        options.addOption().setBundleName(OptionsParam.PARAM_OPTIONS)
        .setIdentifier(OptionsParam.PARAM_STATISTICS_SEND)
        .setDefault(true)
        .setType(OptionTypeBoolean.getInstance())
        .setCategory(category)
        .setCommandLine().setGui().setWeb()
        .build();
        
        addFunctionParseStuff(options, category);
	}

    private void addFunctionParseStuff(Options options, Category category) {
        assert options != null;
        Map<String,Class<? extends CommandTask>> commandTaskClasses = options.get(OptionsEPMC.COMMAND_CLASS);
        assert commandTaskClasses != null;
        options.addCommand()
        .setBundleName(OptionsParam.PARAM_OPTIONS)
        .setIdentifier(CommandTaskLoadFunction.IDENTIFIER)
        .setCommandLine()
        .setGui()
        .setWeb().build();
        
        Map<String, Class<? extends FunctionLoader.Builder>> orders = new OrderedMap<>(true);
        orders.put(FunctionLoaderDagJson.IDENTIFIER, FunctionLoaderDagJson.Builder.class);
//        orders.put(FunctionLoaderDagJson.IDENTIFIER, FunctionLoaderDagJson.Builder.class);
        OptionTypeMap<Class<? extends FunctionLoader.Builder>> functionLoaderType = new OptionTypeMap<>(orders);
        
        commandTaskClasses.put(CommandTaskLoadFunction.IDENTIFIER, CommandTaskLoadFunction.class);        
        options.addOption()
            .setBundleName(OptionsParam.PARAM_OPTIONS)
            .setIdentifier(OptionsParam.PARAM_FUNCTION_LOADER)
            .setDefault(FunctionLoaderDagJson.Builder.class)
            .setType(functionLoaderType)
            .setCategory(category)
            .setCommandLine().setGui().build();
                
        options.addOption().setBundleName(OptionsParam.PARAM_OPTIONS)
            .setIdentifier(OptionsParam.PARAM_FUNCTION_INPUT_FILENAME)
            .setType(OptionTypeString.getInstance())
            .setCategory(category)
            .setCommandLine().setGui()
            .build();
    }

    private void addGraphSolvers(Options options) {
        Map<String, Class<? extends GraphSolverExplicit>> graphSolverMap = options.get(OptionsGraphsolver.GRAPHSOLVER_SOLVER_CLASS);
        assert graphSolverMap != null;
        graphSolverMap.put(GraphSolverEliminator.IDENTIFIER, GraphSolverEliminator.class);
    }

    private void addCancellators(Options options, Category category) {
        Map<String, Class<? extends Cancellator.Builder>> cancellators = new OrderedMap<>(true);
        cancellators.put(CancellatorJAS.IDENTIFIER, CancellatorJAS.Builder.class);
        cancellators.put(CancellatorGiNaC.IDENTIFIER, CancellatorGiNaC.Builder.class);
        cancellators.put(CancellatorCoCoALib.IDENTIFIER, CancellatorCoCoALib.Builder.class);
        OptionTypeMap<Class<? extends Cancellator.Builder>> cancellatorType = new OptionTypeMap<>(cancellators);
        options.addOption().setBundleName(OptionsParam.PARAM_OPTIONS)
            .setIdentifier(OptionsParam.PARAM_CANCELLATOR)
            .setDefault(CancellatorJAS.Builder.class)
            .setType(cancellatorType)
            .setCategory(category)
            .setCommandLine().setGui().setWeb()
            .build();
    }
    
    private void addEliminationOrders(Options options, Category category) {
        Map<String, Class<? extends EliminationOrder.Builder>> orders = new OrderedMap<>(true);
        orders.put(EliminationOrderNodeNumbersAscending.IDENTIFIER, EliminationOrderNodeNumbersAscending.Builder.class);
        orders.put(EliminationOrderNodeNumbersDescending.IDENTIFIER, EliminationOrderNodeNumbersDescending.Builder.class);
        orders.put(EliminationOrderRandom.IDENTIFIER, EliminationOrderRandom.Builder.class);
        orders.put(EliminationOrderFromTargets.IDENTIFIER, EliminationOrderFromTargets.Builder.class);
        orders.put(EliminationOrderQuickTarget.IDENTIFIER, EliminationOrderQuickTarget.Builder.class);
        orders.put(EliminationOrderMinProdPredSucc.IDENTIFIER, EliminationOrderMinProdPredSucc.Builder.class);
        orders.put(EliminationOrderNumNew.IDENTIFIER, EliminationOrderNumNew.Builder.class);
        orders.put(EliminationOrderWhenFullyExplored.IDENTIFIER, EliminationOrderWhenFullyExplored.Builder.class);
        orders.put(EliminationOrderSameStructure.IDENTIFIER, EliminationOrderSameStructure.Builder.class);
        OptionTypeMap<Class<? extends EliminationOrder.Builder>> cancellatorType = new OptionTypeMap<>(orders);
        options.addOption().setBundleName(OptionsParam.PARAM_OPTIONS)
            .setIdentifier(OptionsParam.PARAM_ELIMINATION_ORDER)
            .setDefault(EliminationOrderNodeNumbersDescending.Builder.class)
            .setType(cancellatorType)
            .setCategory(category)
            .setCommandLine().setGui().setWeb()
            .build();
    }

    private void addRandomEvaluators(Options options, Category category) {
        assert options != null;
        assert category != null;
        Map<String, Class<? extends Evaluator.Builder>> orders = new OrderedMap<>(true);
        orders.put(EvaluatorDouble.IDENTIFIER, EvaluatorDouble.Builder.class);
        orders.put(EvaluatorRational.IDENTIFIER, EvaluatorRational.Builder.class);
        OptionTypeMap<Class<? extends Evaluator.Builder>> cancellatorType = new OptionTypeMap<>(orders);
        
        options.addOption().setBundleName(OptionsParam.PARAM_OPTIONS)
            .setIdentifier(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_NUMBER_TYPE)
            .setDefault(EvaluatorDouble.Builder.class)
            .setType(cancellatorType)
            .setCategory(category)
            .setCommandLine().setGui().setWeb()
            .build();
    }

    private void addDagOptions(Options options, Category category) {
        assert options != null;
        assert category != null;
        options.addOption().setBundleName(OptionsParam.PARAM_OPTIONS)
            .setIdentifier(OptionsParam.PARAM_DAG_USE_PROB_SIMPLIFIER)
            .setDefault(true)
            .setType(OptionTypeBoolean.getInstance())
            .setCategory(category)
            .setCommandLine().setGui().setWeb()
            .build();

        options.addOption().setBundleName(OptionsParam.PARAM_OPTIONS)
            .setIdentifier(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_BITS)
            .setDefault(32)
            .setType(OptionTypeInteger.getInstance())
            .setCategory(category)
            .setCommandLine().setGui().setWeb()
            .build();
    
        options.addOption().setBundleName(OptionsParam.PARAM_OPTIONS)
            .setIdentifier(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_DOUBLE_CUTOFF_BIN_DIGITS)
            .setDefault(8)
            .setType(OptionTypeIntegerNonNegative.getInstance())
            .setCategory(category)
            .setCommandLine().setGui().setWeb()
            .build();
    
        
        Map<String, Class<? extends NodeStore.Builder>> nodeStorage = new OrderedMap<>(true);
        nodeStorage.put(NodeStoreArray.IDENTIFIER, NodeStoreArray.Builder.class);
        nodeStorage.put(NodeStoreDisk.IDENTIFIER, NodeStoreDisk.Builder.class);
        OptionTypeMap<Class<? extends NodeStore.Builder>> nodeStorageType = new OptionTypeMap<>(nodeStorage);
        options.addOption().setBundleName(OptionsParam.PARAM_OPTIONS)
            .setIdentifier(OptionsParam.PARAM_DAG_NODE_STORE)
            .setDefault(NodeStoreArray.Builder.class)
            .setType(nodeStorageType)
            .setCategory(category)
            .setCommandLine().setGui().setWeb()
            .build();
        
        Map<String, Class<? extends NodeLookup.Builder>> nodeLookup = new OrderedMap<>(true);
        nodeLookup.put(NodeLookupHashMap.IDENTIFIER, NodeLookupHashMap.Builder.class);
        nodeLookup.put(NodeLookupBoundedHashMap.IDENTIFIER, NodeLookupBoundedHashMap.Builder.class);
        OptionTypeMap<Class<? extends NodeLookup.Builder>> nodeLookupType = new OptionTypeMap<>(nodeLookup);
        options.addOption().setBundleName(OptionsParam.PARAM_OPTIONS)
            .setIdentifier(OptionsParam.PARAM_DAG_NODE_LOOKUP)
            .setDefault(NodeLookupHashMap.Builder.class)
            .setType(nodeLookupType)
            .setCategory(category)
            .setCommandLine().setGui().setWeb()
            .build();
        
        Map<String, Class<? extends DoubleLookup.Builder>> lookup = new OrderedMap<>(true);
        lookup.put(DoubleLookupHashMap.IDENTIFIER, DoubleLookupHashMap.Builder.class);
        lookup.put(DoubleLookupTreeMap.IDENTIFIER, DoubleLookupTreeMap.Builder.class);
        lookup.put(DoubleLookupSearchStore.IDENTIFIER, DoubleLookupSearchStore.Builder.class);
        lookup.put(DoubleLookupBoundedHashMap.IDENTIFIER, DoubleLookupBoundedHashMap.Builder.class);
        OptionTypeMap<Class<? extends DoubleLookup.Builder>> lookupType = new OptionTypeMap<>(lookup);
        options.addOption().setBundleName(OptionsParam.PARAM_OPTIONS)
            .setIdentifier(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_DOUBLE_PROB_LOOKUP)
            .setDefault(DoubleLookupHashMap.Builder.class)
            .setType(lookupType)
            .setCategory(category)
            .setCommandLine().setGui().setWeb()
            .build();
        
        addPolynomialFractionExporters(options, category);
        addDagExporters(options, category);
        addDagProbabilisticSimplifierOptions(options, category);
        addPointsOptions(options, category);
    }
    
    private void addPolynomialFractionExporters(Options options, Category category) {
        assert options != null;
        assert category != null;
        Map<String, Class<? extends PolynomialFractionExporter.Builder>> exporters = new OrderedMap<>(true);
        exporters.put(PolynomialFractionExporterSimple.IDENTIFIER, PolynomialFractionExporterSimple.Builder.class);
        exporters.put(PolynomialFractionExporterDag.IDENTIFIER, PolynomialFractionExporterDag.Builder.class);
        exporters.put(PolynomialFractionExporterPoints.IDENTIFIER, PolynomialFractionExporterPoints.Builder.class);
        OptionTypeMap<Class<? extends PolynomialFractionExporter.Builder>> exporterType = new OptionTypeMap<>(exporters);
        options.addOption().setBundleName(OptionsParam.PARAM_OPTIONS)
            .setIdentifier(OptionsParam.PARAM_FRACTION_EXPORTER)
            .setDefault(PolynomialFractionExporterSimple.Builder.class)
            .setType(exporterType)
            .setCategory(category)
            .setCommandLine().setGui().setWeb()
            .build();
    }

    private void addDagExporters(Options options, Category category) {
        assert options != null;
        assert category != null;
        Map<String, Class<? extends DagExporter.Builder>> exporters = new OrderedMap<>(true);
        exporters.put(ExporterC.IDENTIFIER, ExporterC.Builder.class);
        exporters.put(ExporterCInterval.IDENTIFIER, ExporterCInterval.Builder.class);
        exporters.put(ExporterSimple.IDENTIFIER, ExporterSimple.Builder.class);
        exporters.put(ExporterGraphviz.IDENTIFIER, ExporterGraphviz.Builder.class);
        exporters.put(ExporterEntryNumber.IDENTIFIER, ExporterEntryNumber.Builder.class);
        exporters.put(ExporterGinsh.IDENTIFIER, ExporterGinsh.Builder.class);
        exporters.put(ExporterJson.IDENTIFIER, ExporterJson.Builder.class);
        exporters.put(ExporterPoints.IDENTIFIER, ExporterPoints.Builder.class);
        OptionTypeMap<Class<? extends DagExporter.Builder>> exporterType = new OptionTypeMap<>(exporters);
        options.addOption().setBundleName(OptionsParam.PARAM_OPTIONS)
            .setIdentifier(OptionsParam.PARAM_DAG_EXPORTER)
            .setDefault(ExporterC.Builder.class)
            .setType(exporterType)
            .setCategory(category)
            .setCommandLine().setGui().setWeb()
            .build();
    }

    private void addDagProbabilisticSimplifierOptions(Options options, Category category) {
        Map<String, Class<? extends DoubleStore.Builder>> storage = new OrderedMap<>(true);
        storage.put(DoubleStoreArray.IDENTIFIER, DoubleStoreArray.Builder.class);
        storage.put(DoubleStoreDisk.IDENTIFIER, DoubleStoreDisk.Builder.class);
        
        OptionTypeMap<Class<? extends DoubleStore.Builder>> storageType = new OptionTypeMap<>(storage);
        options.addOption().setBundleName(OptionsParam.PARAM_OPTIONS)
            .setIdentifier(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_DOUBLE_PROB_STORAGE)
            .setDefault(DoubleStoreArray.Builder.class)
            .setType(storageType)
            .setCategory(category)
            .setCommandLine().setGui().setWeb()
            .build();
        
        Map<String, Class<? extends DoubleLookup.Builder>> lookup = new OrderedMap<>(true);
        lookup.put(DoubleLookupHashMap.IDENTIFIER, DoubleLookupHashMap.Builder.class);
        lookup.put(DoubleLookupTreeMap.IDENTIFIER, DoubleLookupTreeMap.Builder.class);
        lookup.put(DoubleLookupSearchStore.IDENTIFIER, DoubleLookupSearchStore.Builder.class);
        lookup.put(DoubleLookupBoundedHashMap.IDENTIFIER, DoubleLookupBoundedHashMap.Builder.class);
        
        OptionTypeMap<Class<? extends DoubleLookup.Builder>> lookupType = new OptionTypeMap<>(lookup);
        options.addOption().setBundleName(OptionsParam.PARAM_OPTIONS)
            .setIdentifier(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_DOUBLE_PROB_LOOKUP)
            .setDefault(DoubleLookupHashMap.Builder.class)
            .setType(lookupType)
            .setCategory(category)
            .setCommandLine().setGui().setWeb()
            .build();
        
    }

    private void addFunctionTypes(Options options, Category category) {
        Map<String, Class<? extends TypeFunction.Builder>> orders = new OrderedMap<>(true);
        orders.put(TypePolynomialFraction.IDENTIFIER, TypePolynomialFraction.Builder.class);
        orders.put(TypeDag.IDENTIFIER, TypeDag.Builder.class);
        OptionTypeMap<Class<? extends TypeFunction.Builder>> functionType = new OptionTypeMap<>(orders);
        options.addOption().setBundleName(OptionsParam.PARAM_OPTIONS)
            .setIdentifier(OptionsParam.PARAM_FUNCTION_TYPE)
            .setDefault(TypeDag.Builder.class)
            .setType(functionType)
            .setCategory(category)
            .setCommandLine().setGui().setWeb()
            .build();
    }
    
    private void addPointsOptions(Options options, Category category) {
        options.addOption().setBundleName(OptionsParam.PARAM_OPTIONS)
            .setIdentifier(OptionsParam.PARAM_POINTS)
            .setType(OptionTypeString.getInstance())
            .setCategory(category)
            .setCommandLine().setGui().setWeb()
            .build();
        
        Map<String, Class<? extends Points.Builder>> points = new OrderedMap<>(true);
        points.put(PointsList.IDENTIFIER, PointsList.Builder.class);
        points.put(PointsRange.IDENTIFIER, PointsRange.Builder.class);
        points.put(PointsRangeIntervals.IDENTIFIER, PointsRangeIntervals.Builder.class);
        points.put(PointsListIntervals.IDENTIFIER, PointsListIntervals.Builder.class);
        OptionTypeMap<Class<? extends Points.Builder>> pointsType = new OptionTypeMap<>(points);
        options.addOption().setBundleName(OptionsParam.PARAM_OPTIONS)
            .setIdentifier(OptionsParam.PARAM_POINTS_TYPE)
            .setType(pointsType)
            .setCategory(category)
            .setDefault(PointsList.IDENTIFIER)
            .setCommandLine().setGui().setWeb()
            .build();
        
        Map<String, Class<? extends FunctionEvaluator.Builder>> functionEvaluators = new OrderedMap<>(true);
        functionEvaluators.put(EvaluatorDagSimpleGeneral.IDENTIFIER, EvaluatorDagSimpleGeneral.Builder.class);
        functionEvaluators.put(EvaluatorDagDoubleJava.IDENTIFIER, EvaluatorDagDoubleJava.Builder.class);
        functionEvaluators.put(EvaluatorDagDoubleIntervalJava.IDENTIFIER, EvaluatorDagDoubleIntervalJava.Builder.class);
        functionEvaluators.put(EvaluatorDagGMPNative.IDENTIFIER, EvaluatorDagGMPNative.Builder.class);
        functionEvaluators.put(EvaluatorPolynomialFractionSimpleGeneral.IDENTIFIER, EvaluatorPolynomialFractionSimpleGeneral.Builder.class);
        OptionTypeStringListSubsetDirect<Class<? extends FunctionEvaluator.Builder>> functionEvaluatorsType = new OptionTypeStringListSubsetDirect<>(functionEvaluators);
        options.addOption().setBundleName(OptionsParam.PARAM_OPTIONS)
            .setIdentifier(OptionsParam.PARAM_POINTS_EVALUATORS)
            .setType(functionEvaluatorsType)
            .setCategory(category)
            .setCommandLine().setGui().setWeb()
            .build();
        
        Map<String, Class<? extends PointResultsExporter.Builder>> resultsExporter = new OrderedMap<>(true);
        resultsExporter.put(PointResultsExporterData.IDENTIFIER, PointResultsExporterData.Builder.class);
        resultsExporter.put(PointResultsExporterPgfplots.IDENTIFIER, PointResultsExporterPgfplots.Builder.class);
        OptionTypeMap<Class<? extends PointResultsExporter.Builder>> resultsExporterType = new OptionTypeMap<>(resultsExporter);
        options.addOption().setBundleName(OptionsParam.PARAM_OPTIONS)
            .setIdentifier(OptionsParam.PARAM_POINTS_EXPORTER)
            .setType(resultsExporterType)
            .setCategory(category)
            .setCommandLine().setGui().setWeb()
            .build();

        Map<String, Class<? extends TypeProvider>> types = new OrderedMap<>(true);
        types.put(TypeProviderDouble.IDENTIFIER, TypeProviderDouble.class);
        types.put(TypeProviderRational.IDENTIFIER, TypeProviderRational.class);
        types.put(TypeProviderIntervalDouble.IDENTIFIER, TypeProviderIntervalDouble.class);
        types.put(TypeProviderIntervalRational.IDENTIFIER, TypeProviderIntervalRational.class);
        OptionTypeMap<Class<? extends TypeProvider>> typesType = new OptionTypeMap<>(types);
        options.addOption().setBundleName(OptionsParam.PARAM_OPTIONS)
            .setIdentifier(OptionsParam.PARAM_POINTS_EVALUATOR_RESULT_TYPE)
            .setType(typesType)
            .setDefault(TypeProviderDouble.IDENTIFIER)
            .setCategory(category)
            .setCommandLine().setGui().setWeb()
            .build();

        Map<String, Class<? extends ValueFormat>> valueFormats = new OrderedMap<>(true);
        valueFormats.put(ValueFormatFraction.IDENTIFIER, ValueFormatFraction.class);
        valueFormats.put(ValueFormatDoubleHex.IDENTIFIER, ValueFormatDoubleHex.class);
        valueFormats.put(ValueFormatDouble.IDENTIFIER, ValueFormatDouble.class);
        OptionTypeMap<Class<? extends ValueFormat>> valueFormatsType = new OptionTypeMap<>(valueFormats);

        Map<String, Class<? extends IntervalFormat>> intervalFormats = new OrderedMap<>(true);
        intervalFormats.put(IntervalFormatInterval.IDENTIFIER, IntervalFormatInterval.class);
        intervalFormats.put(IntervalFormatTwoValues.IDENTIFIER, IntervalFormatTwoValues.class);
        OptionTypeMap<Class<? extends IntervalFormat>> intervalFormatType = new OptionTypeMap<>(intervalFormats);
        
        options.addOption().setBundleName(OptionsParam.PARAM_OPTIONS)
            .setIdentifier(OptionsParam.PARAM_POINTS_EXPORTER_POINT_FORMAT)
            .setType(valueFormatsType)
            .setCategory(category)
            .setCommandLine().setGui().setWeb()
            .build();

        options.addOption().setBundleName(OptionsParam.PARAM_OPTIONS)
            .setIdentifier(OptionsParam.PARAM_POINTS_EXPORTER_POINT_INTERVAL_FORMAT)
            .setType(intervalFormatType)
            .setCategory(category)
            .setCommandLine().setGui().setWeb()
            .build();
        options.addOption().setBundleName(OptionsParam.PARAM_OPTIONS)
            .setIdentifier(OptionsParam.PARAM_POINTS_EXPORTER_RESULT_FORMAT)
            .setType(valueFormatsType)
            .setCategory(category)
            .setCommandLine().setGui().setWeb()
            .build();

        options.addOption().setBundleName(OptionsParam.PARAM_OPTIONS)
            .setIdentifier(OptionsParam.PARAM_POINTS_EXPORTER_RESULT_INTERVAL_FORMAT)
            .setType(intervalFormatType)
            .setCategory(category)
            .setCommandLine().setGui().setWeb()
            .build();
        
        options.addOption().setBundleName(OptionsParam.PARAM_OPTIONS)
            .setIdentifier(OptionsParam.PARAM_POINTS_EXPORTER_POINT_FORCE_INTERVAL)
            .setType(OptionTypeBoolean.getInstance())
            .setCategory(category)
            .setCommandLine().setGui().setWeb()
            .build();
        
        options.addOption().setBundleName(OptionsParam.PARAM_OPTIONS)
            .setIdentifier(OptionsParam.PARAM_POINTS_EXPORTER_RESULT_FORCE_INTERVAL)
            .setType(OptionTypeBoolean.getInstance())
            .setCategory(category)
            .setCommandLine().setGui().setWeb()
            .build();
    }
}
