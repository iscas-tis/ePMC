package epmc.util.map;

/**
 * Only for testing purposes!
 * 
 * @author Ernst Moritz Hahn
 */
final class HashFunctionConstantZero implements HashFunction {

    @Override
    public void setArray(int[] array) {
        assert array != null;
    }

    @Override
    public void setNumBits(int numBits) {
        assert numBits >= 0;
    }

    @Override
    public void setFieldSize(int size) {
        assert size >= 0;
    }

    @Override
    public int computeHash() {
        return 0;
    }

    @Override
    public HashFunctionConstantZero clone() {
        return new HashFunctionConstantZero();
    }
}
