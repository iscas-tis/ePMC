package epmc.util.map;

final class ProbingFunctionLinear implements ProbingFunction {
    private int fieldSize;

    @Override
    public void setFieldSize(int size) {
        assert size >= 0;
        this.fieldSize = size;
    }

    @Override
    public int getPosition(int initial, int test) {
        assert initial >= 0 : initial;
        assert initial < fieldSize;
        assert test >= 0;
        int position = (initial + test) % fieldSize;
        if (position < 0) {
            position += fieldSize;
        }
        if (test > 0 && position == initial) {
            position = -1;
        }
        return position;
    }
    
    @Override
    public ProbingFunctionLinear clone() {
        ProbingFunctionLinear result = new ProbingFunctionLinear();
        result.setFieldSize(fieldSize);
        return result;
    }
}
