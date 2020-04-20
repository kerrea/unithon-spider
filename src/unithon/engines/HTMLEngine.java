package unithon.engines;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import unithon.boot.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class HTMLEngine extends Engine<Document> {

    /**
     * {@inheritDoc}
     */
    public HTMLEngine(String baseURL, String name, int maxEntry) throws MalformedURLException {
        super(baseURL, name, maxEntry);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Document init(URL url) throws IOException {
        return fetchDocument(url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void fillURLBucket(Document document) {
        Elements anchors = document.getElementsByTag("a");
        anchors.forEach((element -> {
            String href = element.attr("href");
            try {
                if (checkURL(href)) {
                    URL origin = new URL(href);
                    if (super.urlFilter.doFilter(origin)) {
                        Log.i("'" + origin + "' added.");
                        urls.push(origin);
                    }
                }
            } catch (MalformedURLException e) {
                Log.e("'" + href + "' is not an valid url.");
            }
        }));
    }
}
