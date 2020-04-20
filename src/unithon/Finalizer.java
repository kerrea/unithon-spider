package unithon;

import sun.misc.Signal;
import sun.misc.SignalHandler;
import unithon.boot.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.util.Stack;

final class Finalizer {
    private static final boolean IS_LINUX;
    private static Finalizer finalizer;

    static {
        IS_LINUX = !System.getProperties().getProperty("os.name").toLowerCase().startsWith("win");
    }

    private final Stack<Closeable> closeables = new Stack<>();

    private Finalizer() {
        Log.i("Attached.");
        SignalHandler shutdown = sig -> closeAll();
        attachSignal("INT", shutdown);
        if (IS_LINUX) {
            attachSignal("TERM", shutdown);
            attachSignal("HUP", sig -> {
                Log.i("Start background serving.");
                String name = ManagementFactory.getRuntimeMXBean().getName();
                String pid = name.split("@")[0];
                try {
                    File stop = new File("stop.sh");
                    if (stop.createNewFile()) {
                        // auto delete.
                        stop.deleteOnExit();
                    }
                    FileOutputStream fileOutputStream = new FileOutputStream(stop);
                    // stop server and delete itself
                    Log.i("Server pid:" + pid);
                    String content = "kill -15 " + pid + "\n";
                    fileOutputStream.write(content.getBytes(StandardCharsets.UTF_8));
                    fileOutputStream.close();
                    //noinspection ResultOfMethodCallIgnored
                    stop.setExecutable(true);
                } catch (IOException e) {
                    Log.e(e);
                }
            });
        }
    }


    static Finalizer getInstance() {
        if (finalizer == null) {
            finalizer = new Finalizer();
        }
        return finalizer;
    }

    private void attachSignal(String signal, SignalHandler handler) {
        Signal.handle(new Signal(signal), handler);
    }

    /**
     * all service must push to this method.
     *
     * @param closeable closeable.
     */
    public void addCloseable(Closeable closeable) {
        closeables.push(closeable);
    }

    void closeAll() {
        while (!closeables.empty()) {
            Closeable closeable = closeables.pop();
            try {
                closeable.close();
            } catch (IOException e) {
                Log.e(e);
            }
        }
        Runtime.getRuntime().exit(0);
    }
}