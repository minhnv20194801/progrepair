package org.group10.utils.instrument;

import java.util.HashMap;
import java.util.Map;

public class InstrumentingClassLoader extends ClassLoader {
    private final Map<String, byte[]> classes = new HashMap<>();

    public void addClass(String className, byte[] bytes) {
        classes.put(className, bytes);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes = classes.get(name);
        if (bytes != null) {
            return defineClass(name, bytes, 0, bytes.length);
        }
        return super.findClass(name);
    }
}
