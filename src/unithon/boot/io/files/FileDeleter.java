package unithon.boot.io.files;

import unithon.boot.Log;
import unithon.boot.io.uitils.IOUtils;

import java.io.File;

public final class FileDeleter {

    /**
     * delete file at the dictionary.
     *
     * @param path file path.
     */
    public static void delete(String... path) {
        delete(IOUtils.compilePath(File.separator, path));
    }

    /**
     * delete file at the dictionary.
     *
     * @param path file path.
     */
    public static void delete(String path) {
        File file = new File(path);
        delete(file);
    }

    /**
     * delete file at the dictionary.
     *
     * @param file file object.
     */
    public static void delete(File file) {
        if (file.isFile()) {
            if (file.exists()) {
                executeDelete(file);
            }
        } else {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    delete(f);
                }
                executeDelete(file);
            }
        }
    }

    /**
     * do file
     *
     * @param file provided from caller
     */
    private static void executeDelete(File file) {
        if (file.delete()) {
            Log.i("File delete successfully.");
        } else {
            Log.w("File delete failed.");
        }
    }
}
