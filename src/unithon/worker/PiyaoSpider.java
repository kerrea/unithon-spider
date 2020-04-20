package unithon.worker;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.jsoup.nodes.Document;
import unithon.engines.JSONEngine;

import java.net.MalformedURLException;
import java.net.URL;

public class PiyaoSpider extends JSONEngine {

    public PiyaoSpider(int max) throws MalformedURLException {
        super("https://piyao.sina.cn/api/list/group?len=200", "piyao", max);
    }

    @Override
    protected JSONObject parsePage(Document document) {
        return null;
    }

    @Override
    protected void fillURLBucket(JSON src) {

    }
}
