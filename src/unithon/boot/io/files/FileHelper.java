package unithon.boot.io.files;

import unithon.boot.Log;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * utils for file
 */
public final class FileHelper {

    /**
     * get message digest.
     *
     * @param source byte source
     * @return Formatted byte string.
     */
    public static String getMD5(byte[] source) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Log.e(e);
            return "";
        }
        byte[] bytes = messageDigest.digest(source);
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            int x = ((int) b) & 0xff;
            String s = Integer.toHexString(x);
            if (s.length() == 1) {
                stringBuilder.append("0").append(s);
            } else {
                stringBuilder.append(s);
            }
        }
        return stringBuilder.toString();
    }

    public static void copyFile(File src, File dest) {
        NativeWriter.createFileWriter(dest).add(NativeReader.createFileReader(src).getData()).flush();
    }

    public static void moveFile(File src, File dest) {
        copyFile(src, dest);
        FileDeleter.delete(src);
    }
}