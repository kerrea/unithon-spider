package unithon;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.tag.Nature;

import com.hankcs.hanlp.seg.Segment;
import unithon.boot.io.files.FileHelper;
import unithon.boot.io.files.NativeReader;
import unithon.boot.io.files.NativeWriter;

import java.util.ArrayList;

/**
 * boot up code!
 */
public final class Invoker {
    private static final JSONArray ROOT = new JSONArray();

    public static void main(String[] args) {
        Segment segment = HanLP.newSegment();
        JSONObject analyzed = new JSONObject();
        JSONArray merge = JSONArray.parseArray(NativeReader.createFileReader("merged.json").getResult());
        merge.forEach((news) -> {
            assert news instanceof JSONObject;
            JSONObject object = (JSONObject) news;
            String title = object.getString("title");
            StringBuilder builder = new StringBuilder();
            JSONArray para = object.getJSONArray("paragraph");
            para.forEach((string) -> {
                assert string instanceof String;
                String str = (String) string;
                builder.append(str);
            });
            String content = builder.toString();
            JSONObject result = new JSONObject();
            String md5 = FileHelper.getMD5(title.getBytes());
            if (!analyzed.containsKey(md5)) {
                result.put("title", title);
                ArrayList<String> position = filter(segment, Nature.ns, content),
                        people = filter(segment, Nature.nr, content),
                        org = filter(segment, Nature.nt, content);
                ArrayList<String> entities = new ArrayList<>();
                entities.addAll(position);
                entities.addAll(people);
                entities.addAll(org);
                result.put("positions", position);
                result.put("people", people);
                result.put("organizations", org);
                result.put("entities", entities);
                result.put("content", content);
                analyzed.putIfAbsent(md5, result);
            }
        });
        // ---------------------------- output ------------------------------------------
        ArrayList<JSONObject> objects = new ArrayList<>();
        analyzed.forEach((k, v) -> {
            assert v instanceof JSONObject;
            objects.add((JSONObject) v);
        });
        ROOT.addAll(objects);
        stop();
    }

    private static ArrayList<String> filter(Segment segment, Nature nature, String content) {
        ArrayList<String> result = new ArrayList<>();
        segment.seg(content).stream()
                .filter((v) -> v.nature.equals(nature)).forEach((v) -> {
                    String word = v.word;
                    if (!result.contains(word)) {
                        result.add(v.word);
                    }
                }
        );
        return result;
    }

    private static void stop() {
        NativeWriter.createFileWriter("root.json").add(ROOT.toString()).flush();
        Runtime.getRuntime().exit(0);
    }
}
