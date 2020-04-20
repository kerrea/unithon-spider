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
    private long time;

    public PiyaoSpider(long time) throws MalformedURLException {
        super("https://piyao.sina.cn/api/list/group?len=50&ptime=" + time, "piyao-" + time, 50);
        this.time = time;
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
                var j = (JSONObject) news;
                time = Math.min(j.getLong("ptime"),time);
                String s = j.getString("url");
                s = s.replaceAll("\\\\/", "");
                try {
                    URL url = new URL(s);
                    if (urlFilter.doFilter(url)) {
                        urls.push(url);
                    }
                } catch (MalformedURLException e) {
                    Log.e("Unable to parse '" + s + "' as url.");
                }
            });
        });
    }

    public long getTime() {
        return time;
    }
}
