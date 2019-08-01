package fd.ng.core.utils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class MapUtil {
	public MapUtil() { throw new AssertionError("No MapUtil instances for you!"); }

	/**
	 * 转置 Map 的 key value
	 */
	public static <K, V> Map<V, K> invert(Map<K, V> source) {
		if ( source.isEmpty() ) return Collections.emptyMap();
		Map<V, K> target = new LinkedHashMap<V, K>(source.size());
		for (Map.Entry<K, V> entry : source.entrySet()) {
			target.put(entry.getValue(), entry.getKey());
		}
		return target;
	}
}
