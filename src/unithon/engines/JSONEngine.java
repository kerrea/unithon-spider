package unithon.engines;

import com.alibaba.fastjson.JSON;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class JSONEngine extends Engine<JSON> {

    /**
     * {@inheritDoc}
     */
    public JSONEngine(String api, String name, int maxEntry) throws MalformedURLException {
        super(api, name, maxEntry);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JSON init(URL url) throws IOException {
        return JSON.parseObject(download(url));
    }
}
