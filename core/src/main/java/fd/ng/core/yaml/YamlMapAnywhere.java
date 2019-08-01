package fd.ng.core.yaml;

import fd.ng.core.yaml.lineproc.AbstractYamlLinesProcessor;
import fd.ng.core.yaml.lineproc.OrderedYamlLinesProcessor;
import fd.ng.core.yaml.lineproc.YamlLine;

import java.util.*;

/**
 * ReadYamlMapping from somewhere
 * ( 用于从yaml文件中初始装载的数据，默认使用本类获取 yaml mapping 对象 )
 */
public class YamlMapAnywhere extends AbstractYamlMap {

    /**
     * Lines read.
     */
    private AbstractYamlLinesProcessor lines;

    /**
     * @param lines Given lines.
     */
    public YamlMapAnywhere(final AbstractYamlLinesProcessor lines) {
        this.lines = lines;
    }

    @Override
    public Collection<YamlNode> children() {
        final List<YamlNode> kids = new LinkedList<>();
        final OrderedYamlLinesProcessor ordered = new OrderedYamlLinesProcessor(this.lines);
        for (final YamlLine line : ordered) {
            final String trimmed = line.trimmed();
            if("?".equals(trimmed) || ":".equals(trimmed)) {
                continue;
            } else {
                if(trimmed.endsWith(":")) {
                    kids.add(ordered.nested(line.number()).toYamlNode(line));
                } else {
                    final String[] parts = trimmed.split(":");
                    if(parts.length < 2) {
                        throw new IllegalStateException(
                            "Expected ':' on line " + line.number()
                        );
                    } else {
                        kids.add(
                            new Scalar(
                                trimmed.substring(
                                    trimmed.indexOf(":") + 1
                                ).trim()
                            )
                        );
                    }
                }
            }
        }
        return kids;
    }

    @Override
    public YamlMap getMap(final String key) {
        return (YamlMap) this.nodeValue(key, true);
    }

    @Override
    public YamlMap getMap(final YamlNode key) {
        return (YamlMap) this.nodeValue(key, true);
    }

    @Override
    public YamlArray getArray(final String key) {
        return (YamlArray) this.nodeValue(key, false);
    }

    @Override
    public YamlArray getArray(final YamlNode key) {
        return (YamlArray) this.nodeValue(key, false);
    }

    @Override
    public String value(final String key) {
        final int keyLen = key.length();
        for (final YamlLine line : this.lines) { // lines重载了iterator，会先执行RtYamlLines中的iterator方法，实际只循环配置文件中同一级别的所有行
            final String trimmed = line.trimmed();
            if(trimmed.startsWith(key + ":")||trimmed.startsWith(key + "=")) {
                // 标准的YAML格式：key 必须紧跟一个':'。
                // = 形式不是标准的YAML格式
                return trimmed.substring(keyLen + 1).trim();
            } else if(trimmed.startsWith(key + " ")) {
                int newLoc = trimmed.indexOf(":", keyLen+1);
                if(newLoc<0) newLoc = trimmed.indexOf("=", keyLen+1);
                return trimmed.substring(newLoc + 1).trim();
            }
        }
        return null;
    }

    @Override
    public String value(final YamlNode key) {
        String value = null;
        boolean found = false;
        for (final YamlLine line : this.lines) {
            final String trimmed = line.trimmed();
            if("?".equals(trimmed)) {
                final YamlNode keyNode = this.lines.nested(line.number())
                        .toYamlNode(line);
                if(keyNode.equals(key)) {
                    found = true;
                    continue;
                }
            }
            if(found && trimmed.startsWith(":")) {
                value = trimmed.substring(trimmed.indexOf(":") + 1).trim();
                break;
            }
        }
        return value;
    }

    @Override
    public String toString() {
        return this.indent(0);
    }

    @Override
    public String indent(final int indentation) {
        return new OrderedYamlLinesProcessor(this.lines).indent(indentation);
    }

    /**
     * The YamlNode value associated with a String key.
     * @param key String key.
     * @param map Is the value a map or a sequence?
     * @return YamlNode.
     */
    private YamlNode nodeValue(final String key, final boolean map) {
        final int keyLen = key.length();
        YamlNode value = null;
        for (final YamlLine line : this.lines) {
            final String trimmed = line.trimmed();
            boolean matchLine = false;
            if(trimmed.startsWith(key + ":")||trimmed.startsWith(key + "=")) {
                matchLine = true;
            } else if(trimmed.startsWith(key + " ")) {
                int newLoc = trimmed.indexOf(":", keyLen+1);
                if(newLoc<0) newLoc = trimmed.indexOf("=", keyLen+1);
                if(newLoc>-1) matchLine = true;
            }
            if(matchLine) {
                if (map) {
                    value = new YamlMapAnywhere(
                        this.lines.nested(line.number())
                    );
                } else {
                    value = new YamlArrayAnywhere(
                        this.lines.nested(line.number())
                    );
                }
            }
        }
        return value;
    }

    /**
     * The YamlNode value associated with a String key.
     * @param key YamlNode key.
     * @param map Is the value a map or a sequence?
     * @return YamlNode.
     */
    private YamlNode nodeValue(final YamlNode key, final boolean map) {
        YamlNode value = null;
        for (final YamlLine line : this.lines) {
            final String trimmed = line.trimmed();
            if("?".equals(trimmed)) {
                final AbstractYamlLinesProcessor keyLines = this.lines.nested(
                    line.number()
                );
                final YamlNode keyNode = keyLines.toYamlNode(line);
                if(keyNode.equals(key)) {
                    int colonLine = line.number() + keyLines.count() + 1;
                    if (map) {
                        value = new YamlMapAnywhere(
                            this.lines.nested(colonLine)
                        );
                    } else {
                        value = new YamlArrayAnywhere(
                            this.lines.nested(colonLine)
                        );
                    }
                }
            }
        }
        return value;
    }

    @Override
    public Set<YamlNode> keys() {
        final Set<YamlNode> keys = new HashSet<>();
        for (final YamlLine line : this.lines) {
            final String trimmed = line.trimmed();
            if(":".equals(trimmed)) {
                continue;
            } else if ("?".equals(trimmed)) {
                keys.add(this.lines.nested(line.number()).toYamlNode(line));
            } else {
                final String[] parts = trimmed.split(":");
                if(parts.length < 2 && !trimmed.endsWith(":")) {
                    throw new IllegalStateException(
                        "Expected ':' on line " + line.number()
                    );
                } else {
                    keys.add(
                        new Scalar(
                            trimmed.substring(0, trimmed.indexOf(":")).trim()
                        )
                    );
                }
            }
        }
        return keys;
    }

    @Override
    public boolean exist(final String key) {
        for (final YamlLine line : this.lines) {
            final String trimmed = line.trimmed();
            if(foundKey(trimmed, key)) return true;
        }
        return false;
    }

    @Override
    protected String value2String(final String key) {
        return this.value(key);
    }
}
