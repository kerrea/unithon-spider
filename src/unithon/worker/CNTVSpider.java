package unithon.worker;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jsoup.nodes.Document;
import unithon.boot.Log;
import unithon.engines.JSONEngine;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public final class CNTVSpider extends JSONEngine {

    /**
     * {@inheritDoc}
     */
    public CNTVSpider(int i) throws MalformedURLException {
        super("http://news.cctv.com/2019/07/gaiban/cmsdatainterface/page/news_" + i + ".jsonp?cb=t&cb=news", "cntv" + i, 100);
    }

    @Override
    protected JSONObject init(URL url) throws IOException {
        String s = download(url).replace("news(", "");
        return JSON.parseObject(s.substring(0, s.lastIndexOf(")")));
    }

    @Override
    protected JSONObject parsePage(Document document) {
        try {
            News news = new News(document.getElementsByTag("h1").get(0).text());
            var info1 = document.getElementsByClass("info1").get(0).text();
            news.setDate(info1.substring(info1.indexOf("|") + 2));
            news.addParagraph(document.getElementsByClass("content_area").get(0).text());
            return news.toJSONObject();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void fillURLBucket(JSONObject src) {
        JSONArray data = src.getJSONObject("data").getJSONArray("list");
        data.forEach((entry) -> {
            assert entry instanceof JSONObject;
            JSONObject news = (JSONObject) entry;
            String url = news.getString("url");
            try {
                urls.push(new URL(url));
            } catch (MalformedURLException e) {
                Log.e("Unable to parse '" + url + "' as url.");
            }
        });
    }
}
