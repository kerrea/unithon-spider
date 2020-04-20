package unithon.boot.io.files;

import unithon.boot.Log;
import unithon.boot.io.uitils.IOUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * read file from disk or jar file.
 */
public final class NativeReader {
    private final String path;
    private final boolean jar;
    private boolean read;
    private ClassLoader classLoader;
    private byte[] data;

    private NativeReader(String path, boolean jar) {
        this.path = path;
        this.jar = jar;
    }

    private NativeReader(File file) {
        jar = false;
        path = file.getPath();
    }

    /**
     * read from jar file
     *
     * @param path path in jar
     * @return reader object
     */
    public static NativeReader createJarReader(String... path) {
        return createJarReader(IOUtils.compilePath("/", path));
    }

    /**
     * read from jar file
     *
     * @param path path in jar
     * @return reader object
     */
    public static NativeReader createJarReader(String path) {
        NativeReader reader = new NativeReader(path, true);
        reader.classLoader = Thread.currentThread().getContextClassLoader();
        return reader;
    }

    /**
     * read from disk
     *
     * @param path data source
     * @return reader object
     */
    public static NativeReader createFileReader(String... path) {
        return createFileReader(IOUtils.compilePath(File.separator, path));
    }

    /**
     * read from disk
     *
     * @param path data source
     * @return reader object
     */
    public static NativeReader createFileReader(String path) {
        return new NativeReader(path, false);
    }

    /**
     * read from disk
     *
     * @param file data source
     * @return reader object
     */
    public static NativeReader createFileReader(File file) {
        return new NativeReader(file);
    }

    /**
     * do read operation.
     */
    private void read() {
        if (!read) {
            InputStream inputStream = null;
            if (jar) {
                inputStream = classLoader.getResourceAsStream(path);
            } else {
                try {
                    inputStream = new FileInputStream(path);
                } catch (FileNotFoundException e) {
                    Log.e(e);
                }
            }
            try {
                Objects.requireNonNull(inputStream);
            } catch (NullPointerException e) {
                Log.e(e);
                return;
            }
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                outputStream.write(bufferedInputStream.readAllBytes());
            } catch (IOException e) {
                Log.e(e);
            }
            data = outputStream.toByteArray();
            read = true;
        }
    }

    public InputStream getJarInputStream() {
        return classLoader.getResourceAsStream(path);
    }

    public byte[] getData() {
        read();
        return data;
    }

    /**
     * get raw result
     */
    public String getResult() {
        read();
        if (data == IOUtils.NULL_BYTE) {
            return "";
        }
        return new String(data);
    }

    /**
     * ger read result as string.
     *
     * @param charset target charset.
     * @return data string.
     */
    public String getResult(Charset charset) {
        read();
        if (data == IOUtils.NULL_BYTE) {
            return "";
        }
        return new String(data, charset);
    }
}
