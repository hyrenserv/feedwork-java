package fd.ng.core.yaml;

import fd.ng.core.exception.internal.FrameworkRuntimeException;
import fd.ng.core.utils.Validator;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;

/**
 * AbstractYamlArray implementing methods which should be the same across
 * all final implementations of YamlArray.
 */
abstract class AbstractYamlArray extends AbstractYamlOper implements YamlArray {
    
    @Override
    public int hashCode() {
        int hash = 0;
        for(final YamlNode node : this.children()) {
            hash += node.hashCode();
        }
        return hash;
    }

    /**
     * Equals method for YamlArray. It returns true if the compareTo(...)
     * method returns 0.
     * @param other The YamlArray to which this is compared.
     * @return True or false
     */
    @Override
    public boolean equals(final Object other) {
        final boolean result;
        if (other == null || !(other instanceof YamlArray)) {
            result = false;
        } else if (this == other) {
            result = true;
        } else {
            result = this.compareTo((YamlArray) other) == 0;
        }
        return result;
    }

    /**
     * Compare this Sequence to another node.<br><br>
     *
     * A Sequence is always considered greater than a Scalar and less than
     * a Mapping.<br>
     *
     * If o is a Sequence, their integer lengths are compared - the one with
     * the greater length is considered greater. If the lengths are equal,
     * then the 2 Sequences are equal if all elements are equal. If the
     * elements are not identical, the comparison of the first unequal
     * elements is returned.
     *
     * @param other The other AbstractNode.
     * @return
     *   a value &lt; 0 if this &lt; o <br>
     *   0 if this == o or <br>
     *   a value &gt; 0 if this &gt; o
     */
    @Override
    public int compareTo(final YamlNode other) {
        int result = 0;
        if (other == null || other instanceof Scalar) {
            result = 1;
        } else if (other instanceof YamlMap) {
            result = -1;
        } else if (this != other) {
            final Collection<YamlNode> nodes = this.children();
            nodes.hashCode();
            final Collection<YamlNode> others = other.children();
            if(nodes.size() > others.size()) {
                result = 1;
            } else if (nodes.size() < others.size()) {
                result = -1;
            } else {
                final Iterator<YamlNode> iterator = others.iterator();
                final Iterator<YamlNode> here = nodes.iterator();
                while(iterator.hasNext()) {
                    result = here.next().compareTo(iterator.next());
                    if(result != 0) {
                        break;
                    }
                }
            }
        }
        return result;
    }

    protected abstract String value2String(final int index);

    @Override
    public int getInt(final int index) {
        String orgnValue = value2String(index);
        Validator.notNull(orgnValue, "index="+index+" can not get value. Does key exist?");

        try {
            return fixInt(orgnValue);
        } catch (NumberFormatException e) {
            throw new FrameworkRuntimeException("index="+index, e);
        }
    }

    @Override
    public int getInt(final int index, final int defaultValue) {
        String orgnValue = value2String(index);
        if(orgnValue==null) return defaultValue;
        else {
            try {
                return fixInt(orgnValue);
            } catch (NumberFormatException e) {
                throw new FrameworkRuntimeException("index="+index, e);
            }
        }
    }

    @Override
    public long getLong(final int index) {
        String orgnValue = value2String(index);
        Validator.notNull(orgnValue, "index="+index+" can not get value. Does key exist?");

        try {
            return fixLong(orgnValue);
        } catch (NumberFormatException e) {
            throw new FrameworkRuntimeException("index="+index, e);
        }
    }

    @Override
    public long getLong(final int index, final long defaultValue) {
        String orgnValue = value2String(index);
        if(orgnValue==null) return defaultValue;
        else {
            try {
                return fixLong(orgnValue);
            } catch (NumberFormatException e) {
                throw new FrameworkRuntimeException("index="+index, e);
            }
        }
    }

    @Override
    public BigDecimal getDecimal(final int index) {
        String orgnValue = value2String(index);
        Validator.notNull(orgnValue, "index="+index+" can not get value. Does key exist?");

        try {
            return fixDecimal(orgnValue);
        } catch (NumberFormatException e) {
            throw new FrameworkRuntimeException("index="+index, e);
        }
    }

    @Override
    public BigDecimal getDecimal(final int index, final BigDecimal defaultValue) {
        String orgnValue = value2String(index);
        if(orgnValue==null) return defaultValue;
        else {
            try {
                return fixDecimal(orgnValue);
            } catch (NumberFormatException e) {
                throw new FrameworkRuntimeException("index="+index, e);
            }
        }
    }

    @Override
    public boolean getBool(final int index) {
        String orgnValue = value2String(index);
        Validator.notNull(orgnValue, "index="+index+" can not get value. Does key exist?");

        return (orgnValue.equalsIgnoreCase("true")||orgnValue.equalsIgnoreCase("yes"));
    }

    @Override
    public boolean getBool(final int index, final boolean defaultValue) {
        String orgnValue = value2String(index);

        if(orgnValue==null) return defaultValue;
        else return (orgnValue.equalsIgnoreCase("true")||orgnValue.equalsIgnoreCase("yes"));
    }

    @Override
    public String getString(final int index) {
        String orgnValue = value2String(index);
        Validator.notNull(orgnValue, "index="+index+" can not get value. Does key exist?");
        return fixString(orgnValue);
    }

    @Override
    public String getString(final int index, final String defaultValue) {
        String orgnValue = value2String(index);
        if(orgnValue==null) return defaultValue;
        else return fixString(orgnValue);
    }

    @Override
    public <T extends Enum<T>> T getEnum(Class<T> enumClass, final int index) {
        String orgnValue = value2String(index);
        Validator.notNull(orgnValue, "index="+index+" can not get value. Does key exist?");
        return _getEnum(enumClass, orgnValue);
    }

    @Override
    public <T extends Enum<T>> T getEnum(Class<T> enumClass, final int index, T defaultValue) {
        String orgnValue = value2String(index);
        if (orgnValue==null) return defaultValue;
        else return _getEnum(enumClass, orgnValue);
    }
}
