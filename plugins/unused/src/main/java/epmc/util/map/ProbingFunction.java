package epmc.util.map;

interface ProbingFunction extends Cloneable {
    void setFieldSize(int size);

    int getPosition(int initial, int test);
    
    ProbingFunction clone();
}
