package fd.ng.core.yaml.lineproc;

/**
 * A decorator class for YamlLine to remove comments from a given YamlLine.
 *
 */
public class NoCommentsYamlLine implements YamlLine {

    /**
     * Original line.
     */
    private YamlLine line;

    /**
     * Ctor.
     * @param line Original YamlLine
     */
    public NoCommentsYamlLine(final YamlLine line) {
        this.line = line;
    }

    @Override
    public int compareTo(final YamlLine other) {
        return this.line.compareTo(other);
    }

    /**
     * 去掉该行的注释
     * @return 去掉注释后的数据
     */
    @Override
    public String trimmed() {
        String trimmed = this.line.trimmed(); // 去掉该行前后空格
        int i = 0;
        while(i < trimmed.length()) {
            if(trimmed.charAt(i) == '#') {
                trimmed = trimmed.substring(0, i);
                break;
            } else if(trimmed.charAt(i) == '"') { // 跳过双引号包裹的整个串
                i++;
                while(i < trimmed.length() && trimmed.charAt(i) != '"') {
                    i++;
                }
            }
            i++;
        }
        return trimmed.trim();
    }

    @Override
    public int number() {
        return this.line.number();
    }

    @Override
    public int indentation() {
        return this.line.indentation();
    }

    @Override
    public boolean hasNestedNode() {
        return new DefaultYamlLine(this.trimmed(), 0).hasNestedNode();
    }

    @Override
    public String toString() {
        return this.line.toString();
    }
}
