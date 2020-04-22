package unithon.worker;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import unithon.boot.io.files.NativeWriter;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

public class BiliBiliSpider implements Closeable {
    // if offset = 0. will pull the last dynamics
    private static final String API_DYNAMIC = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?host_uid={uid}&offset_dynamic_id={offset}";
    private static final String API_COMMENTS = "https://api.bilibili.com/x/v2/reply?pn={page}&type=1&oid={aid}&sort=2";
    private static final Pattern EXPRESSION_FILTER = Pattern.compile("\\[.+]");

    private String dynamicApi;
    private String commentApi;
    private String dynamicOffset;
    private String hostUID;
    private boolean hasMoreComment;
    private int pageNumber;
    private final long dynamicQuantity;
    private long currentPage = 0;
    private final JSONArray result = new JSONArray();

    /**
     * download json from target url
     *
     * @param url target
     * @return json parsed
     * @throws IOException io exception occur when download
     */
    private static JSONObject downloadJSON(URL url) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(url.openStream());
        String jsonString = new String(bufferedInputStream.readAllBytes());
        bufferedInputStream.close();
        return JSONObject.parseObject(jsonString);
    }

    public BiliBiliSpider(long uid, long quantity) {
        if (quantity != 0) {
            this.dynamicQuantity = quantity / 20;
        } else {
            this.dynamicQuantity = Long.MAX_VALUE;
        }
        this.hostUID = String.valueOf(uid);
        this.dynamicApi = API_DYNAMIC.replace("{uid}", this.hostUID)
                .replace("{offset}", "0");
    }

    public void open() throws IOException {
        JSONObject dynamic = downloadJSON(new URL(dynamicApi));
        JSONArray cards = dynamic.getJSONObject("data").getJSONArray("cards");
        cards.forEach((s) -> {
            assert s instanceof JSONObject;
            String cardRaw = ((JSONObject) s).getString("card").replaceAll("\\\\\"", "\"");
            String extendRaw = ((JSONObject) s).getString("extend_json").replaceAll("\\\\\"", "\"");
            JSONObject card = JSONObject.parseObject(cardRaw);
            JSONObject extend = JSONObject.parseObject(extendRaw);
            ((JSONObject) s).put("card", card);
            ((JSONObject) s).put("extend_json", extend);
        });
        NativeWriter.createFileWriter("dy.json").add(cards.toString(SerializerFeature.PrettyFormat)).flush();
    }

    private void parseDynamic(JSONObject card) {

    }

    private void parseComment(JSONObject comment) {

    }

    @Override
    public void close() {
        NativeWriter.createFileWriter("comments.json")
                .add(result.toString(SerializerFeature.PrettyFormat))
                .flush();
    }
}
