package fd.ng.core.utils.translate;

import fd.ng.core.utils.StringUtil;

import java.io.IOException;
import java.io.Writer;

public final class CsvTranslators {

    /** Comma character. */
    private static final char CSV_DELIMITER = ',';
    /** Quote character. */
    private static final char CSV_QUOTE = '"';
    /** Quote character converted to string. */
    private static final String CSV_QUOTE_STR = String.valueOf(CSV_QUOTE);
    /** Escaped quote string. */
    private static final String CSV_ESCAPED_QUOTE_STR = CSV_QUOTE_STR + CSV_QUOTE_STR;
    /** CSV key characters in an array. */
    private static final char[] CSV_SEARCH_CHARS =
            new char[] {CSV_DELIMITER, CSV_QUOTE, StringUtil.CR, StringUtil.LF};

    /** Hidden constructor. */
    private CsvTranslators() { }

    /**
     * Translator for escaping Comma Separated Values.
     */
    public static class CsvEscaper extends SinglePassTranslator {

        @Override
        void translateWhole(final CharSequence input, final Writer out) throws IOException {
            final String inputSting = input.toString();
            if (StringUtil.containsNone(inputSting, CSV_SEARCH_CHARS)) {
                out.write(inputSting);
            } else {
                // input needs quoting
                out.write(CSV_QUOTE);
                out.write(inputSting.replace(CSV_QUOTE_STR, CSV_ESCAPED_QUOTE_STR));
                out.write(CSV_QUOTE);
            }
        }
    }

    /**
     * Translator for unescaping escaped Comma Separated Value entries.
     */
    public static class CsvUnescaper extends SinglePassTranslator {

        @Override
        void translateWhole(final CharSequence input, final Writer out) throws IOException {
            // is input not quoted?
            if (input.charAt(0) != CSV_QUOTE || input.charAt(input.length() - 1) != CSV_QUOTE) {
                out.write(input.toString());
                return;
            }

            // strip quotes
            final String quoteless = input.subSequence(1, input.length() - 1).toString();

            if (StringUtil.containsAny(quoteless, CSV_SEARCH_CHARS)) {
                // deal with escaped quotes; ie) ""
                out.write(quoteless.replace(CSV_ESCAPED_QUOTE_STR, CSV_QUOTE_STR));
            } else {
                out.write(input.toString());
            }
        }
    }
}
