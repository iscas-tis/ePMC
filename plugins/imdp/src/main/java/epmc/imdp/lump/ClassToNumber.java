package epmc.imdp.lump;

public interface ClassToNumber {
    interface Builder {
        Builder setSize(int size);

        ClassToNumber build();
    }

    void reset();

    int get(int classs);

    void set(int classs, int number);
}
