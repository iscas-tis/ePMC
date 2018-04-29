package epmc.imdp.lump;

import java.util.Map;

public interface CacheTypeProvider {
    <K,V> Map<K,V> newMap();
}
