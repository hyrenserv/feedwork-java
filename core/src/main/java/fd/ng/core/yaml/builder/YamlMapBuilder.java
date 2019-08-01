package fd.ng.core.yaml.builder;

import fd.ng.core.yaml.Scalar;
import fd.ng.core.yaml.YamlMap;
import fd.ng.core.yaml.YamlMapRuntime;
import fd.ng.core.yaml.YamlNode;

import java.util.HashMap;
import java.util.Map;

/**
 * YamlMapBuilder implementation.
 * This class is immutable and thread-safe.
 *
 */
public class YamlMapBuilder {

    /**
     * Added pairs.
     */
    private final Map<YamlNode, YamlNode> pairs;

    /**
     * Default ctor.
     */
    public YamlMapBuilder() {
        this(new HashMap<>());
    }

    /**
     * Constructor.
     * @param pairs Pairs used in building the YamlMap.
     */
    public YamlMapBuilder(final Map<YamlNode, YamlNode> pairs) {
        this.pairs = pairs;
    }

    /**
     * Add a pair to the map(Yaml mapping)
     * @param key String
     * @param value String
     * @return This builder
     */
    public YamlMapBuilder add(final String key, final String value) {
        return this.add(new Scalar(key), new Scalar(value));
    }

    /**
     * Add a pair to the mapping.
     * @param key YamlNode (sequence or mapping)
     * @param value String
     * @return This builder
     */
    public YamlMapBuilder add(final YamlNode key, final String value) {
        return this.add(key, new Scalar(value));
    }

    public YamlMapBuilder add(final String key, final YamlNode value) {
        return this.add(new Scalar(key), value);
    }

    public YamlMapBuilder add(final YamlNode key, final YamlNode value) {
        final Map<YamlNode, YamlNode> withAddedPair = new HashMap<>(this.pairs);
        withAddedPair.put(key, value);
        return new YamlMapBuilder(withAddedPair);
    }

    public YamlMap build() {
        return new YamlMapRuntime(this.pairs);
    }

}
