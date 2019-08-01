package fd.ng.core.yaml.lineproc;

/**
 * =RtYamlLine
 */
public class DefaultYamlLine implements YamlLine {

    /**
     * Content.
     */
    private String value;

    /**
     * Line nr.
     */
    private int number;

    /**
     * Ctor.
     * @param value Contents of this line.
     * @param number Number of the line.
     */
    public DefaultYamlLine(final String value, final int number) {
        this.value = value;
        this.number = number;
    }

    /**
     * Trim the spaces off.
     * @return String
     */
    @Override
    public String trimmed() {
        return this.value.trim();
    }

    @Override
    public int number() {
        return this.number;
    }

    @Override
    public int indentation() {
        int index = 0;
        while (index < this.value.length() && this.value.charAt(index) == ' '){
            index++;
        }
        return index;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @Override
    public int compareTo(final YamlLine other) {
        int result = -1;
        if (this == other) {
            result = 0;
        } else if (other == null) {
            result = 1;
        } else {
            result = this.trimmed().compareTo(other.trimmed());
        }
        return result;
    }

    @Override
    public boolean hasNestedNode() {
        final boolean result;
        final String specialCharacters = ":>|-";
 
        final CharSequence prevLineLastChar = 
            this.trimmed().substring(this.trimmed().length()-1);
        if(specialCharacters.contains(prevLineLastChar)) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }
}
