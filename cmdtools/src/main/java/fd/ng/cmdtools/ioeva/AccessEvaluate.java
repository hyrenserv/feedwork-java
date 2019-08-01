package fd.ng.cmdtools.ioeva;

import fd.ng.core.utils.DateUtil;
import fd.ng.core.utils.FileUtil;
import fd.ng.core.utils.StringUtil;
import okio.*;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class AccessEvaluate {
	private final File file;
	private final String _colSep;

	// Jdkio方式下使用
	private final String startDate = DateUtil.getSysDate();
	private final String endDate = "99991231";
	String newColSep = ",";
	String newRowSep = "\n";
	// OKio方式下使用
	private byte[] newColSepOK = newColSep.getBytes(); // new byte[]{','};
	private byte[] newRowSepOK = newRowSep.getBytes(); //new byte[]{'\n'};
	private ByteString startDateOK = ByteString.encodeUtf8(startDate);
	private ByteString endDateOK = ByteString.encodeUtf8(endDate);

	/**
	 *
	 * @param filename 测试读写的文件名
	 */
	public AccessEvaluate(String filename) {
		this.file = new File(filename);
		this._colSep = "$$";
	}

	/**
	 *
	 * @param rows
	 * @param flushValue 多少行后执行 flush
	 * @param isOneFlushOneWrite 是否为：先把flushValue数量的数据用 sbuf 缓存并一次write
	 */
	public void writeByJdkio(final int rows, final int flushValue, boolean isOneFlushOneWrite) {
		System.out.println();
		System.out.printf("Jdkio Start writing ... ... by flushValue=%,d  OneFlushOneWrite=%s %n", flushValue, isOneFlushOneWrite);

		if(file.exists()) if(!file.delete()) throw new RuntimeException("Delete file failed : " + file.getName());

		long startTotal = System.currentTimeMillis();
		try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));){
			String datetime = DateUtil.getDateTime();
			StringBuilder lineBuf = new StringBuilder(256);
			long start = System.currentTimeMillis();
			for (int i = 1; i <= rows; i++) {
				genLine(i, datetime, lineBuf);
				if(isOneFlushOneWrite) {
					if(i%flushValue==0) {
						out.write(lineBuf.toString());
						lineBuf.delete(0,lineBuf.length());
						out.flush();
					}
				} else {
					out.write(lineBuf.toString());
					lineBuf.delete(0,lineBuf.length());
					if(i%flushValue==0) out.flush();
				}
			}
			if(isOneFlushOneWrite&&lineBuf.length()>0) out.write(lineBuf.toString());
			out.flush();
			long end = System.currentTimeMillis();
			System.out.printf("write data flush. total write : %,d, time=(%ds, %dms) %n", rows, (end-start)/1000, (end-start));
			System.out.printf("before close, file length=%,d %n", file.length());
		} catch (IOException e) {
			throw new RuntimeException(file.getName(), e);
		}
		long endTotal = System.currentTimeMillis();
		System.out.printf("write data over.  total write : %,d, time=(%ds, %dms) %n", rows, (endTotal-startTotal)/1000, (endTotal-startTotal));
		System.out.printf("after  close, file length=%,d %n", file.length());
	}

	public void writeByOkio(final int rows, final String opt) {
		System.out.println();
		System.out.printf("OKio  Start writing ... ... by type=%s %n", opt);

		try {
			if(file.exists()) if(!file.delete()) throw new RuntimeException("Delete file failed : " + file.getName());
			if(!file.createNewFile()) throw new RuntimeException("Create file failed : " + file.getName());
		} catch (IOException e) {
			throw new RuntimeException(file.getName(), e);
		}

		long startTotal = System.currentTimeMillis();
		try (BufferedSink sink = Okio.buffer(Okio.sink(file))) {
			String datetime = DateUtil.getDateTime();
			StringBuilder lineBuf = new StringBuilder(256);
			long start = System.currentTimeMillis();
			if("raw".equalsIgnoreCase(opt)) {
				for (int i = 1; i <= rows; i++) {
					writeLine(sink, i, datetime);
//					if(i%50000==0) sink.flush(); 这种方式性能没有区别
				}
			} else if("sbuf".equalsIgnoreCase(opt)) {
				for (int i = 1; i <= rows; i++) {
					genLine(i, datetime, lineBuf);
					sink.writeUtf8(lineBuf.toString());
					lineBuf.delete(0,lineBuf.length());
//					if(i%50000==0) sink.flush();
				}
			} else {
				throw new IllegalArgumentException("illegal arguments : opt="+opt);
			}
			sink.flush();
			long end = System.currentTimeMillis();
			System.out.printf("write data flush. total write : %,d, time=(%ds, %dms) %n", rows, (end-start)/1000, (end-start));
			System.out.printf("before close, file length=%,d %n", file.length());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		long endTotal = System.currentTimeMillis();
		System.out.printf("write data over.  total write : %,d, time=(%ds, %dms) %n", rows, (endTotal-startTotal)/1000, (endTotal-startTotal));
		System.out.printf("after  close, file length=%,d %n", file.length());
	}

	public void rpwByJdkio() {
		System.out.println();
		System.out.printf("Jdkio Start read and write ... ... %n");

		if (!file.exists()) throw new RuntimeException("file is not exist : " + file.getName());
		File newFile = new File(file.getAbsoluteFile()+".new"); // 如果传入文件路径有../这样的相对路径，那么应该使用 getCanonicalPath
		if(newFile.exists()) if(!newFile.delete()) throw new RuntimeException("Delete new file failed : " + newFile.getName());

		long startTotal = System.currentTimeMillis();
		try (BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newFile), "UTF-8"));){
			StringBuilder sbLine = new StringBuilder(1024);
			int i = 1;
			while (true) {
				sbLine.delete(0, sbLine.length());
				String lineStr = in.readLine();
				if(lineStr==null) break;
				List<String> cols = StringUtil.split(lineStr, _colSep);
				for(String col : cols) {
					sbLine.append(col).append(newColSep);
				}
				sbLine.append(startDate).append(newColSep).append(endDate).append(newColSep)
						.append(DigestUtils.md5Hex(lineStr)).append(newRowSep);
				out.write(sbLine.toString());
				if(i%50000==0) {
					out.flush();
				}
				i++;
			}
			out.flush();
		} catch (IOException e) {
			throw new RuntimeException(file.getName(), e);
		}
		long endTotal = System.currentTimeMillis();
		System.out.printf("read and write.  total time=(%ds, %dms) %n", (endTotal-startTotal)/1000, (endTotal-startTotal));
	}
	public void rpwByOkio() {
		System.out.println();
		System.out.printf("OKio  Start read and write ... ... %n");

		if (!file.exists()) throw new RuntimeException("file is not exist : " + file.getName());
		File newFile = new File(file.getAbsoluteFile()+".new"); // 如果传入文件路径有../这样的相对路径，那么应该使用 getCanonicalPath
		if(newFile.exists()) if(!newFile.delete()) throw new RuntimeException("Delete new file failed : " + newFile.getName());
		try {
			if (!newFile.createNewFile()) throw new RuntimeException("Create new file failed : " + newFile.getName());
		} catch (IOException e) {
			throw new RuntimeException(file.getName(), e);
		}

		ByteString colSep = ByteString.encodeUtf8(_colSep);
		final long colSepLen = colSep.size();
		final int bufferSize = 8 * 1024; // 256K
		long startTotal = System.currentTimeMillis();
		try (BufferedSource source = Okio.buffer(Okio.source(file));
		     BufferedSink sink = Okio.buffer(Okio.sink(newFile))) {
//			Buffer rBuffer = source.getBuffer();
			Buffer wBuffer = sink.getBuffer();
			while(true) {
				Buffer lineBuf = new Buffer();
				Buffer blkhole = new Buffer();

				final long LF_loc = source.indexOf((byte)'\n');
				long line_len = LF_loc;
				if(LF_loc==-1) line_len = 1; // 最后一行（没有\n的行）
				// TODO 还应该判断最后一行是否为空格、制表符行

				boolean isEOF = false;
				while (true) { // 找列分隔符，生成新行数据
					long colSep_loc = source.indexOf(colSep); // 列分隔符的位置
					if(colSep_loc==-1) { // 已经没有列分隔符，完成了整个文件的读取
						isEOF = true; // 为了让外面循环知道文件结束了
						break;
					} else { // 找到了列分隔符，则：写入 linebuf ；读一个列分隔符（为了让读取的指针移动过去）；继续循环找下一个
						long oneColLen = colSep_loc; // 因为指针在持续递增，所以这个找到列分隔符位置就是本来宽度
						if(LF_loc!=-1&&colSep_loc>line_len) // 列分隔符的位置大于上面找到的行分隔符的位置，说明本行读取结束
							oneColLen = line_len; // 本行剩余的字符串的长度现在等于持续递减的LF_loc
						source.read(lineBuf, oneColLen);   // 读一列并写入 lineBuf 中
						if(LF_loc!=-1) line_len = line_len - oneColLen - colSepLen; // read了两次，要把指针偏移减去
						if(line_len>0) {
							lineBuf.write(newColSepOK);

							final long bkhLen = source.read(blkhole, colSepLen); // bkhLen永远不可能为-1！
						} else {
							break;
						}
					}
				}
				if(isEOF) {
					final long LF = source.indexOf((byte)'\n'); // 文件最后一行是否为\n结尾
					if(LF==-1) source.readAll(lineBuf);
					else source.read(lineBuf, LF);
					ByteString md5 = lineBuf.md5();
					lineBuf.readAll(wBuffer);
					writeLineEnd(wBuffer, md5);
					break;
				}

				ByteString md5 = lineBuf.md5();
				lineBuf.readAll(wBuffer);
				writeLineEnd(wBuffer, md5);

				source.read(blkhole, 1); // 跳过结尾的\n
				blkhole.clear();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		long endTotal = System.currentTimeMillis();
		System.out.printf("read and write data over.  total time=(%ds, %dms) %n", (endTotal-startTotal)/1000, (endTotal-startTotal));
	}

	private void writeLineEnd(Buffer wBuffer, ByteString md5) {
		wBuffer
				.write(newColSepOK).write(startDateOK)
				.write(newColSepOK).write(endDateOK)
				.write(newColSepOK).writeUtf8(md5.hex())
				.write(newRowSepOK);
	}

	private void genLine(int i, String datetime, StringBuilder lineBuf) {
		lineBuf.append("name-").append(i).append(_colSep)
				.append(i).append(_colSep)
				.append(datetime).append(_colSep)
				.append("1").append(i).append(".").append(i%100).append(_colSep)
				.append(i%10).append(_colSep)
				.append("0000000000000000000000000000000000000000")
				.append("\n");
	}
	private void writeLine(BufferedSink sink, int i, String datetime) throws IOException {
		sink.writeUtf8("name-"+i).writeUtf8(_colSep)
				.writeUtf8(String.valueOf(i)).writeUtf8(_colSep)
				.writeUtf8(datetime).writeUtf8(_colSep)
				.writeUtf8("1").writeUtf8(String.valueOf(i)).writeUtf8(".").writeUtf8(Integer.toString(i%100)).writeUtf8(_colSep)
				.writeUtf8(Integer.toString(i%10)).writeUtf8(_colSep)
				.writeUtf8("0000000000000000000000000000000000000000")
				.writeUtf8("\n");
	}
}
