package fd.ng.core.yaml;

import fd.ng.core.exception.internal.FrameworkRuntimeException;
import fd.ng.core.utils.Validator;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Set;

/**
 * AbstractYamlMap implementing methods which should be the same across
 * all final implementations of YamlMap.
 */
abstract class AbstractYamlMap extends AbstractYamlOper implements YamlMap {

    /**
     * Return the keys' set of this mapping.
     * @return Set of YamlNode keys.
     */
    abstract Set<YamlNode> keys();

    @Override
    public int hashCode() {
        int hash = 0;
        for(final YamlNode key : this.keys()) {
            hash += key.hashCode();
        }
        for(final YamlNode value : this.children()) {
            hash += value.hashCode();
        }
        return hash;
    }

    /**
     * Equals method for YamlMap. It returns true if the compareTo(...)
     * method returns 0.
     * @param other The YamlMap to which this is compared.
     * @return True or false.
     */
    @Override
    public boolean equals(final Object other) {
        final boolean result;
        if (other == null || !(other instanceof YamlMap)) {
            result = false;
        } else if (this == other) {
            result = true;
        } else {
            result = this.compareTo((YamlMap) other) == 0;
        }
        return result;
    }

    /**
     * Compare this Mapping to another node.<br><br>
     *
     * A Mapping is always considered greater than a Scalar or a Sequence.<br>
     *
     * If o is a Mapping, their integer lengths are compared - the one with
     * the greater length is considered greater. If the lengths are equal,
     * then the 2 Mappings are equal if all elements are equal (K==K and V==V).
     * If the elements are not identical, the comparison of the first unequal
     * elements is returned.
     *
     * @param other The other AbstractNode.
     * @checkstyle NestedIfDepth (100 lines)
     * @checkstyle ExecutableStatementCount (100 lines)
     * @return
     *   a value &lt; 0 if this &lt; o <br>
     *   0 if this == o or <br>
     *   a value &gt; 0 if this &gt; o
     */
    @Override
    public int compareTo(final YamlNode other) {
        int result = 0;
        if (other == null || !(other instanceof YamlMap)) {
            result = 1;
        } else if (this != other) {
            final AbstractYamlMap map = (AbstractYamlMap) other;
            final Set<YamlNode> keys = this.keys();
            final Set<YamlNode> otherKeys = map.keys();
            if(keys.size() > otherKeys.size()) {
                result = 1;
            } else if (keys.size() < otherKeys.size()) {
                result = -1;
            } else {
                final Iterator<YamlNode> keysIt = keys.iterator();
                final Iterator<YamlNode> otherKeysIt = otherKeys.iterator();
                final Iterator<YamlNode> values = this.children().iterator();
                final Iterator<YamlNode> otherVals = map.children().iterator();
                int keysComparison;
                int valuesComparison;
                while(values.hasNext()) {
                    keysComparison = keysIt.next()
                        .compareTo(otherKeysIt.next());
                    valuesComparison = values.next()
                        .compareTo(otherVals.next());
                    if(keysComparison != 0) {
                        result = keysComparison;
                        break;
                    }
                    if(valuesComparison != 0) {
                        result = valuesComparison;
                        break;
                    }
                }
            }
        }
        return result;
    }

    protected abstract String value2String(final String key);

    @Override
    public int getInt(final String key) {
        String orgnValue = value2String(key);
        Validator.notNull(orgnValue, "key="+key+" can not get value. Does key exist?");

        try {
            return fixInt(orgnValue);
        } catch (NumberFormatException e) {
            throw new FrameworkRuntimeException("key="+key, e);
        }
    }

    @Override
    public int getInt(final String key, final int defaultValue) {
        String orgnValue = value2String(key);
        if(orgnValue==null) return defaultValue;
        else {
            try {
                return fixInt(orgnValue);
            } catch (NumberFormatException e) {
                throw new FrameworkRuntimeException("key="+key, e);
            }
        }
    }

    @Override
    public long getLong(final String key) {
        String orgnValue = value2String(key);
        Validator.notNull(orgnValue, "key="+key+" can not get value. Does key exist?");

        try {
            return fixLong(orgnValue);
        } catch (NumberFormatException e) {
            throw new FrameworkRuntimeException("key="+key, e);
        }
    }

    @Override
    public long getLong(final String key, final long defaultValue) {
        String orgnValue = value2String(key);
        if(orgnValue==null) return defaultValue;
        else {
            try {
                return fixLong(orgnValue);
            } catch (NumberFormatException e) {
                throw new FrameworkRuntimeException("key="+key, e);
            }
        }
    }

    @Override
    public BigDecimal getDecimal(final String key) {
        String orgnValue = value2String(key);
        Validator.notNull(orgnValue, "key="+key+" can not get value. Does key exist?");

        try {
            return fixDecimal(orgnValue);
        } catch (NumberFormatException e) {
            throw new FrameworkRuntimeException("key="+key, e);
        }
    }

    @Override
    public BigDecimal getDecimal(final String key, final BigDecimal defaultValue) {
        String orgnValue = value2String(key);
        if(orgnValue==null) return defaultValue;
        else {
            try {
                return fixDecimal(orgnValue);
            } catch (NumberFormatException e) {
                throw new FrameworkRuntimeException("key="+key, e);
            }
        }
    }

    @Override
    public boolean getBool(final String key) {
        String orgnValue = value2String(key);
        Validator.notNull(orgnValue, "key="+key+" can not get value. Does key exist?");

        return (orgnValue.equalsIgnoreCase("true")||orgnValue.equalsIgnoreCase("yes"));
    }

    @Override
    public boolean getBool(final String key, final boolean defaultValue) {
        String orgnValue = value2String(key);

        if(orgnValue==null) return defaultValue;
        else return (orgnValue.equalsIgnoreCase("true")||orgnValue.equalsIgnoreCase("yes"));
    }

    @Override
    public String getString(final String key) {
        String orgnValue = value2String(key);
        Validator.notNull(orgnValue, "key="+key+" can not get value. Does key exist?");
        return fixString(orgnValue);
    }

    @Override
    public String getString(final String key, final String defaultValue) {
        String orgnValue = value2String(key);
        if(orgnValue==null) return defaultValue;
        else return fixString(orgnValue);
    }

    @Override
    public <T extends Enum<T>> T getEnum(Class<T> enumClass, final String key) {
        String orgnValue = value2String(key);
        Validator.notNull(orgnValue, "key="+key+" can not get value. Does key exist?");
        return _getEnum(enumClass, orgnValue);
    }

    @Override
    public <T extends Enum<T>> T getEnum(Class<T> enumClass, final String key, T defaultValue) {
        String orgnValue = value2String(key);
        if (orgnValue==null) return defaultValue;
        else return _getEnum(enumClass, orgnValue);
    }

    protected boolean foundKey(final String trimmedLine, final String key) {
        if(trimmedLine.startsWith(key + ":")||trimmedLine.startsWith(key + "=")) {
            return true;
        } else if(trimmedLine.startsWith(key + " ")) {
            final int keyLen = key.length();
            final int lineLen = trimmedLine.length();
            for (int i=keyLen; i<lineLen; i++) {
                char c = trimmedLine.charAt(i);
                if(c==' ') continue;
                else {
                    if(c==':'||c=='=') return true;
                    else return false;
                }
            }
        }
        return false;
    }
}
