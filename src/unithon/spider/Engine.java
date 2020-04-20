package unithon.spider;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import unithon.boot.Log;
import unithon.boot.io.files.NativeWriter;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Engine implements Closeable {
    /**
     * output destination
     */
    private final File output;
    /**
     * spider start position
     */
    private final URL baseURL;
    /**
     * news data
     */
    protected JSONArray entries = new JSONArray();
    /**
     * news arraylist
     */
    protected volatile Stack<URL> urls = new Stack<>();
    /**
     * work service
     */
    protected ExecutorService executors = Executors.newSingleThreadExecutor();
    /**
     * used to filter anchor.
     * pass all anchors default.
     */
    protected AnchorFilter filter = (origin) -> true;

    /**
     * @param name spider name
     */
    public Engine(URL baseURL, String name) {
        this.baseURL = baseURL;
        if (name.equals("")) {
            this.output = new File("spider-" + new Date().getTime() + ".json");
        } else {
            this.output = new File("spider-" + name + ".json");
        }
    }

    @Override
    public final void close() {
        executors.shutdown();
        NativeWriter.createFileWriter(output).add(entries.toJSONString()).flush();
    }

    /**
     * start to execute spider service
     */
    public final void work() {
        executors.execute(() -> {
            Document base = init(baseURL);
            fillURLBucket(base, filter);
            int size = urls.size();
            while (!urls.isEmpty()) {
                URL url = urls.pop();
                Document document = fetchDocument(url);
                JSONObject newsObject = run(document);
                if (newsObject != null) {
                    entries.add(newsObject);
                    Log.i(urls.size() + "/" + size);
                } else {
                    Log.w("content of " + url + " parse failed.");
                }
            }
            close();
        });
    }

    /**
     * a general html document fetch method.
     *
     * @param url origin
     * @return parsed document
     */
    protected Document fetchDocument(URL url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            Scanner scanner = new Scanner(connection.getInputStream());
            StringBuilder documentString = new StringBuilder();
            while (scanner.hasNext()) {
                documentString.append(scanner.next()).append(" ");
            }
            connection.disconnect();
            String document = documentString.toString();
            return Jsoup.parse(document);
        } catch (IOException e) {
            Log.e(e);
            return null;
        }
    }

    /**
     * initialize engine
     *
     * @param url base url
     * @return base document
     */
    protected Document init(URL url) {
        return fetchDocument(url);
    }

    /**
     * read url html data.
     *
     * @return json object
     */
    protected abstract JSONObject run(Document document);

    /**
     * to parse all child node
     *
     * @param htmlDoc html document
     * @param filter  anchor filter rule
     */
    protected void fillURLBucket(Document htmlDoc, AnchorFilter filter) {
        Elements anchors = htmlDoc.getElementsByTag("a");
        anchors.forEach((element -> {
            String href = element.attr("href");
            try {
                if(checkURL(href)) {
                    URL origin = new URL(href);
                    if (filter.doFilter(origin)) {
                        Log.i("'" + origin + "' added.");
                        urls.push(origin);
                    }
                }
            } catch (MalformedURLException e) {
                Log.e("'" + href + "' is not an valid url.");
            }
        }));
    }

    /**
     * a class to represent news data
     */
    protected static class News {
        private final JSONArray paragraph = new JSONArray();
        private final JSONObject object = new JSONObject();

        protected News(String title) {
            object.put("title", title);
        }

        protected void setDate(String date) {
            object.put("date", date);
        }

        protected void addParagraph(String entry) {
            paragraph.add(entry);
        }

        protected JSONObject toJSONObject() {
            return object.fluentPut("paragraph", paragraph);
        }
    }

    private boolean checkURL(String s) {
        return !s.startsWith("javascript") && !s.equals("") && !s.startsWith("#");
    }
}
