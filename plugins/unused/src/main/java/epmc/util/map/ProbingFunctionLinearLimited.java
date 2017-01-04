package epmc.util.map;

/**
 * For testing only!
 * 
 * @author Ernst Moritz Hahn
 */
final class ProbingFunctionLinearLimited implements ProbingFunction {
    int size;

    @Override
    public void setFieldSize(int size) {
        assert size >= 0;
        this.size = size;
    }

    @Override
    public int getPosition(int initial, int test) {
        assert initial >= 0;
        assert initial < size;
        assert test >= 0;
        if (test <= 2) {
            int position = (initial + test) % size;
            if (position < 0) {
                position += size;
            }
            if (test > 0 && position == initial) {
                position = -1;
            }
            return position;
        } else {
            return -1;
        }
    }

    @Override
    public ProbingFunctionLinearLimited clone() {
        ProbingFunctionLinearLimited result = new ProbingFunctionLinearLimited();
        result.setFieldSize(size);
        return result;
    }

}
