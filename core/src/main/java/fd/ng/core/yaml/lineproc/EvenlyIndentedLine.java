package fd.ng.core.yaml.lineproc;

/**
 * Any YamlFactory line should have an even indentation (multiple of 2).
 *
 */
public class EvenlyIndentedLine implements YamlLine {

    /**
     * Original line.
     */
    private YamlLine line;
    
    /**
     * Ctor.
     * @param line Original YamlLine
     */
    public EvenlyIndentedLine(final YamlLine line) {
        this.line = line;
    }
    
    @Override
    public String trimmed() {
        return this.line.trimmed();
    }
    
    @Override
    public int number() {
        return this.line.number();
    }

    @Override
    public int indentation() {
        final int indentation = this.line.indentation();
        if (indentation % 2 != 0) {
            throw new IllegalStateException(
                "Indentation of line " + this.line.number() + " is incorrect. "
                + "It is " + indentation + " and it should be a multiple of 2!"
            );
        }
        return indentation;
    }

    @Override
    public String toString() {
        return this.line.toString();
    }

    @Override
    public int compareTo(final YamlLine other) {
        return this.line.compareTo(other);
    }

    @Override
    public boolean hasNestedNode() {
        return this.line.hasNestedNode();
    }
}
