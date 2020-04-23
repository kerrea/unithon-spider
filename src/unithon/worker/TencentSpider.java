package unithon.worker;

import com.alibaba.fastjson.JSONObject;
import org.jsoup.nodes.Document;
import unithon.boot.Log;
import unithon.engines.JSONEngine;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class TencentSpider extends JSONEngine {

    /**
     * {@inheritDoc}
     */
    public TencentSpider() throws MalformedURLException {
        super("https://i.match.qq.com/tubdhotinterface?site=aiotwf&num=131&type=img&app=aio&child=news_news_antip", "tencent", 131);
    }

    @Override
    protected JSONObject parsePage(Document document) {
        try {
            document.removeClass("videoPlayerWrap");
            News news = new News(document.getElementsByTag("h1").get(0).text());
            document.getElementsByAttribute("name")
                    .forEach((element -> news.setDate(element.attr("apub:time"))));
            news.addParagraph(document.getElementsByClass("content-article").text());
            return news.toJSONObject();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void fillURLBucket(JSONObject src) {
        src.getJSONObject("data").getJSONArray("hot_data").forEach((object) -> {
            assert object instanceof JSONObject;
            String s = ((JSONObject) object).getString("url");
            s = s.replaceAll("\\\\/", "");
            try {
                urls.push(new URL(s));
            } catch (MalformedURLException e) {
                Log.e("Unable to parse '" + s + "' as url.");
            }
        });
    }

    @Override
    protected String download(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        Scanner scanner = new Scanner(connection.getInputStream(), Charset.forName("gb2312"));
        StringBuilder builder = new StringBuilder();
        while (scanner.hasNext()) {
            builder.append(scanner.next()).append(" ");
        }
        connection.disconnect();
        String src = builder.toString();
        return new String(src.getBytes(), StandardCharsets.UTF_8);
    }
}
