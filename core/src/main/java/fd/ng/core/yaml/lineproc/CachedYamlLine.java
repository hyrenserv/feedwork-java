package fd.ng.core.yaml.lineproc;

/**
 * Decorator class to cache values of trimmed() and indentation() method for 
 * a YamlLine.
 *
 */
public class CachedYamlLine implements YamlLine {

    /**
     * Content line.
     */
    private YamlLine line;

    /**
     * Cached trimmed line.
     */
    private String trimmed;

    /**
     * Cached indentation.
     */
    private int indentation = -1;

    /**
     * Cached value.
     */
    private Boolean hasNestedNode;

    /**
     * Ctor.
     * @param line YamlLine
     */
    public CachedYamlLine(final YamlLine line) {
        this.line = line;
    }

    @Override
    public int compareTo(final YamlLine other) {
        return this.line.compareTo(other);
    }

    @Override
    public String trimmed() {
        if(this.trimmed == null) {
            this.trimmed = this.line.trimmed();
        }
        return this.trimmed;
    }

    @Override
    public int number() {
        return this.line.number();
    }

    @Override
    public int indentation() {
        if(this.indentation == -1) {
            this.indentation = this.line.indentation();
        }
        return this.indentation;
    }

    @Override
    public boolean hasNestedNode() {
        if (this.hasNestedNode == null) {
            this.hasNestedNode = this.line.hasNestedNode();
        }
        return this.hasNestedNode;
    }
    
    @Override
    public String toString() {
        return this.line.toString();
    }

}
