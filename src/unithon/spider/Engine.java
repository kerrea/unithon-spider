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
    private final ExecutorService download = Executors.newSingleThreadExecutor();
    /**
     * parse service
     */
    private final ExecutorService parse = Executors.newCachedThreadPool();
    /**
     * used to filter anchor.
     * pass all anchors default.
     */
    protected Filter<URL> filter = (origin) -> true;
    /**
     * to mark max entry.
     */
    private final int maxEntry;
    /**
     *
     */
    private final Stack<Page> pages = new Stack<>();

    /**
     * @param name spider name
     */
    public Engine(URL baseURL, String name, int maxEntry) {
        this.maxEntry = maxEntry;
        this.baseURL = baseURL;
        if (name.equals("")) {
            this.output = new File("spider-" + new Date().getTime() + ".json");
        } else {
            this.output = new File("spider-" + name + ".json");
        }
    }

    @Override
    public final void close() {
        download.shutdown();
        NativeWriter.createFileWriter(output).add(entries.toJSONString()).flush();
    }

    /**
     * start to execute spider service
     */
    public final void work() {
        download.execute(() -> {
            Document base = init(baseURL);
            // parse root
            fillURLBucket(base, filter);
            while (!urls.isEmpty()) {
                if (entries.size() < maxEntry) {
                    parse.execute(() -> {
                        URL url = urls.pop();
                        synchronized (pages) {
                            pages.add(new Page(url, fetchDocument(url)));
                        }
                        pages.notifyAll();
                    });
                } else {
                    break;
                }
            }
            close();
        });
        parse.execute(() -> {
            Page page;
            synchronized (pages) {
                page = pages.pop();
            }
            pages.notifyAll();
            JSONObject newsObject = run(page.getDocument());
            if (newsObject != null) {
                entries.add(newsObject);
            } else {
                Log.w("content of " + page.getUrl() + "  parse failed.");
            }
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
    protected synchronized void fillURLBucket(Document htmlDoc, Filter<URL> filter) {
        Elements anchors = htmlDoc.getElementsByTag("a");
        anchors.forEach((element -> {
            String href = element.attr("href");
            try {
                if (checkURL(href)) {
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

    /**
     * do basic filter to avoid {@code MalformedUrlException}
     *
     * @param url url string
     * @return true if it may be a valid url
     */
    private boolean checkURL(String url) {
        return !url.startsWith("javascript") && !url.equals("") && !url.startsWith("#");
    }

    private static final class Page {

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
