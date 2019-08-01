package fd.ng.core.yaml;

import fd.ng.core.yaml.builder.YamlMapBuilder;

import java.util.Map;
import java.util.Set;

/**
 * A Map represented as YamlNode.
 *
 */
public final class YamlMapDump extends AbstractYamlDump {

    /**
     * Map<Object, Object> to dump.
     */
    private Map<Object, Object> map;
    
    /**
     * Ctor.
     * @param map Map to dump
     */
    public YamlMapDump(final Map<Object, Object> map) {
        this.map = map;
    }
    
    @Override
    public YamlMap represent() {
        YamlMapBuilder mapBuilder = new YamlMapBuilder();
        Set<Map.Entry<Object, Object>> entries = this.map.entrySet();
        for (final Map.Entry<Object, Object> entry : entries) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (super.leafProperty(key) && super.leafProperty(value)) {
                mapBuilder = mapBuilder.add(
                    key.toString(),
                    value.toString()
                );
            } else if (super.leafProperty(key)) {
                mapBuilder = mapBuilder.add(
                    key.toString(),
                    new YamlObjectDump(value).represent()
                );
            } else if (super.leafProperty(value)) {
                mapBuilder = mapBuilder.add(
                    new YamlObjectDump(key).represent(),
                    value.toString()
                );
            } else {
                mapBuilder = mapBuilder.add(
                    new YamlObjectDump(key).represent(),
                    new YamlObjectDump(value).represent()
                );
            }
        }
        return mapBuilder.build();
    }

}
