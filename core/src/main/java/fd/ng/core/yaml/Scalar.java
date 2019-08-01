package fd.ng.core.yaml;

import java.util.Collection;
import java.util.LinkedList;

/**
 * YAML scalar.
 * http://yaml.org/spec/1.2/spec.html#scalar//
 */
public class Scalar implements YamlNode {

    /**
     * This scalar's value.
     */
    private String value;

    public Scalar(final String value) {
        this.value = value;
    }

    /**
     * Value of this scalar.
     * @return Value of type T.
     */
    public String value() {
        return this.value;
    }

    @Override
    public Collection<YamlNode> children() {
        return new LinkedList<YamlNode>();
    }

    /**
     * Equality of two objects.
     * @param anotherObject Reference to righthand object
     * @return True if object are equal and False if are not.
     */
    @Override
    public boolean equals(final Object anotherObject) {
        if (this == anotherObject) {
            return true;
        }
        if (anotherObject == null || getClass() != anotherObject.getClass()) {
            return false;
        }

        final Scalar scalar = (Scalar) anotherObject;

        return this.value.equals(scalar.value);

    }

    /**
     * Hash Code of this scalar.
     * @return Value of hashCode() of type int.
     */
    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    /**
     * Compare this Scalar to another node.<br><br>
     * 
     * A Scalar is always considered less than a Sequence or a Mapping.<br>
     * If o is Scalar then their String values are compared lexicographically
     * 
     * @param other The other AbstractNode.
     * @return
     *  a value < 0 if this < o <br>
     *   0 if this == o or <br>
     *  a value > 0 if this > o
     */
    @Override
    public int compareTo(final YamlNode other) {
        int result = -1;
        if (this == other) {
            result = 0;
        } else if (other == null) {
            result = 1;
        } else if (other instanceof Scalar) {
            result = this.value.compareTo(((Scalar) other).value);
        }
        return result;
    }

    @Override
    public String toString() {
        return this.indent(0);
    }

    @Override
    public String indent(final int indentation) {
        int spaces = indentation;
        StringBuilder printed = new StringBuilder();
        while (spaces > 0) {
            printed.append(" ");
            spaces--;
        }
        return printed.append(this.value).toString();
    }

}
