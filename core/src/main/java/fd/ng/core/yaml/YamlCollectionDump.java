package fd.ng.core.yaml;

import fd.ng.core.yaml.builder.YamlArrayBuilder;

import java.util.Collection;

/**
 * A collection represented as YamlNode.
 *
 */
public final class YamlCollectionDump extends AbstractYamlDump {
    /**
     * Collection<Object> to dump.
     */
    private Collection<Object> collection;
    
    /**
     * Ctor.
     * @param collection Collection to dump
     */
    public YamlCollectionDump(final Collection<Object> collection) {
        this.collection = collection;
    }
    
    @Override
    public YamlArray represent() {
        YamlArrayBuilder builder = new YamlArrayBuilder();
        for(final Object element: this.collection) {
            if(super.leafProperty(element)) {
                builder = builder.add(element.toString());
            } else {
                builder = builder.add(new YamlObjectDump(element).represent());
            }
        }
        return builder.build();
    }
}
