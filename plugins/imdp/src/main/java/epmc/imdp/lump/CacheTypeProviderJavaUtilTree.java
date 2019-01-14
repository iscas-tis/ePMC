package epmc.imdp.lump;

import java.util.HashMap;
import java.util.Map;

public final class CacheTypeProviderJavaUtilTree implements CacheTypeProvider {
    public static String IDENTIFIER = "java-util-tree";

    @Override
    public <K,V> Map<K,V> newMap() {
        return new HashMap<>();
    }

}
