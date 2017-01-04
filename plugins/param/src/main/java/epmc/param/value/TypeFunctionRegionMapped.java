package epmc.param.value;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeArrayReal;
import epmc.value.TypeNumBitsKnown;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueReal;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TObjectIntHashMap;

final class TypeFunctionRegionMapped implements TypeFunction, TypeNumBitsKnown {
    private final static class Region implements Comparable<Region>, Serializable {
        private static final long serialVersionUID = 1L;
        private final static Point testPoint = null;
        private final ValueArrayFunction functions;
        private final ValueFunction function1;
        private final ValueFunction function2;
        private final ValueFunction testFunction;
        private final ValueReal real1;
        private final ValueReal real2;
        private final ValueReal testReal;
        
        private Region(ValueArrayFunction functions) {
            this.functions = UtilValue.clone(functions);
            // TODO
//            this.functions.sort();
            this.function1 = functions.getType().getEntryType().newValue();
            this.function2 = functions.getType().getEntryType().newValue();
            this.testFunction = functions.getType().getEntryType().newValue();
            // TODO
            this.real1 = null;
            this.real2 = null;
            this.testReal = null;
        }
        
        private Region(TypeFunctionRegionMapped type) {
            this(type.typeArrayFunction.newValue());
        }
        
        @Override
        public int hashCode() {
            return functions.hashCode();
        }
        
        @Override
        public boolean equals(Object obj) {
            assert obj != null;
            if (!(obj instanceof Region)) {
                return false;
            }
            Region other = (Region) obj;
            return functions.equals(other.functions);
        }

        @Override
        public int compareTo(Region other) {
            assert other != null;
            return functions.compareTo(other.functions);
        }

        private boolean almostDisjoint(Region other) throws EPMCException {
            assert other != null;
            for (int i = 0; i < functions.size(); i++) {
                functions.get(function1, i);
                function1.evaluate(real1, testPoint);
                for (int j = 0; j < other.functions.size(); j++) {
                    functions.get(function2, j);
                    if (!function1.equals(function2)) {
                        function1.evaluate(real2, testPoint);
                        testReal.add(real1, real2);
                        if (testReal.isZero()) {
                            testFunction.add(function1, function2);
                            if (testFunction.isZero()) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }
    }

    private final static class RegionMap {
        private final Region[] regions;
        private final Value[] values;

        private RegionMap(Region[] regions, Value[] values) {
            // TODO simplify if possible
            assert regions != null;
            assert values != null;
            for (Region region : regions) {
                assert region !=  null;
            }
            for (Value value : values) {
                assert value != null;
            }
            assert regions.length == values.length;
            this.regions = regions.clone();
            this.values = new Value[values.length];
            for (int i = 0; i < values.length; i++) {
                this.values[i] = UtilValue.clone(values[i]);
            }
            Map<Region,Value> sortMap = new THashMap<>();
            for (int i = 0; i < this.regions.length; i++) {
                sortMap.put(this.regions[i], this.values[i]);
            }
            Arrays.sort(this.regions);
            for (int i = 0; i < this.regions.length; i++) {
                this.values[i] = sortMap.get(this.regions[i]);
            }
        }

        private RegionMap(TypeFunctionRegionMapped type, Value value) {
            this(type.unboundedRegions, makeValueArray(value));
        }
        
        private static Value[] makeValueArray(Value value) {
            Value[] result = new Value[1];
            result[0] = value;
            return result;
        }

        public int size() {
            return regions.length;
        }
        
        @Override
        public int hashCode() {
            int hash = 0;
            hash = Arrays.hashCode(regions) + (hash << 6) + (hash << 16) - hash;
            hash = Arrays.hashCode(values) + (hash << 6) + (hash << 16) - hash;
            return hash;
        }
        
        @Override
        public boolean equals(Object obj) {
            assert obj != null;
            if (!(obj instanceof RegionMap)) {
                return false;
            }
            RegionMap other = (RegionMap) obj;
            if (!Arrays.equals(this.regions, other.regions)) {
                return false;
            }
            if (!Arrays.equals(this.values, other.values)) {
                return false;
            }
            return true;
        }
    }

    private final Type typeFunction;
    private final TypeArrayFunction typeArrayFunction;
    private final Map<Region, Region> regions = new THashMap<>();
    private final Map<RegionMap, RegionMap> regionMaps = new THashMap<>();
    private final TObjectIntMap<RegionMap> regionMapToNumber = new TObjectIntHashMap<>();
    private final List<RegionMap> numberToRegionMap = new ArrayList<>();
    private final Region[] unboundedRegions;
    private ContextValuePARAM context;
    
    TypeFunctionRegionMapped(ContextValuePARAM context) {
        this.context = context;
        this.typeFunction= null; // TODO
//        this.typeFunction = getContext().getTypeFunction();
        this.typeArrayFunction = (TypeArrayFunction) typeFunction.getTypeArray();
        Region unboundedRegion = makeUnique(new Region(this));
        this.unboundedRegions = new Region[1];
        this.unboundedRegions[0] = unboundedRegion;
    }
    
    private Region makeUnique(Region region) {
        assert region != null;
        Region result = regions.get(region);
        if (result == null) {
            result = region;
            regions.put(result, result);
        }
        return result;
    }
    
    private RegionMap makeUnique(RegionMap regionMap) {
        assert regionMap != null;
        RegionMap result = regionMaps.get(regionMap);
        if (result == null) {
            result = regionMap;
            regionMaps.put(result, result);
            regionMapToNumber.put(result, regionMapToNumber.size());
            numberToRegionMap.add(result);
        }
        return result;
    }
    
    private int regionMapToNumber(RegionMap regionMap) {
        assert regionMap != null;
        regionMap = makeUnique(regionMap);
        return regionMapToNumber.get(regionMap);
    }

    @Override
    public ValueFunctionRegionMapped newValue() {
        return new ValueFunctionRegionMapped(this);
    }

    public int newRegion(Value value) {
        assert value != null;
        return regionMapToNumber(new RegionMap(this, value));
    }
    
    int apply(Operator operator, int...operands) {
        assert operands != null;
        for (int operand : operands) {
            assert operand >= 0;
            assert operand < numberToRegionMap.size();
        }
        // TODO cache lookup
        RegionMap[] operandMaps = new RegionMap[operands.length];
        for (int i = 0; i < operands.length; i++) {
            operandMaps[i] = numberToRegionMap.get(operands[i]);
        }
        return regionMapToNumber.get(apply(operator, operandMaps));
    }
    
    private RegionMap apply(Operator operator, RegionMap... operands) {
        assert operator != null;
        assert operands != null;
        for (RegionMap operand : operands) {
            assert operand != null;
        }
        int numCombinations = 1;
        for (RegionMap operand : operands) {
            numCombinations *= operand.size();
        }
        Region[] choiceRegion = new Region[operands.length];
        Value[] choiceValue = new Value[operands.length];
        for (int number = 0; number < numCombinations; number++) {
            int ckNum = number;
            for (int operandNr = 0; operandNr < operands.length; operandNr++) {
                RegionMap map = operands[operandNr];
                int regionNr = ckNum % map.size();
                ckNum /= map.size();
                choiceRegion[operandNr] = map.regions[regionNr];
                choiceValue[operandNr] = map.values[regionNr];
            }
            assert ckNum == 1;
            if (checkConsistency(choiceRegion)) {
                
            }
        }
        
        return null;
    }

    private boolean checkConsistency(Region[] choiceRegion) {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public int getNumBits() {
        return 32;
    }

    String mapToString(int entry) {
        assert entry >= 0;
        assert entry < numberToRegionMap.size();
        return numberToRegionMap.get(entry).toString();
    }

    @Override
    public ContextValue getContext() {
        return this.context.getContextValue();
    }
    
    @Override
	public ContextValuePARAM getContextPARAM() {
    	return this.context;
    }

	@Override
	public boolean isSupportOperator(String identifier) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ValueFunctionRegionMapped getZero() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ValueFunctionRegionMapped getOne() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ValueReal getUnderflow() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ValueReal getOverflow() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeArrayReal getTypeArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canImport(Type type) {
        assert type != null;
        if (this == type) {
            return true;
        }
        return false;
	}
}
