package epmc.dd;

import epmc.error.EPMCException;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.TypeEnumerable;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import epmc.value.ValueEnumerable;

public final class EnumerateSAT {
    @FunctionalInterface
    public interface EnumerateSATCallback {
        void call(Value[] values);
    }
    
    private DD dd;
    private VariableDD[] variables;
    private EnumerateSATCallback callback;

    private Walker ddWalker;
    private Walker cubeWalker;
    private boolean[] marks;
    private Type[] types;
    private ValueEnumerable[] values;
    private int[] highLevelFromTo;
    private int[] highLevelVariables;
    
    public void setBDD(DD dd) {
        assert dd != null;
        assert TypeBoolean.isBoolean(dd.getType());
        this.dd = dd;
    }
    
    public void setVariables(VariableDD[] variables) {
        assert variables != null;
        for (VariableDD variable : variables) {
            assert variable != null;
        }
        this.variables = variables;
    }
    
    public void setCallback(EnumerateSATCallback callback) {
        assert callback != null;
        this.callback = callback;
    }
    
    public void enumerate() throws EPMCException {
        DD cube = buildCube();
        int cubeMaxSize = cubeMaxSize(cube);
        marks = new boolean[cubeMaxSize];
        values = new ValueEnumerable[variables.length];   
        types = new Type[variables.length];
        for (int varNr = 0; varNr < variables.length; varNr++) {
            TypeEnumerable type = TypeEnumerable.asEnumerable(variables[varNr].getType());
            types[varNr] = type;
            values[varNr] = type.newValue();
        }

        int cubeSize = cubeSize(cube);

        highLevelFromTo = new int[variables.length + 1];
        highLevelVariables = new int[cubeSize];
        int lowNr = 0;
        for (int highNr = 0; highNr < variables.length; highNr++) {
            VariableDD variableDD = variables[highNr];
            for (DD dd : variableDD.getDDVariables(0)) {
                highLevelVariables[lowNr] = dd.variable();
                lowNr++;
            }
            highLevelFromTo[highNr + 1] = highLevelFromTo[highNr]
                    + variableDD.getDDVariables(0).size();
        }
        cubeWalker = cube.walker();
        ddWalker = dd.walker();
        recurse();
        cube.dispose();
    }
    
    private int cubeMaxSize(DD cube) {
        assert cube != null;
        int maxVar = 0;
        Walker cubeWalker = cube.walker();
        while (!cubeWalker.isTrue()) {
            maxVar = Math.max(maxVar, cubeWalker.variable());
            cubeWalker.high();
        }
        return maxVar + 1;
    }

    private int cubeSize(DD cube) {
        assert cube != null;
        int size = 0;
        Walker cubeWalker = cube.walker();
        while (!cubeWalker.isTrue()) {
            size++;
            cubeWalker.high();
        }
        return size;
    }

    private DD buildCube() throws EPMCException {
        DD cube = getContextDD().newConstant(true);
        for (VariableDD variableDD : variables) {
            cube = cube.andWith(variableDD.newCube(0));
        }
        return cube;
    }

    private void recurse() {
        if (ddWalker.isFalse()) {
            return;
        } else if (cubeWalker.isLeaf()) {
            assert ddWalker.isLeaf();
            if (ValueBoolean.asBoolean(ddWalker.value()).getBoolean()) {
                terminalCase();
            }
        } else {
            boolean ddMove = !ddWalker.isLeaf() && (cubeWalker.variable() == ddWalker.variable());
            int variable = cubeWalker.variable();
            if (ddMove) {
                ddWalker.low();
            }
            cubeWalker.high();
            marks[variable] = false;
            recurse();
            if (ddMove) {
                ddWalker.back();
                ddWalker.high();
            }
            marks[variable] = true;
            recurse();
            if (ddMove) {
                ddWalker.back();
            }
            cubeWalker.back();
        }
    }
    
    private void terminalCase() {
        for (int highNr = 0; highNr < highLevelFromTo.length - 1; highNr++) {
            int from = highLevelFromTo[highNr];
            int to = highLevelFromTo[highNr + 1];
            int varValue = 0;
            int digit = 1;
            for (int lowNr = from; lowNr < to; lowNr++) {
                int var = highLevelVariables[lowNr];
                boolean value = marks[var];
                if (value) {
                    varValue |= digit;
                }
                digit <<= 1;
            }
            if (varValue >= TypeEnumerable.asEnumerable(types[highNr]).getNumValues()) {
                return;
            }
            values[highNr].setValueNumber(varValue);
        }
        callback.call(values);
    }

    private ContextDD getContextDD() {
        return dd.getContext();
    }
}