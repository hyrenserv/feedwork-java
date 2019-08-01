package fd.ng.core.yaml;

import java.math.BigDecimal;

public interface YamlArray extends YamlNode {

    /**
     * The number of Yaml elements (scalars, mappings and sequences)
     * @return Integer.
     */
    int size();

    /**
     * Get the Yaml map  from the given index.
     * @param index array index from 0.
     * @return YamlFactory mapping
     */
    YamlMap getMap(final int index);

    /**
     * Get the Yaml array(sequence)  from the given index.
     * @param index array index from 0.
     * @return YamlFactory sequence
     */
    YamlArray getArray(final int index);

    /**
     * Get the original String from the given index.
     * @param index array index from 0.
     * @return String
     */
    String value(final int index);

    int getInt(final int index);
    int getInt(final int index, final int defaultValue);
    long getLong(final int index);
    long getLong(final int index, final long defaultValue);
    BigDecimal getDecimal(final int index);
    BigDecimal getDecimal(final int index, final BigDecimal defaultValue);
    boolean getBool(final int index);
    boolean getBool(final int index, final boolean defaultValue);
    String getString(final int index);
    String getString(final int index, final String defaultValue);
    <T extends Enum<T>> T getEnum(Class<T> enumClass, final int index);
    <T extends Enum<T>> T getEnum(Class<T> enumClass, final int index, T defaultValue);
}
