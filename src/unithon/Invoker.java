package unithon;

import unithon.worker.PiyaoSpider;

import java.net.MalformedURLException;
import java.util.Date;

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
        PiyaoSpider[] spiders = new PiyaoSpider[3];
        spiders[0] = new PiyaoSpider(new Date().getTime() / 1000);
        spiders[0].work();
        finalizer.addCloseable(spiders[0]);
        for (int i = 1; i < spiders.length; i++) {
            spiders[i] = new PiyaoSpider(spiders[i - 1].getTime());
            spiders[i].work();
            finalizer.addCloseable(spiders[i]);
        }
        // make sure close method will be called.
        finalizer.closeAll();
    }
}
