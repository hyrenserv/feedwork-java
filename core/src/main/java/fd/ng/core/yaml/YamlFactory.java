package fd.ng.core.yaml;

import fd.ng.core.exception.internal.BaseInternalRuntimeException;
import fd.ng.core.exception.internal.FrameworkRuntimeException;
import fd.ng.core.exception.internal.RawlayerRuntimeException;
import fd.ng.core.utils.ClassUtil;
import fd.ng.core.yaml.builder.YamlArrayBuilder;
import fd.ng.core.yaml.builder.YamlMapBuilder;

import java.io.*;
import java.net.URL;

public final class YamlFactory {
    private YamlFactory(){}

    /**
     * Create a {@link YamlMapBuilder}.
     * @return Builder of YamlMap.
     */
    public static YamlMapBuilder getYamlMapBuilder() {
        return new YamlMapBuilder();
    }

    /**
     * Create a {@link YamlArrayBuilder}.
     * @return Builder of YamlMap.
     */
    public static YamlArrayBuilder getYamlArrayBuilder() {
        return new YamlArrayBuilder();
    }

    /**
     * Create a {@link YamlReader} from a File.
     * @param input File to read from.
     * @return YamlReader
     */
    public static YamlReader getYamlReader(final File input) {
        try {
            return YamlFactory.getYamlReader(new FileInputStream(input));
        } catch (FileNotFoundException e) {
            throw new RawlayerRuntimeException(e);
        }
    }

    /**
     * Create a {@link YamlReader} from a String.
     * @param input String to read from.
     * @return YamlReader
     */
    public static YamlReader getYamlReader(final String input) {
        return YamlFactory.getYamlReader(
            new ByteArrayInputStream(input.getBytes())
        );
    }

    /**
     * Create a {@link YamlReader} from a CLASSPATH file
     * @param classpath CLASSPATH file
     * @return YamlReader
     */
    public static YamlReader getYamlReaderByClasspath(final String classpath) {
        ClassLoader loader = ClassUtil.getClassLoader("yaml : " + classpath);
        URL url = loader.getResource(classpath);
        if (url == null) throw new YamlFileNotFoundException("Load yaml from '" + classpath + "' failed.");
        String protocol = url.getProtocol();
        if (!protocol.equals("file")) throw new RawlayerRuntimeException("Wrong protocol(="+protocol+") when loading yaml from '" + classpath + "'");
        File file = new File(url.getFile());
        return getYamlReader(file);
    }

    /**
     * Create a {@link YamlReader} from a CLASSPATH file
     * @param classpath CLASSPATH file
     * @return YamlReader
     */
    public static YamlReader load(final Object confFile) {
        if(confFile instanceof String)
            return getYamlReaderByClasspath((String)confFile);
        else if(confFile instanceof File)
            return getYamlReader((File)confFile);
        else
            throw new FrameworkRuntimeException("Unsupport conf file type : " + confFile);
    }

    /**
     * Create a {@link YamlReader} from an InputStream.
     * @param input InputStream to read from.
     * @return YamlReader
     */
    public static YamlReader getYamlReader(final InputStream input) {
        return new YamlReaderImpl(input);
    }

    public static class YamlFileNotFoundException extends BaseInternalRuntimeException {
        public YamlFileNotFoundException(final String msg) {
            super(msg);
        }
    }
}
