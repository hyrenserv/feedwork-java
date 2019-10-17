package fd.ng.core.utils;

import fd.ng.core.exception.internal.FrameworkRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.file.*;
import java.text.DecimalFormat;

/*
Path用于来表示文件路径和文件（和File对象类似），Path就是取代File的.
Path对象并不一定要对应一个实际存在的文件，它只是一个路径的抽象序列。可通过 Paths.get 创建Path对象。

快速方便的读写文件：
byte[] data = Files.readAllBytes(Paths.get("a.txt"));
List<String> lines = Files.readAllLines(Paths.get("a.txt"));
Files.write(Paths.get("b.txt"), "Hello JDK7!".getBytes());
默认情况Files类中的所有方法都会使用UTF-8编码进行操作

创建文件、目录
Files.createFile(path);
Files.createDirectory(path);
Files.createTempFile(dir, prefix, suffix);
Files.createTempFile(prefix, suffix);
Files.createTempDirectory(dir, prefix);
Files.createTempDirectory(prefix);

读取目录下文件：
Files.list
Files.walk

复制、移动文件
Files.copy(in, path);
Files.move(path, path);

删除一个文件
Files.delete(path);

遍历整个文件目录：
List<Path> result = new LinkedList<Path>();
Files.walkFileTree(startingDir, new FindJavaVisitor(result));
 */
public class FileUtil {
	private static final Logger logger = LogManager.getLogger(FileUtil.class.getName());
	private FileUtil() { throw new AssertionError("No FileUtil instances for you!"); }

	public static final char SHIT_PATH_SEPARATOR = '\\';
	public static final char LINUX_PATH_SEPARATOR = '/';
	public static final char PATH_SEPARATOR_CHAR = File.separatorChar;
	public static final String PATH_SEPARATOR = String.valueOf(PATH_SEPARATOR_CHAR);
	public static final char FILE_EXT_CHAR = '.';
	public static final String FILE_EXT = String.valueOf(FILE_EXT_CHAR);
	public static final String TEMP_DIR_NAME; // 系统临时目录名
	public static final File TEMP_DIR; // 系统临时目录对象

	public static final String[] FILESIZE_UNITS = new String[] { "B", "KB", "MB", "GB", "TB" };
	public static final String FILESIZE_ZERO = "0 B";
	public static final DecimalFormat FILESIZE_FORMAT = new DecimalFormat("#,##0.#");
	public static final long ONE_KB = 1024L;

	public static final Long ONE_KB_LONG = ONE_KB;
	public static final long ONE_MB = ONE_KB * ONE_KB;
	public static final Long ONE_MB_LONG = ONE_KB_LONG * ONE_KB_LONG;
	public static final long ONE_GB = ONE_KB * ONE_MB;
	public static final Long ONE_GB_LONG = ONE_KB_LONG * ONE_MB_LONG;
	public static final long ONE_TB = ONE_KB * ONE_GB;
	public static final Long ONE_TB_LONG = ONE_KB_LONG * ONE_GB_LONG;
	public static final long ONE_PB = ONE_KB * ONE_TB;
	public static final Long ONE_PB_LONG = ONE_KB_LONG * ONE_TB_LONG;

	private static final long FILE_COPY_BUFFER_SIZE = ONE_MB * 30;

	static {
		String tmpDir =  System.getProperty("java.io.tmpdir");
		if(tmpDir.charAt(tmpDir.length()-1) == PATH_SEPARATOR_CHAR) TEMP_DIR_NAME = tmpDir;
		else TEMP_DIR_NAME = tmpDir + PATH_SEPARATOR_CHAR;
		TEMP_DIR = new File(TEMP_DIR_NAME);
		if(!TEMP_DIR.exists()) throw new FrameworkRuntimeException("temp dir [] wrong, it's not exist.");
		else if(!TEMP_DIR.isDirectory()) throw new FrameworkRuntimeException("temp dir [] wrong, it's not Dir.");
	}

	/**
	 * 从一组名字中创建一个 File。
	 * 使用本方法可以避免因为不同操作系统的分隔符导致的问题。
	 * 例如： FileUtil.getFile("src", "main", "java"));
	 *
	 * @param names 名字
	 * @return the file
	 */
	public static File getFile(final String... names) {
		Validator.notNull(names);

		File file = null;
		for (final String name : names) {
			if (file == null) {
				file = new File(name);
			} else {
				file = new File(file, name);
			}
		}
		return file;
	}
	/**
	 * 基于一个父目录，从一组名字中创建一个 File。
	 * 使用本方法可以避免因为不同操作系统的分隔符导致的问题。
	 * 例如： FileUtil.getFile("src", "main", "java"));
	 *
	 * @param parentDirectory 父目录
	 * @param names 名字
	 * @return the file
	 */
	public static File getFile(final File parentDirectory, final String... names) {
		Validator.notNull(parentDirectory);
		Validator.notNull(names);

		File file = parentDirectory;
		for (final String name : names) {
			file = new File(file, name);
		}
		return file;
	}
	/**
	 * 将文件的字节转换为对应的大小
	 *
	 * @param size long 文件字节大小
	 * @return
	 */
	public static String fileSizeConversion(long size) {
		if( size <= 0 )
			return FILESIZE_ZERO;
		int digitGroups = (int)(Math.log10(size) / Math.log10(ONE_KB));
		return FILESIZE_FORMAT.format(size / Math.pow(ONE_KB, digitGroups)) + " " + FILESIZE_UNITS[digitGroups];
	}

	// 给传入的 pathName 追加文件分隔符
	public static String fixPathName(String pathName) {
		if(pathName.charAt(pathName.length()-1)==FileUtil.PATH_SEPARATOR_CHAR) return pathName;
		else return (pathName + FileUtil.PATH_SEPARATOR);
	}
	public static boolean createFileIfAbsent(String pathFilename, String content) {
		return createFileIfAbsent(pathFilename, content, CodecUtil.UTF8_CHARSET);
	}
	public static boolean createFileIfAbsent(String pathFilename, String content, Charset cs) {
		Path path = Paths.get(pathFilename);
		if(path.toFile().isDirectory()) {
			throw new FrameworkRuntimeException(String.format("Failed to create file because '%s' is a directory!", pathFilename));
		}
		if(Files.exists(path)) {
			logger.warn(String.format("No new file created because '%s' already exists!", pathFilename));
			return true;
		}
		else
			return createOrReplaceFile(pathFilename, content, cs);
	}

	/**
	 * 写文件，UTF8 编码
	 * @param pathFilename 文件全路径名
	 * @param content 文件内容
	 * @return 成功失败
	 */
	public static boolean createOrReplaceFile(String pathFilename, String content) {
		return createOrReplaceFile(pathFilename, content, CodecUtil.UTF8_CHARSET);
	}
	/**
	 * 使用指定的编码写文件
	 * @param pathFilename 文件全路径名
	 * @param cs Charset 指定编码
	 * @param content 文件内容
	 * @return 成功失败
	 */
	public static boolean createOrReplaceFile(String pathFilename, String content, Charset cs) {
		return createOrReplaceFile(pathFilename, content, cs.newEncoder());
	}
	/**
	 * 使用指定的编码器写文件。在大循环中写文件，可以在循环外面提前创建好编码器，或许能提高些效率
	 * @param pathFilename 文件全路径名
	 * @param encoder CharsetEncoder 指定编码器
	 * @param content 文件内容
	 * @return 成功失败
	 */
	public static boolean createOrReplaceFile(String pathFilename, String content, CharsetEncoder encoder) {
		Path filepath= Paths.get(pathFilename);
		if(!Files.exists(filepath)) {
			try {
				Files.createFile(filepath);
			} catch (IOException e) {
				logger.error("create file failed, filename=" + pathFilename, e);
				return false;
			}
		}
		try ( BufferedWriter bfw=newBufferedWriter(filepath, encoder) ) {
			bfw.write((content==null?StringUtil.EMPTY:content));
			bfw.flush();
			return true;
		} catch (IOException e) {
			logger.error("write file failed, filename=" + pathFilename, e);
			return false;
		}
	}

	/**
	 * 使用指定的编码器创建BufferedWriter。在大循环中写文件，可以在循环外面提前创建好编码器，或许能提高些效率
	 *
	 * @param path
	 * @param encoder
	 * @return
	 * @throws IOException
	 */
	public static BufferedWriter newBufferedWriter(Path path, CharsetEncoder encoder) throws IOException {
		return new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(path), encoder));
	}

	/**
	 * 拷贝文件到新位置。
	 * 新文件的日期设置为源文件相同。（但有可能设置失败）
	 * 如果目标文件所在目录不存在，则自动创建。
	 * 如果目标文件存在，则自动覆盖。
	 *
	 * @param srcFile  源文件
	 * @param destFile 目标文件
	 */
	public static void copyFile(final File srcFile, final File destFile) throws IOException {
		copyFile(srcFile, destFile, true);
	}

	/**
	 * 拷贝文件到新位置。
	 * 如果目标文件所在目录不存在，则自动创建。
	 * 如果目标文件存在，则自动覆盖。
	 *
	 * @param srcFile          源文件
	 * @param destFile         目标文件
	 * @param preserveFileDate 是否保留（维持）文件日期（有可能设置失败）
	 */
	public static void copyFile(final File srcFile, final File destFile, final boolean preserveFileDate) throws IOException {
		Validator.notNull(srcFile, "Source must not be null");
		Validator.notNull(destFile, "Destination must not be null");

		Validator.isFalse(srcFile.exists(), "Source '" + srcFile + "' does not exist");
		Validator.isTrue(srcFile.isDirectory(), "Source '" + srcFile + "' must not be directory");

		if (srcFile.getCanonicalPath().equals(destFile.getCanonicalPath())) {
			throw new IOException("Source '" + srcFile + "' and destination '" + destFile + "' are the same");
		}

		final File parentFile = destFile.getParentFile();
		if (parentFile != null) {
			if (!parentFile.mkdirs() && !parentFile.isDirectory()) {
				throw new IOException("Destination '" + parentFile + "' directory cannot be created");
			}
		}
		if (destFile.exists() && !destFile.canWrite()) {
			throw new IOException("Destination '" + destFile + "' exists but is read-only");
		}
		doCopyFile(srcFile, destFile, preserveFileDate);
	}

	/**
	 * 因为提前取得原始文件长度，在之后的分段拷贝过程中，如果输出文件长度与当前输入文件长度不同时会引发IOException。
	 * 也就是说，如果文件大小被改变了，可能造成失败。
	 *
	 * @param srcFile          源文件
	 * @param destFile         目标文件
	 * @param preserveFileDate 是否保留（维持）文件日期
	 * @throws IllegalArgumentException "Negative size" 如果文件被截断，导致其大小小于拷贝偏移
	 */
	private static void doCopyFile(final File srcFile, final File destFile, final boolean preserveFileDate) throws IOException {
		Validator.isTrue((destFile.exists() && destFile.isDirectory()), "Destination '" + destFile + "' exists but is a directory");

		try (FileInputStream fis = new FileInputStream(srcFile);
		     FileChannel input = fis.getChannel();
		     FileOutputStream fos = new FileOutputStream(destFile);
		     FileChannel output = fos.getChannel()) {
			final long size = input.size();
			long pos = 0;
			long count = 0;
			while (pos < size) {
				final long remain = size - pos;
				count = remain > FILE_COPY_BUFFER_SIZE ? FILE_COPY_BUFFER_SIZE : remain;
				final long bytesCopied = output.transferFrom(input, pos, count);
				if (bytesCopied == 0) { // 假如文件被截断
					logger.warn("Destination '" + destFile + "' transfer error. srcFile size={}, pos={}, count={}", size, pos, count);
					break; // 出错了跳出
				}
				pos += bytesCopied;
			}
		}

		final long srcLen = srcFile.length();
		final long dstLen = destFile.length();
		if (srcLen != dstLen) {
			throw new IOException("Failed to copy full contents from '" +
					srcFile + "' to '" + destFile + "' Expected length: " + srcLen + " Actual: " + dstLen);
		}
		if (preserveFileDate) {
			if(!destFile.setLastModified(srcFile.lastModified()))
				logger.warn("Destination '" + destFile + "' setLastModified failed.");
		}
	}

	/**
	 * 移动文件
	 *
	 * @param srcFile  源文件
	 * @param destFile 目标文件
	 */
	public static void moveFile(final File srcFile, final File destFile) throws IOException {
		Validator.notNull(srcFile, "Source must not be null");
		Validator.notNull(destFile, "Destination must not be null");

		Validator.isFalse(srcFile.exists(), "Source '" + srcFile + "' does not exist");
		Validator.isTrue(srcFile.isDirectory(), "Source '" + srcFile + "' is not a directory");

		Validator.isTrue(destFile.exists(), "Destination '" + destFile + "' already exists");

		final boolean rename = srcFile.renameTo(destFile);
		if (!rename) {
			copyFile(srcFile, destFile);
			if (!srcFile.delete()) {
				if(!destFile.delete())
					logger.error("Failed to delete destination file '" + destFile + "'");
				throw new IOException("Failed to delete original file '" + srcFile + "' after copy to '" + destFile + "'");
			}
		}
	}

	/**
	 * Cleans a directory without deleting it.
	 *
	 * @param directory directory to clean
	 * @throws IOException              in case cleaning is unsuccessful
	 * @throws IllegalArgumentException if {@code directory} does not exist or is not a directory
	 */
	public static void cleanDirectory(final File directory) throws IOException {
		Validator.isTrue(directory.exists(), directory + " does not exist");
		Validator.isTrue(directory.isDirectory(), directory + " is not a directory");

		final File[] files = getListFiles(directory);

		IOException exception = null;
		for (final File file : files) {
			try {
				forceDelete(file);
			} catch (final IOException ioe) {
				exception = ioe;
			}
		}

		if (null != exception) {
			throw exception;
		}
	}

	/**
	 * Deletes a file. If file is a directory, delete it and all sub-directories.
	 * <p>
	 * The difference between File.delete() and this method are:
	 * <ul>
	 * <li>A directory to be deleted does not have to be empty.</li>
	 * <li>You get exceptions when a file or directory cannot be deleted.
	 * (java.io.File methods returns a boolean)</li>
	 * </ul>
	 *
	 * @param file file or directory to delete, must not be {@code null}
	 * @throws NullPointerException  if the directory is {@code null}
	 * @throws FileNotFoundException if the file was not found
	 * @throws IOException           in case deletion is unsuccessful
	 */
	public static void forceDelete(final File file) throws IOException {
		if (file.isDirectory()) {
			deleteDirectory(file);
		} else {
			final boolean filePresent = file.exists();
			if (!file.delete()) {
				if (!filePresent) {
					throw new FileNotFoundException("File does not exist: " + file);
				}
				throw new IOException("Failed to delete file: " + file);
			}
		}
	}

	/**
	 * Deletes a directory recursively.
	 *
	 * @param directory directory to delete
	 * @throws IOException              in case deletion is unsuccessful
	 * @throws IllegalArgumentException if {@code directory} does not exist or is not a directory
	 */
	public static void deleteDirectory(final File directory) throws IOException {
		Validator.isTrue(directory.exists(), directory + " does not exist");
		Validator.isTrue(directory.isDirectory(), directory + " is not a directory");

		if (!isSymlink(directory))
			cleanDirectory(directory);

		if (!directory.delete())
			throw new IOException("Failed to delete directory: " + directory);
	}

	/**
	 * 仅删除目录下所有文件。
	 * @param dirName 要删除的目录名字
	 * @return true/false
	 */
	public static boolean deleteDirectoryFiles(String dirName) {
		return deleteDirectoryFiles(dirName, Integer.MAX_VALUE, null);
	}
	/**
	 * 仅删除目录下所有文件。
	 * 如果提供了filePatten，保留目录，仅删除匹配的文件
	 *
	 * @param dirName 要删除的目录名字
	 * @param filePatten 要删除的文件匹配规则：
	 *                   null：删除所有文件
	 *                   正则串： 保留目录，删除匹配的文件。例如：所有 .txt 文件为 (.*).txt
	 * @return true/false
	 */
	public static boolean deleteDirectoryFiles(String dirName, String filePatten) { return deleteDirectoryFiles(dirName, Integer.MAX_VALUE, filePatten); }
	/**
	 * 指定目录层级，递归删除目录里面的文件，保留目录。
	 * 如果提供了filePatten，保留目录，仅删除匹配的文件
	 *
	 * @param dirName 要删除的目录名字
	 * @param maxDepth 目录级数
	 * @param filePatten 要删除的文件匹配规则：
	 *                   null：删除所有文件
	 *                   正则串： 保留目录，删除匹配的文件。例如：所有 .txt 文件为 (.*).txt
	 * @return true/false
	 *
	 */
	public static boolean deleteDirectoryFiles(String dirName, int maxDepth, String filePatten) {
		Path path = Paths.get(dirName);
		if(!Files.isDirectory(path)) {
			logger.warn("FileUtil.deepDelete() Abort! Because of, [{}] is not dir!", dirName);
			return false;
		}

		try {
			final boolean[] deleteFlag = new boolean[]{true};
//			if(filePatten==null) { // 删除所有（包括各级目录）
//				Files.walk(path, maxDepth)
//						.sorted(Comparator.reverseOrder())  // 保证先处理文件
//						.map(Path::toFile)
//						.forEach(file -> {
//							if(!file.delete()) {
//								logger.error("Failed to delete file '{}'", file);
//								deleteFlag[0] = false;
//							}
//						});
//				return deleteFlag[0];
//			} else {
				// 仅删除符合匹配规则的文件，保留目录
				Files.walk(path, maxDepth).filter(Files::isRegularFile).map(Path::toFile)
						.forEach(file -> {
							if (filePatten==null || "*".equals(filePatten) || "*.*".equals(filePatten) || file.getName().matches(filePatten)) {
								if (!file.delete()) {
									logger.error("Failed to delete file '{}'", file);
									deleteFlag[0] = false;
								}
							}
						});
				return deleteFlag[0];
//			}
		} catch (IOException e) {
			logger.error("deleteDirectoryFiles() for Dir [" + dirName + "] IOException!", e);
			return false;
		}
	}

	/**
	 * Determines whether the specified file is a Symbolic Link rather than an actual file.
	 * <p>
	 * Will not return true if there is a Symbolic Link anywhere in the path,
	 * only if the specific file is.
	 * <p>
	 * When using jdk1.7, this method delegates to {@code boolean java.nio.file.Files.isSymbolicLink(Path path)}
	 *
	 * <b>Note:</b> the current implementation always returns {@code false} if running on
	 * jkd1.6 and the system is detected as Windows using {@link FileNameUtils#isSystemWindows()}
	 * <p>
	 * For code that runs on Java 1.7 or later, use the following method instead:
	 * <br>
	 * {@code boolean java.nio.file.Files.isSymbolicLink(Path path)}
	 * @param file the file to check
	 * @return true if the file is a Symbolic Link
	 */
	public static boolean isSymlink(final File file) {
		if (file == null) {
			throw new NullPointerException("File must not be null");
		}
		return Files.isSymbolicLink(file.toPath());
	}

	/**
	 * Lists files in a directory, asserting that the supplied directory satisfies exists and is a directory
	 * @param directory The directory to list
	 * @return The files in the directory, never null.
	 */
	private static File[] getListFiles(final File directory) throws IOException {
		final File[] files = directory.listFiles();
		if (files == null) {  // null if security restricted
			throw new IOException("Failed to get listFiles of " + directory);
		}
		return files;
	}

	/**
	 * Makes any necessary but nonexistent parent directories for a given File. If the parent directory cannot be
	 * created then an IOException is thrown.
	 *
	 * @param file file with parent to create, must not be {@code null}
	 * @throws NullPointerException if the file is {@code null}
	 * @throws IOException          if the parent directory cannot be created
	 */
	public static void forceMkdirParent(final File file) throws IOException {
		final File parent = file.getParentFile();
		if (parent == null) {
			return;
		}
		forceMkdir(parent);
	}

	/**
	 * Makes a directory, including any necessary but nonexistent parent
	 * directories. If a file already exists with specified name but it is
	 * not a directory then an IOException is thrown.
	 * If the directory cannot be created (or does not already exist)
	 * then an IOException is thrown.
	 *
	 * @param directory directory to create, must not be {@code null}
	 * @throws NullPointerException if the directory is {@code null}
	 * @throws IOException          if the directory cannot be created or the file already exists but is not a directory
	 */
	public static void forceMkdir(final File directory) throws IOException {
		if (directory.exists()) {
			if (!directory.isDirectory()) {
				final String message =
						"File "
								+ directory
								+ " exists and is "
								+ "not a directory. Unable to create directory.";
				throw new IOException(message);
			}
		} else {
			if (!directory.mkdirs()) {
				// Double-check that some other thread or process hasn't made
				// the directory in the background
				if (!directory.isDirectory()) {
					final String message =
							"Unable to create directory " + directory;
					throw new IOException(message);
				}
			}
		}
	}
}
