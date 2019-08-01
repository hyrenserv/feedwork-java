package fd.ng.core.yaml;

import fd.ng.core.exception.internal.RawlayerRuntimeException;
import fd.ng.core.utils.CodecUtil;
import fd.ng.core.yaml.lineproc.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation for {@link YamlReader}.
 */
final class YamlReaderImpl implements YamlReader {

    private InputStream source;

    YamlReaderImpl(final InputStream source) {
        this.source = source;
    }

    @Override
    public YamlMap asMap() {
        return new YamlMapAnywhere(this.readInput());
    }

    @Override
    public YamlArray asArray() {
        return new YamlArrayAnywhere(this.readInput());
    }

    /**
     * 逐行读配置文件，最终构造 DefaultYamlLinesProcessor 对象
     * @return YamlLines
     */
    private AbstractYamlLinesProcessor readInput() {
        final List<YamlLine> lines = new ArrayList<>();
        try (
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(this.source, CodecUtil.UTF8_STRING)
            )
        ) {
            String line;
            int number = 0;
            YamlLine previous = new YamlLine.NullYamlLine();
            while ((line = reader.readLine()) != null) {
                final YamlLine current = new NoCommentsYamlLine(
                    new DefaultYamlLine(line, number)
                );
                if(!current.trimmed().isEmpty()) {
                    lines.add(
                        new CachedYamlLine(
                            new WellIndentedLine(
                                previous,
                                new EvenlyIndentedLine(current)
                            )
                        )
                    ); // lines 目前，里面保存的每行数据是依然保留着注释的原始行数据。仅仅去掉了整行是注释的数据行
                    number++;
                    previous = current;
                }
            }
            return new DefaultYamlLinesProcessor(lines);
        } catch (IOException e) {
            throw new RawlayerRuntimeException(e);
        }
    }
}
