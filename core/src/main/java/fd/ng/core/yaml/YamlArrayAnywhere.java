package fd.ng.core.yaml;

import fd.ng.core.yaml.lineproc.AbstractYamlLinesProcessor;
import fd.ng.core.yaml.lineproc.YamlLine;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * ReadYamlSequence read from somewhere
 * ( 从yaml文件中初始装载的数据，默认使用本类获取 yaml sequence 对象 )
 */
public class YamlArrayAnywhere extends AbstractYamlArray {

    /**
     * Lines read.
     */
    private AbstractYamlLinesProcessor lines;

    /**
     * Ctor.
     * @param lines Given lines.
     */
    public YamlArrayAnywhere(final AbstractYamlLinesProcessor lines) {
        this.lines = lines;
    }

    @Override
    public Collection<YamlNode> children() {
        final List<YamlNode> kids = new LinkedList<>();
        for(final YamlLine line : this.lines) {
            if("-".equals(line.trimmed())) {
                kids.add(this.lines.nested(line.number()).toYamlNode(line));
            } else {
                final String trimmed = line.trimmed();
                kids.add(
                    new Scalar(trimmed.substring(trimmed.indexOf('-')+1).trim())
                );
            }
        }
        return kids;
    }

    @Override
    public String toString() {
        return this.indent(0);
    }

    @Override
    public String indent(final int indentation) {
        return this.lines.indent(indentation);
    }

    @Override
    public YamlMap getMap(final int index) {
        YamlMap mapping = null;
        int count = 0;
        for (final YamlNode node : this.children()) {
            if (count == index && node instanceof YamlMap) {
                mapping = (YamlMap) node;
            }
            count = count + 1;
        }
        return mapping;
    }

    @Override
    public YamlArray getArray(final int index) {
        YamlArray sequence = null;
        int count = 0;
        for (final YamlNode node : this.children()) {
            if (count == index && node instanceof YamlArray) {
                sequence = (YamlArray) node;
            }
            count = count + 1;
        }
        return sequence;
    }

    @Override
    public String value(final int index) {
        String scalar = null;
        int count = 0;
        for (final YamlNode node : this.children()) {
            if (count == index && node instanceof Scalar) {
                scalar = ((Scalar) node).value();
            }
            count = count + 1;
        }
        return scalar;
    }

    @Override
    public int size() {
        int size = 0;
        for(final YamlLine line : this.lines) {
            size = size + 1;
        }
        return size;
    }

    @Override
    protected String value2String(final int index) {
        return this.value(index);
    }
}
