package fd.ng.core.yaml;

import fd.ng.core.yaml.lineproc.AbstractYamlLinesProcessor;
import fd.ng.core.yaml.lineproc.YamlLine;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Read YamlFactory Scalar when previous yaml line ends with '>' character.
 */
public class ReadPointedScalar implements YamlNode {

    /**
     * Lines to be represented as a wrapped scalar.
     */
    private AbstractYamlLinesProcessor lines;

    /**
     * Ctor.
     * @param lines Given lines to represent.
     */
    public ReadPointedScalar(final AbstractYamlLinesProcessor lines) {
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
        } else if (other instanceof ReadPointedScalar) {
            result =
                this.value().compareTo(((ReadPointedScalar) other).value());
        }
        return result;
    }

    @Override
    public Collection<YamlNode> children() {
        return new LinkedList<YamlNode>();
    }

    @Override
    public String indent(final int indentation) {
        StringBuilder builder = new StringBuilder();
        for(final YamlLine line: this.lines) {
            if(line.trimmed().length() == 0 || line.indentation() > 0) {
                if(this.doNotEndWithNewLine(builder)) {
                    builder.append('\n');
                }
                int spaces = line.indentation();
                if(spaces > 0) {
                    for(int i = 0; i < spaces + indentation; i++) {
                        builder.append(' ');
                    }
                }
                builder.append(line.trimmed());
                builder.append('\n');
            } else {
                if(this.doNotEndWithNewLine(builder)) {
                    builder.append(' ');
                } else {
                    for(int i = 0; i < indentation; i++) {
                        builder.append(' ');
                    }
                }
                builder.append(line.trimmed());
            }
        }
        return builder.toString();
    }

    /**
     * Checks whether StringBuilder do not end with newline or not.
     * @param builder StringBuilder
     * @return Boolean Whether builder do not end with newline char or not
     */
    private boolean doNotEndWithNewLine(final StringBuilder builder) {
        return builder.length() > 0
                && builder.charAt(builder.length()-1) != '\n';
    }
    /**
     * Value of this scalar.
     * @return String
     */
    public String value() {
        StringBuilder builder = new StringBuilder();
        for(final YamlLine line: this.lines) {
            if(line.trimmed().length() == 0 || line.indentation() > 0) {
                if(this.doNotEndWithNewLine(builder)) {
                    builder.append('\n');
                }
                int indentation = line.indentation();
                for(int i = 0; i < indentation; i++) {
                    builder.append(' ');
                }
                builder.append(line.trimmed());
                builder.append('\n');
            } else {
                if(this.doNotEndWithNewLine(builder)) {
                    builder.append(' ');
                }
                builder.append(line.trimmed());
            }
        }
        return builder.toString();
    }
    
    @Override
    public String toString() {
        return this.indent(0);
    }

}
