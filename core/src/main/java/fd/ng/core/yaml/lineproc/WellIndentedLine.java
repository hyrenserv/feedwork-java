package fd.ng.core.yaml.lineproc;

/**
 * Decorator for YamlLine to check if the line is well indented relative to the
 * previous one.
 *
 */
public class WellIndentedLine implements YamlLine {

    /**
     * Content line.
     */
    private YamlLine line;
    
    /**
     * Previous line.
     */
    private YamlLine previousLine;
    
    /**
     * Ctor.
     * @param previous YamlLine
     * @param current YamlLine
     */
    public WellIndentedLine(final YamlLine previous, final YamlLine current) {
        this.previousLine = previous;
        this.line = current;
    }
    
    @Override
    public String trimmed() {
        return this.line.trimmed();
    }
    
    @Override
    public int number() {
        return this.line.number();
    }

    /**
     * 检查当前行缩进与上一行缩进的关系是否合法，并返回当前行缩进值
     * @return 当前行的缩进值
     */
    @Override
    public int indentation() {
        final int lineIndent = this.line.indentation();
        final int previousLineIndent = this.previousLine.indentation();
        if(this.previousLine.hasNestedNode()) {
//            if(lineIndent != previousLineIndent+2) 原来强制成了必须多2个空格
            if(lineIndent < (previousLineIndent+2)) // 当前行缩进应该比上一行缩进至少多2个空格
            {
                throw new IllegalStateException(
                    "Indentation of line (" + this.line + "  linenumber=" + this.line.number() + ")"
                     + " should be greater than the previous line(" + this.previousLine + ")'s by 1"
                );
            }
        } else {
            if(lineIndent > previousLineIndent) {
                throw new IllegalStateException(
                    "Indentation of line ("+ this.line + "  linenumber=" + this.line.number() + ") must be equal to "
                    + "previous(" + this.previousLine + ")"
                );
            }
        }
        return lineIndent;
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
