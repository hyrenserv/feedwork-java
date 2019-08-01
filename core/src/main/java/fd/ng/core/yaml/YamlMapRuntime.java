package fd.ng.core.yaml;

import fd.ng.core.exception.internal.FrameworkRuntimeException;
import fd.ng.core.utils.StringUtil;
import fd.ng.core.utils.Validator;

import java.util.*;

/**
 * YAML Map ( 运行时，用代码动态构建的 yaml mapping 对象 )
 * http://yaml.org/spec/1.2/spec.html#mapping//
 */
public class YamlMapRuntime extends AbstractYamlMap {

    /**
     * Key:value tree map (ordered keys).
     */
    private final Map<YamlNode, YamlNode> mappings = new TreeMap<>();

    public YamlMapRuntime(final Map<YamlNode, YamlNode> entries) {
        this.mappings.putAll(entries);
    }

    @Override
    public YamlMap getMap(final String key) {
        return this.getMap(new Scalar(key));
    }

    @Override
    public YamlMap getMap(final YamlNode key) {
        final YamlNode value = this.mappings.get(key);
        final YamlMap found;
        if (value != null && value instanceof YamlMap) {
            found = (YamlMap) value;
        } else {
            found = null;
        }
        return found;
    }

    @Override
    public YamlArray getArray(final String key) {
        return this.getArray(new Scalar(key));
    }

    @Override
    public YamlArray getArray(final YamlNode key) {
        final YamlNode value = this.mappings.get(key);
        final YamlArray found;
        if (value != null && value instanceof YamlArray) {
            found =  (YamlArray) value;
        } else {
            found = null;
        }
        return found;
    }

    @Override
    public String value(final String key) {
        return this.value(new Scalar(key));
    }

    @Override
    public String value(final YamlNode key) {
        final YamlNode value = this.mappings.get(key);
        final String found;
        if (value != null && value instanceof Scalar) {
            found = ((Scalar) value).value();
        } else {
            found = null;
        }
        return found;
    }

    @Override
    public Collection<YamlNode> children() {
        return this.mappings.values();
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
        for(final Map.Entry<YamlNode, YamlNode> entry
            : this.mappings.entrySet()
        ) {
            print.append(indent);
            YamlNode key = entry.getKey();
            YamlNode value = entry.getValue();
            if(key instanceof Scalar) {
                print.append(key.toString()).append(": ");
                if (value instanceof Scalar) {
                    print.append(value.toString()).append("\n");
                } else  {
                    print
                    .append("\n")
                    .append(value.indent(indentation + 2))
                        .append("\n");
                }
            } else {
                print.append("?\n").append(key.indent(indentation + 2))
                    .append("\n").append(indent).append(":\n")
                    .append(value.indent(indentation + 2))
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
    public Set<YamlNode> keys() {
        return new HashSet<>(this.mappings.keySet());
    }

    @Override
    public boolean exist(final String key) {
	    return this.mappings.containsKey(new Scalar(key));
    }

	@Override
	protected String value2String(final String key) {
		return this.value(new Scalar(key));
	}
}
