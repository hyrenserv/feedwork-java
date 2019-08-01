package fd.ng.core.yaml;

import fd.ng.core.yaml.lineproc.AbstractYamlLinesProcessor;
import fd.ng.core.yaml.lineproc.YamlLine;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Read YamlFactory Scalar when previous yaml line ends with '|' character.
 *
 */
public class ReadPipeScalar implements YamlNode {

    /**
     * Lines to be represented as a wrapped scalar.
     */
    private AbstractYamlLinesProcessor lines;

    /**
     * Ctor.
     * @param lines Given lines to represent.
     */
    public ReadPipeScalar(final AbstractYamlLinesProcessor lines) {
        this.lines = lines;
    }

    @Override
    public int compareTo(final YamlNode other) {
        int result = -1;
        if (this == other) {
            result = 0;
        } else if (other == null) {
            result = 1;
        } else if (other instanceof Scalar) {
            result = this.value().compareTo(((Scalar) other).value());
        } else if (other instanceof ReadPipeScalar) {
            result = this.value().compareTo(((ReadPipeScalar) other).value());
        }
        return result;
    }

    @Override
    public Collection<YamlNode> children() {
        return new LinkedList<YamlNode>();
    }

    @Override
    public String indent(final int indentation) {
        StringBuilder printed = new StringBuilder();
        for(final YamlLine line: this.lines) {
            int spaces = indentation;
            while (spaces > 0) {
                printed.append(" ");
                spaces--;
            }
            printed.append(line.trimmed());
            printed.append('\n');
        }
        printed.delete(printed.length()-1, printed.length());
        return printed.toString();
    }

    /**
     * Value of this scalar.
     * @return String
     */
    public String value() {
        StringBuilder builder = new StringBuilder();
        for(final YamlLine line: this.lines) {
            builder.append(line.trimmed());
            builder.append('\n');
        }
        builder.delete(builder.length()-1, builder.length());
        return builder.toString();
    }
    
    @Override
    public String toString() {
        return this.indent(0);
    }

}
