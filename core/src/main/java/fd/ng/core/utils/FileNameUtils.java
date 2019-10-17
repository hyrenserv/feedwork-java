package fd.ng.core.utils;

import java.io.File;
import java.util.ArrayList;

/**
 * General filename and filepath manipulation utilities.
 * <p>
 * When dealing with filenames you can hit problems when moving from a Windows
 * based development machine to a Unix based production machine.
 * This class aims to help avoid those problems.
 * <p>
 * <b>NOTE</b>: You may be able to avoid using this class entirely simply by
 * using JDK {@link File File} objects and the two argument constructor
 * {@link File#File(File, String) File(File,String)}.
 * <p>
 * Most methods on this class are designed to work the same on both Unix and Windows.
 * Those that don't include 'System', 'Unix' or 'Windows' in their name.
 * <p>
 * Most methods recognise both separators (forward and back), and both
 * sets of prefixes. See the javadoc of each method for details.
 * <p>
 * This class defines six components within a filename
 * (example C:\dev\project\file.txt):
 * <ul>
 * <li>the prefix - C:\</li>
 * <li>the path - dev\project\</li>
 * <li>the full path - C:\dev\project\</li>
 * <li>the name - file.txt</li>
 * <li>the base name - file</li>
 * <li>the extension - txt</li>
 * </ul>
 * Note that this class works best if directory filenames end with a separator.
 * If you omit the last separator, it is impossible to determine if the filename
 * corresponds to a file or a directory. As a result, we have chosen to say
 * it corresponds to a file.
 * <p>
 * This class only supports Unix and Windows style names.
 * Prefixes are matched as follows:
 * <pre>
 * Windows:
 * a\b\c.txt           --&gt; ""          --&gt; relative
 * \a\b\c.txt          --&gt; "\"         --&gt; current drive absolute
 * C:a\b\c.txt         --&gt; "C:"        --&gt; drive relative
 * C:\a\b\c.txt        --&gt; "C:\"       --&gt; absolute
 * \\server\a\b\c.txt  --&gt; "\\server\" --&gt; UNC
 *
 * Unix:
 * a/b/c.txt           --&gt; ""          --&gt; relative
 * /a/b/c.txt          --&gt; "/"         --&gt; absolute
 * ~/a/b/c.txt         --&gt; "~/"        --&gt; current user
 * ~                   --&gt; "~/"        --&gt; current user (slash added)
 * ~user/a/b/c.txt     --&gt; "~user/"    --&gt; named user
 * ~user               --&gt; "~user/"    --&gt; named user (slash added)
 * </pre>
 * Both prefix styles are matched always, irrespective of the machine that you are
 * currently running on.
 * <p>
 * Origin of code: Excalibur, Alexandria, Tomcat, Commons-Utils.
 *
 * @since 1.1
 */
public class FileNameUtils {

	private static final int NOT_FOUND = -1;

	/**
	 * The extension separator character.
	 *
	 * @since 1.4
	 */
	public static final char EXTENSION_SEPARATOR = '.';

	/**
	 * The extension separator String.
	 *
	 * @since 1.4
	 */
	public static final String EXTENSION_SEPARATOR_STR = Character.toString(EXTENSION_SEPARATOR);

	/**
	 * The Unix separator character.
	 */
	private static final char UNIX_SEPARATOR = '/';

	/**
	 * The Windows separator character.
	 */
	private static final char WINDOWS_SEPARATOR = '\\';

	/**
	 * The system separator character.
	 */
	private static final char SYSTEM_SEPARATOR = File.separatorChar;

	/**
	 * The separator character that is the opposite of the system separator.
	 */
	private static final char OTHER_SEPARATOR;

	static {
		if (isSystemWindows()) {
			OTHER_SEPARATOR = UNIX_SEPARATOR;
		} else {
			OTHER_SEPARATOR = WINDOWS_SEPARATOR;
		}
	}

	/**
	 * Instances should NOT be constructed in standard programming.
	 */
	public FileNameUtils() {
		super();
	}

	//-----------------------------------------------------------------------

	/**
	 * Determines if Windows file system is in use.
	 *
	 * @return true if the system is Windows
	 */
	static boolean isSystemWindows() {
		return SYSTEM_SEPARATOR == WINDOWS_SEPARATOR;
	}
	//-----------------------------------------------------------------------

	/**
	 * Checks if the character is a separator.
	 *
	 * @param ch the character to check
	 * @return true if it is a separator character
	 */
	private static boolean isSeparator(final char ch) {
		return ch == UNIX_SEPARATOR || ch == WINDOWS_SEPARATOR;
	}
//-----------------------------------------------------------------------

	/**
	 * Normalizes a path, removing double and single dot path steps.
	 * <p>
	 * This method normalizes a path to a standard format.
	 * The input may contain separators in either Unix or Windows format.
	 * The output will contain separators in the format of the system.
	 * <p>
	 * A trailing slash will be retained.
	 * A double slash will be merged to a single slash (but UNC names are handled).
	 * A single dot path segment will be removed.
	 * A double dot will cause that path segment and the one before to be removed.
	 * If the double dot has no parent path segment to work with, {@code null}
	 * is returned.
	 * <p>
	 * The output will be the same on both Unix and Windows except
	 * for the separator character.
	 * <pre>
	 * /foo//               --&gt;   /foo/
	 * /foo/./              --&gt;   /foo/
	 * /foo/../bar          --&gt;   /bar
	 * /foo/../bar/         --&gt;   /bar/
	 * /foo/../bar/../baz   --&gt;   /baz
	 * //foo//./bar         --&gt;   /foo/bar
	 * /../                 --&gt;   null
	 * ../foo               --&gt;   null
	 * foo/bar/..           --&gt;   foo/
	 * foo/../../bar        --&gt;   null
	 * foo/../bar           --&gt;   bar
	 * //server/foo/../bar  --&gt;   //server/bar
	 * //server/../bar      --&gt;   null
	 * C:\foo\..\bar        --&gt;   C:\bar
	 * C:\..\bar            --&gt;   null
	 * ~/foo/../bar/        --&gt;   ~/bar/
	 * ~/../bar             --&gt;   null
	 * </pre>
	 * (Note the file separator returned will be correct for Windows/Unix)
	 *
	 * @param filename the filename to normalize, null returns null
	 * @return the normalized filename, or null if invalid. Null bytes inside string will be removed
	 */
	public static String normalize(final String filename) {
		return doNormalize(filename, SYSTEM_SEPARATOR, true);
	}

	/**
	 * Normalizes a path, removing double and single dot path steps.
	 * <p>
	 * This method normalizes a path to a standard format.
	 * The input may contain separators in either Unix or Windows format.
	 * The output will contain separators in the format specified.
	 * <p>
	 * A trailing slash will be retained.
	 * A double slash will be merged to a single slash (but UNC names are handled).
	 * A single dot path segment will be removed.
	 * A double dot will cause that path segment and the one before to be removed.
	 * If the double dot has no parent path segment to work with, {@code null}
	 * is returned.
	 * <p>
	 * The output will be the same on both Unix and Windows except
	 * for the separator character.
	 * <pre>
	 * /foo//               --&gt;   /foo/
	 * /foo/./              --&gt;   /foo/
	 * /foo/../bar          --&gt;   /bar
	 * /foo/../bar/         --&gt;   /bar/
	 * /foo/../bar/../baz   --&gt;   /baz
	 * //foo//./bar         --&gt;   /foo/bar
	 * /../                 --&gt;   null
	 * ../foo               --&gt;   null
	 * foo/bar/..           --&gt;   foo/
	 * foo/../../bar        --&gt;   null
	 * foo/../bar           --&gt;   bar
	 * //server/foo/../bar  --&gt;   //server/bar
	 * //server/../bar      --&gt;   null
	 * C:\foo\..\bar        --&gt;   C:\bar
	 * C:\..\bar            --&gt;   null
	 * ~/foo/../bar/        --&gt;   ~/bar/
	 * ~/../bar             --&gt;   null
	 * </pre>
	 * The output will be the same on both Unix and Windows including
	 * the separator character.
	 *
	 * @param filename      the filename to normalize, null returns null
	 * @param unixSeparator {@code true} if a unix separator should
	 *                      be used or {@code false} if a windows separator should be used.
	 * @return the normalized filename, or null if invalid. Null bytes inside string will be removed
	 * @since 2.0
	 */
	public static String normalize(final String filename, final boolean unixSeparator) {
		final char separator = unixSeparator ? UNIX_SEPARATOR : WINDOWS_SEPARATOR;
		return doNormalize(filename, separator, true);
	}

	/**
	 * Internal method to perform the normalization.
	 *
	 * @param filename      the filename
	 * @param separator     The separator character to use
	 * @param keepSeparator true to keep the final separator
	 * @return the normalized filename. Null bytes inside string will be removed.
	 */
	private static String doNormalize(final String filename, final char separator, final boolean keepSeparator) {
		if (filename == null) {
			return null;
		}

		failIfNullBytePresent(filename);

		int size = filename.length();
		if (size == 0) {
			return filename;
		}
		final int prefix = getPrefixLength(filename);
		if (prefix < 0) {
			return null;
		}

		final char[] array = new char[size + 2];  // +1 for possible extra slash, +2 for arraycopy
		filename.getChars(0, filename.length(), array, 0);

		// fix separators throughout
		final char otherSeparator = separator == SYSTEM_SEPARATOR ? OTHER_SEPARATOR : SYSTEM_SEPARATOR;
		for (int i = 0; i < array.length; i++) {
			if (array[i] == otherSeparator) {
				array[i] = separator;
			}
		}

		// add extra separator on the end to simplify code below
		boolean lastIsDirectory = true;
		if (array[size - 1] != separator) {
			array[size++] = separator;
			lastIsDirectory = false;
		}

		// adjoining slashes
		for (int i = prefix + 1; i < size; i++) {
			if (array[i] == separator && array[i - 1] == separator) {
				System.arraycopy(array, i, array, i - 1, size - i);
				size--;
				i--;
			}
		}

		// dot slash
		for (int i = prefix + 1; i < size; i++) {
			if (array[i] == separator && array[i - 1] == '.' &&
					(i == prefix + 1 || array[i - 2] == separator)) {
				if (i == size - 1) {
					lastIsDirectory = true;
				}
				System.arraycopy(array, i + 1, array, i - 1, size - i);
				size -= 2;
				i--;
			}
		}

		// double dot slash
		outer:
		for (int i = prefix + 2; i < size; i++) {
			if (array[i] == separator && array[i - 1] == '.' && array[i - 2] == '.' &&
					(i == prefix + 2 || array[i - 3] == separator)) {
				if (i == prefix + 2) {
					return null;
				}
				if (i == size - 1) {
					lastIsDirectory = true;
				}
				int j;
				for (j = i - 4; j >= prefix; j--) {
					if (array[j] == separator) {
						// remove b/../ from a/b/../c
						System.arraycopy(array, i + 1, array, j + 1, size - i);
						size -= i - j;
						i = j + 1;
						continue outer;
					}
				}
				// remove a/../ from a/../c
				System.arraycopy(array, i + 1, array, prefix, size - i);
				size -= i + 1 - prefix;
				i = prefix + 1;
			}
		}

		if (size <= 0) {  // should never be less than 0
			return "";
		}
		if (size <= prefix) {  // should never be less than prefix
			return new String(array, 0, size);
		}
		if (lastIsDirectory && keepSeparator) {
			return new String(array, 0, size);  // keep trailing separator
		}
		return new String(array, 0, size - 1);  // lose trailing separator
	}

	/**
	 * Check the input for null bytes, a sign of unsanitized data being passed to to file level functions.
	 * <p>
	 * This may be used for poison byte attacks.
	 *
	 * @param path the path to check
	 */
	private static void failIfNullBytePresent(final String path) {
		final int len = path.length();
		for (int i = 0; i < len; i++) {
			if (path.charAt(i) == 0) {
				throw new IllegalArgumentException("Null byte present in file/path name. There are no " +
						"known legitimate use cases for such data, but several injection attacks may use it");
			}
		}
	}


	//-----------------------------------------------------------------------

	/**
	 * Returns the length of the filename prefix, such as <code>C:/</code> or <code>~/</code>.
	 * <p>
	 * This method will handle a file in either Unix or Windows format.
	 * <p>
	 * The prefix length includes the first slash in the full filename
	 * if applicable. Thus, it is possible that the length returned is greater
	 * than the length of the input string.
	 * <pre>
	 * Windows:
	 * a\b\c.txt           --&gt; ""          --&gt; relative
	 * \a\b\c.txt          --&gt; "\"         --&gt; current drive absolute
	 * C:a\b\c.txt         --&gt; "C:"        --&gt; drive relative
	 * C:\a\b\c.txt        --&gt; "C:\"       --&gt; absolute
	 * \\server\a\b\c.txt  --&gt; "\\server\" --&gt; UNC
	 * \\\a\b\c.txt        --&gt;  error, length = -1
	 *
	 * Unix:
	 * a/b/c.txt           --&gt; ""          --&gt; relative
	 * /a/b/c.txt          --&gt; "/"         --&gt; absolute
	 * ~/a/b/c.txt         --&gt; "~/"        --&gt; current user
	 * ~                   --&gt; "~/"        --&gt; current user (slash added)
	 * ~user/a/b/c.txt     --&gt; "~user/"    --&gt; named user
	 * ~user               --&gt; "~user/"    --&gt; named user (slash added)
	 * //server/a/b/c.txt  --&gt; "//server/"
	 * ///a/b/c.txt        --&gt; error, length = -1
	 * </pre>
	 * <p>
	 * The output will be the same irrespective of the machine that the code is running on.
	 * ie. both Unix and Windows prefixes are matched regardless.
	 * <p>
	 * Note that a leading // (or \\) is used to indicate a UNC name on Windows.
	 * These must be followed by a server name, so double-slashes are not collapsed
	 * to a single slash at the start of the filename.
	 *
	 * @param filename the filename to find the prefix in, null returns -1
	 * @return the length of the prefix, -1 if invalid or null
	 */
	public static int getPrefixLength(final String filename) {
		if (filename == null) {
			return NOT_FOUND;
		}
		final int len = filename.length();
		if (len == 0) {
			return 0;
		}
		char ch0 = filename.charAt(0);
		if (ch0 == ':') {
			return NOT_FOUND;
		}
		if (len == 1) {
			if (ch0 == '~') {
				return 2;  // return a length greater than the input
			}
			return isSeparator(ch0) ? 1 : 0;
		} else {
			if (ch0 == '~') {
				int posUnix = filename.indexOf(UNIX_SEPARATOR, 1);
				int posWin = filename.indexOf(WINDOWS_SEPARATOR, 1);
				if (posUnix == NOT_FOUND && posWin == NOT_FOUND) {
					return len + 1;  // return a length greater than the input
				}
				posUnix = posUnix == NOT_FOUND ? posWin : posUnix;
				posWin = posWin == NOT_FOUND ? posUnix : posWin;
				return Math.min(posUnix, posWin) + 1;
			}
			final char ch1 = filename.charAt(1);
			if (ch1 == ':') {
				ch0 = Character.toUpperCase(ch0);
				if (ch0 >= 'A' && ch0 <= 'Z') {
					if (len == 2 || isSeparator(filename.charAt(2)) == false) {
						return 2;
					}
					return 3;
				} else if (ch0 == UNIX_SEPARATOR) {
					return 1;
				}
				return NOT_FOUND;

			} else if (isSeparator(ch0) && isSeparator(ch1)) {
				int posUnix = filename.indexOf(UNIX_SEPARATOR, 2);
				int posWin = filename.indexOf(WINDOWS_SEPARATOR, 2);
				if (posUnix == NOT_FOUND && posWin == NOT_FOUND || posUnix == 2 || posWin == 2) {
					return NOT_FOUND;
				}
				posUnix = posUnix == NOT_FOUND ? posWin : posUnix;
				posWin = posWin == NOT_FOUND ? posUnix : posWin;
				return Math.min(posUnix, posWin) + 1;
			} else {
				return isSeparator(ch0) ? 1 : 0;
			}
		}
	}

	/**
	 * Normalizes a path, removing double and single dot path steps,
	 * and removing any final directory separator.
	 * <p>
	 * This method normalizes a path to a standard format.
	 * The input may contain separators in either Unix or Windows format.
	 * The output will contain separators in the format specified.
	 * <p>
	 * A trailing slash will be removed.
	 * A double slash will be merged to a single slash (but UNC names are handled).
	 * A single dot path segment will be removed.
	 * A double dot will cause that path segment and the one before to be removed.
	 * If the double dot has no parent path segment to work with, {@code null}
	 * is returned.
	 * <p>
	 * The output will be the same on both Unix and Windows including
	 * the separator character.
	 * <pre>
	 * /foo//               --&gt;   /foo
	 * /foo/./              --&gt;   /foo
	 * /foo/../bar          --&gt;   /bar
	 * /foo/../bar/         --&gt;   /bar
	 * /foo/../bar/../baz   --&gt;   /baz
	 * //foo//./bar         --&gt;   /foo/bar
	 * /../                 --&gt;   null
	 * ../foo               --&gt;   null
	 * foo/bar/..           --&gt;   foo
	 * foo/../../bar        --&gt;   null
	 * foo/../bar           --&gt;   bar
	 * //server/foo/../bar  --&gt;   //server/bar
	 * //server/../bar      --&gt;   null
	 * C:\foo\..\bar        --&gt;   C:\bar
	 * C:\..\bar            --&gt;   null
	 * ~/foo/../bar/        --&gt;   ~/bar
	 * ~/../bar             --&gt;   null
	 * </pre>
	 *
	 * @param filename      the filename to normalize, null returns null
	 * @param unixSeparator {@code true} if a unix separator should
	 *                      be used or {@code false} if a windows separator should be used.
	 * @return the normalized filename, or null if invalid. Null bytes inside string will be removed
	 * @since 2.0
	 */
	public static String normalizeNoEndSeparator(final String filename, final boolean unixSeparator) {
		final char separator = unixSeparator ? UNIX_SEPARATOR : WINDOWS_SEPARATOR;
		return doNormalize(filename, separator, false);
	}
	//-----------------------------------------------------------------------

	/**
	 * Concatenates a filename to a base path using normal command line style rules.
	 * <p>
	 * The effect is equivalent to resultant directory after changing
	 * directory to the first argument, followed by changing directory to
	 * the second argument.
	 * <p>
	 * The first argument is the base path, the second is the path to concatenate.
	 * The returned path is always normalized via {@link #normalize(String)},
	 * thus <code>..</code> is handled.
	 * <p>
	 * If <code>pathToAdd</code> is absolute (has an absolute prefix), then
	 * it will be normalized and returned.
	 * Otherwise, the paths will be joined, normalized and returned.
	 * <p>
	 * The output will be the same on both Unix and Windows except
	 * for the separator character.
	 * <pre>
	 * /foo/ + bar          --&gt;   /foo/bar
	 * /foo + bar           --&gt;   /foo/bar
	 * /foo + /bar          --&gt;   /bar
	 * /foo + C:/bar        --&gt;   C:/bar
	 * /foo + C:bar         --&gt;   C:bar (*)
	 * /foo/a/ + ../bar     --&gt;   foo/bar
	 * /foo/ + ../../bar    --&gt;   null
	 * /foo/ + /bar         --&gt;   /bar
	 * /foo/.. + /bar       --&gt;   /bar
	 * /foo + bar/c.txt     --&gt;   /foo/bar/c.txt
	 * /foo/c.txt + bar     --&gt;   /foo/c.txt/bar (!)
	 * </pre>
	 * (*) Note that the Windows relative drive prefix is unreliable when
	 * used with this method.
	 * (!) Note that the first parameter must be a path. If it ends with a name, then
	 * the name will be built into the concatenated path. If this might be a problem,
	 * use {@link #getFullPath(String)} on the base path argument.
	 *
	 * @param basePath          the base path to attach to, always treated as a path
	 * @param fullFilenameToAdd the filename (or path) to attach to the base
	 * @return the concatenated path, or null if invalid.  Null bytes inside string will be removed
	 */
	public static String concat(final String basePath, final String fullFilenameToAdd) {
		final int prefix = getPrefixLength(fullFilenameToAdd);
		if (prefix < 0) {
			return null;
		}
		if (prefix > 0) {
			return normalize(fullFilenameToAdd);
		}
		if (basePath == null) {
			return null;
		}
		final int len = basePath.length();
		if (len == 0) {
			return normalize(fullFilenameToAdd);
		}
		final char ch = basePath.charAt(len - 1);
		if (isSeparator(ch)) {
			return normalize(basePath + fullFilenameToAdd);
		} else {
			return normalize(basePath + '/' + fullFilenameToAdd);
		}
	}

	/**
	 * Gets the full path from a full filename, which is the prefix + path.
	 * <p>
	 * This method will handle a file in either Unix or Windows format.
	 * The method is entirely text based, and returns the text before and
	 * including the last forward or backslash.
	 * <pre>
	 * C:\a\b\c.txt --&gt; C:\a\b\
	 * ~/a/b/c.txt  --&gt; ~/a/b/
	 * a.txt        --&gt; ""
	 * a/b/c        --&gt; a/b/
	 * a/b/c/       --&gt; a/b/c/
	 * C:           --&gt; C:
	 * C:\          --&gt; C:\
	 * ~            --&gt; ~/
	 * ~/           --&gt; ~/
	 * ~user        --&gt; ~user/
	 * ~user/       --&gt; ~user/
	 * </pre>
	 * <p>
	 * The output will be the same irrespective of the machine that the code is running on.
	 *
	 * @param filename the filename to query, null returns null
	 * @return the path of the file, an empty string if none exists, null if invalid
	 */
	public static String getFullPath(final String filename) {
		return doGetFullPath(filename, true);
	}

	/**
	 * Gets the full path from a full filename, which is the prefix + path,
	 * and also excluding the final directory separator.
	 * <p>
	 * This method will handle a file in either Unix or Windows format.
	 * The method is entirely text based, and returns the text before the
	 * last forward or backslash.
	 * <pre>
	 * C:\a\b\c.txt --&gt; C:\a\b
	 * ~/a/b/c.txt  --&gt; ~/a/b
	 * a.txt        --&gt; ""
	 * a/b/c        --&gt; a/b
	 * a/b/c/       --&gt; a/b/c
	 * C:           --&gt; C:
	 * C:\          --&gt; C:\
	 * ~            --&gt; ~
	 * ~/           --&gt; ~
	 * ~user        --&gt; ~user
	 * ~user/       --&gt; ~user
	 * </pre>
	 * <p>
	 * The output will be the same irrespective of the machine that the code is running on.
	 *
	 * @param filename the filename to query, null returns null
	 * @return the path of the file, an empty string if none exists, null if invalid
	 */
	public static String getFullPathNoEndSeparator(final String filename) {
		return doGetFullPath(filename, false);
	}

	/**
	 * Does the work of getting the path.
	 *
	 * @param filename         the filename
	 * @param includeSeparator true to include the end separator
	 * @return the path
	 */
	private static String doGetFullPath(final String filename, final boolean includeSeparator) {
		if (filename == null) {
			return null;
		}
		final int prefix = getPrefixLength(filename);
		if (prefix < 0) {
			return null;
		}
		if (prefix >= filename.length()) {
			if (includeSeparator) {
				return getPrefix(filename);  // add end slash if necessary
			} else {
				return filename;
			}
		}
		final int index = indexOfLastSeparator(filename);
		if (index < 0) {
			return filename.substring(0, prefix);
		}
		int end = index + (includeSeparator ? 1 : 0);
		if (end == 0) {
			end++;
		}
		return filename.substring(0, end);
	}

	/**
	 * Gets the name minus the path from a full filename.
	 * <p>
	 * This method will handle a file in either Unix or Windows format.
	 * The text after the last forward or backslash is returned.
	 * <pre>
	 * a/b/c.txt --&gt; c.txt
	 * a.txt     --&gt; a.txt
	 * a/b/c     --&gt; c
	 * a/b/c/    --&gt; ""
	 * </pre>
	 * <p>
	 * The output will be the same irrespective of the machine that the code is running on.
	 *
	 * @param filename the filename to query, null returns null
	 * @return the name of the file without the path, or an empty string if none exists.
	 * Null bytes inside string will be removed
	 */
	public static String getName(final String filename) {
		if (filename == null) {
			return null;
		}
		failIfNullBytePresent(filename);
		final int index = indexOfLastSeparator(filename);
		return filename.substring(index + 1);
	}

	/**
	 * Returns the index of the last directory separator character.
	 * <p>
	 * This method will handle a file in either Unix or Windows format.
	 * The position of the last forward or backslash is returned.
	 * <p>
	 * The output will be the same irrespective of the machine that the code is running on.
	 *
	 * @param filename the filename to find the last path separator in, null returns -1
	 * @return the index of the last separator character, or -1 if there
	 * is no such character
	 */
	public static int indexOfLastSeparator(final String filename) {
		if (filename == null) {
			return NOT_FOUND;
		}
		final int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
		final int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
		return Math.max(lastUnixPos, lastWindowsPos);
	}
//-----------------------------------------------------------------------

	/**
	 * Gets the prefix from a full filename, such as <code>C:/</code>
	 * or <code>~/</code>.
	 * <p>
	 * This method will handle a file in either Unix or Windows format.
	 * The prefix includes the first slash in the full filename where applicable.
	 * <pre>
	 * Windows:
	 * a\b\c.txt           --&gt; ""          --&gt; relative
	 * \a\b\c.txt          --&gt; "\"         --&gt; current drive absolute
	 * C:a\b\c.txt         --&gt; "C:"        --&gt; drive relative
	 * C:\a\b\c.txt        --&gt; "C:\"       --&gt; absolute
	 * \\server\a\b\c.txt  --&gt; "\\server\" --&gt; UNC
	 *
	 * Unix:
	 * a/b/c.txt           --&gt; ""          --&gt; relative
	 * /a/b/c.txt          --&gt; "/"         --&gt; absolute
	 * ~/a/b/c.txt         --&gt; "~/"        --&gt; current user
	 * ~                   --&gt; "~/"        --&gt; current user (slash added)
	 * ~user/a/b/c.txt     --&gt; "~user/"    --&gt; named user
	 * ~user               --&gt; "~user/"    --&gt; named user (slash added)
	 * </pre>
	 * <p>
	 * The output will be the same irrespective of the machine that the code is running on.
	 * ie. both Unix and Windows prefixes are matched regardless.
	 *
	 * @param filename the filename to query, null returns null
	 * @return the prefix of the file, null if invalid. Null bytes inside string will be removed
	 */
	public static String getPrefix(final String filename) {
		if (filename == null) {
			return null;
		}
		final int len = getPrefixLength(filename);
		if (len < 0) {
			return null;
		}
		if (len > filename.length()) {
			failIfNullBytePresent(filename + UNIX_SEPARATOR);
			return filename + UNIX_SEPARATOR;
		}
		final String path = filename.substring(0, len);
		failIfNullBytePresent(path);
		return path;
	}

	/**
	 * Gets the path from a full filename, which excludes the prefix.
	 * <p>
	 * This method will handle a file in either Unix or Windows format.
	 * The method is entirely text based, and returns the text before and
	 * including the last forward or backslash.
	 * <pre>
	 * C:\a\b\c.txt --&gt; a\b\
	 * ~/a/b/c.txt  --&gt; a/b/
	 * a.txt        --&gt; ""
	 * a/b/c        --&gt; a/b/
	 * a/b/c/       --&gt; a/b/c/
	 * </pre>
	 * <p>
	 * The output will be the same irrespective of the machine that the code is running on.
	 * <p>
	 * This method drops the prefix from the result.
	 * See {@link #getFullPath(String)} for the method that retains the prefix.
	 *
	 * @param filename the filename to query, null returns null
	 * @return the path of the file, an empty string if none exists, null if invalid.
	 * Null bytes inside string will be removed
	 */
	public static String getPath(final String filename) {
		return doGetPath(filename, 1);
	}

	/**
	 * Does the work of getting the path.
	 *
	 * @param filename     the filename
	 * @param separatorAdd 0 to omit the end separator, 1 to return it
	 * @return the path. Null bytes inside string will be removed
	 */
	private static String doGetPath(final String filename, final int separatorAdd) {
		if (filename == null) {
			return null;
		}
		final int prefix = getPrefixLength(filename);
		if (prefix < 0) {
			return null;
		}
		final int index = indexOfLastSeparator(filename);
		final int endIndex = index + separatorAdd;
		if (prefix >= filename.length() || index < 0 || prefix >= endIndex) {
			return "";
		}
		final String path = filename.substring(prefix, endIndex);
		failIfNullBytePresent(path);
		return path;
	}

	/**
	 * Gets the path from a full filename, which excludes the prefix, and
	 * also excluding the final directory separator.
	 * <p>
	 * This method will handle a file in either Unix or Windows format.
	 * The method is entirely text based, and returns the text before the
	 * last forward or backslash.
	 * <pre>
	 * C:\a\b\c.txt --&gt; a\b
	 * ~/a/b/c.txt  --&gt; a/b
	 * a.txt        --&gt; ""
	 * a/b/c        --&gt; a/b
	 * a/b/c/       --&gt; a/b/c
	 * </pre>
	 * <p>
	 * The output will be the same irrespective of the machine that the code is running on.
	 * <p>
	 * This method drops the prefix from the result.
	 * See {@link #getFullPathNoEndSeparator(String)} for the method that retains the prefix.
	 *
	 * @param filename the filename to query, null returns null
	 * @return the path of the file, an empty string if none exists, null if invalid.
	 * Null bytes inside string will be removed
	 */
	public static String getPathNoEndSeparator(final String filename) {
		return doGetPath(filename, 0);
	}

	/**
	 * Gets the base name, minus the full path and extension, from a full filename.
	 * <p>
	 * This method will handle a file in either Unix or Windows format.
	 * The text after the last forward or backslash and before the last dot is returned.
	 * <pre>
	 * a/b/c.txt --&gt; c
	 * a.txt     --&gt; a
	 * a/b/c     --&gt; c
	 * a/b/c/    --&gt; ""
	 * </pre>
	 * <p>
	 * The output will be the same irrespective of the machine that the code is running on.
	 *
	 * @param filename the filename to query, null returns null
	 * @return the name of the file without the path, or an empty string if none exists. Null bytes inside string
	 * will be removed
	 */
	public static String getBaseName(final String filename) {
		return removeExtension(getName(filename));
	}

	//-----------------------------------------------------------------------

	/**
	 * Removes the extension from a filename.
	 * <p>
	 * This method returns the textual part of the filename before the last dot.
	 * There must be no directory separator after the dot.
	 * <pre>
	 * foo.txt    --&gt; foo
	 * a\b\c.jpg  --&gt; a\b\c
	 * a\b\c      --&gt; a\b\c
	 * a.b\c      --&gt; a.b\c
	 * </pre>
	 * <p>
	 * The output will be the same irrespective of the machine that the code is running on.
	 *
	 * @param filename the filename to query, null returns null
	 * @return the filename minus the extension
	 */
	public static String removeExtension(final String filename) {
		if (filename == null) {
			return null;
		}
		failIfNullBytePresent(filename);

		final int index = indexOfExtension(filename);
		if (index == NOT_FOUND) {
			return filename;
		} else {
			return filename.substring(0, index);
		}
	}

	/**
	 * Returns the index of the last extension separator character, which is a dot.
	 * <p>
	 * This method also checks that there is no directory separator after the last dot. To do this it uses
	 * {@link #indexOfLastSeparator(String)} which will handle a file in either Unix or Windows format.
	 * </p>
	 * <p>
	 * The output will be the same irrespective of the machine that the code is running on.
	 * </p>
	 *
	 * @param filename the filename to find the last extension separator in, null returns -1
	 * @return the index of the last extension separator character, or -1 if there is no such character
	 */
	public static int indexOfExtension(final String filename) {
		if (filename == null) {
			return NOT_FOUND;
		}
		final int extensionPos = filename.lastIndexOf(EXTENSION_SEPARATOR);
		final int lastSeparator = indexOfLastSeparator(filename);
		return lastSeparator > extensionPos ? NOT_FOUND : extensionPos;
	}

	/**
	 * Gets the extension of a filename.
	 * <p>
	 * This method returns the textual part of the filename after the last dot.
	 * There must be no directory separator after the dot.
	 * <pre>
	 * foo.txt      --&gt; "txt"
	 * a/b/c.jpg    --&gt; "jpg"
	 * a/b.txt/c    --&gt; ""
	 * a/b/c        --&gt; ""
	 * </pre>
	 * <p>
	 * The output will be the same irrespective of the machine that the code is running on.
	 *
	 * @param filename the filename to retrieve the extension of.
	 * @return the extension of the file or an empty string if none exists or {@code null}
	 * if the filename is {@code null}.
	 */
	public static String getExtension(final String filename) {
		if (filename == null) {
			return null;
		}
		final int index = indexOfExtension(filename);
		if (index == NOT_FOUND) {
			return "";
		} else {
			return filename.substring(index + 1);
		}
	}

	/**
	 * Splits a string into a number of tokens.
	 * The text is split by '?' and '*'.
	 * Where multiple '*' occur consecutively they are collapsed into a single '*'.
	 *
	 * @param text the text to split
	 * @return the array of tokens, never null
	 */
	static String[] splitOnTokens(final String text) {
		// used by wildcardMatch
		// package level so a unit test may run on this

		if (text.indexOf('?') == NOT_FOUND && text.indexOf('*') == NOT_FOUND) {
			return new String[]{text};
		}

		final char[] array = text.toCharArray();
		final ArrayList<String> list = new ArrayList<>();
		final StringBuilder buffer = new StringBuilder();
		char prevChar = 0;
		for (final char ch : array) {
			if (ch == '?' || ch == '*') {
				if (buffer.length() != 0) {
					list.add(buffer.toString());
					buffer.setLength(0);
				}
				if (ch == '?') {
					list.add("?");
				} else if (prevChar != '*') {// ch == '*' here; check if previous char was '*'
					list.add("*");
				}
			} else {
				buffer.append(ch);
			}
			prevChar = ch;
		}
		if (buffer.length() != 0) {
			list.add(buffer.toString());
		}

		return list.toArray(new String[list.size()]);
	}
}
