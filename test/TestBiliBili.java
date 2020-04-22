import unithon.Finalizer;
import unithon.worker.BiliBiliSpider;

import java.io.IOException;

public class TestBiliBili {
    public static void main(String[] args) throws IOException {
        BiliBiliSpider spider = new BiliBiliSpider(456664753, 0);
        Finalizer finalizer = Finalizer.getInstance();
        finalizer.addCloseable(spider);
        spider.open();
        finalizer.closeAll();
    }
}
