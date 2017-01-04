package epmc.util.map;

final class ProbingFunctionQuadracticAlternate implements ProbingFunction {
    private int fieldSize;

    @Override
    public void setFieldSize(int size) {
        assert size >= 0;
        this.fieldSize = size;
    }

    @Override
    public int getPosition(int initial, int test) {
        assert initial >= 0;
        assert initial < fieldSize;
        assert test >= 0;
        int qAdd = test * test;
        qAdd *= test % 2 == 1 ? 1 : -1;
        int position = (initial + qAdd) % fieldSize;
        if (position < 0) {
            position += fieldSize;
        }
        if (test > 0 && position == initial) {
            position = -1;
        }
        return position;
    }
    
    @Override
    public ProbingFunctionQuadracticAlternate clone() {
        ProbingFunctionQuadracticAlternate result = new ProbingFunctionQuadracticAlternate();
        result.setFieldSize(fieldSize);
        return result;
    }
}
