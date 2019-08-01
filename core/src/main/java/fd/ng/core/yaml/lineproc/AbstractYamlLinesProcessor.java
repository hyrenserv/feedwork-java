package fd.ng.core.yaml.lineproc;

import fd.ng.core.yaml.*;

/**
 * Iterable yaml lines.
 */
public abstract class AbstractYamlLinesProcessor implements Iterable<YamlLine> {

    /**
     * Lines which are nested after the given YamlLine (lines which are
     * <br> indented by 2 or more spaces beneath it).
     * @param after Number of a YamlLine
     * @return YamlLines
     */
    public abstract AbstractYamlLinesProcessor nested(final int after);

    /**
     * Number of lines.
     * @return Integer.
     */
    public abstract int count();

    /**
     * Indent these lines.
     * @param indentation Spaces to precede each line.
     * @return String with the pretty-printed, indented lines.
     */
    public abstract String indent(int indentation);

    /**
     * Turn these lines into a YamlNode.
     * @param prev Previous YamlLine
     * @return YamlNode
     *  possibilities.
     */
    public YamlNode toYamlNode(final YamlLine prev) {
        final String trimmed = prev.trimmed();
        final String last = trimmed.substring(trimmed.length()-1);
        final YamlNode node;
        switch (last) {
            case NestedType.YAML:
                final boolean sequence = this.iterator()
                    .next().trimmed().startsWith("-");
                if(sequence) {
                    node = new YamlArrayAnywhere(this);
                } else {
                    node = new YamlMapAnywhere(this);
                }
                break;
            case NestedType.KEY_YAML:
                final boolean sequenceKey = this.iterator()
                    .next().trimmed().startsWith("-");
                if(sequenceKey) {
                    node = new YamlArrayAnywhere(this);
                } else {
                    node = new YamlMapAnywhere(this);
                }
                break;
            case NestedType.ARRAY:
                if(trimmed.length() == 1) {
                    final boolean elementSequence = this.iterator()
                        .next().trimmed().startsWith("-");
                    if(elementSequence) {
                        node = new YamlArrayAnywhere(this);
                    } else {
                        node = new YamlMapAnywhere(this);
                    }
                } else {
                    node = new YamlArrayAnywhere(this);
                }
                break;
            case NestedType.PIPED_SCALAR:
                node = new ReadPipeScalar(this);
                break;
            case NestedType.POINTED_SCALAR:
                node = new ReadPointedScalar(this);
                break;
            default:
                throw new IllegalStateException(
                    "No nested YamlFactory node after line " + prev.number()
                    + " which has [" + last + "] character at the end"
                );
        }
        return node;
    }

}
