package org.group10.utils.instrument;

import java.util.HashMap;
import java.util.Map;

/**
 * A custom {@link ClassLoader} that allows loading classes from in-memory byte arrays.
 */
public class InstrumentingClassLoader extends ClassLoader {
    /**
     * Stores class names mapped to their bytecode.
     */
    private final Map<String, byte[]> classes = new HashMap<>();

    /**
     * Adds a class to this loader from its byte array representation. <br>
     *
     * The added class can later be loaded using {@link #loadClass(String)}.
     *
     * @param className the fully qualified name of the class (e.g., "com.example.MyClass")
     * @param bytes     the bytecode of the class
     */
    public void addClass(String className, byte[] bytes) {
        classes.put(className, bytes);
    }

    /**
     * Finds and loads the class with the specified name.
     * <p>
     * This method first checks if the class was added via {@link #addClass(String, byte[])}.
     * If found, it defines the class from the provided bytecode. Otherwise, it delegates
     * to the parent class loader.
     * </p>
     *
     * @param name the fully qualified name of the class
     * @return the resulting {@link Class} object
     * @throws ClassNotFoundException if the class could not be found
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes = classes.get(name);
        if (bytes != null) {
            return defineClass(name, bytes, 0, bytes.length);
        }
        return super.findClass(name);
    }
}
