package fd.ng.core.yaml;

import fd.ng.core.yaml.builder.YamlMapBuilder;

import java.util.Collection;
import java.util.Map;

/**
 * An Object represented as a YamlMap.
 *
 */
public final class YamlObjectDump extends AbstractYamlDump {

	/**
	 * Object to dump.
	 */
	private Object obj;

	/**
	 * @param obj Object to dump.
	 */
	public YamlObjectDump(final Object obj) {
		this.obj = obj;
	}

	@Override
	public YamlMap represent() {
		YamlMapBuilder builder = new YamlMapBuilder();
//		Set<Map.Entry<Object, Object>> entries = new BeanMap(this.obj)
//			.entrySet();
//		for (final Map.Entry<Object, Object> entry : entries) {
//			String key = (String) entry.getKey();
//			if(!"class".equals(key)) {
//				Object value = entry.getValue();
//				if(super.leafProperty(value)) {
//					builder = builder
//						.add((String) entry.getKey(), value.toString());
//				} else {
//					builder = builder
//						.add((String) entry.getKey(), this.yamlNode(value));
//				}
//			}
//		}
		return builder.build();
	}


	/**
	 * Convert a complex property to a YamlFactory node.
	 * @param property The property to represent as a YamlNode
	 * @return YamlNode representation of
	 */
	private YamlNode yamlNode(final Object property) {
		YamlNode node;
		if (property instanceof Map) {
			node = new YamlMapDump((Map) property).represent();
		} else if (property instanceof Collection<?>) {
			node = new YamlCollectionDump((Collection) property).represent();
		} else {
			node = new YamlObjectDump(property).represent();
		}
		return node;
	}
}
