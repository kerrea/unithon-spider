package unithon.engines;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import unithon.boot.Log;
import unithon.boot.io.files.NativeWriter;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingDeque;

public abstract class Engine<T> implements Closeable {
    /**
     * output destination
     */
    private final File output;
    /**
     * to mark max entry.
     */
    private final int maxEntry;
    /**
     * news data
     */
    protected JSONArray entries = new JSONArray();
    /**
     * news arraylist
     */
    protected LinkedBlockingDeque<URL> urls = new LinkedBlockingDeque<>();
    /**
     * used to filter anchor.
     * pass all anchors default.
     */
    protected Filter<URL> urlFilter = (origin) -> true;
    /**
     * spider start position
     */
    private final URL baseURL;

    /**
     * @param baseURL  spider start url
     * @param name     spider name
     * @param maxEntry quantity limit of entry
     */
    public Engine(String baseURL, String name, int maxEntry) throws MalformedURLException {
        this.maxEntry = maxEntry;
        this.baseURL = new URL(baseURL);
        if (name.equals("")) {
            this.output = new File("spider-" + new Date().getTime() + ".json");
        } else {
            this.output = new File("spider-" + name + ".json");
        }
    }

    @Override
    public final void close() {
        NativeWriter.createFileWriter(output).add(entries.toJSONString()).flush();
    }

    /**
     * start to execute spider service
     */
    public final void work() {
        try {
            fillURLBucket(init(baseURL));
        } catch (IOException e) {
            Log.e(e);
            return;
        }
        while (!urls.isEmpty()) {
            // if limit is 500, will load 500 items into url entry.
            if (entries.size() < maxEntry) {
                URL url;
                try {
                    url = urls.pop();
                } catch (Exception e) {
                    return;
                }
                try {
                    Page page = new Page(url, fetchDocument(url));
                    JSONObject newsObject = parsePage(page.getDocument());
                    if (newsObject != null) {
                        entries.add(newsObject);
                    } else {
                        Log.w("content of " + page.getUrl() + "  parse failed.");
                    }
                } catch (IOException e) {
                    Log.e(e);
                }
            } else {
                break;
            }
        }
    }

    /**
     * a general html document fetch method.
     *
     * @param url origin
     * @return parsed document
     * @throws IOException io error occur
     */
    protected Document fetchDocument(URL url) throws IOException {
        return Jsoup.parse(download(url));
    }

    /**
     * download from target destination
     * @param url           destination
     * @return              downloaded string
     * @throws IOException  io error occur.
     */
    protected String download(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("Accept-Charset", "utf-8");
        connection.connect();
        Scanner scanner = new Scanner(connection.getInputStream());
        StringBuilder builder = new StringBuilder();
        while (scanner.hasNext()) {
            builder.append(scanner.next()).append(" ");
        }
        connection.disconnect();
        return builder.toString();
    }

    /**
     * initialize engine
     *
     * @param url base url
     * @return base document
     * @throws IOException  io error occur.
     */
    protected abstract T init(URL url)  throws IOException;

    /**
     * read url html data.
     *
     * @return json object
     */
    protected abstract JSONObject parsePage(Document document);

    /**
     * to parse all child node
     *
     * @param src url source
     */
    protected abstract void fillURLBucket(T src);

    /**
     * do basic filter to avoid {@code MalformedUrlException}
     *
     * @param url url string
     * @return true if it may be a valid url
     */
    protected boolean checkURL(String url) {
        return !url.startsWith("javascript") && !url.equals("") && !url.startsWith("#");
    }

    /**
     * a class to represent news data
     */
    protected static final class News {
        private final JSONArray paragraph = new JSONArray();
        private final JSONObject object = new JSONObject();

        public News(String title) {
            object.put("title", title);
        }

        public void setDate(String date) {
            object.put("date", date);
        }

        public void addParagraph(String entry) {
            paragraph.add(entry);
        }

        public JSONObject toJSONObject() {
            return object.fluentPut("paragraph", paragraph);
        }
    }

    protected static final class Page {

        private final Document document;
        private final URL url;

        public Page(URL url, Document document) {
            this.url = url;
            this.document = document;
        }

        public Document getDocument() {
            return document;
        }

        public URL getUrl() {
            return url;
        }
    }
}
