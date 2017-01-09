package epmc.graph;

public final class OptionsTypesGraph {
    public static enum StateStorage {
        SMALLEST,
        LONG_ARRAY,
        LONG,
        INT,
    }

    public static enum WrapperGraphSuccessorsSize {
        SMALLEST,
        INT,
        SHORT,
        BYTE
    }

    private OptionsTypesGraph() {
    }
}
