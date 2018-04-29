package epmc.imdp.lump;

import java.util.HashMap;
import java.util.Map;

public final class CacheTypeProviderJavaUtilHash implements CacheTypeProvider {
    public static String IDENTIFIER = "java-util-hash";

    @Override
    public <K,V> Map<K,V> newMap() {
        return new HashMap<>();
    }
}
