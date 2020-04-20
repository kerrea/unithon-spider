package unithon.spider;

import com.alibaba.fastjson.JSON;

import java.io.IOException;
import java.net.URL;

public abstract class JSONEngine extends Engine<JSON> {

    /**
     * {@inheritDoc}
     */
    public JSONEngine(URL baseURL, String name, int maxEntry) {
        super(baseURL, name, maxEntry);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JSON init(URL url) throws IOException {
        return JSON.parseObject(download(url));
    }
}
