package fd.ng.core.yaml;

/**
 * NestedType YamlFactory types.
 */
public final class NestedType {
    private NestedType() {}

    /**
     * If this is the last char on a line, it means a YamlFactory
     * node should be nested bellow.
     */
    public static final String YAML = ":";
    
    /**
     * If this is the last char on a line, it means that 
     * 1) a YamlFactory array should be wrapped bellow (looks the same as a normal
     *    one, but the char '-' is ommitted from the beginning of its lines).
     * or 
     * 2) a YamlFactory node is bellow it (an element from the current sequence).
     */
    public static final String ARRAY = "-";
    
    /**
     * If this is the last char on a line, it means a pointed wrapped scalar
     * should be nested bellow (see Example 2.15 from YAML spec 1.2).
     */
    public static final String POINTED_SCALAR = ">";
    
    /**
     * If this is the last char on a line, it means a piped wrapped scalar
     * should be nested bellow (all newlines are significant, and
     * taken into account).
     */
    public static final String PIPED_SCALAR = "|";
    
    /**
     * If this is the last char on a line, it means a complex key
     * (mapping or sequence) should be nested bellow.
     */
    public static final String KEY_YAML = "?";
}
