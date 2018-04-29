package epmc.imdp.lump;

import java.util.Map;

import gnu.trove.map.hash.THashMap;

public final class CacheTypeProviderJavaUtilTree implements CacheTypeProvider {
    public static String IDENTIFIER = "java-util-tree";

    @Override
    public <K,V> Map<K,V> newMap() {
        return new THashMap<>();
    }

}
