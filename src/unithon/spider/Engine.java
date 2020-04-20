package unithon.spider;

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
import java.net.URL;
import java.util.Date;
import java.util.Scanner;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Engine<T> implements Closeable {
    /**
     * output destination
     */
    private final File output;
    /**
     * spider start position
     */
    private final URL baseURL;
    /**
     * work service
     */
    private final ExecutorService download = Executors.newSingleThreadExecutor();
    /**
     * parse service
     */
    private final ExecutorService parse = Executors.newCachedThreadPool();
    /**
     * to mark max entry.
     */
    private final int maxEntry;
    /**
     *
     */
    private final Stack<Page> pages = new Stack<>();
    /**
     * news data
     */
    protected JSONArray entries = new JSONArray();
    /**
     * news arraylist
     */
    protected volatile Stack<URL> urls = new Stack<>();
    /**
     * used to filter anchor.
     * pass all anchors default.
     */
    protected Filter<URL> urlFilter = (origin) -> true;

    /**
     * @param baseURL  spider start url
     * @param name     spider name
     * @param maxEntry quantity limit of entry
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
            T base;
            try {
                base = init(baseURL);
            }catch (IOException e){
                Log.e(e);
                return;
            }
            // parse root
            fillURLBucket(base);
            while (!urls.isEmpty()) {
                if (entries.size() < maxEntry) {
                    parse.execute(() -> {
                        URL url = urls.pop();
                        synchronized (pages) {
                            try {
                                pages.add(new Page(url, fetchDocument(url)));
                            } catch (IOException e) {
                                Log.e(e);
                            }
                        }
                        pages.notifyAll();
                    });
                } else {
                    break;
                }
            }
            close();
        });
        while (!pages.isEmpty()) {
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
    protected final String download(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
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
    protected abstract JSONObject run(Document document);

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
