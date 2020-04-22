package unithon;

import unithon.worker.CNTVSpider;
import unithon.worker.PiyaoSpider;
import unithon.worker.TencentSpider;

import java.net.MalformedURLException;
import java.util.Date;

public class SpiderInvoker {
    public static void main(String[] args) throws MalformedURLException {
        Finalizer finalizer = Finalizer.getInstance();
        /*
            finalizer.addCloseable(closeable);
            used to save data when exit.
         */
        // --------------------- rumor breaker ------------------------
        {
            PiyaoSpider[] spiders = new PiyaoSpider[3];
            for (int i = 0; i < spiders.length; i++) {
                if (i == 0) {
                    spiders[0] = new PiyaoSpider(new Date().getTime() / 1000);
                    spiders[0].work();
                    finalizer.addCloseable(spiders[0]);
                } else {
                    spiders[i] = new PiyaoSpider(spiders[i - 1].getTime());
                    spiders[i].work();
                    finalizer.addCloseable(spiders[i]);
                }
            }
        }
        // -------------------------- cctv ----------------------------
        {
            CNTVSpider[] spiders = new CNTVSpider[7];
            for (int i = 0; i < spiders.length; i++) {
                spiders[i] = new CNTVSpider(i + 1);
                spiders[i].work();
                finalizer.addCloseable(spiders[i]);
            }
        }
        // -------------------------- qq ------------------------------
        {
            TencentSpider spider = new TencentSpider();
            spider.work();
            finalizer.addCloseable(spider);
        }
        // make sure close method will be called.
        finalizer.closeAll();
    }
}
