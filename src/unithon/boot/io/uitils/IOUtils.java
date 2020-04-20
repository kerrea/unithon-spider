package unithon.boot.io.uitils;

import unithon.boot.io.files.FileCreator;

import java.io.File;

public class IOUtils {
    /**
     * mark io exception found.
     */
    public static final byte[] NULL_BYTE = new byte[0];

    /**
     * compile path
     *
     * @param separator file system path separator {@systemProperty file.separator}
     * @param path      path you need
     * @return compiled path.
     */
    public static String compilePath(String separator, String... path) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : path) {
            stringBuilder.append(s).append(separator);
        }
        String builder = stringBuilder.toString();
        builder = builder.substring(0, builder.lastIndexOf(separator));
        return builder;
    }

    /**
     * compile path
     *
     * @param create true will create path for each entry.
     *               example: /root/dir1/dir2/file1
     *               will create /root/
     *               then create /root/dir1/
     *               then create /root/dir1/dir2
     *               but won't create file.
     * @param path   path list
     * @return complied path
     */
    public static String compilePath(boolean create, String... path) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < path.length - 1; i++) {
            stringBuilder.append(path[i]);
            File file = new File(stringBuilder.toString());
            if (create) {
                FileCreator.create(FileType.Dictionary).setPath(file).doCreate();
            }
            stringBuilder.append(File.separator);
        }
        return stringBuilder.append(path[path.length - 1]).toString();
    }
}