package fd.ng.core.yaml.lineproc;

/**
 * A line of yaml.
 */
public interface YamlLine extends Comparable<YamlLine> {

    /**
     * The line's trimmed contents.
     * @return String contents.
     */
    String trimmed();

    /**
     * Number of the line (count start from 0).
     * @return Integer.
     */
    int number();

    /**
     * This line's indentation (number of spaces at the beginning of it).>br>
     * Should be a multiple of 2! If not, IllegalStateException is thrown.
     * @return Integer.
     * @throws IllegalStateException if the indentation is not multiple of 2.
     */
    int indentation();
    
    /**
     * Does this line precede a nested node?
     * @return True or false
     */
    boolean hasNestedNode();
    /**
     * YamlLine null object.
     */
    class NullYamlLine implements YamlLine {

        @Override
        public String trimmed() {
            return "";
        }

        @Override
        public int number() {
            return -1;
        }

        @Override
        public int indentation() {
            return 0;    
        }
        
        @Override
        public int compareTo(final YamlLine other) {
            return -1;
        }

        @Override
        public boolean hasNestedNode() {
            return false;
        }
        
    }
}
