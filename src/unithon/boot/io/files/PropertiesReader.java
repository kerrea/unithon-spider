package unithon.boot.io.files;

import unithon.boot.Log;
import unithon.boot.io.uitils.FileSource;
import unithon.boot.io.uitils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * load properties.
 */
public final class PropertiesReader {

    private final String[] path;
    private File file;

    private PropertiesReader(File file) {
        this.file = file;
        this.path = file.getPath().split(File.separator);
    }

    private PropertiesReader(String... path) {
        this.path = path;
        file = null;
    }

    /**
     * create writer
     *
     * @param file destination
     * @return instance of writer
     */
    public static PropertiesReader createReader(File file) {
        return new PropertiesReader(file);
    }

    /**
     * create writer
     *
     * @param path destination
     * @return instance of writer
     */
    public static PropertiesReader createReader(String... path) {
        return new PropertiesReader(path);
    }

    public Properties getProperties(FileSource source) {
        Properties properties = new Properties();
        InputStream inputStream = null;
        try {
            switch (source) {
                case disk -> {
                    if (file == null) file = new File(IOUtils.compilePath(false, path));
                    inputStream = new FileInputStream(file);
                }
                case jar -> inputStream = NativeReader.createJarReader(path).getJarInputStream();
            }
            properties.load(inputStream);
        } catch (IOException | NullPointerException e) {
            Log.e(e);
        }
        return properties;
    }
}