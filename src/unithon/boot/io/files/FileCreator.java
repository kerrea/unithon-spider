package unithon.boot.io.files;

import unithon.boot.Log;
import unithon.boot.io.uitils.FileType;
import unithon.boot.io.uitils.IOUtils;

import java.io.File;
import java.io.IOException;

public final class FileCreator {


    private final FileType type;
    private File target;

    private FileCreator(FileType type) {
        this.type = type;
    }

    public static FileCreator create(FileType type) {
        return new FileCreator(type);
    }

    public FileCreator setPath(String... path) {
        return setPath(IOUtils.compilePath(true, path));
    }

    public FileCreator setPath(String path) {
        return setPath(new File(path));
    }

    public FileCreator setPath(File file) {
        this.target = file;
        return this;
    }

    public File doCreate() {
        switch (type) {
            case File -> {
                try {
                    if (target.createNewFile()) Log.i(target.getName() + ": created.");
                } catch (IOException e) {
                    Log.e(e);
                }
            }
            case Dictionary -> {
                if (target.mkdir()) Log.i("Created dictionary " + target.getName());
            }
        }
        return target;
    }
}
