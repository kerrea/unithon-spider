import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import unithon.boot.io.files.NativeReader;
import unithon.boot.io.files.NativeWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class Cleaner {
    public static final JSONArray ROOT = new JSONArray();

    private static final ArrayList<String> BLACKLIST = new ArrayList<>();

    static {
        BLACKLIST.add("社区");
        BLACKLIST.add("学校");
        BLACKLIST.add("国");
        BLACKLIST.add("园区");
        BLACKLIST.add("街道");
        BLACKLIST.add("地区");
        BLACKLIST.add("高度");
        BLACKLIST.add("医院");
        BLACKLIST.add("力度");
        BLACKLIST.add("首都");
        BLACKLIST.add("周边");
        BLACKLIST.add("层");
        BLACKLIST.add("街道");
        BLACKLIST.add("中德");
        BLACKLIST.add("产业");
        BLACKLIST.add("开发");
        BLACKLIST.add("花园");
        BLACKLIST.add("遵守");
    }
    public static void main(String[] args) {
        String rawList = NativeReader.createFileReader("positions.json").getResult();
        JSONArray positionList = JSONArray.parseArray(rawList);
        List<String> pos = positionList.toJavaList(String.class);
        pos.sort(Comparator.comparingInt(String::length));
        List<String> result = new ArrayList<>();
        pos.forEach((s) -> {
            if (s.length() == 2) {
                if (!result.contains(s)) {
                    result.add(s);
                }
            } else if (s.length() == 3 || s.length() == 4) {
                if (!result.contains(s.substring(0, s.length() - 1))) {
                    result.add(s);
                } else {
                    if (result.contains(s)) {
                        result.add(s);
                    }
                }
            } else {
                if (s.length() != 1) {
                    result.add(s);
                }
            }
        });
        List<String> target = result.stream().filter((s) -> {
            AtomicBoolean condition = new AtomicBoolean();
            BLACKLIST.forEach((v) -> condition.set(condition.get() | s.contains(v)));
            return !condition.get();
        }).collect(Collectors.toList());
        ROOT.clear();
        ROOT.addAll(target);
        stop();
    }

    private static void read(JSONArray newsList){
        newsList.forEach((v) -> ROOT.addAll(((JSONObject) v).getJSONArray("positions")));
        JSONArray result = new JSONArray();
        ROOT.forEach((v) -> {
            if (!result.contains(v)) {
                assert v instanceof String;
                if (!result.contains(v)) {
                    result.add(v);
                }
            }
        });
        ROOT.clear();
        ROOT.addAll(result);
        stop();
    }

    private static void stop() {
        NativeWriter.createFileWriter("positions.json").add(ROOT.toString(SerializerFeature.PrettyFormat,SerializerFeature.SortField,SerializerFeature.MapSortField)).flush();
        Runtime.getRuntime().exit(0);
    }

}
