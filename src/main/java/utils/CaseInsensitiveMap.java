package utils;

import java.util.HashMap;

/**
 * Used in the windows application manager to associate applications
 * to their path in a case-independent way.
 * So C:\Windows is equal to C:\WINDOWS:
 */
public class CaseInsensitiveMap<T> extends HashMap<String, T>{
    @Override
    public T get(Object key) {
        return super.get(((String) key).toLowerCase());
    }

    @Override
    public boolean containsKey(Object key) {
        return super.containsKey(((String) key).toLowerCase());
    }

    @Override
    public T put(String key, T value) {
        return super.put(key.toLowerCase(), value);
    }
}
