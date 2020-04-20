package unithon;

import unithon.worker.PiyaoSpider;

import java.net.MalformedURLException;
import java.util.Scanner;

/**
 * server boot up code!
 */
public final class Invoker {
    public static void main(String[] args) throws MalformedURLException {
        Finalizer finalizer = Finalizer.getInstance();
        /*
        finalizer.addCloseable(closeable);
        used to save data when exit.
        */
        PiyaoSpider piyaoSpider = new PiyaoSpider(10);
        finalizer.addCloseable(piyaoSpider);
        piyaoSpider.work();
        Scanner scanner = new Scanner(System.in);
    }
}
