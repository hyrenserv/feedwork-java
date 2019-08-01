package fd.ng.core.cache;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 这是线程安全的类，可以任意方式来使用，比如声明为 static final 全局共享。
 * 当无法对读写频率预测时，使用本cache。如果是大量读少量写的场景，使用 ｛@link fd.ng.core.cache.MuchReadFewWriteCache}
 *
 * 使用ConcurrentHashMap和WeakHashMap做分代缓存。
 * 在put方法里，在插入一个k-v时，先检查eden缓存的容量是不是超了。没有超就直接放入eden缓存，
 * 如果超了则锁定longterm将eden中所有的k-v都放入longterm。再将eden清空并插入k-v。在get方法中，也是优先从eden中找对应的v，
 * 如果没有则进入longterm缓存中查找，找到后就加入eden缓存并返回。
 *
 * 相对常用的对象都能在eden缓存中找到，不常用（有可能被销毁的对象）的则进入longterm缓存，让GC自动回收。
 *
 */
public class OftenCache<K,V> {
	private final int size;
	private final Map<K,V> eden;
	private final Map<K,V> longterm;

	public OftenCache() {
		this(128);
	}

	public OftenCache(int size) {
		this.size = size;
		this.eden = new ConcurrentHashMap<>(size);
		this.longterm = new WeakHashMap<>(size);
	}

	public V get(K k) {
		V v = this.eden.get(k);
		if (v == null) {
			synchronized (longterm) {
				v = this.longterm.get(k);
			}
			if (v != null) {
				this.eden.put(k, v);
			}
		}
		return v;
	}

	public void put(K k, V v) {
		if (this.eden.size() >= size) {
			synchronized (longterm) {
				this.longterm.putAll(this.eden);
			}
			this.eden.clear();
		}
		this.eden.put(k, v);
	}

	public void putIfAbsent(K k, V v) {
		if(this.eden.containsKey(k)) return;
		put(k, v);
	}
}
