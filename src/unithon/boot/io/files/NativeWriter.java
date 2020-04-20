package unithon.boot.io.files;

import unithon.boot.Log;
import unithon.boot.io.uitils.FileType;
import unithon.boot.io.uitils.IOUtils;

import java.io.*;
import java.nio.charset.Charset;

/**
 * write file to specified disk file.
 */
public final class NativeWriter {
    private final String path;
    private final ByteArrayOutputStream byteArrayOutputStream;
    private File file;
    private boolean append = false;

    private NativeWriter(String path) {
        byteArrayOutputStream = new ByteArrayOutputStream();
        this.path = path;
        file = null;
    }

    private NativeWriter(File file) {
        byteArrayOutputStream = new ByteArrayOutputStream();
        this.file = file;
        path = "";
    }

    /**
     * create writer
     *
     * @param file destination
     * @return instance of writer
     */
    public static NativeWriter createFileWriter(File file) {
        return new NativeWriter(file);
    }

    /**
     * create writer
     *
     * @param path destination
     * @return instance of writer
     */
    public static NativeWriter createFileWriter(String path) {
        return new NativeWriter(path);
    }

    /**
     * create writer
     *
     * @param path destination
     * @return instance of writer
     */
    public static NativeWriter createFileWriter(String... path) {
        return new NativeWriter(IOUtils.compilePath(File.separator, path));
    }

    /**
     * set whether need append writing.
     *
     * @param append true will write after existed file data.
     * @return instance of writer.
     */
    public NativeWriter setAppend(boolean append) {
        this.append = append;
        return this;
    }

    public FileOutputStream getFileOutputStream() {
        FileOutputStream outputStream;
        try {
            if (file == null) {
                file = new File(path);
                FileCreator.create(FileType.File)
                        .setPath(file)
                        .doCreate();
            }
            outputStream = new FileOutputStream(file, append);
            return outputStream;
        } catch (FileNotFoundException e) {
            Log.e(e);
            return null;
        }
    }

    /**
     * add byte[] array to stream.
     *
     * @param data you want to write
     * @return instance of writer
     */
    public NativeWriter add(byte[] data) {
        byteArrayOutputStream.writeBytes(data);
        return this;
    }

    /**
     * add string to stream.
     *
     * @param data string
     * @return instance of writer
     */
    public NativeWriter add(String data) {
        byteArrayOutputStream.writeBytes(data.getBytes());
        return this;
    }

    /**
     * add string to stream.
     *
     * @param data    original string
     * @param charset string decode charset
     * @return instance of writer
     */
    public NativeWriter add(String data, Charset charset) {
        byteArrayOutputStream.writeBytes(data.getBytes(charset));
        return this;
    }

    /**
     * write all data to file.
     */
    public void flush() {
        FileOutputStream outputStream = getFileOutputStream();
        try {
            this.byteArrayOutputStream.writeTo(outputStream);
        } catch (IOException e) {
            Log.e(e);
        }
    }
}