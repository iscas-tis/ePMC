package epmc.imdp.plugin;

import java.lang.reflect.Method;
import java.util.Map;

import epmc.graph.LowLevel;
import epmc.graphsolver.OptionsGraphsolver;
import epmc.graphsolver.lumping.LumperExplicit;
import epmc.imdp.IntervalPlayer;
import epmc.imdp.bio.LowLevelBioBuilder;
import epmc.imdp.error.preprocessor.PreprocessorExplicitIMDPOne;
import epmc.imdp.graphsolver.GraphSolverExplicitIMDPReachabilityJavaDouble;
import epmc.imdp.graphsolver.GraphSolverExplicitIMDPRewardsCumulativeJavaDouble;
import epmc.imdp.graphsolver.GraphSolverIterativeIMDPMultiObjectiveScheduledJavaDouble;
import epmc.imdp.graphsolver.GraphSolverIterativeIMDPMultiObjectiveWeightedJavaDouble;
import epmc.imdp.lump.CacheTypeProvider;
import epmc.imdp.lump.CacheTypeProviderJavaUtilHash;
import epmc.imdp.lump.CacheTypeProviderJavaUtilTree;
import epmc.imdp.lump.LumperExplicitIMDP;
import epmc.imdp.model.LowLevelIMDPBuilder;
import epmc.imdp.model.ModelIMDP;
import epmc.imdp.model.PropertyIMDP;
import epmc.imdp.options.OptionsIMDP;
import epmc.imdp.options.OptionsIMDPLump;
import epmc.imdp.options.OptionsIMDPModel;
import epmc.imdp.robot.LowLevelRobotBuilder;
import epmc.jani.model.ModelExtensionSemantics;
import epmc.jani.model.OptionsJANIModel;
import epmc.jani.type.imdp.ModelExtensionIMDP;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Category;
import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeEnum;
import epmc.options.OptionTypeMap;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;
import epmc.util.OrderedMap;

/**
 * IMDP plugin class containing method to execute after options creation.
 * 
 * @author Ernst Moritz Hahn
 */
public final class AfterOptionsCreationIMDP implements AfterOptionsCreation {
    public final static String IDENTIFIER = "after-options-creation-imdp";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        assert options != null;
        preparePreprocessorOptions(options);
        prepareGeneralOptions(options);
        prepareModelOptions(options);
        prepareLumpOptions(options);
        prepareGraphsolverOptions(options);
        addLowLevels(options);

        Map<String, Class<? extends ModelExtensionSemantics>> janiToSemantics = options.get(OptionsJANIModel.JANI_MODEL_EXTENSION_SEMANTICS);
        assert janiToSemantics != null;
        janiToSemantics.put(ModelExtensionIMDP.IDENTIFIER, ModelExtensionIMDP.class);
    }

    private void prepareGeneralOptions(Options options) {
        assert options != null;
        Category category = options.addCategory()
                .setBundleName(OptionsIMDP.OPTIONS_IMDP)
                .setIdentifier(OptionsIMDP.IMDP_CATEGORY)
                .build();
        OptionTypeEnum typeInterval = new OptionTypeEnum(IntervalPlayer.class);
        options.addOption().setBundleName(OptionsIMDP.OPTIONS_IMDP)
        .setIdentifier(OptionsIMDP.IMDP_INTERVAL_PLAYER)
        .setType(typeInterval).setDefault(IntervalPlayer.COOPERATIVE)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
    }

    private void preparePreprocessorOptions(Options options) {
        assert options != null;
        Map<String,Class<?>> preprocessors = options.get(OptionsGraphsolver.GRAPHSOLVER_PREPROCESSOR_EXPLICIT_CLASS);
        preprocessors.put(PreprocessorExplicitIMDPOne.IDENTIFIER, PreprocessorExplicitIMDPOne.class);
    }

    private void prepareModelOptions(Options options) {
        assert options != null;
        Category category = options.addCategory()
                .setBundleName(OptionsIMDPModel.OPTIONS_IMDP_MODEL)
                .setIdentifier(OptionsIMDPModel.IMDP_MODEL_CATEGORY)
                .setParent(OptionsIMDP.IMDP_CATEGORY)
                .build();
        OptionTypeMap<Class<?>> modelInputType = options.getType(OptionsModelChecker.MODEL_INPUT_TYPE);
        assert modelInputType != null;
        modelInputType.put(ModelIMDP.IDENTIFIER, ModelIMDP.class);
        Map<String,Class<?>> propertyClasses = options.get(OptionsModelChecker.PROPERTY_CLASS);
        assert propertyClasses != null;
        propertyClasses.put(PropertyIMDP.IDENTIFIER, PropertyIMDP.class);
        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();
        options.addOption().setBundleName(OptionsIMDPModel.OPTIONS_IMDP_MODEL)
        .setIdentifier(OptionsIMDPModel.IMDP_FLATTEN)
        .setType(typeBoolean).setDefault(true)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();
    }

    private void prepareLumpOptions(Options options) {
        assert options != null;
        Category category = options.addCategory()
                .setBundleName(OptionsIMDPLump.OPTIONS_IMDP_LUMP)
                .setIdentifier(OptionsIMDPLump.IMDP_LUMP_CATEGORY)
                .setParent(OptionsIMDP.IMDP_CATEGORY)
                .build();
        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();

        Map<String,Method> lumpMethodsMap = new OrderedMap<>(true);
        try {
            lumpMethodsMap.put("statewise", LumperExplicitIMDP.class.getMethod("lumpPerState"));
            lumpMethodsMap.put("blockewise", LumperExplicitIMDP.class.getMethod("lumpPerBlock"));
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
        OptionTypeMap<Method> optionTypeLumpMethod = new OptionTypeMap<>(lumpMethodsMap);
        options.addOption().setBundleName(OptionsIMDPLump.OPTIONS_IMDP_LUMP)
        .setIdentifier(OptionsIMDPLump.IMDP_LUMP_METHOD)
        .setType(optionTypeLumpMethod)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();

        options.addOption().setBundleName(OptionsIMDPLump.OPTIONS_IMDP_LUMP)
        .setIdentifier(OptionsIMDPLump.IMDP_SHORTCUT_ZERO_ACTIONS)
        .setType(typeBoolean).setDefault(true)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();

        options.addOption().setBundleName(OptionsIMDPLump.OPTIONS_IMDP_LUMP)
        .setIdentifier(OptionsIMDPLump.IMDP_SHORTCUT_SINGLE_ACTION_BEFORE_NORMALISATION)
        .setType(typeBoolean).setDefault(true)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();

        options.addOption().setBundleName(OptionsIMDPLump.OPTIONS_IMDP_LUMP)
        .setIdentifier(OptionsIMDPLump.IMDP_SHORTCUT_UNSIMULABLE_BLOCK)
        .setType(typeBoolean).setDefault(true)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();

        options.addOption().setBundleName(OptionsIMDPLump.OPTIONS_IMDP_LUMP)
        .setIdentifier(OptionsIMDPLump.IMDP_SHORTCUT_SINGLE_ACTION_AFTER_NORMALISATION)
        .setType(typeBoolean).setDefault(true)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();

        options.addOption().setBundleName(OptionsIMDPLump.OPTIONS_IMDP_LUMP)
        .setIdentifier(OptionsIMDPLump.IMDP_PROBLEM_SET_CACHE_BEFORE_NORMALISATION)
        .setType(typeBoolean).setDefault(false)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();

        options.addOption().setBundleName(OptionsIMDPLump.OPTIONS_IMDP_LUMP)
        .setIdentifier(OptionsIMDPLump.IMDP_PROBLEM_SET_CACHE_AFTER_NORMALISATION)
        .setType(typeBoolean).setDefault(true)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();

        Map<String,Class<? extends CacheTypeProvider>> cacheTypeMap = new OrderedMap<>(true);
        cacheTypeMap.put(CacheTypeProviderJavaUtilTree.IDENTIFIER, CacheTypeProviderJavaUtilTree.class);
        cacheTypeMap.put(CacheTypeProviderJavaUtilHash.IDENTIFIER, CacheTypeProviderJavaUtilHash.class);
        OptionTypeMap<Class<? extends CacheTypeProvider>> optionTypeCacheType = new OptionTypeMap<>(cacheTypeMap);
        options.addOption().setBundleName(OptionsIMDPLump.OPTIONS_IMDP_LUMP)
        .setIdentifier(OptionsIMDPLump.IMDP_LP_CACHE_TYPE)
        .setType(optionTypeCacheType)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();

        options.addOption().setBundleName(OptionsIMDPLump.OPTIONS_IMDP_LUMP)
        .setIdentifier(OptionsIMDPLump.IMDP_NO_SELF_COMPARE)
        .setType(typeBoolean).setDefault(true)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();

        Map<String,Method> blockSplitMethodsMap = new OrderedMap<>(true);
        try {
            blockSplitMethodsMap.put("simple", LumperExplicitIMDP.class.getMethod("splitBlockSimple", int.class));
            blockSplitMethodsMap.put("pseudo-signature", LumperExplicitIMDP.class.getMethod("splitBlockPseudoSignature", int.class));
            blockSplitMethodsMap.put("signature", LumperExplicitIMDP.class.getMethod("splitBlockSignature", int.class));
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
        OptionTypeMap<Method> optionTypeBlockSplitMethod = new OptionTypeMap<>(blockSplitMethodsMap);
        options.addOption().setBundleName(OptionsIMDPLump.OPTIONS_IMDP_LUMP)
        .setIdentifier(OptionsIMDPLump.IMDP_SPLIT_BLOCK_METHOD)
        .setType(optionTypeBlockSplitMethod)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();

        options.addOption().setBundleName(OptionsIMDPLump.OPTIONS_IMDP_LUMP)
        .setIdentifier(OptionsIMDPLump.IMDP_SIGNATURE_NORMALISE)
        .setType(typeBoolean).setDefault(false)
        .setCommandLine().setGui().setWeb()
        .setCategory(category).build();

        Map<String,Class<? extends LumperExplicit>> lumpersExplicit = options.get(OptionsGraphsolver.GRAPHSOLVER_LUMPER_EXPLICIT_CLASS);
        assert lumpersExplicit != null;
        lumpersExplicit.put(LumperExplicitIMDP.IDENTIFIER, LumperExplicitIMDP.class);
    }

    private void prepareGraphsolverOptions(Options options) {
        assert options != null;
        Map<String, Class<?>> graphSolverMap = options.get(OptionsGraphsolver.GRAPHSOLVER_SOLVER_CLASS);
        assert graphSolverMap != null;
        graphSolverMap.put(GraphSolverExplicitIMDPReachabilityJavaDouble.IDENTIFIER, GraphSolverExplicitIMDPReachabilityJavaDouble.class);
        graphSolverMap.put(GraphSolverExplicitIMDPRewardsCumulativeJavaDouble.IDENTIFIER, GraphSolverExplicitIMDPRewardsCumulativeJavaDouble.class);
        graphSolverMap.put(GraphSolverIterativeIMDPMultiObjectiveWeightedJavaDouble.IDENTIFIER, GraphSolverIterativeIMDPMultiObjectiveWeightedJavaDouble.class);
        graphSolverMap.put(GraphSolverIterativeIMDPMultiObjectiveScheduledJavaDouble.IDENTIFIER, GraphSolverIterativeIMDPMultiObjectiveScheduledJavaDouble.class);
    }
    
    private void addLowLevels(Options options) {
        assert options != null;
        Map<String,Class<? extends LowLevel.Builder>> map = 
                options.get(OptionsModelChecker.LOW_LEVEL_ENGINE_CLASS);
        map.put(LowLevelIMDPBuilder.IDENTIFIER, LowLevelIMDPBuilder.class);
        map.put(LowLevelBioBuilder.IDENTIFIER, LowLevelBioBuilder.class);
        map.put(LowLevelRobotBuilder.IDENTIFIER, LowLevelRobotBuilder.class);
    }
}
