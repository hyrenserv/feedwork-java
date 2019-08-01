package fd.ng.core.yaml.lineproc;

import fd.ng.core.yaml.YamlNode;

import java.util.*;

/**
 * Ordered YamlLines. Use this decorator only punctually, when it the
 * AbstractYamlLinesProcessor you're working with need to be ordered.
 */
public class OrderedYamlLinesProcessor extends AbstractYamlLinesProcessor {

    /**
     * Lines to order.
     */
    private AbstractYamlLinesProcessor unordered;

    /**
     * @param unordered Decorated lines.
     */
    public OrderedYamlLinesProcessor(final AbstractYamlLinesProcessor unordered) {
        this.unordered = unordered;
    }

    /**
     * Iterates over the lines with the same indentation. The lines
     * are ordered. <br><br> This method is a little more complex - we're not
     * simply sorting the lines. We have to also cover the case when these
     * lines are a sequence; then a line might be just a simple dash,
     * so we have to order these "dash" lines by the node that is nested under
     * them.
     * @return Iterator over the ordered lines.
     */
    @Override
    public Iterator<YamlLine> iterator() {
        final Iterator<YamlLine> lines = this.unordered.iterator();
        final List<YamlLine> ordered = new LinkedList<>();
        final List<YamlLine> dashes = new LinkedList<>();
        final Map<YamlNode, Integer> nodesInSequence = new TreeMap<>();
        int index = 0;
        while (lines.hasNext()) {
            final YamlLine line = lines.next();
            if("-".equals(line.trimmed())) {
                nodesInSequence.put(
                    this.nested(line.number()).toYamlNode(line),
                    index
                );
                dashes.add(line);
                index = index + 1;
            } else {
                ordered.add(line);
            }
        }
        Collections.sort(ordered);
        for (final Integer idx : nodesInSequence.values()) {
            ordered.add(dashes.get(idx));
        }
        return ordered.iterator();
    }

    /**
     * Returns the lines which are nested after the given line. 
     * The lines are not necessarily ordered. If the resulting lines
     * should be ordered (be iterated in order), then they have
     * to be wrapped inside a new OrderedYamlLinesProcessor.
     * @return AbstractYamlLinesProcessor
     * @param after The number of the parent line
     */
    @Override
    public AbstractYamlLinesProcessor nested(final int after) {
        return this.unordered.nested(after);
    }

    @Override
    public int count() {
        return this.unordered.count();
    }

    @Override
    public String indent(final int indentation) {
        final StringBuilder indented = new StringBuilder();
        final Iterator<YamlLine> linesIt = this.iterator();
        if(linesIt.hasNext()) {
            final YamlLine first = linesIt.next();
            final int offset = indentation - first.indentation();
            indented.append(this.indentLine(first, offset));
            while(linesIt.hasNext()) {
                indented.append(
                    this.indentLine(linesIt.next(), offset)
                );
            }
        }
        return indented.toString();
    }

    /**
     * Indent the given line (and its possible nested nodes) with the given
     * offset.
     * @param line YamlLine to indent.
     * @param offset Offset added to its already existing indentation.
     * @return String indented result.
     */
    private String indentLine(final YamlLine line, final int offset){
        final StringBuilder indented = new StringBuilder();
        int indentation = line.indentation() + offset;
        while (indentation > 0) {
            indented.append(" ");
            indentation--;
        }
        indented.append(line.toString()).append("\n");
        if (line.hasNestedNode()) {
            final OrderedYamlLinesProcessor nested = new OrderedYamlLinesProcessor(
                this.unordered.nested(line.number())
            );
            indented.append(nested.indent(indentation + offset));
        }
        return indented.toString();
    }
}
