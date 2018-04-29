package epmc.imdp.lump.antagonistic;

import java.util.Map;

import epmc.constraintsolver.ConstraintSolverConfiguration;
import epmc.constraintsolver.Feature;
import epmc.imdp.lump.CacheTypeProvider;
import epmc.imdp.options.OptionsIMDPLump;
import epmc.options.Options;
import epmc.value.TypeWeightTransition;
import epmc.value.ValueInterval;

final class PolytopeSetSolver {
    private final ConstraintSolverConfiguration contextConstraintSolver;
    private final Map<PolytopeSet,PolytopeSet> problemSetCache;
    private final boolean cacheBefore;
    private final boolean cacheAfter;
    private final PolytopeSetNormaliser normaliser;
    private final ValueInterval entry;
    private int numNonNormalisedSuccessfullLookups;
    private int numNormalisedSuccessfullLookups;

    PolytopeSetSolver() {
        contextConstraintSolver = new ConstraintSolverConfiguration();
        contextConstraintSolver.requireFeature(Feature.LP);
        Class<? extends CacheTypeProvider> clazz = Options.get().get(OptionsIMDPLump.IMDP_LP_CACHE_TYPE);
        try {
            problemSetCache = clazz.newInstance().newMap();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        entry = newValueWeightTransition();
        cacheBefore = Options.get().getBoolean(OptionsIMDPLump.IMDP_PROBLEM_SET_CACHE_BEFORE_NORMALISATION);
        cacheAfter = Options.get().getBoolean(OptionsIMDPLump.IMDP_PROBLEM_SET_CACHE_AFTER_NORMALISATION);
        normaliser = new PolytopeSetNormaliser();
    }

    void findMinimal(PolytopeSet result, PolytopeSet problemSet) {
        assert result != null;
        assert problemSet != null;

        PolytopeSet beforeNormalisation = null;
        if (cacheBefore) {
            PolytopeSet cachedResult = problemSetCache.get(problemSet);
            if (cachedResult != null) {
                numNonNormalisedSuccessfullLookups++;
                result.set(cachedResult);
                return;
            }
            beforeNormalisation = problemSet.clone();
        }
        normaliser.normalise(problemSet, true);
        if (cacheAfter) {
            PolytopeSet cachedResult = problemSetCache.get(problemSet);
            if (cachedResult != null) {
                numNormalisedSuccessfullLookups++;
                result.set(cachedResult);
                return;
            }
        }
        solveByLP(result, problemSet);
        /*
		System.out.println("PS");
		System.out.println(problemSet);
		System.out.println("RES");
		System.out.println(result);
		System.out.println("-----");
         */
        result = result.clone();
        if (cacheBefore) {
            problemSetCache.put(beforeNormalisation, result);			
        }
        if (cacheAfter) {
            problemSetCache.put(problemSet.clone(), result);
        }
    }

    private void solveByLP(PolytopeSet result, PolytopeSet polytopeSet) {
        assert result != null;
        assert polytopeSet != null;
        int numClasses = polytopeSet.getNumClasses();
        int numActions = polytopeSet.getNumActions();
        int numMinimalActions = 0;
        result.setDimensions(numClasses, numActions);
        for (int actionNr = 0; actionNr < numActions; actionNr++) {
            boolean isMinimal;
            try (MinimalityChecker minChecker = new MinimalityChecker(contextConstraintSolver, polytopeSet, actionNr)) {
                isMinimal = minChecker.isMinimal();
            }
            if (isMinimal) {
                for (int classNr = 0; classNr < numClasses; classNr++) {
                    polytopeSet.get(actionNr, classNr, entry);
                    result.set(numMinimalActions, classNr, entry);
                }
                numMinimalActions++;
            }
        }
        result.setDimensions(polytopeSet.getNumClasses(), numMinimalActions);
        normaliser.normalise(result, true);
    }

    private ValueInterval newValueWeightTransition() {
        return ValueInterval.as(TypeWeightTransition.get().newValue());
    }
}
