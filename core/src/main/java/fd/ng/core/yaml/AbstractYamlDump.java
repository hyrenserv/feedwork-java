package fd.ng.core.yaml;

/**
 * A YamlFactory representer.
 */
public abstract class AbstractYamlDump {

    /**
     * Turn it into YamlFactory.
     * @return YamlFactory node
     */
    abstract YamlNode represent();
    
    /**
     * Check if the given property is a 'leaf'. For instance, a String is
     * considered a leaf, we don't need to go deeper to check for its
     * properties, we can directly print it.
     * @param property Tested property
     * @return Boolean
     */
    protected final boolean leafProperty(final Object property) {
        boolean leaf = false;
        Class<?> clazz = property.getClass();
        try {
            if(clazz.getName().startsWith("java.lang.")
                || clazz.getName().startsWith("java.util.")){
                if(clazz.getMethod("toString").getDeclaringClass()
                        .equals(clazz)
                ) {
                    leaf = true;
                }
            }
        } catch (final NoSuchMethodException nsme) {
            nsme.printStackTrace();
        } catch (final SecurityException ex) {
            ex.printStackTrace();
        }
        return leaf;
    }
}
