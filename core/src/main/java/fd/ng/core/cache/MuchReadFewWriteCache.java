package fd.ng.core.cache;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 这是线程安全的类，可以任意方式来使用，比如声明为 static final 全局共享。
 *
 * 使用场景：大量的读，少量的写
 */
public class MuchReadFewWriteCache<K, V> {
	private final Map<K, V> cache = new WeakHashMap<>(); // 缓存自动清理
	private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
	private final ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
	private final ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();

	public V get(K key) {
		readLock.lock();
		try {
			return cache.get(key);
		} finally {
			readLock.unlock();
		}
	}

	public V put(K key, V value){
		writeLock.lock();
		try {
			cache.put(key, value);
			return value;
		} finally {
			writeLock.unlock();
		}
	}

	public V putIfAbsent(K key, V value){
		writeLock.lock();
		try {
			cache.putIfAbsent(key, value);
			return value;
		} finally {
			writeLock.unlock();
		}
	}

	public V remove(K key) {
		writeLock.lock();
		try {
			return cache.remove(key);
		} finally {
			writeLock.unlock();
		}
	}

	public void removeAll() {
		writeLock.lock();
		try {
			cache.clear();
		} finally {
			writeLock.unlock();
		}
	}
}
