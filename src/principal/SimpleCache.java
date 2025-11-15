package principal;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleCache {
    private static class Entry {
        double value;
        Instant expiresAt;
        Entry(double value, Instant expiresAt) { this.value = value; this.expiresAt = expiresAt; }
    }

    private final Map<String, Entry> cache = new ConcurrentHashMap<>();
    private final long ttlSeconds;

    public SimpleCache(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    private String key(String base, String target) {
        return base.toUpperCase() + "_" + target.toUpperCase();
    }

    public void put(String base, String target, double rate) {
        cache.put(key(base,target), new Entry(rate, Instant.now().plusSeconds(ttlSeconds)));
    }

    public Double get(String base, String target) {
        Entry e = cache.get(key(base,target));
        if (e == null) return null;
        if (Instant.now().isAfter(e.expiresAt)) {
            cache.remove(key(base,target));
            return null;
        }
        return e.value;
    }
}
