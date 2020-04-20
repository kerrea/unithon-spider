package unithon.worker;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import unithon.boot.Log;
import unithon.engines.JSONEngine;

import java.net.MalformedURLException;
import java.net.URL;

public class PiyaoSpider extends JSONEngine {

    public PiyaoSpider(int max) throws MalformedURLException {
        super("https://piyao.sina.cn/api/list/group?len=" + max, "piyao", max);
        urlFilter = (origin -> origin.getPath().endsWith(".html"));
    }

    @Override
    protected JSONObject parsePage(Document document) {
        try {
            News news = new News(document.getElementsByTag("h3").get(0).text());
            news.setDate(document.getElementsByClass("article_date").get(0).text());
            Elements elements = document.getElementsByClass("text");
            elements.forEach((e) -> news.addParagraph(e.text()));
            return news.toJSONObject();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void fillURLBucket(JSONObject src) {
        JSONObject data = src.getJSONObject("result").getJSONObject("data");
        data.forEach((k, v) -> {
            assert v instanceof JSONArray;
            JSONArray newsArray = (JSONArray) v;
            newsArray.forEach((news) -> {
                assert news instanceof JSONObject;
                String s = ((JSONObject) news).getString("url");
                s = s.replaceAll("\\\\/", "");
                try {
                    URL url = new URL(s);
                    if(urlFilter.doFilter(url)){
                        urls.push(url);
                    }
                } catch (MalformedURLException e) {
                    Log.e("Unable to parse '" + s + "' as url.");
                }
            });
        });
    }
}
