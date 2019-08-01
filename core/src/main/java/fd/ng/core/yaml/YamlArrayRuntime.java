package fd.ng.core.yaml;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * YAML 数组 ( 运行时，用代码动态构建的 yaml sequence 对象 )
 * http://yaml.org/spec/1.2/spec.html#sequence//
 */
public class YamlArrayRuntime extends AbstractYamlArray {

    private final List<YamlNode> nodes = new LinkedList<>();

    public YamlArrayRuntime(final Collection<YamlNode> elements) {
        this.nodes.addAll(elements);
    }

    @Override
    public YamlMap getMap(final int index) {
        final YamlNode value = this.nodes.get(index);
        final YamlMap found;
        if (value instanceof YamlMap) {
            found = (YamlMap) value;
        } else {
            found = null;
        }
        return found;
    }

    @Override
    public YamlArray getArray(final int index) {
        final YamlNode value = this.nodes.get(index);
        final YamlArray found;
        if (value instanceof YamlArray) {
            found = (YamlArray) value;
        } else {
            found = null;
        }
        return found;
    }

    @Override
    public String value(final int index) {
        final YamlNode value = this.nodes.get(index);
        final String found;
        if (value instanceof Scalar) {
            found = ((Scalar) value).value();
        } else {
            found = null;
        }
        return found;
    }

    @Override
    public Collection<YamlNode> children() {
        return new LinkedList<>(this.nodes);
    }

    @Override
    public String toString() {
        return this.indent(0);
    }

    @Override
    public String indent(final int indentation) {
        StringBuilder print = new StringBuilder();
        int spaces = indentation;
        StringBuilder indent = new StringBuilder();
        while (spaces > 0) {
            indent.append(" ");
            spaces--;
        }
        for (final YamlNode node : this.nodes) {
            print.append(indent)
                .append("- ");
            if (node instanceof Scalar) {
                print.append(node.toString()).append("\n");
            } else  {
                print.append("\n").append(node.indent(indentation + 2))
                    .append("\n");
            }
        }
        String printed = print.toString();
        if(printed.length() > 0) {
            printed = printed.substring(0, printed.length() - 1);
        }
        return printed;
    }

    @Override
    public int size() {
        return this.nodes.size();
    }

    @Override
    protected String value2String(final int index) {
        return this.value(index);
    }
}
