package fd.ng.core.yaml;

import java.util.Collection;

/**
 * YAML node.
 */
public interface YamlNode extends Comparable<YamlNode> {

    /**
     * Fetch the child nodes of this node.
     * @return Collection of {@link YamlNode}
     */
    Collection<YamlNode> children();
    
    /**
     * Print this node with a specified indentation.
     * @param indentation Number of preciding spaces of each line.
     * @return String
     */
    String indent(final int indentation);
}
