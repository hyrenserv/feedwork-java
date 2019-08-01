package fd.ng.core.yaml.builder;

import fd.ng.core.yaml.Scalar;
import fd.ng.core.yaml.YamlArray;
import fd.ng.core.yaml.YamlArrayRuntime;
import fd.ng.core.yaml.YamlNode;

import java.util.LinkedList;
import java.util.List;

/**
 * 在程序中动态构造一个 YamlArray 对象。
 * This class is immutable and thread-safe
 *
 * 用法例子：
 * YamlFactory.getYamlArrayBuilder()
 *      .add("one")
 *      .add("two")
 *      .add("three")
 *      .build()
 */
public class YamlArrayBuilder {
	private final List<YamlNode> nodes;

	public YamlArrayBuilder() {
		this(new LinkedList<YamlNode>());
	}

	public YamlArrayBuilder(final List<YamlNode> nodes) {
		this.nodes = nodes;
	}

	/**
	 * 添加值到数组中（sequence）
	 * @param value String
	 * @return This builder
	 */
	public YamlArrayBuilder add(final String value) {
		return this.add(new Scalar(value));
	}
	public YamlArrayBuilder add(final YamlNode node) {
		final List<YamlNode> list = new LinkedList<>(this.nodes);
		list.add(node);
		return new YamlArrayBuilder(list);
	}

	public YamlArray build() {
		return new YamlArrayRuntime(this.nodes);
	}
}
